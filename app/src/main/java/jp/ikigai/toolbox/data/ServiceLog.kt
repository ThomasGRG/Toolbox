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
) {
    companion object {
        const val TABLE = "service_logs"

        const val SERVICE_ID = "service_name"
        const val MESSAGE_ID = "message"
        const val EXTRA = "extra"
        const val EXCEPTION_MESSAGE = "exception_message"
        const val DATE_TIME = "date_time"
        const val LOG_TYPE = "log_type"

        val CREATE_TABLE_QUERY = """
            CREATE TABLE IF NOT EXISTS $TABLE (
                $SERVICE_ID INTEGER NOT NULL,
                $MESSAGE_ID INTEGER NOT NULL,
                $EXTRA TEXT,
                $EXCEPTION_MESSAGE TEXT,
                $DATE_TIME INTEGER,
                $LOG_TYPE INTEGER
            )
        """.trimIndent()

        val GET_ALL = """
            SELECT $SERVICE_ID, $MESSAGE_ID, $EXTRA, $EXCEPTION_MESSAGE, $DATE_TIME, $LOG_TYPE FROM $TABLE
        """.trimIndent()
    }
}
