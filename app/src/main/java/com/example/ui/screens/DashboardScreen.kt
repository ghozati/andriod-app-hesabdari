package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.FintechCard
import com.example.ui.theme.AlertOrangeText
import com.example.ui.theme.CardBackground
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeLight
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMedium
import com.example.ui.theme.WarningYellowBg
import com.example.ui.theme.WarningYellowBorder
import com.example.ui.theme.WarningYellowText
import com.example.ui.util.CurrencyFormatter
import com.example.viewmodel.BankBalanceStat
import com.example.viewmodel.FinancialLedgerSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    summary: FinancialLedgerSummary,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    projects: List<ProjectEntity>,
    bankStats: List<BankBalanceStat> = emptyList(),
    onOpenInbox: () -> Unit,
    onOpenBanks: () -> Unit = {},
    onAssignCategory: (TransactionEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTransactions = remember(transactions, selectedFilter) {
        val confirmed = transactions.filter { it.status == TransactionEntity.STATUS_CONFIRMED }
        when (selectedFilter) {
            "SHOP" -> confirmed.filter { it.type == TransactionEntity.TYPE_SHOP }
            "PERSONAL" -> confirmed.filter { it.type == TransactionEntity.TYPE_PERSONAL }
            "SERVICE" -> confirmed.filter { it.type == TransactionEntity.TYPE_SERVICE }
            else -> confirmed
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Modern Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            ModernDashboardHeader()
        }

        // 2. Pending SMS Banner (Solid #FFF0E6 with NO border) or Empty State
        item {
            if (summary.pendingInboxCount > 0) {
                SolidPendingAlertBanner(
                    pendingCount = summary.pendingInboxCount,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenInbox()
                    }
                )
            } else {
                EmptyPendingSmsBanner(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenInbox()
                    }
                )
            }
        }

        // 3. Balance Cards (Pure White with diffuse shadows, NO borders, Extra Bold amounts)
        item {
            BalanceCardsSection(
                summary = summary,
                bankStats = bankStats,
                onOpenBanks = onOpenBanks
            )
        }

        // 4. Filter Tabs and Section Title
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تراکنش‌های اخیر",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "${CurrencyFormatter.formatNumber(filteredTransactions.size)} مورد",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMedium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Modern smooth pill tabs without harsh borders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBackground)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf(
                        "ALL" to "همه",
                        "SHOP" to "مغازه",
                        "PERSONAL" to "شخصی",
                        "SERVICE" to "خدمات"
                    ).forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PrimaryGreenLight else Color.Transparent)
                                .clickable {
                                    selectedFilter = key
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PrimaryGreen else TextMedium
                            )
                        }
                    }
                }
            }
        }

        // 5. Recent Transactions List or Beautiful Empty State
        if (filteredTransactions.isEmpty()) {
            item {
                EmptyTransactionsCard()
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                val category = categories.find { it.id == tx.categoryId }
                val project = projects.find { it.id == tx.projectId }

                TransactionItemCard(
                    transaction = tx,
                    category = category,
                    project = project,
                    onAssignCategory = { onAssignCategory(tx) },
                    onClick = { onEditTransaction(tx) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

/**
 * Modern Clean Dashboard Header with Avatar Badge
 */
@Composable
private fun ModernDashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "داشبورد حسابداری",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "تفکیک هوشمند وجوه مشاع و شخصی",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextMedium
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PrimaryGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = "مغازه",
                tint = PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Pending SMS Card:
 * Solid, vibrant, soft Orange background (#FFF0E6) with NO border.
 */
@Composable
private fun SolidPendingAlertBanner(
    pendingCount: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = PrimaryOrangeLight, // #FFF0E6
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_inbox_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = PrimaryOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "صندوق پیامک‌های بررسی نشده",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlertOrangeText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "شما ${CurrencyFormatter.formatNumber(pendingCount)} تراکنش تعیین‌تکلیف نشده دارید",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AlertOrangeText
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "برای بررسی لمس کنید",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AlertOrangeText.copy(alpha = 0.85f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, // RTL back arrow points left into content
                    contentDescription = "بررسی",
                    tint = PrimaryOrange,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Empty State for Pending SMS
 */
@Composable
private fun EmptyPendingSmsBanner(
    onClick: () -> Unit
) {
    FintechCard(
        elevation = 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "صندوق پیامک‌های بررسی نشده",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "پیامک بررسی‌نشده‌ای وجود ندارد",
                        fontSize = 11.sp,
                        color = TextMedium
                    )
                }
            }

            Text(
                text = "مشاهده",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreen
            )
        }
    }
}

/**
 * Balance Cards:
 * Pure White (#FFFFFF) with NO borders and soft elevation.
 * Amounts are Extra Bold and much larger for clear visual hierarchy.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BalanceCardsSection(
    summary: FinancialLedgerSummary,
    bankStats: List<BankBalanceStat> = emptyList(),
    onOpenBanks: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Card 1: Real Bank Balance (Pure White Card + Green Icon)
        FintechCard(elevation = 3.dp) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "موجودی واقعی بانک‌ها",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "مجموع کل موجودی حساب‌های شخصی و مغازه",
                                fontSize = 11.sp,
                                color = TextMedium
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryGreenLight
                    ) {
                        Text(
                            text = "نقدینگی کل",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Extra Bold, much larger amount (ASCII numerals + تومان)
                Text(
                    text = CurrencyFormatter.format(summary.realBankBalance),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (summary.realBankBalance >= 0) PrimaryGreen else Color(0xFFEF4444)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown of individual banks
                if (bankStats.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenBanks),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تفکیک بر اساس حساب‌های بانکی:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "مدیریت حساب‌ها ❯",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bankStats.forEach { stat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.clickable(onClick = onOpenBanks)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${stat.bank.name}: ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(stat.currentBalance),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (stat.currentBalance >= 0) PrimaryGreen else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenBanks)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حساب بانکی تعریف نشده (بلو، ملت و...)",
                                fontSize = 11.sp,
                                color = TextMedium
                            )
                            Text(
                                text = "تعریف بانک +",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                    }
                }
            }
        }

        // Card 2: Debt to Shop (Pure White Card + Orange Icon, NO borders)
        FintechCard(elevation = 3.dp) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryOrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "بدهی به مغازه",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "دفتر حساب وجوه امانی مغازه",
                                fontSize = 11.sp,
                                color = TextMedium
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryOrangeLight
                    ) {
                        Text(
                            text = "بدهکار به مغازه",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "بدهی لحظه‌ای",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Extra Bold, much larger amount in Orange
                        Text(
                            text = CurrencyFormatter.format(summary.instantShopDebt),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryOrange
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "تسویه شده رسمی",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.format(summary.officiallyClearedDebt),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "هزینه‌های منتظر تسویه: ${CurrencyFormatter.format(summary.pendingSettlementAmount)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                    Text(
                        text = "ارزش خدمات: ${CurrencyFormatter.format(summary.totalServicesValue)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryGreen
                    )
                }
            }
        }
    }
}

/**
 * Individual Transaction Item Card (Pure White with NO border)
 */
@Composable
private fun TransactionItemCard(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    project: ProjectEntity?,
    onAssignCategory: () -> Unit,
    onClick: () -> Unit
) {
    val isIncoming = transaction.isIncoming
    val isService = transaction.type == TransactionEntity.TYPE_SERVICE
    val isPersonal = transaction.type == TransactionEntity.TYPE_PERSONAL
    val hasNoCategory = isPersonal && transaction.categoryId == null

    val dateFormatter = remember { SimpleDateFormat("d MMMM - HH:mm", Locale.getDefault()) }
    val dateStr = remember(transaction.date) {
        dateFormatter.format(Date(transaction.date))
    }

    FintechCard(
        elevation = 2.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Right Icon & Description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isService -> Color(0xFFF3E8FF)
                                    isIncoming -> PrimaryGreenLight
                                    else -> PrimaryOrangeLight
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isService -> Icons.Default.Build
                                isIncoming -> Icons.Default.ArrowUpward
                                else -> Icons.Default.ArrowDownward
                            },
                            contentDescription = null,
                            tint = when {
                                isService -> Color(0xFF7E22CE)
                                isIncoming -> PrimaryGreen
                                else -> PrimaryOrange
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (transaction.type) {
                                    TransactionEntity.TYPE_SHOP -> "مغازه"
                                    TransactionEntity.TYPE_PERSONAL -> "شخصی"
                                    TransactionEntity.TYPE_SERVICE -> "خدمات"
                                    else -> "انتقال"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            if (transaction.isCleared) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "تسویه شده",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Text(
                            text = transaction.notes.ifBlank {
                                transaction.bankName ?: "تراکنش بانکی"
                            },
                            fontSize = 11.sp,
                            color = TextMedium,
                            maxLines = 1
                        )
                    }
                }

                // Left Amount & Date & Edit button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (isIncoming) "+ " else "− "}${CurrencyFormatter.format(transaction.amount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isService -> Color(0xFF7E22CE)
                                isIncoming -> PrimaryGreen
                                else -> TextDark
                            }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = dateStr,
                            fontSize = 10.sp,
                            color = TextMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_tx_btn_${transaction.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش تراکنش",
                            tint = TextLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Warning Chip: No Category
            if (hasNoCategory) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarningYellowBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningYellowBorder),
                        modifier = Modifier
                            .testTag("no_category_pill_${transaction.id}")
                            .clickable(onClick = onAssignCategory)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "بدون دسته‌بندی ⚠️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningYellowText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "انتخاب دسته‌بندی ❯",
                                fontSize = 10.sp,
                                color = WarningYellowText
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bank badge (if not service)
                    if (!isService && !transaction.bankName.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(11.dp)
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
                    } else if (isService) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF3E8FF)
                        ) {
                            Text(
                                text = "خدمت مغازه (بدون گردش بانکی)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF7E22CE),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (category != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryGreenLight
                        ) {
                            Text(
                                text = category.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (project != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "📁 ${project.name}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Beautiful Empty State for Recent Transactions:
 * "تراکنشی وجود ندارد"
 */
@Composable
private fun EmptyTransactionsCard() {
    FintechCard(elevation = 1.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = TextLight,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "تراکنشی وجود ندارد",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "برای افزودن تراکنش روی دکمه + پایین صفحه ضربه بزنید یا پیامک‌های بانکی جدید دریافت کنید.",
                fontSize = 12.sp,
                color = TextMedium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
