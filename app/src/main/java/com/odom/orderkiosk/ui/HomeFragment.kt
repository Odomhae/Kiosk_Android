package com.odom.orderkiosk.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odom.orderkiosk.BaseFragment
import com.odom.orderkiosk.R
import com.odom.orderkiosk.databinding.FragmentHomeBinding
import com.odom.orderkiosk.ui.order.OrderFragment

class HomeFragment : BaseFragment(){
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            orderButton.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, OrderFragment())
                    .addToBackStack(null)
                    .commit()
            }

            speakOut(binding.descriptionTextView.text.toString())
        }
    }

    override fun onRecognized(results: List<String>) {
        super.onRecognized(results)
        //주문이라는 키워드가 입력되면 자동으로 넘어감
        if (results.any { it.contains("주문") }) {
//            binding.orderButton.performClick()
            if (results.any { it.contains("주문안") || it.contains("안")
                        || it.contains("주문않") || it.contains("않")
                        || it.contains("주문아날") || it.contains("아날")
                        || it.contains("주문아내") || it.contains("아내")
                        || it.contains("주문아네") || it.contains("아네") }) {
                // 주문 안~, 이중 부정은 포함 X
                // 주문 못~, 은 포함 X - 말이 안되므로
                Log.d("onRecognized", "USER_BLOCKED_ORDER - 주문 안함 [" + results.toString() + "]")
            } else {
                binding.orderButton.performClick()
            }
        }
    }
}