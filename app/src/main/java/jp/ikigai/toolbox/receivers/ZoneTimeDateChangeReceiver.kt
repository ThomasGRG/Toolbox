package jp.ikigai.toolbox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import jp.ikigai.toolbox.data.AppModule
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.utils.canScheduleExactAlarms
import jp.ikigai.toolbox.utils.setupSunlightSchedule

class ZoneTimeDateChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || !context.canScheduleExactAlarms) return
        when (intent?.action) {
            Intent.ACTION_TIME_CHANGED -> {
                AppModule.serviceLogDao.insert(
                    serviceType = ServiceType.TOOLBOX,
                    logMessage = LogMessage.DEVICE_TIME_CHANGED,
                )
                context.setupSunlightSchedule(ToolboxPreferences.getSunlightSchedule(context))
            }

            Intent.ACTION_TIMEZONE_CHANGED -> {
                AppModule.serviceLogDao.insert(
                    serviceType = ServiceType.TOOLBOX,
                    logMessage = LogMessage.DEVICE_TIME_ZONE_CHANGED,
                )
                context.setupSunlightSchedule(ToolboxPreferences.getSunlightSchedule(context))
            }

            Intent.ACTION_DATE_CHANGED -> {
                AppModule.serviceLogDao.insert(
                    serviceType = ServiceType.TOOLBOX,
                    logMessage = LogMessage.DEVICE_DATE_CHANGED,
                )
                context.setupSunlightSchedule(ToolboxPreferences.getSunlightSchedule(context))
            }
        }
    }
}
