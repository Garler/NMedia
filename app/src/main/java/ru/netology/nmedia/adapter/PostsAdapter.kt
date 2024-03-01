package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.load
import ru.netology.nmedia.util.loadAvatar
import kotlin.math.floor

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onMedia(post: String)
    fun onCardPost(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> throw IllegalArgumentException("unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when(viewType){
            R.layout.card_post -> {
                val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            R.layout.card_ad -> {
                val binding = CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding, onInteractionListener)
            }
            else -> error("unknown item type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = getItem(position)){
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("unknown item type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        binding.apply {
            image.load("${BASE_URL}/media/${ad.image}")
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    private val gson = Gson()
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            avatar.loadAvatar("${BASE_URL}/avatars/${post.authorAvatar}")
            published.text = post.published
            content.text = post.content
            image.let {
                if (post.attachment != null) {
                    it.visibility = View.VISIBLE
                    it.load("${BASE_URL}/media/${post.attachment.url}")
                } else it.visibility = View.GONE
            }
            icLikes.text = numberFormat(post.likes)
            icLikes.isChecked = post.likedByMe
//            icReposts.text = numberFormat(post.reposts)
//            icViews.text = numberFormat(post.views)
            icLikes.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            icReposts.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            binding.image.setOnClickListener {
                post.attachment ?: return@setOnClickListener
                onInteractionListener.onMedia(gson.toJson(post))
            }
            binding.cardPost.setOnClickListener {
                onInteractionListener.onCardPost(post)
            }
            menu.isVisible = post.ownedByMe
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

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
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