package jp.ikigai.toolbox.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.InstalledAppInfo

@Composable
fun AppSelectionDialog(
    configuredPackages: Set<String>,
    onSelect: (InstalledAppInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var loading by remember {
        mutableStateOf(true)
    }

    var apps by remember {
        mutableStateOf(
            emptyList<InstalledAppInfo>()
        )
    }

    LaunchedEffect(key1 = context) {
        apps = context.packageManager.getInstalledPackages(0)
            .mapNotNull { packageInfo ->
                packageInfo.applicationInfo?.let {
                    val label = context.packageManager.getApplicationLabel(it)
                    InstalledAppInfo(
                        packageName = it.packageName,
                        label = label.toString(),
                        icon = context.packageManager.getApplicationIcon(it).toBitmap()
                            .asImageBitmap(),
                    )
                } ?: return@mapNotNull null
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    var search by remember { mutableStateOf("") }

    var filteredApps by remember {
        mutableStateOf(emptyList<InstalledAppInfo>())
    }

    LaunchedEffect(key1 = apps, key2 = search, key3 = configuredPackages) {
        filteredApps = apps.filter {
            it.packageName !in configuredPackages &&
                    (
                            it.label.contains(search, ignoreCase = true) ||
                                    it.packageName.contains(search, ignoreCase = true)
                            )
        }
        loading = false
    }

    AlertDialog(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .heightIn(max = 600.dp)
            .imePadding(),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        dismissButton = null,
        confirmButton = {
            Button(
                onClick = onDismiss,
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.select_app),
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = stringResource(R.string.search)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    )
                )
                Spacer(Modifier.height(12.dp))
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
                            items = filteredApps,
                            key = { it.packageName }
                        ) { app ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelect(app)
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        bitmap = app.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(42.dp)
                                    )

                                    Spacer(Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            app.label,
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            app.packageName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (!loading && filteredApps.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.no_results_found_for, search),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    if (loading) {
                        Text(
                            text = stringResource(R.string.loading),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        },
    )
}
