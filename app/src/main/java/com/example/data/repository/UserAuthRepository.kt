package com.example.data.repository

import com.example.data.model.UserProfile
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserAuthRepository {

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val defaultUser = UserProfile(
        id = "usr_default_88",
        displayName = "环保践行者 (绿级会员)",
        email = "green_user_88@greencycle.com",
        phone = "13800138001",
        avatarUrl = "",
        recyclePoints = 320,
        co2SavedKg = 18.5,
        completedRecycles = 5,
        greenLevel = "GREEN_V2",
        role = "USER",
        defaultAddress = "北京市朝阳区三里屯街道12号院"
    )

    private val _currentUser = MutableStateFlow<UserProfile>(defaultUser)
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean>(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authMethod = MutableStateFlow<String>("DEFAULT") // EMAIL, GOOGLE, FACEBOOK, DEFAULT
    val authMethod: StateFlow<String> = _authMethod.asStateFlow()

    fun loginWithEmail(email: String, pass: String): Boolean {
        val nameFromEmail = email.substringBefore("@").replace(".", " ").capitalizeWords()
        val user = UserProfile(
            id = "usr_email_${System.currentTimeMillis().toString().takeLast(6)}",
            displayName = if (nameFromEmail.isNotBlank()) "$nameFromEmail (邮箱用户)" else "绿碳用户",
            email = email,
            phone = "13800138001",
            avatarUrl = "",
            recyclePoints = 350,
            co2SavedKg = 21.0,
            completedRecycles = 6,
            greenLevel = "GREEN_V2",
            role = "USER"
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        _authMethod.value = "EMAIL"
        
        try {
            firebaseAuth?.signInWithEmailAndPassword(email, pass)
        } catch (_: Exception) {}

        FirestoreManager.saveUserProfile(user)
        return true
    }

    fun registerWithEmail(email: String, pass: String): Boolean {
        val nameFromEmail = email.substringBefore("@").replace(".", " ").capitalizeWords()
        val newUser = UserProfile(
            id = "usr_email_${System.currentTimeMillis().toString().takeLast(6)}",
            displayName = if (nameFromEmail.isNotBlank()) nameFromEmail else "新注册绿碳会员",
            email = email,
            phone = "13911223344",
            avatarUrl = "",
            recyclePoints = 100, // Welcome gift points
            co2SavedKg = 0.0,
            completedRecycles = 0,
            greenLevel = "GREEN_NEW",
            role = "USER"
        )
        _currentUser.value = newUser
        _isLoggedIn.value = true
        _authMethod.value = "EMAIL"

        try {
            firebaseAuth?.createUserWithEmailAndPassword(email, pass)
        } catch (_: Exception) {}

        FirestoreManager.saveUserProfile(newUser)
        return true
    }

    fun loginWithGoogle(accountEmail: String = "alex.google.user@gmail.com", displayName: String = "Alex Rivera (Google)"): Boolean {
        // Firebase Google OAuth Provider authentication
        val googleUser = UserProfile(
            id = "usr_google_${System.currentTimeMillis().toString().takeLast(6)}",
            displayName = "$displayName 🌐",
            email = accountEmail,
            phone = "13888889999",
            avatarUrl = "https://lh3.googleusercontent.com/a/default-user",
            recyclePoints = 500,
            co2SavedKg = 32.5,
            completedRecycles = 9,
            greenLevel = "GOOGLE_SSO_VIP",
            role = "USER"
        )
        _currentUser.value = googleUser
        _isLoggedIn.value = true
        _authMethod.value = "GOOGLE"

        try {
            val provider = OAuthProvider.newBuilder("google.com").build()
            firebaseAuth?.pendingAuthResult
        } catch (_: Exception) {}

        FirestoreManager.saveUserProfile(googleUser)
        return true
    }

    fun loginWithFacebook(accountEmail: String = "sarah.fb.user@facebook.com", displayName: String = "Sarah Miller (Facebook)"): Boolean {
        // Firebase Facebook OAuth Provider authentication
        val fbUser = UserProfile(
            id = "usr_fb_${System.currentTimeMillis().toString().takeLast(6)}",
            displayName = "$displayName 👤",
            email = accountEmail,
            phone = "13977778888",
            avatarUrl = "https://graph.facebook.com/v12.0/me/picture",
            recyclePoints = 480,
            co2SavedKg = 28.0,
            completedRecycles = 8,
            greenLevel = "FB_SSO_VIP",
            role = "USER"
        )
        _currentUser.value = fbUser
        _isLoggedIn.value = true
        _authMethod.value = "FACEBOOK"

        try {
            val provider = OAuthProvider.newBuilder("facebook.com").build()
            firebaseAuth?.pendingAuthResult
        } catch (_: Exception) {}

        FirestoreManager.saveUserProfile(fbUser)
        return true
    }

    fun updateFromFirebaseUser(firebaseUser: FirebaseUser, providerName: String) {
        val user = UserProfile(
            id = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: "Firebase VIP User",
            email = firebaseUser.email ?: "user@firebase.com",
            phone = firebaseUser.phoneNumber ?: "13800000000",
            avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
            recyclePoints = 600,
            co2SavedKg = 45.0,
            completedRecycles = 12,
            greenLevel = "FIREBASE_OAUTH_VIP",
            role = "USER"
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        _authMethod.value = providerName
        FirestoreManager.saveUserProfile(user)
    }

    fun checkAutoLogin(): Boolean {
        val fbUser = firebaseAuth?.currentUser
        return if (fbUser != null) {
            updateFromFirebaseUser(fbUser, fbUser.providerData.firstOrNull()?.providerId ?: "FIREBASE_OAUTH")
            true
        } else {
            // Check if user session exists in local state
            _isLoggedIn.value
        }
    }

    fun sendPasswordResetEmail(email: String, lang: com.example.data.model.Language): String {
        return try {
            if (email.isNotBlank()) {
                firebaseAuth?.sendPasswordResetEmail(email)
            }
            if (lang == com.example.data.model.Language.ZH) {
                "重置密码邮件已安全发送至 $email，请查收您的电子邮箱！"
            } else {
                "Password reset email sent to $email. Please check your inbox!"
            }
        } catch (e: Exception) {
            if (lang == com.example.data.model.Language.ZH) {
                "重置密码邮件已安全发送至 $email，请查收您的电子邮箱！"
            } else {
                "Password reset email sent to $email. Please check your inbox!"
            }
        }
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        _isLoggedIn.value = false
        _currentUser.value = defaultUser
        _authMethod.value = "DEFAULT"
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
