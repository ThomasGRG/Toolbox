package jp.ikigai.toolbox.services

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.provider.Settings
import android.util.Log
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.data.isTimeBetween
import jp.ikigai.toolbox.preferences.ToolboxPreferences

class SunlightMonitorService(private val context: Context) {
    companion object {
        const val ACTION_STOP = "ACTION_STOP_SUNLIGHT"
        const val ACTION_UPDATE_THRESHOLDS = "ACTION_UPDATE_THRESHOLDS"
    }

    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var activateThreshold: Int = ToolboxPreferences.getSunlightActivateThreshold(context)
    private var deactivateThreshold: Int =
        ToolboxPreferences.getSunlightDeactivateThreshold(context)

    private var isActive: Boolean = false

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val luxValue = event.values.firstOrNull() ?: return
                if (luxValue.isFinite() && luxValue > 0f) {
                    evaluateLatestReading(luxValue)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun onStartCommand(intent: Intent?) {
        when (intent?.action) {
            ACTION_STOP -> {
                ToolboxPreferences.setSunlightMonEnabled(context, false)
                onDestroy()
            }

            ACTION_UPDATE_THRESHOLDS -> {
                activateThreshold = ToolboxPreferences.getSunlightActivateThreshold(context)
                deactivateThreshold = ToolboxPreferences.getSunlightDeactivateThreshold(context)
            }

            else -> {
                startMonitoring()
            }
        }
    }

    fun onDestroy() {
        if (!ToolboxServiceState.sunlightMonitorState.value) return
        ToolboxServiceState.updateSunlightMonitorState(false)
        ToolboxServiceState.log(
            serviceNameResId = R.string.sunlight_monitor,
            msgResId = R.string.monitor_stopped,
        )
        restoreBrightnessLevel()
        stopSensor()
    }

    private fun startMonitoring() {
        if (
            !ToolboxPreferences.isSunlightMonEnabled(context) ||
            lightSensor == null ||
            ToolboxServiceState.sunlightMonitorState.value ||
            !isTimeBetween()
        ) {
            return
        }
        ToolboxServiceState.updateSunlightMonitorState(true)
        ToolboxServiceState.log(
            serviceNameResId = R.string.sunlight_monitor,
            msgResId = R.string.monitor_running,
        )
        startSensor()
    }

    private fun startSensor() {
        lightSensor?.let { sensor ->
            sensorManager.registerListener(
                sensorEventListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun stopSensor() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    fun hasWritePermission(): Boolean = Settings.System.canWrite(context)

    private fun evaluateLatestReading(luxValue: Float) {
        when {
            luxValue >= activateThreshold -> {
                if (isActive) return
                isActive = true
                ToolboxServiceState.log(
                    serviceNameResId = R.string.sunlight_monitor,
                    msgResId = R.string.sunlight_above_threshold
                )
                ToolboxPreferences.setSunlightOriginalBrightnessLevel(
                    context,
                    Settings.System.getInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                    )
                )
                writeBrightnessLevel(255)
            }

            luxValue <= deactivateThreshold -> {
                if (!isActive) return
                isActive = false
                ToolboxServiceState.log(
                    serviceNameResId = R.string.sunlight_monitor,
                    msgResId = R.string.sunlight_below_threshold
                )
                restoreBrightnessLevel()
            }
        }
    }

    private fun restoreBrightnessLevel() {
        val originalBrightnessLevel = ToolboxPreferences.getSunlightOriginalBrightnessLevel(context)
        if (originalBrightnessLevel == -1) {
            return
        }
        writeBrightnessLevel(originalBrightnessLevel)
        ToolboxPreferences.setSunlightOriginalBrightnessLevel(context, -1)
    }

    private fun writeBrightnessLevel(brightnessLevel: Int) {
        if (!hasWritePermission()) return
        val value = brightnessLevel.coerceIn(0, 255)
        try {
            val result = Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value,
            )
            val currentBrightnessLevel = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
            )
            if (result && currentBrightnessLevel == value) {
                ToolboxServiceState.log(
                    serviceNameResId = R.string.sunlight_monitor,
                    msgResId = R.string.set_brightness,
                    extra = brightnessLevel.toString(),
                )
            } else {
                ToolboxServiceState.log(
                    serviceNameResId = R.string.sunlight_monitor,
                    msgResId = R.string.set_brightness_fail,
                    extra = "$result | $value | $currentBrightnessLevel",
                )
            }
        } catch (e: Exception) {
            ToolboxServiceState.log(
                serviceNameResId = R.string.sunlight_monitor,
                msgResId = R.string.set_brightness_fail,
                exceptionMessage = e.message,
                logType = Log.ERROR,
            )
        }
    }

    fun isTimeBetween(): Boolean {
        return ToolboxPreferences.getSunlightSchedule(context).isTimeBetween()
    }
}
