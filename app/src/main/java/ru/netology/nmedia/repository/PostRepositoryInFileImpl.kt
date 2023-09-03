package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositoryInFileImpl(
    private val context: Context
) : PostRepository {
    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val postFile = "posts.json"
    private val nextIdFile = "next_id.json"
    private var nextId = 1
    private var posts = emptyList<Post>()
    private val data = MutableLiveData(posts)

    init {
        val postsFile = context.filesDir.resolve(postFile)

        if (postsFile.exists()) {
            postsFile.reader().buffered().use {
                posts = gson.fromJson(it, type)
                data.value = posts
            }
        } else {
            emptyList<Post>()
        }

        val nextIdFile = context.filesDir.resolve(nextIdFile)
        nextId = if (nextIdFile.exists()) {
            nextIdFile.reader().buffered().use {
                gson.fromJson(it, Int::class.java)
            }
        } else {
            nextId
        }
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun like(id: Int) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likedByMe = !it.likedByMe,
                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
            )
        }
        data.value = posts
        sync()
    }

    override fun repost(id: Int) {
        posts = posts.map {
            if (it.id != id) it else it.copy(reposts = it.reposts + 1)
        }
        data.value = posts
        sync()
    }

    override fun removeById(id: Int) {
        posts = posts.filter { it.id != id }
        data.value = posts
        sync()
    }

    override fun save(post: Post) {
        if (post.id == 0) {
            posts = listOf(
                post.copy(
                    id = nextId++,
                    author = "Me",
                    published = "now"
                )
            ) + posts
            data.value = posts
            sync()
            return
        }
        posts = posts.map {
            if (it.id != post.id) it else it.copy(content = post.content)
        }
        data.value = posts
        sync()
    }

    private fun sync() {
        context.filesDir.resolve(postFile).writer().buffered().use {
            it.write(gson.toJson(posts))
        }
        context.filesDir.resolve(nextIdFile).writer().buffered().use {
            it.write(gson.toJson(nextId))
        }
    }
}
