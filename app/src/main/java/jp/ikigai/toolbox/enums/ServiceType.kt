package jp.ikigai.toolbox.enums

import androidx.annotation.StringRes
import jp.ikigai.toolbox.R

enum class ServiceType(@StringRes val nameResId: Int, val id: Int) {
    TOOLBOX(R.string.app_name, 1),
    APP_NOTIFIER(R.string.app_notifier, 2),
    NET_SPEED_MON(R.string.net_speed_mon, 3),
    SOUND_FIXER(R.string.sound_fixer, 4),
    SUNLIGHT_MON(R.string.sunlight_monitor, 5),
    ACCESSIBILITY(R.string.toolbox_accessibility_service, 6),
    NOTIFICATION_LISTENER(R.string.toolbox_notification_listener, 7),
}
