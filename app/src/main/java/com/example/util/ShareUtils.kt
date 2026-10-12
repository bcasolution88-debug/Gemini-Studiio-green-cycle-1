package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.Language

object ShareUtils {

    /**
     * Native Social Sharing for User Green Credits & Recycling Achievements.
     * Fires Intent.ACTION_SEND to allow users to share their achievements across WeChat, Weibo, WhatsApp, Telegram, Twitter, SMS, etc.
     */
    fun shareGreenCreditsAchievement(
        context: Context,
        displayName: String,
        points: Int,
        co2SavedKg: Double,
        completedRecycles: Int,
        lang: Language
    ) {
        val shareMessage = if (lang == Language.ZH) {
            """
            🌱【GreenCycle 绿碳回收环保成就】
            用户「$displayName」在平台累计践行旧物循环与绿色捐赠！
            
            ⭐ 累积绿碳积分: $points 积分
            🍃 减少碳排放量: $co2SavedKg kg CO₂
            ♻️ 成功交接回收: $completedRecycles 次
            
            每一件旧物的重新利用，都是对地球的一份关爱！快来与我一起加入绿碳回收行动吧！♻️✨
            """.trimIndent()
        } else {
            """
            🌱 [GreenCycle Eco Achievement]
            User "$displayName" is making a difference through sustainable recycling & charity donation!
            
            ⭐ Green Credits: $points pts
            🍃 CO₂ Emissions Saved: $co2SavedKg kg
            ♻️ Completed Recycles: $completedRecycles
            
            Join me in recycling idle items and protecting our planet! ♻️✨
            """.trimIndent()
        }

        val shareTitle = if (lang == Language.ZH) "分享我的绿碳环保成就" else "Share Green Achievement"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            putExtra(Intent.EXTRA_TITLE, shareTitle)
            type = "text/plain"
        }

        try {
            val chooserIntent = Intent.createChooser(sendIntent, shareTitle)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (lang == Language.ZH) "无法启动分享功能，请确认系统已安装社交应用" else "Unable to launch social share",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Native Social Sharing for a specific Recycle / Donation Item Listing.
     */
    fun shareRecycleItem(
        context: Context,
        title: String,
        condition: String,
        priceOrDonation: String,
        location: String,
        lang: Language
    ) {
        val shareText = if (lang == Language.ZH) {
            """
            ♻️【GreenCycle 待回收旧物推荐】
            物品名称: $title
            成色新旧: $condition
            回收/评估: $priceOrDonation
            交接地点: $location
            
            发现好物不浪费，支持附近旧物循环与爱心传递！详细了解或预约请打开 GreenCycle 绿碳回收应用。
            """.trimIndent()
        } else {
            """
            ♻️ [GreenCycle Item Listing]
            Title: $title
            Condition: $condition
            Type: $priceOrDonation
            Location: $location
            
            Check out this item for green recycling and donation!
            """.trimIndent()
        }

        val chooserTitle = if (lang == Language.ZH) "分享此待回收旧物" else "Share Item Listing"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        try {
            context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, "Share error", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Native Social Sharing for Community Forum Posts and Green Recycling Tips.
     */
    fun shareForumPost(
        context: Context,
        title: String,
        authorName: String,
        content: String,
        lang: Language
    ) {
        val shareText = if (lang == Language.ZH) {
            """
            📢【GreenCycle 绿碳社区精选动态】
            标题: $title
            发布者: $authorName
            
            摘要:
            $content
            
            ♻️ 传递旧物回收知识，共建绿色零废弃社区！来自 GreenCycle 绿碳回收。
            """.trimIndent()
        } else {
            """
            📢 [GreenCycle Community Forum Post]
            Title: $title
            Author: $authorName
            
            Content:
            $content
            
            ♻️ Join the sustainable lifestyle conversation on GreenCycle!
            """.trimIndent()
        }

        val chooserTitle = if (lang == Language.ZH) "分享到社交平台" else "Share Community Post"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, title)
            type = "text/plain"
        }

        try {
            context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (lang == Language.ZH) "无法启动分享功能" else "Unable to share post",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Native Social Sharing for Recycling & Upcycling Dismantling Tips/Tutorials.
     */
    fun shareRecyclingTip(
        context: Context,
        title: String,
        authorName: String,
        steps: List<String>,
        lang: Language
    ) {
        val stepsFormatted = steps.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n")
        val shareText = if (lang == Language.ZH) {
            """
            💡【GreenCycle 实用旧物拆解与循环妙招】
            《$title》
            指导作者: $authorName
            
            关键步骤:
            $stepsFormatted
            
            ♻️ 掌握旧物无害化拆解与改造技巧，变废为宝！更多环保教学尽在 GreenCycle 绿碳回收。
            """.trimIndent()
        } else {
            """
            💡 [GreenCycle Recycling & Upcycling Tip]
            "$title"
            By: $authorName
            
            Key Steps:
            $stepsFormatted
            
            ♻️ Learn how to upcycle and recycle smartly with GreenCycle!
            """.trimIndent()
        }

        val chooserTitle = if (lang == Language.ZH) "分享旧物拆解指南" else "Share Recycling Tip"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        try {
            context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, "Share error", Toast.LENGTH_SHORT).show()
        }
    }
}
