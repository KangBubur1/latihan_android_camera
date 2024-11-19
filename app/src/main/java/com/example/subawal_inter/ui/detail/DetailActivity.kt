package com.example.subawal_inter.ui.detail

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.subawal_inter.R
import com.example.subawal_inter.data.response.ListStoryItem
import com.example.subawal_inter.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    companion object {
        const val EXTRA_STORY = "extra_story"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story = intent.getParcelableExtra<ListStoryItem>(EXTRA_STORY)

        story?.let {
            binding.tvDetailName.text = it.name
            binding.tvDetailDescription.text = it.description

            Glide.with(this)
                .load(it.photoUrl)
                .into(binding.ivDetailPhoto)
            ViewCompat.setTransitionName(binding.ivDetailPhoto, "sharedTransitionName")
        }

        window?.let {
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
                binding.root.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                )
                WindowInsetsCompat.CONSUMED
            }
        }
    }
}
