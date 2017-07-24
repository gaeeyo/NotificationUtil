package jp.syoboi.android.notificationutil

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener

class TtsHelper {

    var textToSpeech: TextToSpeech? = null
    var ttsReady: Boolean = false
    var ttsPendingMessages: ArrayList<CharSequence>? = null


    fun speech(context: Context, message: CharSequence) {
        val tts = getTts(context)

        if (ttsReady) {
            tts.speak(message, TextToSpeech.QUEUE_ADD, null,
                    System.currentTimeMillis().toString())
        } else {
            if (ttsPendingMessages == null) {
                ttsPendingMessages = ArrayList<CharSequence>()
            }
            ttsPendingMessages?.add(message)
        }
    }

    private fun getTts(context :Context): TextToSpeech {
        var tts = textToSpeech
        if (tts == null) {
            ttsReady = false
            tts = TextToSpeech(context, object : TextToSpeech.OnInitListener {
                override fun onInit(p0: Int) {
                    ttsReady = true
                    ttsPendingMessages?.forEach {
                        textToSpeech?.speak(it, TextToSpeech.QUEUE_ADD, null,
                                System.currentTimeMillis().toString())
                    }
                    ttsPendingMessages = null
                }
            })
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(p0: String?) {
                }

                override fun onError(p0: String?) {
                }

                override fun onDone(p0: String?) {
                }
            })
            textToSpeech = tts
        }
        return tts
    }
}
