package jp.ikigai.toolbox.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.data.ServiceLog
import jp.ikigai.toolbox.data.ToolboxServiceState

@Composable
fun ViewLogsDialog(
    dismiss: () -> Unit,
) {
    val logs = remember { mutableStateListOf<ServiceLog>() }

    LaunchedEffect(Unit) {
        ToolboxServiceState.logs.collect { log ->
            logs.add(0, log)
        }
    }

    AlertDialog(
        onDismissRequest = dismiss,
        dismissButton = null,
        confirmButton = {
            Button(
                onClick = dismiss,
            ) {
                Text(
                    text = stringResource(R.string.close)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.logs)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                reverseLayout = true,
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_logs_found),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(
                    items = logs,
                ) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                        ) {
                            Text(
                                text = stringResource(log.serviceNameResId),
                            )
                            Text(
                                text = stringResource(log.msgResId),
                                color = if (log.logType == Log.ERROR) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                },
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = log.date,
                            )
                            Text(
                                text = log.time,
                            )
                            log.extra?.let {
                                Text(
                                    text = it,
                                )
                            }
                            log.exceptionMessage?.let {
                                Text(
                                    text = it,
                                    color = if (log.logType == Log.ERROR) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onBackground
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}
