package com.odom.orderkiosk.model

data class BotResponse(
    val success: Boolean,
    val fulfillmentText: String,
    val foods: List<Order>?,
    val ambiguousFoods: List<Order>?
) {
}