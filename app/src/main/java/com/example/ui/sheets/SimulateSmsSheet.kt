package com.example.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertOrangeText
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeLight
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulateSmsSheet(
    onSimulate: (smsText: String, bankName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    var customSmsText by remember {
        mutableStateOf("بلو\nبرداشت پول\nمیرمحمدرضا عزیز، 40,000,000 ریال از حساب شما پرید.\nموجودی: 1,353,705,475 ریال\n۲۰:۱۱\n۱۴۰۵.۰۷.۰۵")
    }
    var bankName by remember { mutableStateOf("بلوبانک") }

    val presets = listOf(
        "بلوبانک (برداشت ۴۰ میلیون ریال)" to ("بلو\nبرداشت پول\nمیرمحمدرضا عزیز، 40,000,000 ریال از حساب شما پرید.\nموجودی: 1,353,705,475 ریال\n۲۰:۱۱\n۱۴۰۵.۰۷.۰۵" to "بلوبانک"),
        "بلوبانک (واریز ۴۸.۹ میلیون ریال)" to ("بلو\nواریز پول\nمیرمحمدرضا عزیز، 48,900,000 ریال به حساب شما نشست.\nموجودی: 1,393,726,975 ریال\n۱۸:۲۴\n۱۴۰۵.۰۷.۰۵" to "بلوبانک"),
        "بانک ملی (واریز ۵۰ میلیون ریال)" to ("حساب7703571824\nواریز50,000,000\nمانده335,547,577\n05/07/05-14:08" to "بانک ملی"),
        "بانک ملت (واریز ۳.۵ میلیون ریال)" to ("بانک ملت: واریز مبلغ 3,500,000 ریال به حساب 8291. مانده: 18,200,000 ریال" to "بانک ملت"),
        "بانک سامان (واریز ۱.۲۵ میلیون تومان)" to ("بانک سامان: واریز مبلغ 1,250,000 تومان - انتقال پایا" to "بانک سامان")
    )

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
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sms, contentDescription = null, tint = PrimaryOrange)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "شبیه‌ساز پیامک بانکی",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextMedium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryOrangeLight
            ) {
                Text(
                    text = "پیامک‌های بانکی به ریال به صورت خودکار با حذف یک صفر به تومان تبدیل شده و به لیست ثبت موقت اضافه می‌شوند.",
                    fontSize = 11.sp,
                    color = AlertOrangeText,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "الگوهای آماده پیامک:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            presets.forEach { (label, data) ->
                val (txt, bName) = data
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BorderLight.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            customSmsText = txt
                            bankName = bName
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = customSmsText,
                onValueChange = { customSmsText = it },
                label = { Text("متن پیامک ارسالی") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("نام یا فرستنده بانک") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (customSmsText.isNotBlank()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSimulate(customSmsText, bankName)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "شبیه‌سازی دریافت پیامک",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
