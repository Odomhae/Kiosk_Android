package com.odom.orderkiosk.ui.order.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odom.orderkiosk.databinding.FragmentOrderCompleteBinding
import java.util.Timer
import java.util.TimerTask

class OrderCompleteFragment : OrderChildrenBaseFragment() {
    private var _binding: FragmentOrderCompleteBinding? = null
    private val binding get() = _binding!!


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

        with(binding) {
            toolbar.setOnClickListener { parentFragmentManager.popBackStack() }

            orderCompleteTextView.text = "주문이 완료되었습니다."
            orderCompleteTextHelpView.text = "(10초 뒤 메인화면으로 자동 이동됩니다.)"

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

        speakOut("주문이 완료되었습니다.")
    }

}