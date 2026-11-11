package com.example.util

import android.content.Context
import com.example.data.model.Language

object SearchHistoryManager {

    private const val PREFS_NAME = "recycling_search_history_prefs"
    private const val KEY_HISTORY = "search_history_list"
    private const val MAX_HISTORY_ITEMS = 8

    private val defaultHistoryZh = listOf(
        "PET透明塑料瓶",
        "废旧纸箱瓦楞纸",
        "二手图书文具",
        "旧棉服与羊毛衫",
        "台式电脑小家电"
    )

    private val defaultHistoryEn = listOf(
        "PET Plastic Bottles",
        "Cardboard Shipping Boxes",
        "Used Books & Stationery",
        "Clothing & Apparel",
        "Small Household Electronics"
    )

    fun getSearchHistory(context: Context, lang: Language): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawSet = prefs.getStringSet(KEY_HISTORY, null)
        return if (rawSet.isNullOrEmpty()) {
            if (lang == Language.ZH) defaultHistoryZh else defaultHistoryEn
        } else {
            rawSet.toList().sorted()
        }
    }

    fun addSearchQuery(context: Context, query: String, lang: Language): List<String> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return getSearchHistory(context, lang)

        val currentHistory = getSearchHistory(context, lang).toMutableList()
        currentHistory.remove(trimmed)
        currentHistory.add(0, trimmed)

        val limited = currentHistory.take(MAX_HISTORY_ITEMS)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_HISTORY, limited.toSet()).apply()
        return limited
    }

    fun removeSearchQuery(context: Context, query: String, lang: Language): List<String> {
        val currentHistory = getSearchHistory(context, lang).toMutableList()
        currentHistory.remove(query)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_HISTORY, currentHistory.toSet()).apply()
        return currentHistory
    }

    fun clearSearchHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
        return emptyList()
    }

    fun getTrendingSearches(lang: Language): List<String> {
        return if (lang == Language.ZH) {
            listOf(
                "🔥 废旧家电上门",
                "🔥 快递泡沫瓦楞纸",
                "🔥 旧衣服爱心捐赠",
                "🔥 废旧锂电池处置",
                "🔥 红酒金属易拉罐",
                "🔥 实木废旧家具"
            )
        } else {
            listOf(
                "🔥 E-Waste Pickup",
                "🔥 Cardboard Packaging",
                "🔥 Clothes Donation",
                "🔥 Battery Disposal",
                "🔥 Aluminum Cans",
                "🔥 Wooden Furniture"
            )
        }
    }
}
