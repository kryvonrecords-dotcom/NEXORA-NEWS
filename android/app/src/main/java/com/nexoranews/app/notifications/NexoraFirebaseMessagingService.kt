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
import com.nexoranews.app.ui.detail.ArticleDetailActivity
import com.nexoranews.app.R
import com.nexoranews.app.data.remote.ApiClient
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class NexoraFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "nexora_news_notifications"
        private const val CHANNEL_NAME = "Nexora News"
        private const val SERVER_BASE_URL = "https://nexora-news.nexoranews.blitz.cloud"
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

        android.util.Log.d("NexoraFCM", "IMAGEM FCM RECEBIDA: $imageUrl")
        val newsSlug = message.data["newsSlug"]
        val clickUrl = message.data["clickUrl"]
            ?: newsSlug?.let {
                "/noticia/$it"
            }
            ?: "/"

        val isBreaking = message.data["isBreaking"] == "true"

        showNotification(
            title = title,
            body = body,
            imageUrl = imageUrl,
            clickUrl = clickUrl,
            newsSlug = newsSlug,
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
        newsSlug: String?,
        isBreaking: Boolean
    ) {
        createNotificationChannel()

        val slug = newsSlug
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: clickUrl
                .substringBefore("?")
                .substringAfterLast("/")
                .trim()

        val intent = Intent(
            this,
            ArticleDetailActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra(
                "extra_news_slug",
                slug
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
                    imageUrl.replaceFirst("http://", "https://")
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
                        .setLargeIcon(bitmap)
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
            connection.readTimeout = 20000
            connection.doInput = true
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "NexoraNews/1.0")
            connection.connect()

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                android.util.Log.e(
                    "NexoraFCM",
                    "Falha HTTP ao baixar imagem: $responseCode"
                )
                return null
            }

            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var count: Int
                var total = 0

                while (input.read(buffer).also { count = it } != -1) {
                    total += count

                    if (total > 8 * 1024 * 1024) {
                        android.util.Log.e(
                            "NexoraFCM",
                            "Imagem excede o limite de 8 MB."
                        )
                        return@use null
                    }

                    output.write(buffer, 0, count)
                }

                output.toByteArray()
            } ?: return null

            android.util.Log.d(
                "NexoraFCM",
                "Imagem baixada: ${bytes.size} bytes"
            )

            val bitmap = BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            ) ?: return null

            val maxSize = 1024

            val scale = minOf(
                1f,
                maxSize.toFloat() / bitmap.width,
                maxSize.toFloat() / bitmap.height
            )

            if (scale < 1f) {
                val scaled = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )

                if (scaled !== bitmap) {
                    bitmap.recycle()
                }

                scaled
            } else {
                bitmap
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
