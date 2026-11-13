package jp.ikigai.toolbox.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.utils.ringtoneExists
import java.io.File

class SoundFixerService(private val context: Context) {
    companion object {
        const val ACTION_STOP = "ACTION_STOP_SOUND_FIXER"
    }

    private var ringing = false
    private var player: MediaPlayer? = null
    private var telephonyCallback: TelephonyCallback? = null

    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val telephonyManager = context.getSystemService(TelephonyManager::class.java)
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun onStartCommand(intent: Intent?) {
        if (intent != null && intent.action == ACTION_STOP) {
            onDestroy()
            return
        } else if (
            !ToolboxServiceState.soundFixerState.value &&
            ToolboxPreferences.isSoundFixerEnabled(context)
        ) {
            ToolboxServiceState.updateSoundFixerState(true)
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.monitor_running,
            )
            registerCallStateListener()
        }
    }

    fun onDestroy() {
        if (!ToolboxServiceState.soundFixerState.value) return
        ToolboxServiceState.updateSoundFixerState(false)
        ToolboxServiceState.log(
            serviceNameResId = R.string.sound_fixer,
            msgResId = R.string.monitor_stopped,
        )
        ringing = false
        stopMusic()

        telephonyCallback?.let {
            try {
                telephonyManager.unregisterTelephonyCallback(it)
            } catch (e: Exception) {
                ToolboxServiceState.log(
                    serviceNameResId = R.string.sound_fixer,
                    msgResId = R.string.caught_exception,
                    exceptionMessage = e.message,
                    logType = Log.ERROR,
                )
            }
        }
    }

    private fun hasSelectedAudio(): Boolean {
        val ringtone = ToolboxPreferences.getSoundFixerRingtone(context)
        if (ringtone == null) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.ringtone_not_set,
                logType = Log.WARN,
            )
            return false
        }
        val ringtoneExists = context.ringtoneExists(ringtone)
        if (!ringtoneExists) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.file_not_exist,
                logType = Log.WARN,
            )
        }
        return ringtoneExists
    }

    private fun registerCallStateListener() {
        val callback = object : TelephonyCallback(),
            TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        ToolboxServiceState.log(
                            serviceNameResId = R.string.sound_fixer,
                            msgResId = R.string.incoming_call,
                        )
                        ringing = true
                        evaluatePlayback()
                    }

                    TelephonyManager.CALL_STATE_OFFHOOK,
                    TelephonyManager.CALL_STATE_IDLE -> {
                        ToolboxServiceState.log(
                            serviceNameResId = R.string.sound_fixer,
                            msgResId = R.string.call_idle_off_hook,
                        )
                        ringing = false
                        stopMusic()
                    }
                }
            }
        }

        telephonyCallback = callback

        try {
            telephonyManager.registerTelephonyCallback(
                context.mainExecutor,
                callback
            )
        } catch (e: SecurityException) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.caught_exception,
                exceptionMessage = e.message,
                logType = Log.ERROR,
            )
        }
    }

    private fun isOnVibrate(): Boolean {
        val isRingModeVibrate = audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
        val isRingVolumeZero = audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0
        if (isRingModeVibrate) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.on_vibrate,
            )
        }
        if (isRingVolumeZero) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.ring_volume_zero,
            )
        }
        return isRingModeVibrate || isRingVolumeZero
    }

    private fun isOnSilent(): Boolean {
        val isRingModeSilent = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        val isRingVolumeZero = audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0
        if (isRingModeSilent) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.on_silent,
            )
        }
        if (isRingVolumeZero) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.ring_volume_zero,
            )
        }
        return isRingModeSilent || isRingVolumeZero
    }

    private fun isDndOn(): Boolean {
        return try {
            val isFilterAll = notificationManager.currentInterruptionFilter !=
                    NotificationManager.INTERRUPTION_FILTER_ALL
            if (isFilterAll) {
                ToolboxServiceState.log(
                    serviceNameResId = R.string.sound_fixer,
                    msgResId = R.string.dnd_on,
                )
            }
            return isFilterAll
        } catch (e: Exception) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.caught_exception,
                exceptionMessage = e.message,
                logType = Log.ERROR,
            )
            true
        }
    }

    private fun canPlay(): Boolean {
        return ringing &&
                hasSelectedAudio() &&
                !isDndOn() &&
                !isOnSilent() &&
                !isOnVibrate()
    }

    private fun evaluatePlayback() {
        if (canPlay()) {
            startMusic()
        } else {
            stopMusic()
        }
    }

    @Synchronized
    private fun startMusic() {
        if (!canPlay() || player != null) return

        val file = File(context.filesDir, ToolboxPreferences.getSoundFixerRingtone(context)!!)

        try {
            val mediaPlayer = MediaPlayer()
            player = mediaPlayer

            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )

            mediaPlayer.setDataSource(context, Uri.fromFile(file))
            mediaPlayer.isLooping = true

            mediaPlayer.setOnPreparedListener { prepared ->
                if (player === prepared && canPlay()) {
                    try {
                        prepared.start()
                        ToolboxServiceState.log(
                            serviceNameResId = R.string.sound_fixer,
                            msgResId = R.string.player_start,
                        )
                    } catch (e: Exception) {
                        ToolboxServiceState.log(
                            serviceNameResId = R.string.sound_fixer,
                            msgResId = R.string.caught_exception,
                            exceptionMessage = e.message,
                            logType = Log.ERROR,
                        )
                        stopMusic()
                    }
                } else {
                    stopMusic()
                }
            }

            mediaPlayer.setOnErrorListener { _, what, extra ->
                ToolboxServiceState.log(
                    serviceNameResId = R.string.sound_fixer,
                    msgResId = R.string.player_error,
                    extra = "$what/$extra",
                    logType = Log.ERROR,
                )
                stopMusic()
                true
            }

            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.caught_exception,
                exceptionMessage = e.message,
                logType = Log.ERROR,
            )
            stopMusic()
        }
    }

    @Synchronized
    private fun stopMusic() {
        val oldPlayer = player
        player = null

        try {
            oldPlayer?.let {
                it.setOnPreparedListener(null)
                it.setOnErrorListener(null)
                it.release()
                ToolboxServiceState.log(
                    serviceNameResId = R.string.sound_fixer,
                    msgResId = R.string.player_released,
                )
            }
        } catch (e: Exception) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sound_fixer,
                msgResId = R.string.caught_exception,
                exceptionMessage = e.message,
                logType = Log.ERROR,
            )
        }
    }
}
