package jp.ikigai.toolbox.ui.screens

import android.Manifest
import android.app.StatusBarManager
import android.content.ComponentName
import android.graphics.drawable.Icon
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ToolboxServiceState
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.services.AppNotifierService
import jp.ikigai.toolbox.services.NetSpeedMonService
import jp.ikigai.toolbox.services.SoundFixerService
import jp.ikigai.toolbox.services.SunlightMonitorService
import jp.ikigai.toolbox.services.ToolboxService
import jp.ikigai.toolbox.ui.components.AccessibilityServiceDialog
import jp.ikigai.toolbox.ui.components.LockScreenWidgetPreview
import jp.ikigai.toolbox.ui.components.PermissionCard
import jp.ikigai.toolbox.ui.components.ServiceCard
import jp.ikigai.toolbox.ui.components.SunlightMonitorSettingsDialog
import jp.ikigai.toolbox.ui.components.TileCard
import jp.ikigai.toolbox.ui.components.ViewLogsDialog
import jp.ikigai.toolbox.ui.icons.DashboardGear
import jp.ikigai.toolbox.ui.icons.MobileGear
import jp.ikigai.toolbox.ui.widget.LockWidget
import jp.ikigai.toolbox.ui.widget.LockWidgetReceiver
import jp.ikigai.toolbox.utils.batteryOptimizationStatus
import jp.ikigai.toolbox.utils.canScheduleExactAlarms
import jp.ikigai.toolbox.utils.canWriteSettings
import jp.ikigai.toolbox.utils.copyUriToInternalStorage
import jp.ikigai.toolbox.utils.fileExists
import jp.ikigai.toolbox.utils.hasNotificationListenerAccess
import jp.ikigai.toolbox.utils.hasPostNotificationPerm
import jp.ikigai.toolbox.utils.hasReadPhoneStatePerm
import jp.ikigai.toolbox.utils.isAccessibilityServiceEnabled
import jp.ikigai.toolbox.utils.requestAccessibilitySettings
import jp.ikigai.toolbox.utils.requestBatteryUnrestricted
import jp.ikigai.toolbox.utils.requestNotificationListenerAccess
import jp.ikigai.toolbox.utils.requestScheduleExactAlarms
import jp.ikigai.toolbox.utils.requestWriteSettings
import jp.ikigai.toolbox.utils.startToolboxService
import jp.ikigai.toolbox.utils.stopToolboxService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    val vibrateLabel = stringResource(id = R.string.vibrate_tile_label)

    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        pageCount = { 2 }
    )

    val toolboxState by ToolboxServiceState.toolboxState.collectAsState()
    val appNotifierState by ToolboxServiceState.appNotifierState.collectAsState()
    val netSpeedMonState by ToolboxServiceState.netSpeedMonState.collectAsState()
    val soundFixerState by ToolboxServiceState.soundFixerState.collectAsState()
    val sunlightMonitorState by ToolboxServiceState.sunlightMonitorState.collectAsState()

    var showSunlightMonitorSettingsDialog by remember {
        mutableStateOf(false)
    }

    var showLogs by remember {
        mutableStateOf(false)
    }

    var soundFixerRingtone by remember(key1 = context) {
        mutableStateOf(
            ToolboxPreferences.getSoundFixerRingtone(context)
        )
    }

    val ringtoneExists by remember(key1 = soundFixerRingtone, key2 = context) {
        mutableStateOf(
            soundFixerRingtone?.let {
                context.fileExists(it)
            } ?: false
        )
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch(Dispatchers.IO) {
                context.copyUriToInternalStorage(selectedUri)?.let {
                    ToolboxPreferences.setSoundFixerRingtone(context, it)
                    soundFixerRingtone = it
                }
            }
        }
    }

    var canScheduleExactAlarms by remember(key1 = context) {
        mutableStateOf(
            context.canScheduleExactAlarms
        )
    }

    var canWriteSettings by remember(key1 = context) {
        mutableStateOf(
            context.canWriteSettings
        )
    }

    var hasNotificationListenerAccess by remember(key1 = context) {
        mutableStateOf(
            context.hasNotificationListenerAccess
        )
    }

    var hasNotificationPermission by remember(key1 = context) {
        mutableStateOf(
            context.hasPostNotificationPerm
        )
    }

    var hasReadPhoneStatePermission by remember(key1 = context) {
        mutableStateOf(
            context.hasReadPhoneStatePerm
        )
    }

    var batteryOptimizationStatus by remember {
        mutableStateOf(context.batteryOptimizationStatus)
    }

    var accessibilityServiceEnabled by remember(key1 = context) {
        mutableStateOf(
            context.isAccessibilityServiceEnabled
        )
    }

    var showAccessibilityRequiredDialog by remember {
        mutableStateOf(false)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        accessibilityServiceEnabled = context.isAccessibilityServiceEnabled
        if (accessibilityServiceEnabled && showAccessibilityRequiredDialog) {
            showAccessibilityRequiredDialog = false
        }
        batteryOptimizationStatus = context.batteryOptimizationStatus
        hasNotificationPermission = context.hasPostNotificationPerm
        hasReadPhoneStatePermission = context.hasReadPhoneStatePerm
        canWriteSettings = context.canWriteSettings
        canScheduleExactAlarms = context.canScheduleExactAlarms
        hasNotificationListenerAccess = context.hasNotificationListenerAccess
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { results ->
            hasNotificationPermission = results[Manifest.permission.POST_NOTIFICATIONS] ?: false
            hasReadPhoneStatePermission = results[Manifest.permission.READ_PHONE_STATE] ?: false
        },
    )

    if (showAccessibilityRequiredDialog) {
        AccessibilityServiceDialog(
            dismiss = {
                showAccessibilityRequiredDialog = false
            },
        )
    }

    if (showLogs) {
        ViewLogsDialog(
            dismiss = {
                showLogs = false
            },
        )
    }

    if (showSunlightMonitorSettingsDialog) {
        SunlightMonitorSettingsDialog(
            dismiss = {
                showSunlightMonitorSettingsDialog = false
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (pagerState.currentPage) {
                            1 -> stringResource(R.string.sound_fixer)

                            else -> stringResource(R.string.app_name)
                        }
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = DashboardGear,
                            contentDescription = stringResource(id = R.string.dashboard),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.dashboard),
                        )
                    }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = MobileGear,
                            contentDescription = stringResource(id = R.string.sound_fixer),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.sound_fixer),
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { page ->
            when (page) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ServiceCard(
                            serviceName = stringResource(R.string.app_name),
                            serviceDesc = null,
                            permissionList = listOf(
                                stringResource(R.string.post_notification)
                            ),
                            canStart = hasNotificationPermission,
                            isRunning = toolboxState,
                            startService = {
                                ToolboxPreferences.setToolboxEnabled(context, true)
                                context.startToolboxService()
                            },
                            stopService = {
                                ToolboxPreferences.setToolboxEnabled(context, false)
                                context.stopToolboxService(ToolboxService.ACTION_STOP)
                            },
                            serviceSettings = null,
                            viewLogs = {
                                showLogs = true
                            },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.permissions),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                        PermissionCard(
                            permissionName = stringResource(R.string.post_notification),
                            granted = hasNotificationPermission,
                            grant = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    )
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionCard(
                            permissionName = stringResource(R.string.read_phone_state),
                            granted = hasReadPhoneStatePermission,
                            grant = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_PHONE_STATE,
                                    )
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionCard(
                            permissionName = stringResource(R.string.battery_optimization),
                            granted = batteryOptimizationStatus,
                            grant = {
                                context.requestBatteryUnrestricted()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionCard(
                            permissionName = stringResource(R.string.write_settings),
                            granted = canWriteSettings,
                            grant = {
                                context.requestWriteSettings()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionCard(
                            permissionName = stringResource(R.string.schedule_exact_alarms),
                            granted = canScheduleExactAlarms,
                            grant = {
                                context.requestScheduleExactAlarms()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionCard(
                            permissionName = stringResource(R.string.accessibility_service),
                            granted = accessibilityServiceEnabled,
                            grant = {
                                context.requestAccessibilitySettings()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionCard(
                            permissionName = stringResource(R.string.notification_read_access),
                            granted = hasNotificationListenerAccess,
                            grant = {
                                context.requestNotificationListenerAccess()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.services),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                        ServiceCard(
                            serviceName = stringResource(R.string.app_notifier),
                            serviceDesc = stringResource(R.string.app_notifier_desc),
                            permissionList = listOf(
                                stringResource(R.string.post_notification)
                            ),
                            canStart = hasNotificationPermission,
                            isRunning = appNotifierState,
                            startService = {
                                ToolboxPreferences.setToolboxEnabled(context, true)
                                ToolboxPreferences.setAppNotifierEnabled(context, true)
                                context.startToolboxService()
                            },
                            stopService = {
                                ToolboxPreferences.setAppNotifierEnabled(context, false)
                                context.stopToolboxService(AppNotifierService.ACTION_STOP)
                            },
                            serviceSettings = null,
                            viewLogs = null,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ServiceCard(
                            serviceName = stringResource(R.string.net_speed_mon),
                            serviceDesc = stringResource(R.string.net_speed_mon_desc),
                            permissionList = listOf(
                                stringResource(R.string.post_notification)
                            ),
                            canStart = hasNotificationPermission,
                            isRunning = netSpeedMonState,
                            startService = {
                                ToolboxPreferences.setToolboxEnabled(context, true)
                                ToolboxPreferences.setNetSpeedMonEnabled(context, true)
                                context.startToolboxService()
                            },
                            stopService = {
                                ToolboxPreferences.setNetSpeedMonEnabled(context, false)
                                context.stopToolboxService(NetSpeedMonService.ACTION_STOP)
                            },
                            serviceSettings = null,
                            viewLogs = null,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ServiceCard(
                            serviceName = stringResource(R.string.sound_fixer),
                            serviceDesc = stringResource(R.string.sound_fixer_desc),
                            permissionList = listOf(
                                stringResource(R.string.post_notification),
                                stringResource(R.string.read_phone_state),
                                stringResource(R.string.notification_read_access),
                            ),
                            canStart = hasNotificationPermission && hasReadPhoneStatePermission && hasNotificationListenerAccess,
                            isRunning = soundFixerState,
                            startService = {
                                if (ringtoneExists) {
                                    ToolboxPreferences.setToolboxEnabled(context, true)
                                    ToolboxPreferences.setSoundFixerEnabled(context, true)
                                    context.startToolboxService()
                                } else {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                }
                            },
                            stopService = {
                                ToolboxPreferences.setSoundFixerEnabled(context, false)
                                context.stopToolboxService(SoundFixerService.ACTION_STOP)
                            },
                            serviceSettings = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                            viewLogs = null,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ServiceCard(
                            serviceName = stringResource(R.string.sunlight_monitor),
                            serviceDesc = stringResource(R.string.sunlight_monitor_desc),
                            permissionList = listOf(
                                stringResource(R.string.post_notification),
                                stringResource(R.string.write_settings),
                                stringResource(R.string.schedule_exact_alarms),
                            ),
                            canStart = hasNotificationPermission && canWriteSettings && canScheduleExactAlarms,
                            isRunning = sunlightMonitorState,
                            startService = {
                                if (
                                    ToolboxPreferences.getSunlightActivateThreshold(context) != -1 &&
                                    ToolboxPreferences.getSunlightDeactivateThreshold(context) != -1
                                ) {
                                    ToolboxPreferences.setToolboxEnabled(context, true)
                                    ToolboxPreferences.setSunlightMonEnabled(context, true)
                                    if (ToolboxPreferences.getSunlightSchedule(context)
                                            .isTimeBetween()
                                    ) {
                                        context.startToolboxService()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            R.string.sunlight_start_outside_schedule_hint,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    showSunlightMonitorSettingsDialog = true
                                }
                            },
                            stopService = {
                                ToolboxPreferences.setSunlightMonEnabled(context, false)
                                context.stopToolboxService(SunlightMonitorService.ACTION_STOP)
                            },
                            serviceSettings = {
                                showSunlightMonitorSettingsDialog = true
                            },
                            viewLogs = null,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.widgets),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            LockScreenWidgetPreview(
                                onClick = {
                                    if (accessibilityServiceEnabled) {
                                        coroutineScope.launch {
                                            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                                                receiver = LockWidgetReceiver::class.java,
                                                preview = LockWidget(),
                                                previewState = DpSize(100.dp, 100.dp),
                                            )
                                        }
                                    } else {
                                        showAccessibilityRequiredDialog = true
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.tiles),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            TileCard(
                                tileIconResId = R.drawable.mobile_vibrate_24px,
                                tileName = stringResource(R.string.vibrate_tile_label),
                                onClick = {
                                    val statusBarManager =
                                        context.getSystemService(StatusBarManager::class.java)
                                    statusBarManager.requestAddTileService(
                                        ComponentName(
                                            "jp.ikigai.toolbox",
                                            "jp.ikigai.toolbox.services.VibrateTileService",
                                        ),
                                        vibrateLabel,
                                        Icon.createWithResource(
                                            context,
                                            R.drawable.mobile_vibrate_24px
                                        ),
                                        context.mainExecutor,
                                    ) { result ->
                                        val toastMsg = when (result) {
                                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> R.string.tile_added
                                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> R.string.tile_already_added
                                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> R.string.tile_not_added
                                            else -> R.string.error_adding_tile
                                        }
                                        Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                1 -> {
                    SoundFixerSettingsScreen(
                        canStart = hasNotificationPermission && hasReadPhoneStatePermission && hasNotificationListenerAccess,
                        isRunning = soundFixerState,
                        selectedRingtone = soundFixerRingtone,
                        ringtoneExists = ringtoneExists,
                        ringtonePickerLauncher = ringtonePickerLauncher,
                    )
                }
            }
        }
    }
}
