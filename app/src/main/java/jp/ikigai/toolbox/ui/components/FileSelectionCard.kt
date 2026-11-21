package jp.ikigai.toolbox.ui.components

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FileSelectionCard(
    title: String,
    selectedFileName: String?,
    noFileSelectedPlaceholder: String,
    selectLabel: String,
    mimeTypes: Array<String>,
    filePickerLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>,
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = selectedFileName ?: noFileSelectedPlaceholder,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    filePickerLauncher.launch(mimeTypes)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = selectLabel,
                )
            }
        }
    }
}
