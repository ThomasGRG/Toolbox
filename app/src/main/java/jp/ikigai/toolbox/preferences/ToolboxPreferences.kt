package jp.ikigai.toolbox.preferences

import android.content.Context
import androidx.core.content.edit
import jp.ikigai.toolbox.data.SunlightSchedule
import jp.ikigai.toolbox.utils.setupSunlightSchedule

object ToolboxPreferences {
    private const val PREF_NAME = "ToolboxPrefs"
    private const val TOOLBOX_ENABLED = "toolbox_enabled"
    private const val VERSION_CODE = "version_code"
    private const val VERSION_NAME = "version_name"

    // SunlightMonitor Pref Keys
    private const val SUNLIGHT_MON_ENABLED = "sunlight_mon_enabled"
    private const val SUNLIGHT_ACTIVATE_THRESHOLD = "sunlight_activate_threshold_lux"
    private const val SUNLIGHT_DEACTIVATE_THRESHOLD = "sunlight_deactivate_threshold_lux"
    private const val SUNLIGHT_START_HOUR = "sunlight_schedule_start_hour"
    private const val SUNLIGHT_START_MINUTE = "sunlight_schedule_start_minute"
    private const val SUNLIGHT_END_HOUR = "sunlight_schedule_end_hour"
    private const val SUNLIGHT_END_MINUTE = "sunlight_schedule_end_minute"
    private const val SUNLIGHT_ORIGINAL_BRIGHTNESS_VALUE = "sunlight_original_brightness_value"

    // NetSpeedMon Pref Keys
    private const val NET_SPEED_MON_ENABLED = "net_speed_mon_enabled"

    // SoundFixer Pref Keys
    private const val SOUND_FIXER_ENABLED = "sound_fixer_enabled"
    private const val SOUND_FIXER_RINGTONE = "sound_fixer_ringtone"
    private const val SOUND_FIXER_GLOBAL_NOTIFICATION_SOUND = "sound_fixer_global_notification_sound"

    // AppNotifier Pref Keys
    private const val APP_NOTIFIER_ENABLED = "app_notifier_enabled"

    fun setToolboxEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(TOOLBOX_ENABLED, enabled) }
    }

    fun isToolboxEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(TOOLBOX_ENABLED, false)
    }

    fun setSunlightOriginalBrightnessLevel(context: Context, value: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(SUNLIGHT_ORIGINAL_BRIGHTNESS_VALUE, value) }
    }

    fun getSunlightOriginalBrightnessLevel(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(SUNLIGHT_ORIGINAL_BRIGHTNESS_VALUE, -1)
    }

    fun setSunlightActivateThreshold(context: Context, value: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(SUNLIGHT_ACTIVATE_THRESHOLD, value) }
    }

    fun getSunlightActivateThreshold(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(SUNLIGHT_ACTIVATE_THRESHOLD, -1)
    }

    fun setSunlightDeactivateThreshold(context: Context, value: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(SUNLIGHT_DEACTIVATE_THRESHOLD, value) }
    }

    fun getSunlightDeactivateThreshold(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(SUNLIGHT_DEACTIVATE_THRESHOLD, -1)
    }

    fun setSunlightSchedule(context: Context, sunlightSchedule: SunlightSchedule) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(SUNLIGHT_START_HOUR, sunlightSchedule.startHour)
            putInt(SUNLIGHT_START_MINUTE, sunlightSchedule.startMinute)
            putInt(SUNLIGHT_END_HOUR, sunlightSchedule.endHour)
            putInt(SUNLIGHT_END_MINUTE, sunlightSchedule.endMinute)
        }
        context.setupSunlightSchedule(sunlightSchedule)
    }

    fun getSunlightSchedule(context: Context): SunlightSchedule {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return SunlightSchedule(
            startHour = prefs.getInt(SUNLIGHT_START_HOUR, 7),
            startMinute = prefs.getInt(SUNLIGHT_START_MINUTE, 0),
            endHour = prefs.getInt(SUNLIGHT_END_HOUR, 19),
            endMinute = prefs.getInt(SUNLIGHT_END_MINUTE, 0)
        )
    }

    fun setSoundFixerRingtone(context: Context, fileName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(SOUND_FIXER_RINGTONE, fileName) }
    }

    fun getSoundFixerRingtone(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SOUND_FIXER_RINGTONE, null)
    }

    fun setSoundFixerGlobalNotificationSound(context: Context, fileName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(SOUND_FIXER_GLOBAL_NOTIFICATION_SOUND, fileName) }
    }

    fun getSoundFixerGlobalNotificationSound(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SOUND_FIXER_GLOBAL_NOTIFICATION_SOUND, null)
    }

    fun setSunlightMonEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(SUNLIGHT_MON_ENABLED, enabled) }
    }

    fun isSunlightMonEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(SUNLIGHT_MON_ENABLED, false)
    }

    fun setNetSpeedMonEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(NET_SPEED_MON_ENABLED, enabled) }
    }

    fun isNetSpeedMonEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(NET_SPEED_MON_ENABLED, false)
    }

    fun setSoundFixerEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(SOUND_FIXER_ENABLED, enabled) }
    }

    fun isSoundFixerEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(SOUND_FIXER_ENABLED, false)
    }

    fun setAppNotifierEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(APP_NOTIFIER_ENABLED, enabled) }
    }

    fun isAppNotifierEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(APP_NOTIFIER_ENABLED, false)
    }

    fun saveVersion(context: Context, versionCode: Long, versionName: String?) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putLong(VERSION_CODE, versionCode)
            putString(VERSION_NAME, versionName)
        }
    }

    fun getVersionCode(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(VERSION_CODE, 0)
    }

    fun getVersionName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(VERSION_NAME, null)
    }
}
