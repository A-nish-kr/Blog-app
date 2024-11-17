package com.example.bloggerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var btnLogout: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseHandler: FirebaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        firebaseHandler = FirebaseHandler()

        firebaseAuth = FirebaseAuth.getInstance()

        tvUsername = findViewById(R.id.username)
        btnLogout = findViewById(R.id.Logout)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            firebaseHandler.fetchUserData(firebaseHandler.getCurrentUserId().toString()){
                username ->
                tvUsername.text = username
            }
        } else {
            tvUsername.text = "Guest"
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Redirect to LoginActivity or MainActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
