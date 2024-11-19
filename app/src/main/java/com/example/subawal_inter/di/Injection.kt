package com.example.subawal_inter.di

import android.content.Context
import com.example.subawal_inter.data.DataStoreManager
import com.example.subawal_inter.data.Repository
import com.example.subawal_inter.data.network.ApiConfig


object Injection {
    fun provideRepository(context: Context): Repository {
        val dataStore = DataStoreManager.getInstance(context)
        val apiService = ApiConfig.getApiService(null)
        return Repository.getInstance(apiService, dataStore)
    }
}