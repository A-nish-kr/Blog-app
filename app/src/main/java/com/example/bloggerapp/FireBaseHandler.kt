package com.example.bloggerapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.Exception

class FirebaseHandler {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun insertBlog(
        title: String,
        description: String,
        location: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val userId = getCurrentUserId()
        if (userId == null) {
            callback(false, "User not logged in")
            return
        }

        val blogId = database.child("blogs").push().key ?: run {
            callback(false, "Failed to generate blog ID")
            return
        }

        fetchUserData(userId) { username ->
            val blog = Blog(
                id = blogId,
                title = title,
                username = username,
                description = description,
                location = location,
                createdBy = userId,
                createdAt = System.currentTimeMillis()
            )

            database.child("blogs").child(blogId).setValue(blog)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, null)
                    } else {
                        logError(task.exception, "Insert Blog Failed")
                        callback(false, task.exception?.message)
                    }
                }
        }
    }


    fun updateBlog(
        blogId: String,
        updatedData: Map<String, Any>,
        callback: (Boolean, String?) -> Unit
    ) {
        val currentUserId = getCurrentUserId() ?: run {
            callback(false, "User not logged in")
            return
        }

        database.child("blogs").child(blogId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val blog = task.result?.getValue(Blog::class.java)
                    if (blog != null && blog.createdBy == currentUserId) {
                        database.child("blogs").child(blogId).updateChildren(updatedData)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    callback(true, null)
                                } else {
                                    logError(updateTask.exception, "Update Blog Failed")
                                    callback(false, updateTask.exception?.message)
                                }
                            }
                    } else {
                        callback(false, "You can only update your own blogs")
                    }
                } else {
                    logError(task.exception, "Fetch Blog Failed")
                    callback(false, task.exception?.message)
                }
            }
    }

    fun deleteBlog(blogId: String, callback: (Boolean, String?) -> Unit) {
        val currentUserId = getCurrentUserId() ?: run {
            callback(false, "User not logged in")
            return
        }

        database.child("blogs").child(blogId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val blog = task.result?.getValue(Blog::class.java)
                    if (blog != null && blog.createdBy == currentUserId) {
                        // Proceed with the deletion if the current user is the creator
                        database.child("blogs").child(blogId).removeValue()
                            .addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    callback(true, null)
                                } else {
                                    logError(deleteTask.exception, "Delete Blog Failed")
                                    callback(false, deleteTask.exception?.message)
                                }
                            }
                    } else {
                        callback(false, "You can only delete your own blogs")
                    }
                } else {
                    logError(task.exception, "Fetch Blog Failed")
                    callback(false, task.exception?.message)
                }
            }
    }
    fun readAllBlogs(callback: (Boolean, List<Blog>?, String?) -> Unit) {
        database.child("blogs").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val blogList = task.result?.children?.mapNotNull { it.getValue(Blog::class.java) }
                    callback(true, blogList, null)
                } else {
                    logError(task.exception, "Read All Blogs Failed")
                    callback(false, null, task.exception?.message)
                }
            }
    }

    fun readSpecificBlog(blogId: String, callback: (Boolean, Blog?, String?) -> Unit) {
        database.child("blogs").child(blogId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val blog = task.result?.getValue(Blog::class.java)
                    if (blog != null) {
                        callback(true, blog, null)
                    } else {
                        callback(false, null, "Blog not found")
                    }
                } else {
                    logError(task.exception, "Read Specific Blog Failed")
                    callback(false, null, task.exception?.message)
                }
            }
    }

    fun registerUser(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    logError(task.exception, "Register User Failed")
                    callback(false, task.exception?.message)
                }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    logError(task.exception, "Login User Failed")
                    callback(false, task.exception?.message)
                }
            }
    }

    fun logoutUser(callback: (Boolean, String?) -> Unit) {
        try {
            auth.signOut()
            callback(true, null)
        } catch (e: Exception) {
            logError(e, "Logout User Failed")
            callback(false, e.message)
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun saveUserDataToDatabase(path: String, data: Map<String, Any>, callback: (Boolean, String?) -> Unit) {
        database.child(path).setValue(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    logError(task.exception, "Save User Data Failed")
                    callback(false, task.exception?.message)
                }
            }
    }

    private fun logError(exception: Exception?, message: String) {
        Log.e("FirebaseHandler", "$message: ${exception?.message}", exception)
    }
    fun fetchUserData(userId: String, callback: (String) -> Unit) {
        database.child("users").child(userId).child("username").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val username = task.result?.value as? String ?: "Unknown"
                callback(username)
            } else {
                callback("Unknown")
            }
        }
    }
}
