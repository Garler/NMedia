package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel

interface PostRepository {
    val data: Flow<PagingData<Post>>

    fun getNewer(id: Long): Flow<Int>

    suspend fun getAll(show: Boolean = true)
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(post: Post)
    suspend fun updateShow()
    suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel)
}