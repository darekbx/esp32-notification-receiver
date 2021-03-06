package com.darekbx.m5stickwidget

import android.app.Application
import com.darekbx.m5stickwidget.di.AppModule
import com.darekbx.m5stickwidget.di.DaggerAppComponent

class M5WidgetApplication : Application() {

    val appComponent = DaggerAppComponent
        .builder()
        .appModule(AppModule(this))
        .build()
}