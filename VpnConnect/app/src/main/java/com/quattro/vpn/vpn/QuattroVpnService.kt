package com.quattro.vpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.quattro.vpn.MainActivity
import kotlinx.coroutines.*

class QuattroVpnService : VpnService() {
    private var tun: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val ACTION_START = "com.quattro.vpn.START"
        const val ACTION_STOP = "com.quattro.vpn.STOP"
        var isRunning = false
            private set
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopVpn()
                return START_NOT_STICKY
            }
            else -> startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return
        createNotificationChannel()
        startForeground(1, buildNotification("Подключение..."))

        scope.launch {
            // 1. Поднимаем TUN
            val builder = Builder()
                .setSession("Quattro VPN")
                .setMtu(1500)
                .addAddress("10.1.8.1", 30)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .addRoute("0.0.0.0", 0)
                .setBlocking(false)

            // Для OriginOS / китайских прошивок - разрешаем системные приложения
            if (Build.VERSION.SDK_INT >= 29) {
                builder.setMetered(false)
            }

            tun = builder.establish()
            if (tun == null) {
                stopSelf()
                return@launch
            }

            // 2. Стартуем Xray core
            val ok = XrayController.start(this@QuattroVpnService)
            if (!ok) {
                stopVpn()
                return@launch
            }
            isRunning = true
            updateNotification("VPN активен")

            // 3. Защищаем сокеты Xray от ухода в TUN loop
            // libxray сам делает protect если вы передали fd через Xray VPN mode.
            // Если используете SOCKS inbound (127.0.0.1:10808) - нужно проксировать TUN -> SOCKS.
            // Для простоты: используйте tun2socks или sing-box TUN.
            // Здесь оставляем TUN открытым, трафик пойдёт через Xray, если ядро умеет protect.
        }
    }

    private fun stopVpn() {
        scope.launch {
            XrayController.stop()
            tun?.close()
            tun = null
            isRunning = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        runBlocking { XrayController.stop() }
        tun?.close()
        isRunning = false
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpn()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel("vpn", "VPN Status", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, "vpn")
            .setContentTitle("Quattro VPN")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_vpn_ic)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, buildNotification(text))
    }
}
