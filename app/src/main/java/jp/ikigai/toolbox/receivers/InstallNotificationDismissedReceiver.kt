package jp.ikigai.toolbox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.services.AppNotifierService.Companion.PACKAGE_NAME_EXTRA

class InstallNotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra(PACKAGE_NAME_EXTRA).orEmpty()
        ToolboxPreferences.deleteInstall(context, packageName)
    }
}
