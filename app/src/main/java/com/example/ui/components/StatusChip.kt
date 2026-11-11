package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.theme.StatusCancelledColor
import com.example.ui.theme.StatusCompletedColor
import com.example.ui.theme.StatusMatchingColor
import com.example.ui.theme.StatusPendingColor
import com.example.ui.theme.StatusScheduledColor

@Composable
fun StatusChip(
    status: String,
    lang: Language,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        "PENDING" -> Triple(StatusPendingColor.copy(alpha = 0.15f), StatusPendingColor, if (lang == Language.ZH) "待审核" else "Pending")
        "MATCHING" -> Triple(StatusMatchingColor.copy(alpha = 0.15f), StatusMatchingColor, if (lang == Language.ZH) "待对接" else "Matching")
        "SCHEDULED" -> Triple(StatusScheduledColor.copy(alpha = 0.15f), StatusScheduledColor, if (lang == Language.ZH) "已安排" else "Scheduled")
        "COMPLETED" -> Triple(StatusCompletedColor.copy(alpha = 0.15f), StatusCompletedColor, if (lang == Language.ZH) "已完成" else "Completed")
        "CANCELLED" -> Triple(StatusCancelledColor.copy(alpha = 0.15f), StatusCancelledColor, if (lang == Language.ZH) "已取消" else "Cancelled")
        else -> Triple(Color.Gray.copy(alpha = 0.15f), Color.DarkGray, status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
