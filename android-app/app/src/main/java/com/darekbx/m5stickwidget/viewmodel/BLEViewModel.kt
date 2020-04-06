package com.darekbx.m5stickwidget.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.darekbx.m5stickwidget.bluetooth.BluetoothWrapper
import javax.inject.Inject

class BLEViewModel @Inject constructor(
    private val bluetoothWrapper: BluetoothWrapper
) : ViewModel() {

    var deviceStatus = MutableLiveData<BluetoothWrapper.DeviceStatus>()
    var writeStatus = MutableLiveData<BluetoothWrapper.WriteStatus>()
    var receivedNewData = MutableLiveData<String>()

    fun connectToDevice(name: String? = null, address: String? = null) {
        bluetoothWrapper.deviceStatus = { deviceStatus.postValue(it) }
        bluetoothWrapper.receivedNewData = { receivedNewData.postValue(it) }
        bluetoothWrapper.writeStatus = { writeStatus.postValue(it) }
        bluetoothWrapper.connectToDevice(name, address)
    }

    fun write(value: String) {
        bluetoothWrapper.write(value)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothWrapper.dispose()
    }
}