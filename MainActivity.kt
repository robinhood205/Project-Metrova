package jp.co.aranova.metrova

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import jp.co.aranova.metrova.serial.SerialService
import jp.co.aranova.metrova.ui.theme.MonitorScreen
import jp.co.aranova.metrova.ui.theme.TweliteCueAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 仅针对 Android 13+ 申请通知权限（前台服务必需）
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // 2. 直接启动前台服务（因为 SDK >= 26，直接用 startForegroundService）
        startForegroundService(Intent(this, SerialService::class.java))

        setContent {
            TweliteCueAppTheme {
                MonitorScreen()
            }
        }
    }
}