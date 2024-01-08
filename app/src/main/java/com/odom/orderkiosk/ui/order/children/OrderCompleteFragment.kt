package com.odom.orderkiosk.ui.order.children

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.odom.orderkiosk.R
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

            val random = (1000..9999).random()  // 1000 <= n <= 9999

            orderCompleteTextView.text = resources.getString(R.string.order_number) + ":  $random"
            orderCompleteTextView2.text = resources.getString(R.string.order_completed)

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

        speakOut(resources.getString(R.string.order_completed))

        reviewApp()
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