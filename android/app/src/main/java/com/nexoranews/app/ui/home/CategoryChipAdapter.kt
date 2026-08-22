package com.nexoranews.app.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nexoranews.app.R
import com.nexoranews.app.data.model.Category
import com.nexoranews.app.databinding.ItemCategoryChipBinding

class CategoryChipAdapter(
    private var categories: List<Category>,
    private var selectedCategory: String = "Todos",
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryChipAdapter.ViewHolder>() {

    fun updateCategories(newCategories: List<Category>, selected: String = selectedCategory) {
        this.categories = newCategories
        this.selectedCategory = selected
        notifyDataSetChanged()
    }

    fun setSelectedCategory(selected: String) {
        this.selectedCategory = selected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(private val binding: ItemCategoryChipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.tvChipName.text = category.name

            val isSelected = category.name.equals(selectedCategory, ignoreCase = true) ||
                             category.slug.equals(selectedCategory, ignoreCase = true)

            val context = binding.root.context

            if (isSelected) {
                binding.cardCategoryChip.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_blue))
                binding.tvChipName.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.cardCategoryChip.strokeWidth = 0
            } else {
                binding.cardCategoryChip.setCardBackgroundColor(ContextCompat.getColor(context, R.color.bg_card))
                binding.tvChipName.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                binding.cardCategoryChip.strokeWidth = 1
            }

            try {
                if (!category.color.isNullOrBlank()) {
                    binding.dotCategoryIndicator.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        Color.parseColor(category.color)
                    )
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
