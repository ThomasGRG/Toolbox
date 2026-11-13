package jp.ikigai.toolbox.data

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ToolboxServiceState {
    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM dd, EEEE, yyyy")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss.SSS a")
    private val _logs = MutableSharedFlow<ServiceLog>(
        replay = 400,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val logs = _logs.asSharedFlow()

    fun log(
        serviceNameResId: Int,
        msgResId: Int,
        extra: String? = null,
        exceptionMessage: String? = null,
        dateTime: LocalDateTime = LocalDateTime.now(),
        logType: Int = Log.INFO
    ) {
        _logs.tryEmit(
            ServiceLog(
                serviceNameResId = serviceNameResId,
                msgResId = msgResId,
                extra = extra,
                exceptionMessage = exceptionMessage,
                date = dateTime.format(dateFormatter),
                time = dateTime.format(timeFormatter),
                logType = logType,
            )
        )
    }

    private val _toolboxState = MutableStateFlow(false)
    val toolboxState = _toolboxState.asStateFlow()

    fun updateToolboxState(state: Boolean) {
        _toolboxState.update { state }
    }

    private val _appNotifierState = MutableStateFlow(false)
    val appNotifierState = _appNotifierState.asStateFlow()

    fun updateAppNotifierState(state: Boolean) {
        _appNotifierState.update { state }
    }

    private val _netSpeedMonState = MutableStateFlow(false)
    val netSpeedMonState = _netSpeedMonState.asStateFlow()

    fun updateNetSpeedMonState(state: Boolean) {
        _netSpeedMonState.update { state }
    }

    private val _soundFixerState = MutableStateFlow(false)
    val soundFixerState = _soundFixerState.asStateFlow()

    fun updateSoundFixerState(state: Boolean) {
        _soundFixerState.update { state }
    }

    private val _sunlightMonitorState = MutableStateFlow(false)
    val sunlightMonitorState = _sunlightMonitorState.asStateFlow()

    fun updateSunlightMonitorState(state: Boolean) {
        _sunlightMonitorState.update { state }
    }
}

data class ServiceLog(
    val serviceNameResId: Int,
    val msgResId: Int,
    val extra: String?,
    val exceptionMessage: String?,
    val date: String,
    val time: String,
    val logType: Int = Log.INFO,
)
