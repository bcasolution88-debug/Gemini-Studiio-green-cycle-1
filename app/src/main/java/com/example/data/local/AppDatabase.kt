package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DonationReceipt
import com.example.data.model.ForumPost
import com.example.data.model.OrderAgreement
import com.example.data.model.RecycleItem
import com.example.data.model.RecycleStation

@Database(
    entities = [
        RecycleItem::class,
        ForumPost::class,
        RecycleStation::class,
        OrderAgreement::class,
        DonationReceipt::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recycleDao(): RecycleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "green_cycle_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
