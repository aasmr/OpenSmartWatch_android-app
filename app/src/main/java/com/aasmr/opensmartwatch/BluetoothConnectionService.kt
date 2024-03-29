package com.aasmr.opensmartwatch

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.concurrent.fixedRateTimer

class BluetoothConnectionService : Service() {

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )
    } else {
        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var connectedDevice: BluetoothDevice? = null
    private var timer: Timer? = null

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothConnectionService = this@BluetoothConnectionService
    }
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
    fun connectToDevice(device: BluetoothDevice) {
        // Проверяем разрешение на использование Bluetooth
        if (requiredPermissions.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            bluetoothGatt = device.connectGatt(this, false, gattCallback)

        } else {
            Log.e(TAG, "Bluetooth permission is not granted")
        }
    }

    fun disconnect() {
        if (requiredPermissions.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            connectedDevice = null
        } else {
            Log.e(TAG, "Bluetooth permission is not granted")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.")
                gatt?.discoverServices()
                // Запуск таймера при успешном соединении
                timer = fixedRateTimer(name = "timer", initialDelay = 0, period = 1800000) {
                    sendTimeRequest()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.")
                // Остановка таймера при разъединении
                timer?.cancel()
                timer = null
            }
        }
    }

    private fun sendTimeRequest() {
        if (requiredPermissions.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            bluetoothGatt?.let { gatt ->
                val timeCharacteristic =
                    gatt.getService(UUID.fromString("faa84dff-9112-404c-9735-34e4e14bb895"))
                        ?.getCharacteristic(UUID.fromString("fba84dff-9112-404c-9735-34e4e14bb895"))
                if (timeCharacteristic != null) {
                    // Получить текущее системное время
                    val currentTime = System.currentTimeMillis()
                    val totalSeconds = currentTime / 1000

                    val hours = (totalSeconds / 3600).toInt()
                    val minutes = ((totalSeconds % 3600) / 60).toInt()
                    val seconds = (totalSeconds % 60).toInt()
                    val timeBytes = byteArrayOf(hours.toByte(), minutes.toByte(), seconds.toByte())
                    // Записать время в характеристику
                    timeCharacteristic.value = timeBytes.clone()
                    gatt.writeCharacteristic(timeCharacteristic)
                }
            }
        } else {
            Log.e(TAG, "Bluetooth permission is not granted")
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        timer = null
    }
    companion object {
        private const val TAG = "BluetoothConnectionService"
    }
}