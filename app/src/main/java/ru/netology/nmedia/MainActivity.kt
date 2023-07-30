package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import kotlin.math.floor

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            likedByMe = false,
            likes = 1099,
            reposts = 1000000
        )
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likes.text = numberFormat(post.likes)
            if (post.likedByMe) {
                icLikes.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
            reposts.text = numberFormat(post.reposts)

            icLikes.setOnClickListener {
                post.likedByMe = !post.likedByMe
                if (post.likedByMe) post.likes++ else post.likes--
                icLikes.setImageResource(
                    if (post.likedByMe) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24
                )
                likes.text = numberFormat(post.likes)
            }

            icReposts.setOnClickListener{
                post.reposts++
                reposts.text = numberFormat(post.reposts)
            }
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