package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity WHERE show = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE show = 0 ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("UPDATE PostEntity SET show = 0")
    suspend fun show()

    @Query("UPDATE PostEntity SET likes = likes + 1, likedByMe = NOT likedByMe WHERE id = :id")
    suspend fun likeById(id: Long)

    @Query("UPDATE PostEntity SET likes = likes - 1, likedByMe = NOT likedByMe WHERE id = :id")
    suspend fun unlikeById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun clear()
}