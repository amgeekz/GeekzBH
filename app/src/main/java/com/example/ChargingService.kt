package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChargingService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private const val TAG = "ChargingService"
        private const val CHANNEL_ID = "charging_control_service_channel"
        private const val NOTIFICATION_ID = 101
        
        fun startService(context: Context) {
            val intent = Intent(context, ChargingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ChargingService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Ensure the charging daemon is running
        ChargingController.startDaemon(applicationContext)

        // Periodically update the notification in memory to show live telemetry
        serviceScope.launch {
            while (isActive) {
                try {
                    val data = NotificationData(
                        ChargingController.batteryLevel.value,
                        ChargingController.batteryStatus.value,
                        ChargingController.batteryTemp.value,
                        ChargingController.batteryCurrent.value,
                        ChargingController.batteryVoltage.value
                    )
                    updateNotification(data)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in notification loop", e)
                }
                delay(2000) // Syncs perfectly with ChargingController's telemetry refresh rate
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialData = NotificationData(
            ChargingController.batteryLevel.value,
            ChargingController.batteryStatus.value,
            ChargingController.batteryTemp.value,
            ChargingController.batteryCurrent.value,
            ChargingController.batteryVoltage.value
        )
        val notification = buildNotification(initialData)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Charging Controller Status",
                NotificationManager.IMPORTANCE_LOW // Keeps notification silent and non-intrusive
            ).apply {
                description = "Shows real-time battery voltage, current, and bypass modes"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(data: NotificationData): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "Battery Status: ${data.level}% (${data.status})"
        val content = "${data.temp}°C | ${data.voltage} mV | ${data.current} mA"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_charging_notification) // Monochrome vector suitable for status bar
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(data: NotificationData) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(data))
    }

    private data class NotificationData(
        val level: Int,
        val status: String,
        val temp: Float,
        val current: Int,
        val voltage: Int
    )
}
