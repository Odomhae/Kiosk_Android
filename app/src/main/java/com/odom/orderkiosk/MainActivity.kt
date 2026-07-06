package com.odom.orderkiosk

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.odom.orderkiosk.databinding.ActivityMainBinding
import com.odom.orderkiosk.ui.order.OrderFragment
import com.odom.orderkiosk.utils.TtsSettings
import kotlinx.coroutines.Job
import java.util.Locale
import java.util.UUID


class MainActivity : AppCompatActivity(), RecognitionListener, TextToSpeech.OnInitListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val speechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(this) }
    private var speechRecognizerIntent: Intent? = null
    private lateinit var textToSpeech: TextToSpeech
    private val textToSpeechReady = MutableLiveData(false)
    private var job: Job? = null

    // 종료 다이얼로그에 표시할 미리 로드된 배너 광고
    private var exitAdView: AdView? = null
    private var exitDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        speechRecognizer.setRecognitionListener(this)
        textToSpeech = TextToSpeech(this, this)

        MobileAds.initialize(this) {}
        loadExitAd()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OrderFragment())
            .commit()
    }

    // 다이얼로그를 띄우는 시점에 이미 로드되어 있도록 앱 시작/다이얼로그 닫힘 시 미리 로드
    private fun loadExitAd() {
        exitAdView?.destroy()

        val adView = AdView(this)
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE)
        adView.adUnitId = getString(R.string.REAL_banner_ad_unit_id)
        adView.loadAd(AdRequest.Builder().build())

        exitAdView = adView
    }

    // 첫 화면에서 뒤로가기 시 OrderFragment가 직접 호출 (광고 포함 종료 다이얼로그)
    fun showExitDialog() {
        if (exitDialog?.isShowing == true) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_exit, null)
        val adContainer = dialogView.findViewById<FrameLayout>(R.id.exit_ad_container)

        exitAdView?.let { adView ->
            (adView.parent as? ViewGroup)?.removeView(adView)
            adContainer.addView(adView)
        }

        exitDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.exit) { _, _ -> finish() }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .apply {
                setOnDismissListener {
                    adContainer.removeAllViews()
                    // 취소한 경우 다음 종료 시도에 새 광고가 준비되도록 다시 로드
                    if (!isFinishing) {
                        loadExitAd()
                    }
                }
                show()
            }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val contentView: View = this.findViewById(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { v, insets ->
            val innerPadding: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(0, innerPadding.top, 0, innerPadding.bottom)

            insets
        }


        fun isSystemInDarkMode() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val isLightStatusBars = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES
        val compat = WindowInsetsControllerCompat(this.window, this.window.decorView)

        compat.isAppearanceLightStatusBars = !isSystemInDarkMode()
        compat.isAppearanceLightNavigationBars = isLightStatusBars
    }

    override fun onDestroy() {
        exitDialog?.dismiss()
        exitAdView?.destroy()
        exitAdView = null

        textToSpeech.stop()
        textToSpeech.shutdown()
        speechRecognizer.destroy()

        super.onDestroy()
    }

    fun speakOut(text: String?) {
        // 음성 안내 꺼짐 설정이면 말하지 않음 (채팅 버블 표시는 OrderFragment에서 별도 처리됨)
        if (!TtsSettings.isEnabled(this)) return

        textToSpeechReady.observe(this, object : androidx.lifecycle.Observer<Boolean> {
            override fun onChanged(t: Boolean?) {
                if (t == true) {
                    textToSpeechReady.removeObserver(this)

                    if (textToSpeech.isSpeaking) {
                        textToSpeech.stop()
                    }

                    if (text == null) return

                    if (Resources.getSystem().configuration.locale != Locale.KOREA) {
                        textToSpeech.language = Locale.US
                    }

                    textToSpeech.speak(
                        text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        UUID.randomUUID().toString()
                    )
                }
            }
        })
    }

    // 음성 안내 토글을 껐을 때 진행 중인 안내를 즉시 중단
    fun stopSpeaking() {
        if (this::textToSpeech.isInitialized && textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
    }

    private fun stopSpeechRecognizer() {
        if (speechRecognizerIntent == null) return

        speechRecognizer.stopListening()
        speechRecognizerIntent = null

        binding.micButton.isSelected = false
        job?.cancel()
    }

    override fun onReadyForSpeech(params: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onBeginningOfSpeech() {
        TODO("Not yet implemented")
    }

    override fun onRmsChanged(rmsdB: Float) {
        TODO("Not yet implemented")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onEndOfSpeech() {
        Log.d("MainActivity", "onEndOfSpeech")

        stopSpeechRecognizer()
    }

    override fun onError(error: Int) {
        val message: String = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
            SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
            SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
            SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER가 바쁨"
            SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
            else -> "알 수 없는 오류임"
        }

        Log.e("MainActivity", "onError: $message")
    }

    override fun onResults(results: Bundle?) {
        Log.d("MainActivity", "onResults")

        if (results == null) return
        supportFragmentManager.setFragmentResult(SpeechRecognizer.RESULTS_RECOGNITION, results)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onInit(status: Int) {
        Log.d("MainActivity", "onInit")

        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.KOREA)
            if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.e("MainActivity", "This Language is not supported")
            } else {
                textToSpeechReady.postValue(true)
            }
        } else {
            Log.e("MainActivity", "Initialization Failed!")
        }
    }
}