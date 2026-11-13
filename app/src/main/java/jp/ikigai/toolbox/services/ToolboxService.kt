package jp.ikigai.toolbox.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ToolboxServiceState

class ToolboxService : Service() {

    companion object {
        const val ACTION_STOP = "ACTION_STOP_TOOLBOX"
        const val ACTION_BOOT_COMPLETED = "ACTION_BOOT_COMPLETED_TOOLBOX"
        const val CHANNEL_ID = "toolbox_notification_channel"
        const val NOTIFICATION_ID = 82664
    }

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    ToolboxServiceState.log(
                        serviceNameResId = R.string.app_name,
                        msgResId = R.string.screen_off
                    )
                    netSpeedMonService.onStartCommand(intent)
                    sunlightMonitorService.onDestroy()
                }

                Intent.ACTION_SCREEN_ON -> {
                    ToolboxServiceState.log(
                        serviceNameResId = R.string.app_name,
                        msgResId = R.string.screen_on
                    )
                    netSpeedMonService.onStartCommand(intent)
                    sunlightMonitorService.onStartCommand(null)
                }
            }
        }
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var appNotifierService: AppNotifierService
    private lateinit var netSpeedMonService: NetSpeedMonService
    private lateinit var soundFixerService: SoundFixerService
    private lateinit var sunlightMonitorService: SunlightMonitorService

    override fun onCreate() {
        super.onCreate()
        ToolboxServiceState.log(
            serviceNameResId = R.string.app_name,
            msgResId = R.string.service_created
        )
        notificationManager = getSystemService(NotificationManager::class.java)
        appNotifierService = AppNotifierService(this)
        netSpeedMonService = NetSpeedMonService(this)
        soundFixerService = SoundFixerService(this)
        sunlightMonitorService = SunlightMonitorService(this)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }

        registerReceiver(screenStateReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.app_name,
                msgResId = R.string.service_stopped
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else {
            startToolbox(intent)
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startToolbox(intent: Intent?) {
        createNotificationChannels()
        startForegroundCompat()
        if (!ToolboxServiceState.toolboxState.value) {
            ToolboxServiceState.updateToolboxState(true)
            ToolboxServiceState.log(
                serviceNameResId = R.string.app_name,
                msgResId = R.string.service_running
            )
        }
        appNotifierService.onStartCommand(intent)
        netSpeedMonService.onStartCommand(intent)
        soundFixerService.onStartCommand(intent)
        sunlightMonitorService.onStartCommand(intent)
    }

    private fun createNotificationChannels() {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.toolbox_background_service),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun startForegroundCompat() {
        val stopActionIntent = Intent(this, ToolboxService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.outline_stop_circle_24,
            getString(R.string.stop),
            stopPendingIntent
        ).build()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.home_repair_service)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.toolbox_background_service_enabled))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAllowSystemGeneratedContextualActions(false)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(stopAction)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                notification,
            )
        }
    }

    override fun onDestroy() {
        ToolboxServiceState.updateToolboxState(false)
        ToolboxServiceState.log(
            serviceNameResId = R.string.app_name,
            msgResId = R.string.service_destroyed
        )
        appNotifierService.onDestroy()
        netSpeedMonService.onDestroy()
        soundFixerService.onDestroy()
        sunlightMonitorService.onDestroy()
        unregisterReceiver(screenStateReceiver)
        super.onDestroy()
    }
}
