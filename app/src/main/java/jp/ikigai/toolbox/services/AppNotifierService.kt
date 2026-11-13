package jp.ikigai.toolbox.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import jp.ikigai.toolbox.BuildConfig
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.AppUpdateInfo
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.receivers.InstallNotificationDismissedReceiver
import jp.ikigai.toolbox.receivers.UpdateNotificationDismissedReceiver
import kotlin.math.max
import kotlin.math.min

class AppNotifierService(private val context: Context) {

    companion object {
        const val ACTION_STOP = "ACTION_STOP_APP_NOTIFIER"
        const val ACTION_PACKAGE_UPGRADED = "ACTION_PACKAGE_UPGRADED"
        const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        const val PACKAGE_DATA_SCHEME = "package"
        const val PACKAGE_NAME_EXTRA = "package_name"
        const val APP_INSTALL_GROUP = "app_install_group"
        const val APP_UPDATE_CHANNEL_ID = "app_updates"
        const val APP_UPDATE_ID = 72648
        const val APP_INSTALL_CHANNEL_ID = ""
        const val APP_INSTALL_ID = 92645
    }

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    private val packageNameVersionCodeMap = mutableMapOf<String, Long>()
    private val packageNameVersionNameMap = mutableMapOf<String, String>()

    private val packageAddedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val packageName = intent.dataString?.removePrefix("$PACKAGE_DATA_SCHEME:").orEmpty()
            ToolboxServiceState.log(
                serviceNameResId = R.string.app_notifier,
                msgResId = R.string.package_added,
                extra = packageName
            )

            getPackageInfoSafely(packageName)?.let { packageInfo ->
                val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
                if (versionCode < (packageNameVersionCodeMap[packageName] ?: 0)) return@let

                packageNameVersionCodeMap[packageName] = versionCode

                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
                    handleAppUpdateNotification(packageInfo)
                else {
                    handleAppInstallNotification(packageInfo)
                }
                packageInfo.versionName?.let { versionName ->
                    packageNameVersionNameMap[packageName] = versionName
                }
            }
        }
    }

    private val packageRemovedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return

            val packageName = intent.dataString?.removePrefix("$PACKAGE_DATA_SCHEME:").orEmpty()
            ToolboxServiceState.log(
                serviceNameResId = R.string.app_notifier,
                msgResId = R.string.package_removed,
                extra = packageName
            )
            packageNameVersionCodeMap.remove(packageName)
            packageNameVersionNameMap.remove(packageName)
            cancelAppInstallNotification(packageName)
        }
    }

    fun onStartCommand(intent: Intent?) {
        when (intent?.action) {
            ACTION_STOP -> {
                ToolboxPreferences.setAppNotifierEnabled(context, false)
                onDestroy()
            }

            ACTION_PACKAGE_UPGRADED -> {
                handlePackageUpgrade()
            }

            ToolboxService.ACTION_BOOT_COMPLETED -> {
                start()
                replayAppUpdates()
                replayAppInstalls()
            }

            else -> {
                start()
            }
        }
    }

    fun onDestroy() {
        if (!ToolboxServiceState.appNotifierState.value) return
        ToolboxServiceState.updateAppNotifierState(false)
        ToolboxServiceState.log(
            serviceNameResId = R.string.app_notifier,
            msgResId = R.string.monitor_stopped,
        )
        context.unregisterReceiver(packageAddedReceiver)
        context.unregisterReceiver(packageRemovedReceiver)
    }

    private fun start() {
        if (!ToolboxPreferences.isAppNotifierEnabled(context) || ToolboxServiceState.appNotifierState.value) return
        ToolboxServiceState.updateAppNotifierState(true)
        ToolboxServiceState.log(
            serviceNameResId = R.string.app_notifier,
            msgResId = R.string.monitor_running,
        )
        val packageInfo = context.packageManager.getInstalledPackages(0)
        for (info in packageInfo) {
            val versionCode = PackageInfoCompat.getLongVersionCode(info)
            packageNameVersionCodeMap[info.packageName] = versionCode
            info.versionName?.let { versionName ->
                packageNameVersionNameMap[info.packageName] = versionName
            }

            if (info.packageName == BuildConfig.APPLICATION_ID)
                ToolboxPreferences.saveVersionCode(context, versionCode)
        }

        context.registerReceiver(
            packageAddedReceiver,
            IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply {
                addDataScheme(PACKAGE_DATA_SCHEME)
            }
        )

        context.registerReceiver(
            packageRemovedReceiver,
            IntentFilter(Intent.ACTION_PACKAGE_REMOVED).apply {
                addDataScheme(PACKAGE_DATA_SCHEME)
            }
        )
    }

    private fun handlePackageUpgrade() {
        if (!ToolboxPreferences.isAppNotifierEnabled(context) || ToolboxServiceState.appNotifierState.value) return
        start()
        getPackageInfoSafely(BuildConfig.APPLICATION_ID)?.let {
            val versionCode = PackageInfoCompat.getLongVersionCode(it)
            if (versionCode >= ToolboxPreferences.getVersionCode(context)) {
                handleAppUpdateNotification(it)
            }
            replayAppUpdates()
            replayAppInstalls()
        }
    }

    private fun getPackageInfoSafely(packageName: String): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.app_notifier,
                msgResId = R.string.caught_exception,
                exceptionMessage = e.message,
                logType = Log.ERROR,
            )
            ToolboxPreferences.apply {
                deleteInstall(context, packageName)
                deleteUpdate(context, packageName)
            }
            null
        }
    }

    private fun handleAppUpdateNotification(pkgInfo: PackageInfo) {
        val info = AppUpdateInfo(
            packageName = pkgInfo.packageName,
            label = pkgInfo.applicationInfo?.loadLabel(context.packageManager).toString(),
            updatedAt = System.currentTimeMillis(),
            version = pkgInfo.versionName,
            prevVersion = packageNameVersionNameMap[pkgInfo.packageName],
            isInstall = false
        )

        val list: List<AppUpdateInfo>

        ToolboxPreferences.apply {
            deleteUpdate(context, info.packageName)
            list = getAllUpdates(context).plus(info).reversed()
            insert(context, info)
        }

        buildAppUpdateNotification(list, lazy { getIcon(pkgInfo) })
    }

    private fun buildAppUpdateNotification(list: List<AppUpdateInfo>, icon: Lazy<Bitmap?>) {
        if (list.isEmpty()) return

        val header: String
        val content: String

        context.resources.apply {
            header = getQuantityString(R.plurals.apps_updated_header, list.size, list.size)

            if (list.size == 1) {
                val info = list.first()
                content = if (
                    info.version != null &&
                    info.prevVersion != null
                ) {
                    getString(
                        R.string.enhanced_template,
                        info.label,
                        info.prevVersion,
                        info.version,
                    )
                } else if (info.version != null) {
                    getString(
                        R.string.simple_template,
                        info.label,
                        info.version,
                    )
                } else {
                    info.label
                }
                return@apply
            }

            val subList = list.map { it.label }
                .subList(0, min(4, list.size))
                .toMutableList()
                .apply {
                    if (list.size >= 5) {
                        val newSize = list.size - 4
                        add(
                            getQuantityString(R.plurals.apps_updated_footer, newSize, newSize)
                        )
                    }
                }

            val stringResName =
                "apps_updated_${if (list.size < 5) list.size.toString() else "other"}"

            val stringRes = when (stringResName) {
                "apps_updated_1" -> R.string.apps_updated_1
                "apps_updated_2" -> R.string.apps_updated_2
                "apps_updated_3" -> R.string.apps_updated_3
                "apps_updated_4" -> R.string.apps_updated_4
                else -> R.string.apps_updated_other
            }

            content = getString(
                stringRes,
                *subList.toTypedArray()
            )
        }

        createNotificationChannel(APP_UPDATE_CHANNEL_ID, R.string.app_updates)

        val dismissedIntent =
            Intent(context, UpdateNotificationDismissedReceiver::class.java).apply {
                setPackage(BuildConfig.APPLICATION_ID)
            }

        val pendingDismissedIntent = PendingIntent.getBroadcast(context, 0, dismissedIntent, FLAGS)

        val builder = NotificationCompat.Builder(context, APP_UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_updated)
            .setContentTitle(header)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().also {
                val text = list.joinToString("\n") { info ->
                    if (info.version != null && info.prevVersion != null) {
                        context.getString(
                            R.string.enhanced_template,
                            info.label,
                            info.prevVersion,
                            info.version,
                        )
                    } else if (info.version != null) {
                        context.getString(
                            R.string.simple_template,
                            info.label,
                            info.version,
                        )
                    } else {
                        info.label
                    }
                }

                it.bigText(text)
            })
            .setDeleteIntent(pendingDismissedIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        builder.setLargeIcon(icon.value)

        notificationManager.notify(APP_UPDATE_ID, builder.build())
    }

    private fun handleAppInstallNotification(pkgInfo: PackageInfo) {
        val info = AppUpdateInfo(
            packageName = pkgInfo.packageName,
            label = pkgInfo.applicationInfo?.loadLabel(context.packageManager).toString(),
            updatedAt = System.currentTimeMillis(),
            isInstall = true,
        )

        ToolboxPreferences.apply {
            deleteInstall(context, info.packageName)
            insert(context, info)
        }

        buildAppInstallNotification(info.packageName, info.label, getIcon(pkgInfo))
    }

    private fun buildAppInstallNotification(packageName: String, label: String, icon: Bitmap?) {
        createNotificationChannel(APP_INSTALL_CHANNEL_ID, R.string.app_installs)

        val code = packageName.hashCode()

        val dismissedIntent =
            Intent(context, InstallNotificationDismissedReceiver::class.java).apply {
                setPackage(BuildConfig.APPLICATION_ID)
                putExtra(PACKAGE_NAME_EXTRA, packageName)
            }

        val pendingDismissedIntent =
            PendingIntent.getBroadcast(context, code, dismissedIntent, FLAGS)

        val extras = Bundle().apply {
            putBoolean("android.app.preferSmallIcon", true)
        }

        val builder = NotificationCompat.Builder(context, APP_INSTALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_updated)
            .setContentTitle(label)
            .setContentText(context.getString(R.string.successfully_installed))
            .setStyle(NotificationCompat.BigTextStyle())
            .setLargeIcon(icon)
            .setDeleteIntent(pendingDismissedIntent)
            .setGroup(APP_INSTALL_GROUP)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addExtras(extras)

        val groupBuilder = NotificationCompat.Builder(context, APP_INSTALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_updated)
            .setGroup(APP_INSTALL_GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addExtras(extras)

        notificationManager.notify(APP_INSTALL_ID, groupBuilder.build())

        notificationManager.notify(code, builder.build())
    }

    private fun cancelAppInstallNotification(packageName: String) {
        notificationManager.cancel(packageName.hashCode())

        ToolboxPreferences.apply {
            deleteInstall(context, packageName)

            if (getAllInstalls(context).isEmpty())
                notificationManager.cancel(APP_INSTALL_ID)
        }
    }

    private fun getIcon(pkgInfo: PackageInfo): Bitmap? {
        return pkgInfo.applicationInfo?.let {
            val icon = context.packageManager.getApplicationIcon(it)
            return if (icon !is BitmapDrawable) {
                val width = max(icon.intrinsicWidth, 1)
                val height = max(icon.intrinsicHeight, 1)

                val bitmap = createBitmap(width, height)
                val canvas = Canvas(bitmap)

                icon.setBounds(0, 0, canvas.width, canvas.height)
                icon.draw(canvas)

                bitmap.toDrawable(context.resources).bitmap
            } else
                icon.bitmap
        }
    }

    private fun replayAppUpdates() {
        val updates = ToolboxPreferences.getAllUpdates(context).reversed()
        if (updates.isEmpty()) return

        ToolboxServiceState.log(
            serviceNameResId = R.string.app_notifier,
            msgResId = R.string.replay_updates,
        )

        getPackageInfoSafely(updates.first().packageName)?.let {
            buildAppUpdateNotification(updates, lazy { getIcon(it) })
        }
    }

    private fun replayAppInstalls() {
        val installs = ToolboxPreferences.getAllInstalls(context)
        if (installs.isEmpty()) return

        ToolboxServiceState.log(
            serviceNameResId = R.string.app_notifier,
            msgResId = R.string.replay_installs,
        )

        for (install in installs) {
            getPackageInfoSafely(install.packageName)?.let {
                buildAppInstallNotification(install.packageName, install.label, getIcon(it))
            }
        }
    }

    private fun createNotificationChannel(channelId: String, nameRes: Int) {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                channelId,
                context.getString(nameRes),
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }
}