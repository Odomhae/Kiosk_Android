package com.odom.orderkiosk.ui.order.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.odom.orderkiosk.R
import com.odom.orderkiosk.databinding.FragmentCategoryMenuBinding
import com.odom.orderkiosk.databinding.ItemFoodBinding
import com.odom.orderkiosk.model.Food
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

//region Food list fragment
class CategoryMenuFragment : OrderChildrenBaseFragment() {
    private var _binding: FragmentCategoryMenuBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val adapter by lazy { FoodListAdapter() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryMenuBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setOnClickListener { requireActivity().onBackPressed() }
            toolbar.title = when (type) {
                IncompleteType.HamburgerSetSideMenu ->
                    "${food.name} " + getString(R.string.menu_side)

                IncompleteType.HamburgerSetBeverage ->
                    "${food.name} " + getString(R.string.menu_beverage)

                else -> ""
            }

            recyclerView.adapter = adapter.apply {
                onItemClickListener = {
                    onSelectedFood(it)
                }
            }
        }

        lifecycleScope.launch {
            db.collection(getString(R.string.db_name))
                .whereEqualTo(
                    "type",
                    if (type == IncompleteType.HamburgerSetSideMenu) Food.Type.SIDE_MENU.ordinal else Food.Type.BEVERAGE.ordinal
                )
                .snapshots()
                .collectLatest {
                    val foods = it.documents.mapNotNull { it.toObject(Food::class.java) }

                    adapter.submitList(foods)
                }
        }

        if (type == IncompleteType.HamburgerSetSideMenu) {
            speakOut("${food.name} ${order.option} " + getString(R.string.ask_side_menu))
        } else if (type == IncompleteType.HamburgerSetBeverage) {
            speakOut("${food.name} ${order.option} " + getString(R.string.ask_beverage))
        }
    }

    private fun onSelectedFood(food: Food) {
        if (type == IncompleteType.HamburgerSetSideMenu) {
            order.sideMenu = food
            if (food.options.size <= 1) {
                order.sideMenuOption = ""
            }

            next(orderList)

        } else if (type == IncompleteType.HamburgerSetBeverage) {
            order.beverage = food
            if (food.options.size <= 1) {
                order.beverageOption = ""
            }

            next(orderList)
        }
    }

    override fun onRecognized(message: String) {
//        super.onRecognized(message)

        lifecycleScope.launch {
            val response = sendMessageToBot(message) ?: return@launch

            if (response.foods == null) {
                speakOut(response.fulfillmentText)

            } else {
                val food = when (type) {
                    IncompleteType.HamburgerSetSideMenu ->
//                        response.foods.firstOrNull { it.food.type == Food.Type.SIDE_MENU }?.food
                    response.foods.firstOrNull { it.food?.type == Food.Type.SIDE_MENU }?.food

                    IncompleteType.HamburgerSetBeverage ->
//                        response.foods.firstOrNull { it.food.type == Food.Type.BEVERAGE }?.food
                    response.foods.firstOrNull { it.food?.type == Food.Type.BEVERAGE }?.food

                    else -> return@launch
                }

                if (food == null) {
                    speakOut("판매하는 상품이 아닙니다. 다시 주문해 주세요.")
                    return@launch
                }

                onSelectedFood(food)
            }
        }
    }

    //region Food list adapter
    private class FoodListAdapter :
        ListAdapter<Food, FoodListAdapter.FoodItemViewHolder>(diffUtil) {

        var onItemClickListener: ((Food) -> Unit)? = null

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FoodItemViewHolder {
            return FoodItemViewHolder(
                ItemFoodBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
            val item = getItem(position)
            with(holder.binding) {
                Glide.with(imageView)
                    .load(item.image)
                    .into(imageView)

                nameTextView.text = item.name

                item.options.toList().zip(listOf(option1TextView, option2TextView))
                    .forEach {
                        val textView = it.second
                        val option = it.first.first
                        val price = it.first.second

                        textView.text = if (item.options.size == 1) {
                            String.format(
                                Locale.KOREA,
                                holder.itemView.context.getString(R.string.price_format1),
                                NumberFormat.getInstance(Locale.KOREA).format(price)
                            )
                        } else {
                            String.format(
                                Locale.KOREA,
                                holder.itemView.context.getString(R.string.price_format3),
                                option,
                                NumberFormat.getInstance(Locale.KOREA).format(price)
                            )
                        }
                    }

                imageContainer.setOnClickListener {
                    onItemClickListener?.invoke(item)
                }
            }
        }

        companion object {
            val diffUtil = object : DiffUtil.ItemCallback<Food>() {
                override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
                    return oldItem.documentId == newItem.documentId
                }

                override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
                    return oldItem.name == newItem.name &&
                            oldItem.options.size == newItem.options.size &&
                            oldItem.options.toList().zip(newItem.options.toList())
                                .none { it.first.first != it.second.first || it.first.second != it.second.second }
                }
            }
        }

        class FoodItemViewHolder(val binding: ItemFoodBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
    //endregion
}
//endregion
