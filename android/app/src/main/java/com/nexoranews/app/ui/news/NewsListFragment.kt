package com.nexoranews.app.ui.news

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexoranews.app.MainActivity
import com.nexoranews.app.R
import com.nexoranews.app.data.model.Category
import com.nexoranews.app.data.repository.NewsRepository
import com.nexoranews.app.databinding.FragmentNewsListBinding
import com.nexoranews.app.ui.home.CategoryChipAdapter
import com.nexoranews.app.ui.home.NewsAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewsListFragment : Fragment() {

    private var _binding: FragmentNewsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: NewsRepository
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var categoryChipAdapter: CategoryChipAdapter

    private var currentCategory: String = "Todos"
    private var currentSearchQuery: String = ""
    private var searchJob: Job? = null

    companion object {
        private const val ARG_INITIAL_CATEGORY = "initial_category"

        fun newInstance(categoryName: String? = null): NewsListFragment {
            val fragment = NewsListFragment()
            if (!categoryName.isNullOrBlank()) {
                val args = Bundle()
                args.putString(ARG_INITIAL_CATEGORY, categoryName)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        repository = NewsRepository(context)

        arguments?.getString(ARG_INITIAL_CATEGORY)?.let {
            currentCategory = it
        }

        setupUI(context)
        setupSearch()
        loadCategoriesAndNews()
    }

    private fun setupUI(context: Context) {
        // Categories
        categoryChipAdapter = CategoryChipAdapter(emptyList(), currentCategory) { cat ->
            currentCategory = cat.name
            categoryChipAdapter.setSelectedCategory(currentCategory)
            loadNews(forceRefresh = false)
        }
        binding.rvNewsListCategories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryChipAdapter
        }

        // News List
        newsAdapter = NewsAdapter(emptyList(), repository)
        binding.rvNewsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = newsAdapter
        }

        // Swipe Refresh
        binding.swipeRefreshNewsList.setColorSchemeResources(R.color.primary_blue, R.color.accent_red)
        binding.swipeRefreshNewsList.setOnRefreshListener {
            loadNews(forceRefresh = true)
        }

        // Retry on Error
        binding.layoutNewsListError.findViewById<View>(R.id.btnRetry)?.setOnClickListener {
            loadNews(forceRefresh = true)
        }
    }

    private fun setupSearch() {
        binding.etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                currentSearchQuery = query

                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(400) // Debounce search
                    loadNews(forceRefresh = false)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearchQuery.text.clear()
            hideKeyboard()
        }

        binding.etSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                loadNews(forceRefresh = false)
                true
            } else {
                false
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etSearchQuery.windowToken, 0)
    }

    private fun loadCategoriesAndNews() {
        viewLifecycleOwner.lifecycleScope.launch {
            val categoriesResult = repository.getCategories()
            val allCategory = Category(
                id = "cat-all",
                name = "Todos",
                slug = "todos",
                description = "Todas as notícias",
                color = "#146EF5",
                order = 0
            )
            val combined = listOf(allCategory) + categoriesResult.categories
            categoryChipAdapter.updateCategories(combined, currentCategory)
            loadNews(forceRefresh = false)
        }
    }

    fun filterByCategory(categoryName: String) {
        currentCategory = categoryName
        if (::categoryChipAdapter.isInitialized) {
            categoryChipAdapter.setSelectedCategory(currentCategory)
        }
        loadNews(forceRefresh = false)
    }

    private fun loadNews(forceRefresh: Boolean = false) {
        if (!isAdded) return

        viewLifecycleOwner.lifecycleScope.launch {
            if (!binding.swipeRefreshNewsList.isRefreshing) {
                binding.layoutNewsListLoading.visibility = View.VISIBLE
            }
            binding.layoutNewsListError.visibility = View.GONE
            binding.layoutNewsListEmpty.visibility = View.GONE
            binding.rvNewsList.visibility = View.GONE

            val newsResult = repository.getNews(
                category = if (currentCategory == "Todos") null else currentCategory,
                search = currentSearchQuery.ifBlank { null },
                forceRefresh = forceRefresh
            )

            binding.swipeRefreshNewsList.isRefreshing = false
            binding.layoutNewsListLoading.visibility = View.GONE

            (activity as? MainActivity)?.setOfflineBannerVisible(newsResult.isOffline)

            if (newsResult.news.isEmpty()) {
                if (currentSearchQuery.isNotBlank() || currentCategory != "Todos") {
                    binding.layoutNewsListEmpty.visibility = View.VISIBLE
                } else {
                    binding.layoutNewsListError.visibility = View.VISIBLE
                }
                binding.tvNewsCountHeader.text = "0 artigos encontrados"
            } else {
                binding.rvNewsList.visibility = View.VISIBLE
                newsAdapter.updateNews(newsResult.news)
                val count = newsResult.news.size
                val catText = if (currentCategory == "Todos") "todas as categorias" else currentCategory
                binding.tvNewsCountHeader.text = "$count notícias em $catText"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
