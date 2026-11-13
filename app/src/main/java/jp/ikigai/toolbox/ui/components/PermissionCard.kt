package jp.ikigai.toolbox.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
fun PermissionCard(
    permissionName: String,
    granted: Boolean,
    grant: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = permissionName,
                style = MaterialTheme.typography.titleMedium,
            )
            Button(
                onClick = grant,
                enabled = !granted,
            ) {
                Text(
                    text = stringResource(
                        if (granted) {
                            R.string.permission_granted
                        } else {
                            R.string.grant_permission
                        }
                    )
                )
            }
        }
    }
}
