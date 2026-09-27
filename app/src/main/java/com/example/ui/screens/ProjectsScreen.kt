package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.ProjectEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.FintechCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMedium
import com.example.ui.util.CurrencyFormatter
import com.example.viewmodel.ProjectStat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    projectStats: List<ProjectStat>,
    allTransactions: List<TransactionEntity>,
    onAddNewProject: (String) -> Unit,
    onToggleStatus: (id: Long, newStatus: String) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Active, 1 = Archived
    var showAddDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var inspectingProject by remember { mutableStateOf<ProjectStat?>(null) }

    val filteredStats = remember(projectStats, selectedTab) {
        val targetStatus = if (selectedTab == 0) ProjectEntity.STATUS_ACTIVE else ProjectEntity.STATUS_ARCHIVED
        projectStats.filter { it.project.status == targetStatus }
    }

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
                    text = "پروژه‌ها",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "تفکیک هزینه‌های سفرها، نمایشگاه و بازسازی",
                    fontSize = 12.sp,
                    color = TextMedium
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("add_project_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("پروژه جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: Active vs Archived
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
                text = { Text("پروژه‌های فعال", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                text = { Text("آرشیو شده", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = TextMedium,
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (selectedTab == 0) "پروژه فعالی تعریف نشده است" else "پروژه آرشیو شده‌ای وجود ندارد",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "برای گروه‌بندی هزینه‌های سفر کاری یا بازسازی، دکمه «پروژه جدید» را لمس کنید.",
                        fontSize = 12.sp,
                        color = TextMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredStats, key = { it.project.id }) { stat ->
                    ProjectCard(
                        stat = stat,
                        onClick = { inspectingProject = stat },
                        onToggleArchive = {
                            val newSt = if (stat.project.status == ProjectEntity.STATUS_ACTIVE) {
                                ProjectEntity.STATUS_ARCHIVED
                            } else {
                                ProjectEntity.STATUS_ACTIVE
                            }
                            onToggleStatus(stat.project.id, newSt)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }

    // Add Project Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("تعریف پروژه / تگ جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("نام پروژه را برای دسته‌بندی و تخصیص هزینه‌ها وارد کنید:", fontSize = 12.sp, color = TextMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        placeholder = { Text("مثال: نمایشگاه شیراز، خرید بار پاییزه") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectName.isNotBlank()) {
                            onAddNewProject(newProjectName.trim())
                            newProjectName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text("ایجاد پروژه")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Inspect Project Details Sheet
    inspectingProject?.let { stat ->
        val linkedTransactions = remember(stat, allTransactions) {
            allTransactions.filter { it.projectId == stat.project.id && it.status == TransactionEntity.STATUS_CONFIRMED }
        }
        ProjectDetailSheet(
            stat = stat,
            transactions = linkedTransactions,
            onEditTransaction = { tx ->
                inspectingProject = null
                onEditTransaction(tx)
            },
            onDismiss = { inspectingProject = null }
        )
    }
}

@Composable
private fun ProjectCard(
    stat: ProjectStat,
    onClick: () -> Unit,
    onToggleArchive: () -> Unit
) {
    FintechCard(
        elevation = 2.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stat.project.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "${CurrencyFormatter.formatNumber(stat.transactionCount)} تراکنش ثبت شده",
                            fontSize = 10.sp,
                            color = TextMedium
                        )
                    }
                }

                IconButton(onClick = onToggleArchive) {
                    Icon(
                        imageVector = if (stat.project.status == ProjectEntity.STATUS_ACTIVE) {
                            Icons.Default.Archive
                        } else {
                            Icons.Default.Unarchive
                        },
                        contentDescription = "آرشیو",
                        tint = TextMedium,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Values: Spent, Received, Net Cost
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BorderLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Spent (Orange down arrow)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("کل هزینه", fontSize = 10.sp, color = TextMedium)
                    }
                    Text(
                        text = CurrencyFormatter.format(stat.totalSpent),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryOrange
                    )
                }

                // Total Received / Split (Green up arrow)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("دریافتی / دنگی", fontSize = 10.sp, color = TextMedium)
                    }
                    Text(
                        text = CurrencyFormatter.format(stat.totalReceived),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }

                // Net Cost
                Column(horizontalAlignment = Alignment.End) {
                    Text("هزینه خالص", fontSize = 10.sp, color = TextMedium)
                    Text(
                        text = CurrencyFormatter.format(stat.netCost),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectDetailSheet(
    stat: ProjectStat,
    transactions: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                Column {
                    Text(
                        text = stat.project.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "هزینه خالص: ${CurrencyFormatter.format(stat.netCost)}",
                        fontSize = 12.sp,
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextMedium)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("تراکنش‌های مرتبط با این پروژه:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Text("هنوز تراکنشی به این پروژه متصل نشده است.", fontSize = 12.sp, color = TextMedium)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { tx ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditTransaction(tx) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.notes.ifBlank { "تراکنش ${tx.type}" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                    Text(
                                        text = when (tx.type) {
                                            TransactionEntity.TYPE_SHOP -> "مغازه"
                                            TransactionEntity.TYPE_PERSONAL -> "شخصی"
                                            TransactionEntity.TYPE_SERVICE -> "خدمات"
                                            else -> "انتقال"
                                        },
                                        fontSize = 10.sp,
                                        color = TextMedium
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${if (tx.isIncoming) "+ " else "− "}${CurrencyFormatter.format(tx.amount)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tx.isIncoming) PrimaryGreen else PrimaryOrange
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { onEditTransaction(tx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "ویرایش",
                                            tint = TextMedium,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
