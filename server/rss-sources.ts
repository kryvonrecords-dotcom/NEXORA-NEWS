export interface RSSSource {
  name: string;
  url: string;
  category: string;
  country: string;
}

export const RSS_SOURCES: RSSSource[] = [
  {
    name: 'Africanews',
    url: 'https://www.africanews.com/feed/rss',
    category: 'africa',
    country: 'Africa'
  },
  {
    name: 'Africanews Français',
    url: 'https://fr.africanews.com/feed/rss',
    category: 'africa',
    country: 'Africa'
  },
  {
    name: 'AllAfrica Angola',
    url: 'https://allafrica.com/tools/headlines/rdf/angola/headlines.rdf',
    category: 'angola',
    country: 'Angola'
  },
  {
    name: 'AllAfrica Africa',
    url: 'https://allafrica.com/tools/headlines/rdf/africa/headlines.rdf',
    category: 'africa',
    country: 'Africa'
  },
  {
    name: 'AllAfrica Business',
    url: 'https://allafrica.com/tools/headlines/rdf/business/headlines.rdf',
    category: 'economia',
    country: 'Africa'
  },
  {
    name: 'AllAfrica Health',
    url: 'https://allafrica.com/tools/headlines/rdf/health/headlines.rdf',
    category: 'saude',
    country: 'Africa'
  },
  {
    name: 'AllAfrica Sport',
    url: 'https://allafrica.com/tools/headlines/rdf/sport/headlines.rdf',
    category: 'desporto',
    country: 'Africa'
  },
  {
    name: 'AllAfrica Entertainment',
    url: 'https://allafrica.com/tools/headlines/rdf/entertainment/headlines.rdf',
    category: 'entretenimento',
    country: 'Africa'
  },
  {
    name: 'RTP Notícias',
    url: 'https://www.rtp.pt/noticias/rss',
    category: 'portugal',
    country: 'Portugal'
  },
  {
    name: 'Renascença',
    url: 'https://rr.pt/rss',
    category: 'portugal',
    country: 'Portugal'
  },
  {
    name: 'Agência Brasil - Últimas Notícias',
    url: 'https://agenciabrasil.ebc.com.br/rss/ultimasnoticias/feed.xml',
    category: 'brasil',
    country: 'Brasil'
  },
  {
    name: 'Agência Brasil - Política',
    url: 'https://agenciabrasil.ebc.com.br/rss/politica/feed.xml',
    category: 'politica',
    country: 'Brasil'
  },
  {
    name: 'Agência Brasil - Economia',
    url: 'https://agenciabrasil.ebc.com.br/rss/economia/feed.xml',
    category: 'economia',
    country: 'Brasil'
  },
  {
    name: 'Agência Brasil - Educação',
    url: 'https://agenciabrasil.ebc.com.br/rss/educacao/feed.xml',
    category: 'educacao',
    country: 'Brasil'
  },
  {
    name: 'Agência Brasil - Saúde',
    url: 'https://agenciabrasil.ebc.com.br/rss/saude/feed.xml',
    category: 'saude',
    country: 'Brasil'
  },
  {
    name: 'Agência Brasil - Esportes',
    url: 'https://agenciabrasil.ebc.com.br/rss/esportes/feed.xml',
    category: 'desporto',
    country: 'Brasil'
  },
  {
    name: 'Agência Brasil - Internacional',
    url: 'https://agenciabrasil.ebc.com.br/rss/internacional/feed.xml',
    category: 'mundo',
    country: 'Mundo'
  }
];
