package com.example.subawal_inter

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subawal_inter.data.Repository
import com.example.subawal_inter.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repository: Repository) : ViewModel() {
    private val _stories = MutableLiveData<List<ListStoryItem>?>()
    val stories: MutableLiveData<List<ListStoryItem>?> = _stories
    private var token: String? = null

    fun setToken(token: String) {
        this.token = token // Store token
    }

    fun fetchStories() {
        viewModelScope.launch {
            try {
                val storyList = repository.getStories() // No token passed
                if (storyList.isEmpty()) {
                    Log.d("MainActivityViewModel", "No stories fetched")
                }
                _stories.postValue(storyList)
            } catch (e: Exception) {
                Log.e("MainActivityViewModel", "Error fetching stories", e)
                _stories.postValue(null)
            }
        }
    }
}
