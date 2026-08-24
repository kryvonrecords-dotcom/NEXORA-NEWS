package com.nexoranews.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import com.nexoranews.app.data.remote.NewsApiService
import com.nexoranews.app.databinding.ActivityMainBinding
import com.nexoranews.app.ui.bookmarks.BookmarksFragment
import com.nexoranews.app.ui.categories.CategoriesFragment
import com.nexoranews.app.ui.detail.ArticleDetailActivity
import com.nexoranews.app.ui.home.HomeFragment
import com.nexoranews.app.ui.news.NewsListFragment
import com.nexoranews.app.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val newsApiService = NewsApiService()

    private val homeFragment by lazy { HomeFragment() }
    private val newsListFragment by lazy { NewsListFragment() }
    private val categoriesFragment by lazy { CategoriesFragment() }
    private val bookmarksFragment by lazy { BookmarksFragment() }
    private val settingsFragment by lazy { SettingsFragment() }

    private var activeFragment: Fragment = homeFragment

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Notification permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_NexoraNews)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeAdMob()
        checkNotificationPermission()
        setupFragments(savedInstanceState)
        setupBottomNavigation()
        setupHeaderActions()
        setupWhatsAppFloatingButton()
        setupBackNavigation()
        handleIntent(intent)
    }

    private fun setupWhatsAppFloatingButton() {
        binding.fabWhatsApp.setOnClickListener {
            try {
                val phoneNumber = "244921281315"
                val message = "Olá! Gostaria de falar com a redação do Nexora News."
                val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                        Toast.makeText(this, "Não foi possível abrir o WhatsApp.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeAdMob() {
        try {
            MobileAds.initialize(this) {
                // AdMob SDK initialized successfully
            }
            setupAdMobBanner()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupAdMobBanner() {
        try {
            val adRequest = AdRequest.Builder().build()
            binding.adViewMain.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    binding.adBannerContainer.visibility = View.VISIBLE
                    // Adjust fragment container bottom margin so banner + bottom nav don't obscure content
                    val density = resources.displayMetrics.density
                    val bottomMarginPx = ((60 + 50) * density).toInt()
                    val params = binding.fragmentContainer.layoutParams as? CoordinatorLayout.LayoutParams
                    params?.let {
                        it.bottomMargin = bottomMarginPx
                        binding.fragmentContainer.layoutParams = it
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    binding.adBannerContainer.visibility = View.GONE
                    val density = resources.displayMetrics.density
                    val bottomMarginPx = (60 * density).toInt()
                    val params = binding.fragmentContainer.layoutParams as? CoordinatorLayout.LayoutParams
                    params?.let {
                        it.bottomMargin = bottomMarginPx
                        binding.fragmentContainer.layoutParams = it
                    }
                }
            }
            binding.adViewMain.loadAd(adRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fragmentContainer, bookmarksFragment, "bookmarks").hide(bookmarksFragment)
                .add(R.id.fragmentContainer, categoriesFragment, "categories").hide(categoriesFragment)
                .add(R.id.fragmentContainer, newsListFragment, "news").hide(newsListFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit()
            activeFragment = homeFragment
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(homeFragment)
                    true
                }
                R.id.nav_news -> {
                    switchFragment(newsListFragment)
                    true
                }
                R.id.nav_categories -> {
                    switchFragment(categoriesFragment)
                    true
                }
                R.id.nav_bookmarks -> {
                    switchFragment(bookmarksFragment)
                    true
                }
                R.id.nav_settings -> {
                    switchFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (activeFragment != targetFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(targetFragment)
                .commit()
            activeFragment = targetFragment
        }
    }

    private fun setupHeaderActions() {
        binding.btnHeaderSearch.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_news
        }

        binding.btnHeaderRefresh.setOnClickListener {
            when (activeFragment) {
                is HomeFragment -> (activeFragment as HomeFragment).loadData(forceRefresh = true)
                is NewsListFragment -> (activeFragment as NewsListFragment).filterByCategory("Todos")
                is CategoriesFragment -> {
                    // Refreshes categories
                }
            }
        }

        binding.btnLanguage.setOnClickListener {
            val languages = arrayOf(
                "🇵🇹 Português",
                "🇺🇸 English",
                "🇪🇸 Español",
                "🇫🇷 Français"
            )

            val languageCodes = arrayOf("pt", "en", "es", "fr")
            val preferences = getSharedPreferences("nexora_preferences", MODE_PRIVATE)

            AlertDialog.Builder(this)
                .setTitle("Idioma")
                .setItems(languages) { _, which ->
                    val selectedCode = languageCodes[which]

                    preferences.edit()
                        .putString("app_language", selectedCode)
                        .apply()

                    val localeTag = when (selectedCode) {
                        "pt" -> "pt-PT"
                        "en" -> "en"
                        "es" -> "es"
                        "fr" -> "fr"
                        else -> "pt-PT"
                    }

                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(localeTag)
                    )
                }
                .show()
        }

        binding.btnBannerRetry.setOnClickListener {
            if (activeFragment is HomeFragment) {
                (activeFragment as HomeFragment).loadData(forceRefresh = true)
            }
        }
    }

    fun setOfflineBannerVisible(isVisible: Boolean) {
        binding.offlineTopBanner.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun navigateToNewsTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_news
    }

    fun openCategory(categoryName: String) {
        newsListFragment.filterByCategory(categoryName)
        binding.bottomNavigation.selectedItemId = R.id.nav_news
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (activeFragment != homeFragment) {
                    binding.bottomNavigation.selectedItemId = R.id.nav_home
                } else {
                    finish()
                }
            }
        })
    }

    override fun onPause() {
        binding.adViewMain.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.adViewMain.resume()
    }

    override fun onDestroy() {
        binding.adViewMain.destroy()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val clickUrl =
            intent?.getStringExtra("notification_click_url")
                ?: intent?.getStringExtra("clickUrl")
                ?: intent?.getStringExtra("newsSlug")?.let {
                    "/noticia/$it"
                }

        if (!clickUrl.isNullOrBlank()) {
            val path = Uri.parse(clickUrl).path ?: ""

            if (path.contains("/noticia/") || path.contains("/news/")) {
                val slug = path.substringAfterLast("/").trim()

                if (slug.isNotEmpty()) {
                    ArticleDetailActivity.startWithSlug(this, slug)
                    return
                }
            }
        }

        val appLinkData: Uri? = intent?.data

        if (appLinkData != null) {
            val path = appLinkData.path ?: ""

            if (path.contains("/noticia/") || path.contains("/news/")) {
                val slug = path.substringAfterLast("/").trim()

                if (slug.isNotEmpty()) {
                    ArticleDetailActivity.startWithSlug(this, slug)
                }
            }
        }
    }
}
