package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0L,
    authorId = 0,
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

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: PostRepository, appAuth: AppAuth) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = appAuth
        .authStateFlow
        .flatMapLatest {
            auth ->
            repository.data.map {posts ->
                posts.map { it.copy(ownedByMe = auth.id == it.authorId) }
            }
        }
        .flowOn(Dispatchers.Default)

//    val newerCount = data.switchMap {
//        repository.getNewer(it.posts.firstOrNull()?.id ?: 0L)
//            .asLiveData(Dispatchers.Default, 100)
//    }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    init {
        loadPosts()
    }

    fun setPhoto(uri: Uri, file: File){
        _photo.value = PhotoModel(uri, file)
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
            viewModelScope.launch {
                try {
                    val photoModel = _photo.value
                    if (photoModel == null){
                    repository.save(it)
                    } else {
                        repository.saveWithAttachment(it, photoModel)
                    }

                    _dataState.value = FeedModelState()
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun clearPhoto() {
        _photo.value = null
    }

    fun like(post: Post) = viewModelScope.launch {
        try {
            repository.likeById(post)
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




