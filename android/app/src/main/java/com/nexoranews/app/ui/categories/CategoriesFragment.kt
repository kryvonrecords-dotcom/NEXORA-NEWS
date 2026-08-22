package com.nexoranews.app.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.nexoranews.app.MainActivity
import com.nexoranews.app.R
import com.nexoranews.app.data.repository.NewsRepository
import com.nexoranews.app.databinding.FragmentCategoriesBinding
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: NewsRepository
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        repository = NewsRepository(context)

        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            // Navigate to news feed filtered by this category
            (activity as? MainActivity)?.openCategory(category.name)
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = categoryAdapter
        }

        binding.swipeRefreshCategories.setColorSchemeResources(R.color.primary_blue, R.color.accent_red)
        binding.swipeRefreshCategories.setOnRefreshListener {
            loadCategories()
        }

        loadCategories()
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.swipeRefreshCategories.isRefreshing = true
            val categoriesResult = repository.getCategories()
            binding.swipeRefreshCategories.isRefreshing = false

            categoryAdapter.updateCategories(categoriesResult.categories)
            (activity as? MainActivity)?.setOfflineBannerVisible(categoriesResult.isOffline)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
