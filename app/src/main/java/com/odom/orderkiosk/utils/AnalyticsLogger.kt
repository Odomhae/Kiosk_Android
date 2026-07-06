package com.odom.orderkiosk.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

// 사용자 성장 측정용 최소 이벤트 로깅
// 이벤트 정의: docs/superpowers/specs/2026-07-07-user-growth-monetization-design.md
object AnalyticsLogger {

    private const val EVENT_ORDER_STARTED = "order_started"
    private const val EVENT_ORDER_COMPLETED = "order_completed"
    private const val EVENT_VOICE_INPUT_USED = "voice_input_used"

    private const val PARAM_LOCALE = "locale"
    private const val PARAM_ITEM_COUNT = "item_count"

    // 메뉴 화면 진입 = 주문 흐름 시작 (앱 시작 + 주문 완료 후 복귀 시마다 1회)
    fun logOrderStarted(context: Context) {
        log(context, EVENT_ORDER_STARTED, localeBundle(context))
    }

    fun logOrderCompleted(context: Context, itemCount: Int) {
        val bundle = localeBundle(context).apply {
            putLong(PARAM_ITEM_COUNT, itemCount.toLong())
        }
        log(context, EVENT_ORDER_COMPLETED, bundle)
    }

    fun logVoiceInputUsed(context: Context) {
        log(context, EVENT_VOICE_INPUT_USED, localeBundle(context))
    }

    private fun localeBundle(context: Context) = Bundle().apply {
        putString(PARAM_LOCALE, context.resources.configuration.locales[0].language)
    }

    private fun log(context: Context, event: String, params: Bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(event, params)
    }
}
