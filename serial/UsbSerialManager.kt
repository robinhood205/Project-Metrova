package jp.co.aranova.metrova.serial

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import jp.co.aranova.metrova.data.PalParser
import jp.co.aranova.metrova.data.TweRepository
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager

class UsbSerialManager(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var ioManager: SerialInputOutputManager? = null
    private var usbSerialPort: UsbSerialPort? = null

    // Thread-safe buffer for SerialInputOutputManager
    private val lineBuffer = StringBuffer()

    companion object {
        const val ACTION_USB_PERMISSION = "com.example.twelitecueapp.USB_PERMISSION"
        const val BAUD_RATE = 115200
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                // 1. Handle permission request result
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }

                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device?.let {
                                TweRepository.addLog("USB権限を取得しました。接続を開始します...")
                                connectToDevice(it)
                            }
                        } else {
                            TweRepository.addLog("ERROR: ユーザーがUSB権限を拒否しました")
                        }
                    }
                }
                // 2. Hardware attached
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    TweRepository.addLog("USBデバイスの挿入を検知。自動再接続を試行します...")
                    open()
                }
                // 3. Hardware detached
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    TweRepository.addLog("USBデバイスが取り外されました。リソースを解放します")
                    disconnectPort()
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(usbReceiver, filter)
        }
    }

    @Synchronized
    fun open(): Boolean {
        if (usbSerialPort != null && usbSerialPort!!.isOpen) {
            return true
        }

        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            TweRepository.addLog("USBデバイスが見つかりません。物理的な接続を確認してください")
            return false
        }

        val driver = availableDrivers[0]
        val device = driver.device

        if (usbManager.hasPermission(device)) {
            return connectToDevice(device)
        } else {
            TweRepository.addLog("USB権限をリクエストしています...")
            val intent = Intent(ACTION_USB_PERMISSION).apply {
                setPackage(context.packageName)
            }
            val permissionIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_MUTABLE
            )
            usbManager.requestPermission(device, permissionIntent)
            return false
        }
    }

    private fun connectToDevice(device: UsbDevice): Boolean {
        return try {
            val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
            val port = driver.ports[0]
            val connection = usbManager.openDevice(device) ?: return false

            port.open(connection)
            port.setParameters(BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            usbSerialPort = port

            ioManager = SerialInputOutputManager(usbSerialPort, object : SerialInputOutputManager.Listener {
                override fun onNewData(data: ByteArray) {
                    processRawData(String(data))
                }

                override fun onRunError(e: Exception) {
                    TweRepository.addLog("シリアル通信エラー: ${e.message}")
                    disconnectPort()
                }
            })
            ioManager?.start()

            TweRepository.addLog("シリアルポート接続完了。センサー信号を待機中...")
            true
        } catch (e: Exception) {
            TweRepository.addLog("接続失敗: ${e.message}")
            false
        }
    }

    private fun processRawData(rawText: String) {
        lineBuffer.append(rawText)
        var newlineIndex: Int
        while (lineBuffer.indexOf("\n").also { newlineIndex = it } >= 0) {
            val line = lineBuffer.substring(0, newlineIndex).trim()
            lineBuffer.delete(0, newlineIndex + 1)

            if (line.startsWith(":")) {
                TweRepository.addLog("RX: $line")
                try {
                    val device = PalParser.parse(line)
                    device?.let { TweRepository.updateDevice(it) }
                } catch (ignored: Exception) { }
            }
        }
        if (lineBuffer.length > 2048) lineBuffer.setLength(0)
    }

    @Synchronized
    private fun disconnectPort() {
        try {
            ioManager?.stop()
            usbSerialPort?.close()
        } catch (ignored: Exception) { }
        finally {
            ioManager = null
            usbSerialPort = null
        }
    }

    @Synchronized
    fun close() {
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (ignored: Exception) {}
        disconnectPort()
    }
}