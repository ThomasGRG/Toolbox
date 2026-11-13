package jp.ikigai.toolbox.preferences

import android.content.Context
import androidx.core.content.edit
import jp.ikigai.toolbox.data.AppUpdateInfo
import jp.ikigai.toolbox.data.SunlightSchedule
import jp.ikigai.toolbox.data.setupAlarms
import org.json.JSONArray
import org.json.JSONObject

object ToolboxPreferences {
    private const val PREF_NAME = "ToolboxPrefs"
    private const val VERSION_CODE = "version_code"

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

    // AppNotifier Pref Keys
    private const val APP_NOTIFIER_ENABLED = "app_notifier_enabled"
    private const val APP_UPDATE_INFO_LIST = "app_update_info_list"

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
        sunlightSchedule.setupAlarms(context)
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

    fun saveVersionCode(context: Context, versionCode: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(VERSION_CODE, versionCode) }
    }

    fun getVersionCode(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(VERSION_CODE, 0)
    }

    fun saveAppUpdateInfoList(context: Context, appInfoList: List<AppUpdateInfo>) {
        val jsonArray = JSONArray()

        for (appInfo in appInfoList) {
            val jsonObject = JSONObject().apply {
                put("packageName", appInfo.packageName)
                put("label", appInfo.label)
                put("updatedAt", appInfo.updatedAt)
                put("version", appInfo.version)
                put("prevVersion", appInfo.prevVersion)
                put("isInstall", appInfo.isInstall)
            }
            jsonArray.put(jsonObject)
        }

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(APP_UPDATE_INFO_LIST, jsonArray.toString()) }
    }

    private fun getAppUpdateInfoList(context: Context): List<AppUpdateInfo> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(APP_UPDATE_INFO_LIST, null) ?: return emptyList()

        val appUpdateInfoList = mutableListOf<AppUpdateInfo>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val appUpdateInfo = AppUpdateInfo(
                packageName = jsonObject.getString("packageName"),
                label = jsonObject.getString("label"),
                updatedAt = jsonObject.getLong("updatedAt"),
                version = jsonObject.optString("version"),
                prevVersion = jsonObject.optString("prevVersion"),
                isInstall = jsonObject.getBoolean("isInstall"),
            )
            appUpdateInfoList.add(appUpdateInfo)
        }

        return appUpdateInfoList
    }

    fun insert(context: Context, appUpdateInfo: AppUpdateInfo) {
        val appUpdateInfoList = getAppUpdateInfoList(context).toMutableList()
        appUpdateInfoList.add(appUpdateInfo)
        saveAppUpdateInfoList(
            context,
            appUpdateInfoList,
        )
    }

    fun getAllUpdates(context: Context): List<AppUpdateInfo> {
        return getAppUpdateInfoList(context).filter { appUpdateInfo -> !appUpdateInfo.isInstall }
    }

    fun getAllInstalls(context: Context): List<AppUpdateInfo> {
        return getAppUpdateInfoList(context).filter { appUpdateInfo -> appUpdateInfo.isInstall }
    }

    fun deleteInstall(context: Context, packageName: String) {
        saveAppUpdateInfoList(
            context,
            getAppUpdateInfoList(context).filter { appUpdateInfo ->
                !(appUpdateInfo.packageName == packageName && appUpdateInfo.isInstall)
            },
        )
    }

    fun deleteAllUpdates(context: Context) {
        saveAppUpdateInfoList(
            context,
            getAppUpdateInfoList(context).filter { appUpdateInfo ->
                appUpdateInfo.isInstall
            },
        )
    }

    fun deleteUpdate(context: Context, packageName: String) {
        saveAppUpdateInfoList(
            context,
            getAppUpdateInfoList(context).filter { appUpdateInfo ->
                !(appUpdateInfo.packageName == packageName && !appUpdateInfo.isInstall)
            },
        )
    }
}
