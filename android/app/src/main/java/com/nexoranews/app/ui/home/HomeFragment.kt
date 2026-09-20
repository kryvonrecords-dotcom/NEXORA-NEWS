package com.nexoranews.app.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nexoranews.app.MainActivity
import com.nexoranews.app.R
import com.nexoranews.app.data.model.Advertisement
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
    private var currentAds: List<Advertisement> = emptyList()
    private var currentAdIndex: Int = 0
    private val adRotationHandler = Handler(Looper.getMainLooper())
    private val adRotationRunnable = object : Runnable {
        override fun run() {
            if (currentAds.size > 1 && isAdded && _binding != null) {
                currentAdIndex = (currentAdIndex + 1) % currentAds.size
                displayAd(currentAds[currentAdIndex])
                adRotationHandler.postDelayed(this, 8000)
            }
        }
    }

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
        // loadSocialMedia() // TEMPORARIAMENTE DESATIVADO PARA TESTE

        setupRecyclerViews(context)
        setupListeners()
        loadAd()
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

        binding.btnWhatsappFloating.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/244921281315"))
                startActivity(intent)
            } catch (_: Exception) {
            }
        }
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

    private fun loadAd() {
        // O espaço do banner permanece sempre reservado.
        binding.adBannerContainer.root.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getAds("top_hero")

            result.onSuccess { ads ->
                val newAds = ads
                    .filter { it.status.equals("active", ignoreCase = true) }
                    .sortedBy { it.order }

                if (newAds.isNotEmpty()) {
                    currentAds = newAds
                    currentAdIndex = 0

                    displayAd(currentAds[currentAdIndex])
                    adRotationHandler.removeCallbacks(adRotationRunnable)

                    if (currentAds.size > 1) {
                        adRotationHandler.postDelayed(adRotationRunnable, 8000)
                    }
                }
                // Não esconder o banner quando não houver anúncio temporariamente.
            }.onFailure {
                // Mantém o banner visível durante falhas temporárias da API.
                binding.adBannerContainer.root.visibility = View.VISIBLE
            }
        }
    }

    private fun displayAd(ad: Advertisement) {
        val root = binding.adBannerContainer.root

        root.visibility = View.VISIBLE

        val image = root.findViewById<android.widget.ImageView>(R.id.ivAdImage)
        val button = root.findViewById<android.widget.TextView>(R.id.tvAdButton)

        button.text = ad.callToAction.ifBlank { "Acessar Agora" }

        val imageUrl = if (ad.mediaUrl.startsWith("/")) "https://nexora-news.nexoranews.blitz.cloud${ad.mediaUrl}" else ad.mediaUrl
        if (imageUrl.isNotBlank()) {
            Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.secondary_dark)
                .centerCrop()
                .into(image)
        }

        val openLink = View.OnClickListener {
            if (ad.linkUrl.isNotBlank()) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ad.linkUrl)))
                } catch (_: Exception) {
                }
            }
        }

        root.setOnClickListener(openLink)
        button.setOnClickListener(openLink)
    }

    fun loadData(forceRefresh: Boolean = false) {
        if (!isAdded) return

        viewLifecycleOwner.lifecycleScope.launch {
            if (!binding.swipeRefreshHome.isRefreshing) {
                binding.layoutLoading.visibility = View.VISIBLE
            }
            binding.layoutError.visibility = View.GONE

            // Primeiro mostra o conteúdo local sem esperar pela internet.
            val newsResult = repository.getNews(
                category = if (currentCategory == "Todos") null else currentCategory,
                forceRefresh = forceRefresh
            )

            if (!isAdded) return@launch

            binding.swipeRefreshHome.isRefreshing = false
            binding.layoutLoading.visibility = View.GONE

            // Não mostrar falso aviso de "sem internet" por falha da API.
            (activity as? MainActivity)?.setOfflineBannerVisible(false)

            if (newsResult.news.isEmpty()) {
                binding.layoutContent.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
            } else {
                binding.layoutContent.visibility = View.VISIBLE
                binding.layoutError.visibility = View.GONE

                // Setup Hero
                val hero = newsResult.news.firstOrNull { it.isFeatured }
                    ?: newsResult.news.first()

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

                // Recent news
                val recentNews = if (currentCategory == "Todos") {
                    newsResult.news.filter { it.id != hero.id }
                } else {
                    newsResult.news
                }

                newsAdapter.updateNews(recentNews)
            }

            // Depois de mostrar o cache, atualiza pela internet em segundo plano.
            if (!forceRefresh && newsResult.news.isNotEmpty()) {
                launch {
                    val freshResult = repository.getNews(
                        category = if (currentCategory == "Todos") null else currentCategory,
                        forceRefresh = true
                    )

                    if (!isAdded || freshResult.news.isEmpty()) return@launch

                    val freshNews = freshResult.news
                    val freshHero = freshNews.firstOrNull { it.isFeatured }
                        ?: freshNews.first()

                    heroArticle = freshHero
                    setupHeroCard(freshHero)

                    val freshBreaking = freshNews.firstOrNull { it.isBreaking }

                    if (freshBreaking != null) {
                        binding.breakingNewsContainer.visibility = View.VISIBLE
                        binding.tvBreakingText.text = freshBreaking.title
                        binding.breakingNewsContainer.setOnClickListener {
                            ArticleDetailActivity.start(requireContext(), freshBreaking)
                        }
                    } else {
                        binding.breakingNewsContainer.visibility = View.GONE
                    }

                    val freshRecentNews = if (currentCategory == "Todos") {
                        freshNews.filter { it.id != freshHero.id }
                    } else {
                        freshNews
                    }

                    newsAdapter.updateNews(freshRecentNews)
                }
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
            val imageUrl = if (hero.imageUrl.startsWith("/")) {
                "https://nexora-news.nexoranews.blitz.cloud${hero.imageUrl}"
            } else {
                hero.imageUrl
            }

            Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.secondary_dark)
                .centerCrop()
                .into(ivHero)
        }
    }

    override fun onDestroyView() {
        adRotationHandler.removeCallbacks(adRotationRunnable)
        super.onDestroyView()
        _binding = null
    }
}
