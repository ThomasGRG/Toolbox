package jp.ikigai.toolbox.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import jp.ikigai.toolbox.data.AppModule
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.services.AppNotifierService
import jp.ikigai.toolbox.utils.canScheduleExactAlarms
import jp.ikigai.toolbox.utils.setupSunlightSchedule

class PackageUpgradeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        AppModule.serviceLogDao.insert(
            serviceType = ServiceType.APP_NOTIFIER,
            logMessage = LogMessage.PACKAGE_UPGRADED,
        )

        if (context == null || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        if (context.canScheduleExactAlarms) {
            context.setupSunlightSchedule(ToolboxPreferences.getSunlightSchedule(context))
        }

        context.apply {
            startForegroundService(
                Intent(
                    this,
                    ToolboxPreferences::class.java
                ).apply {
                    action = AppNotifierService.ACTION_PACKAGE_UPGRADED
                }
            )
        }
    }
}
