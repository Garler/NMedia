package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import kotlin.math.floor

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onVideo(post: Post)
    fun onCardPost(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            icLikes.text = numberFormat(post.likes)
            icLikes.isChecked = post.likedByMe
            icReposts.text = numberFormat(post.reposts)
            icViews.text = numberFormat(post.views)
            icLikes.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            icReposts.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            playButton.visibility = if (post.video.isNotEmpty()) View.VISIBLE else View.GONE
            playButton.setOnClickListener {
                onInteractionListener.onVideo(post)
            }
            binding.cardPost.setOnClickListener {
                onInteractionListener.onCardPost(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.option_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}

fun numberFormat(number: Int): String =
    when {
        number >= 1_000_000 && (number / 100_000) % 10 == 0 -> (number / 1_000_000).toString() + "M"
        number >= 1_000_000 -> (floor(number.toDouble() / 100_000) / 10).toString() + "M"
        number >= 10_000 || (number >= 1_000 && (number / 100) % 10 == 0) -> (number / 1_000).toString() + "K"
        number >= 1_000 -> (floor(number.toDouble() / 100) / 10).toString() + "K"
        else -> number.toString()
    }