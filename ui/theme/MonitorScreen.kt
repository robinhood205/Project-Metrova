package jp.co.aranova.metrova.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.co.aranova.metrova.data.TweRepository
import jp.co.aranova.metrova.data.CueDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen() {
    val deviceMap by TweRepository.devices.collectAsState()
    val deviceList = deviceMap.values.toList().sortedBy { it.sid }
    val logs by TweRepository.rawLogs.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Project Metrova Monitor") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Upper section: Device list
            LazyColumn(modifier = Modifier.weight(0.6f).padding(8.dp)) {
                items(deviceList) { device ->
                    DeviceCard(device)
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.5f))

            // Lower section: Communication logs
            Text("通信ログ (Raw RX Data)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(8.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                // Reversed to show the latest logs at the top
                items(logs.reversed()) { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceCard(device: CueDevice) {
    val bgColor = if (device.isOnline) MaterialTheme.colorScheme.surface else Color(0xFFFFEBEE)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "SID: ${device.sid}", fontWeight = FontWeight.Bold)
                Text(text = "電波強度(LQI): ${device.lqi}", color = if(device.lqi > 100) Color(0xFF388E3C) else Color.Red)
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "🔋 電圧: ${device.battery} mV", style = MaterialTheme.typography.bodySmall)
                Text(text = device.formattedTime, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "ステータス: ${device.statusText}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}