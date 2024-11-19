package com.example.subawal_inter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.subawal_inter.data.response.ListStoryItem
import com.example.subawal_inter.databinding.ItemStoryBinding

class StoryAdapter(private val onItemClick: (ListStoryItem) -> Unit) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    private var stories: List<ListStoryItem> = emptyList()

    fun submitList(newStories: List<ListStoryItem>) {
        stories = newStories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    inner class StoryViewHolder(
        private val binding: ItemStoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(stories[position])
                }
            }
        }

        fun bind(story: ListStoryItem) {
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(binding.imageHolder)

            binding.titleHolder.text = story.name
            binding.description.text = story.description
        }
    }
}
