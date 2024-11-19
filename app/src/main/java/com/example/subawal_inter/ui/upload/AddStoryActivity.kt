package com.example.subawal_inter.ui.upload

import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.subawal_inter.data.DataStoreManager
import com.example.subawal_inter.databinding.ActivityAddStoryBinding
import com.example.subawal_inter.di.Injection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel: AddStoryViewModel by viewModels {
        AddStoryViewModelFactory(Injection.provideRepository(this))
    }

    private lateinit var dataStore: DataStoreManager
    private var photoUri: Uri? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val STORAGE_PERMISSION_REQUEST_CODE = 101

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                photoUri = it
                viewModel.updateSelectedImageUri(it)
                binding.imagePreview.setImageURI(it)
            }
        }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
        if (isSuccess) {
            val imageUri = photoUri // the URI passed to the cameraLauncher
            imageUri?.let {
                viewModel.updateSelectedImageUri(it)
                binding.imagePreview.setImageURI(it) // Display the captured image
            }
        } else {
            Toast.makeText(this, "Camera failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataStore = DataStoreManager.getInstance(applicationContext)

        checkPermissions()

        binding.buttonSelectImage.setOnClickListener { openGallery() }
        binding.buttonTakePhoto.setOnClickListener { openCamera() }

        binding.buttonAdd.setOnClickListener {
            val description = binding.edAddDescription.text.toString()
            if (description.isNotEmpty() && photoUri != null) {
                uploadStory(description, photoUri)
            } else {
                Toast.makeText(this, "Silakan isi deskripsi dan pilih gambar", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.selectedImageUri.observe(this) { uri ->
            photoUri = uri
            binding.imagePreview.setImageURI(uri)
        }
    }

    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        )
        val storagePermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*")
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Prepare the content values for MediaStore to save the image
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            }

            // Create a new image URI
            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let {
                // Save the URI for later use
                photoUri = it

                // Launch the camera with the URI
                cameraLauncher.launch(it)
            } ?: run {
                Toast.makeText(this, "Failed to create file for photo", Toast.LENGTH_SHORT).show()
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    // Kompresi gambar
    private fun compressImage(imageUri: Uri): File {
        val inputStream = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        var quality = 100
        val byteArrayOutputStream = ByteArrayOutputStream()

        // Kompres gambar sampai ukuran file < 1MB
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        // Menurunkan kualitas hingga ukuran file lebih kecil dari 1MB
        while (byteArrayOutputStream.size() > 1024 * 1024 && quality > 10) {
            byteArrayOutputStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        }

        // Simpan gambar yang terkompresi ke file sementara
        val compressedFile = File(cacheDir, "compressed_image.jpg")
        FileOutputStream(compressedFile).use { it.write(byteArrayOutputStream.toByteArray()) }

        return compressedFile
    }

    // Fungsi untuk upload story
    private fun uploadStory(description: String, photoUri: Uri?) {
        lifecycleScope.launch {
            try {
                val token = getTokenFromDataStore()

                if (token != null) {
                    val descriptionPart = description.toRequestBody("text/plain".toMediaType())

                    // Kompres gambar sebelum upload
                    val photoPart = photoUri?.let {
                        val compressedImageFile = compressImage(it)

                        val requestBody = compressedImageFile.asRequestBody("image/jpeg".toMediaType())
                        MultipartBody.Part.createFormData("photo", compressedImageFile.name, requestBody)
                    }

                    val latPart = 0.0
                    val lonPart = 0.0

                    if (photoPart != null) {
                        viewModel.addStory(descriptionPart, photoPart, latPart, lonPart) { result ->
                            result.onSuccess {
                                Toast.makeText(this@AddStoryActivity, "Story added successfully!", Toast.LENGTH_SHORT).show()
                                finish() // Close activity after success
                            }.onFailure { exception ->
                                Toast.makeText(this@AddStoryActivity, "Failed to add story: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@AddStoryActivity, "Failed to process image file", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AddStoryActivity, "No token found, please login again", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddStoryActivity, "Failed to add story: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Ambil token dari DataStore
    private suspend fun getTokenFromDataStore(): String? {
        return dataStore.getToken().firstOrNull()
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Storage permission is required to select a photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
