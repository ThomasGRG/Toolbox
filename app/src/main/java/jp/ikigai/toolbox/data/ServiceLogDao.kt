package jp.ikigai.toolbox.data

import android.content.ContentValues
import android.util.Log
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ServiceLogDao(
    private val database: Database,
) {
    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM dd, EEEE, yyyy")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss.SSS a")

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val _logs = MutableStateFlow<List<ServiceLog>>(emptyList())
    val logs = _logs.asStateFlow()

    private val initialization by lazy {
        coroutineScope.launch {
            _logs.update {
                getAll()
            }
        }
    }

    fun observeAll(): StateFlow<List<ServiceLog>> {
        initialization
        return logs
    }

    fun getAll(): List<ServiceLog> {
        val logs = mutableListOf<ServiceLog>()
        database.readableDatabase.rawQuery(
            ServiceLog.GET_ALL,
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val service = getService(cursor.getInt(0))
                val logMessage = getLogMessage(cursor.getInt(1))
                if (service == null || logMessage == null) continue
                val dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(cursor.getLong(4)),
                    ZoneId.systemDefault()
                )
                logs.add(
                    ServiceLog(
                        serviceNameResId = service.nameResId,
                        msgResId = logMessage.msgResId,
                        extra = cursor.getString(2),
                        exceptionMessage = cursor.getString(3),
                        date = dateTime.format(dateFormatter),
                        time = dateTime.format(timeFormatter),
                        logType = cursor.getInt(5),
                    )
                )
            }
        }
        return logs
    }

    private fun getService(value: Int): ServiceType? {
        return ServiceType.entries.firstOrNull { it.id == value }
    }

    private fun getLogMessage(value: Int): LogMessage? {
        return LogMessage.entries.firstOrNull { it.id == value }
    }

    fun insert(
        serviceType: ServiceType,
        logMessage: LogMessage,
        extra: String? = null,
        exceptionMessage: String? = null,
        dateTime: Long = System.currentTimeMillis(),
        logType: Int = Log.INFO,
    ) {
        coroutineScope.launch {
            val values = ContentValues().apply {
                put(ServiceLog.SERVICE_ID, serviceType.id)
                put(ServiceLog.MESSAGE_ID, logMessage.id)
                put(ServiceLog.EXTRA, extra)
                put(ServiceLog.EXCEPTION_MESSAGE, exceptionMessage)
                put(ServiceLog.DATE_TIME, dateTime)
                put(ServiceLog.LOG_TYPE, logType)
            }

            val result = database.writableDatabase.insert(
                ServiceLog.TABLE,
                null,
                values,
            )

            if (result != -1L) {
                _logs.update {
                    getAll()
                }
            }
        }
    }

    fun deleteAll() {
        coroutineScope.launch {
            val result = database.writableDatabase.delete(
                ServiceLog.TABLE,
                null,
                null,
            )

            if (result > 0) {
                _logs.update {
                    emptyList()
                }
            }
        }
    }
}
