package com.aasmr.opensmartwatch

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ScaffoldState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.aasmr.opensmartwatch.ui.theme.OpenSmartWatchTheme

data class BluetoothDeviceWithRssi(
    val name: String?,
    val macAddress: String?,
    val rssi: Int
)

interface DeviceClickListener {
    fun onDeviceClicked(device: BluetoothDevice)
}

class ScanDeviceActivity : AppCompatActivity(), DeviceClickListener {

    private var bluetoothService: BluetoothConnectionService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothConnectionService.LocalBinder
            bluetoothService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bluetoothService = null
            isBound = false
        }
    }

    private val devices = mutableStateListOf<BluetoothDeviceWithRssi>()
    private val devicesState = mutableStateOf<List<BluetoothDeviceWithRssi>>(emptyList())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Связываемся со службой BluetoothConnectionService
        val serviceIntent = Intent(this, BluetoothConnectionService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        setContent {
            OpenSmartWatchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),

                ) {
                    ScanDeviceView(devicesState.value, this)
                }
            }
        }
        checkAndRequestPermissions()
    }
    // Реализация функции из интерфейса DeviceClickListener
    override fun onDestroy() {
        super.onDestroy()
        // Отключаемся от службы BluetoothConnectionService при уничтожении активности
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothService?.connectToDevice(device)
    }

    override fun onDeviceClicked(device: BluetoothDevice) {
        connectToDevice(device)
    }
    // Внутри класса ScanActivity

    // Необходимые разрешения
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Проверка и запрос разрешений
    private fun checkAndRequestPermissions() {
        if (requiredPermissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE_PERMISSIONS)
        } else {
            // Разрешения уже предоставлены, можно начинать сканирование
            startBleScan()
        }
    }

    // Обработка результата запроса разрешений
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Все разрешения предоставлены, можно начинать сканирование
                startBleScan()
            } else {
                // Не все разрешения предоставлены, показать сообщение об ошибке
                //Toast.makeText(this, "Необходимо предоставить все разрешения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Константа для идентификации запроса разрешений
    private companion object {
        const val REQUEST_CODE_PERMISSIONS = 100
    }
    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter?.isEnabled == true) {
            val scanner = bluetoothAdapter.bluetoothLeScanner

            // Настройки сканирования
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            // Фильтр для сканирования (необязательно)
            val scanFilters: List<ScanFilter>? = null

            // Запуск сканирования
            scanner.startScan(scanFilters, scanSettings, scanCallback)
        } else {
            // Bluetooth выключен, запросить включение
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        }
    }

    // Callback для обработки найденных устройств
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val rssi = result.rssi
            val name = device.name

            // Проверяем, есть ли у устройства название
            if (!name.isNullOrBlank()) {
                val macAddress = device.address
                val bluetoothDeviceWithRssi = BluetoothDeviceWithRssi(
                    name = name,
                    macAddress = device.address,
                    rssi = rssi
                )
                // Проверяем, есть ли устройство с таким MAC-адресом в списке
                val existingDevice = devices.find { it.macAddress == macAddress }
                if (existingDevice == null) {
                    devices.add(bluetoothDeviceWithRssi)
                    devicesState.value = devices.toList() // Обновление состояния
                }
            }
        }
    }
}
@Composable
fun ScanDeviceView(
    devices: List<BluetoothDeviceWithRssi>,
    deviceClickListener: DeviceClickListener, // Параметр обратного вызова
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Добавление нового устройства") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back button click */ }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            items(devices) { device ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { deviceClickListener.onDeviceClicked(device.toBluetoothDevice()) } // Вызов метода обратного вызова// Обработчик клика на элементе списка
                ) {
                    Text(
                        text = device.name ?: "Unknown Device",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = device.macAddress ?: "Unknown MAC Address",
                        style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                    )
                    Text(
                        text = "RSSI: ${device.rssi}",
                        style = TextStyle(fontSize = 14.sp)
                    )
                }
            }
        }
    }
}
fun BluetoothDeviceWithRssi.toBluetoothDevice(): BluetoothDevice {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    return bluetoothAdapter?.getRemoteDevice(this.macAddress)
        ?: throw IllegalArgumentException("Invalid MAC address: ${this.macAddress}")
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun ScanDeviceViewPreview() {
    val devices = listOf(
        BluetoothDeviceWithRssi("Device 1", "00:11:22:33:44:55", -70),
        BluetoothDeviceWithRssi("Device 2", "AA:BB:CC:DD:EE:FF", -80),
        BluetoothDeviceWithRssi("Device 3", "11:22:33:44:55:66", -90)
    )
    OpenSmartWatchTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ScanDeviceView(devices, object : DeviceClickListener {
                override fun onDeviceClicked(device: BluetoothDevice) {
                    // Dummy implementation for preview
                }
            })
        }
    }
}