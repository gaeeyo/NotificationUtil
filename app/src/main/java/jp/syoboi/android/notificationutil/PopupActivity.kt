package jp.syoboi.android.notificationutil

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.view.WindowManager
import java.util.*

class PopupActivity : Activity() {

    var popupView: PopupView? = null


    val rand = Random()
    val handler = Handler()

    override fun onResume() {
        super.onResume()
        handler.post(addLogRunnable)

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        popupView = PopupView(applicationContext, 4,
                if (BuildConfig.DEBUG) 1000 else 2000)
        popupView?.showBorder = true

        val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        )
        wm.addView(popupView, lp)
    }

    override fun onPause() {
        handler.removeCallbacks(addLogRunnable)

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.removeView(popupView)

        super.onPause()
    }


    val addLogRunnable = object : Runnable {
        override fun run() {
            val message = when (rand.nextInt(6)) {
                0 -> "さとう: こんにちは"
                1 -> "すずき: こんばんは"
                2 -> "たかはし: おはようございます"
                3 -> "The Canvas class holds the \"draw\" calls. To draw something, you need 4 basic components: A Bitmap to hold the pixels, a Canvas to host the draw calls (writing into the bitmap), a drawing primitive (e.g. Rect, Path, text, Bitmap), and a paint (to describe the colors and styles for the drawing)."
                4 -> "エツミ、エアーフィルター搭載の「ダストフリーエアーブロアー」\n" +
                        "21µm以上の粒子をカット　逆流防止弁も装備"
                else -> Date().toString()
            }
            popupView?.addLog(message)

            handler.removeCallbacks(this)
            handler.postDelayed(this, (rand.nextInt(5) + 1) * 1000L)
        }
    }
}