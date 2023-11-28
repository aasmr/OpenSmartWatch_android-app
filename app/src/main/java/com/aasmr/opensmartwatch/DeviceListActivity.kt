package com.aasmr.opensmartwatch

import android.os.Bundle
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.aasmr.opensmartwatch.ui.theme.OpenSmartWatchTheme

class DeviceListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenSmartWatchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeviceListView()
                }
            }
        }
    }
}

@Composable
fun DeviceListView(modifier: Modifier = Modifier) {
    var stateMenu by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = rememberScaffoldState(),
        topBar = {
            TopAppBar(
                modifier = Modifier,
                title = {
                    Text("Список подключённых устройств")
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
                        DropdownMenuItem(onClick = { }) {
                            Text(text = "Добавить устройство")
                        }
                    }
                },
            )
        }
    )
    {
            innerPadding -> Text(modifier = Modifier
        .padding(innerPadding), text = "Нет подключённых устройств")
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