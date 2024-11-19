package com.example.subawal_inter

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.subawal_inter.data.DataStoreManager
import com.example.subawal_inter.databinding.ActivityMainBinding
import com.example.subawal_inter.di.Injection
import com.example.subawal_inter.ui.detail.DetailActivity
import com.example.subawal_inter.ui.login.LoginActivity
import com.example.subawal_inter.ui.upload.AddStoryActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Pair

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(Injection.provideRepository(this))
    }
    private lateinit var adapter: StoryAdapter
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataStoreManager = DataStoreManager.getInstance(applicationContext)

        adapter = StoryAdapter { story ->
            binding.rvStories.animate().alpha(0f).setDuration(300).withEndAction {
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_STORY, story)
                }

                val options = ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    Pair(binding.rvStories as View, "sharedTransitionName")
                )

                startActivity(intent, options.toBundle())
                binding.rvStories.postDelayed({
                    binding.rvStories.animate().alpha(1f).setDuration(300)
                }, 300)
            }.start()
        }

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }


        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                dataStoreManager.clearToken()

                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }


        lifecycleScope.launch {
            dataStoreManager.getToken().collect { token ->
                Log.d("MainActivity", "Collected Token: $token")
                if (token.isNullOrEmpty()) {
                    Log.d("MainActivity", "Token is empty, redirecting to login")
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.d("MainActivity", "Token exists, attempting to fetch stories")
                    viewModel.setToken(token)
                    setupRecyclerView()
                    viewModel.fetchStories()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val token = dataStoreManager.getToken().first()
            if (!token.isNullOrEmpty()) {
                viewModel.fetchStories()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.adapter = adapter

        viewModel.stories.observe(this) { stories ->
            if (stories != null) {
                adapter.submitList(stories)
            }
        }
    }
}

