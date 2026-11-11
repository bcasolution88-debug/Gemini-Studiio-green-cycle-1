package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DonationReceipt
import com.example.data.model.ForumPost
import com.example.data.model.OrderAgreement
import com.example.data.model.RecycleItem
import com.example.data.model.RecycleStation
import kotlinx.coroutines.flow.Flow

@Dao
interface RecycleDao {
    // Items
    @Query("SELECT * FROM recycle_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<RecycleItem>>

    @Query("SELECT * FROM recycle_items WHERE id = :id")
    suspend fun getItemById(id: String): RecycleItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: RecycleItem)

    @Update
    suspend fun updateItem(item: RecycleItem)

    @Query("DELETE FROM recycle_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    // Forum Posts
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    fun getAllForumPosts(): Flow<List<ForumPost>>

    @Query("SELECT * FROM forum_posts WHERE id = :id")
    suspend fun getForumPostById(id: String): ForumPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumPost(post: ForumPost)

    @Update
    suspend fun updateForumPost(post: ForumPost)

    // Recycle Stations
    @Query("SELECT * FROM recycle_stations ORDER BY distanceKm ASC")
    fun getAllStations(): Flow<List<RecycleStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: RecycleStation)

    // Orders
    @Query("SELECT * FROM recycle_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderAgreement>>

    @Query("SELECT * FROM recycle_orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): OrderAgreement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderAgreement)

    @Update
    suspend fun updateOrder(order: OrderAgreement)

    // Donation Receipts (Room Offline Caching)
    @Query("SELECT * FROM donation_receipts ORDER BY completedTimestamp DESC")
    fun getAllDonationReceipts(): Flow<List<DonationReceipt>>

    @Query("SELECT * FROM donation_receipts WHERE receiptId = :receiptId")
    suspend fun getDonationReceiptById(receiptId: String): DonationReceipt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonationReceipt(receipt: DonationReceipt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonationReceipts(receipts: List<DonationReceipt>)
}
