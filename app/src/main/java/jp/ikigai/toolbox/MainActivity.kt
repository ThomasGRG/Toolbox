/*
Toolbox
Copyright (C) 2026 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.toolbox

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.STATUS_BAR_SERVICE
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import jp.ikigai.toolbox.services.LockService
import jp.ikigai.toolbox.ui.theme.ToolboxTheme
import jp.ikigai.toolbox.ui.widget.LockWidget
import jp.ikigai.toolbox.ui.widget.LockWidgetReceiver
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToolboxTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    val vibrateLabel = stringResource(id = R.string.vibrate_tile_label)

    val coroutineScope = rememberCoroutineScope()

    var serviceEnabled by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(context) {
        serviceEnabled = checkAccessibilityEnabled(context)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        serviceEnabled = checkAccessibilityEnabled(context)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (serviceEnabled) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                                receiver = LockWidgetReceiver::class.java,
                                preview = LockWidget(),
                                previewState = DpSize(100.dp, 100.dp),
                            )
                        }
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.add_lock_widget)
                    )
                }
            } else {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.request_accessibility_perm)
                    )
                }
            }
            Button(
                onClick = {
                    val statusBarManager =
                        context.getSystemService(STATUS_BAR_SERVICE) as StatusBarManager
                    statusBarManager.requestAddTileService(
                        ComponentName(
                            "jp.ikigai.toolbox",
                            "jp.ikigai.toolbox.services.VibrateTileService",
                        ),
                        vibrateLabel,
                        Icon.createWithResource(context, R.drawable.mobile_vibrate_24px),
                        {},
                        {},
                    )
                }
            ) {
                Text(
                    text = stringResource(id = R.string.add_vibrate_tile)
                )
            }
        }
    }
}

fun checkAccessibilityEnabled(context: Context): Boolean {
    val expectedComponentName = "${context.packageName}/${LockService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val splitter = TextUtils.SimpleStringSplitter(':')
    splitter.setString(enabledServices)
    while (splitter.hasNext()) {
        if (splitter.next().equals(expectedComponentName, ignoreCase = true)) {
            return true
        }
    }
    return false
}