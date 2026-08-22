package com.nexoranews.app.ui.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nexoranews.app.MainActivity
import com.nexoranews.app.R
import com.nexoranews.app.data.model.Category
import com.nexoranews.app.data.model.NewsItem
import com.nexoranews.app.data.repository.NewsRepository
import com.nexoranews.app.databinding.FragmentHomeBinding
import com.nexoranews.app.ui.detail.ArticleDetailActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: NewsRepository
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var chipAdapter: CategoryChipAdapter

    private var currentCategory: String = "Todos"
    private var heroArticle: NewsItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        repository = NewsRepository(context)

        setupRecyclerViews(context)
        setupListeners()
        loadData(forceRefresh = false)
    }

    private fun setupRecyclerViews(context: Context) {
        // Category Chips
        chipAdapter = CategoryChipAdapter(emptyList(), currentCategory) { category ->
            currentCategory = category.name
            chipAdapter.setSelectedCategory(currentCategory)
            loadData(forceRefresh = false)
        }
        binding.rvCategoryChips.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = chipAdapter
        }

        // Recent News List
        newsAdapter = NewsAdapter(emptyList(), repository)
        binding.rvRecentNews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = newsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupListeners() {
        binding.swipeRefreshHome.setColorSchemeResources(R.color.primary_blue, R.color.accent_red)
        binding.swipeRefreshHome.setOnRefreshListener {
            loadData(forceRefresh = true)
        }

        binding.btnViewAllRecent.setOnClickListener {
            (activity as? MainActivity)?.navigateToNewsTab()
        }

        binding.layoutError.findViewById<View>(R.id.btnRetry)?.setOnClickListener {
            loadData(forceRefresh = true)
        }

        // Hero Card Click
        binding.root.findViewById<View>(R.id.cardHero)?.setOnClickListener {
            heroArticle?.let { article ->
                ArticleDetailActivity.start(requireContext(), article)
            }
        }
    }

    fun loadData(forceRefresh: Boolean = false) {
        if (!isAdded) return

        viewLifecycleOwner.lifecycleScope.launch {
            if (!binding.swipeRefreshHome.isRefreshing) {
                binding.layoutLoading.visibility = View.VISIBLE
            }
            binding.layoutError.visibility = View.GONE

            // 1. Fetch Categories
            val categoriesResult = repository.getCategories()
            val allCategory = Category(
                id = "cat-all",
                name = "Todos",
                slug = "todos",
                description = "Todas as notícias",
                color = "#146EF5",
                order = 0
            )
            val combinedCategories = listOf(allCategory) + categoriesResult.categories
            chipAdapter.updateCategories(combinedCategories, currentCategory)

            // 2. Fetch News
            val newsResult = repository.getNews(
                category = if (currentCategory == "Todos") null else currentCategory,
                forceRefresh = forceRefresh
            )

            binding.swipeRefreshHome.isRefreshing = false
            binding.layoutLoading.visibility = View.GONE

            // Notify activity about offline status banner
            (activity as? MainActivity)?.setOfflineBannerVisible(newsResult.isOffline)

            if (newsResult.news.isEmpty()) {
                binding.layoutContent.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
            } else {
                binding.layoutContent.visibility = View.VISIBLE
                binding.layoutError.visibility = View.GONE

                // Setup Hero
                val hero = newsResult.news.firstOrNull { it.isFeatured } ?: newsResult.news.first()
                heroArticle = hero
                setupHeroCard(hero)

                // Setup Breaking news ticker
                val breaking = newsResult.news.firstOrNull { it.isBreaking }
                if (breaking != null) {
                    binding.breakingNewsContainer.visibility = View.VISIBLE
                    binding.tvBreakingText.text = breaking.title
                    binding.breakingNewsContainer.setOnClickListener {
                        ArticleDetailActivity.start(requireContext(), breaking)
                    }
                } else {
                    binding.breakingNewsContainer.visibility = View.GONE
                }

                // Recent news (excluding hero to avoid duplication if in "Todos")
                val recentNews = if (currentCategory == "Todos") {
                    newsResult.news.filter { it.id != hero.id }
                } else {
                    newsResult.news
                }
                newsAdapter.updateNews(recentNews)
            }
        }
    }

    private fun setupHeroCard(hero: NewsItem) {
        val heroView = binding.root.findViewById<View>(R.id.cardHero) ?: return
        val ivHero = heroView.findViewById<android.widget.ImageView>(R.id.ivHeroImage)
        val tvCat = heroView.findViewById<android.widget.TextView>(R.id.tvHeroCategory)
        val tvRead = heroView.findViewById<android.widget.TextView>(R.id.tvHeroReadTime)
        val tvTitle = heroView.findViewById<android.widget.TextView>(R.id.tvHeroTitle)
        val tvMeta = heroView.findViewById<android.widget.TextView>(R.id.tvHeroMeta)

        tvCat?.text = hero.category.uppercase()
        tvRead?.text = "${hero.readTime} min de leitura"
        tvTitle?.text = hero.title
        val author = hero.author ?: "Redação Nexora"
        tvMeta?.text = "Por $author • Destaque Editorial"

        if (!hero.imageUrl.isNullOrBlank() && ivHero != null) {
            Glide.with(this)
                .load(hero.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.secondary_dark)
                .centerCrop()
                .into(ivHero)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
