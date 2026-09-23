package com.doctorapp.data

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.doctorapp.BuildConfig
import com.doctorapp.network.TokenStore
import okhttp3.*
import okio.ByteString

/**
 * جایگزین FCM: یک اتصال WebSocket زنده به بک‌اند نگه می‌دارد و پیام‌های
 * "درخواست جدید" / "پیام جدید" را دریافت می‌کند. برای نسخه اول کافی است؛
 * در نسخه بعدی می‌توان UnifiedPush یا Foreground Service پایدارتر اضافه کرد.
 */
class NotificationSocketService : Service() {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        connect()
        return START_STICKY
    }

    private fun connect() {
        val token = TokenStore.accessToken ?: return
        val request = Request.Builder().url(BuildConfig.WS_BASE_URL).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                ws.send("""{"type":"auth","token":"$token"}""")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                // TODO: پیام را پارس کن و یک Notification محلی (NotificationCompat) نمایش بده
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                // TODO: تلاش مجدد با backoff
            }
        })
    }

    override fun onDestroy() {
        webSocket?.close(1000, "service stopped")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
