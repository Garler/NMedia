package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.viewModels
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.math.floor

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel by viewModels<PostViewModel>()

        viewModel.data.observe(this) {post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                likes.text = numberFormat(post.likes)
                icLikes.setImageResource(if (post.likedByMe) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24)
                reposts.text = numberFormat(post.reposts)
            }
        }
        binding.icLikes.setOnClickListener {
            viewModel.like()
        }

        binding.icReposts.setOnClickListener {
            viewModel.repost()
        }
    }
    fun numberFormat (number: Int): String =
        when {
            number >= 1_000_000 -> (floor(number.toDouble() / 100_000) / 10).toString() + "M"
            number >= 10_000 -> (number / 1_000).toString() + "K"
            number >= 1_000 -> (floor(number.toDouble() / 100) / 10).toString() + "K"
            else -> number.toString()
        }
}