package ru.netology.nmedia.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PushMessage
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var appAuth: AppAuth
    private val channelId = "server"
    private val gson = Gson()
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val pushMessage = gson.fromJson(message.data["content"], PushMessage::class.java)
        val appAuthToken = appAuth.authStateFlow.value.id

        when (pushMessage.recipientId) {
            appAuthToken, null
            -> notification(pushMessage.content)
            else -> appAuth.sendPushToken()
        }

        message.data["action"]?.let {
            try {
                when (Actions.valueOf(it)) {
                    Actions.LIKE -> handleLike(
                        Gson().fromJson(
                            message.data["content"],
                            Like::class.java
                        )
                    )

                    Actions.NEW_POST -> newPost(
                        Gson().fromJson(
                            message.data["content"],
                            NewPost::class.java
                        )
                    )
                }
            } catch (e: RuntimeException) {
                println("ERROR")
            }
        }
        println(Gson().toJson(message))
    }

    private fun notification(content: String) {
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(
                content
            )
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        checkPermission()
        NotificationManagerCompat.from(this).notify(
            Random.nextInt(100_000),
            notification
        )
    }

    private fun handleLike(like: Like) {
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(
                getString(
                    R.string.notification_user_liked,
                    like.userName,
                    like.postAuthor
                )
            )
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        checkPermission()
        NotificationManagerCompat.from(this).notify(
            Random.nextInt(100_000),
            notification
        )
    }

    private fun newPost(post: NewPost) {
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(
                getString(
                    R.string.notification_new_post,
                    post.userName,
                    post.content
                )
            )
            .setContentIntent(pi)
            .setStyle(NotificationCompat.BigTextStyle().bigText(post.content))
            .setAutoCancel(true)
            .build()

        checkPermission()
        NotificationManagerCompat.from(this).notify(
            Random.nextInt(100_000),
            notification
        )
    }

    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

}

enum class Actions {
    LIKE,
    NEW_POST
}

data class Like(
    val userId: Int,
    val userName: String,
    val postId: Int,
    val postAuthor: String
)

data class NewPost(
    val userId: Int,
    val userName: String,
    val postId: Int,
    val content: String
)