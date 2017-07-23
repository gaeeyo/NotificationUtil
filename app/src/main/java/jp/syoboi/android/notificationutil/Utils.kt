package jp.syoboi.android.notificationutil

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.NotificationCompat


fun isEnabledReadNotification(context: Context): Boolean {
    val orgListeners = Settings.Secure.getString(context.contentResolver,
            "enabled_notification_listeners")
    if (orgListeners.isNullOrEmpty()) {
        return false
    } else {
        val listeners = orgListeners.split(":").map { it.substringBefore("/") }
        return listeners.contains(context.packageName)
    }
}

fun canDrawOverlays(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return Settings.canDrawOverlays(context)
    } else {
        return true
    }
}


fun testNotification(context: Context) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val messagingStyle = NotificationCompat.MessagingStyle("テスト通知のユーザー名").apply {
        addMessage("テスト通知のメッセージ", System.currentTimeMillis(), "テスト通知の送信者")
        conversationTitle = "テスト通知のタイトル"
    }

    val n = NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentText("テスト通知のテキストです")
            .setContentTitle("テスト通知のタイトルです")
            .setTicker("テストの通知のティッカーです")
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(messagingStyle)
            .build()
    nm.notify(1, n)
}

fun startNotificationListenerSettingActivity(context: Activity) {
    val intent = Intent()
    intent.action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
    context.startActivity(intent)
}

fun startManageOverlayPermissionActivity(context: Activity) {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.getPackageName()}"));
    context.startActivityForResult(intent, 0)
}