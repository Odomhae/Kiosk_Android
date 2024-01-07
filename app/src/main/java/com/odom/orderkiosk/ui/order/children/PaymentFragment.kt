package com.odom.orderkiosk.ui.order.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odom.orderkiosk.R
import com.odom.orderkiosk.databinding.FragmentPaymentBinding
import java.text.NumberFormat
import java.util.Locale

class PaymentFragment : OrderChildrenBaseFragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setOnClickListener { parentFragmentManager.popBackStack() }

            var totalPrice = 0L
            var count = orderList.elements.first().count!!
            val price = orderList.elements.sumOf { it.price }
            if (count < 1) {
                count = 1
            }
//            var count2 = 0
//            var price2 = 0L
//            if (orderList.elements.size > 1) {
//                count2 = orderList.elements[1].count!!
//                price2 = orderList.elements[1].price
//            }

            totalPrice += price * count
            //     totalPrice += price2 * count2
            priceTextView.text = String.format(getString(R.string.total_amount), NumberFormat.getInstance(Locale.KOREA).format(totalPrice))

            option1Container.setOnClickListener {
                //TODO: 카드 결제

//                backToFullMenuFragment()
                goToOrderCompleteFragment(orderList)
            }

            option2Container.setOnClickListener {
                //TODO: 쿠폰 결제

//                backToFullMenuFragment()
                goToOrderCompleteFragment(orderList)
            }
        }
    }

    override fun onRecognized(message: String) {
//        super.onRecognized(message)
    }
}