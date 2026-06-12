package com.odom.orderkiosk.utils

import android.content.Context

// 음성 안내(TTS) 켜기/끄기 설정. 음성 인식(SpeechRecognizer)과는 무관
object TtsSettings {

    private const val PREFS_NAME = "tts_preferences"
    private const val KEY_TTS_ENABLED = "tts_enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_TTS_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_TTS_ENABLED, enabled)
            .apply()
    }
}
