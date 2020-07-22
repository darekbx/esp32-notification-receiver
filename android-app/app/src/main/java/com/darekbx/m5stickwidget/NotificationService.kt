package com.darekbx.m5stickwidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.darekbx.m5stickwidget.bluetooth.BluetoothWrapper
import com.darekbx.m5stickwidget.ui.MainActivity
import java.lang.IllegalStateException
import javax.inject.Inject

class
NotificationService : NotificationListenerService() {

    companion object {
        val NOTIFICATION_CHANNEL_ID = "m5_notification_channel"
        val NOTIFICATION_ID = 1
        val ACTION_RESET = "action_reset"
        val IGNORED_PACKAGES = listOf(
            "com.darekbx.m5stickwidget",
            "com.google.android.packageinstaller",
            "com.android.providers.downloads",
            "com.brave.browser",
            "android",
            "com.android.vending"
        )
        val TRANSLATIONS = mapOf(
            "messaging" to "SMS",
            "aib" to "Alior Bank",
            "gm" to "Gmail",
            "tablica" to "OLX",
            "calendar" to "Calendar",
            "inmobile" to "InPost",
            "dialer" to "Dialer",
            "gms" to "Google Play"
        )
    }

    @Inject
    lateinit var bluetoothWrapper: BluetoothWrapper

    private var isConnected = false

    var communicationBroadcast = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    ACTION_RESET -> resetBluetooth()
                }
            }
        }
    }

    fun resetBluetooth() {
        removeActiveNotification()
        bluetoothWrapper.dispose()
        bluetoothWrapper.connectToDevice("M5StickC Widget")
    }

    override fun onCreate() {
        super.onCreate()
        (application as M5WidgetApplication).appComponent.inject(this)

        registerReceiver(communicationBroadcast, IntentFilter(ACTION_RESET))

        bluetoothWrapper.writeStatus = { }

        bluetoothWrapper.deviceStatus = {

            isConnected = when (it) {
                BluetoothWrapper.DeviceStatus.CONNECTED -> true
                BluetoothWrapper.DeviceStatus.NOTIFICATIONS_SET -> true
                BluetoothWrapper.DeviceStatus.FAILED -> false
                BluetoothWrapper.DeviceStatus.DISCONNECTED -> false
                else -> false
            }

            when (isConnected) {
                true -> createActiveNotification()
                false -> removeActiveNotification()
            }

            sendBroadcast(Intent(MainActivity.ACTION_STATUS).apply {
                putExtra(MainActivity.STATUS_KEY, isConnected)
            })
        }

        bluetoothWrapper.connectToDevice("M5StickC Widget")
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothWrapper.dispose()

        try {
            unregisterReceiver(communicationBroadcast)
        } catch (e: IllegalStateException) {
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (isConnected) {
            sbn?.let { statusBarNotification ->
                if (!IGNORED_PACKAGES.contains(statusBarNotification.packageName)) {
                    forwardNotificationToWidget(statusBarNotification)
                }
            }
        }
    }

    private fun forwardNotificationToWidget(statusBarNotification: StatusBarNotification) {
        val packangeChunks = statusBarNotification.packageName.split(".")
        val packageName = packangeChunks.lastOrNull()
        packageName?.let { packageName ->
            Log.v("------------", "packageName: ${statusBarNotification.packageName}")
            if (!IGNORED_PACKAGES.contains(statusBarNotification.packageName)) {
                val applicationName = TRANSLATIONS.get(packageName) ?: packageName

                with(statusBarNotification.notification.extras) {
                    val title = getString(NotificationCompat.EXTRA_TITLE) ?: ""
                    val text = getString(NotificationCompat.EXTRA_TEXT) ?: ""
                    bluetoothWrapper.write("$applicationName,$title,$text")
                }
            }
        }
    }

    private fun createActiveNotification() {
        val builder = NotificationCompat.Builder(this,
            NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_m5)
            .setContentTitle(getString(R.string.widget_is_active))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "M5", importance)

        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun removeActiveNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}