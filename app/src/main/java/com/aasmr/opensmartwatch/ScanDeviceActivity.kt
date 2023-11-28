package com.aasmr.opensmartwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.aasmr.opensmartwatch.ui.theme.OpenSmartWatchTheme

class ScanDeviceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenSmartWatchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScanDeviceView()
                }
            }
        }
    }
}

@Composable
fun ScanDeviceView(modifier: Modifier = Modifier) {
}

@Preview(showBackground = true)
@Composable
fun ScanDeviceViewPreview() {
    OpenSmartWatchTheme {
        ScanDeviceView()
    }
}