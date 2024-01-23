package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0L,
    content = "",
    author = "",
    likedByMe = false,
    published = "",
    likes = 0,
    authorAvatar = null,
    show = true
//    reposts = 0,
//    views = 0,
//    video = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl(AppDb.getInstance(context = application).postDao())
    val data: LiveData<FeedModel> = repository.data
        .map(::FeedModel)
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    val newerCount = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0L)
            .asLiveData(Dispatchers.Default, 100)
    }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

//    fun errorMsg(error: String) {
//        Toast.makeText(getApplication(), "Что-то пошло не так...", Toast.LENGTH_SHORT).show()
//    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContentAndSave(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.save(it)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun like(id: Long, likedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun repost(id: Long) {
                TODO("Not yet implemented")
            }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun updateShow() = viewModelScope.launch {
        repository.updateShow()
    }

//    fun clearEdit() {
//        edited.value = empty
//    }
}




