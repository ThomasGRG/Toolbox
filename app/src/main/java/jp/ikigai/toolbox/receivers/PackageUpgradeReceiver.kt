package jp.ikigai.toolbox.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.data.setupAlarms
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.services.AppNotifierService
import jp.ikigai.toolbox.utils.canScheduleExactAlarms

class PackageUpgradeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        ToolboxServiceState.log(
            serviceNameResId = R.string.app_notifier,
            msgResId = R.string.package_upgraded,
        )

        if (context == null || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        if (context.canScheduleExactAlarms) {
            ToolboxPreferences.getSunlightSchedule(context).setupAlarms(context)
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
