package com.example.ui.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankAccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.AlertOrangeText
import com.example.ui.theme.CardBackground
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeLight
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMedium

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTransactionSheet(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    projects: List<ProjectEntity>,
    bankAccounts: List<BankAccountEntity> = emptyList(),
    onSave: (amount: Double, isIncoming: Boolean, type: String, categoryId: Long?, projectId: Long?, bankId: Long?, notes: String) -> Unit,
    onDelete: (id: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    val initialAmountFormatted = remember(transaction.amount) {
        if (transaction.amount % 1.0 == 0.0) {
            transaction.amount.toLong().toString()
        } else {
            transaction.amount.toString()
        }
    }

    var amountText by remember { mutableStateOf(initialAmountFormatted) }
    var isIncoming by remember { mutableStateOf(transaction.isIncoming) }
    var selectedType by remember { mutableStateOf(transaction.type) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(transaction.categoryId) }
    var selectedProjectId by remember { mutableStateOf<Long?>(transaction.projectId) }
    var selectedBankId by remember {
        mutableStateOf<Long?>(
            transaction.bankId ?: bankAccounts.find { it.name == transaction.bankName }?.id
        )
    }
    var notes by remember { mutableStateOf(transaction.notes) }

    var projectDropdownExpanded by remember { mutableStateOf(false) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ویرایش تراکنش",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextMedium)
                }
            }

            // Settlement warning if currently cleared
            if (transaction.isCleared) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryOrangeLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "هشدار تسویه‌حساب: ویرایش مبلغ یا جهت این تراکنش باعث ابطال تسویه رسمی و بازگشت آن به وضعیت پرداخت‌نشده خواهد شد.",
                            fontSize = 11.sp,
                            color = AlertOrangeText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direction Toggle: Spent vs Received (Disabled if Service)
            if (selectedType != TransactionEntity.TYPE_SERVICE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryGreenLight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isIncoming) PrimaryOrange else CardBackground.copy(alpha = 0f),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable {
                                isIncoming = false
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (!isIncoming) CardBackground else TextMedium,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "هزینه / برداشت",
                                fontSize = 13.sp,
                                fontWeight = if (!isIncoming) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isIncoming) CardBackground else TextDark
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isIncoming) PrimaryGreen else CardBackground.copy(alpha = 0f),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable {
                                isIncoming = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isIncoming) CardBackground else TextMedium,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "دریافت / واریز",
                                fontSize = 13.sp,
                                fontWeight = if (isIncoming) FontWeight.Bold else FontWeight.Medium,
                                color = if (isIncoming) CardBackground else TextDark
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                label = { Text("مبلغ به تومان") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_amount_input")
            )

            // Bank Account Selector (Only for non-service transactions)
            if (selectedType != TransactionEntity.TYPE_SERVICE && bankAccounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "حساب بانکی مرتبط:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = bankDropdownExpanded,
                    onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded }
                ) {
                    val currentBank = bankAccounts.find { it.id == selectedBankId }
                    OutlinedTextField(
                        value = currentBank?.name ?: (transaction.bankName ?: "بدون بانک"),
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryGreen)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("بدون بانک") },
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
            }

            // Type Chips (Disabled if Service to preserve dual-ledger integrity)
            if (selectedType != TransactionEntity.TYPE_SERVICE) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "نوع تراکنش:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
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
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryGreen else PrimaryGreenLight.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
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
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryGreenLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "این تراکنش بابت خدمت یا شیفت کاری مغازه است و مستقیماً از بدهی مغازه کسر می‌شود (بدون گردش نقدی در بانک).",
                        fontSize = 11.sp,
                        color = PrimaryGreen,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Category Selection (Only if Personal)
            AnimatedVisibility(visible = selectedType == TransactionEntity.TYPE_PERSONAL) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "دسته‌بندی شخصی:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
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

            // Project Selector (Optional)
            if (projects.isNotEmpty() && selectedType != TransactionEntity.TYPE_SERVICE) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "اتصال به پروژه (اختیاری):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = projectDropdownExpanded,
                    onExpandedChange = { projectDropdownExpanded = !projectDropdownExpanded }
                ) {
                    val currentProject = projects.find { it.id == selectedProjectId }
                    OutlinedTextField(
                        value = currentProject?.name ?: "بدون پروژه",
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

            // Notes Field
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("توضیحات و یادداشت") },
                placeholder = { Text("توضیح کوتاه در مورد تراکنش...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_notes_input"),
                singleLine = true
            )

            // Save Button (Green #10B981)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val parsedAmount = amountText.toDoubleOrNull() ?: transaction.amount
                    if (parsedAmount > 0) {
                        onSave(
                            parsedAmount,
                            if (selectedType == TransactionEntity.TYPE_SERVICE) false else isIncoming,
                            selectedType,
                            if (selectedType == TransactionEntity.TYPE_PERSONAL) selectedCategoryId else null,
                            selectedProjectId,
                            if (selectedType == TransactionEntity.TYPE_SERVICE) null else selectedBankId,
                            notes
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_transaction_button")
            ) {
                Text(
                    text = "ذخیره تغییرات",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Delete Button (Red #EF4444)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDeleteConfirmDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFEF4444)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("delete_transaction_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "حذف تراکنش",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("تأیید حذف تراکنش", fontWeight = FontWeight.Bold) },
            text = { Text("آیا از حذف این تراکنش اطمینان دارید؟ تمام دفاتر و موجودی‌ها به طور خودکار دوباره محاسبه خواهند شد.") },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete(transaction.id)
                        showDeleteConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("بله، حذف کن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
