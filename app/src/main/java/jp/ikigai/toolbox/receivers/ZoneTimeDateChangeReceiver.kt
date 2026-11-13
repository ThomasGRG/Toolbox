package jp.ikigai.toolbox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.data.setupAlarms
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.utils.canScheduleExactAlarms

class ZoneTimeDateChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || !context.canScheduleExactAlarms) return
        when (intent?.action) {
            Intent.ACTION_TIME_CHANGED -> {
                ToolboxServiceState.log(
                    serviceNameResId = R.string.app_name,
                    msgResId = R.string.time_changed,
                )
                ToolboxPreferences.getSunlightSchedule(context).setupAlarms(context)
            }

            Intent.ACTION_TIMEZONE_CHANGED -> {
                ToolboxServiceState.log(
                    serviceNameResId = R.string.app_name,
                    msgResId = R.string.time_zone_changed,
                )
                ToolboxPreferences.getSunlightSchedule(context).setupAlarms(context)
            }

            Intent.ACTION_DATE_CHANGED -> {
                ToolboxServiceState.log(
                    serviceNameResId = R.string.app_name,
                    msgResId = R.string.date_changed,
                )
                ToolboxPreferences.getSunlightSchedule(context).setupAlarms(context)
            }
        }
    }
}
