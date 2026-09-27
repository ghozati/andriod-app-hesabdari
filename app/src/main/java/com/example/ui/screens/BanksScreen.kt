package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankAccountEntity
import com.example.ui.components.FintechCard
import com.example.ui.theme.CardBackground
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeLight
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMedium
import com.example.ui.util.CurrencyFormatter
import com.example.viewmodel.BankBalanceStat

@Composable
fun BanksScreen(
    bankStats: List<BankBalanceStat>,
    totalBankBalance: Double,
    onAddNewBank: (name: String, accountNumber: String, initialBalance: Double) -> Unit,
    onEditBank: (id: Long, name: String, accountNumber: String, initialBalance: Double) -> Unit,
    onDeleteBank: (id: Long) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showAddDialog by remember { mutableStateOf(false) }
    var bankToEdit by remember { mutableStateOf<BankAccountEntity?>(null) }
    var bankToDelete by remember { mutableStateOf<BankBalanceStat?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مدیریت حساب‌های بانکی",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "تفکیک و کنترل جداگانه موجودی هر بانک (بلو، ملت و...)",
                    fontSize = 11.sp,
                    color = TextMedium
                )
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_bank_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("بانک جدید", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total Bank Summary Card
        FintechCard(elevation = 3.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "مجموع نقدینگی کل بانک‌ها",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "${CurrencyFormatter.formatNumber(bankStats.size)} حساب بانکی فعال",
                            fontSize = 11.sp,
                            color = TextMedium
                        )
                    }
                }

                Text(
                    text = CurrencyFormatter.format(totalBankBalance),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (totalBankBalance >= 0) PrimaryGreen else Color(0xFFEF4444)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (bankStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryOrangeLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "هنوز بانکی ثبت نشده است",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "برای تفکیک موجودی حساب‌ها (مثل بلوبانک، بانک ملت و...) دکمه «بانک جدید» را لمس کنید.",
                        fontSize = 12.sp,
                        color = TextMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("افزودن اولین بانک", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(bankStats, key = { it.bank.id }) { stat ->
                    BankStatItemCard(
                        stat = stat,
                        onEdit = {
                            bankToEdit = stat.bank
                        },
                        onDelete = {
                            bankToDelete = stat
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }

    // Add Bank Dialog
    if (showAddDialog) {
        BankFormDialog(
            title = "افزودن حساب بانکی جدید",
            initialName = "",
            initialAccountNumber = "",
            initialBalance = 0.0,
            onConfirm = { name, accNum, bal ->
                onAddNewBank(name, accNum, bal)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit Bank Dialog
    bankToEdit?.let { b ->
        BankFormDialog(
            title = "ویرایش حساب بانکی",
            initialName = b.name,
            initialAccountNumber = b.accountNumber,
            initialBalance = b.initialBalance,
            onConfirm = { name, accNum, bal ->
                onEditBank(b.id, name, accNum, bal)
                bankToEdit = null
            },
            onDismiss = { bankToEdit = null }
        )
    }

    // Delete Bank Confirmation Dialog
    bankToDelete?.let { stat ->
        AlertDialog(
            onDismissRequest = { bankToDelete = null },
            title = { Text("حذف بانک ${stat.bank.name}", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "آیا از حذف این حساب بانکی اطمینان دارید؟ تراکنش‌های متصل به این بانک بدون بانک ثبت خواهند شد.",
                    fontSize = 13.sp,
                    color = TextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteBank(stat.bank.id)
                        bankToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف بانک")
                }
            },
            dismissButton = {
                TextButton(onClick = { bankToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun BankStatItemCard(
    stat: BankBalanceStat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    FintechCard(elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stat.bank.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        if (stat.bank.accountNumber.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = TextMedium,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stat.bank.accountNumber,
                                    fontSize = 11.sp,
                                    color = TextMedium
                                )
                            }
                        } else {
                            Text(
                                text = "${CurrencyFormatter.formatNumber(stat.transactionCount)} تراکنش ثبت‌شده",
                                fontSize = 11.sp,
                                color = TextMedium
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = TextMedium, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Balance Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryGreenLight.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "موجودی فعلی بانک:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = CurrencyFormatter.format(stat.currentBalance),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = if (stat.currentBalance >= 0) PrimaryGreen else Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Inflow / Outflow & Initial Balance Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ورودی: +${CurrencyFormatter.format(stat.totalInflow)}",
                    fontSize = 11.sp,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "خروجی: −${CurrencyFormatter.format(stat.totalOutflow)}",
                    fontSize = 11.sp,
                    color = TextMedium,
                    fontWeight = FontWeight.Medium
                )
                if (stat.bank.initialBalance != 0.0) {
                    Text(
                        text = "موجودی اولیه: ${CurrencyFormatter.format(stat.bank.initialBalance)}",
                        fontSize = 11.sp,
                        color = TextMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun BankFormDialog(
    title: String,
    initialName: String,
    initialAccountNumber: String,
    initialBalance: Double,
    onConfirm: (name: String, accountNumber: String, initialBalance: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var accountNumber by remember { mutableStateOf(initialAccountNumber) }
    val initialBalanceStr = remember(initialBalance) {
        if (initialBalance == 0.0) "" else if (initialBalance % 1.0 == 0.0) initialBalance.toLong().toString() else initialBalance.toString()
    }
    var balanceText by remember { mutableStateOf(initialBalanceStr) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام بانک (مثال: بلوبانک، بانک ملت)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("شماره حساب یا ۴ رقم آخر کارت (اختیاری)") },
                    placeholder = { Text("مثال: 7703 یا 8291") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("موجودی اولیه به تومان (اختیاری)") },
                    placeholder = { Text("مثال: 50000000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val bal = balanceText.toDoubleOrNull() ?: 0.0
                        onConfirm(name.trim(), accountNumber.trim(), bal)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("ذخیره بانک", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
