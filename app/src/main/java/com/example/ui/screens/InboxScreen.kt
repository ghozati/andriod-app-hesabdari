package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankAccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.FintechCard
import com.example.ui.theme.AlertOrangeText
import com.example.ui.theme.AppBackground
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    pendingTransactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    projects: List<ProjectEntity>,
    bankAccounts: List<BankAccountEntity> = emptyList(),
    onBack: () -> Unit,
    onConfirm: (id: Long, type: String, categoryId: Long?, projectId: Long?, bankId: Long?, notes: String) -> Unit,
    onDelete: (id: Long) -> Unit,
    onDismissAll: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showDismissAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // App Bar
        Surface(
            color = CardBackground,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowForward, // RTL back arrow points forward/right
                            contentDescription = "بازگشت",
                            tint = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = "صندوق پیامک‌های بررسی نشده",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "شما ${CurrencyFormatter.formatNumber(pendingTransactions.size)} تراکنش تعیین‌تکلیف نشده دارید",
                            fontSize = 11.sp,
                            color = TextMedium
                        )
                    }
                }

                if (pendingTransactions.isNotEmpty()) {
                    androidx.compose.material3.TextButton(
                        onClick = { showDismissAllDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("dismiss_all_pending_btn")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "رد کردن همه",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Dismiss All Confirmation Dialog
        if (showDismissAllDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDismissAllDialog = false },
                title = { Text("رد کردن تمام موارد", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "آیا از رد کردن و حذف تمام موارد بررسی‌نشده اطمینان دارید؟ تمام این موارد بدون ثبت در دفاتر حذف خواهند شد.",
                        fontSize = 13.sp,
                        color = TextDark
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismissAll()
                            showDismissAllDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("رد کردن و پاکسازی")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDismissAllDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }

        if (pendingTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "موردی در ثبت موقت وجود ندارد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تمام تراکنش‌ها، پیامک‌ها و خدمات تعیین تکلیف شده‌اند.",
                        fontSize = 12.sp,
                        color = TextMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("بازگشت به داشبورد", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryOrangeLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Sms,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تراکنش‌های ورودی یا دستی ابتدا در اینجا قرار می‌گیرند. پس از تأیید شما، در دفاتر و موجودی بانک ثبت خواهند شد.",
                                fontSize = 11.sp,
                                color = AlertOrangeText,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                items(pendingTransactions, key = { it.id }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDelete(item.id)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = false,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFEF4444))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "حذف",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    ) {
                        PendingTransactionCard(
                            transaction = item,
                            categories = categories,
                            projects = projects,
                            bankAccounts = bankAccounts,
                            onConfirm = { type, catId, prjId, bankId, notes ->
                                onConfirm(item.id, type, catId, prjId, bankId, notes)
                            },
                            onDelete = {
                                onDelete(item.id)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PendingTransactionCard(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    projects: List<ProjectEntity>,
    bankAccounts: List<BankAccountEntity>,
    onConfirm: (type: String, categoryId: Long?, projectId: Long?, bankId: Long?, notes: String) -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedType by remember { mutableStateOf(transaction.type) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var selectedBankId by remember {
        mutableStateOf<Long?>(
            transaction.bankId ?: bankAccounts.find { it.name == transaction.bankName }?.id
        )
    }
    var notesText by remember { mutableStateOf(transaction.notes) }
    var projectDropdownExpanded by remember { mutableStateOf(false) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEEE d MMMM - HH:mm", Locale.getDefault()) }
    val dateStr = remember(transaction.date) {
        CurrencyFormatter.toPersianDigits(dateFormatter.format(Date(transaction.date)))
    }

    FintechCard(
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Amount & Bank & Quick Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (transaction.isIncoming) PrimaryGreenLight else PrimaryOrangeLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (transaction.isIncoming) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (transaction.isIncoming) PrimaryGreen else PrimaryOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = transaction.bankName ?: "پیامک بانکی",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = dateStr,
                            fontSize = 10.sp,
                            color = TextMedium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (transaction.isIncoming) "+ " else "− "}${CurrencyFormatter.format(transaction.amount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (transaction.isIncoming) PrimaryGreen else TextDark
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("quick_reject_btn_${transaction.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "رد کردن",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Raw SMS text snippet
            if (!transaction.rawSms.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BorderLight.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💬 «${CurrencyFormatter.toPersianDigits(transaction.rawSms)}»",
                        fontSize = 11.sp,
                        color = TextMedium,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bank Account Selector (If banks exist)
            if (bankAccounts.isNotEmpty() && selectedType != TransactionEntity.TYPE_SERVICE) {
                Text(
                    text = "حساب بانکی:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = bankDropdownExpanded,
                    onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded }
                ) {
                    val currentBank = bankAccounts.find { it.id == selectedBankId }
                    OutlinedTextField(
                        value = currentBank?.name ?: (transaction.bankName ?: "انتخاب بانک"),
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryGreen)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("بدون بانک مشخص") },
                            onClick = {
                                selectedBankId = null
                                bankDropdownExpanded = false
                            }
                        )
                        bankAccounts.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text("${bank.name}${if (bank.accountNumber.isNotBlank()) " (${bank.accountNumber})" else ""}") },
                                onClick = {
                                    selectedBankId = bank.id
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3 Main Chips: [Shop], [Personal], [Transfer]
            Text(
                text = "تعیین نوع تراکنش:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    TransactionEntity.TYPE_SHOP to "مغازه",
                    TransactionEntity.TYPE_PERSONAL to "شخصی",
                    TransactionEntity.TYPE_TRANSFER to "انتقال"
                ).forEach { (typeVal, label) ->
                    val isSelected = selectedType == typeVal
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryGreen else PrimaryGreenLight,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("inbox_chip_${typeVal.lowercase()}")
                            .clickable {
                                selectedType = typeVal
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CardBackground else TextDark
                            )
                        }
                    }
                }
            }

            // Dynamic Category Row: Expands right inside card if [Personal] is selected
            AnimatedVisibility(visible = selectedType == TransactionEntity.TYPE_PERSONAL) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "دسته‌بندی هزینه شخصی:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isCatSelected = selectedCategoryId == cat.id
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isCatSelected) PrimaryGreen else PrimaryGreenLight,
                                modifier = Modifier
                                    .height(38.dp)
                                    .clickable {
                                        selectedCategoryId = if (isCatSelected) null else cat.id
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    if (isCatSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = CardBackground, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = cat.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isCatSelected) CardBackground else TextDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Project Dropdown (Optional)
            if (projects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                ExposedDropdownMenuBox(
                    expanded = projectDropdownExpanded,
                    onExpandedChange = { projectDropdownExpanded = !projectDropdownExpanded }
                ) {
                    val currentProject = projects.find { it.id == selectedProjectId }
                    OutlinedTextField(
                        value = currentProject?.name ?: "انتساب به پروژه (اختیاری)",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = projectDropdownExpanded,
                        onDismissRequest = { projectDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("بدون پروژه") },
                            onClick = {
                                selectedProjectId = null
                                projectDropdownExpanded = false
                            }
                        )
                        projects.forEach { prj ->
                            DropdownMenuItem(
                                text = { Text(prj.name) },
                                onClick = {
                                    selectedProjectId = prj.id
                                    projectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Notes Text Field
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("یادداشت / توضیحات") },
                placeholder = { Text("توضیحات کوتاه برای بایگانی...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Action Buttons: Confirm OR Discard / Reject
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Confirm Button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfirm(
                            selectedType,
                            if (selectedType == TransactionEntity.TYPE_PERSONAL) selectedCategoryId else null,
                            selectedProjectId,
                            if (selectedType == TransactionEntity.TYPE_SERVICE) null else selectedBankId,
                            notesText
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
                        .testTag("confirm_inbox_button_${transaction.id}")
                ) {
                    Text(
                        text = "تأیید و ثبت در دفاتر",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Discard / Reject Button
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFEF2F2),
                        contentColor = Color(0xFFDC2626)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("discard_inbox_button_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "رد کردن",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
