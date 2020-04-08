package com.darekbx.m5stickwidget.ui

import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.darekbx.m5stickwidget.M5WidgetApplication
import com.darekbx.m5stickwidget.NotificationService
import com.darekbx.m5stickwidget.R
import com.darekbx.m5stickwidget.utils.PermissionsHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalStateException
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    /**
     * TODO:
     *  - from activity select device to connect
     *  - add messages about missing notification permissions
     *
     */

    companion object {
        /**
         * Value from secured Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
         */
        val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
        val ACTION_STATUS = "action_status"
        val STATUS_KEY = "status_key"

        val NOTIFICATION_PERMISSION_RESULT_CODE = 100
    }

    var communicationBroadcast = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    ACTION_STATUS -> updateStatus(intent.getBooleanExtra(STATUS_KEY, false))
                }
            }
        }
    }

    @Inject
    lateinit var permissionsHelper: PermissionsHelper

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as M5WidgetApplication).appComponent.inject(this)

        restart.setOnClickListener {
            sendBroadcast(Intent(NotificationService.ACTION_RESET))
            animateButtonTap()
        }

        checkBLESupport()

        val isNotificationVisible = notificationManager.activeNotifications.size == 1
        updateStatus(isNotificationVisible)
    }

    private fun animateButtonTap() {
        restart.animate().scaleX(1.1F).scaleY(1.1F).setDuration(150).withEndAction {
            restart.animate().scaleX(1.0F).scaleY(1.0F).setDuration(100)
        }
    }

    private fun updateStatus(isConnected: Boolean) {
        val statusMessage = when (isConnected) {
            true -> R.string.status_connected
            else -> R.string.status_disconnected
        }
        status_text.isEnabled = isConnected
        status_text.setText(statusMessage)
    }

    fun isNotificationPermissionRequired(): Boolean {
        val component = ComponentName(this, NotificationService::class.java)
        val value = Settings.Secure.getString(this.contentResolver,
            ENABLED_NOTIFICATION_LISTENERS
        )
        return value?.contains(component.flattenToString())?.not() ?: false
    }

    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivityForResult(intent,
            NOTIFICATION_PERMISSION_RESULT_CODE
        )
    }

    override fun onResume() {
        super.onResume()

        if (isNotificationPermissionRequired()) {
            requestNotificationPermission()
        } else {
            handlePermissions()
        }

        registerReceiver(communicationBroadcast, IntentFilter(ACTION_STATUS))
    }

    override fun onPause() {
        super.onPause()

        try {
            unregisterReceiver(communicationBroadcast)
        } catch (e: IllegalStateException) {
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

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}
