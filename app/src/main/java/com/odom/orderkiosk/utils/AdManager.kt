package com.odom.orderkiosk.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("ad_preferences", Context.MODE_PRIVATE)
    
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoaded = false
    private var pendingShowCallback: (() -> Unit)? = null
    
    companion object {
        private const val ORDER_COUNT_KEY = "order_count"
    }
    
    fun loadInterstitialAd() {
        val adUnitId = context.getString(com.odom.orderkiosk.R.string.REAL_FULLSCREEN_ad_unit_id)

        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isAdLoaded = true
                Log.d("===ttt", "광고 로드 성공")
                
                // 대기 중인 표시 요청이 있으면 즉시 표시
                pendingShowCallback?.invoke()
                pendingShowCallback = null
            }
            
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                interstitialAd = null
                isAdLoaded = false
                Log.d("===ttt", "광고 로드 실패: ${loadAdError.message}")
                
                // 대기 중인 표시 요청 콜백 호출
                pendingShowCallback?.invoke()
                pendingShowCallback = null
            }
        })
    }
    
    fun shouldShowAd(): Boolean {
        val orderCount = getOrderCount()
        Log.d("===ttt1111" , orderCount.toString())

        return orderCount % 2 == 0 // 2번에 한번
    }
    
    fun showInterstitialAd(onAdDismissed: () -> Unit) {
        if (!shouldShowAd()) {
            Log.d("===ttt", "광고 표시 조건 불충족")
            onAdDismissed()
            return
        }
        
        if (isAdLoaded && interstitialAd != null) {
            try {
                interstitialAd?.show(context as Activity)
                interstitialAd = null
                isAdLoaded = false
                loadInterstitialAd() // 다음 광고 로드
            } catch (e: Exception) {
                Log.d("===ttt", "광고 표시 실패: ${e.message}")
            }
        } else {
            Log.d("===ttt", "광고 로드 대기 중...")
            // 광고가 아직 로드되지 않았으면 로드 후 표시
            pendingShowCallback = {
                if (interstitialAd != null) {
                    try {
                        interstitialAd?.show(context as Activity)
                        interstitialAd = null
                        isAdLoaded = false
                        loadInterstitialAd() // 다음 광고 로드
                    } catch (e: Exception) {
                        Log.d("===ttt", "광고 표시 실패: ${e.message}")
                    }
                }
                onAdDismissed()
            }
            if (!isAdLoaded) {
                loadInterstitialAd()
            }
        }
    }
    
    fun incrementOrderCount() {
        val currentCount = getOrderCount()
        sharedPreferences.edit().putInt(ORDER_COUNT_KEY, currentCount + 1).apply()
    }
    
    private fun getOrderCount(): Int {
        return sharedPreferences.getInt(ORDER_COUNT_KEY, 0)
    }

}
