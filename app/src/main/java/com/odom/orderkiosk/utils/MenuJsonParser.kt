package com.odom.orderkiosk.utils

import android.content.Context
import android.content.res.Configuration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.odom.orderkiosk.model.Food
import java.io.IOException
import java.util.Locale

class MenuJsonParser(private val context: Context) {
    
    private val gson = Gson()
    
    fun getMenuData(): List<Food> {
        return try {
            val resourceId = if (isKoreanLocale()) {
                com.odom.orderkiosk.R.raw.menu_data
            } else {
                com.odom.orderkiosk.R.raw.menu_data_en
            }
            
            val jsonString = context.resources.openRawResource(resourceId)
                .bufferedReader().use { it.readText() }
            
            val listType = object : TypeToken<List<Food>>() {}.type
            gson.fromJson(jsonString, listType) ?: emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }
    
    fun getFoodsByType(type: Food.Type): List<Food> {
        return getMenuData().filter { it.type == type }
    }
    
    private fun isKoreanLocale(): Boolean {
        val locale = context.resources.configuration.locales[0]
        return locale.language == "ko"
    }
}
