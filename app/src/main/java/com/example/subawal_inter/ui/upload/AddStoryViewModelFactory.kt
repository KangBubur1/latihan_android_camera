package com.example.subawal_inter.ui.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.subawal_inter.data.Repository

class AddStoryViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddStoryViewModel::class.java)) {
            return AddStoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}