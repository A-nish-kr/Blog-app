package com.example.bloggerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var blogAdapter: BlogAdapter
    private lateinit var firebaseHandler: FirebaseHandler
    private lateinit var profileImageView: ImageView
    private lateinit var fabAddBlog: FloatingActionButton
    private lateinit var searchView: SearchView

    private var blogList: MutableList<Blog> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseHandler = FirebaseHandler()

        recyclerView = findViewById(R.id.requestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        blogAdapter = BlogAdapter(blogList) { blog ->
            openReadMoreActivity(blog.id)
        }
        recyclerView.adapter = blogAdapter

        profileImageView = findViewById(R.id.profileImage)
        profileImageView.setOnClickListener {
            openProfileActivity()
        }

        fabAddBlog = findViewById(R.id.floatingActionButton)
        fabAddBlog.setOnClickListener {
            openAddArticleActivity()
        }

        loadBlogs()

        searchView = findViewById(R.id.searchView)
        setupSearchView()
    }

    private fun loadBlogs() {
        firebaseHandler.readAllBlogs { success, blogs, error ->
            if (success) {
                blogs?.let {
                    blogList = it.toMutableList()
                    Log.d("MainActivity", "Loaded ${it.size} blogs")
                    blogAdapter.updateBlogs(it)
                }
            } else {
                Toast.makeText(this, error ?: "Failed to load blogs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterBlogs(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBlogs(newText)
                return true
            }
        })
    }

    private fun filterBlogs(query: String?) {
        if (blogList.isEmpty()) {
            Log.d("MainActivity", "Blog list is empty, cannot filter")
            blogAdapter.updateBlogs(emptyList())
            return
        }

        val filteredList = if (query.isNullOrEmpty()) {
            blogList
        } else {
            blogList.filter { it.title?.contains(query, ignoreCase = true) == true }
        }

        Log.d("MainActivity", "Filtered List: ${filteredList.size} items")

        blogAdapter.updateBlogs(filteredList)
    }

    private fun openReadMoreActivity(blogId: String?) {
        if (blogId != null) {
            val intent = Intent(this, ReadMore::class.java)
            intent.putExtra("BLOG_ID", blogId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Invalid blog ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun openAddArticleActivity() {
        val intent = Intent(this, AddArticle::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadBlogs()
    }
}
