package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.R

class AlertService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "CRITICAL SAFETY ALERT"
        val body = message.notification?.body ?: "Evacuate immediately to Assembly Point Alpha."

        // Play alarm sound even on silent mode
        try {
            val player = MediaPlayer.create(this, R.raw.siren_alert).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                start()
            }
            // Stop after 15 seconds to prevent endless looping if unacknowledged
            Handler(mainLooper).postDelayed({
                try {
                    if (player.isPlaying) player.stop()
                    player.release()
                } catch (_: Exception) {}
            }, 15000)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showFullScreenNotification(title, body)
    }

    private fun showFullScreenNotification(title: String, body: String) {
        val channelId = "vediraksha_critical_alert_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Critical Safety Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Plays high-priority factory evacuation sirens"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(911, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Upload token to backend server
    }
}
