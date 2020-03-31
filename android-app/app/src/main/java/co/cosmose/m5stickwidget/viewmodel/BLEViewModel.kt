package co.cosmose.m5stickwidget.viewmodel

import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import co.cosmose.m5stickwidget.M5WidgetApplication
import java.util.*
import javax.inject.Inject

class BLEViewModel @Inject constructor(
    application: M5WidgetApplication,
    private val bluetoothLeScanner: BluetoothLeScanner?
) : AndroidViewModel(application) {

    companion object {
        val M5_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        val M5_CHARACTERISTIC_WRITE = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
        val M5_CHARACTERISTIC_NOTIFY = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    }

    private val context = getApplication<M5WidgetApplication>().applicationContext

    private var name: String? = null
    private var address: String? = null
    private var connectedDeviceGatt: BluetoothGatt? = null

    enum class DeviceStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        FAILED
    }

    var deviceStatus = MutableLiveData<DeviceStatus>()

    fun connectToDevice(name: String? = null, address: String? = null) {
        this.name = name
        this.address = address
        deviceStatus.postValue(DeviceStatus.CONNECTING)
        bluetoothLeScanner?.startScan(scanCallback)
    }

    fun write(value: String) {
        connectedDeviceGatt
            ?.getService(M5_SERVICE_UUID)
            ?.getCharacteristic(M5_CHARACTERISTIC_WRITE)
            ?.let { characteristic ->
                characteristic.setValue(value)
                connectedDeviceGatt?.writeCharacteristic(characteristic)
                Log.v("------------", "WRITE OK ${connectedDeviceGatt}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        connectedDeviceGatt?.run {
            disconnect()
            close()
        }
        bluetoothLeScanner?.stopScan(scanCallback)
    }

    private fun isCorrectDevice(device: BluetoothDevice) =
        name?.let { device.name == it }
            ?: address?.let { device.address == address }
            ?: false

    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result
                ?.takeIf { isCorrectDevice(it.device) }
                ?.let { connectGatt(it.device) }
        }
    }

    private fun connectGatt(device: BluetoothDevice) {
        bluetoothLeScanner?.stopScan(scanCallback)
        connectedDeviceGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> gatt?.discoverServices()
                BluetoothProfile.STATE_DISCONNECTED -> deviceStatus.postValue(DeviceStatus.DISCONNECTED)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    enableNotification()
                    deviceStatus.postValue(DeviceStatus.CONNECTED)
                }
                else -> deviceStatus.postValue(DeviceStatus.FAILED)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.v("------------", "NOTIFY WRITE OK to M5Stick")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.v(
                "------------",
                "NOTIFY NEW DATA FROM M5Stick '${characteristic?.getStringValue(0)}'"
            )
        }
    }

    private fun enableNotification() {
        connectedDeviceGatt
            ?.getService(M5_SERVICE_UUID)
            ?.getCharacteristic(M5_CHARACTERISTIC_NOTIFY)
            ?.let { characteristic ->
                connectedDeviceGatt?.setCharacteristicNotification(characteristic, true)
                characteristic.descriptors.forEach {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    connectedDeviceGatt?.writeDescriptor(it)
                }
                Log.v("------------", "notify OK")
            }
    }
}