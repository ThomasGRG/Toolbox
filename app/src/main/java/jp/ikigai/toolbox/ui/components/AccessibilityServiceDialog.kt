package jp.ikigai.toolbox.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.utils.requestAccessibilitySettings

@Composable
fun AccessibilityServiceDialog(
    dismiss: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = dismiss,
        dismissButton = {
            Button(
                onClick = dismiss,
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    context.requestAccessibilitySettings()
                }
            ) {
                Text(
                    text = stringResource(R.string.grant_permission)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.accessibility_service)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.accessibility_service_desc)
            )
        },
    )
}
