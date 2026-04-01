package jp.co.aranova.metrova.data

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CueDevice(
    val logicalId: Int,
    val sid: String,
    val lqi: Int, // Signal quality (0-255)
    val powerMv: Int? = null,
    val hallic: Int? = null,
    val accelX: Float? = null,
    val accelY: Float? = null,
    val accelZ: Float? = null,
    var lastUpdateTime: LocalDateTime = LocalDateTime.now()
) {
    val formattedTime: String
        get() = lastUpdateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    val battery: String
        get() = powerMv?.let { "${it}mV" } ?: "-"

    val isMultiMode: Boolean
        get() = accelX != null || accelY != null || accelZ != null

    val isOnline: Boolean
        get() = Duration.between(lastUpdateTime, LocalDateTime.now()).seconds < 15

    val statusText: String
        get() {
            if (!isOnline) return "通信タイムアウト (OFFLINE)"

            val parts = mutableListOf<String>()

            // 1. Magnetic sensor status
            hallic?.let {
                parts.add(if (it == 0) "Open(Away)" else "Closed(Near)")
            }

            // 2. Acceleration status
            if (isMultiMode) {
                val x = String.format("%.2f", accelX ?: 0f)
                val y = String.format("%.2f", accelY ?: 0f)
                val z = String.format("%.2f", accelZ ?: 0f)
                parts.add("ACC: X:$x Y:$y Z:$z")
            }

            return if (parts.isEmpty()) "スタンバイ (Standby)" else parts.joinToString(" | ")
        }
}