package jp.ikigai.toolbox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import jp.ikigai.toolbox.data.AppModule
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.services.SunlightMonitorService
import jp.ikigai.toolbox.utils.setupSunlightSchedule
import jp.ikigai.toolbox.utils.startToolboxService
import jp.ikigai.toolbox.utils.stopToolboxService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        AppModule.serviceLogDao.insert(
            serviceType = ServiceType.SUNLIGHT_MON,
            logMessage = LogMessage.RECEIVED_ALARM,
        )
        val sunlightSchedule = ToolboxPreferences.getSunlightSchedule(context)
        if (sunlightSchedule.isTimeBetween()) {
            context.startToolboxService()
        } else {
            context.stopToolboxService(SunlightMonitorService.ACTION_STOP)
            context.setupSunlightSchedule(sunlightSchedule)
        }
    }
}
