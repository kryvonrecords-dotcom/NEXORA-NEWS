package com.nexoranews.app.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nexoranews.app.R
import com.nexoranews.app.data.model.NewsItem
import com.nexoranews.app.data.repository.NewsRepository
import com.nexoranews.app.databinding.ItemNewsCardBinding
import com.nexoranews.app.ui.detail.ArticleDetailActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsAdapter(
    private var newsList: List<NewsItem>,
    private val repository: NewsRepository,
    private val onItemClick: ((NewsItem) -> Unit)? = null
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    fun updateNews(newItems: List<NewsItem>) {
        this.newsList = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    override fun getItemCount(): Int = newsList.size

    inner class ViewHolder(private val binding: ItemNewsCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(news: NewsItem) {
            val context = binding.root.context

            binding.tvNewsCategory.text = news.category.uppercase(Locale.ROOT)
            binding.tvNewsReadTime.text = "${news.readTime} min"
            binding.tvNewsTitle.text = news.title
            binding.tvNewsSummary.text = news.summary ?: ""

            // Format author and published date
            val author = news.author ?: "Redação Nexora"
            val formattedDate = formatDate(news.publishedAt)
            binding.tvNewsAuthorAndDate.text = "$author • $formattedDate"

            // Set Category Tag Color
            val catColor = getCategoryColor(news.category)
            binding.tvNewsCategory.backgroundTintList = android.content.res.ColorStateList.valueOf(catColor)

            // Load Image with Glide
            if (!news.imageUrl.isNullOrBlank()) {
                Glide.with(context)
                    .load(news.imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.color.surface_variant)
                    .error(R.color.surface_variant)
                    .centerCrop()
                    .into(binding.ivNewsThumbnail)
            } else {
                binding.ivNewsThumbnail.setImageResource(R.color.surface_variant)
            }

            // Bookmark State
            updateBookmarkIcon(context, news.id)

            // Listeners
            binding.root.setOnClickListener {
                if (onItemClick != null) {
                    onItemClick.invoke(news)
                } else {
                    ArticleDetailActivity.start(context, news)
                }
            }

            binding.btnItemBookmark.setOnClickListener {
                val isBookmarked = repository.toggleBookmark(news)
                updateBookmarkIcon(context, news.id)
                val msg = if (isBookmarked) {
                    context.getString(R.string.action_bookmarked)
                } else {
                    context.getString(R.string.action_unbookmarked)
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

            binding.btnItemShare.setOnClickListener {
                shareArticle(context, news)
            }
        }

        private fun updateBookmarkIcon(context: Context, newsId: String) {
            val isSaved = repository.isBookmarked(newsId)
            if (isSaved) {
                binding.btnItemBookmark.setImageResource(R.drawable.ic_bookmark_filled)
                binding.btnItemBookmark.setColorFilter(ContextCompat.getColor(context, R.color.accent_amber))
            } else {
                binding.btnItemBookmark.setImageResource(R.drawable.ic_bookmark)
                binding.btnItemBookmark.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary))
            }
        }

        private fun formatDate(dateString: String?): String {
            if (dateString.isNullOrBlank()) return "Recente"
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = parser.parse(dateString) ?: Date()
                val formatter = SimpleDateFormat("d MMM", Locale("pt", "AO"))
                formatter.format(date)
            } catch (e: Exception) {
                "Recente"
            }
        }

        private fun getCategoryColor(category: String): Int {
            return when (category.lowercase(Locale.ROOT)) {
                "angola" -> Color.parseColor("#E53935")
                "política", "politica" -> Color.parseColor("#3B82F6")
                "economia" -> Color.parseColor("#10B981")
                "sociedade" -> Color.parseColor("#8B5CF6")
                "desporto" -> Color.parseColor("#F59E0B")
                "tecnologia" -> Color.parseColor("#06B6D4")
                "mundo" -> Color.parseColor("#6366F1")
                else -> Color.parseColor("#146EF5")
            }
        }

        private fun shareArticle(context: Context, news: NewsItem) {
            val shareUrl = "https://nexoranews.ao/noticia/${news.slug}"
            val shareText = context.getString(
                R.string.share_article_format,
                news.title,
                news.summary ?: "",
                shareUrl
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, news.title)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_article_title)))
        }
    }
}
