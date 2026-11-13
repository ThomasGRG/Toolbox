package jp.ikigai.toolbox.ui.components

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerSelectionMode
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.SunlightSchedule
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.services.SunlightMonitorService
import jp.ikigai.toolbox.utils.startToolboxService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunlightMonitorSettingsDialog(
    dismiss: () -> Unit,
) {
    val context = LocalContext.current

    val sensor by remember(key1 = context) {
        mutableStateOf(
            context.getSystemService(SensorManager::class.java).getDefaultSensor(Sensor.TYPE_LIGHT)
        )
    }

    val maxRange: String? by remember(key1 = sensor) {
        mutableStateOf(
            sensor?.maximumRange?.toBigDecimal()?.stripTrailingZeros()?.toPlainString()
        )
    }

    val resolution: String? by remember(key1 = sensor) {
        mutableStateOf(
            sensor?.resolution?.toBigDecimal()?.stripTrailingZeros()?.toPlainString()
        )
    }

    val power: String? by remember(key1 = sensor) {
        mutableStateOf(
            sensor?.power?.toBigDecimal()?.stripTrailingZeros()?.toPlainString()
        )
    }

    val lightLevelState = remember {
        mutableFloatStateOf(0f)
    }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    lightLevelState.floatValue = event.values[0]
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        lightSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    var deactivateThresholdText by remember {
        mutableStateOf(
            ToolboxPreferences.getSunlightDeactivateThreshold(context).let {
                if (it != -1) {
                    it.toString()
                } else {
                    ""
                }
            }
        )
    }

    var deactivateThresholdError: Boolean by remember {
        mutableStateOf(false)
    }

    var activateThresholdText by remember {
        mutableStateOf(
            ToolboxPreferences.getSunlightActivateThreshold(context).let {
                if (it != -1) {
                    it.toString()
                } else {
                    ""
                }
            }
        )
    }

    var activateThresholdError: Boolean by remember {
        mutableStateOf(false)
    }

    var thresholdError: Boolean by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = activateThresholdText, key2 = deactivateThresholdText) {
        val activateThreshold = activateThresholdText.toIntOrNull() ?: return@LaunchedEffect
        val deactivateThreshold = deactivateThresholdText.toIntOrNull() ?: return@LaunchedEffect

        thresholdError = activateThreshold <= deactivateThreshold
    }

    var schedule by remember {
        mutableStateOf(
            ToolboxPreferences.getSunlightSchedule(context)
        )
    }

    val startTimePickerState = rememberTimePickerState(
        initialHour = schedule.startHour,
        initialMinute = schedule.startMinute,
        is24Hour = false,
    )

    val startTime by remember(
        key1 = schedule.startHour,
        key2 = schedule.startMinute,
    ) {
        mutableStateOf(
            "%02d:%02d".format(
                schedule.startHour,
                schedule.startMinute,
            )
        )
    }

    var showStartTimePicker by remember {
        mutableStateOf(false)
    }

    val endTimePickerState = rememberTimePickerState(
        initialHour = schedule.endHour,
        initialMinute = schedule.endMinute,
        is24Hour = false,
    )

    val endTime by remember(
        key1 = schedule.endHour,
        key2 = schedule.endMinute,
    ) {
        mutableStateOf(
            "%02d:%02d".format(
                schedule.endHour,
                schedule.endMinute,
            )
        )
    }

    var showEndTimePicker by remember {
        mutableStateOf(false)
    }

    fun dismissTimePicker(restoreTime: Boolean = true, restoreSelection: Boolean = true) {
        if (restoreTime) {
            startTimePickerState.hour = schedule.startHour
            startTimePickerState.minute = schedule.startMinute
            endTimePickerState.hour = schedule.endHour
            endTimePickerState.minute = schedule.endMinute
        }
        showStartTimePicker = false
        showEndTimePicker = false
        if (restoreSelection) {
            startTimePickerState.selection = TimePickerSelectionMode.Hour
            endTimePickerState.selection = TimePickerSelectionMode.Hour
        }
    }

    if (showStartTimePicker || showEndTimePicker) {
        TimePickerDialog(
            dismiss = {
                dismissTimePicker()
            },
            set = {
                schedule = schedule.copy(
                    startHour = startTimePickerState.hour,
                    startMinute = startTimePickerState.minute,
                    endHour = endTimePickerState.hour,
                    endMinute = endTimePickerState.minute,
                )
                dismissTimePicker(restoreTime = false, restoreSelection = true)
            },
            timePickerState = if (showStartTimePicker) startTimePickerState else endTimePickerState
        )
    }

    AlertDialog(
        modifier = Modifier.imePadding(),
        onDismissRequest = {
            dismiss()
        },
        confirmButton = {
            Button(
                onClick = {
                    activateThresholdError = activateThresholdText.toIntOrNull() == null
                    deactivateThresholdError = deactivateThresholdText.toIntOrNull() == null
                    if (!activateThresholdError && !deactivateThresholdError && !thresholdError) {
                        ToolboxPreferences.setSunlightActivateThreshold(
                            context,
                            activateThresholdText.toInt()
                        )
                        ToolboxPreferences.setSunlightDeactivateThreshold(
                            context,
                            deactivateThresholdText.toInt()
                        )
                        ToolboxPreferences.setSunlightSchedule(
                            context,
                            SunlightSchedule(
                                startHour = startTimePickerState.hour,
                                startMinute = startTimePickerState.minute,
                                endHour = endTimePickerState.hour,
                                endMinute = endTimePickerState.minute,
                            )
                        )
                        context.startToolboxService(SunlightMonitorService.ACTION_UPDATE_THRESHOLDS)
                        dismiss()
                    }
                },
            ) {
                Text(
                    text = stringResource(R.string.save)
                )
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    dismiss()
                },
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.settings)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                sensor.let {
                    if (it == null) {
                        Text(
                            text = stringResource(R.string.live_sensor_unavailable),
                        )
                    } else {
                        Text(
                            text = "%.1f lux".format(lightLevelState.floatValue),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(
                                R.string.live_sensor_name,
                                it.name,
                            )
                        )
                        Text(
                            text = stringResource(
                                R.string.live_sensor_vendor,
                                it.vendor,
                            )
                        )
                    }
                }

                maxRange?.let {
                    Text(
                        text = stringResource(
                            R.string.live_sensor_max_range,
                            it,
                        )
                    )
                }
                resolution?.let {
                    Text(
                        text = stringResource(
                            R.string.live_sensor_resolution,
                            it,
                        )
                    )
                }
                power?.let {
                    Text(
                        text = stringResource(
                            R.string.live_sensor_power,
                            it,
                        )
                    )
                }

                Text(
                    text = stringResource(R.string.live_sensor_hint)
                )

                HorizontalDivider()

                OutlinedTextField(
                    value = activateThresholdText,
                    onValueChange = {
                        val threshold = it.toIntOrNull()
                        activateThresholdText = threshold?.toString() ?: ""
                        activateThresholdError = threshold == null
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.activation_threshold)
                        )
                    },
                    singleLine = true,
                    isError = activateThresholdError || thresholdError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                )
                if (activateThresholdError) {
                    Text(
                        text = stringResource(
                            R.string.invalid_threshold_val,
                            sensor?.maximumRange?.toInt() ?: Int.MAX_VALUE
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = deactivateThresholdText,
                    onValueChange = {
                        val threshold = it.toIntOrNull()
                        deactivateThresholdText = threshold?.toString() ?: ""
                        deactivateThresholdError = threshold == null
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.deactivation_threshold)
                        )
                    },
                    singleLine = true,
                    isError = deactivateThresholdError || thresholdError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                )
                if (deactivateThresholdError) {
                    Text(
                        text = stringResource(
                            R.string.invalid_threshold_val,
                            sensor?.maximumRange?.toInt() ?: Int.MAX_VALUE
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!activateThresholdError && !deactivateThresholdError && thresholdError) {
                    Text(
                        text = stringResource(R.string.invalid_threshold_config),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = stringResource(R.string.threshold_hint)
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showStartTimePicker = true
                        },
                    ) {
                        Text(text = startTime)
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showEndTimePicker = true
                        },
                    ) {
                        Text(text = endTime)
                    }
                }

                Text(
                    text = stringResource(R.string.schedule_hint)
                )
            }
        }
    )
}
