package jp.syoboi.android.notificationutil

import android.app.Activity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testNotification.setOnClickListener { testNotification(this) }

        readNotificationSetting.paint.isUnderlineText = true
        readNotificationSetting.setOnClickListener { startNotificationListenerSettingActivity(this) }

        drawOverlaySetting.paint.isUnderlineText = true
        drawOverlaySetting.setOnClickListener { startManageOverlayPermissionActivity(this) }

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
                if (isEnabledReadNotification(this)) View.GONE else View.VISIBLE

        drawOverlaySetting.visibility =
                if (canDrawOverlays(this)) View.GONE else View.VISIBLE

        popupNotificationGroup.visibility =
                if (popupNotification.isChecked) View.VISIBLE else View.GONE
    }
}
