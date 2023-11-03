package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callback: RepositoryCallback<List<Post>>)
    fun likeAsync(id: Long, likedByMe: Boolean, callback: RepositoryCallback<Post>)
    fun repostAsync(id: Long, callback: RepositoryCallback<Post>)
    fun removeByIdAsync(id: Long, callback: RepositoryCallback<List<Post>>)
    fun saveAsync(post: Post, callback: RepositoryCallback<Post>)

    interface RepositoryCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Exception)
    }
}