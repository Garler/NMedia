package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int,
    val authorAvatar: String?,
    val show: Boolean,
    @Embedded
    var attachment: Attachment? = null
//    val reposts: Int,
//    val views: Int,
//    val video: String
) {
    fun toDto() = Post(id, authorId, author, content, published, likedByMe, likes, authorAvatar, show, attachment)

    companion object {
        fun fromDto(post: Post) = PostEntity(
            post.id,
            post.authorId,
            post.author,
            post.content,
            post.published,
            post.likedByMe,
            post.likes,
            post.authorAvatar,
            post.show,
            post.attachment
//            post.reposts,
//            post.views,
//            post.video
        )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)