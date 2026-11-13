package jp.ikigai.toolbox.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.TrafficStats
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import jp.ikigai.toolbox.MainActivity
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.preferences.ToolboxPreferences

class NetSpeedMonService(private val context: Context) {

    companion object {
        const val ACTION_STOP = "ACTION_STOP_NETSPEEDMON"
        const val CHANNEL_ID = "netspeedmon_notification_channel"
        const val NOTIFICATION_ID = 16249
    }

    private val handler = Handler(Looper.getMainLooper())

    private var previousRxBytes: Long = 0
    private var previousTxBytes: Long = 0
    private var sessionStartRxBytes: Long = 0
    private var sessionStartTxBytes: Long = 0
    private val intervalMs: Long = 1000

    private val iconKiBLimit = 1022976
    private val iconMiBStart = 1022977
    private val icon10MiBLimit = 10485760
    private val format1MiBDouble = 1048576.0
    private val format1MiBInt = 1048576
    private val format1GiBDouble = 1073741824.0
    private val format1GiBInt = 1073741824

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun onStartCommand(intent: Intent?) {
        when (intent?.action) {
            ACTION_STOP -> {
                ToolboxPreferences.setNetSpeedMonEnabled(context, false)
                stopMon()
            }

            Intent.ACTION_SCREEN_OFF -> {
                pauseMon()
            }

            Intent.ACTION_SCREEN_ON -> {
                resumeMon()
            }

            else -> {
                startMon()
            }
        }
    }

    fun onDestroy() {
        if (!ToolboxServiceState.netSpeedMonState.value) return
        stopMon()
    }

    private fun startMon() {
        if (!ToolboxPreferences.isNetSpeedMonEnabled(context) || ToolboxServiceState.netSpeedMonState.value) return
        createNotificationChannels()
        updateNotification(
            Pair("-", "-"),
            "--",
            "--",
            "--",
            "--"
        )
        previousRxBytes = TrafficStats.getTotalRxBytes()
        previousTxBytes = TrafficStats.getTotalTxBytes()
        sessionStartRxBytes = TrafficStats.getTotalRxBytes()
        sessionStartTxBytes = TrafficStats.getTotalTxBytes()
        handler.postDelayed(calcSpeedRunnable, intervalMs)
        ToolboxServiceState.updateNetSpeedMonState(true)
        ToolboxServiceState.log(
            serviceNameResId = R.string.net_speed_mon,
            msgResId = R.string.monitor_running
        )
    }

    private fun pauseMon() {
        if (!ToolboxPreferences.isNetSpeedMonEnabled(context) || !ToolboxServiceState.netSpeedMonState.value) return
        handler.removeCallbacks(calcSpeedRunnable)
        ToolboxServiceState.log(
            serviceNameResId = R.string.net_speed_mon,
            msgResId = R.string.monitor_paused
        )
    }

    private fun resumeMon() {
        if (!ToolboxPreferences.isNetSpeedMonEnabled(context) || !ToolboxServiceState.netSpeedMonState.value) return
        handler.post(calcSpeedRunnable)
        ToolboxServiceState.log(
            serviceNameResId = R.string.net_speed_mon,
            msgResId = R.string.monitor_running
        )
    }

    private fun stopMon() {
        sessionStartRxBytes = 0
        sessionStartTxBytes = 0

        handler.removeCallbacks(calcSpeedRunnable)

        notificationManager.cancel(NOTIFICATION_ID)

        ToolboxServiceState.updateNetSpeedMonState(false)
        ToolboxServiceState.log(
            serviceNameResId = R.string.net_speed_mon,
            msgResId = R.string.monitor_stopped
        )
    }

    private val calcSpeedRunnable = object : Runnable {
        override fun run() {
            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()

            val downloadSpeed = currentRxBytes - previousRxBytes
            val uploadSpeed = currentTxBytes - previousTxBytes

            val sessionDownload = currentRxBytes - sessionStartRxBytes
            val sessionUpload = currentTxBytes - sessionStartTxBytes

            updateNotification(
                totalSpeed = formatSpeedForIcon(downloadSpeed + uploadSpeed),
                downloadSpeed = formatSpeed(downloadSpeed),
                uploadSpeed = formatSpeed(uploadSpeed),
                sessionDownload = formatSessionUsage(sessionDownload),
                sessionUpload = formatSessionUsage(sessionUpload)
            )

            previousRxBytes = currentRxBytes
            previousTxBytes = currentTxBytes

            handler.postDelayed(this, intervalMs)
        }
    }

    private fun updateNotification(
        totalSpeed: Pair<String, String>,
        downloadSpeed: String,
        uploadSpeed: String,
        sessionDownload: String,
        sessionUpload: String
    ) {
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                totalSpeed,
                downloadSpeed,
                uploadSpeed,
                sessionDownload,
                sessionUpload
            )
        )
    }

    private fun formatSpeedForIcon(speed: Long): Pair<String, String> {
        return when {
            speed < 1000 -> Pair("$speed", "B/s")
            speed in 1000..1023 -> {
                val speedInK = (speed / 1024.0).toString()
                val endIndex = speedInK.indexOf(".") + 2
                Pair(speedInK.take(endIndex.coerceAtMost(speedInK.length)), "K/s")
            }

            speed in 1024..iconKiBLimit -> {
                Pair("${speed / 1024}", "K/s")
            }

            speed in iconMiBStart..<icon10MiBLimit -> {
                val speedInM = (speed / format1MiBDouble).toString()
                val endIndex = speedInM.indexOf(".") + 2
                Pair(speedInM.take(endIndex.coerceAtMost(speedInM.length)), "M/s")
            }

            else -> {
                Pair("${speed / format1MiBInt}", "M/s")
            }
        }
    }

    private fun formatSpeed(speed: Long): String {
        return when {
            speed < 1024 -> "$speed B/s"
            speed in 1024..<format1MiBInt -> {
                val speedInK = (speed / 1024.0).toString()
                val endIndex = speedInK.indexOf(".") + 3
                "${speedInK.take(endIndex.coerceAtMost(speedInK.length))} KiB/s"
            }

            else -> {
                val speedInM = (speed / format1MiBDouble).toString()
                val endIndex = speedInM.indexOf(".") + 3
                "${speedInM.take(endIndex.coerceAtMost(speedInM.length))} MiB/s"
            }
        }
    }

    private fun formatSessionUsage(totalBytes: Long): String {
        return when {
            totalBytes < 1024 -> "$totalBytes B"
            totalBytes in 1024..<format1MiBInt -> {
                val totalBytesInK = (totalBytes / 1024.0).toString()
                val endIndex = totalBytesInK.indexOf(".") + 3
                "${totalBytesInK.take(endIndex.coerceAtMost(totalBytesInK.length))} KiB"
            }

            totalBytes in format1MiBInt..<format1GiBInt -> {
                val totalBytesInM = (totalBytes / format1MiBDouble).toString()
                val endIndex = totalBytesInM.indexOf(".") + 3
                "${totalBytesInM.take(endIndex.coerceAtMost(totalBytesInM.length))} MiB"
            }

            else -> {
                val totalBytesInG = (totalBytes / format1GiBDouble).toString()
                val endIndex = totalBytesInG.indexOf(".") + 3
                "${totalBytesInG.take(endIndex.coerceAtMost(totalBytesInG.length))} GiB"
            }
        }
    }

    private fun createSpeedIcon(speed: Pair<String, String>): IconCompat {
        val bitmap = createBitmap(48, 48)
        val canvas = Canvas(bitmap)
        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 23f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawText(speed.first, 24f, 20f, numberPaint)
        canvas.drawText(speed.second, 24f, 46f, unitPaint)

        return IconCompat.createWithBitmap(bitmap)
    }

    private fun createNotificationChannels() {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.net_speed_mon_monitor),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.setSound(null, null)
        notificationChannel.enableVibration(false)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createNotification(
        totalSpeed: Pair<String, String>,
        downloadSpeed: String,
        uploadSpeed: String,
        sessionDownload: String,
        sessionUpload: String
    ): Notification {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val tapPendingIntent =
            PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE)
        val stopActionIntent = Intent(context, ToolboxService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            1,
            stopActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.outline_stop_circle_24,
            context.getString(R.string.stop),
            stopPendingIntent
        ).build()
        val extras = Bundle().apply {
            putBoolean("android.app.preferSmallIcon", true)
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(createSpeedIcon(totalSpeed))
            .setOngoing(true)
            .setContentTitle(
                context.getString(
                    R.string.net_speed_mon_content,
                    downloadSpeed,
                    uploadSpeed,
                ),
            )
            .setContentText(
                context.getString(
                    R.string.net_speed_mon_content,
                    sessionDownload,
                    sessionUpload,
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(tapPendingIntent)
            .setOnlyAlertOnce(true)
            .setAllowSystemGeneratedContextualActions(false)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(stopAction)
            .addExtras(extras)
            .build()
    }
}
