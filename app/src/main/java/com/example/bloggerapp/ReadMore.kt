package com.example.bloggerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ReadMore : AppCompatActivity() {

    private lateinit var textViewTitle: TextView
    private lateinit var textViewLocation: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var buttonUpdate: Button
    private lateinit var buttonDelete: Button

    private lateinit var blog: Blog
    private val firebaseHandler = FirebaseHandler()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_more)

        textViewTitle = findViewById(R.id.textViewTitle)
        textViewLocation = findViewById(R.id.textViewLocation)
        textViewDescription = findViewById(R.id.textViewDescription)
        buttonUpdate = findViewById(R.id.buttonUpdate)
        buttonDelete = findViewById(R.id.buttonDelete)

        val blogId = intent.getStringExtra("BLOG_ID") ?: return

        fetchBlogDetails(blogId)

        buttonUpdate.setOnClickListener {
            if (blog.createdBy == firebaseAuth.currentUser?.uid) {
                openUpdateActivity(blog.id.toString())
            } else {
                Toast.makeText(this, "You can only update your own blogs", Toast.LENGTH_SHORT).show()
            }
        }

        buttonDelete.setOnClickListener {
            if (blog.createdBy == firebaseAuth.currentUser?.uid) {
                deleteBlog(blog.id.toString())
            } else {
                Toast.makeText(this, "You can only delete your own blogs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchBlogDetails(blogId: String) {
        firebaseHandler.readSpecificBlog(blogId) { success, blog, error ->
            if (success && blog != null) {
                this.blog = blog
                displayBlogDetails(blog)
            } else {
                Toast.makeText(this, error ?: "Failed to load blog", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayBlogDetails(blog: Blog) {
        textViewTitle.text = blog.title
        textViewLocation.text = blog.location
        textViewDescription.text = blog.description
    }

    private fun openUpdateActivity(blogId: String) {
        val intent = Intent(this, AddArticle::class.java)
        intent.putExtra("BLOG_ID", blogId)
        startActivity(intent)
    }

    private fun deleteBlog(blogId: String) {
        firebaseHandler.deleteBlog(blogId) { success, error ->
            if (success) {
                Toast.makeText(this, "Blog deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, error ?: "Failed to delete blog", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
