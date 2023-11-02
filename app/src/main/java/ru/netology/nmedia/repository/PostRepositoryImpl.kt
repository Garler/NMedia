package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit

class PostRepositoryImpl : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    private fun <T> enqueuePostRepository(request: Request, typeToken: Class<T>, callback: PostRepository.RepositoryCallback<T>) {
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val result = response.body?.string() ?: throw RuntimeException("body is null")
                        callback.onSuccess(gson.fromJson(result, typeToken))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }
    override fun getAllAsync(callback: PostRepository.RepositoryCallback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        callback.onSuccess(gson.fromJson(body, typeToken))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

    override fun likeAsync(id: Long, likedByMe: Boolean, callback: PostRepository.RepositoryCallback<Post>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .run {
                if (likedByMe) {
                    delete(gson.toJson(id).toRequestBody(jsonType))
                } else {
                    post(gson.toJson(id).toRequestBody(jsonType))
                }
            }
            .build()

        enqueuePostRepository(request, Post::class.java, callback)
    }

    override fun repostAsync(id: Long, callback: PostRepository.RepositoryCallback<Post>) {
        TODO()
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.RepositoryCallback<Post>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        enqueuePostRepository(request, Post::class.java, callback)
    }

    override fun saveAsync(post: Post, callback: PostRepository.RepositoryCallback<Post>) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        enqueuePostRepository(request, Post::class.java, callback)
    }
}