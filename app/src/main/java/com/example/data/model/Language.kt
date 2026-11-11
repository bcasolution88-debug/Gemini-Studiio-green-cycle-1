package com.example.data.model

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Language {
    ZH, EN
}

class LanguageManager(private val context: Context? = null) {
    private val prefs = context?.getSharedPreferences("app_locale_prefs", Context.MODE_PRIVATE)

    var currentLanguage by mutableStateOf(loadSavedLanguage())
        private set

    private fun loadSavedLanguage(): Language {
        val saved = prefs?.getString("selected_language", null)
        return if (saved != null) {
            try {
                Language.valueOf(saved)
            } catch (e: Exception) {
                Language.ZH
            }
        } else {
            Language.ZH
        }
    }

    fun toggleLanguage() {
        val nextLang = if (currentLanguage == Language.ZH) Language.EN else Language.ZH
        setLanguage(nextLang)
    }

    fun setLanguage(lang: Language) {
        currentLanguage = lang
        prefs?.edit()?.putString("selected_language", lang.name)?.apply()
    }
}

val LocalLanguageManager = compositionLocalOf { LanguageManager() }

object AppStrings {
    fun appName(lang: Language) = if (lang == Language.ZH) "绿循环" else "Green Cycle"
    fun slogan(lang: Language) = if (lang == Language.ZH) "让旧物焕发新生·共建绿色地球" else "Give Old Items New Life · Build a Green Earth"
    
    // Bottom Nav
    fun navHome(lang: Language) = if (lang == Language.ZH) "首页" else "Home"
    fun navCategory(lang: Language) = if (lang == Language.ZH) "分类" else "Category"
    fun navForum(lang: Language) = if (lang == Language.ZH) "论坛" else "Forum"
    fun navDonation(lang: Language) = if (lang == Language.ZH) "捐赠" else "Donation"
    fun navSupply(lang: Language) = if (lang == Language.ZH) "循环需求" else "Loop Demand"
    fun navStation(lang: Language) = if (lang == Language.ZH) "回收站点" else "Stations"
    fun navOrders(lang: Language) = if (lang == Language.ZH) "订单" else "Orders"
    fun navProfile(lang: Language) = if (lang == Language.ZH) "我的" else "Profile"
    fun navAdmin(lang: Language) = if (lang == Language.ZH) "管理后台" else "Admin Console"

    // General Actions
    fun searchPlaceholder(lang: Language) = if (lang == Language.ZH) "搜索旧物、废品、回收站点..." else "Search items, waste, stations..."
    fun publishBtn(lang: Language) = if (lang == Language.ZH) "发布待回收" else "Publish Item"
    fun applyBtn(lang: Language) = if (lang == Language.ZH) "申请领取" else "Apply to Claim"
    fun contactRecipient(lang: Language) = if (lang == Language.ZH) "联系回收员" else "Contact Collector"
    fun signAgreement(lang: Language) = if (lang == Language.ZH) "查看电子协议" else "View Agreement"
    fun stationMap(lang: Language) = if (lang == Language.ZH) "地图模式" else "Map View"
    fun stationList(lang: Language) = if (lang == Language.ZH) "列表模式" else "List View"
    fun darkMode(lang: Language) = if (lang == Language.ZH) "夜间模式" else "Dark Mode"
    fun lightMode(lang: Language) = if (lang == Language.ZH) "白天模式" else "Light Mode"
    
    // Statuses
    fun statusPending(lang: Language) = if (lang == Language.ZH) "待审核" else "Pending Audit"
    fun statusMatching(lang: Language) = if (lang == Language.ZH) "待对接" else "Awaiting Match"
    fun statusScheduled(lang: Language) = if (lang == Language.ZH) "已安排" else "Scheduled"
    fun statusCompleted(lang: Language) = if (lang == Language.ZH) "已完成" else "Completed"
    fun statusCancelled(lang: Language) = if (lang == Language.ZH) "已取消" else "Cancelled"
}
