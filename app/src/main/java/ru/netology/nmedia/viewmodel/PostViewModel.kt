package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0L,
    content = "",
    author = "",
    likedByMe = false,
    published = "",
    likes = 0
//    reposts = 0,
//    views = 0,
//    video = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModelState())
    val data: LiveData<FeedModelState>
        get() = _data
    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        thread {
            _data.postValue(FeedModelState(loading = true))
            try {
                val posts = repository.getAll()
                FeedModelState(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                FeedModelState(error = true)
            }.also(_data::postValue)
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContentAndSave(content: String) {
        thread {
            edited.value?.let {
                val text = content.trim()
                if (it.content != content) {
                    repository.save(it.copy(content = text))
                    loadPosts()
                }
                _postCreated.postValue(Unit)
                edited.postValue(empty)
            }
        }
    }

    fun like(id: Long, likedByMe: Boolean) {
        thread {
            val refreshPost = repository.like(id, likedByMe)
            val refreshPosts = _data.value?.posts?.map {
                if (it.id == id) refreshPost else it
            } ?: emptyList()
            _data.postValue(FeedModelState(posts = refreshPosts, empty = refreshPosts.isEmpty()))
        }
    }

    fun repost(id: Long) = repository.repost(id)
    fun removeById(id: Long) {
        thread {
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }
//    fun clearEdit() {
//        edited.value = empty
//    }
}