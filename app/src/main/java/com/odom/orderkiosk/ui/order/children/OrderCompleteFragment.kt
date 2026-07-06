package com.odom.orderkiosk.ui.order.children

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.odom.orderkiosk.R
import com.odom.orderkiosk.databinding.FragmentOrderCompleteBinding
import com.odom.orderkiosk.databinding.ItemReceiptBinding
import com.odom.orderkiosk.utils.AdManager
import com.odom.orderkiosk.utils.AnalyticsLogger
import java.text.NumberFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class OrderCompleteFragment : OrderChildrenBaseFragment() {
    private var _binding: FragmentOrderCompleteBinding? = null
    private val binding get() = _binding!!
    private val adManager by lazy { AdManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderNumber = (1000..9999).random()  // 1000 <= n <= 9999

        with(binding) {
            toolbar.setOnClickListener { parentFragmentManager.popBackStack() }

            orderCompleteTextView.text = resources.getString(R.string.order_number) + ":  $orderNumber"
          //  orderCompleteTextView2.text = resources.getString(R.string.order_completed)
            orderCompleteTextView2.isVisible = false

            // 영수증: 주문 항목별 메뉴/옵션/수량/금액 요약
            val numberFormat = NumberFormat.getInstance(Locale.KOREA)
            orderList.elements.forEach { order ->
                val receiptBinding = ItemReceiptBinding.inflate(
                    LayoutInflater.from(view.context),
                    receiptContainer,
                    false
                )
                val count = (order.count ?: 1).coerceAtLeast(1)

                receiptBinding.receiptNameTextView.text = buildString {
                    append(order.food.name)
                    order.option?.takeIf { it.isNotBlank() }?.let { append(" ($it)") }
                    append(" x $count")
                }

                val details = listOfNotNull(
                    order.sideMenu?.let { side ->
                        side.name + (order.sideMenuOption?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: "")
                    },
                    order.beverage?.let { beverage ->
                        beverage.name + (order.beverageOption?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: "")
                    }
                )
                if (details.isNotEmpty()) {
                    receiptBinding.receiptDetailTextView.isVisible = true
                    receiptBinding.receiptDetailTextView.text = details.joinToString(", ")
                }

                receiptBinding.receiptPriceTextView.text = String.format(
                    getString(R.string.price_format1),
                    numberFormat.format(order.price * count)
                )

                receiptContainer.addView(receiptBinding.root)
            }

            takeoutTextView.text = if (orderList.takeOut) {
                getString(R.string.takeout)
            } else {
                getString(R.string.store)
            }

            val totalPrice = orderList.elements.sumOf { it.price * (it.count ?: 1).coerceAtLeast(1) }
            totalPriceTextView.text = String.format(
                getString(R.string.total_amount),
                numberFormat.format(totalPrice)
            )

            // 자동으로 10초뒤 이동
            val timer = Timer()
            timer.schedule(object : TimerTask(){
                override fun run() {
                    backToFullMenuFragment("none")
                }
            }, 10000)

            gotoMainButton.setOnClickListener {
                timer.cancel()
                backToFullMenuFragment("none")
            }
        }

        speakOut(
            resources.getString(R.string.order_completed) +
                    " " + resources.getString(R.string.order_number) + " $orderNumber"
        )

        AnalyticsLogger.logOrderCompleted(requireContext(), orderList.elements.size)

        // 주문 완료 카운트 증가 및 광고 로드
        adManager.incrementOrderCount()
        adManager.loadInterstitialAd()

        reviewApp()
        
        // 전면 광고 표시 (2번에 한번)
        adManager.showInterstitialAd {
            // 광고가 닫힌 후 추가 작업 있으면 여기에
        }
    }

    private fun reviewApp() {
        val manager = ReviewManagerFactory.create(requireContext())
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener {
                    if (task.isSuccessful){
                      Log.d("TAG" , "Review Success")

                  } else {
                      Log.d("TAG" , "Review Error")

                  }
                }

            }
        }

    }

}