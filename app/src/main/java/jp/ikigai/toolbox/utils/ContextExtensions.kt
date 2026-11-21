package jp.ikigai.toolbox.utils

import android.Manifest
import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import jp.ikigai.toolbox.data.AppModule
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.services.ToolboxAccessibilityService
import jp.ikigai.toolbox.services.ToolboxNotificationListener
import jp.ikigai.toolbox.services.ToolboxService
import java.io.File
import java.io.FileOutputStream

fun Context.startToolboxService() {
    if (!ToolboxPreferences.isToolboxEnabled(this)) return
    startForegroundService(
        Intent(
            this,
            ToolboxService::class.java
        )
    )
}

fun Context.startToolboxService(action: String) {
    if (!ToolboxPreferences.isToolboxEnabled(this)) return
    startForegroundService(
        Intent(
            this,
            ToolboxService::class.java
        ).apply {
            this.action = action
        }
    )
}

fun Context.stopToolboxService(action: String) {
    startForegroundService(
        Intent(
            this,
            ToolboxService::class.java
        ).apply {
            this.action = action
        }
    )
}

val Context.hasPostNotificationPerm: Boolean
    get() {
        return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

val Context.hasReadPhoneStatePerm: Boolean
    get() {
        return checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

val Context.batteryOptimizationStatus: Boolean
    get() {
        val powerManager = getSystemService(PowerManager::class.java)
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

fun Context.requestBatteryUnrestricted() {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    intent.data = "package:$packageName".toUri()
    startActivity(intent)
}

val Context.canScheduleExactAlarms: Boolean
    get() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        return alarmManager.canScheduleExactAlarms()
    }

fun Context.requestScheduleExactAlarms() {
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    intent.data = "package:$packageName".toUri()
    startActivity(intent)
}

val Context.canWriteSettings: Boolean
    get() {
        return Settings.System.canWrite(this)
    }

fun Context.requestWriteSettings() {
    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
    intent.data = "package:$packageName".toUri()
    startActivity(intent)
}

val Context.hasNotificationListenerAccess: Boolean
    get() {
        val component = ComponentName(
            this,
            ToolboxNotificationListener::class.java
        )

        val enabledListeners = Settings.Secure.getString(
            this.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        return enabledListeners.split(":").any {
            ComponentName.unflattenFromString(it) == component
        }
    }

fun Context.requestNotificationListenerAccess() {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    startActivity(intent)
}

val Context.isAccessibilityServiceEnabled: Boolean
    get() {
        val expectedComponentName =
            "${packageName}/${ToolboxAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

fun Context.requestAccessibilitySettings() {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

fun Context.fileExists(fileName: String): Boolean {
    val file = File(filesDir, fileName)
    return file.exists() && file.length() > 0
}

fun Context.copyUriToInternalStorage(uri: Uri): String? {
    val originalFileName = uri.getFileNameFromUri(contentResolver) ?: return null

    val destinationFile = File(filesDir, originalFileName)

    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return originalFileName
    } catch (_: Exception) {
        return null
    }
}

fun Context.getAvailableSounds(): List<String> {
    return filesDir
        .listFiles()
        ?.filter {
            if (!it.isFile || it.length() > 1048576) return@filter false
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(it.extension.lowercase())
            mimeType?.startsWith("audio/") == true
        }
        ?.map { it.name }
        ?: emptyList()
}

fun Context.getPackageInfoSafely(packageName: String, serviceType: ServiceType): PackageInfo? {
    return try {
        packageManager.getPackageInfo(packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        AppModule.serviceLogDao.insert(
            serviceType = serviceType,
            logMessage = LogMessage.CAUGHT_EXCEPTION,
            exceptionMessage = e.message,
            logType = Log.ERROR,
        )
        null
    }
}
