package jp.ikigai.toolbox

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import jp.ikigai.toolbox.receivers.ToolboxDeviceAdminReceiver
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

    val explanation = stringResource(id = R.string.admin_perm_explanation)

    val coroutineScope = rememberCoroutineScope()

    var isAdmin by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(context) {
        isAdmin = checkIsAdmin(context)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isAdmin = checkIsAdmin(context)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isAdmin) {
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
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        val adminReceiver =
                            ComponentName(context, ToolboxDeviceAdminReceiver::class.java)
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver)
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.request_admin_perm)
                    )
                }
            }
        }
    }
}

fun checkIsAdmin(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminReceiver = ComponentName(context, ToolboxDeviceAdminReceiver::class.java)
    return dpm.isAdminActive(adminReceiver)
}