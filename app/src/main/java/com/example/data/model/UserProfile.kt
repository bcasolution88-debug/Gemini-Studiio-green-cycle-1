package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String = "usr_default_88",
    val displayName: String = "环保践行者 (绿级会员)",
    val email: String = "green_user_88@greencycle.com",
    val phone: String = "13800138001",
    val avatarUrl: String = "",
    val recyclePoints: Int = 320,
    val co2SavedKg: Double = 18.5,
    val completedRecycles: Int = 5,
    val greenLevel: String = "GREEN_V2",
    val role: String = "USER", // USER, STATION_ADMIN, RECYCLER_SPECIALIST
    val defaultAddress: String = "北京市朝阳区三里屯街道12号院",
    val createdAt: Long = System.currentTimeMillis()
)
