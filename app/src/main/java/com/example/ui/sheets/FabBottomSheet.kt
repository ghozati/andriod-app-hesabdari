package com.example.ui.sheets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeLight
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FabBottomSheet(
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عملیات سریع",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextMedium)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FabActionItem(
                icon = Icons.Default.Build,
                iconTint = PrimaryGreen,
                iconBg = PrimaryGreenLight,
                title = "ثبت خدمت (کسر از بدهی)",
                subtitle = "ثبت کار یا شیفت مغازه برای کسر مستقیم از بدهی",
                testTag = "fab_add_service",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOptionSelected("ADD_SERVICE")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            FabActionItem(
                icon = Icons.Default.Receipt,
                iconTint = PrimaryOrange,
                iconBg = PrimaryOrangeLight,
                title = "تراکنش دستی",
                subtitle = "ثبت دستی هزینه، واریز، یا انتقال حساب‌ها",
                testTag = "fab_manual_transaction",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOptionSelected("MANUAL_TRANSACTION")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            FabActionItem(
                icon = Icons.Default.AccountBalance,
                iconTint = Color(0xFF10B981),
                iconBg = Color(0xFFD1FAE5),
                title = "مدیریت حساب‌های بانکی",
                subtitle = "تفکیک موجودی و تعریف بانک‌ها (بلو، ملت و...)",
                testTag = "fab_manage_banks",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOptionSelected("MANAGE_BANKS")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            FabActionItem(
                icon = Icons.Default.AccountBalance,
                iconTint = Color(0xFF2563EB),
                iconBg = Color(0xFFEFF6FF),
                title = "تنظیم موجودی اولیه",
                subtitle = "ثبت موجودی اولیه بانک و مانده بدهی قبلی به مغازه",
                testTag = "fab_initial_setup",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOptionSelected("INITIAL_SETUP")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            FabActionItem(
                icon = Icons.Default.Sms,
                iconTint = Color(0xFF7E22CE),
                iconBg = Color(0xFFF3E8FF),
                title = "شبیه‌ساز پیامک بانکی",
                subtitle = "آزمایش استخراج و دریافت خودکار پیامک بانک",
                testTag = "fab_simulate_sms",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOptionSelected("SIMULATE_SMS")
                }
            )
        }
    }
}

@Composable
private fun FabActionItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextMedium,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
