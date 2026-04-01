package jp.co.aranova.metrova.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object TweRepository {
    private val _devices = MutableStateFlow<Map<String, CueDevice>>(emptyMap())
    val devices = _devices.asStateFlow()

    private val _rawLogs = MutableStateFlow<List<String>>(emptyList())
    val rawLogs = _rawLogs.asStateFlow()

    // Keep the latest 100 logs
    fun addLog(message: String) {
        val currentLogs = _rawLogs.value.toMutableList()
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        currentLogs.add("[$time] $message")
        if (currentLogs.size > 100) currentLogs.removeAt(0)
        _rawLogs.value = currentLogs
    }

    fun updateDevice(newDevice: CueDevice) {
        val current = _devices.value.toMutableMap()
        val oldDevice = current[newDevice.sid]

        // Merge logic: Retain old data if the new packet has null values
        val mergedDevice = if (oldDevice != null) {
            newDevice.copy(
                powerMv = newDevice.powerMv ?: oldDevice.powerMv,
                hallic = newDevice.hallic ?: oldDevice.hallic,
                accelX = newDevice.accelX ?: oldDevice.accelX,
                accelY = newDevice.accelY ?: oldDevice.accelY,
                accelZ = newDevice.accelZ ?: oldDevice.accelZ
                // lastUpdateTime updates automatically
            )
        } else {
            newDevice
        }

        current[newDevice.sid] = mergedDevice
        _devices.value = current
    }
}