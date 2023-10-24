package com.odom.orderkiosk.ui.order.children

import android.icu.text.NumberFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.odom.orderkiosk.databinding.FragmentFullMenuBinding
import com.odom.orderkiosk.databinding.ItemFoodBinding
import com.odom.orderkiosk.model.Food
import java.util.Locale

class FullMenuFragment : OrderChildrenBaseFragment() {
    private var _binding: FragmentFullMenuBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val adapter by lazy { ViewPagerAdapter() }
    private var copiedFoods : List<Food>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFullMenuBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }


    private class ViewPagerAdapter :
        ListAdapter<Pair<Food.Type, List<Food>>, RecyclerView.ViewHolder>(diffUtil) {
        var onItemClickListener: ((Food) -> Unit)? = null

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            val recyclerView = RecyclerView(parent.context).apply {
                layoutManager =
                    GridLayoutManager(parent.context, 3, LinearLayoutManager.VERTICAL, false)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val padding = (parent.context.resources.displayMetrics.density * 8).toInt()
                setPadding(padding, padding * 2, padding, 0)
            }

            return object : RecyclerView.ViewHolder(recyclerView) {
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val adapter = FoodListAdapter().apply {
                onItemClickListener = this@ViewPagerAdapter.onItemClickListener
            }

            adapter.submitList(getItem(position).second)
            (holder.itemView as RecyclerView).adapter = adapter
        }

        companion object {
            val diffUtil = object : DiffUtil.ItemCallback<Pair<Food.Type, List<Food>>>() {
                override fun areItemsTheSame(
                    oldItem: Pair<Food.Type, List<Food>>,
                    newItem: Pair<Food.Type, List<Food>>
                ): Boolean {
                    return oldItem.first == newItem.first
                }

                override fun areContentsTheSame(
                    oldItem: Pair<Food.Type, List<Food>>,
                    newItem: Pair<Food.Type, List<Food>>
                ): Boolean {
                    if (oldItem.second.size != newItem.second.size) {
                        return false
                    }

                    oldItem.second.zip(newItem.second).forEach {
                        if (it.first.documentId != it.second.documentId) {
                            return false
                        }
                    }

                    return true
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
                                    "%s원",
                                    NumberFormat.getInstance(Locale.KOREA).format(price)
                                )
                            } else {
                                String.format(
                                    Locale.KOREA,
                                    "%s: %s원",
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


}