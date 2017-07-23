package jp.syoboi.android.notificationutil

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.WindowManager
import android.view.WindowManager.LayoutParams

class NotificationListener : NotificationListenerService() {

    companion object {
        private val _ACTION_BASE = BuildConfig.APPLICATION_ID + ".action"
        val TAG = "NotificationListener"
        val ACTION_OVERLAY_CHANGED = _ACTION_BASE + ".overlay_changed"
    }

    var popupView: PopupView? = null
    lateinit var prefs: Prefs

    val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val prefs = Prefs.get(this@NotificationListener)
            if (prefs.get(BooleanValues.popupNotification)) {
                ensurePopup()
            } else {
                closePopup()
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        prefs = Prefs.get(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        registerReceiver(mReceiver, IntentFilter().apply { ACTION_OVERLAY_CHANGED })
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        unregisterReceiver(mReceiver)
        closePopup()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (!prefs.get(BooleanValues.popupNotification)) {
            return
        }
        
        val n: Notification = sbn?.notification ?: return

        if (n.flags == 0 && n.sound == null) {
            // 通知音を鳴らす設定の通知のみ扱う
            return
        }

        val message = extractMessage(sbn) ?: return
        if (message.isNotEmpty()) {
            onPopupMessage(message)
        }
    }

    fun onPopupMessage(message: CharSequence) {
        ensurePopup()
        popupView?.addLog(message)
    }

    fun ensurePopup() {
        if (popupView == null) {

            val prefs = Prefs.get(this)

            popupView = PopupView(this, prefs.get(IntValues.popupTextLines),
                    prefs.get(IntValues.popupScrollInterval).toLong())
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                    LayoutParams.TYPE_SYSTEM_ALERT,
                    LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT)
            wm.addView(popupView, lp)
        }
    }

    fun closePopup() {
        val view = popupView
        if (view != null) {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(view)
            popupView = null
        }

    }

    fun extractMessage(sbn: StatusBarNotification): CharSequence? {
        val n = sbn.notification
//        val template = n.extras.getString(NotificationCompat.EXTRA_TEMPLATE);
//        Log.d(TAG, "template: ${template} " + NotificationCompat.MessagingStyle::class.java.name)
        var msg = n.tickerText
        if (msg.isNullOrBlank()) {
            val text = n.extras.getCharSequence(NotificationCompat.EXTRA_TEXT)
            val title = n.extras.getCharSequence(NotificationCompat.EXTRA_TITLE)
            if (!text.isNullOrBlank()) {
                if (!title.isNullOrBlank()) {
                    msg = "${title}: ${text}"
                } else {
                    msg = text
                }
            }
        }
        Log.d(TAG, "msg: ${msg}")
        return msg
    }
}