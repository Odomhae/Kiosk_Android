package com.odom.orderkiosk.ui.order.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                        "단품"
                    } else {
                        "${order.sideMenu!!.name} ${order.beverage!!.name}"
                    }
                }

                binding.contentContainer.addView(priceBinding.root, index)
            }

            // NOTICE: 개별 주문 상품 금액 = 현재는 1종이라 바로 대입이지만, 리스트에서 넣도록 하는 것이 안전함
            var totalPrice = 0L
            var count = orderList.elements.first().count!!
            val price = orderList.elements.sumOf { it.price }
            if (count < 1) {
                count = 1
            }

            // 두번째 메뉴 갯수
//            var count2 = 0
//            var price2 = 0L
//            if (orderList.elements.size > 1) {
//                count2 = orderList.elements[1].count!!
//                price2 = orderList.elements[1].price
//            }

            priceTextView.text = String.format(
                Locale.KOREA,
                // NOTICE: 수량 추가
                /*
                "%s원",
                NumberFormat.getInstance(Locale.KOREA).format(orderList.elements.sumOf { it.price })
                 */
                "%s원 x %s개 = %s원",
                NumberFormat.getInstance(Locale.KOREA).format(price),
                NumberFormat.getInstance(Locale.KOREA).format(count),
                NumberFormat.getInstance(Locale.KOREA).format(price * count)
            )

//            if (count2 > 0) {
//                priceTextView.text = String.format(
//                    Locale.KOREA,
//                    // NOTICE: 수량 추가
//                    /*
//                    "%s원",
//                    NumberFormat.getInstance(Locale.KOREA).format(orderList.elements.sumOf { it.price })
//                     */
//                    "%s원 x %s개 = %s원\n %s원 x %s개 = %s원",
//                    NumberFormat.getInstance(Locale.KOREA).format(price),
//                    NumberFormat.getInstance(Locale.KOREA).format(count),
//                    NumberFormat.getInstance(Locale.KOREA).format(price * count),
//                    NumberFormat.getInstance(Locale.KOREA).format(price2),
//                    NumberFormat.getInstance(Locale.KOREA).format(count2),
//                    NumberFormat.getInstance(Locale.KOREA).format(price2 * count2)
//                )
//
//            }

            totalPrice += price * count
            // totalPrice += price2 * count2
            // NOTICE: 총 주문금액 추가 (할인/쿠폰 등 제외)
            totalPriceTextView.text = "총 주문금액 : ${NumberFormat.getInstance(Locale.KOREA).format(totalPrice)}원"

            negativeButton.setOnClickListener {
                backToFullMenuFragment("none")
            }

            positiveButton.setOnClickListener {
                goToPaymentFragment(orderList)
            }
        }

        speakOut("주문하신 메뉴가 일치하면 '맞아' 아니면 '아니야' 라고 말씀해 주세요.")
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