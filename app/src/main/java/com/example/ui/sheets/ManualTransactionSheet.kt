package com.example.ui.sheets

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.ui.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManualTransactionSheet(
    categories: List<CategoryEntity>,
    projects: List<ProjectEntity>,
    bankAccounts: List<BankAccountEntity> = emptyList(),
    onAddTransaction: (amount: Double, isIncoming: Boolean, type: String, categoryId: Long?, projectId: Long?, bankId: Long?, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    var amountText by remember { mutableStateOf("") }
    var isIncoming by remember { mutableStateOf(false) } // Default: expense
    var selectedType by remember { mutableStateOf(TransactionEntity.TYPE_SHOP) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var selectedBankId by remember { mutableStateOf<Long?>(bankAccounts.firstOrNull()?.id) }
    var notes by remember { mutableStateOf("") }

    var projectDropdownExpanded by remember { mutableStateOf(false) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }

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
                Text(
                    text = "ثبت تراکنش دستی",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextMedium)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direction Toggle: Spent vs Received
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

            // Amount Input Field (in Tomans)
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                label = { Text("مبلغ به تومان") },
                placeholder = { Text("مثال: 450000") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_amount_input"),
                supportingText = {
                    val num = amountText.toDoubleOrNull()
                    if (num != null && num > 0) {
                        Text(
                            text = "معادل: ${CurrencyFormatter.format(num)}",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bank Account Selector (If banks are defined)
            if (bankAccounts.isNotEmpty()) {
                Text(
                    text = "انتخاب حساب بانکی:",
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
                        value = currentBank?.let { "${it.name}${if (it.accountNumber.isNotBlank()) " (${it.accountNumber})" else ""}" } ?: "بدون بانک",
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
                            text = { Text("بدون حساب بانکی مشخص") },
                            onClick = {
                                selectedBankId = null
                                bankDropdownExpanded = false
                            }
                        )
                        bankAccounts.forEach { bank ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${bank.name}${if (bank.accountNumber.isNotBlank()) " - شماره: ${bank.accountNumber}" else ""}"
                                    )
                                },
                                onClick = {
                                    selectedBankId = bank.id
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Type Selector: Shop vs Personal vs Transfer
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
                    TransactionEntity.TYPE_SHOP to "مغازه (امانی)",
                    TransactionEntity.TYPE_PERSONAL to "شخصی",
                    TransactionEntity.TYPE_TRANSFER to "انتقال داخلی"
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

            // Contextual Helper Box
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selectedType == TransactionEntity.TYPE_SHOP) PrimaryOrangeLight else PrimaryGreenLight
            ) {
                Text(
                    text = when (selectedType) {
                        TransactionEntity.TYPE_SHOP -> if (isIncoming)
                            "قانون ۱: دریافت پول مغازه در حساب شما، بدهی شما به مغازه را افزایش می‌دهد."
                        else
                            "قانون ۲: پرداخت هزینه برای مغازه، بدهی شما به مغازه را کسر می‌کند."
                        TransactionEntity.TYPE_PERSONAL -> "قانون ۳: هزینه یا واریز شخصی موجودی بانک را تغییر می‌دهد اما بدهی مغازه دست‌نخورده می‌ماند."
                        else -> "انتقال داخلی بین حساب‌ها بدون تغییر در مانده بدهی مغازه."
                    },
                    fontSize = 11.sp,
                    color = if (selectedType == TransactionEntity.TYPE_SHOP) AlertOrangeText else PrimaryGreen,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Categories (If Personal)
            if (selectedType == TransactionEntity.TYPE_PERSONAL) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("دسته‌بندی شخصی:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                Spacer(modifier = Modifier.height(6.dp))

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

            // Projects (Optional)
            if (projects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("اتصال به پروژه (اختیاری):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
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

            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("شرح و یادداشت") },
                placeholder = { Text("مثال: خرید قطعات برای مغازه") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            val amountVal = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amountVal > 0) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddTransaction(
                            amountVal,
                            isIncoming,
                            selectedType,
                            if (selectedType == TransactionEntity.TYPE_PERSONAL) selectedCategoryId else null,
                            selectedProjectId,
                            selectedBankId,
                            notes
                        )
                        onDismiss()
                    }
                },
                enabled = amountVal > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryOrange,
                    disabledContainerColor = PrimaryOrange.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_manual_transaction_button")
            ) {
                Text("ثبت تراکنش", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
