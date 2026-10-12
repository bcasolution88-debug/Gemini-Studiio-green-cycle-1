package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Language
import com.example.data.model.LocalLanguageManager
import com.example.data.repository.UserAuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthDialog(
    initialTab: Int = 0, // 0 = Login, 1 = Register
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val langManager = LocalLanguageManager.current
    var currentLang by remember { mutableStateOf(langManager.currentLanguage) }

    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }

    // Form fields
    var loginEmail by remember { mutableStateOf("eco.user@greencycle.com") }
    var loginPassword by remember { mutableStateOf("12345678") }

    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isAgreedToTerms by remember { mutableStateOf(false) }

    // Dialog state for Agreements and Forgot Password
    var showUserAgreementDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Loading State
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header with Title and Language Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (currentLang == Language.ZH) "绿碳账号与身份验证" else "Account & Authentication",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentLang == Language.ZH) "安全登录 / 快速注册" else "Sign In / Quick Registration",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Language Switcher Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    langManager.toggleLanguage()
                                    currentLang = if (currentLang == Language.ZH) Language.EN else Language.ZH
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("auth_dialog_lang_toggle_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Switch Language",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLang == Language.ZH) "English" else "中文",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDismiss()
                            },
                            modifier = Modifier.testTag("close_auth_dialog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher: 登录 (Sign In) vs 注册帐号 (Register)
                val tabTitles = listOf(
                    if (currentLang == Language.ZH) "🔑 账号登录" else "🔑 Sign In",
                    if (currentLang == Language.ZH) "✨ 注册新账号" else "✨ Register"
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedTabIndex = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_$index")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Loading Overlay if social auth or request is processing
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = loadingMessage,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    // Smooth Cross-Fade Animation between Login and Register views
                    Crossfade(
                        targetState = selectedTabIndex,
                        label = "AuthViewCrossfade"
                    ) { tabIndex ->
                        if (tabIndex == 0) {
                            // TAB 0: LOGIN FORM
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it },
                                    label = { Text(if (currentLang == Language.ZH) "电子邮箱地址" else "Email Address") },
                                    placeholder = { Text("example@domain.com") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_email_input")
                                )

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    label = { Text(if (currentLang == Language.ZH) "登录密码" else "Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle Password Visibility"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input")
                                )

                                // Remember me & Forgot password
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isAgreedToTerms,
                                            onCheckedChange = { isAgreedToTerms = it },
                                            modifier = Modifier.testTag("login_agree_checkbox")
                                        )
                                        Text(
                                            text = if (currentLang == Language.ZH) "记住登录状态" else "Remember me",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showForgotPasswordDialog = true
                                        },
                                        modifier = Modifier.testTag("forgot_password_btn")
                                    ) {
                                        Text(
                                            text = if (currentLang == Language.ZH) "忘记密码？" else "Forgot Password?",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Submit Login Button with Haptic Feedback
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (loginEmail.isBlank() || !loginEmail.contains("@")) {
                                            Toast.makeText(
                                                context,
                                                if (currentLang == Language.ZH) "请输入有效的电子邮箱地址" else "Please enter a valid email address",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }
                                        if (loginPassword.length < 6) {
                                            Toast.makeText(
                                                context,
                                                if (currentLang == Language.ZH) "密码长度不能少于6位" else "Password must be at least 6 characters",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }

                                        isLoading = true
                                        loadingMessage = if (currentLang == Language.ZH) "正在验证身份并登录中..." else "Authenticating credentials..."

                                        scope.launch {
                                            delay(800)
                                            UserAuthRepository.loginWithEmail(loginEmail, loginPassword)
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                if (currentLang == Language.ZH) "🎉 登录成功！欢迎回来" else "🎉 Sign in successful!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onSuccess()
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("submit_login_btn"),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.ZH) "立即安全登录" else "Sign In Now",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // TAB 1: EMAIL REGISTRATION FORM
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = registerEmail,
                                    onValueChange = { registerEmail = it },
                                    label = { Text(if (currentLang == Language.ZH) "设置注册邮箱" else "Email Address") },
                                    placeholder = { Text("user@example.com") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_email_input")
                                )

                                OutlinedTextField(
                                    value = registerPassword,
                                    onValueChange = { registerPassword = it },
                                    label = { Text(if (currentLang == Language.ZH) "设置登录密码" else "Create Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle Password Visibility"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_password_input")
                                )

                                if (registerPassword.isNotEmpty()) {
                                    val strength = getPasswordStrength(registerPassword)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (currentLang == Language.ZH) "密码强度: " else "Strength: ",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(strength.color)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (currentLang == Language.ZH) strength.labelZh else strength.labelEn,
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = registerConfirmPassword,
                                    onValueChange = { registerConfirmPassword = it },
                                    label = { Text(if (currentLang == Language.ZH) "确认登录密码" else "Confirm Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle Confirm Visibility"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_confirm_password_input")
                                )

                                if (registerConfirmPassword.isNotEmpty()) {
                                    val isMatch = registerPassword == registerConfirmPassword
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isMatch) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (isMatch) Color(0xFF2E7D32) else Color.Red,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isMatch) {
                                                if (currentLang == Language.ZH) "两次输入的密码一致" else "Passwords match"
                                            } else {
                                                if (currentLang == Language.ZH) "两次输入的密码不一致" else "Passwords do not match"
                                            },
                                            fontSize = 10.sp,
                                            color = if (isMatch) Color(0xFF2E7D32) else Color.Red
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Checkbox(
                                        checked = isAgreedToTerms,
                                        onCheckedChange = { isAgreedToTerms = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .testTag("register_agreement_checkbox")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (currentLang == Language.ZH) "我已阅读并同意 " else "I have read and agree to ",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (currentLang == Language.ZH) "《用户服务协议》" else "User Agreement",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .clickable { showUserAgreementDialog = true }
                                                    .testTag("open_user_agreement_link")
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (currentLang == Language.ZH) "与 " else " and ",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (currentLang == Language.ZH) "《隐私保护政策》" else "Privacy Policy",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier
                                                    .clickable { showPrivacyPolicyDialog = true }
                                                    .testTag("open_privacy_policy_link")
                                            )
                                        }
                                    }
                                }

                                // Submit Registration Button with Haptic Feedback
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (registerEmail.isBlank() || !registerEmail.contains("@")) {
                                            Toast.makeText(
                                                context,
                                                if (currentLang == Language.ZH) "请输入有效的电子邮箱地址" else "Please enter a valid email address",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }
                                        if (registerPassword.length < 6) {
                                            Toast.makeText(
                                                context,
                                                if (currentLang == Language.ZH) "密码长度不能少于6位" else "Password must be at least 6 characters",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }
                                        if (registerPassword != registerConfirmPassword) {
                                            Toast.makeText(
                                                context,
                                                if (currentLang == Language.ZH) "两次输入的密码不一致" else "Passwords do not match",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }
                                        if (!isAgreedToTerms) {
                                            Toast.makeText(
                                                context,
                                                if (currentLang == Language.ZH) "请勾选同意《用户服务协议》与《隐私政策》" else "Please agree to the User Agreement and Privacy Policy",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }

                                        isLoading = true
                                        loadingMessage = if (currentLang == Language.ZH) "正在创建绿碳账号并注册中..." else "Creating account..."

                                        scope.launch {
                                            delay(1000)
                                            UserAuthRepository.registerWithEmail(registerEmail, registerPassword)
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                if (currentLang == Language.ZH) "🎉 账号注册成功！获得 100 绿碳积分奖励" else "🎉 Registration successful! +100 Credits earned!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onSuccess()
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("submit_register_btn"),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.ZH) "注册并领取绿碳新人礼包" else "Register & Claim Welcome Gift",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = if (currentLang == Language.ZH) "  第三方快捷登录  " else "  Quick Social Sign In  ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Google Login with Haptic Feedback
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isLoading = true
                                loadingMessage = if (currentLang == Language.ZH) "正在安全对接 Google One-Tap 身份认证..." else "Connecting to Google Auth..."

                                scope.launch {
                                    delay(900)
                                    UserAuthRepository.loginWithGoogle()
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        if (currentLang == Language.ZH) "🌐 Google 帐号快捷登录成功！" else "🌐 Google sign in successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onSuccess()
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("google_sso_login_btn"),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEA4335)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (currentLang == Language.ZH) "Google 帐号一键登录" else "Continue with Google",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Facebook Login with Haptic Feedback
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isLoading = true
                                loadingMessage = if (currentLang == Language.ZH) "正在安全对接 Facebook Single Sign-On..." else "Connecting to Facebook Identity..."

                                scope.launch {
                                    delay(900)
                                    UserAuthRepository.loginWithFacebook()
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        if (currentLang == Language.ZH) "👤 Facebook 帐号快捷登录成功！" else "👤 Facebook sign in successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onSuccess()
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("facebook_sso_login_btn"),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1877F2),
                                contentColor = Color.White
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "f",
                                        color = Color(0xFF1877F2),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (currentLang == Language.ZH) "Facebook 帐号一键登录" else "Continue with Facebook",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sub-Dialogs for Agreements and Forgot Password
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            initialEmail = loginEmail,
            lang = currentLang,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }

    if (showUserAgreementDialog) {
        UserAgreementDialog(
            initialLang = currentLang,
            onDismiss = { showUserAgreementDialog = false },
            onAccept = {
                isAgreedToTerms = true
                showUserAgreementDialog = false
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            initialLang = currentLang,
            onDismiss = { showPrivacyPolicyDialog = false },
            onAccept = {
                isAgreedToTerms = true
                showPrivacyPolicyDialog = false
            }
        )
    }
}

@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    lang: Language,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var emailInput by remember { mutableStateOf(initialEmail) }
    var isSending by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("forgot_password_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (lang == Language.ZH) "重置绿碳账号密码" else "Reset Your Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (lang == Language.ZH)
                        "请输入注册时使用的电子邮箱，系统将通过 Firebase 安全发送重置密码邮件。"
                    else
                        "Enter your registered email address. We will send a password reset link via Firebase Auth.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text(if (lang == Language.ZH) "重置邮箱" else "Reset Email") },
                    placeholder = { Text("example@domain.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_email_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_reset_btn")
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "取消" else "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (emailInput.isBlank() || !emailInput.contains("@")) {
                                Toast.makeText(
                                    context,
                                    if (lang == Language.ZH) "请输入有效电子邮箱" else "Please enter a valid email",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            isSending = true
                            scope.launch {
                                delay(900)
                                val msg = UserAuthRepository.sendPasswordResetEmail(emailInput, lang)
                                isSending = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        },
                        enabled = !isSending,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("send_reset_email_btn")
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (lang == Language.ZH) "发送重置邮件" else "Send Reset Link",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private data class PasswordStrength(
    val labelZh: String,
    val labelEn: String,
    val color: Color
)

private fun getPasswordStrength(pass: String): PasswordStrength {
    return when {
        pass.length < 6 -> PasswordStrength("过短", "Too Short", Color.Gray)
        pass.length < 8 -> PasswordStrength("弱", "Weak", Color(0xFFE53935))
        pass.length < 12 && pass.any { it.isDigit() } -> PasswordStrength("中等", "Medium", Color(0xFFFB8C00))
        else -> PasswordStrength("高强度", "Strong", Color(0xFF43A047))
    }
}
