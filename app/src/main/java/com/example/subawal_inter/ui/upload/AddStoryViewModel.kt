package com.example.subawal_inter.ui.upload

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subawal_inter.data.Repository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val repository: Repository) : ViewModel() {
    private val _selectedImageUri = MutableLiveData<Uri>()
    val selectedImageUri: LiveData<Uri> = _selectedImageUri

    fun updateSelectedImageUri(uri: Uri) {
        _selectedImageUri.value = uri
    }

    // Add Story method with callback
    fun addStory(
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: Double,
        lon: Double,
        callback: (Result<Any>) -> Unit // Callback parameter
    ) {
        viewModelScope.launch {
            try {
                val result = repository.addStory(description, photo, lat, lon)
                callback(Result.success(result))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }
}