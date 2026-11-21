package jp.ikigai.toolbox.data

import android.content.Context

object AppModule {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val database: Database by lazy {
        Database(appContext)
    }

    val appUpdateInfoDao: AppUpdateInfoDao by lazy {
        AppUpdateInfoDao(database)
    }

    val serviceLogDao: ServiceLogDao by lazy {
        ServiceLogDao(database)
    }
}
