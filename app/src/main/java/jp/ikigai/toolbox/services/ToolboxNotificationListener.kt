package jp.ikigai.toolbox.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import jp.ikigai.toolbox.data.AppModule
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType

class ToolboxNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        AppModule.serviceLogDao.insert(
            serviceType = ServiceType.NOTIFICATION_LISTENER,
            logMessage = LogMessage.CONNECTED,
        )
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        AppModule.serviceLogDao.insert(
            serviceType = ServiceType.NOTIFICATION_LISTENER,
            logMessage = LogMessage.DISCONNECTED,
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (
            sbn == null ||
            isGroupSummaryNotification(sbn) ||
            isMediaNotification(sbn)
        ) return
        ToolboxServiceState.postNotificationSound(
            sbn.packageName
        )
        AppModule.serviceLogDao.insert(
            serviceType = ServiceType.NOTIFICATION_LISTENER,
            logMessage = LogMessage.NOTIFICATION_POSTED,
            extra = sbn.packageName,
        )
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
