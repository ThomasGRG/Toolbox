package jp.ikigai.toolbox

import android.app.Application
import jp.ikigai.toolbox.data.AppModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
    }
}
