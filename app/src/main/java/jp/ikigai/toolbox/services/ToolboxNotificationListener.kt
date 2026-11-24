package jp.ikigai.toolbox.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType

class ToolboxNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        ToolboxServiceState.log(
            serviceType = ServiceType.NOTIFICATION_LISTENER,
            logMessage = LogMessage.CONNECTED,
        )
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        ToolboxServiceState.log(
            serviceType = ServiceType.NOTIFICATION_LISTENER,
            logMessage = LogMessage.DISCONNECTED,
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName != "com.android.dialer") return
        val extras = sbn.notification?.extras ?: return
        val verificationText = extras.getString("android.verificationText")
        if (verificationText != null) {
            val title = extras.getString("android.title") ?: return
            ToolboxServiceState.callerChannel.trySend(title)
        }
    }
}
