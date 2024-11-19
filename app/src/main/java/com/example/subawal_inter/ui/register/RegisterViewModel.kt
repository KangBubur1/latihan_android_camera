package com.example.subawal_inter.ui.register

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.subawal_inter.data.Repository
import com.example.subawal_inter.utils.Result


class RegisterViewModel(private val repository: Repository) : ViewModel() {
    private val _registerStatus = MutableLiveData<Result<String>>()
    val registerStatus: LiveData<Result<String>> = _registerStatus

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                repository.register(name, email, password)
                _registerStatus.value = Result.Success("Registration successful")
                _toastMessage.value = "Registration successful"
            } catch (e: Exception) {
                _registerStatus.value = Result.Error("Registration failed")
                _toastMessage.value = "Registration failed: ${e.message}"
                Log.d("RegisterViewModel", "Registration failed: ${e.message}")
            }
        }
    }
}

