package com.nexoranews.app.ui.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nexoranews.app.data.model.Category
import com.nexoranews.app.databinding.ItemCategoryCardBinding

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    fun updateCategories(newCategories: List<Category>) {
        this.categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(private val binding: ItemCategoryCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.tvCategoryTitle.text = category.name
            binding.tvCategoryDescription.text = category.description ?: "Cobertura completa e notícias exclusivas"
            binding.tvCategoryCount.text = "${category.count.coerceAtLeast(3)} artigos disponíveis"

            try {
                if (!category.color.isNullOrBlank()) {
                    binding.viewCategoryAccent.setBackgroundColor(Color.parseColor(category.color))
                }
            } catch (e: Exception) {
                // Fallback default color
            }

            binding.root.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}
