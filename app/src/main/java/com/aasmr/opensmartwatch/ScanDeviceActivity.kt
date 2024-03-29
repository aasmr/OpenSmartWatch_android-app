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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat.startActivityForResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.aasmr.opensmartwatch.ui.theme.OpenSmartWatchTheme

class ScanDeviceActivity : AppCompatActivity() {

    private val devices = mutableListOf<BluetoothDevice>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenSmartWatchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),

                ) {
                    ScanDeviceView()
                }
            }
        }
        checkAndRequestPermissions()
    }
    // Внутри класса ScanActivity

    // Необходимые разрешения
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
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
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (!devices.contains(device)) {
                devices.add(device)
                // Обновить UI (например, вызвать recomposition)
            }
        }
    }
}
@Composable
fun ScanDeviceView(modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    OpenSmartWatchTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    modifier = Modifier,
                    title = {
                        Text("Добавление нового устройства")
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    },
                )
            }
        )
        { innerPadding ->
            Text(
                modifier = Modifier
                    .padding(innerPadding), text = "Нет подключённых устройств"
            )
        }
    }
}

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun ScanDeviceViewPreview() {
    OpenSmartWatchTheme {
        ScanDeviceView()
    }
}