package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel

interface PostRepository {
    val appDb: AppDb
    val data: Flow<PagingData<FeedItem>>

    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(post: Post)
    suspend fun updateShow()
    suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel)
}