package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bank account entity managed dynamically by the user.
 * Users can register multiple bank accounts (e.g. بلوبانک, بانک ملت, بانک ملی)
 * with an optional card number/IBAN and an initial balance.
 */
@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "بلوبانک", "بانک ملت"
    @ColumnInfo(name = "account_number")
    val accountNumber: String = "", // Optional account or card number
    @ColumnInfo(name = "initial_balance")
    val initialBalance: Double = 0.0, // Starting balance in Tomans
    val color: String = "#10B981" // Hex color code for badge/avatar
)
