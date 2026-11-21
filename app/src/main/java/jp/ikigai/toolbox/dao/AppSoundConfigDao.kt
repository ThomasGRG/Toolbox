package jp.ikigai.toolbox.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import jp.ikigai.toolbox.data.AppSoundConfig
import jp.ikigai.toolbox.data.Database
import jp.ikigai.toolbox.enums.ServiceType
import jp.ikigai.toolbox.utils.getPackageInfoSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppSoundConfigDao(
    private val database: Database,
    private val context: Context,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _appSounds = MutableStateFlow<List<AppSoundConfig>>(emptyList())
    val appSounds = _appSounds.asStateFlow()

    private val initialization by lazy {
        coroutineScope.launch {
            _appSounds.update {
                getAll()
            }
        }
    }

    fun observeAll(): StateFlow<List<AppSoundConfig>> {
        initialization
        return appSounds
    }

    fun getAll(): List<AppSoundConfig> {
        val appSoundConfigList = mutableListOf<AppSoundConfig>()
        database.readableDatabase.rawQuery(
            AppSoundConfig.GET_ALL,
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val packageName = cursor.getString(0)
                context.getPackageInfoSafely(
                    packageName,
                    ServiceType.SOUND_FIXER
                )?.applicationInfo?.let {
                    appSoundConfigList.add(
                        AppSoundConfig(
                            packageName = packageName,
                            label = context.packageManager.getApplicationLabel(it).toString(),
                            icon = context.packageManager.getApplicationIcon(it).toBitmap()
                                .asImageBitmap(),
                            fileName = cursor.getString(1),
                        )
                    )
                }
            }
        }
        return appSoundConfigList
    }

    fun insert(packageName: String, fileName: String) = coroutineScope.launch {
        val values = ContentValues().apply {
            put(AppSoundConfig.PACKAGE_NAME, packageName)
            put(AppSoundConfig.FILE_NAME, fileName)
        }

        val result = database.writableDatabase.insertWithOnConflict(
            AppSoundConfig.TABLE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )

        if (result != -1L) {
            _appSounds.update {
                getAll()
            }
        }
    }

    fun delete(packageName: String) = coroutineScope.launch {
        val result = database.writableDatabase.delete(
            AppSoundConfig.TABLE,
            "${AppSoundConfig.PACKAGE_NAME} = ?",
            arrayOf(packageName),
        )

        if (result > 0) {
            _appSounds.update {
                getAll()
            }
        }
    }
}
