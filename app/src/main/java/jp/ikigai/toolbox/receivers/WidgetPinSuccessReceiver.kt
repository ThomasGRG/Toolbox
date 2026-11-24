package jp.ikigai.toolbox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import jp.ikigai.toolbox.R

class WidgetPinSuccessReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        Toast.makeText(
            context,
            R.string.widget_added,
            Toast.LENGTH_LONG
        ).show()
    }
}