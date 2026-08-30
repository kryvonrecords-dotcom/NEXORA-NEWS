package com.nexoranews.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nexoranews.app.MainActivity
import com.nexoranews.app.R
import com.nexoranews.app.data.remote.ApiClient
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class NexoraFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "nexora_news_notifications"
        private const val CHANNEL_NAME = "Nexora News"
        private const val SERVER_BASE_URL = "https://nexora-news.onrender.com"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        android.util.Log.d("NexoraFCM", "FCM RECEBIDO - notification=${message.notification != null}, data=${message.data}")
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Nexora News"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Há uma nova notícia no Nexora News."

        val imageUrl = message.notification?.imageUrl?.toString()
            ?: message.data["imageUrl"]
            ?: ""

        val clickUrl = message.data["clickUrl"]
            ?: message.data["newsSlug"]?.let {
                "/noticia/$it"
            }
            ?: "/"

        val isBreaking = message.data["isBreaking"] == "true"

        showNotification(
            title = title,
            body = body,
            imageUrl = imageUrl,
            clickUrl = clickUrl,
            isBreaking = isBreaking
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        android.util.Log.d(
            "NexoraFCM",
            "Novo token recebido: $token"
        )

        thread {
            try {
                val safeToken = token
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")

                val json = """
                    {
                      "token": "$safeToken",
                      "userAgent": "Nexora News Android"
                    }
                """.trimIndent()

                val result = ApiClient.postJson(
                    "/fcm/token",
                    json
                )

                result.onSuccess {
                    android.util.Log.d(
                        "NexoraFCM",
                        "Token FCM registrado no servidor."
                    )
                }

                result.onFailure { error ->
                    android.util.Log.e(
                        "NexoraFCM",
                        "Falha ao registrar token FCM.",
                        error
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e(
                    "NexoraFCM",
                    "Erro ao registrar token FCM.",
                    e
                )
            }
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        imageUrl: String,
        clickUrl: String,
        isBreaking: Boolean
    ) {
        createNotificationChannel()

        val intent = Intent(
            this,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra(
                "notification_click_url",
                clickUrl
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val priority =
            if (isBreaking) {
                NotificationCompat.PRIORITY_MAX
            } else {
                NotificationCompat.PRIORITY_HIGH
            }

        val builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
            )
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(
                if (isBreaking) {
                    NotificationCompat.CATEGORY_ALARM
                } else {
                    NotificationCompat.CATEGORY_MESSAGE
                }
            )

        if (imageUrl.isBlank()) {
            NotificationManagerCompat
                .from(this)
                .notify(
                    System.currentTimeMillis().toInt(),
                    builder.build()
                )
            return
        }

        thread {
            val fullImageUrl =
                if (imageUrl.startsWith("/")) {
                    SERVER_BASE_URL + imageUrl
                } else {
                    imageUrl
                }

            val bitmap = downloadBitmap(fullImageUrl)

            if (bitmap != null) {
                val imageBuilder =
                    NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                    )
                        .setSmallIcon(
                            R.drawable.ic_notifications
                        )
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon(null as android.graphics.Bitmap?)
                        )
                        .setPriority(priority)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setCategory(
                            if (isBreaking) {
                                NotificationCompat.CATEGORY_ALARM
                            } else {
                                NotificationCompat.CATEGORY_MESSAGE
                            }
                        )

                NotificationManagerCompat
                    .from(this)
                    .notify(
                        System.currentTimeMillis().toInt(),
                        imageBuilder.build()
                    )

            } else {
                NotificationManagerCompat
                    .from(this)
                    .notify(
                        System.currentTimeMillis().toInt(),
                        builder.build()
                    )
            }
        }
    }

    private fun downloadBitmap(
        imageUrl: String
    ): Bitmap? {
        var connection: HttpURLConnection? = null

        return try {
            connection =
                URL(imageUrl)
                    .openConnection() as HttpURLConnection

            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            connection.doInput = true
            connection.connect()

            if (connection.responseCode in 200..299) {
                connection.inputStream.use {
                    BitmapFactory.decodeStream(it)
                }
            } else {
                null
            }

        } catch (e: Exception) {
            android.util.Log.e(
                "NexoraFCM",
                "Erro ao baixar imagem da notificação.",
                e
            )
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Notificações de novas notícias do Nexora News"
            }

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }
    }
}
