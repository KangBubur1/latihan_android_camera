package com.example.subawal_inter.data

import android.util.Log
import com.example.subawal_inter.data.network.ApiService
import com.example.subawal_inter.data.response.AddStoryResponse
import com.example.subawal_inter.data.response.ListStoryItem
import com.example.subawal_inter.data.response.LoginResponse
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class Repository private constructor(
    private val apiService: ApiService,
    private val dataStore: DataStoreManager
) {

    suspend fun register(name: String, email: String, password: String) = apiService.register(name, email, password)

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(email, password)
            if (!response.error) {
                dataStore.saveToken(response.loginResult.token)
                Result.success(response)
            } else {
                Result.failure(Exception("Login failed: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStories(): List<ListStoryItem> {
        try {
            val token = dataStore.getToken().firstOrNull()
                ?: throw Exception("No authentication token available")

            val formattedToken = "Bearer $token"
            Log.d("Repository", "Fetching stories with token: $formattedToken")

            val response = apiService.getStories(formattedToken)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Repository", "Error response code: ${response.code()}")
                Log.e("Repository", "Error response body: $errorBody")
                throw Exception("API Error: ${response.code()} - $errorBody")
            }

            val storyList = response.body()?.listStory
            return storyList ?: throw Exception("Empty story list")

        } catch (e: Exception) {
            Log.e("Repository", "Error fetching stories: ${e.message}", e)
            throw e
        }
    }

    suspend fun addStory(
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: Double,
        lon: Double
    ): Result<AddStoryResponse> {
        val token = dataStore.getToken().firstOrNull() // Fetch the token from DataStore

        return try {
            val response = apiService.addStory(
                description = description,
                photo = photo,
                lat = lat,
                lon = lon,
                token = "Bearer $token",
            )

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to upload story: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    companion object {
        @Volatile
        private var instance: Repository? = null

        fun getInstance(apiService: ApiService, dataStore: DataStoreManager): Repository =
            instance ?: synchronized(this) {
                instance ?: Repository(apiService, dataStore)
            }.also { instance = it }
    }
}
