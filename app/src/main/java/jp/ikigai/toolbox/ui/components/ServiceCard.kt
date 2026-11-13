package jp.ikigai.toolbox.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.toolbox.R

@Composable
fun ServiceCard(
    serviceName: String,
    serviceDesc: String?,
    canStart: Boolean,
    isRunning: Boolean,
    startService: () -> Unit,
    stopService: () -> Unit,
    serviceSettings: (() -> Unit)?,
    viewLogs: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Text(
                text = serviceName,
                style = MaterialTheme.typography.titleMedium,
            )
            serviceDesc?.let {
                Text(
                    text = it,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (isRunning) {
                            stopService()
                        } else {
                            startService()
                        }
                    },
                    enabled = canStart,
                ) {
                    Text(
                        text = stringResource(
                            if (isRunning) {
                                R.string.stop
                            } else {
                                R.string.start
                            }
                        )
                    )
                }
                serviceSettings?.let {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = it,
                    ) {
                        Text(
                            text = stringResource(R.string.settings)
                        )
                    }
                }
                viewLogs?.let {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = it,
                    ) {
                        Text(
                            text = stringResource(R.string.logs)
                        )
                    }
                }
            }
        }
    }
}
