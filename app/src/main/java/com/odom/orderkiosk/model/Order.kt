package com.odom.orderkiosk.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val food: Food,
    var option: String? = null,
    var count: Int? = null,

    @Expose(serialize = false, deserialize = false)
    var sideMenu: Food? = null,

    @Expose(serialize = false, deserialize = false)
    var sideMenuOption: String? = null,

    @Expose(serialize = false, deserialize = false)
    var beverage: Food? = null,

    @Expose(serialize = false, deserialize = false)
    var beverageOption: String? = null,

    @Expose(serialize = false, deserialize = false)
    var isTakeOut: Boolean = false,
) : Parcelable {
    val price: Long
//        get() = food.options[option]!! +
          get() = (food.options.get(option) ?: food.options.get(food.options.keys.first()) ?: 0) +
                (sideMenu?.options?.get(sideMenuOption) ?: sideMenu?.options?.values?.first() ?: 0) +
                (beverage?.options?.get(beverageOption) ?: beverage?.options?.values?.first() ?: 0)
}
