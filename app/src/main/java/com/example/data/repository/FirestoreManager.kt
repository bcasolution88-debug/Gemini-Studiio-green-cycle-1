package com.example.data.repository

import com.example.data.model.RecycleItem
import com.example.data.model.RecycleStation
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {

    private fun getDb(): FirebaseFirestore? {
        return runCatching {
            FirebaseFirestore.getInstance()
        }.getOrNull()
    }

    const val COLLECTION_ITEMS = "recycle_items"
    const val COLLECTION_STATIONS = "recycle_stations"
    const val COLLECTION_USER_PROFILES = "user_profiles"

    /**
     * Save or update an item post in Firestore.
     */
    fun saveItemPost(item: RecycleItem, onComplete: ((Boolean) -> Unit)? = null) {
        val db = getDb() ?: run {
            onComplete?.invoke(false)
            return
        }

        val itemMap = hashMapOf(
            "id" to item.id,
            "title" to item.title,
            "majorCategoryId" to item.majorCategoryId,
            "subCategoryId" to item.subCategoryId,
            "condition" to item.condition,
            "description" to item.description,
            "expectedPrice" to item.expectedPrice,
            "isDonation" to item.isDonation,
            "donorTarget" to (item.donorTarget ?: ""),
            "imageListJson" to item.imageListJson,
            "videoUrl" to (item.videoUrl ?: ""),
            "location" to item.location,
            "latitude" to item.latitude,
            "longitude" to item.longitude,
            "contactPhone" to item.contactPhone,
            "contactWechat" to (item.contactWechat ?: ""),
            "status" to item.status,
            "assignedRecipientName" to (item.assignedRecipientName ?: ""),
            "assignedStationName" to (item.assignedStationName ?: ""),
            "evaluation" to (item.evaluation ?: ""),
            "createdAt" to item.createdAt
        )

        db.collection(COLLECTION_ITEMS)
            .document(item.id)
            .set(itemMap)
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    /**
     * Fetch item posts from Firestore.
     */
    fun fetchItemPosts(onResult: (List<RecycleItem>) -> Unit) {
        val db = getDb() ?: run {
            onResult(emptyList())
            return
        }

        db.collection(COLLECTION_ITEMS)
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        RecycleItem(
                            id = doc.getString("id") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            majorCategoryId = doc.getString("majorCategoryId") ?: "cat_home",
                            subCategoryId = doc.getString("subCategoryId") ?: "sub_furniture",
                            condition = doc.getString("condition") ?: "90新",
                            description = doc.getString("description") ?: "",
                            expectedPrice = doc.getDouble("expectedPrice") ?: 0.0,
                            isDonation = doc.getBoolean("isDonation") ?: false,
                            donorTarget = doc.getString("donorTarget")?.ifBlank { null },
                            imageListJson = doc.getString("imageListJson") ?: "",
                            videoUrl = doc.getString("videoUrl")?.ifBlank { null },
                            location = doc.getString("location") ?: "",
                            latitude = doc.getDouble("latitude") ?: 39.9042,
                            longitude = doc.getDouble("longitude") ?: 116.4074,
                            contactPhone = doc.getString("contactPhone") ?: "",
                            contactWechat = doc.getString("contactWechat")?.ifBlank { null },
                            status = doc.getString("status") ?: "PENDING",
                            assignedRecipientName = doc.getString("assignedRecipientName")?.ifBlank { null },
                            assignedStationName = doc.getString("assignedStationName")?.ifBlank { null },
                            evaluation = doc.getString("evaluation")?.ifBlank { null },
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    }.getOrNull()
                }
                onResult(items)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * Save or update a recycle station location in Firestore.
     */
    fun saveStationLocation(station: RecycleStation, onComplete: ((Boolean) -> Unit)? = null) {
        val db = getDb() ?: run {
            onComplete?.invoke(false)
            return
        }

        val stationMap = hashMapOf(
            "id" to station.id,
            "name" to station.name,
            "rating" to station.rating.toDouble(),
            "address" to station.address,
            "latitude" to station.latitude,
            "longitude" to station.longitude,
            "phone" to station.phone,
            "wechat" to (station.wechat ?: ""),
            "businessHours" to station.businessHours,
            "tagsJson" to station.tagsJson,
            "photoUrl" to station.photoUrl,
            "serviceCategoriesJson" to station.serviceCategoriesJson,
            "priceListJson" to station.priceListJson,
            "isVerified" to station.isVerified,
            "distanceKm" to station.distanceKm
        )

        db.collection(COLLECTION_STATIONS)
            .document(station.id)
            .set(stationMap)
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    /**
     * Fetch recycle station locations from Firestore.
     */
    fun fetchStationLocations(onResult: (List<RecycleStation>) -> Unit) {
        val db = getDb() ?: run {
            onResult(emptyList())
            return
        }

        db.collection(COLLECTION_STATIONS)
            .get()
            .addOnSuccessListener { snapshot ->
                val stations = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        RecycleStation(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            rating = (doc.getDouble("rating") ?: 4.8).toFloat(),
                            address = doc.getString("address") ?: "",
                            latitude = doc.getDouble("latitude") ?: 39.9080,
                            longitude = doc.getDouble("longitude") ?: 116.4800,
                            phone = doc.getString("phone") ?: "",
                            wechat = doc.getString("wechat")?.ifBlank { null },
                            businessHours = doc.getString("businessHours") ?: "08:00 - 20:00",
                            tagsJson = doc.getString("tagsJson") ?: "",
                            photoUrl = doc.getString("photoUrl") ?: "",
                            serviceCategoriesJson = doc.getString("serviceCategoriesJson") ?: "",
                            priceListJson = doc.getString("priceListJson") ?: "",
                            isVerified = doc.getBoolean("isVerified") ?: true,
                            distanceKm = doc.getDouble("distanceKm") ?: 1.0
                        )
                    }.getOrNull()
                }
                onResult(stations)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * Save or update a User Profile in Firestore.
     */
    fun saveUserProfile(profile: UserProfile, onComplete: ((Boolean) -> Unit)? = null) {
        val db = getDb() ?: run {
            onComplete?.invoke(false)
            return
        }

        val profileMap = hashMapOf(
            "id" to profile.id,
            "displayName" to profile.displayName,
            "email" to profile.email,
            "phone" to profile.phone,
            "avatarUrl" to profile.avatarUrl,
            "recyclePoints" to profile.recyclePoints,
            "co2SavedKg" to profile.co2SavedKg,
            "completedRecycles" to profile.completedRecycles,
            "greenLevel" to profile.greenLevel,
            "role" to profile.role,
            "defaultAddress" to profile.defaultAddress,
            "createdAt" to profile.createdAt
        )

        db.collection(COLLECTION_USER_PROFILES)
            .document(profile.id)
            .set(profileMap)
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    /**
     * Save Dark Theme preference to Firestore user document.
     */
    fun saveUserProfilePreference(userId: String, isDarkMode: Boolean, onComplete: ((Boolean) -> Unit)? = null) {
        val db = getDb() ?: run {
            onComplete?.invoke(false)
            return
        }

        db.collection(COLLECTION_USER_PROFILES)
            .document(userId)
            .update("isDarkModePreference", isDarkMode)
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener {
                // If update fails because doc doesn't exist yet, set with merge
                db.collection(COLLECTION_USER_PROFILES)
                    .document(userId)
                    .set(mapOf("isDarkModePreference" to isDarkMode), com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener { onComplete?.invoke(true) }
                    .addOnFailureListener { onComplete?.invoke(false) }
            }
    }

    /**
     * Fetch User Profile from Firestore.
     */
    fun fetchUserProfile(userId: String, onResult: (UserProfile?) -> Unit) {
        val db = getDb() ?: run {
            onResult(null)
            return
        }

        db.collection(COLLECTION_USER_PROFILES)
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = runCatching {
                        UserProfile(
                            id = doc.getString("id") ?: userId,
                            displayName = doc.getString("displayName") ?: "环保践行者",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: "",
                            avatarUrl = doc.getString("avatarUrl") ?: "",
                            recyclePoints = (doc.getLong("recyclePoints") ?: 320).toInt(),
                            co2SavedKg = doc.getDouble("co2SavedKg") ?: 18.5,
                            completedRecycles = (doc.getLong("completedRecycles") ?: 5).toInt(),
                            greenLevel = doc.getString("greenLevel") ?: "GREEN_V2",
                            role = doc.getString("role") ?: "USER",
                            defaultAddress = doc.getString("defaultAddress") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    }.getOrNull()
                    onResult(profile)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    /**
     * Submit star rating & comment feedback for a recycle station after job completion,
     * storing the rating in the corresponding station document in Firestore.
     */
    fun submitStationRating(
        stationId: String,
        newRating: Int,
        comment: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val db = getDb() ?: run {
            onComplete?.invoke(false)
            return
        }

        val stationRef = db.collection(COLLECTION_STATIONS).document(stationId)
        stationRef.get().addOnSuccessListener { doc ->
            val currentRating = doc.getDouble("rating") ?: 4.8
            // Calculate smooth new weighted average rating
            val updatedRating = ((currentRating * 4 + newRating) / 5.0)

            val updateData = hashMapOf<String, Any>(
                "rating" to updatedRating,
                "latestReviewComment" to comment,
                "latestReviewTime" to System.currentTimeMillis()
            )

            stationRef.update(updateData)
                .addOnSuccessListener {
                    // Also store feedback document in reviews subcollection
                    val reviewMap = hashMapOf(
                        "rating" to newRating,
                        "comment" to comment,
                        "timestamp" to System.currentTimeMillis()
                    )
                    stationRef.collection("reviews").add(reviewMap)
                    onComplete?.invoke(true)
                }
                .addOnFailureListener {
                    onComplete?.invoke(false)
                }
        }.addOnFailureListener {
            onComplete?.invoke(false)
        }
    }
}
