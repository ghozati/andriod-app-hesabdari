package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Transaction entity for the dual-ledger accounting system.
 *
 * Types:
 * - "Shop":
 *     * isIncoming = true  -> Incoming money to bank belonging to Shop (increases bank balance AND increases shop debt)
 *     * isIncoming = false -> Money spent for Shop (decreases bank balance AND decreases shop debt, eligible for settlement)
 * - "Personal":
 *     * isIncoming = true  -> Personal deposit (increases bank balance, debt unchanged)
 *     * isIncoming = false -> Personal expense (decreases bank balance, debt unchanged; if categoryId == null -> "No Category ⚠️")
 * - "Transfer":
 *     * Bank internal transfer / account adjustment (debt unchanged)
 * - "Service":
 *     * User provides service/shift to shop (decreases user's debt to shop, eligible for settlement).
 *     * NOTE: Does NOT affect bank balances (no actual bank cash movement).
 *
 * Status:
 * - 0 = Pending/Unseen (waiting for Inbox review)
 * - 1 = Confirmed
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    @ColumnInfo(name = "is_incoming")
    val isIncoming: Boolean = false,
    val type: String = TYPE_SHOP, // "Shop", "Personal", "Transfer", "Service"
    val status: Int = STATUS_CONFIRMED, // 0 = Pending, 1 = Confirmed
    @ColumnInfo(name = "is_cleared")
    val isCleared: Boolean = false,
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,
    @ColumnInfo(name = "project_id")
    val projectId: Long? = null,
    @ColumnInfo(name = "bank_id")
    val bankId: Long? = null,
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "raw_sms")
    val rawSms: String? = null,
    @ColumnInfo(name = "bank_name")
    val bankName: String? = null
) {
    companion object {
        const val TYPE_SHOP = "Shop"
        const val TYPE_PERSONAL = "Personal"
        const val TYPE_TRANSFER = "Transfer"
        const val TYPE_SERVICE = "Service"

        const val STATUS_PENDING = 0
        const val STATUS_CONFIRMED = 1
    }
}
