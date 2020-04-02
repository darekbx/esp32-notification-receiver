package co.cosmose.m5stickwidget.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import co.cosmose.m5stickwidget.M5WidgetApplication
import co.cosmose.m5stickwidget.bluetooth.BluetoothWrapper
import co.cosmose.m5stickwidget.utils.PermissionsHelper
import dagger.Module
import dagger.Provides

@Module
class AppModule(val application: M5WidgetApplication) {

    @Provides
    fun provideApplication() = application

    @Provides
    fun provideContext(): Context = application

    @Provides
    fun providePermissionsHelper() = PermissionsHelper()

    @Provides
    fun provideBluetoothLeScanner(adapter: BluetoothAdapter?): BluetoothLeScanner? = adapter?.bluetoothLeScanner

    @Provides
    fun provideBluetoothAdapter(manager: BluetoothManager?): BluetoothAdapter? = manager?.adapter

    @Provides
    fun provideBluetoothManager(context: Context) = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    @Provides
    fun provideBluetoothWrapper(application: M5WidgetApplication, bluetoothLeScanner: BluetoothLeScanner?)
            = BluetoothWrapper(application, bluetoothLeScanner)
}