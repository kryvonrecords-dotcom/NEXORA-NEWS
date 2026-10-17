interface Env {
  ASSETS: { fetch: (request: Request) => Promise<Response> };
}

const SOURCES = [
  { name: "G1", url: "https://g1.globo.com/rss/g1/", category: "Brasil" },
  {
    name: "Agência Brasil",
    url: "https://agenciabrasil.ebc.com.br/rss/ultimasnoticias/feed.xml",
    category: "Brasil"
  },
  {
    name: "CNN Brasil",
    url: "https://admin.cnnbrasil.com.br/feed/",
    category: "Brasil"
  },
  {
    name: "InfoMoney",
    url: "https://www.infomoney.com.br/feed/",
    category: "Economia"
  }
];

function decode(value: string): string {
  return value
    .replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, "$1")
    .replace(/&#x([0-9a-f]+);/gi, (_, n) =>
      String.fromCodePoint(parseInt(n, 16))
    )
    .replace(/&#(\d+);/g, (_, n) =>
      String.fromCodePoint(parseInt(n, 10))
    )
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/<[^>]*>/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

function field(xml: string, tag: string): string {
  const escaped = tag.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  const match = xml.match(
    new RegExp(`<${escaped}(?:\\s[^>]*)?>([\\s\\S]*?)<\\/${escaped}\\s*>`, "i")
  );
  return match ? decode(match[1]) : "";
}

function imageFrom(xml: string): string {
  const enclosure = xml.match(
    /<enclosure\b[^>]*url=["']([^"']+)["'][^>]*>/i
  );
  if (enclosure && /image/i.test(enclosure[0])) return enclosure[1];

  const media = xml.match(
    /<(?:media:content|media:thumbnail)\b[^>]*url=["']([^"']+)["'][^>]*>/i
  );
  if (media) return media[1];

  const html = xml.match(/<img\b[^>]*src=["']([^"']+)["']/i);
  return html ? html[1] : "";
}

async function readSource(source: typeof SOURCES[number]) {
  const response = await fetch(source.url, {
    headers: {
      "User-Agent": "NexoraNews/1.0 RSS Reader",
      "Accept": "application/rss+xml, application/xml, text/xml"
    },
    signal: AbortSignal.timeout(8000)
  });

  if (!response.ok) throw new Error(`${source.name}: HTTP ${response.status}`);

  const xml = await response.text();
  const items = [...xml.matchAll(/<(item|entry)\b[^>]*>([\s\S]*?)<\/\1\s*>/gi)];

  return items.slice(0, 30).map((match, index) => {
    const item = match[2];
    const title = field(item, "title");
    const description =
      field(item, "description") ||
      field(item, "summary") ||
      field(item, "content:encoded") ||
      field(item, "content");

    const linkTag = item.match(/<link\b[^>]*href=["']([^"']+)["'][^>]*\/?>/i);
    const link = field(item, "link") || (linkTag ? linkTag[1] : "");
    const date =
      field(item, "pubDate") ||
      field(item, "published") ||
      field(item, "updated") ||
      new Date().toISOString();

    if (!title || !link || !/^https?:\/\//i.test(link)) return null;

    return {
      id: `${source.name.toLowerCase().replace(/\W+/g, "-")}-${encodeURIComponent(link)}`,
      title,
      excerpt: description.slice(0, 500),
      content: description,
      imageUrl: imageFrom(item) || undefined,
      url: link,
      link,
      source: source.name,
      category: source.category,
      country: "Brasil",
      language: "pt-BR",
      publishedAt: date,
      isPublished: true,
      isDemo: false
    };
  }).filter(Boolean);
}

function json(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": "public, max-age=120",
      "Access-Control-Allow-Origin": "*"
    }
  });
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    if (url.pathname === "/api/health") {
      return json({ success: true, service: "Nexora News Brasil" });
    }

    if (url.pathname === "/api/news" ||
        url.pathname === "/api/news/breaking") {
      const results = await Promise.allSettled(SOURCES.map(readSource));
      let news = results
        .filter((result): result is PromiseFulfilledResult<any[]> =>
          result.status === "fulfilled"
        )
        .flatMap(result => result.value);

      news = news.filter((item, index, all) =>
        all.findIndex(other => other.link === item.link) === index
      );

      news.sort((a, b) =>
        Date.parse(b.publishedAt) - Date.parse(a.publishedAt)
      );

      if (url.pathname.endsWith("/breaking")) {
        return json({ success: true, data: [], news: [], total: 0 });
      }

      const search = url.searchParams.get("search")?.toLowerCase();
      if (search) {
        news = news.filter(item =>
          `${item.title} ${item.excerpt}`.toLowerCase().includes(search)
        );
      }

      const total = news.length;
      const offset = Math.max(0, Number(url.searchParams.get("offset")) || 0);
      const limit = Math.min(100, Math.max(1, Number(url.searchParams.get("limit")) || 20));
      news = news.slice(offset, offset + limit);

      return json({
        success: true,
        data: news,
        news,
        total,
        limit,
        offset,
        sources: SOURCES.map(source => source.name)
      });
    }

    if (url.pathname === "/api/categories") {
      return json({
        success: true,
        data: [
          { name: "Brasil", slug: "brasil" },
          { name: "Economia", slug: "economia" }
        ],
        categories: [
          { name: "Brasil", slug: "brasil" },
          { name: "Economia", slug: "economia" }
        ]
      });
    }

    return env.ASSETS.fetch(request);
  }
};
