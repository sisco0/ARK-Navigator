package space.taran.arknavigator.ui

import android.app.Application
import android.content.Context
import org.acra.config.toast
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import space.taran.arknavigator.BuildConfig
import space.taran.arknavigator.di.AppComponent
import space.taran.arknavigator.di.DaggerAppComponent
import space.taran.arknavigator.di.modules.AppModule

class App: Application() {

    companion object {
        lateinit var instance: App
    }

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        instance = this

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            toast {
                text = "Sorry, the app crashed. Developers will be notified."
            }
        }
    }
}