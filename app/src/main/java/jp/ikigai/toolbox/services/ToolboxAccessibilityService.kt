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

package jp.ikigai.toolbox.services

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import jp.ikigai.toolbox.data.AppModule
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType
import jp.ikigai.toolbox.utils.startToolboxService

class ToolboxAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        AppModule.serviceLogDao.insert(
            serviceType = ServiceType.ACCESSIBILITY,
            logMessage = LogMessage.CONNECTED,
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        when (event?.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                AppModule.serviceLogDao.insert(
                    serviceType = ServiceType.ACCESSIBILITY,
                    logMessage = LogMessage.VOL_UP,
                )
                startToolboxService(SoundFixerService.ACTION_STOP_PLAYBACK)
                return false
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                AppModule.serviceLogDao.insert(
                    serviceType = ServiceType.ACCESSIBILITY,
                    logMessage = LogMessage.VOL_DOWN,
                )
                startToolboxService(SoundFixerService.ACTION_STOP_PLAYBACK)
                return false
            }
        }
        return super.onKeyEvent(event)
    }

    fun lockScreen() {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    companion object {
        var instance: ToolboxAccessibilityService? = null
    }
}
