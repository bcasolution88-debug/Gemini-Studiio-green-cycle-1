package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.components.BilingualHeader

@Composable
fun StationRegistrationScreen(
    lang: Language,
    onSubmit: (
        name: String,
        address: String,
        phone: String,
        wechat: String,
        businessHours: String,
        tags: String,
        services: String,
        priceList: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var stationName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var wechat by remember { mutableStateOf("") }
    var businessHours by remember { mutableStateOf("08:00 - 20:00") }
    var tags by remember { mutableStateOf("官方认证,地磅称重,打包上门") }
    var services by remember { mutableStateOf("塑料瓶,饮料听罐,废纸板,旧家电,数码产品") }
    var priceList by remember { mutableStateOf("PET塑料瓶:¥2.6/kg;废铜线:¥50/kg;旧冰箱:¥100/台") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BilingualHeader(
            title = if (lang == Language.ZH) "回收站点入驻申请" else "Station Registration"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (lang == Language.ZH) "提交站点资质与回收名片 (审核通过后上线地图)" else "Submit Station Qualifications & Rate Sheet",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = stationName,
                onValueChange = { stationName = it },
                label = { Text(if (lang == Language.ZH) "回收站点名称 (含营业执照名称)" else "Station Name") },
                modifier = Modifier.fillMaxWidth().testTag("reg_station_name_input")
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(if (lang == Language.ZH) "站点经营详细地址" else "Operating Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (lang == Language.ZH) "联系电话" else "Phone") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = wechat,
                    onValueChange = { wechat = it },
                    label = { Text(if (lang == Language.ZH) "微信号名片" else "WeChat Card") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = businessHours,
                onValueChange = { businessHours = it },
                label = { Text(if (lang == Language.ZH) "营业时间段" else "Business Hours") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = services,
                onValueChange = { services = it },
                label = { Text(if (lang == Language.ZH) "服务涵盖品类" else "Service Categories") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = priceList,
                onValueChange = { priceList = it },
                label = { Text(if (lang == Language.ZH) "回收价目表 (格式: 品类:价格;品类:价格)" else "Price Rate Sheet") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (stationName.isBlank()) stationName = "新入驻环保回收点"
                    if (address.isBlank()) address = "北京市朝阳区"
                    if (phone.isBlank()) phone = "010-88889999"
                    onSubmit(stationName, address, phone, wechat, businessHours, tags, services, priceList)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_station_reg_btn"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Submit")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (lang == Language.ZH) "提交入驻申请" else "Submit Station Registration",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
