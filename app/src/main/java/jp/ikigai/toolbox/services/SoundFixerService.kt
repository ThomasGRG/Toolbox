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
import jp.ikigai.toolbox.data.AppModule
import jp.ikigai.toolbox.dao.ServiceLogDao
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.utils.fileExists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class SoundFixerService(
    private val context: Context,
    private val serviceLogDao: ServiceLogDao = AppModule.serviceLogDao,
) {
    companion object {
        const val ACTION_STOP = "ACTION_STOP_SOUND_FIXER"
        const val ACTION_STOP_PLAYBACK = "ACTION_STOP_PLAYBACK"
    }

    private var ringing = false
    private var player: MediaPlayer? = null
    private var telephonyCallback: TelephonyCallback? = null

    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val telephonyManager = context.getSystemService(TelephonyManager::class.java)
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var callerJob: Job? = null
    private var activeCaller: String? = null

    fun onStartCommand(intent: Intent?) {
        when (intent?.action) {
            ACTION_STOP -> {
                onDestroy()
            }

            ACTION_STOP_PLAYBACK -> {
                stopMusic()
            }

            else -> {
                if (
                    !ToolboxServiceState.soundFixerState.value &&
                    ToolboxPreferences.isSoundFixerEnabled(context)
                ) {
                    ToolboxServiceState.updateSoundFixerState(true)
                    serviceLogDao.insert(
                        serviceType = ServiceType.SOUND_FIXER,
                        logMessage = LogMessage.MON_RUNNING,
                    )
                    initJobs()
                    registerCallStateListener()
                }
            }
        }
    }

    fun onDestroy() {
        if (!ToolboxServiceState.soundFixerState.value) return
        ToolboxServiceState.updateSoundFixerState(false)
        serviceLogDao.insert(
            serviceType = ServiceType.SOUND_FIXER,
            logMessage = LogMessage.MON_STOPPED,
        )
        ringing = false
        stopMusic()

        callerJob?.cancel()

        telephonyCallback?.let {
            try {
                telephonyManager.unregisterTelephonyCallback(it)
            } catch (e: Exception) {
                serviceLogDao.insert(
                    serviceType = ServiceType.SOUND_FIXER,
                    logMessage = LogMessage.CAUGHT_EXCEPTION,
                    exceptionMessage = e.message,
                    logType = Log.ERROR,
                )
            }
        }
    }

    private fun initJobs() {
        callerJob = coroutineScope.launch {
            for (caller in ToolboxServiceState.callerChannel) {
                if (activeCaller == null) {
                    activeCaller = caller
                    evaluatePlayback()
                }
            }
        }
    }

    private fun hasSelectedAudio(): Boolean {
        val ringtone = ToolboxPreferences.getSoundFixerRingtone(context)
        if (ringtone == null) {
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.NO_RINGTONE,
                logType = Log.WARN,
            )
            return false
        }
        val ringtoneExists = context.fileExists(ringtone)
        if (!ringtoneExists) {
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.FILE_NOT_EXISTS,
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
                        serviceLogDao.insert(
                            serviceType = ServiceType.SOUND_FIXER,
                            logMessage = LogMessage.INCOMING_CALL,
                        )
                        ringing = true
                    }

                    TelephonyManager.CALL_STATE_OFFHOOK,
                    TelephonyManager.CALL_STATE_IDLE -> {
                        val logMessage = if (state == TelephonyManager.CALL_STATE_IDLE) {
                            activeCaller = null
                            LogMessage.CALL_IDLE
                        } else LogMessage.CALL_OFF_HOOK
                        serviceLogDao.insert(
                            serviceType = ServiceType.SOUND_FIXER,
                            logMessage = logMessage,
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
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.CAUGHT_EXCEPTION,
                exceptionMessage = e.message,
                logType = Log.ERROR,
            )
        }
    }

    private fun isOnVibrate(): Boolean {
        val isRingModeVibrate = audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
        val isRingVolumeZero = audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0
        if (isRingModeVibrate) {
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.ON_VIBRATE,
            )
        }
        if (isRingVolumeZero) {
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.RING_VOL_ZERO,
            )
        }
        return isRingModeVibrate || isRingVolumeZero
    }

    private fun isOnSilent(): Boolean {
        val isRingModeSilent = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        val isRingVolumeZero = audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0
        if (isRingModeSilent) {
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.ON_SILENT,
            )
        }
        if (isRingVolumeZero) {
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.RING_VOL_ZERO,
            )
        }
        return isRingModeSilent || isRingVolumeZero
    }

    private fun isDndOn(): Boolean {
        return try {
            val isFilterAll = notificationManager.currentInterruptionFilter !=
                    NotificationManager.INTERRUPTION_FILTER_ALL
            if (isFilterAll) {
                serviceLogDao.insert(
                    serviceType = ServiceType.SOUND_FIXER,
                    logMessage = LogMessage.DND_ON,
                )
            }
            return isFilterAll
        } catch (e: Exception) {
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.CAUGHT_EXCEPTION,
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
                        serviceLogDao.insert(
                            serviceType = ServiceType.SOUND_FIXER,
                            logMessage = LogMessage.PLAYBACK_STARTED,
                        )
                    } catch (e: Exception) {
                        serviceLogDao.insert(
                            serviceType = ServiceType.SOUND_FIXER,
                            logMessage = LogMessage.CAUGHT_EXCEPTION,
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
                serviceLogDao.insert(
                    serviceType = ServiceType.SOUND_FIXER,
                    logMessage = LogMessage.PLAYBACK_ERROR,
                    extra = "$what/$extra",
                    logType = Log.ERROR,
                )
                stopMusic()
                true
            }

            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.CAUGHT_EXCEPTION,
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
                serviceLogDao.insert(
                    serviceType = ServiceType.SOUND_FIXER,
                    logMessage = LogMessage.PLAYER_RELEASED,
                )
            }
        } catch (e: Exception) {
            serviceLogDao.insert(
                serviceType = ServiceType.SOUND_FIXER,
                logMessage = LogMessage.CAUGHT_EXCEPTION,
                exceptionMessage = e.message,
                logType = Log.ERROR,
            )
        }
    }
}
