package jp.ikigai.toolbox.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import jp.ikigai.toolbox.data.setupAlarms
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.services.ToolboxService
import jp.ikigai.toolbox.utils.canScheduleExactAlarms
import jp.ikigai.toolbox.utils.startToolboxService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        if (context.canScheduleExactAlarms && ToolboxPreferences.isSunlightMonEnabled(context)) {
            ToolboxPreferences.getSunlightSchedule(context).setupAlarms(context)
        }
        context.startToolboxService(ToolboxService.ACTION_BOOT_COMPLETED)
    }
}
