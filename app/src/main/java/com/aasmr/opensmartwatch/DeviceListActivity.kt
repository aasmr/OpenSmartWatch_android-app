package com.aasmr.opensmartwatch


import android.content.Context
import android.os.Bundle
import android.content.Intent
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import androidx.core.content.ContextCompat.startActivity
import com.aasmr.opensmartwatch.ui.theme.OpenSmartWatchTheme

data class Device(val name: String, val macAddress: String, val status: String, val batteryLevel: Int)

class DeviceListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenSmartWatchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    DeviceListView(context = this@DeviceListActivity)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListView(modifier: Modifier = Modifier,
                   context: Context? = null,
                   devices:List<Device> ){
    var stateMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    OpenSmartWatchTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    modifier = Modifier,
                    title = {
                        Text("Подключённые устройства")
                    },
                    actions = {
                        IconButton(onClick = { stateMenu = !stateMenu }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Открыть меню"
                            )
                        }
                        DropdownMenu(
                            expanded = stateMenu,
                            onDismissRequest = { stateMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                val intent = Intent(context, ScanDeviceActivity::class.java)
                                context?.startActivity(intent)
                            },
                                text = {
                                    Text("Добавить устройство")
                                },
                            )
                        }
                    },
                )
            }
        )
        { innerPadding ->
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
                            .clickable { } // Вызов метода обратного вызова// Обработчик клика на элементе списка
                    ) {
                        androidx.compose.material.Text(
                            text = device.name ?: "Unknown Device",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                        androidx.compose.material.Text(
                            text = device.macAddress ?: "Unknown MAC Address",
                            style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                        )
                        androidx.compose.material.Text(
                            text = "Статус: ${device.status}",
                            style = TextStyle(fontSize = 14.sp)
                        )
                        androidx.compose.material.Text(
                            text = "${device.batteryLevel}",
                            style = TextStyle(fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun DeviceListViewPreview() {
    OpenSmartWatchTheme {
        DeviceListView()
    }
}