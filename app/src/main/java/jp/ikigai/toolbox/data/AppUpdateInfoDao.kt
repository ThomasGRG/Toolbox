package jp.ikigai.toolbox.data

import android.content.ContentValues
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppUpdateInfoDao(
    private val database: Database,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun getAll(query: String): List<AppUpdateInfo> {
        val appUpdateInfoList = mutableListOf<AppUpdateInfo>()
        database.readableDatabase.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                appUpdateInfoList.add(
                    AppUpdateInfo(
                        packageName = cursor.getString(0),
                        label = cursor.getString(1),
                        updatedAt = cursor.getLong(2),
                        version = cursor.getStringOrNull(3),
                        prevVersion = cursor.getStringOrNull(4),
                        isInstall = cursor.getInt(5) == 1,
                    )
                )
            }
        }
        return appUpdateInfoList
    }

    fun getAllInstalls(): List<AppUpdateInfo> {
        return getAll(AppUpdateInfo.GET_ALL_INSTALLS)
    }

    fun getAllUpdates(): List<AppUpdateInfo> {
        return getAll(AppUpdateInfo.GET_ALL_UPDATES)
    }

    fun insert(appUpdateInfo: AppUpdateInfo) = coroutineScope.launch {
        val values = ContentValues().apply {
            put(AppUpdateInfo.PACKAGE_NAME, appUpdateInfo.packageName)
            put(AppUpdateInfo.LABEL, appUpdateInfo.label)
            put(AppUpdateInfo.UPDATED_AT, appUpdateInfo.updatedAt)
            put(AppUpdateInfo.VERSION, appUpdateInfo.version)
            put(AppUpdateInfo.PREV_VERSION, appUpdateInfo.prevVersion)
            put(AppUpdateInfo.IS_INSTALL, appUpdateInfo.isInstall)
        }

        database.writableDatabase.insert(
            AppUpdateInfo.TABLE,
            null,
            values,
        )
    }

    fun deleteInstall(packageName: String) = coroutineScope.launch {
        database.writableDatabase.delete(
            AppUpdateInfo.TABLE,
            "${AppUpdateInfo.PACKAGE_NAME} = ? AND ${AppUpdateInfo.IS_INSTALL} = 1",
            arrayOf(packageName),
        )
    }

    fun deleteUpdate(packageName: String) = coroutineScope.launch {
        database.writableDatabase.delete(
            AppUpdateInfo.TABLE,
            "${AppUpdateInfo.PACKAGE_NAME} = ? AND ${AppUpdateInfo.IS_INSTALL} = 0",
            arrayOf(packageName),
        )
    }

    fun deleteAllUpdates() = coroutineScope.launch {
        database.writableDatabase.delete(
            AppUpdateInfo.TABLE,
            "${AppUpdateInfo.IS_INSTALL} = 0",
            null,
        )
    }
}
