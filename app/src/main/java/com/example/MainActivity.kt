package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TransactionEntity
import com.example.ui.screens.BanksScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.SettlementScreen
import com.example.ui.sheets.EditTransactionSheet
import com.example.ui.sheets.FabBottomSheet
import com.example.ui.sheets.InitialSetupSheet
import com.example.ui.sheets.ManualTransactionSheet
import com.example.ui.sheets.QuickCategoryPickerSheet
import com.example.ui.sheets.SimulateSmsSheet
import com.example.ui.theme.CardBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavInactive
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.TextDark
import com.example.ui.util.CurrencyFormatter
import com.example.viewmodel.AccountingViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openInboxFromNotification = intent?.getBooleanExtra("OPEN_INBOX", false) ?: false

        setContent {
            // Force RTL across the entire application and Force Light theme
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MyApplicationTheme(darkTheme = false) {
                    val app = application as ShopLedgerApplication
                    val viewModel: AccountingViewModel = viewModel(
                        factory = AccountingViewModel.provideFactory(app.repository)
                    )

                    ShopLedgerApp(
                        viewModel = viewModel,
                        initialOpenInbox = openInboxFromNotification
                    )
                }
            }
        }
    }
}

@Composable
fun ShopLedgerApp(
    viewModel: AccountingViewModel,
    initialOpenInbox: Boolean = false
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // State
    val summary by viewModel.ledgerSummary.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val pendingTransactions by viewModel.pendingTransactions.collectAsStateWithLifecycle()
    val unclearedShopItems by viewModel.unclearedShopItems.collectAsStateWithLifecycle()
    val clearedHistory by viewModel.clearedShopItems.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val projectStats by viewModel.projectStats.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    val bankStats by viewModel.bankStats.collectAsStateWithLifecycle()
    val selectedSettlementIds by viewModel.selectedSettlementIds.collectAsStateWithLifecycle()

    // Navigation state: 0 = داشبورد, 1 = تسویه‌حساب, 2 = بانک‌ها, 3 = خدمات, 4 = پروژه‌ها
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var showInboxFlow by rememberSaveable { mutableStateOf(initialOpenInbox) }

    // Sheets state
    var showFabMenu by remember { mutableStateOf(false) }
    var showManualTransactionSheet by remember { mutableStateOf(false) }
    var showInitialSetupSheet by remember { mutableStateOf(false) }
    var showSimulateSmsSheet by remember { mutableStateOf(false) }
    var categoryPickerTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    if (showInboxFlow) {
        InboxScreen(
            pendingTransactions = pendingTransactions,
            categories = categories,
            projects = projects,
            bankAccounts = bankAccounts,
            onBack = { showInboxFlow = false },
            onConfirm = { id, type, catId, prjId, bankId, notes ->
                viewModel.confirmPendingTransaction(id, type, catId, prjId, bankId, notes)
            },
            onDelete = { id ->
                viewModel.dismissOrDeletePending(id)
                Toast.makeText(context, "پیامک رد شد و نادیده گرفته شد", Toast.LENGTH_SHORT).show()
            },
            onDismissAll = {
                viewModel.dismissAllPending()
                Toast.makeText(context, "تمام پیامک‌های بررسی‌نشده رد شدند", Toast.LENGTH_SHORT).show()
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                // Bottom Navigation: Pure white background, active item uses #10B981, inactive uses #9CA3AF. No labels in English.
                NavigationBar(
                    containerColor = CardBackground,
                    contentColor = TextDark,
                    tonalElevation = 6.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val tabs = listOf(
                        Triple(0, "داشبورد", Icons.Default.Home),
                        Triple(1, "تسویه‌حساب", Icons.Default.ReceiptLong),
                        Triple(2, "بانک‌ها", Icons.Default.AccountBalance),
                        Triple(3, "خدمات", Icons.Default.Build),
                        Triple(4, "پروژه‌ها", Icons.Default.Folder)
                    )

                    tabs.forEach { (index, title, icon) ->
                        val isSelected = currentTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentTab = index
                            },
                            icon = {
                                if (index == 0 && summary.pendingInboxCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = PrimaryOrange) {
                                                Text(
                                                    text = CurrencyFormatter.formatNumber(summary.pendingInboxCount),
                                                    color = Color.White,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else if (index == 1 && unclearedShopItems.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = PrimaryGreen) {
                                                Text(
                                                    text = CurrencyFormatter.formatNumber(unclearedShopItems.size),
                                                    color = Color.White,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryGreen,
                                selectedTextColor = PrimaryGreen,
                                unselectedIconColor = NavInactive,
                                unselectedTextColor = NavInactive,
                                indicatorColor = PrimaryGreenLight
                            ),
                            modifier = Modifier.testTag("nav_tab_${index}")
                        )
                    }
                }
            },
            floatingActionButton = {
                // Large Centered FAB in Orange (#F97316)
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showFabMenu = true
                    },
                    containerColor = PrimaryOrange,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    modifier = Modifier
                        .size(58.dp)
                        .testTag("fab_main")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "عملیات جدید",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardScreen(
                            summary = summary,
                            transactions = allTransactions,
                            categories = categories,
                            projects = projects,
                            bankStats = bankStats,
                            onOpenInbox = { showInboxFlow = true },
                            onOpenBanks = { currentTab = 2 },
                            onAssignCategory = { tx -> categoryPickerTransaction = tx },
                            onEditTransaction = { tx -> editingTransaction = tx }
                        )
                        1 -> SettlementScreen(
                            unclearedItems = unclearedShopItems,
                            clearedHistory = clearedHistory,
                            selectedIds = selectedSettlementIds,
                            onToggleSelection = { id -> viewModel.toggleSettlementSelection(id) },
                            onSelectAll = { ids -> viewModel.selectAllSettlement(ids) },
                            onClearSelection = { viewModel.clearSettlementSelection() },
                            onSettle = { viewModel.settleSelectedItems() },
                            onEditTransaction = { tx -> editingTransaction = tx }
                        )
                        2 -> BanksScreen(
                            bankStats = bankStats,
                            totalBankBalance = summary.realBankBalance,
                            onAddNewBank = { name, accNum, bal ->
                                viewModel.addNewBank(name, accNum, bal)
                                Toast.makeText(context, "حساب بانکی $name با موفقیت افزوده شد", Toast.LENGTH_SHORT).show()
                            },
                            onEditBank = { id, name, accNum, bal ->
                                viewModel.updateBank(id, name, accNum, bal)
                                Toast.makeText(context, "اطلاعات بانک با موفقیت بروزرسانی شد", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteBank = { id ->
                                viewModel.deleteBank(id)
                                Toast.makeText(context, "حساب بانکی حذف شد", Toast.LENGTH_SHORT).show()
                            }
                        )
                        3 -> ServicesScreen(
                            services = services,
                            onAddNewService = { title, price ->
                                viewModel.addNewService(title, price)
                                Toast.makeText(context, "خدمت جدید با موفقیت افزوده شد", Toast.LENGTH_SHORT).show()
                            },
                            onEditService = { id, title, price ->
                                viewModel.updateService(id, title, price)
                                Toast.makeText(context, "خدمت با موفقیت ویرایش شد", Toast.LENGTH_SHORT).show()
                            },
                            onLogServiceForShop = { service, customPrice, notes ->
                                viewModel.logServiceForShop(service, customPrice, notes)
                                Toast.makeText(context, "خدمت برای مغازه ثبت و از بدهی کسر گردید", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteService = { id ->
                                viewModel.deleteService(id)
                                Toast.makeText(context, "خدمت حذف شد", Toast.LENGTH_SHORT).show()
                            }
                        )
                        4 -> ProjectsScreen(
                            projectStats = projectStats,
                            allTransactions = allTransactions,
                            onAddNewProject = { name -> viewModel.addNewProject(name) },
                            onToggleStatus = { id, status -> viewModel.updateProjectStatus(id, status) },
                            onEditTransaction = { tx -> editingTransaction = tx }
                        )
                    }
                }
            }
        }
    }

    // --- Bottom Sheets ---

    // 1. FAB Options Menu
    if (showFabMenu) {
        FabBottomSheet(
            onOptionSelected = { option ->
                showFabMenu = false
                when (option) {
                    "ADD_SERVICE" -> {
                        currentTab = 3
                    }
                    "MANAGE_BANKS" -> {
                        currentTab = 2
                    }
                    "MANUAL_TRANSACTION" -> {
                        showManualTransactionSheet = true
                    }
                    "INITIAL_SETUP" -> {
                        showInitialSetupSheet = true
                    }
                    "SIMULATE_SMS" -> {
                        showSimulateSmsSheet = true
                    }
                }
            },
            onDismiss = { showFabMenu = false }
        )
    }

    // 2. Manual Transaction Sheet
    if (showManualTransactionSheet) {
        ManualTransactionSheet(
            categories = categories,
            projects = projects,
            bankAccounts = bankAccounts,
            onAddTransaction = { amount, isIncoming, type, categoryId, projectId, bankId, notes ->
                viewModel.addManualTransaction(amount, isIncoming, type, categoryId, projectId, bankId, notes)
                Toast.makeText(context, "تراکنش به صف ثبت موقت افزوده شد", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showManualTransactionSheet = false }
        )
    }

    // 3. Initial Setup Sheet
    if (showInitialSetupSheet) {
        InitialSetupSheet(
            onSave = { bankBal, debt ->
                viewModel.setInitialBalances(bankBal, debt)
            },
            onDismiss = { showInitialSetupSheet = false }
        )
    }

    // 4. Simulate Bank SMS Sheet
    if (showSimulateSmsSheet) {
        SimulateSmsSheet(
            onSimulate = { smsText, bankName ->
                viewModel.simulateBankSms(smsText, bankName) { success ->
                    if (success) {
                        showInboxFlow = true
                    } else {
                        Toast.makeText(
                            context,
                            "این پیامک به عنوان پیامک معتبر بانکی شناسایی نشد (فیلتر پیامک غیربانکی)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            onDismiss = { showSimulateSmsSheet = false }
        )
    }

    // 5. Quick Category Picker
    categoryPickerTransaction?.let { tx ->
        QuickCategoryPickerSheet(
            transaction = tx,
            categories = categories,
            onCategorySelected = { catId ->
                viewModel.assignCategoryToTransaction(tx.id, catId)
                categoryPickerTransaction = null
            },
            onAddNewCategory = { name ->
                viewModel.addNewCategory(name)
            },
            onDelete = {
                viewModel.deleteTransaction(tx.id)
                categoryPickerTransaction = null
                Toast.makeText(context, "تراکنش رد شد و حذف گردید", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { categoryPickerTransaction = null }
        )
    }

    // 6. Edit Transaction Sheet (Full CRUD with State Recalculation)
    editingTransaction?.let { tx ->
        EditTransactionSheet(
            transaction = tx,
            categories = categories,
            projects = projects,
            bankAccounts = bankAccounts,
            onSave = { amount, isIncoming, type, categoryId, projectId, bankId, notes ->
                viewModel.updateTransactionDetails(
                    id = tx.id,
                    amount = amount,
                    isIncoming = isIncoming,
                    type = type,
                    categoryId = categoryId,
                    projectId = projectId,
                    bankId = bankId,
                    notes = notes
                ) { revertedSettlement ->
                    if (revertedSettlement) {
                        Toast.makeText(context, "تراکنش ویرایش شد و از حالت تسویه خارج گردید", Toast.LENGTH_LONG).show()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تراکنش ویرایش شد و از حالت تسویه خارج گردید")
                        }
                    } else {
                        Toast.makeText(context, "تغییرات با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                    }
                }
                editingTransaction = null
            },
            onDelete = { id ->
                viewModel.deleteTransaction(id)
                Toast.makeText(context, "تراکنش حذف شد", Toast.LENGTH_SHORT).show()
                editingTransaction = null
            },
            onDismiss = { editingTransaction = null }
        )
    }
}
