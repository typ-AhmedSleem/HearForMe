package com.typ.hearforme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.policy.DetectionPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SoundDetectionService : Service() {

    private val classifier: AudioClassifier by inject()
    private val policy: DetectionPolicy by inject()
    private val alertManager: AlertManager by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastAlertTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("HearForMe", "Service created")
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle stop action
        if (intent?.action == ACTION_STOP) {
            Log.d("HearForMe", "Stop action received, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        classifier.start()

        serviceScope.launch {
            classifier.events.collect { event ->
                Log.d("HearForMe", "Detected: ${event.type.displayName} (${event.confidence})")
                if (policy.shouldAlert(event, lastAlertTime)) {
                    alertManager.onSoundDetected(event)
                    lastAlertTime = event.timestamp
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.stop()
        serviceScope.cancel()
        Log.d("HearForMe", "Service destroyed")
    }

    private fun startForegroundService() {
        val channelId = "sound_detection_channel"
        val channelName = "Sound Detection"

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        // Stop action intent
        val stopIntent = Intent(this, SoundDetectionService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Hear for Me is listening")
            .setContentText("Detecting sounds in background...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_pause, "Stop Detection", stopPendingIntent)
            .build()

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
    }

    companion object {
        const val ACTION_STOP = "com.typ.hearforme.ACTION_STOP_DETECTION"
    }
}
