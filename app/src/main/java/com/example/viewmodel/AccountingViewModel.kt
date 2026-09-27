package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BankAccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ServiceEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.AccountingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinancialLedgerSummary(
    val realBankBalance: Double = 0.0,
    val instantShopDebt: Double = 0.0,
    val officiallyClearedDebt: Double = 0.0,
    val pendingSettlementAmount: Double = 0.0,
    val pendingInboxCount: Int = 0,
    val totalShopInflow: Double = 0.0,
    val totalShopOutflow: Double = 0.0,
    val totalServicesValue: Double = 0.0
)

data class ProjectStat(
    val project: ProjectEntity,
    val totalSpent: Double,
    val totalReceived: Double,
    val netCost: Double,
    val transactionCount: Int
)

data class BankBalanceStat(
    val bank: BankAccountEntity,
    val currentBalance: Double,
    val totalInflow: Double,
    val totalOutflow: Double,
    val transactionCount: Int
)

class AccountingViewModel(
    private val repository: AccountingRepository
) : ViewModel() {

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<TransactionEntity>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = repository.pendingCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unclearedShopItems: StateFlow<List<TransactionEntity>> = repository.unclearedShopItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clearedShopItems: StateFlow<List<TransactionEntity>> = repository.clearedShopItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<ServiceEntity>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAccounts: StateFlow<List<BankAccountEntity>> = repository.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selection state for Settlement Screen
    private val _selectedSettlementIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedSettlementIds: StateFlow<Set<Long>> = _selectedSettlementIds

    // Bank Account Statistics combining bankAccounts and allTransactions
    // Services (TYPE_SERVICE) do NOT affect bank balance!
    val bankStats: StateFlow<List<BankBalanceStat>> = repository.allBankAccounts
        .combine(repository.allTransactions) { banks, txList ->
            val confirmed = txList.filter { it.status == TransactionEntity.STATUS_CONFIRMED }
            banks.map { bank ->
                val linked = confirmed.filter { tx ->
                    // Match by bankId OR match by bankName if bankId was null
                    (tx.bankId == bank.id) || (tx.bankId == null && tx.bankName != null &&
                            (tx.bankName.contains(bank.name, ignoreCase = true) || bank.name.contains(tx.bankName, ignoreCase = true)))
                }
                // Only non-service transactions move cash in bank accounts!
                val bankMoves = linked.filter { it.type != TransactionEntity.TYPE_SERVICE }
                val inflows = bankMoves.filter { it.isIncoming }.sumOf { it.amount }
                val outflows = bankMoves.filter { !it.isIncoming }.sumOf { it.amount }
                val current = bank.initialBalance + inflows - outflows

                BankBalanceStat(
                    bank = bank,
                    currentBalance = current,
                    totalInflow = inflows,
                    totalOutflow = outflows,
                    transactionCount = bankMoves.size
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary calculation combining transactions, pending count, and bank accounts initial balances
    val ledgerSummary: StateFlow<FinancialLedgerSummary> = combine(
        repository.allTransactions,
        repository.pendingCount,
        repository.allBankAccounts
    ) { txList, pendingCnt, banks ->
        calculateSummary(txList, pendingCnt, banks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialLedgerSummary())

    // Project Stats combining projects and transactions
    val projectStats: StateFlow<List<ProjectStat>> = repository.allProjects
        .combine(repository.allTransactions) { prjList, txList ->
            prjList.map { project ->
                val linked = txList.filter { it.projectId == project.id && it.status == TransactionEntity.STATUS_CONFIRMED }
                val spent = linked.filter { !it.isIncoming && it.type != TransactionEntity.TYPE_SERVICE }.sumOf { it.amount }
                val received = linked.filter { it.isIncoming }.sumOf { it.amount }
                ProjectStat(
                    project = project,
                    totalSpent = spent,
                    totalReceived = received,
                    netCost = spent - received,
                    transactionCount = linked.size
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun calculateSummary(
        transactions: List<TransactionEntity>,
        pendingCnt: Int,
        banks: List<BankAccountEntity>
    ): FinancialLedgerSummary {
        val confirmed = transactions.filter { it.status == TransactionEntity.STATUS_CONFIRMED }

        // Start real bank balance with initial balances of all user banks
        var bankBalance = banks.sumOf { it.initialBalance }
        var totalShopInflow = 0.0
        var totalShopOutflow = 0.0
        var totalServicesValue = 0.0
        var officiallyCleared = 0.0
        var pendingSettlement = 0.0

        for (tx in confirmed) {
            // Bank cash flow: services do NOT alter bank balance!
            if (tx.type != TransactionEntity.TYPE_SERVICE) {
                if (tx.isIncoming) {
                    bankBalance += tx.amount
                } else {
                    bankBalance -= tx.amount
                }
            }

            // Shop Debt ledger
            when (tx.type) {
                TransactionEntity.TYPE_SHOP -> {
                    if (tx.isIncoming) {
                        // Rule 1: Shop money arriving in bank -> increases user's debt
                        totalShopInflow += tx.amount
                    } else {
                        // Rule 2: Money spent for shop -> decreases debt
                        totalShopOutflow += tx.amount
                        if (tx.isCleared) {
                            officiallyCleared += tx.amount
                        } else {
                            pendingSettlement += tx.amount
                        }
                    }
                }
                TransactionEntity.TYPE_SERVICE -> {
                    // Rule 4: Service rendered by user to shop -> decreases user's debt
                    // (no money arrives in bank, purely reduces debt)
                    totalServicesValue += tx.amount
                    if (tx.isCleared) {
                        officiallyCleared += tx.amount
                    } else {
                        pendingSettlement += tx.amount
                    }
                }
                TransactionEntity.TYPE_PERSONAL -> {
                    // Rule 3: Personal expense decreases bank balance, but debt remains unchanged
                }
                TransactionEntity.TYPE_TRANSFER -> {
                    // Internal transfer, debt unchanged
                }
            }
        }

        // Instant Debt = (Shop Inflows) - (Shop Expenses + Services)
        val instantShopDebt = totalShopInflow - (totalShopOutflow + totalServicesValue)

        return FinancialLedgerSummary(
            realBankBalance = bankBalance,
            instantShopDebt = instantShopDebt,
            officiallyClearedDebt = officiallyCleared,
            pendingSettlementAmount = pendingSettlement,
            pendingInboxCount = pendingCnt,
            totalShopInflow = totalShopInflow,
            totalShopOutflow = totalShopOutflow,
            totalServicesValue = totalServicesValue
        )
    }

    // --- Settlement Actions ---
    fun toggleSettlementSelection(id: Long) {
        val current = _selectedSettlementIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedSettlementIds.value = current
    }

    fun selectAllSettlement(ids: List<Long>) {
        _selectedSettlementIds.value = ids.toSet()
    }

    fun clearSettlementSelection() {
        _selectedSettlementIds.value = emptySet()
    }

    fun settleSelectedItems() {
        val selected = _selectedSettlementIds.value.toList()
        if (selected.isNotEmpty()) {
            viewModelScope.launch {
                repository.markTransactionsCleared(selected, true)
                _selectedSettlementIds.value = emptySet()
            }
        }
    }

    // --- Inbox & Transaction Actions ---
    fun confirmPendingTransaction(
        id: Long,
        type: String,
        categoryId: Long?,
        projectId: Long?,
        bankId: Long?,
        notes: String
    ) {
        viewModelScope.launch {
            val existing = allTransactions.value.find { it.id == id }
            if (existing != null) {
                val bank = bankAccounts.value.find { it.id == bankId }
                val updated = existing.copy(
                    type = type,
                    categoryId = categoryId,
                    projectId = projectId,
                    bankId = bankId,
                    bankName = bank?.name ?: existing.bankName,
                    notes = notes.ifBlank { existing.notes },
                    status = TransactionEntity.STATUS_CONFIRMED
                )
                repository.updateTransaction(updated)
            }
        }
    }

    fun dismissOrDeletePending(id: Long) {
        viewModelScope.launch {
            val currentSelected = _selectedSettlementIds.value.toMutableSet()
            if (currentSelected.contains(id)) {
                currentSelected.remove(id)
                _selectedSettlementIds.value = currentSelected
            }
            repository.deleteTransaction(id)
        }
    }

    /**
     * Updates an existing transaction with full dynamic recalculation.
     * Settlement Rule: If a transaction that was already settled (isCleared = true) is edited
     * (specifically if amount or type or incoming direction changes),
     * it MUST automatically revert to isCleared = false.
     */
    fun updateTransactionDetails(
        id: Long,
        amount: Double,
        isIncoming: Boolean,
        type: String,
        categoryId: Long?,
        projectId: Long?,
        bankId: Long?,
        notes: String,
        onResult: (revertedSettlement: Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val existing = allTransactions.value.find { it.id == id }
            if (existing != null) {
                val amountChanged = existing.amount != amount
                val typeChanged = existing.type != type || existing.isIncoming != isIncoming
                var newCleared = existing.isCleared
                var reverted = false

                if (existing.isCleared && (amountChanged || typeChanged)) {
                    newCleared = false
                    reverted = true
                }

                val bank = bankAccounts.value.find { it.id == bankId }
                val updated = existing.copy(
                    amount = amount,
                    isIncoming = isIncoming,
                    type = type,
                    categoryId = if (type == TransactionEntity.TYPE_PERSONAL) categoryId else null,
                    projectId = projectId,
                    bankId = if (type == TransactionEntity.TYPE_SERVICE) null else bankId,
                    bankName = if (type == TransactionEntity.TYPE_SERVICE) null else (bank?.name ?: existing.bankName),
                    notes = notes,
                    isCleared = newCleared
                )
                repository.updateTransaction(updated)
                onResult(reverted)
            }
        }
    }

    /**
     * Deletes a transaction and removes it from settlement selection if selected.
     */
    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            val currentSelected = _selectedSettlementIds.value.toMutableSet()
            if (currentSelected.contains(id)) {
                currentSelected.remove(id)
                _selectedSettlementIds.value = currentSelected
            }
            repository.deleteTransaction(id)
        }
    }

    fun assignCategoryToTransaction(id: Long, categoryId: Long?) {
        viewModelScope.launch {
            repository.updateTransactionCategory(id, categoryId)
        }
    }

    /**
     * When a transaction is manually added, it goes into STATUS_PENDING (ثبت موقت)
     * with an associated bankId so that user can review it in the pending queue.
     */
    fun addManualTransaction(
        amount: Double,
        isIncoming: Boolean,
        type: String,
        categoryId: Long?,
        projectId: Long?,
        bankId: Long?,
        notes: String
    ) {
        viewModelScope.launch {
            val bank = bankAccounts.value.find { it.id == bankId }
            val entity = TransactionEntity(
                amount = amount,
                isIncoming = isIncoming,
                type = type,
                status = TransactionEntity.STATUS_CONFIRMED,
                isCleared = false,
                categoryId = categoryId,
                projectId = projectId,
                bankId = if (type == TransactionEntity.TYPE_SERVICE) null else bankId,
                bankName = if (type == TransactionEntity.TYPE_SERVICE) null else bank?.name,
                notes = notes,
                date = System.currentTimeMillis()
            )
            repository.insertTransaction(entity)
        }
    }

    /**
     * When a service or tariff is logged for the shop:
     * NOTE: As requested, service transactions do NOT affect bank balance and do not transfer money to a bank.
     * They purely decrease the debt to the shop and go into pending queue.
     */
    fun logServiceForShop(service: ServiceEntity, customPrice: Double? = null, notes: String = "") {
        viewModelScope.launch {
            val price = customPrice ?: service.defaultPrice
            val entity = TransactionEntity(
                amount = price,
                isIncoming = false,
                type = TransactionEntity.TYPE_SERVICE,
                status = TransactionEntity.STATUS_CONFIRMED,
                isCleared = false,
                bankId = null,
                bankName = null,
                notes = if (notes.isNotBlank()) "${service.title} - $notes" else "خدمت: ${service.title}",
                date = System.currentTimeMillis()
            )
            repository.insertTransaction(entity)
        }
    }

    fun addNewService(title: String, defaultPrice: Double) {
        viewModelScope.launch {
            repository.insertService(ServiceEntity(title = title, defaultPrice = defaultPrice))
        }
    }

    fun updateService(id: Long, title: String, defaultPrice: Double) {
        viewModelScope.launch {
            repository.updateService(ServiceEntity(id = id, title = title, defaultPrice = defaultPrice))
        }
    }

    fun deleteService(id: Long) {
        viewModelScope.launch {
            repository.deleteService(id)
        }
    }

    fun addNewCategory(name: String) {
        viewModelScope.launch {
            repository.insertCategory(CategoryEntity(name = name))
        }
    }

    fun addNewProject(name: String) {
        viewModelScope.launch {
            repository.insertProject(ProjectEntity(name = name))
        }
    }

    fun updateProjectStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateProjectStatus(id, status)
        }
    }

    // Bank Account management
    fun addNewBank(name: String, accountNumber: String, initialBalance: Double) {
        viewModelScope.launch {
            repository.insertBankAccount(
                BankAccountEntity(
                    name = name,
                    accountNumber = accountNumber,
                    initialBalance = initialBalance
                )
            )
        }
    }

    fun updateBank(id: Long, name: String, accountNumber: String, initialBalance: Double) {
        viewModelScope.launch {
            repository.updateBankAccount(
                BankAccountEntity(
                    id = id,
                    name = name,
                    accountNumber = accountNumber,
                    initialBalance = initialBalance
                )
            )
        }
    }

    fun deleteBank(id: Long) {
        viewModelScope.launch {
            repository.deleteBankAccount(id)
        }
    }

    fun dismissAllPending() {
        viewModelScope.launch {
            val list = pendingTransactions.value
            for (item in list) {
                repository.deleteTransaction(item.id)
            }
        }
    }

    fun simulateBankSms(smsText: String, bankName: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val parsed = com.example.util.SmsParser.parse(smsText, bankName)
            if (parsed == null) {
                onResult(false)
                return@launch
            }
            val amount = parsed.amount
            val isIncoming = parsed.isIncoming
            val bName = parsed.bankName

            // Try to match parsed bank with user registered banks
            val matchedBank = bankAccounts.value.find { bank ->
                val bLower = bank.name.lowercase()
                val accNum = bank.accountNumber.trim()
                (bLower.isNotBlank() && (bName.lowercase().contains(bLower) || bLower.contains(bName.lowercase()) || smsText.contains(bank.name, ignoreCase = true))) ||
                (accNum.length >= 4 && smsText.contains(accNum))
            }

            val entity = TransactionEntity(
                amount = amount,
                isIncoming = isIncoming,
                type = TransactionEntity.TYPE_SHOP, // Rule 1 default
                status = TransactionEntity.STATUS_PENDING,
                isCleared = false,
                bankId = matchedBank?.id,
                bankName = matchedBank?.name ?: bName,
                notes = "Auto-intercepted from ${matchedBank?.name ?: bName}",
                date = System.currentTimeMillis(),
                rawSms = smsText
            )
            repository.insertTransaction(entity)
            onResult(true)
        }
    }

    fun setInitialBalances(initialBankBalance: Double, initialShopDebt: Double) {
        viewModelScope.launch {
            // Create an initial bank balance baseline transaction if > 0
            if (initialBankBalance != 0.0) {
                repository.insertTransaction(
                    TransactionEntity(
                        amount = kotlin.math.abs(initialBankBalance),
                        isIncoming = initialBankBalance > 0,
                        type = TransactionEntity.TYPE_PERSONAL,
                        status = TransactionEntity.STATUS_CONFIRMED,
                        notes = "تنظیم موجودی اولیه بانک",
                        date = System.currentTimeMillis() - 86400000L * 7
                    )
                )
            }
            // Create an initial shop debt baseline transaction if > 0
            if (initialShopDebt != 0.0) {
                repository.insertTransaction(
                    TransactionEntity(
                        amount = initialShopDebt,
                        isIncoming = true, // incoming shop money = debt
                        type = TransactionEntity.TYPE_SHOP,
                        status = TransactionEntity.STATUS_CONFIRMED,
                        notes = "بدهی اولیه به مغازه",
                        date = System.currentTimeMillis() - 86400000L * 7
                    )
                )
            }
        }
    }

    companion object {
        fun provideFactory(repository: AccountingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AccountingViewModel(repository) as T
                }
            }
    }
}
