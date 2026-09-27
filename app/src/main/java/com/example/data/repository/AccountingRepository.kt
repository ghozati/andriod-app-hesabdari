package com.example.data.repository

import com.example.data.dao.BankAccountDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.ProjectDao
import com.example.data.dao.ServiceDao
import com.example.data.dao.TransactionDao
import com.example.data.model.BankAccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ServiceEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class AccountingRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val projectDao: ProjectDao,
    private val serviceDao: ServiceDao,
    private val bankAccountDao: BankAccountDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val pendingTransactions: Flow<List<TransactionEntity>> = transactionDao.getPendingTransactions()
    val pendingCount: Flow<Int> = transactionDao.getPendingCount()
    val unclearedShopItems: Flow<List<TransactionEntity>> = transactionDao.getUnclearedShopItems()
    val clearedShopItems: Flow<List<TransactionEntity>> = transactionDao.getClearedShopItems()

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allServices: Flow<List<ServiceEntity>> = serviceDao.getAllServices()
    val allBankAccounts: Flow<List<BankAccountEntity>> = bankAccountDao.getAllBankAccounts()

    fun getTransactionsForProject(projectId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByProject(projectId)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insert(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.update(transaction)

    suspend fun updateTransactionStatus(id: Long, status: Int) =
        transactionDao.updateStatus(id, status)

    suspend fun updateTransactionCategory(id: Long, categoryId: Long?) =
        transactionDao.updateCategory(id, categoryId)

    suspend fun markTransactionsCleared(ids: List<Long>, cleared: Boolean = true) =
        transactionDao.markCleared(ids, cleared)

    suspend fun deleteTransaction(id: Long) =
        transactionDao.delete(id)

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insert(category)

    suspend fun insertCategories(categories: List<CategoryEntity>) =
        categoryDao.insertAll(categories)

    suspend fun deleteCategory(id: Long) =
        categoryDao.delete(id)

    suspend fun insertProject(project: ProjectEntity): Long =
        projectDao.insert(project)

    suspend fun updateProjectStatus(id: Long, status: String) =
        projectDao.updateStatus(id, status)

    suspend fun insertService(service: ServiceEntity): Long =
        serviceDao.insert(service)

    suspend fun insertServices(services: List<ServiceEntity>) =
        serviceDao.insertAll(services)

    suspend fun updateService(service: ServiceEntity) =
        serviceDao.update(service)

    suspend fun deleteService(id: Long) =
        serviceDao.delete(id)

    // Bank Account actions
    suspend fun insertBankAccount(bankAccount: BankAccountEntity): Long =
        bankAccountDao.insert(bankAccount)

    suspend fun updateBankAccount(bankAccount: BankAccountEntity) =
        bankAccountDao.update(bankAccount)

    suspend fun deleteBankAccount(id: Long) =
        bankAccountDao.deleteById(id)
}
