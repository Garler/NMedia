package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int,
    val authorAvatar: String?,
    val show: Boolean,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false
//    val reposts: Int,
//    val views: Int,
//    val video: String
    )

data class Attachment(
    val url: String,
    val type: AttachmentType
)

enum class AttachmentType {
    IMAGE
}