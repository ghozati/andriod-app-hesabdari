package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = 0 ORDER BY date DESC")
    fun getPendingTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE status = 0")
    fun getPendingCount(): Flow<Int>

    // Settlement candidates: All "Shop" transactions and "Service" transactions where is_cleared = 0
    @Query("""
        SELECT * FROM transactions 
        WHERE is_cleared = 0 
          AND status = 1 
          AND (type = 'Shop' OR type = 'Service')
        ORDER BY date DESC
    """)
    fun getUnclearedShopItems(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE is_cleared = 1 
          AND status = 1 
          AND (type = 'Shop' OR type = 'Service')
        ORDER BY date DESC
    """)
    fun getClearedShopItems(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE project_id = :projectId AND status = 1 ORDER BY date DESC")
    fun getTransactionsByProject(projectId: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: Int)

    @Query("UPDATE transactions SET category_id = :categoryId WHERE id = :id")
    suspend fun updateCategory(id: Long, categoryId: Long?)

    @Query("UPDATE transactions SET is_cleared = :cleared WHERE id IN (:ids)")
    suspend fun markCleared(ids: List<Long>, cleared: Boolean = true)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)
}
