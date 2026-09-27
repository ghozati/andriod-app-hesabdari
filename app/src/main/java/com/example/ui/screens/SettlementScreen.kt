package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.components.FintechCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeLight
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMedium
import com.example.ui.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettlementScreen(
    unclearedItems: List<TransactionEntity>,
    clearedHistory: List<TransactionEntity>,
    selectedIds: Set<Long>,
    onToggleSelection: (Long) -> Unit,
    onSelectAll: (List<Long>) -> Unit,
    onClearSelection: () -> Unit,
    onSettle: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Pending, 1 = History

    val totalSelectedAmount by remember(selectedIds, unclearedItems) {
        derivedStateOf {
            unclearedItems.filter { selectedIds.contains(it.id) }.sumOf { it.amount }
        }
    }

    val totalPendingAmount by remember(unclearedItems) {
        derivedStateOf { unclearedItems.sumOf { it.amount } }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Title & Overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تسویه‌حساب",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "هزینه‌ها و خدمات کسر شونده از بدهی مغازه",
                        fontSize = 12.sp,
                        color = TextMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs: Pending vs Cleared
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardBackground,
                contentColor = PrimaryGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryGreen
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    text = {
                        Text(
                            "در انتظار تسویه (${CurrencyFormatter.formatNumber(unclearedItems.size)})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    text = {
                        Text(
                            "تسویه شده رسمی",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Pending Settlement Tab

// Sub-filter Pills: [همه], [واریزی و هزینه], [خدمات]
                var subFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Shop Transactions, 2 = Services

                val currentUnclearedList = remember(unclearedItems, subFilter) {
                    when (subFilter) {
                        1 -> unclearedItems.filter { it.type == TransactionEntity.TYPE_SHOP }
                        2 -> unclearedItems.filter { it.type == TransactionEntity.TYPE_SERVICE }
                        else -> unclearedItems
                    }
                }

                val currentTotalAmount = remember(currentUnclearedList) {
                    currentUnclearedList.sumOf { it.amount }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val shopCount = unclearedItems.count { it.type == TransactionEntity.TYPE_SHOP }
                    val serviceCount = unclearedItems.count { it.type == TransactionEntity.TYPE_SERVICE }

                    listOf(
                        Triple(0, "همه (${CurrencyFormatter.formatNumber(unclearedItems.size)})", "ALL"),
                        Triple(1, "واریزی و هزینه (${CurrencyFormatter.formatNumber(shopCount)})", "SHOP"),
                        Triple(2, "خدمات (${CurrencyFormatter.formatNumber(serviceCount)})", "SERVICE")
                    ).forEach { (idx, label, _) ->
                        val isSelected = subFilter == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PrimaryGreenLight else Color.Transparent)
                                .clickable {
                                    subFilter = idx
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PrimaryGreen else TextMedium,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مجموع این بخش: ${CurrencyFormatter.format(currentTotalAmount)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    if (currentUnclearedList.isNotEmpty()) {
                        val currentListIds = currentUnclearedList.map { it.id }
                        val isAllCurrentSelected = currentListIds.all { selectedIds.contains(it) }
                        Text(
                            text = if (isAllCurrentSelected) "لغو انتخاب همه" else "انتخاب همه",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            modifier = Modifier
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (isAllCurrentSelected) {
                                        onClearSelection()
                                    } else {
                                        onSelectAll((selectedIds + currentListIds).toList())
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (currentUnclearedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "موردی در این بخش وجود ندارد",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تمام تراکنش‌های این بخش تسویه شده‌اند یا ثبت نشده‌اند.",
                                fontSize = 12.sp,
                                color = TextMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(currentUnclearedList, key = { it.id }) { item ->
                            val isSelected = selectedIds.contains(item.id)
                            SettlementItemRow(
                                transaction = item,
                                isSelected = isSelected,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleSelection(item.id)
                                },
                                onEdit = { onEditTransaction(item) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(120.dp)) // padding for sticky bottom bar
                        }
                    }
                }
            } else {
                // Cleared History Tab
                if (clearedHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = TextMedium,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "سابقه تسویه‌ای موجود نیست",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "موارد پس از تسویه با صاحب مغازه به این تاریخچه منتقل خواهند شد.",
                                fontSize = 11.sp,
                                color = TextMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(clearedHistory, key = { it.id }) { item ->
                            ClearedHistoryItemRow(
                                transaction = item,
                                onEdit = { onEditTransaction(item) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
            }
        }

        // STICKY BOTTOM BAR
        if (selectedTab == 0 && unclearedItems.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = CardBackground,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مجموع انتخابی (${CurrencyFormatter.formatNumber(selectedIds.size)})",
                            fontSize = 11.sp,
                            color = TextMedium
                        )
                        Text(
                            text = CurrencyFormatter.format(totalSelectedAmount),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = if (selectedIds.isNotEmpty()) PrimaryGreen else TextDark
                        )
                    }

                    val canSettle = selectedIds.isNotEmpty()
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSettle()
                        },
                        enabled = canSettle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = Color(0xFFE2E8F0),
                            contentColor = CardBackground,
                            disabledContentColor = Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("settle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تسویه موارد انتخابی",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementItemRow(
    transaction: TransactionEntity,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    val isService = transaction.type == TransactionEntity.TYPE_SERVICE
    val isInflow = transaction.isIncoming

    // Distinct Theme Colors per Transaction Flow (مدیریت بصری)
    val accentColor = when {
        isService -> Color(0xFF8B5CF6) // Royal Purple for Services
        isInflow -> Color(0xFF10B981) // Emerald Green for Inflows / Deposits
        else -> Color(0xFFF97316) // Warm Amber / Orange for Expenses / Withdrawals
    }

    val badgeBg = when {
        isService -> Color(0xFFF3E8FF)
        isInflow -> PrimaryGreenLight
        else -> PrimaryOrangeLight
    }

    val amountColor = when {
        isService -> Color(0xFF7E22CE)
        isInflow -> Color(0xFF047857)
        else -> Color(0xFFC2410C)
    }

    val amountBoxBg = when {
        isService -> Color(0xFFF5EEFD)
        isInflow -> Color(0xFFECFDF5)
        else -> Color(0xFFFFF7ED)
    }

    // Different background tints so each type looks completely unique
    val defaultCardBg = when {
        isService -> Color(0xFFFAF8FF) // subtle purple tint
        isInflow -> Color(0xFFF7FEFA)  // subtle green tint
        else -> Color(0xFFFFFDF8)      // subtle warm tint
    }

    val cardBg = if (isSelected) Color(0xFFF0FDF4) else defaultCardBg
    val cardBorder = if (isSelected) {
        BorderStroke(2.dp, PrimaryGreen)
    } else {
        BorderStroke(
            1.dp,
            when {
                isService -> Color(0xFFDDD6FE)
                isInflow -> Color(0xFFA7F3D0)
                else -> Color(0xFFFED7AA)
            }
        )
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateStr = remember(transaction.date) {
        CurrencyFormatter.toPersianDigits(dateFormatter.format(Date(transaction.date)))
    }
    val timeStr = remember(transaction.date) {
        CurrencyFormatter.toPersianDigits(timeFormatter.format(Date(transaction.date)))
    }

    FintechCard(
        elevation = if (isSelected) 4.dp else 2.dp,
        backgroundColor = cardBg,
        border = cardBorder,
        shapeRadius = 16.dp,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Visual Accent Strip on Edge (نوار رنگی ضخیم عمودی برای تفکیک فوق‌العاده سریع چشمی)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // ==================== سطر اول (Line 1: Header / چک‌باکس، نشانگر نوع، بانک و برچسب وضعیت) ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Right: Checkbox + Type Badge + Bank Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Checkbox with accessible click target
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryGreen else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) PrimaryGreen else Color(0xFF94A3B8),
                                    shape = CircleShape
                                )
                                .clickable(onClick = onToggle)
                                .testTag("settlement_checkbox_${transaction.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "انتخاب شده",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Type Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isService -> Icons.Default.Build
                                        isInflow -> Icons.Default.ArrowDownward
                                        else -> Icons.Default.ArrowUpward
                                    },
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when {
                                        isService -> "خدمات مغازه"
                                        isInflow -> "واریزی مغازه"
                                        else -> "هزینه مغازه"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                        }

                        // Bank Badge (if not service)
                        if (!isService && !transaction.bankName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = transaction.bankName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                }
                            }
                        }
                    }

                    // Left: Selection Status Pill + Edit Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) PrimaryGreenLight else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryGreen else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.clickable(onClick = onToggle)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "آماده تسویه",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                } else {
                                    Text(
                                        text = "در انتظار تسویه",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMedium
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("edit_uncleared_btn_${transaction.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "ویرایش",
                                tint = TextMedium,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==================== سطر دوم (Line 2: Main / شرح معامله در راست و مبلغ درشت در چپ) ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Right: Description / Note
                    Text(
                        text = transaction.notes.ifBlank {
                            when {
                                isService -> "ارائه خدمات یا شیفت کاری برای مغازه"
                                isInflow -> "واریز وجه به حساب بانکی به نفع مغازه"
                                else -> "خرید قطعات یا هزینه انجام شده برای مغازه"
                            }
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        lineHeight = 20.sp,
                        maxLines = 2,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    )

                    // Left: Prominent Amount Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = amountBoxBg,
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = when {
                                isService -> CurrencyFormatter.format(transaction.amount)
                                isInflow -> "+ ${CurrencyFormatter.format(transaction.amount)}"
                                else -> "− ${CurrencyFormatter.format(transaction.amount)}"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = amountColor,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Divider line between core info and metadata
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF1F5F9))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ==================== سطر سوم (Line 3: Footer / تاریخ و ساعت و نشانگر اثر مالی بر مغازه) ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Right: Date & Time with icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TextMedium,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$dateStr • ساعت $timeStr",
                            fontSize = 11.sp,
                            color = TextMedium
                        )
                    }

                    // Left: Impact Tag on Store Debt / Credit
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isService -> Color(0xFFF3E8FF)
                            isInflow -> Color(0xFFF0FDF4)
                            else -> Color(0xFFFFF7ED)
                        }
                    ) {
                        Text(
                            text = when {
                                isService -> "📉 کاهش بدهی به مغازه"
                                isInflow -> "📈 طلب شما از مغازه"
                                else -> "📈 طلب شما از مغازه (خرجکرد)"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClearedHistoryItemRow(
    transaction: TransactionEntity,
    onEdit: () -> Unit
) {
    val isService = transaction.type == TransactionEntity.TYPE_SERVICE
    val isInflow = transaction.isIncoming
    val accentColor = when {
        isService -> Color(0xFF8B5CF6)
        isInflow -> Color(0xFF10B981)
        else -> Color(0xFFF97316)
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateStr = remember(transaction.date) {
        CurrencyFormatter.toPersianDigits(dateFormatter.format(Date(transaction.date)))
    }
    val timeStr = remember(transaction.date) {
        CurrencyFormatter.toPersianDigits(timeFormatter.format(Date(transaction.date)))
    }

    FintechCard(
        elevation = 2.dp,
        backgroundColor = CardBackground,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shapeRadius = 16.dp,
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Visual Accent Strip (Green for cleared)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF10B981))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Line 1: Type Badge + Bank Badge | Cleared Badge + Edit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isService -> Color(0xFFF3E8FF)
                                isInflow -> PrimaryGreenLight
                                else -> PrimaryOrangeLight
                            }
                        ) {
                            Text(
                                text = when {
                                    isService -> "خدمات مغازه"
                                    isInflow -> "واریزی مغازه"
                                    else -> "هزینه مغازه"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (!isService && !transaction.bankName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = transaction.bankName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryGreenLight,
                            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "تسویه شده رسمی",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                        }

                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("edit_cleared_btn_${transaction.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "ویرایش",
                                tint = TextMedium,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Line 2: Note / Description on right, Amount box on left
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = transaction.notes.ifBlank {
                            if (isService) "خدمت منظور شده در حساب مغازه" else "هزینه تسویه شده مغازه"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        lineHeight = 20.sp,
                        maxLines = 2,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = CurrencyFormatter.format(transaction.amount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF047857),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF1F5F9))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Line 3: Date & Note of Clearance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TextMedium,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$dateStr • ساعت $timeStr",
                            fontSize = 11.sp,
                            color = TextMedium
                        )
                    }

                    Text(
                        text = "تسویه شده با صاحب مغازه ✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryGreen
                    )
                }
            }
        }
    }
}
