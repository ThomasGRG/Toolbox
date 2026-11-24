package jp.ikigai.toolbox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import jp.ikigai.toolbox.preferences.ToolboxPreferences

class UpdateNotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ToolboxPreferences.deleteAllUpdates(context)
    }
}
