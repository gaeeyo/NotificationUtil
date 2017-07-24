package jp.syoboi.android.notificationutil

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.media.AudioManager
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

    lateinit var prefs: Prefs

    var popupView: PopupView? = null

    val ttsHelper = TtsHelper()

    val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onReceive intent:${p1}")
            val prefs = Prefs.get(this@NotificationListener)
            if (prefs.get(BooleanValues.popupNotification)) {
                closePopup()
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
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onListenerConnected")
        }
        super.onListenerConnected()
        registerReceiver(mReceiver, IntentFilter(ACTION_OVERLAY_CHANGED ))
    }

    override fun onListenerDisconnected() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onListenerDisconnected")
        }
        super.onListenerDisconnected()
        unregisterReceiver(mReceiver)
        closePopup()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val n: Notification = sbn?.notification ?: return

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "notification:${n}\nextras:${n.extras}\nticker:${n.tickerText}")
        }

        if (n.defaults and (Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE) == 0
                && n.sound == null) {
            // 通知音とバイブが両方とも鳴らない設定のものは無視
            return
        }

        val popup = prefs.get(BooleanValues.popupNotification)
        val speech = prefs.get(BooleanValues.speechNotification)

        val message = extractMessage(sbn) ?: return
        if (message.isNotEmpty()) {
            if (popup) {
                onPopupMessage(message)
            }
            if (speech) {
                onSpeechMessage(message)
            }
        }
    }

    fun onSpeechMessage(message: CharSequence) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (am.isWiredHeadsetOn or am.isBluetoothA2dpOn or am.isBluetoothScoOn) {
            ttsHelper.speech(this, message)
        }
    }


    fun onPopupMessage(message: CharSequence) {
        ensurePopup()
        popupView?.addLog(message)
    }


    fun ensurePopup() {
        if (popupView == null) {

            popupView = createPopupView(this)
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