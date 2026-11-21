package jp.ikigai.toolbox.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import jp.ikigai.toolbox.R
import jp.ikigai.toolbox.utils.getAvailableSounds

@Composable
fun SelectSoundDialog(
    pickSound: () -> Unit,
    selectSound: (String) -> Unit,
    dismiss: () -> Unit,
) {
    val context = LocalContext.current

    val availableSounds by remember(key1 = context) {
        mutableStateOf(
            context.getAvailableSounds()
        )
    }

    AlertDialog(
        modifier = Modifier
            .fillMaxWidth(0.9f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
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
                onClick = pickSound,
            ) {
                Text(
                    text = stringResource(R.string.select_file)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.select_sound),
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (availableSounds.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_sounds_found),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 50.dp, horizontal = 10.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(
                    items = availableSounds,
                ) { fileName ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        onClick = {
                            selectSound(fileName)
                            dismiss()
                        }
                    ) {
                        Text(
                            text = fileName,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        },
    )
}
