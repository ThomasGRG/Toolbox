package jp.ikigai.toolbox.ui.components

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.preferences.ToolboxPreferences
import jp.ikigai.toolbox.utils.startToolboxService

@Composable
fun SoundFixerSettingsDialog(
    dismiss: () -> Unit,
    canStart: Boolean,
    selectedFileName: String?,
    ringtoneExists: Boolean,
    audioPickerLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = dismiss,
        dismissButton = {
            Button(
                onClick = dismiss,
            ) {
                Text(
                    text = stringResource(R.string.close)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    ToolboxPreferences.setSoundFixerEnabled(context, true)
                    context.startToolboxService()
                },
                enabled = canStart && ringtoneExists,
            ) {
                Text(
                    text = stringResource(R.string.start)
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
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = selectedFileName ?: stringResource(R.string.no_ringtone_selected),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        audioPickerLauncher.launch(
                            arrayOf("audio/*")
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.select_ringtone)
                    )
                }
            }
        },
    )
}
