package jp.ikigai.toolbox.ui.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class LockWidgetReceiver(override val glanceAppWidget: GlanceAppWidget = LockWidget()) :
    GlanceAppWidgetReceiver()