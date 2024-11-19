package com.example.subawal_inter.ui.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.subawal_inter.databinding.ActivityRegisterBinding
import com.example.subawal_inter.di.Injection
import com.example.subawal_inter.utils.Result
import com.example.subawal_inter.ui.register.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPasswordValidation()
        setupObservers()

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && isValidPassword(password)) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.progressBar.visibility = View.VISIBLE
                    registerViewModel.register(name, email, password)
                } else {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields and ensure the password is valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPasswordValidation() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                val isPasswordValid = isValidPassword(password)
                binding.btnRegister.isEnabled = password.isNotEmpty() && isPasswordValid

                if (password.isNotEmpty() && !isPasswordValid) {
                    binding.etPassword.error = "Password must be at least 8 characters long and contain a mix of letters and numbers."
                } else {
                    binding.etPassword.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        registerViewModel.registerStatus.observe(this, { result ->
            binding.progressBar.visibility = View.GONE

            when (result) {
                is Result.Success -> {
                    Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                }
                Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }
    }
}
