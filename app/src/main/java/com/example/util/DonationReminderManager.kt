package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.Language
import com.example.service.DonationReminderReceiver
import java.util.Calendar

data class DonationReminderSettings(
    val isEnabled: Boolean = true,
    val frequency: String = "MONTHLY", // MONTHLY, BIWEEKLY, QUARTERLY
    val dayOfMonth: Int = 1,
    val categoryFocus: String = "ALL", // ALL, CLOTHES, BOOKS, ELECTRONICS
    val lastScheduledTime: Long = System.currentTimeMillis()
) {
    fun frequencyLabel(lang: Language): String {
        return when (frequency) {
            "MONTHLY" -> if (lang == Language.ZH) "每月 1 日" else "Monthly (1st)"
            "BIWEEKLY" -> if (lang == Language.ZH) "每两周一次" else "Bi-Weekly"
            "QUARTERLY" -> if (lang == Language.ZH) "每季度首日" else "Quarterly"
            else -> frequency
        }
    }

    fun categoryLabel(lang: Language): String {
        return when (categoryFocus) {
            "ALL" -> if (lang == Language.ZH) "全品类储物清理" else "All Pantry Items"
            "CLOTHES" -> if (lang == Language.ZH) "闲置旧衣与纺织物" else "Apparel & Textiles"
            "BOOKS" -> if (lang == Language.ZH) "图书文具与玩具" else "Books & Toys"
            "ELECTRONICS" -> if (lang == Language.ZH) "二手电子与小家电" else "Electronics & Appliances"
            else -> categoryFocus
        }
    }
}

object DonationReminderManager {

    private const val PREFS_NAME = "donation_reminder_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_FREQUENCY = "frequency"
    private const val KEY_DAY = "day"
    private const val KEY_CATEGORY = "category"
    private const val CHANNEL_ID = "donation_reminders_channel"
    private const val CHANNEL_NAME = "Donation & Pantry Reminders"

    fun getSettings(context: Context): DonationReminderSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return DonationReminderSettings(
            isEnabled = prefs.getBoolean(KEY_ENABLED, true),
            frequency = prefs.getString(KEY_FREQUENCY, "MONTHLY") ?: "MONTHLY",
            dayOfMonth = prefs.getInt(KEY_DAY, 1),
            categoryFocus = prefs.getString(KEY_CATEGORY, "ALL") ?: "ALL"
        )
    }

    fun saveSettings(context: Context, settings: DonationReminderSettings) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, settings.isEnabled)
            .putString(KEY_FREQUENCY, settings.frequency)
            .putInt(KEY_DAY, settings.dayOfMonth)
            .putString(KEY_CATEGORY, settings.categoryFocus)
            .apply()

        if (settings.isEnabled) {
            scheduleAlarm(context, settings)
        } else {
            cancelAlarm(context)
        }
    }

    fun scheduleAlarm(context: Context, settings: DonationReminderSettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DonationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intervalMs = when (settings.frequency) {
            "BIWEEKLY" -> AlarmManager.INTERVAL_DAY * 14
            "QUARTERLY" -> AlarmManager.INTERVAL_DAY * 90
            else -> AlarmManager.INTERVAL_DAY * 30 // MONTHLY
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                intervalMs,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DonationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun sendDonationReminderNotification(
        context: Context,
        settings: DonationReminderSettings,
        isTest: Boolean = false,
        lang: Language = Language.ZH
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "DONATION")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (lang == Language.ZH) {
            if (isTest) "⏰ [测试通知] 闲置物品定期捐赠与清理提醒" else "⏰ 闲置物品清理提醒 · 捐赠献爱心"
        } else {
            if (isTest) "⏰ [Test] Recurring Donation & Cleanout Reminder" else "⏰ Time to Clean Pantry & Donate!"
        }

        val message = if (lang == Language.ZH) {
            "定期检查储物柜/箱! 您关注的【${settings.categoryLabel(Language.ZH)}】有可捐赠闲置吗？一键发起定向捐赠，赢绿碳积分！"
        } else {
            "Periodic Cleanout Check! Have items for [${settings.categoryLabel(Language.EN)}]? Donate now to earn green credits."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 10000 + 8000).toInt(), builder.build())

        val toastMsg = if (lang == Language.ZH) "🔔 提醒通知已发出！请查收系统状态栏消息" else "🔔 Notification sent! Check status bar"
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic reminders to check pantry or storage for charity donation items"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
