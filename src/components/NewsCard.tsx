import React from 'react';
import { Calendar, Clock, Eye, Flame, Share2 } from 'lucide-react';
import { NewsItem } from '../types';
import { formatDate, formatTimeAgo, shareArticle } from '../lib/utils';

interface Props {
  key?: React.Key;
  news: NewsItem;
  variant?: 'standard' | 'horizontal' | 'compact' | 'featured';
  onNavigate: (path: string) => void;
}

export function NewsCard({ news, variant = 'standard', onNavigate }: Props) {
  const handleClick = () => {
    onNavigate(`/noticia/${news.slug}`);
  };

  const handleShare = async (e: React.MouseEvent) => {
    e.stopPropagation();
    const url = `${window.location.origin}/noticia/${news.slug}`;
    await shareArticle({ title: news.title, text: news.excerpt, url });
  };

  if (variant === 'compact') {
    return (
      <article 
        onClick={handleClick}
        className="group flex items-start gap-3 py-3 border-b border-slate-100 last:border-0 cursor-pointer"
      >
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <span className="text-[11px] font-bold text-[#146EF5] uppercase tracking-wider">
              {news.categoryName}
            </span>
            {news.isBreaking && (
              <span className="px-1.5 py-0.5 bg-red-600 text-white text-[10px] font-bold rounded">
                URGENTE
              </span>
            )}
          </div>
          <h4 className="text-sm font-semibold text-slate-900 line-clamp-2 group-hover:text-[#146EF5] transition-colors leading-snug">
            {news.title}
          </h4>
          <span className="text-[11px] text-slate-400 mt-1 block">
            {formatTimeAgo(news.publishedAt)}
          </span>
        </div>
        <img
          src={news.featuredImage}
          alt={news.title}
          loading="lazy"
          className="w-16 h-16 object-cover rounded-lg shrink-0 group-hover:opacity-90 transition-opacity bg-slate-100"
          referrerPolicy="no-referrer"
        />
      </article>
    );
  }

  if (variant === 'horizontal') {
    return (
      <article 
        onClick={handleClick}
        className="group bg-white rounded-xl border border-slate-200/80 p-4 sm:p-5 flex flex-col sm:flex-row gap-4 hover:shadow-md hover:border-slate-300 transition-all cursor-pointer"
      >
        <div className="sm:w-1/3 shrink-0 overflow-hidden rounded-lg relative aspect-video sm:aspect-auto">
          <img
            src={news.featuredImage}
            alt={news.title}
            loading="lazy"
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300 bg-slate-100 min-h-[160px]"
            referrerPolicy="no-referrer"
          />
          {news.isBreaking && (
            <span className="absolute top-2 left-2 px-2 py-0.5 bg-[#E53935] text-white text-[10px] font-black uppercase tracking-wider rounded shadow">
              URGENTE
            </span>
          )}
        </div>

        <div className="flex-1 flex flex-col justify-between min-w-0">
          <div>
            <div className="flex items-center justify-between gap-2 mb-2">
              <span className="text-xs font-bold text-[#146EF5] uppercase tracking-wider">
                {news.categoryName}
              </span>
              <button
                onClick={handleShare}
                className="text-slate-400 hover:text-[#146EF5] p-1 rounded hover:bg-slate-100 transition-colors"
                title="Partilhar notícia"
                aria-label="Partilhar notícia"
              >
                <Share2 className="w-3.5 h-3.5" />
              </button>
            </div>

            <h3 className="text-base sm:text-lg font-bold text-slate-900 group-hover:text-[#146EF5] transition-colors line-clamp-2 leading-snug">
              {news.title}
            </h3>

            <p className="text-xs sm:text-sm text-slate-600 line-clamp-2 mt-2 leading-relaxed">
              {news.excerpt}
            </p>
          </div>

          <div className="flex items-center gap-3 text-xs text-slate-400 mt-4 pt-3 border-t border-slate-100">
            <span className="font-medium text-slate-600">{news.authorName}</span>
            <span>•</span>
            <div className="flex items-center gap-1">
              <Clock className="w-3 h-3" />
              <span>{formatTimeAgo(news.publishedAt)}</span>
            </div>
            <span>•</span>
            <span>{news.readTimeMinutes} min</span>
          </div>
        </div>
      </article>
    );
  }

  // Standard Card
  return (
    <article 
      onClick={handleClick}
      className="group bg-white rounded-xl border border-slate-200/80 overflow-hidden flex flex-col hover:shadow-md hover:border-slate-300 transition-all cursor-pointer"
    >
      <div className="relative aspect-[16/10] overflow-hidden bg-slate-100">
        <img
          src={news.featuredImage}
          alt={news.title}
          loading="lazy"
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          referrerPolicy="no-referrer"
        />
        <div className="absolute top-3 left-3 flex items-center gap-1.5">
          <span className="px-2.5 py-1 bg-[#0B132B]/85 backdrop-blur-sm text-white text-[10px] font-black uppercase tracking-wider rounded-md">
            {news.categoryName}
          </span>
          {news.isBreaking && (
            <span className="px-2 py-1 bg-[#E53935] text-white text-[10px] font-black uppercase tracking-wider rounded-md flex items-center gap-1">
              <Flame className="w-2.5 h-2.5" />
              URGENTE
            </span>
          )}
        </div>
      </div>

      <div className="p-4 sm:p-5 flex-1 flex flex-col justify-between">
        <div>
          <h3 className="text-base font-bold text-slate-900 group-hover:text-[#146EF5] transition-colors line-clamp-2 leading-snug">
            {news.title}
          </h3>
          <p className="text-xs sm:text-sm text-slate-600 line-clamp-2 mt-2 leading-relaxed">
            {news.excerpt}
          </p>
        </div>

        <div className="flex items-center justify-between text-xs text-slate-400 mt-4 pt-3 border-t border-slate-100">
          <div className="flex items-center gap-2">
            <span>{formatTimeAgo(news.publishedAt)}</span>
            <span>•</span>
            <span>{news.readTimeMinutes} min</span>
          </div>
          <button
            onClick={handleShare}
            className="text-slate-400 hover:text-[#146EF5] p-1 rounded hover:bg-slate-50 transition-colors"
            title="Partilhar"
            aria-label="Partilhar"
          >
            <Share2 className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </article>
  );
}
