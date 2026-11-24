package jp.ikigai.toolbox.data

import android.util.Log

data class ServiceLog(
    val serviceNameResId: Int,
    val msgResId: Int,
    val extra: String?,
    val exceptionMessage: String?,
    val date: String,
    val time: String,
    val logType: Int = Log.INFO,
)
