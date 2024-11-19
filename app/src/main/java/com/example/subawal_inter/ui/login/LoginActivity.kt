package com.example.subawal_inter.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.subawal_inter.MainActivity
import com.example.subawal_inter.data.DataStoreManager
import com.example.subawal_inter.databinding.ActivityLoginBinding
import com.example.subawal_inter.di.Injection
import com.example.subawal_inter.ui.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(Injection.provideRepository(this))
    }
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataStoreManager = DataStoreManager.getInstance(applicationContext)

        checkForToken()

        signUpPrompt()

        binding.btnLogin.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                setLoadingState(true)
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkForToken() {
        lifecycleScope.launch {
            dataStoreManager.getToken().collect { token ->
                if (token != null) {
                    if (token.isNotEmpty()) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        loginViewModel.login(email, password)

        loginViewModel.loginResult.observe(this) { result ->
            setLoadingState(false)

            result.onSuccess { loginResponse ->
                if (!loginResponse.error) {
                    lifecycleScope.launch {
                        val token = loginResponse.loginResult.token
                        saveTokenToDataStore(token)
                    }


                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, loginResponse.message, Toast.LENGTH_SHORT).show()
                }
            }

            result.onFailure {
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun saveTokenToDataStore(token: String) {
        dataStoreManager.saveToken(token)

        println("Saved Token: $token")
    }

    private fun signUpPrompt() {
        val signUpPromptText = "Don't have an account? Create account"
        val spannable = SpannableString(signUpPromptText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(
                    this@LoginActivity,
                    com.example.subawal_inter.R.color.black
                )
            }
        }
        spannable.setSpan(clickableSpan, 23, 37, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvSignUpPrompt.text = spannable
        binding.tvSignUpPrompt.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }
    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.root.isEnabled = !isLoading
    }
}
