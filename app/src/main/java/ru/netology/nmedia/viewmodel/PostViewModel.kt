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

private val empty = Post(
    id = 0L,
    content = "",
    author = "",
    likedByMe = false,
    published = "",
    likes = 0,
    authorAvatar = null
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
        _data.postValue(FeedModelState(loading = true))
        repository.getAllAsync(object : PostRepository.RepositoryCallback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.postValue(FeedModelState(posts = result, empty = result.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModelState(error = true))
            }
        })
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContentAndSave(content: String) {
        edited.value?.let { editedPost ->
            val text: String = content.trim()
            if (editedPost.content != text) {
                repository.saveAsync(
                    editedPost.copy(content = text),
                    object : PostRepository.RepositoryCallback<Post> {
                        override fun onSuccess(result: Post) {
                            val value = _data.value

                            val updatePosts = value?.posts?.map {
                                if (it.id == editedPost.id) {
                                    result
                                } else {
                                    it
                                }
                            }.orEmpty()

                            val resultList = if (value?.posts == updatePosts) {
                                listOf(result) + updatePosts
                            } else {
                                updatePosts
                            }
                            _data.postValue(
                                value?.copy(posts = resultList)
                            )
                        }

                        override fun onError(e: Exception) {
                            _data.postValue(FeedModelState(error = true))
                        }
                    })
                _postCreated.postValue(Unit)
            }
            edited.postValue(empty)
        }
    }

    fun like(id: Long, likedByMe: Boolean) {
        repository.likeAsync(id, likedByMe, object : PostRepository.RepositoryCallback<Post> {
            override fun onSuccess(result: Post) {
                val updatePosts = _data.value?.posts?.map {
                    if (it.id == id) {
                        result
                    } else {
                        it
                    }
                }.orEmpty()
                val resultList = if (_data.value?.posts == updatePosts) {
                    listOf(result) + updatePosts
                } else {
                    updatePosts
                }
                _data.postValue(
                    _data.value?.copy(posts = resultList)
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModelState(error = true))
            }
        })
    }

    fun repost(id: Long) =
        repository.repostAsync(id, object : PostRepository.RepositoryCallback<Post> {
            override fun onSuccess(result: Post) {
                TODO("Not yet implemented")
            }

            override fun onError(e: Exception) {
                TODO("Not yet implemented")
            }
        })

    fun removeById(id: Long) {
        val old = data.value?.posts
        repository.removeByIdAsync(id, object : PostRepository.RepositoryCallback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                if (old != null) {
                    _data.postValue(FeedModelState(posts = old.filter { it.id != id }))
                }
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModelState(error = true))
            }
        })
    }
//    fun clearEdit() {
//        edited.value = empty
//    }
}