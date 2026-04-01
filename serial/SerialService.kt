package jp.co.aranova.metrova.serial

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import jp.co.aranova.metrova.data.TweRepository

class SerialService : Service() {

    private var usbManager: UsbSerialManager? = null
    private var isServiceStarted = false

    override fun onCreate() {
        super.onCreate()
        usbManager = UsbSerialManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        if (!isServiceStarted) {
            isServiceStarted = true

            TweRepository.addLog("システムサービス起動：シリアルポートの初期化を試行します...")

            val success = usbManager?.open() ?: false

            if (success) {
                TweRepository.addLog("シリアルポートのオープンに成功しました")
            }
        }

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "twelite_channel"
        val channelName = "TWELITE Monitor Service"

        val manager = getSystemService(NotificationManager::class.java)
        if (manager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(channelId) == null) {
                val chan = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW
                )
                manager.createNotificationChannel(chan)
            }
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Project Metrova")
            .setContentText("シリアルデータを監視しています...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceStarted = false
        usbManager?.close()
        TweRepository.addLog("システムサービスを終了し、リソースを解放しました")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}