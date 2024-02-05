package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.activity.EditPostFragment.Companion.text
import ru.netology.nmedia.databinding.FragmentMediaBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.load

class MediaFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMediaBinding.inflate(inflater, container, false)
        val gson = Gson()

        val media = arguments?.text
        if (media != null) {
            val post = gson.fromJson(media, Post::class.java)
            binding.media.load("${BASE_URL}/media/${post.attachment?.url}")
        }
        return binding.root
    }
}