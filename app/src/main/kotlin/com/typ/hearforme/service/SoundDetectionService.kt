package com.typ.hearforme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.policy.DetectionPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SoundDetectionService : Service() {

    private val classifier: AudioClassifier by inject()
    private val policy: DetectionPolicy by inject()

    // Using simple job for now
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lastAlertTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("HearForMe", "Service created")
        startForegroundService()

        serviceScope.launch {
            classifier.events.collectLatest { event ->
                Log.d("HearForMe", "Detected: ${event.type} Confidence: ${event.confidence}")
                val shouldAlert = policy.shouldAlert(event, if (lastAlertTime == 0L) null else lastAlertTime)
                if (shouldAlert) {
                    Log.i("HearForMe", "ALERT TRIGGERED for ${event.type}")
                    lastAlertTime = System.currentTimeMillis()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        classifier.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.stop()
        serviceScope.cancel()
    }

    private fun startForegroundService() {
        val channelId = "sound_detection_channel"
        val channelName = "Sound Detection"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Hear for Me is listening")
            .setContentText("Detecting sounds in background...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1, notification)
        }
    }
}
