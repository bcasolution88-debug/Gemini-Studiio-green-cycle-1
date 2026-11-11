package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Language

@Composable
fun UserAgreementDialog(
    initialLang: Language = Language.ZH,
    onDismiss: () -> Unit,
    onAccept: (() -> Unit)? = null
) {
    var lang by remember { mutableStateOf(initialLang) }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("user_agreement_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == Language.ZH) "📜 用户服务协议" else "📜 User Service Agreement",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Language Toggle Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .clickable {
                                    lang = if (lang == Language.ZH) Language.EN else Language.ZH
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Switch Language",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) "EN" else "中文",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Document Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(380.dp)
                        .verticalScroll(scrollState)
                        .padding(end = 4.dp)
                ) {
                    if (lang == Language.ZH) {
                        AgreementTextSectionZh()
                    } else {
                        AgreementTextSectionEn()
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onAccept?.invoke()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("accept_user_agreement_btn"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "我已阅读并同意本协议" else "I Agree to User Agreement",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyDialog(
    initialLang: Language = Language.ZH,
    onDismiss: () -> Unit,
    onAccept: (() -> Unit)? = null
) {
    var lang by remember { mutableStateOf(initialLang) }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("privacy_policy_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == Language.ZH) "🛡️ 隐私保护政策" else "🛡️ Privacy Policy",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Language Switch Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                                .clickable {
                                    lang = if (lang == Language.ZH) Language.EN else Language.ZH
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Switch Language",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) "EN" else "中文",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Document Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(380.dp)
                        .verticalScroll(scrollState)
                        .padding(end = 4.dp)
                ) {
                    if (lang == Language.ZH) {
                        PrivacyTextSectionZh()
                    } else {
                        PrivacyTextSectionEn()
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onAccept?.invoke()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("accept_privacy_policy_btn"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(
                        text = if (lang == Language.ZH) "我已阅读并同意隐私政策" else "I Agree to Privacy Policy",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AgreementTextSectionZh() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("更新日期：2026年8月 | 欢迎使用绿碳循环（GreenCycle）平台服务！", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionHeadingZh("一、服务条款的确认与接纳")
        Text("本平台（以下简称“本平台”或“绿碳循环”）由绿色可持续发展技术团队运营。您在注册、登录或使用本平台服务前，请审慎阅读并充分理解本《用户服务协议》。点击“注册”或“登录”按钮即表示您已阅读并完全同意接受本协议的全部条款。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingZh("二、账号注册与安全")
        Text("1. 您承诺在注册时提供真实、准确、有效的邮箱地址，并设置符合安全强度的密码。\n2. 用户应妥善保管自己的账号与密码，不得将账号转让、租借或赠与他人。因用户自身原因导致的密码泄露或账号被盗，由用户自行承担后果。\n3. 支持通过 Google 帐号或 Facebook 帐号快速一键登录，单点登录（SSO）授权遵守 OAuth 2.0 国际安全规范。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingZh("三、旧物回收与爱心捐赠规范")
        Text("1. 用户在发布二手旧物、电子废弃物或捐赠意向时，必须确保物品来源合法、真实且无产权纠纷。\n2. 禁投危险品、易燃易爆物、有毒有害未经处理化学品。\n3. 预约站长上门揽收或物流快递回收时，应保持联系电话畅通。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingZh("四、绿碳积分与减碳勋章政策")
        Text("1. 用户参与旧物回收、捐赠接力或社区活动将获得相应的“绿碳积分”与减碳成就。\n2. 积分仅可用于本平台权益兑换或绿碳公益捐赠，不可直接兑现或私下非法买卖。\n3. 平台保留对虚假刷分行为冻结账号并扣除积分的权利。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingZh("五、知识产权与用户内容")
        Text("用户在论坛社区发布的改造教程、DIY视频、心得体会，著作权归用户本人所有，但授予本平台在全球范围内的免费、非独占传播与推广使用权。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingZh("六、免责声明与协议修改")
        Text("本平台有权根据法律法规更新或平台运营需要调整本协议。修改后的协议将在平台公示，继续使用本平台服务即视为同意修改后的协议。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun AgreementTextSectionEn() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Last Updated: August 2026 | Welcome to GreenCycle Platform!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionHeadingEn("1. Acceptance of Terms")
        Text("Welcome to GreenCycle. By creating an account, signing in, or accessing our platform services, you acknowledge that you have read, understood, and agreed to be bound by this User Service Agreement in its entirety.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingEn("2. Account Registration & Security")
        Text("1. You must provide a valid email address and set a secure password upon registration.\n2. You are solely responsible for maintaining the confidentiality of your account credentials. You shall not transfer or share your account with third parties.\n3. Third-party quick authentication via Google Account or Facebook Account is supported using OAuth 2.0 industry safety standards.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingEn("3. Recycling & Donation Guidelines")
        Text("1. Items posted for recycling or donation must be legitimately owned, safe, and legally compliant.\n2. Hazardous materials, explosives, or toxic bio-waste are strictly prohibited.\n3. Ensure active communication when scheduling on-site station pickups or logistics couriers.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingEn("4. Green Credits & Carbon Rewards")
        Text("1. Completing recycling orders and donation drives earns Green Credits and Carbon Badges.\n2. Credits are non-transferable and cannot be exchanged for fiat currency.\n3. GreenCycle reserves the right to audit and revoke improperly obtained points.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingEn("5. Intellectual Property")
        Text("Content posted by users (DIY tutorials, zero-waste videos, community posts) remains user intellectual property, while granting GreenCycle a non-exclusive license to showcase and promote the content globally.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingEn("6. Amendments")
        Text("GreenCycle reserves the right to revise this agreement periodically. Continued use of the service constitutes acceptance of updated terms.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PrivacyTextSectionZh() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("生效日期：2026年8月 | 我们高度重视您的个人隐私与数据安全", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionHeadingZh("一、我们收集的信息")
        Text("1. **账号注册信息**：包括邮箱地址、密码密文、昵称。若您选择 Google 或 Facebook 快捷登录，我们将获取您的公开 OpenID、头像及基础邮箱授权。\n2. **回收履约信息**：上门揽收地址、联系电话、捐赠物品描述及照片。\n3. **设备与位置信息**：地理位置经纬度（用于智能推荐最近的绿色回收站点及地图导航）。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingZh("二、信息的使用方式")
        Text("1. 用于向您提供旧物回收派单、上门预约揽收、绿碳积分统计及收据生成。\n2. 用于计算累计减碳量（CO2减排千克数）及生成个人环保证书。\n3. 改善平台服务安全性，防止欺诈与违规投递。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingZh("三、第三方共享与 SDK 授权")
        Text("1. **物流与站长调度**：在您提交上门回收或捐赠后，必要的地址与电话将同步给负责接单的站长或公益快递（如顺丰/京东快递）。\n2. **社交单点登录**：Google Auth SDK 与 Facebook Identity SDK 仅在您主动发起快捷登录时传递必要的 OAuth 安全 Token，绝不侵犯私人隐私。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingZh("四、数据存储与安全保障")
        Text("我们采用高强度 TLS/SSL 加密传输，并使用 Google Cloud Firestore / Firebase 基础设施进行数据加密持久化。您的密码使用哈希算法散列存储，绝不留存明文。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingZh("五、您的数据权利")
        Text("您有权随时在“个人中心”查看、修改您的个人资料，或请求导出您的回收履约历史。如需注销账号或彻底注销个人信息，请联系 privacy@greencycle.org。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PrivacyTextSectionEn() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Effective Date: August 2026 | We take your privacy and data security seriously.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionHeadingEn("1. Information We Collect")
        Text("1. **Account Registration Data**: Email address, password hash, and display name. If choosing Google or Facebook One-Click Login, we obtain OAuth OpenID, avatar URL, and authorized basic profile info.\n2. **Recycling Transaction Data**: Pickup address, phone number, item descriptions, and photos.\n3. **Location Data**: Precise GPS location used exclusively to render nearest recycling stations and route mapping.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingEn("2. How We Use Your Information")
        Text("1. To fulfill recycling pickup orders, schedule donation drives, and issue donation certificates.\n2. To calculate personal CO2 emissions reduction and credit balance.\n3. To prevent fraudulent posts and ensure system security.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingEn("3. Third-Party Sharing & OAuth")
        Text("1. **Logistics & Station Dispatch**: Contact numbers and pickup addresses are shared strictly with designated station admins and pickup couriers.\n2. **Social Single Sign-On**: Google Auth and Facebook Identity APIs are used solely for identity verification during quick login.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingEn("4. Data Security & Encrypted Storage")
        Text("All communications use TLS/SSL encryption. Data is stored on Google Cloud Firestore with end-to-end security policies. Passwords are password-hashed securely.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        SectionHeadingEn("5. Your Rights & Contact")
        Text("You have the right to view, update, export, or request deletion of your account and recycling records at any time by contacting privacy@greencycle.org.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SectionHeadingZh(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun SectionHeadingEn(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}
