package co.cosmose.m5stickwidget.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import co.cosmose.m5stickwidget.M5WidgetApplication
import co.cosmose.m5stickwidget.NotificationService
import co.cosmose.m5stickwidget.R
import co.cosmose.m5stickwidget.bluetooth.BluetoothWrapper
import co.cosmose.m5stickwidget.utils.PermissionsHelper
import co.cosmose.m5stickwidget.viewmodel.BLEViewModel
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    /**
     * TODO:
     *  - from activity select device to connect
     *  - add messages about missing notification permissions
     *  - parse different notitifications
     *
     */

    companion object {
        /**
         * Value from secured Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
         */
        val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"

        val NOTIFICATION_PERMISSION_RESULT_CODE = 100
    }

    @Inject
    lateinit var permissionsHelper: PermissionsHelper

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    internal lateinit var bleViewModel: BLEViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as M5WidgetApplication).appComponent.inject(this)

        bleViewModel = ViewModelProvider(this, viewModelFactory)[BLEViewModel::class.java]
        bleViewModel.receivedNewData.observe(this@MainActivity, Observer { data ->
            Log.v("------------", "Received new data: ${data}")
        })
        bleViewModel.writeStatus.observe(this@MainActivity, Observer { status ->
            Log.v("------------", "Write status: ${status.name}")
        })
        bleViewModel.deviceStatus.observe(this@MainActivity, Observer { status ->

            Log.v("------------", "Device status; ${status.name}")

            if (status == BluetoothWrapper.DeviceStatus.NOTIFICATIONS_SET) {
                test.postDelayed({
                    bleViewModel.write("Test data")
                }, 1000)
            }
        })

        checkBLESupport()
    }

    fun isNotificationPermissionRequired(): Boolean {
        val component = ComponentName(this, NotificationService::class.java)
        val value = Settings.Secure.getString(this.contentResolver, ENABLED_NOTIFICATION_LISTENERS)
        return value?.contains(component.flattenToString())?.not() ?: false
    }

    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivityForResult(intent, NOTIFICATION_PERMISSION_RESULT_CODE)
    }

    override fun onResume() {
        super.onResume()

        if (isNotificationPermissionRequired()) {
            requestNotificationPermission()
        } else {
            handlePermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionsHelper.PERMISSIONS_REQUEST_CODE) {
            val anyDenied = grantResults.any { it == PackageManager.PERMISSION_DENIED }
            when (anyDenied) {
                true -> Toast.makeText(applicationContext, R.string.permissions_are_required, Toast.LENGTH_SHORT).show()
                else -> startScan()
            }
        }
    }

    private fun startScan() {
      //  bleViewModel.connectToDevice("M5StickC Widget")
    }

    private fun checkBLESupport() {
        packageManager
                .takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }
                ?.also {
                    Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
                    finish()
                }
    }

    private fun handlePermissions() {
        val hasPermissions = permissionsHelper.checkAllPermissionsGranted(this)
        when (hasPermissions) {
            true -> startScan()
            else -> permissionsHelper.requestPermissions(this)
        }
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
}
