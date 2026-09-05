package com.nexoranews.app.ui.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.nexoranews.app.R
import com.nexoranews.app.data.model.NewsItem
import com.nexoranews.app.data.repository.NewsRepository
import com.nexoranews.app.databinding.ActivityArticleDetailBinding
import com.nexoranews.app.ui.home.NewsAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding
    private lateinit var repository: NewsRepository
    private var newsItem: NewsItem? = null
    private var newsSlug: String? = null

    companion object {
        private const val EXTRA_NEWS_ITEM = "extra_news_item"
        private const val EXTRA_NEWS_SLUG = "extra_news_slug"

        fun start(context: Context, news: NewsItem) {
            val intent = Intent(context, ArticleDetailActivity::class.java).apply {
                putExtra(EXTRA_NEWS_ITEM, news)
            }
            context.startActivity(intent)
        }

        fun startWithSlug(context: Context, slug: String) {
            val intent = Intent(context, ArticleDetailActivity::class.java).apply {
                putExtra(EXTRA_NEWS_SLUG, slug)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = NewsRepository(this)

        setupToolbar()
        extractIntentData()
        setupDetailBanner()
    }

    private fun setupDetailBanner() {
        try {
            val adRequest = AdRequest.Builder().build()
            binding.adViewDetail.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    binding.detailAdContainer.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    binding.detailAdContainer.visibility = View.GONE
                }
            }
            binding.adViewDetail.loadAd(adRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        binding.adViewDetail.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.adViewDetail.resume()
    }

    override fun onDestroy() {
        binding.adViewDetail.destroy()
        super.onDestroy()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.detailToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.detailToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun extractIntentData() {
        // Check if passed via Serializable
        val item = intent.getSerializableExtra(EXTRA_NEWS_ITEM) as? NewsItem
        if (item != null) {
            newsItem = item
            displayNews(item)
            loadRelatedNews(item)
            return
        }

        // Check if passed via Slug
        val slug = intent.getStringExtra(EXTRA_NEWS_SLUG)
        if (!slug.isNullOrBlank()) {
            newsSlug = slug
            loadNewsBySlug(slug)
            return
        }

        // Check deep link URL intent data
        val uri = intent.data
        if (uri != null) {
            val path = uri.path ?: ""
            val extractedSlug = path.substringAfterLast("/").trim()
            if (extractedSlug.isNotEmpty()) {
                newsSlug = extractedSlug
                loadNewsBySlug(extractedSlug)
                return
            }
        }

        Toast.makeText(this, "Notícia não encontrada", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun loadNewsBySlug(slug: String) {
        lifecycleScope.launch {
            val item = repository.getNewsBySlug(slug)
            if (item != null) {
                newsItem = item
                displayNews(item)
                loadRelatedNews(item)
            } else {
                Toast.makeText(this@ArticleDetailActivity, "Não foi possível carregar a notícia", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayNews(news: NewsItem) {
        binding.tvDetailTitle.text = news.title
        binding.tvDetailCategory.text = news.category.uppercase(Locale.ROOT)
        binding.tvDetailAuthor.text = news.author ?: "Redação Nexora"
        binding.tvDetailDate.text = formatFullDate(news.publishedAt)
        binding.tvDetailReadTime.text = "${news.readTime} min de leitura"

        // Category Color
        val catColor = getCategoryColor(news.category)
        binding.tvDetailCategory.backgroundTintList = android.content.res.ColorStateList.valueOf(catColor)

        // Featured Image
        if (!news.imageUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(news.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.secondary_dark)
                .error(R.color.secondary_dark)
                .centerCrop()
                .into(binding.ivDetailFeatured)
        }

        // Caption
        if (!news.imageCaption.isNullOrBlank()) {
            binding.tvDetailCaption.visibility = View.VISIBLE
            binding.tvDetailCaption.text = news.imageCaption
        } else {
            binding.tvDetailCaption.visibility = View.GONE
        }

        // Excerpt
        if (!news.summary.isNullOrBlank()) {
            binding.tvDetailExcerpt.visibility = View.VISIBLE
            binding.tvDetailExcerpt.text = news.summary
        } else {
            binding.tvDetailExcerpt.visibility = View.GONE
        }

        // Content
        val bodyContent = if (!news.content.isNullOrBlank()) {
            news.content
        } else {
            news.summary ?: "Conteúdo completo em actualização pela redacção do Nexora News."
        }
        binding.tvDetailContent.text = bodyContent

        // Bookmark Setup
        updateBookmarkButton(news.id)
        binding.btnDetailBookmark.setOnClickListener {
            val isBookmarked = repository.toggleBookmark(news)
            updateBookmarkButton(news.id)
            val msg = if (isBookmarked) {
                getString(R.string.action_bookmarked)
            } else {
                getString(R.string.action_unbookmarked)
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Share Buttons
        binding.btnDetailShare.setOnClickListener {
            shareArticle(news)
        }
        binding.btnBottomShare.setOnClickListener {
            shareArticle(news)
        }
    }

    private fun updateBookmarkButton(newsId: String) {
        val isSaved = repository.isBookmarked(newsId)
        if (isSaved) {
            binding.btnDetailBookmark.setImageResource(R.drawable.ic_bookmark_filled)
            binding.btnDetailBookmark.setColorFilter(ContextCompat.getColor(this, R.color.accent_amber))
        } else {
            binding.btnDetailBookmark.setImageResource(R.drawable.ic_bookmark)
            binding.btnDetailBookmark.setColorFilter(ContextCompat.getColor(this, R.color.white))
        }
    }

    private fun loadRelatedNews(currentNews: NewsItem) {
        lifecycleScope.launch {
            val result = repository.getNews(category = currentNews.category)
            val related = result.news.filter { it.id != currentNews.id }.take(3)
            if (related.isNotEmpty()) {
                binding.tvRelatedHeader.visibility = View.VISIBLE
                binding.rvRelatedNews.visibility = View.VISIBLE
                binding.rvRelatedNews.layoutManager = LinearLayoutManager(this@ArticleDetailActivity)
                binding.rvRelatedNews.adapter = NewsAdapter(related, repository) { clickedArticle ->
                    start(this@ArticleDetailActivity, clickedArticle)
                }
            } else {
                binding.tvRelatedHeader.visibility = View.GONE
                binding.rvRelatedNews.visibility = View.GONE
            }
        }
    }

    private fun shareArticle(news: NewsItem) {
        val shareUrl = "https://nexora-news.onrender.com/noticia/${news.slug}"
        val shareText = getString(
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
        startActivity(Intent.createChooser(intent, getString(R.string.share_article_title)))
    }

    private fun formatFullDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Recente"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = parser.parse(dateString) ?: Date()
            val formatter = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("pt", "AO"))
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
}
