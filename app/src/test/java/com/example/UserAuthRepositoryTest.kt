package com.example

import com.example.data.model.Language
import com.example.data.model.LanguageManager
import com.example.data.repository.UserAuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserAuthRepositoryTest {

    @Test
    fun testRegisterWithEmail() {
        val email = "test_eco_user@greencycle.com"
        val result = UserAuthRepository.registerWithEmail(email, "password123")
        assertTrue(result)
        assertTrue(UserAuthRepository.isLoggedIn.value)
        assertEquals("EMAIL", UserAuthRepository.authMethod.value)
        assertEquals(email, UserAuthRepository.currentUser.value.email)
        assertEquals(100, UserAuthRepository.currentUser.value.recyclePoints)
    }

    @Test
    fun testLoginWithEmail() {
        val email = "green_member@greencycle.com"
        val result = UserAuthRepository.loginWithEmail(email, "securepass")
        assertTrue(result)
        assertTrue(UserAuthRepository.isLoggedIn.value)
        assertEquals("EMAIL", UserAuthRepository.authMethod.value)
        assertEquals(email, UserAuthRepository.currentUser.value.email)
    }

    @Test
    fun testLoginWithGoogleOAuth() {
        val result = UserAuthRepository.loginWithGoogle("alex.google@gmail.com", "Alex Rivera")
        assertTrue(result)
        assertTrue(UserAuthRepository.isLoggedIn.value)
        assertEquals("GOOGLE", UserAuthRepository.authMethod.value)
        assertTrue(UserAuthRepository.currentUser.value.displayName.contains("Alex Rivera"))
    }

    @Test
    fun testLoginWithFacebookOAuth() {
        val result = UserAuthRepository.loginWithFacebook("sarah.fb@facebook.com", "Sarah Miller")
        assertTrue(result)
        assertTrue(UserAuthRepository.isLoggedIn.value)
        assertEquals("FACEBOOK", UserAuthRepository.authMethod.value)
        assertTrue(UserAuthRepository.currentUser.value.displayName.contains("Sarah Miller"))
    }

    @Test
    fun testSendPasswordResetEmail() {
        val email = "reset_test@greencycle.com"
        val msgZh = UserAuthRepository.sendPasswordResetEmail(email, Language.ZH)
        assertTrue(msgZh.contains(email))
        assertTrue(msgZh.contains("重置密码邮件已安全发送"))

        val msgEn = UserAuthRepository.sendPasswordResetEmail(email, Language.EN)
        assertTrue(msgEn.contains(email))
        assertTrue(msgEn.contains("Password reset email sent"))
    }

    @Test
    fun testLanguageManagerToggle() {
        val manager = LanguageManager()
        assertEquals(Language.ZH, manager.currentLanguage)
        manager.toggleLanguage()
        assertEquals(Language.EN, manager.currentLanguage)
        manager.toggleLanguage()
        assertEquals(Language.ZH, manager.currentLanguage)
    }
}
