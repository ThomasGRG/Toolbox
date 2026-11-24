package jp.ikigai.toolbox.enums

import androidx.annotation.StringRes
import jp.ikigai.toolbox.R

enum class LogMessage(@StringRes val msgResId: Int, val id: Int) {
    ABOVE_THRESHOLD(R.string.sunlight_above_threshold, 1),
    BELOW_THRESHOLD(R.string.sunlight_below_threshold, 2),
    SET_BRIGHTNESS(R.string.set_brightness, 3),
    SET_BRIGHTNESS_FAIL(R.string.set_brightness_fail, 4),
    TIME_BETWEEN(R.string.time_is_between, 5),
    ALARMS_SETUP(R.string.setup_alarms, 6),
    RECEIVED_ALARM(R.string.alarm_received, 7),

    PACKAGE_ADDED(R.string.package_added, 8),
    PACKAGE_UPGRADED(R.string.package_upgraded, 9),
    PACKAGE_REMOVED(R.string.package_removed, 10),
    REPLAY_APP_INSTALLS(R.string.replay_installs, 11),
    REPLAY_APP_UPDATES(R.string.replay_updates, 12),

    DEVICE_DATE_CHANGED(R.string.date_changed, 13),
    DEVICE_TIME_CHANGED(R.string.time_changed, 14),
    DEVICE_TIME_ZONE_CHANGED(R.string.time_zone_changed, 15),

    CAUGHT_EXCEPTION(R.string.caught_exception, 16),

    MON_RUNNING(R.string.monitor_running, 17),
    MON_PAUSED(R.string.monitor_paused, 18),
    MON_STOPPED(R.string.monitor_stopped, 19),

    SCREEN_ON(R.string.screen_on, 20),
    SCREEN_OFF(R.string.screen_off, 21),

    CREATED(R.string.service_created, 22),
    RUNNING(R.string.service_running, 23),
    CONNECTED(R.string.service_connected, 24),
    DISCONNECTED(R.string.service_disconnected, 25),
    STOPPED(R.string.service_stopped, 26),
    DESTROYED(R.string.service_destroyed, 27),

    NO_RINGTONE(R.string.ringtone_not_set, 28),
    DND_ON(R.string.dnd_on, 29),
    ON_VIBRATE(R.string.on_vibrate, 30),
    ON_SILENT(R.string.on_silent, 31),

    RING_VOL_ZERO(R.string.ring_volume_zero, 32),
    VOL_UP(R.string.volume_up_pressed, 33),
    VOL_DOWN(R.string.volume_down_pressed, 34),

    INCOMING_CALL(R.string.incoming_call, 35),
    CALL_IDLE(R.string.call_idle, 36),
    CALL_OFF_HOOK(R.string.call_off_hook, 37),

    PLAYBACK_STARTED(R.string.player_start, 38),
    PLAYBACK_ERROR(R.string.player_error, 39),
    PLAYER_RELEASED(R.string.player_released, 40),

    FILE_NOT_EXISTS(R.string.file_not_exist, 41),
}
