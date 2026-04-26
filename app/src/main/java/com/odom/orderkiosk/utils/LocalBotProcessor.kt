package com.odom.orderkiosk.utils

import com.odom.orderkiosk.model.BotResponse
import com.odom.orderkiosk.model.Food
import com.odom.orderkiosk.model.Order

class LocalBotProcessor {
    
    fun processUserInput(input: String, allFoods: List<Food>): BotResponse {
        val normalizedInput = input.lowercase().trim()
        
        // 음식 이름 매칭
        val matchedFood = findFoodByName(normalizedInput, allFoods)
        
        return if (matchedFood != null) {
            BotResponse(
                success = true,
                fulfillmentText = "${matchedFood.name}를 선택하셨습니다.",
                foods = listOf(Order(matchedFood, null)),
                ambiguousFoods = null
            )
        } else {
            // 간단한 응답 처리
            when {
                normalizedInput.contains("아니") || normalizedInput.contains("아니요") -> {
                    BotResponse(
                        success = true,
                        fulfillmentText = "처음으로 돌아갑니다.",
                        foods = null,
                        ambiguousFoods = null
                    )
                }
                normalizedInput.contains("네") || normalizedInput.contains("예") -> {
                    BotResponse(
                        success = true,
                        fulfillmentText = "확인했습니다.",
                        foods = null,
                        ambiguousFoods = null
                    )
                }
                else -> {
                    BotResponse(
                        success = false,
                        fulfillmentText = "죄송합니다. 다시 말씀해주시겠어요?",
                        foods = null,
                        ambiguousFoods = null
                    )
                }
            }
        }
    }
    
    private fun findFoodByName(input: String, foods: List<Food>): Food? {
        return foods.find { food ->
            food.name.lowercase().contains(input) || 
            input.contains(food.name.lowercase()) ||
            checkPartialMatch(input, food.name)
        }
    }
    
    private fun checkPartialMatch(input: String, foodName: String): Boolean {
        val foodWords = foodName.lowercase().split(" ", "-", "_")
        return foodWords.any { word ->
            input.contains(word) && word.length >= 2
        }
    }
}
