package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val postApiService: PostApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    override val appDb: AppDb
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            service = postApiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            db = appDb
        )
    ).flow
        .map { it.map(PostEntity::toDto) }

    override suspend fun getAll(show: Boolean) {
        try {
            val response = postApiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = postApiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.map { it.copy(show = true) }.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
//        .flowOn(Dispatchers.Default)

    override suspend fun likeById(post: Post) {
        try {
            if (post.likedByMe) {
                dao.unlikeById(post.id)
                val response = postApiService.unLikeById(post.id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(PostEntity.fromDto(body))
            } else {
                dao.likeById(post.id)
                val response = postApiService.likeById(post.id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(PostEntity.fromDto(body))
            }
        } catch (e: IOException) {
            dao.insert(PostEntity.fromDto(post))
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(PostEntity.fromDto(post))
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun updateShow() {
        dao.show()
    }

    override suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel) {
        try {
            val mediaResponse = saveMedia(photoModel.file)
            if (!mediaResponse.isSuccessful) {
                throw ApiError(mediaResponse.code(), mediaResponse.message())
            }

            val media = mediaResponse.body() ?: throw ApiError(
                mediaResponse.code(),
                mediaResponse.message()
            )
            val response = postApiService.save(
                post.copy(
                    attachment = Attachment(
                        media.id,
                        AttachmentType.IMAGE
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun saveMedia(file: File): Response<Media> {
        val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        return postApiService.saveMedia(part)
    }

//    override fun repostAsync(id: Long, callback: PostRepository.RepositoryCallback<Post>) {
//        TODO()
//    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
        try {
            val response = postApiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nmedia.error.UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = postApiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}