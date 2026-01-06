package jp.ikigai.toolbox.services

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import jp.ikigai.toolbox.receivers.ToolboxDeviceAdminReceiver

class LockService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminReceiver = ComponentName(this, ToolboxDeviceAdminReceiver::class.java)
        if (dpm.isAdminActive(adminReceiver)) {
            dpm.lockNow()
        }
        stopSelf()
        return START_NOT_STICKY
    }
}