package com.odom.orderkiosk.ui.order.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.odom.orderkiosk.R
import com.odom.orderkiosk.databinding.FragmentCountBinding

//region Option fragment
class CountFragment : OrderChildrenBaseFragment() {
    private var _binding: FragmentCountBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setOnClickListener { parentFragmentManager.popBackStack() }
            toolbar.title = food.name + " " + getString(R.string.select_quantity)

            minusButton.setOnClickListener {
                var count = countTextView.text.toString().removeSuffix("개").toInt()
                if (count > 1) {
                    count -= 1
                }

                countTextView.text = "$count"
            }
            // 10개 미만만 주문되도록 하였고 10개 이상은 주문이 안되도록 함 -
            plusButton.setOnClickListener {
                var count = countTextView.text.toString().removeSuffix("개").toInt()

                if(count + 1 >= 10) {
                    Toast.makeText(binding.root.context, "10개 미만으로 입력해주세요.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener;
                }
                count += 1

                countTextView.text = "$count"
            }

            nextButton.setOnClickListener {
                val count = countTextView.text.toString().removeSuffix("개").toInt()
                onSelectedCount(count)
            }
        }

        speakOut(food.name  + " " + getString(R.string.select_quantity))
    }

    private fun onSelectedCount(count: Int) {
        order.count = count
        next(orderList)
    }

    override fun onRecognized(message: String) {
//        super.onRecognized(message)

        val count = message.replace("[^0-9]", "").toIntOrNull()
        if (count != null) {
            onSelectedCount(count)
        } else {
            val message = message.replace(" ", "")

            if (message.startsWith("한개") || message.startsWith("하나")) {
                onSelectedCount(1)
            } else if (message.startsWith("두개") || message.startsWith("둘")) {
                onSelectedCount(2)
            } else if (message.startsWith("세개") || message.startsWith("셋")) {
                onSelectedCount(3)
            } else if (message.startsWith("네개") || message.startsWith("넷")) {
                onSelectedCount(4)
            } else if (message.startsWith("다섯")) {
                onSelectedCount(5)
            } else if (message.startsWith("여섯")) {
                onSelectedCount(6)
            } else if (message.startsWith("일곱")) {
                onSelectedCount(7)
            } else if (message.startsWith("여덟") || message.startsWith("여덜")) {
                onSelectedCount(8)
            } else if (message.startsWith("아홉")) {
                onSelectedCount(9)
            } else {
                speakOut("다시 말씀해 주세요.")
            }
        }
    }
}
//endregion
