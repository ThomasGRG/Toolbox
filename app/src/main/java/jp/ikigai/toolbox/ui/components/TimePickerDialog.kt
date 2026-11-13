package jp.ikigai.toolbox.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jp.ikigai.toolbox.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    dismiss: () -> Unit,
    set: () -> Unit,
    timePickerState: TimePickerState,
) {
    AlertDialog(
        onDismissRequest = {
            dismiss()
        },
        confirmButton = {
            Button(
                onClick = {
                    set()
                },
            ) {
                Text(
                    text = stringResource(R.string.set)
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
        title = null,
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
