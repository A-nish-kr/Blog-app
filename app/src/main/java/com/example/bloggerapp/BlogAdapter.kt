package com.example.bloggerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class BlogAdapter(
    private var blogList: MutableList<Blog>,
    private val onItemClick: (Blog) -> Unit
) : RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {


    inner class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvBlogTitle)
        val tvCreator: TextView = itemView.findViewById(R.id.tvBlogCreator)
        val tvTime: TextView = itemView.findViewById(R.id.tvBlogTime)

        fun bind(blog: Blog) {
            tvTitle.text = blog.title
            tvCreator.text = "Created by: ${blog.username}"
            tvTime.text = "Created on: ${convertTimestampToDate(blog.createdAt)}"
            itemView.setOnClickListener { onItemClick(blog) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.displaycard, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blog = blogList[position]
        holder.bind(blog)
    }

    override fun getItemCount(): Int = blogList.size

    fun updateBlogs(newBlogs: List<Blog>) {
        blogList.clear()
        blogList.addAll(newBlogs)
        notifyDataSetChanged()
    }

    private fun convertTimestampToDate(timestamp: Long?): String {
        return if (timestamp != null) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "Unknown"
        }
    }
}
