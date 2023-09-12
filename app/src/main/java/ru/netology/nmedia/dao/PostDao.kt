package ru.netology.nmedia.dao

import ru.netology.nmedia.dto.Post

interface PostDao {
    fun getAll(): List<Post>
    fun like(id: Long)
    fun repost(id: Long)
    fun removeById(id: Long)
    fun save(post: Post): Post
}