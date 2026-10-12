package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language

/**
 * 'Recycle Progress' Stepper component that displays the status flow:
 * 'Pending Approval' -> 'Scheduled' -> 'Completed', updating based on the record status in Firestore.
 */
@Composable
fun RecycleProgressStepper(
    currentStatus: String,
    lang: Language,
    modifier: Modifier = Modifier
) {
    // Determine active step index (0 = Pending, 1 = Scheduled, 2 = Completed)
    val activeStep = when (currentStatus.uppercase()) {
        "PENDING", "MATCHING", "SUBMITTED" -> 0
        "SCHEDULED", "PROCESSING", "ACCEPTED" -> 1
        "COMPLETED", "FINISHED", "SIGNED" -> 2
        else -> 0
    }

    val steps = if (lang == Language.ZH) {
        listOf("审核派单\nPending", "预约上门\nScheduled", "完成归档\nCompleted")
    } else {
        listOf("Pending\nApproval", "Scheduled\nPickup", "Completed\nRecycled")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recycle_progress_stepper"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (lang == Language.ZH) "回收服务全流程跟踪 (Firestore 实时监测)" else "Recycle Progress Tracking (Firestore Synced)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, stepName ->
                    val isCompleted = index < activeStep
                    val isCurrent = index == activeStep

                    val circleBg by animateColorAsState(
                        targetValue = when {
                            isCompleted || isCurrent -> MaterialTheme.colorScheme.primary
                            else -> Color.LightGray.copy(alpha = 0.5f)
                        },
                        label = "step_bg"
                    )

                    // Step Node Item
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(circleBg)
                                .border(
                                    width = if (isCurrent) 2.dp else 0.dp,
                                    color = if (isCurrent) Color(0xFFED6C02) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Default.HourglassTop
                                        1 -> Icons.Default.EventAvailable
                                        else -> Icons.Default.TaskAlt
                                    },
                                    contentDescription = stepName,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stepName,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 12.sp
                        )
                    }

                    // Connecting Line between steps
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .height(3.dp)
                                .background(
                                    if (index < activeStep) MaterialTheme.colorScheme.primary
                                    else Color.LightGray.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }
        }
    }
}
