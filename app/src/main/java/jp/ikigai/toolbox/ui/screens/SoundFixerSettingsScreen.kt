package jp.ikigai.toolbox.ui.screens

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.AppModule
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.ui.components.AppSelectionDialog
import jp.ikigai.toolbox.ui.components.FileSelectionCard
import jp.ikigai.toolbox.ui.components.SelectSoundDialog
import jp.ikigai.toolbox.utils.copyUriToInternalStorage
import jp.ikigai.toolbox.utils.startToolboxService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SoundFixerSettingsScreen(
    canStart: Boolean,
    isRunning: Boolean,
    selectedRingtone: String?,
    ringtoneExists: Boolean,
    ringtonePickerLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>,
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val configuredApps by AppModule.appSoundConfigDao.observeAll().collectAsState()

    val configuredPackages by remember(key1 = configuredApps) {
        mutableStateOf(
            configuredApps.map { it.packageName }.toSet()
        )
    }

    var selectedAppPackageName: String? by remember {
        mutableStateOf(null)
    }

    var showSelectAppDialog by remember {
        mutableStateOf(false)
    }

    var showSelectSoundDialog by remember {
        mutableStateOf(false)
    }

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch(Dispatchers.IO) {
                context.copyUriToInternalStorage(selectedUri)?.let { fileName ->
                    selectedAppPackageName?.let { packageName ->
                        AppModule.appSoundConfigDao.insert(
                            packageName = packageName,
                            fileName = fileName,
                        )
                        selectedAppPackageName = null
                        showSelectSoundDialog = false
                        showSelectAppDialog = false
                    }
                }
            }
        }
    }

    if (showSelectAppDialog) {
        AppSelectionDialog(
            configuredPackages = configuredPackages,
            onSelect = {
                selectedAppPackageName = it.packageName
                showSelectSoundDialog = true
            },
            onDismiss = {
                selectedAppPackageName = null
                showSelectAppDialog = false
            }
        )
    }

    if (showSelectSoundDialog) {
        SelectSoundDialog(
            pickSound = {
                soundPickerLauncher.launch(
                    arrayOf("audio/*")
                )
            },
            selectSound = { fileName ->
                selectedAppPackageName?.let { packageName ->
                    AppModule.appSoundConfigDao.insert(
                        packageName = packageName,
                        fileName = fileName,
                    )
                    selectedAppPackageName = null
                    showSelectSoundDialog = false
                    showSelectAppDialog = false
                }
            },
            dismiss = { showSelectSoundDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        FileSelectionCard(
            title = stringResource(R.string.ringtone),
            selectedFileName = selectedRingtone,
            noFileSelectedPlaceholder = stringResource(R.string.no_ringtone_selected),
            selectLabel = stringResource(R.string.select_ringtone),
            mimeTypes = arrayOf("audio/*"),
            filePickerLauncher = ringtonePickerLauncher,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.per_app_sounds),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    showSelectAppDialog = true
                },
            ) {
                Text(
                    text = stringResource(R.string.add)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(
                    items = configuredApps,
                    key = { it.packageName },
                ) { appSoundConfig ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = appSoundConfig.icon,
                                contentDescription = null,
                                modifier = Modifier.size(42.dp)
                            )

                            Spacer(Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = appSoundConfig.label,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = appSoundConfig.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = appSoundConfig.fileName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = {
                                    selectedAppPackageName = appSoundConfig.packageName
                                    showSelectSoundDialog = true
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = stringResource(R.string.edit),
                                )
                            }
                            Button(
                                onClick = {
                                    AppModule.appSoundConfigDao.delete(appSoundConfig.packageName)
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = stringResource(R.string.delete),
                                )
                            }
                        }
                    }
                }
            }
            if (configuredApps.isEmpty()) {
                Text(
                    text = stringResource(R.string.nothing_configured),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                ToolboxPreferences.setToolboxEnabled(context, true)
                ToolboxPreferences.setSoundFixerEnabled(context, true)
                context.startToolboxService()
            },
            enabled = canStart && ringtoneExists && !isRunning,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(
                    if (isRunning) R.string.running else R.string.start
                )
            )
        }
    }
}
