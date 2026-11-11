package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.DonationReminderManager

class DonationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val settings = DonationReminderManager.getSettings(context)
        if (settings.isEnabled) {
            DonationReminderManager.sendDonationReminderNotification(context, settings, isTest = false)
        }
    }
}
