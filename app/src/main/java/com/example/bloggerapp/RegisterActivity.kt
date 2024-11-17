package com.example.bloggerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    private val firebaseHandler = FirebaseHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseHandler.registerUser(email, password) { success, message ->
            if (success) {
                val userId = firebaseHandler.getCurrentUserId()
                if (userId == null) {
                    Toast.makeText(this, "Failed to get user ID", Toast.LENGTH_SHORT).show()
                    return@registerUser
                }

                val userMap = mapOf(
                    "username" to username,
                    "email" to email
                )

                firebaseHandler.saveUserDataToDatabase("users/$userId", userMap) { dbSuccess, dbMessage ->
                    if (dbSuccess) {
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to save user data: $dbMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Registration Failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
