package jp.ikigai.toolbox.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class LockService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun lockScreen() {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    companion object {
        var instance: LockService? = null
    }
}