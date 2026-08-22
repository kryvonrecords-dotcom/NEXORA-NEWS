package com.nexoranews.app.ui.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexoranews.app.data.repository.NewsRepository
import com.nexoranews.app.databinding.FragmentBookmarksBinding
import com.nexoranews.app.ui.home.NewsAdapter

class BookmarksFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: NewsRepository
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        repository = NewsRepository(context)

        newsAdapter = NewsAdapter(emptyList(), repository)
        binding.rvBookmarks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = newsAdapter
        }

        loadBookmarks()
    }

    override fun onResume() {
        super.onResume()
        loadBookmarks()
    }

    private fun loadBookmarks() {
        val bookmarks = repository.getBookmarks()
        if (bookmarks.isEmpty()) {
            binding.layoutBookmarksEmpty.visibility = View.VISIBLE
            binding.rvBookmarks.visibility = View.GONE
        } else {
            binding.layoutBookmarksEmpty.visibility = View.GONE
            binding.rvBookmarks.visibility = View.VISIBLE
            newsAdapter.updateNews(bookmarks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
