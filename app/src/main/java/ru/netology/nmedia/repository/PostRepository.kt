package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun like(id: Long): Post
    fun repost(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)
}