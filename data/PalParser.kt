package jp.co.aranova.metrova.data

import android.util.Log

object PalParser {
    fun parse(line: String): CueDevice? {
        if (!line.startsWith(":")) return null

        return try {
            val hexContent = line.substring(1)
            val bytes = hexContent.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()

            if (bytes.size < 15) return null

            val lqi = bytes[4].toInt() and 0xFF
            val sid = toHex(bytes, 7, 4)
            val logicalId = bytes[11].toInt() and 0xFF
            val sensorNum = bytes[14].toInt() and 0xFF

            var addr = 15
            var hallic: Int? = null
            var powerMv: Int? = null
            var accelX: Float? = null
            var accelY: Float? = null
            var accelZ: Float? = null

            repeat(sensorNum) {
                if (addr + 4 > bytes.size) return@repeat
                val param = toInt(bytes, addr, 4)
                addr += 4

                val sensorId = (param shr 16) and 0xFF
                val dataNum = param and 0xFF

                if (addr + dataNum > bytes.size) return@repeat

                when (sensorId) {
                    0x00, 0x11 -> { // 磁力/开合状态
                        hallic = toInt(bytes, addr, dataNum) and 0x7F
                    }
                    0x30 -> { // 电池电压
                        powerMv = toInt(bytes, addr, dataNum)
                    }
                    0x04 -> { // 加速度数据
                        if (dataNum >= 6) {
                            accelX = toSigned(toInt(bytes, addr, 2), 2) / 1000f
                            accelY = toSigned(toInt(bytes, addr + 2, 2), 2) / 1000f
                            accelZ = toSigned(toInt(bytes, addr + 4, 2), 2) / 1000f
                        }
                    }
                }
                addr += dataNum
            }

            Log.d("TWELITE_PROC", "Parsed SID: $sid, LQI: $lqi, Volt: $powerMv")
            CueDevice(logicalId, sid, lqi, powerMv, hallic, accelX, accelY, accelZ)

        } catch (e: Exception) {
            Log.e("TWELITE_ERROR", "Parse Error: ${e.message}")
            null
        }
    }

    private fun toHex(b: ByteArray, offset: Int, len: Int): String =
        b.sliceArray(offset until offset + len).joinToString("") { "%02X".format(it) }

    private fun toInt(b: ByteArray, offset: Int, len: Int): Int {
        var v = 0
        for (i in 0 until len) v = (v shl 8) or (b[offset + i].toInt() and 0xFF)
        return v
    }

    private fun toSigned(v: Int, bytes: Int): Int {
        val bits = bytes * 8
        return if (v and (1 shl (bits - 1)) != 0) v - (1 shl bits) else v
    }
}