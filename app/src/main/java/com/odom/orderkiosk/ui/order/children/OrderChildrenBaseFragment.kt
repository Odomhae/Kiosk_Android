package com.odom.orderkiosk.ui.order.children

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Lifecycle
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.odom.orderkiosk.R
import com.odom.orderkiosk.model.BotResponse
import com.odom.orderkiosk.model.Food
import com.odom.orderkiosk.model.Order
import com.odom.orderkiosk.model.OrderList
import com.odom.orderkiosk.ui.order.OrderFragment
import com.odom.orderkiosk.ui.order.OrderFragment.Companion.KEY_ORDER_AMB_LIST
import com.odom.orderkiosk.ui.order.OrderFragment.Companion.KEY_ORDER_LIST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

open class OrderChildrenBaseFragment : Fragment(), FragmentResultListener {
    companion object {
        fun <T : OrderChildrenBaseFragment> getInstance(
            orderList: OrderList? = null,
            init: () -> T
        ): T {
            val fragment = init()
            if (orderList != null) {
                fragment.arguments = bundleOf(KEY_ORDER_LIST to orderList)
            }

            return fragment
        }
        fun <T : OrderChildrenBaseFragment> getInstance(
            orderList: OrderList? = null,
            ambiguousOrderList: OrderList? = null,
            init: () -> T
        ): T {
            val fragment = init()
            if (orderList != null) {
                fragment.arguments = bundleOf(KEY_ORDER_LIST to orderList)
            }
            if (ambiguousOrderList != null) {
                fragment.arguments = bundleOf(KEY_ORDER_AMB_LIST to ambiguousOrderList)
            }

            return fragment
        }
    }

    enum class IncompleteType {
        MainFoodOption,
        HamburgerSetSideMenu,
        HamburgerSetSideMenuOption,
        HamburgerSetBeverage,
        HamburgerSetBeverageOption,
        Count
    }

    private var _orderList: OrderList? = null
    protected lateinit var orderList: OrderList
    protected lateinit var ambiguousOrderList: OrderList

    protected lateinit var order: Order
    protected lateinit var type: IncompleteType
    protected lateinit var food: Food
    protected lateinit var options: HashMap<String, Long>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var tmpamb: OrderList? = null
        if (arguments != null) {
            _orderList = arguments?.getParcelable(KEY_ORDER_LIST)
            tmpamb = arguments?.getParcelable(KEY_ORDER_AMB_LIST)
        }

        if (savedInstanceState != null) {
            _orderList = savedInstanceState.getParcelable(KEY_ORDER_LIST)
            tmpamb = arguments?.getParcelable(KEY_ORDER_AMB_LIST)
        }
        if (tmpamb != null) {
            ambiguousOrderList = tmpamb!!.copy()
        }

        if (_orderList != null) {
            orderList = _orderList!!.copy()

            val orderAndType = getIncompleteOrderAndType(orderList) ?: return
            order = orderAndType.first
            type = orderAndType.second

            food = when (type) {
                IncompleteType.HamburgerSetSideMenuOption -> order.sideMenu!!
                IncompleteType.HamburgerSetBeverageOption -> order.beverage!!
//                else -> order.food
                else -> order.food!!
            }

            options = food.options

        } else {
            if (this is FullMenuFragment) {
                orderList = OrderList(arrayListOf(), false)
                ambiguousOrderList = OrderList(arrayListOf(), false)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_ORDER_LIST, _orderList)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            OrderFragment.REQUEST_RECOGNITION,
            viewLifecycleOwner,
            this
        )
    }

    protected fun speakOut(text: String) {
        parentFragmentManager.setFragmentResult(
            OrderFragment.REQUEST_SPEAK_OUT,
            bundleOf("text" to text)
        )
    }

    protected fun addMyMessage(text: String) {
        parentFragmentManager.setFragmentResult(
            OrderFragment.REQUEST_ADD_MY_MESSAGE,
            bundleOf("text" to text)
        )
    }
    // 파이어베이스 function의 detectIntent 호출하는 코드
    protected suspend fun sendMessageToBot(text: String) = withContext(Dispatchers.IO) {
        val progressView = parentFragment?.view?.findViewById<View>(R.id.progress_view)
        if (progressView?.isVisible == true) return@withContext null

        launch(Dispatchers.Main) {
            progressView?.isVisible = true
        }

        try {
            val result = FirebaseFunctions.getInstance().getHttpsCallable("detectIntent")
                .call(hashMapOf("question" to text))
                .await()

            launch(Dispatchers.Main) {
                progressView?.isVisible = false
            }

            return@withContext Gson().fromJson(result.data as String, BotResponse::class.java)

        } catch (e: Exception) {
            e.printStackTrace()
            launch(Dispatchers.Main) {
                progressView?.isVisible = false
            }

            return@withContext BotResponse(
                false,
                e.message ?: "Failed to get a bot response.",
                null,
                null
            )
        }
    }

    protected fun next(orderList: OrderList, backStackName: String? = null) {
        Log.d("SSS","next")
        if (orderList.elements.isEmpty()) return
        if (!isAdded) return

        // 옵션, 햄버거 세트 사이드 메뉴, 햄버거 세트 음료, 수량 순
        val type = getIncompleteOrderAndType(orderList)?.second

        if (type == null) {
            // TODO: 두번째 주문 진행
//            val prefs =  PreferenceUtil(requireContext())
//            val secondOrder = prefs.getSecondFood("SecondFood" , "")
//            if (secondOrder != "") {
//                lifecycleScope.launch {
//                    val response = sendMessageToBot(secondOrder) ?: return@launch
//
//                    if (response.foods == null) {
//                        speakOut(response.fulfillmentText)
//
//                    } else {
//                        val foods = ArrayList(response.foods)
//                        foods.forEach {
//                            if (it.option == null && it.food.options.size <= 1) {
//                                it.option = ""
//                            }
//                        }
//
//                        next(orderList.copy().apply {
//                            elements.addAll(foods)
//                        })
//                    }
//
//                    prefs.setSecondFood("SecondFood" , "")
//                }
//
//            } else {
//
//            }

            // 주문 완료
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.child_fragment_container,
                    getInstance(orderList, ::TakeOutFragment)
                )
                .addToBackStack(backStackName)
                .commit()

        } else {
            when (type) {
                IncompleteType.MainFoodOption,
                IncompleteType.HamburgerSetSideMenuOption,
                IncompleteType.HamburgerSetBeverageOption -> {
                    // 옵션 선택 안함
                    Log.d("SSS","next =  OptionFragment")
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.child_fragment_container,
                            getInstance(orderList, ::OptionFragment)
                        )
                        .addToBackStack(backStackName)
                        .commit()
                }

                IncompleteType.HamburgerSetSideMenu,
                IncompleteType.HamburgerSetBeverage -> {
                    // 햄버거 세트 사이드 메뉴 또는 음료 선택 안함
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.child_fragment_container,
                            getInstance(orderList, ::CategoryMenuFragment)
                        )
                        .addToBackStack(backStackName)
                        .commit()
                }

                else -> {
                    // 수량 선택 안함
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.child_fragment_container,
                            getInstance(orderList, ::CountFragment)
                        )
                        .addToBackStack(backStackName)
                        .commit()
                }
            }
        }
    }

    private fun getIncompleteOrderAndType(orderList: OrderList): Pair<Order, IncompleteType>? {
        orderList.elements.forEach {
//            if (it.food.options.size > 1 && it.option == null) {
            if (it.food?.options?.size!! > 1 && it.option == null) {
                return it to IncompleteType.MainFoodOption
            }

            if (it.food.type == Food.Type.HAMBURGER && it.option == "세트") {
                if (it.sideMenu == null) {
                    return it to IncompleteType.HamburgerSetSideMenu
                }

                if (it.sideMenuOption == null) {
                    return it to IncompleteType.HamburgerSetSideMenuOption
                }

                if (it.beverage == null) {
                    return it to IncompleteType.HamburgerSetBeverage
                }

                if (it.beverageOption == null) {
                    return it to IncompleteType.HamburgerSetBeverageOption
                }
            }

            if (it.count == null) {
                return it to IncompleteType.Count
            }
        }

        return null
    }

    protected fun goToIsRightFragment(message: String) {
//        val bundle = Bundle()
//        bundle.putString("message", message)
//        val recieverFragment = getInstance(orderList, ::IsrightFragment)
//        recieverFragment.arguments = bundle
//        parentFragmentManager.beginTransaction()
//            .replace(
//                R.id.child_fragment_container,
//                recieverFragment
//            )
//            .addToBackStack(null)
//            .commit() todo
    }

    protected fun goToOrderConfirmation(orderList: OrderList) {
        parentFragmentManager.beginTransaction()
//            .replace(
//                R.id.child_fragment_container,
//                getInstance(orderList, ::OrderConfirmationFragment)
//            ) todo
            .addToBackStack(null)
            .commit()
    }

    protected fun goToPaymentFragment(orderList: OrderList) {
        parentFragmentManager.beginTransaction()
//            .replace(
//                R.id.child_fragment_container,
//                getInstance(orderList, ::PaymentFragment) todo
//            )
            .addToBackStack(null)
            .commit()
    }
    // 로직: 신규 화면 "주문 완료 되었습니다" 만듦 - 기존 결제 후 주문 완료 창으로 이동되도록 처리
    protected fun goToOrderCompleteFragment(orderList: OrderList) {
        parentFragmentManager.beginTransaction()
//            .replace(
//                R.id.child_fragment_container,
//                getInstance(orderList, ::OrderCompleteFragment) // todo jihoon
//            )
            .addToBackStack(null)
            .commit()
    }

    // glacier : FullMenuFramgnet로 이동시에 argument 들고 이동하게 수정
    protected fun backToFullMenuFragment(mode: String) {
        val bundle = Bundle()
        bundle.putString("mode", mode)
        val recieverFragment = FullMenuFragment()
        recieverFragment.arguments = bundle

        with(parentFragmentManager) {
            popBackStack("main", 1)

            beginTransaction()
                .replace(R.id.child_fragment_container, recieverFragment)
                .addToBackStack("main")
                .commit()
        }
    }

    protected open fun onRecognized(message: String) {
        Log.d("OrderChildrenBaseFragment", message)
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        val message = result.getString("message")
        // ?: return

        Log.d("Lifecycle", lifecycle.currentState.toString())
        Log.d("Lifecycle", Lifecycle.State.RESUMED.toString())

//        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
        if (message != null) {
            onRecognized(message)
        }
//        }
    }
}