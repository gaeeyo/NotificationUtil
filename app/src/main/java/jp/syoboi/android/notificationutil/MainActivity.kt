package jp.syoboi.android.notificationutil

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readNotificationSetting.paint.isUnderlineText = true
        readNotificationSetting.setOnClickListener { startNotificationListenerSettingActivity() }

        popupNotification.setOnCheckedChangeListener { compoundButton, b ->
            updateViews()
        }

        updateViews()
    }

    override fun onResume() {
        super.onResume()
        updateViews()
    }

    fun updateViews() {
        readNotificationSetting.visibility =
                if (isEnabledReadNotification) View.GONE else View.VISIBLE
        popupNotificationGroup.visibility =
                if (popupNotification.isChecked) View.VISIBLE else View.GONE
    }

    private val isEnabledReadNotification: Boolean
        get() {
            val contentResolver = contentResolver
            val rawListeners = Settings.Secure.getString(contentResolver,
                    "enabled_notification_listeners")
            if (rawListeners == null || "" == rawListeners) {
                return false
            } else {
                val listeners = rawListeners.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                for (listener in listeners) {
                    if (listener.startsWith(packageName)) {
                        return true
                    }
                }
            }
            return false
        }

    private val canDrawOverlays: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(this)
            } else {
                return true
            }
        }


    fun testNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val n = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("テストの通知です")
                .setContentTitle("テスト")
                .build()
        nm.notify(1, n)
    }

    private fun startNotificationListenerSettingActivity() {
        val intent = Intent()
        intent.action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        startActivity(intent)
    }

    private fun startManageOverlayPermissionActivity() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${getPackageName()}"));
        startActivityForResult(intent, 0)
    }
}
