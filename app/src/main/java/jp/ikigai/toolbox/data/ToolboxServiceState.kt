package jp.ikigai.toolbox.data

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ToolboxServiceState {
    val callerChannel = Channel<String>(Channel.RENDEZVOUS)

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
