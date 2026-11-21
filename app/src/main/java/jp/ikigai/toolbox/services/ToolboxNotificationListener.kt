package jp.ikigai.toolbox.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import java.io.File

class ToolboxNotificationListener : NotificationListenerService() {

    private val ongoingNotifications = mutableSetOf<String>()

    override fun onListenerConnected() {
        super.onListenerConnected()
        ToolboxServiceState.log(
            serviceNameResId = R.string.toolbox_notification_listener,
            msgResId = R.string.service_connected,
        )
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        ToolboxServiceState.log(
            serviceNameResId = R.string.toolbox_notification_listener,
            msgResId = R.string.service_disconnected,
        )
        ongoingNotifications.clear()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (
            sbn == null ||
            isGroupSummaryNotification(sbn) ||
            isMediaNotification(sbn)
        ) return
        if (sbn.isOngoing) {
            val key = getOngoingNotificationKey(sbn)
            if (ongoingNotifications.contains(key)) return
            ongoingNotifications.add(key)
        }
        ToolboxPreferences.getSoundFixerGlobalNotificationSound(this)?.let {
            val file = File(filesDir, it)
            ToolboxServiceState.postNotificationSound(
                file.absolutePath
            )
        }
        ToolboxServiceState.log(
            serviceNameResId = R.string.toolbox_notification_listener,
            msgResId = R.string.notification_posted,
            extra = sbn.packageName,
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        ongoingNotifications.remove(
            getOngoingNotificationKey(sbn)
        )
    }

    private fun getOngoingNotificationKey(sbn: StatusBarNotification): String {
        return "${sbn.packageName}-${sbn.id}"
    }

    private fun isGroupSummaryNotification(sbn: StatusBarNotification): Boolean {
        return sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
    }

    private fun isMediaNotification(sbn: StatusBarNotification): Boolean {
        return sbn.notification.extras?.let {
            it.containsKey(Notification.EXTRA_MEDIA_SESSION) ||
                    it.getString(Notification.EXTRA_TEMPLATE)?.contains("MediaStyle") == true
        } ?: false
    }
}
