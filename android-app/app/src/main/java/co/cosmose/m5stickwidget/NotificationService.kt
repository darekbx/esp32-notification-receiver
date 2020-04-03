package co.cosmose.m5stickwidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import co.cosmose.m5stickwidget.bluetooth.BluetoothWrapper
import javax.inject.Inject

class NotificationService : NotificationListenerService() {

    @Inject
    lateinit var bluetoothWrapper: BluetoothWrapper

    private var isConnected = false

    override fun onCreate() {
        super.onCreate()
        (application as M5WidgetApplication).appComponent.inject(this)

        bluetoothWrapper.writeStatus = {
            Log.v("------------", "Write status: ${it.name}")
        }

        bluetoothWrapper.deviceStatus = {
            Log.v("------------", "Device status: ${it.name}")

            isConnected = when (it) {
                BluetoothWrapper.DeviceStatus.CONNECTED -> true
                BluetoothWrapper.DeviceStatus.NOTIFICATIONS_SET -> true
                BluetoothWrapper.DeviceStatus.FAILED -> false
                BluetoothWrapper.DeviceStatus.DISCONNECTED -> false
              else -> false
            }


            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            when(isConnected) {
                true -> {
                    val builder = NotificationCompat.Builder(this, "m5_notification_channel")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Active")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val channel = NotificationChannel("m5_notification_channel", "M5", importance)

                    notificationManager.createNotificationChannel(channel)
                    notificationManager.notify(1, builder.build())
                }
                false -> {
                    notificationManager.cancel(1)
                }
            }
        }


        bluetoothWrapper.connectToDevice("M5StickC Widget")
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothWrapper.dispose()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (isConnected) {
            sbn?.let { statusBarNotification ->
                val packangeChunks = statusBarNotification.packageName.split(".")
                bluetoothWrapper.write(packangeChunks.lastOrNull() ?: "Unknonw")
            }
        }
    }
}