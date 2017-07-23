package jp.syoboi.android.notificationutil

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testNotification.setOnClickListener { testNotification(this) }

        // 通知読み取り設定の警告
        readNotificationSetting.paint.isUnderlineText = true
        readNotificationSetting.setOnClickListener { startNotificationListenerSettingActivity(this) }

        // 重ねて描画設定の警告
        drawOverlaySetting.paint.isUnderlineText = true
        drawOverlaySetting.setOnClickListener { startManageOverlayPermissionActivity(this) }

        updateViews()
    }

    override fun onResume() {
        super.onResume()

        val prefs = Prefs.get(this)

        popupNotification.isChecked = prefs.get(BooleanValues.popupNotification)
        speechNotification.isChecked = prefs.get(BooleanValues.speechNotification)

        popupNotification.setOnCheckedChangeListener { compoundButton, b ->
            prefs.editor().put(BooleanValues.popupNotification, b).apply()
            val i = Intent(this@MainActivity, NotificationListener::class.java)
            i.action = NotificationListener.ACTION_OVERLAY_CHANGED
            sendBroadcast(i)
            updateViews()
        }
        speechNotification.setOnCheckedChangeListener { compoundButton, b ->
            prefs.editor().put(BooleanValues.speechNotification, b).apply()
            updateViews()
        }

        updateViews()
    }

    override fun onPause() {
        popupNotification.setOnCheckedChangeListener(null)
        speechNotification.setOnCheckedChangeListener(null)

        super.onPause()
    }

    fun updateViews() {
        readNotificationSetting.visibility =
                if (isEnabledReadNotification(this)) View.GONE else View.VISIBLE

        drawOverlaySetting.visibility =
                if (canDrawOverlays(this)) View.GONE else View.VISIBLE

        popupNotificationGroup.visibility =
                if (popupNotification.isChecked) View.VISIBLE else View.GONE
    }
}
