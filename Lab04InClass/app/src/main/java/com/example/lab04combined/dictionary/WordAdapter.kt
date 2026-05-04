package com.example.lab04combined.dictionary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab04combined.databinding.ItemWordBinding

class WordAdapter : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {
    private val items = mutableListOf<WordEntity>()

    fun submitList(newItems: List<WordEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class WordViewHolder(private val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WordEntity) {
            binding.textWord.text = item.word
            binding.textDefinition.text = item.definition
        }
    }
}

