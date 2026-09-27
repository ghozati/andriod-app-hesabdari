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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.model.ServiceEntity
import com.example.ui.components.FintechCard
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMedium
import com.example.ui.util.CurrencyFormatter

@Composable
fun ServicesScreen(
    services: List<ServiceEntity>,
    onAddNewService: (title: String, price: Double) -> Unit,
    onEditService: (id: Long, title: String, price: Double) -> Unit,
    onLogServiceForShop: (service: ServiceEntity, customPrice: Double?, notes: String) -> Unit,
    onDeleteService: (Long) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showAddDialog by remember { mutableStateOf(false) }
    var serviceToLog by remember { mutableStateOf<ServiceEntity?>(null) }
    var serviceToEdit by remember { mutableStateOf<ServiceEntity?>(null) }
    var serviceToDelete by remember { mutableStateOf<ServiceEntity?>(null) }

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
                    text = "خدمات",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "قانون ۴: خدمات معادل حقوق، بدهی شما به مغازه را کسر می‌کند",
                    fontSize = 11.sp,
                    color = TextMedium
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("add_service_catalog_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("خدمت جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PrimaryGreenLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚡ هر زمان تعمیری انجام دادید یا شیفت ایستادید، دکمه «ثبت برای مغازه» را بزنید تا مبلغ آن مستقیماً از بدهی شما کسر گردد.",
                fontSize = 11.sp,
                color = TextDark,
                lineHeight = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (services.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = TextMedium,
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "تعرفه‌ای تعریف نشده است",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "خدماتی مانند تعمیرات، نرم‌افزار، یا شیفت روزانه مغازه را اضافه کنید.",
                        fontSize = 12.sp,
                        color = TextMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(services, key = { it.id }) { service ->
                    ServiceItemCard(
                        service = service,
                        onLog = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            serviceToLog = service
                        },
                        onEdit = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            serviceToEdit = service
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            serviceToDelete = service
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }

    // Add Service Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var priceText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("افزودن خدمت به تعرفه‌ها", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان خدمت (مثال: تعویض سوکت شارژ)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                        label = { Text("تعرفه پیش‌فرض (تومان)") },
                        placeholder = { Text("مثال: 250,000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = priceText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && priceVal > 0) {
                            onAddNewService(title.trim(), priceVal)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text("ثبت خدمت")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Edit Service Dialog
    serviceToEdit?.let { svc ->
        var editTitle by remember(svc) { mutableStateOf(svc.title) }
        var editPriceText by remember(svc) { mutableStateOf(svc.defaultPrice.toLong().toString()) }

        AlertDialog(
            onDismissRequest = { serviceToEdit = null },
            title = { Text("ویرایش خدمت", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("عنوان خدمت") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editPriceText,
                        onValueChange = { editPriceText = it.filter { c -> c.isDigit() } },
                        label = { Text("تعرفه پیش‌فرض (تومان)") },
                        placeholder = { Text("مثال: 250,000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = editPriceText.toDoubleOrNull() ?: svc.defaultPrice
                        if (editTitle.isNotBlank() && priceVal > 0) {
                            onEditService(svc.id, editTitle.trim(), priceVal)
                            serviceToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("ذخیره تغییرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceToEdit = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Delete Service Confirmation Dialog
    serviceToDelete?.let { svc ->
        AlertDialog(
            onDismissRequest = { serviceToDelete = null },
            title = { Text("حذف خدمت", fontWeight = FontWeight.Bold) },
            text = {
                Text("آیا از حذف خدمت «${svc.title}» از لیست تعرفه‌ها اطمینان دارید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteService(svc.id)
                        serviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Log Service Dialog
    serviceToLog?.let { svc ->
        var customPriceText by remember { mutableStateOf(svc.defaultPrice.toLong().toString()) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { serviceToLog = null },
            title = { Text("ثبت خدمت برای مغازه", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "با ثبت این مورد، مبلغ خدمت به عنوان بستانکاری شما ثبت شده و از بدهی به مغازه کسر می‌شود.",
                        fontSize = 11.sp,
                        color = TextMedium,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = svc.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customPriceText,
                        onValueChange = { customPriceText = it.filter { c -> c.isDigit() } },
                        label = { Text("مبلغ منظور شده (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("توضیحات (اختیاری)") },
                        placeholder = { Text("مثال: دستگاه مشتری آقای احمدی") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = customPriceText.toDoubleOrNull() ?: svc.defaultPrice
                        onLogServiceForShop(svc, price, notes)
                        serviceToLog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("کسر از بدهی")
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceToLog = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun ServiceItemCard(
    service: ServiceEntity,
    onLog: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    FintechCard(
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Color(0xFF7E22CE),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = service.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.format(service.defaultPrice),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onLog,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("log_service_button_${service.id}")
                ) {
                    Text(
                        text = "ثبت برای مغازه",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_service_button_${service.id}")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "ویرایش خدمت",
                        tint = TextMedium,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_service_button_${service.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف خدمت",
                        tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
