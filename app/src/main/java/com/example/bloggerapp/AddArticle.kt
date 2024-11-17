package com.example.bloggerapp

import LocationHelper
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddArticle : AppCompatActivity() {

    private lateinit var etBlogTitle: TextInputEditText
    private lateinit var etBlogLocation: TextInputEditText
    private lateinit var etBlogDescription: TextInputEditText
    private lateinit var btnGetCurrentLocation: MaterialButton
    private lateinit var btnSubmitArticle: MaterialButton
    private lateinit var updateArticle: MaterialButton

    private lateinit var locationHelper: LocationHelper
    private lateinit var firebaseHandler: FirebaseHandler

    private var blogId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)

        etBlogTitle = findViewById(R.id.etBlogTitle)
        etBlogLocation = findViewById(R.id.etBlogLocation)
        etBlogDescription = findViewById(R.id.etBlogDescription)
        btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation)
        btnSubmitArticle = findViewById(R.id.btnSubmitArticle)
        updateArticle = findViewById(R.id.btnEditArticle)

        locationHelper = LocationHelper(this)
        firebaseHandler = FirebaseHandler()

        blogId = intent.getStringExtra("BLOG_ID")

        if (blogId != null) {
            loadBlogData(blogId!!)
            btnSubmitArticle.visibility = MaterialButton.GONE
            updateArticle.visibility = MaterialButton.VISIBLE
        }

        btnGetCurrentLocation.setOnClickListener {
            locationHelper.getCurrentLocation { address ->
                etBlogLocation.setText(address)
            }
        }

        btnSubmitArticle.setOnClickListener {
            addArticle()
        }

        updateArticle.setOnClickListener {
            updateArticle()
        }
    }

    private fun loadBlogData(blogId: String) {
        firebaseHandler.readSpecificBlog(blogId) { success, blog, error ->
            if (success && blog != null) {
                etBlogTitle.setText(blog.title)
                etBlogDescription.setText(blog.description)
                etBlogLocation.setText(blog.location)
            } else {
                Toast.makeText(this, error ?: "Failed to load blog", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addArticle() {
        val title = etBlogTitle.text.toString().trim()
        val location = etBlogLocation.text.toString().trim()
        val description = etBlogDescription.text.toString().trim()

        if (title.isEmpty() || location.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseHandler.insertBlog(title, description, location) { success, error ->
            if (success) {
                Toast.makeText(this, "Article added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, error ?: "Failed to add article", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateArticle() {
        val title = etBlogTitle.text.toString().trim()
        val location = etBlogLocation.text.toString().trim()
        val description = etBlogDescription.text.toString().trim()

        if (title.isEmpty() || location.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = mapOf(
            "title" to title,
            "description" to description,
            "location" to location
        )

        blogId?.let { id ->
            firebaseHandler.updateBlog(id, updatedData) { success, error ->
                if (success) {
                    Toast.makeText(this, "Article updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, error ?: "Failed to update article", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
