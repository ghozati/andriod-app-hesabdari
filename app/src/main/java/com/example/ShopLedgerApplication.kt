package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.AccountingRepository
import com.example.util.NotificationHelper

class ShopLedgerApplication : Application() {

    lateinit var repository: AccountingRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        repository = AccountingRepository(
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            projectDao = database.projectDao(),
            serviceDao = database.serviceDao(),
            bankAccountDao = database.bankAccountDao()
        )

        NotificationHelper.createNotificationChannel(this)
        // No fake/mock data is injected. The app relies completely and purely on actual user database.
    }
}
