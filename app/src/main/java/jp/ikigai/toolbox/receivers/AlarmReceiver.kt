package jp.ikigai.toolbox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.data.isTimeBetween
import jp.ikigai.toolbox.data.setupAlarms
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.services.SunlightMonitorService
import jp.ikigai.toolbox.utils.startToolboxService
import jp.ikigai.toolbox.utils.stopToolboxService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        ToolboxServiceState.log(
            serviceNameResId = R.string.sunlight_monitor,
            msgResId = R.string.alarm_received,
        )
        val sunlightSchedule = ToolboxPreferences.getSunlightSchedule(context)
        if (sunlightSchedule.isTimeBetween()) {
            context.startToolboxService()
        } else {
            context.stopToolboxService(SunlightMonitorService.ACTION_STOP)
            sunlightSchedule.setupAlarms(context)
        }
    }
}
