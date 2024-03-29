package com.aasmr.opensmartwatch


import android.content.Context
import android.os.Bundle
import android.content.Intent
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
//import androidx.core.content.ContextCompat.startActivity
import com.aasmr.opensmartwatch.ui.theme.OpenSmartWatchTheme

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
fun DeviceListView(modifier: Modifier = Modifier, context: Context? = null) {
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
fun DeviceListViewPreview() {
    OpenSmartWatchTheme {
        DeviceListView()
    }
}