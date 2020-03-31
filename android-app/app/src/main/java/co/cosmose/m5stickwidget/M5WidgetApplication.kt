package co.cosmose.m5stickwidget

import android.app.Application
import co.cosmose.m5stickwidget.di.AppModule
import co.cosmose.m5stickwidget.di.DaggerAppComponent


class M5WidgetApplication : Application() {

    val appComponent = DaggerAppComponent
        .builder()
        .appModule(AppModule(this))
        .build()
}