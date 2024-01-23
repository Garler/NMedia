package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int,
    val authorAvatar: String?,
    val show: Boolean
//    val reposts: Int,
//    val views: Int,
//    val video: String
    )