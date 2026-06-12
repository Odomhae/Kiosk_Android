package com.odom.orderkiosk.ui.order.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odom.orderkiosk.R
import com.odom.orderkiosk.databinding.FragmentOrderConfirmationBinding
import com.odom.orderkiosk.databinding.ItemPriceBinding
import java.text.NumberFormat
import java.util.Locale

class OrderConfirmationFragment : OrderChildrenBaseFragment() {
    private var _binding: FragmentOrderConfirmationBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setOnClickListener { parentFragmentManager.popBackStack() }

            orderList.elements.forEachIndexed { index, order ->
                val priceBinding = ItemPriceBinding.inflate(
                    LayoutInflater.from(view.context),
                    binding.contentContainer,
                    false
                )

                with(priceBinding) {
//                    foodNameTextView.text = order.food.name
                    foodNameTextView.text = order.food?.name
                    optionTextView.text = if (order.sideMenu == null) {
                        getString(R.string.menu_single)
                    } else {
                        "${order.sideMenu!!.name} ${order.beverage!!.name}"
                    }
                }

                binding.contentContainer.addView(priceBinding.root, index)
            }

            // 항목별 (단가 x 수량) 한 줄씩 표시, 총액은 각 항목 금액의 합
            val numberFormat = NumberFormat.getInstance(Locale.KOREA)

            priceTextView.text = orderList.elements.joinToString("\n") { order ->
                val count = (order.count ?: 1).coerceAtLeast(1)
                String.format(
                    Locale.KOREA,
                    getString(R.string.price_format2),
                    numberFormat.format(order.price),
                    numberFormat.format(count),
                    numberFormat.format(order.price * count)
                )
            }

            // NOTICE: 총 주문금액 (할인/쿠폰 등 제외)
            val totalPrice = orderList.elements.sumOf { it.price * (it.count ?: 1).coerceAtLeast(1) }
            totalPriceTextView.text = String.format(getString(R.string.total_amount), numberFormat.format(totalPrice))

            negativeButton.setOnClickListener {
                backToFullMenuFragment("none")
            }

            positiveButton.setOnClickListener {
                goToPaymentFragment(orderList)
            }
        }

        speakOut(resources.getString(R.string.ask_confirmation))
    }

    override fun onRecognized(message: String) {
//        super.onRecognized(message)

        if (message.contains("맞아") ||
            message.contains("예")
        ) {
            goToPaymentFragment(orderList)

        } else if (
            message.contains("아니야") ||
            message.contains("아니오")
        ) {
            backToFullMenuFragment("none")
        }
    }
}