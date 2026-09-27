package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.data.db.AppDatabase
import com.example.data.model.TransactionEntity
import com.example.util.NotificationHelper
import com.example.util.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val fullBody = StringBuilder()
            var sender = ""
            for (sms in messages) {
                fullBody.append(sms.displayMessageBody)
                if (sender.isEmpty()) {
                    sender = sms.displayOriginatingAddress ?: ""
                }
            }

            val parsed = SmsParser.parse(fullBody.toString(), sender)
            if (parsed != null) {
                val db = AppDatabase.getDatabase(context)
                CoroutineScope(Dispatchers.IO).launch {
                    // Try to match parsed bankName or sender or text with user's defined bank accounts
                    val allBanks = db.bankAccountDao().getAllBankAccounts().firstOrNull() ?: emptyList()
                    val matchedBank = allBanks.find { bank ->
                        val bNameLower = bank.name.lowercase()
                        val accNum = bank.accountNumber.trim()
                        (bNameLower.isNotBlank() && (parsed.bankName.lowercase().contains(bNameLower) || bNameLower.contains(parsed.bankName.lowercase()) || fullBody.contains(bank.name, ignoreCase = true))) ||
                        (accNum.length >= 4 && fullBody.contains(accNum))
                    }

                    val entity = TransactionEntity(
                        amount = parsed.amount,
                        isIncoming = parsed.isIncoming,
                        type = TransactionEntity.TYPE_SHOP, // Default rule 1: incoming money is considered Shop's Money
                        status = TransactionEntity.STATUS_PENDING, // 0 = Pending/Unseen
                        isCleared = false,
                        bankId = matchedBank?.id,
                        notes = "دریافت خودکار از ${matchedBank?.name ?: parsed.bankName}",
                        date = parsed.date,
                        rawSms = fullBody.toString(),
                        bankName = matchedBank?.name ?: parsed.bankName
                    )
                    db.transactionDao().insert(entity)

                    NotificationHelper.showPendingSmsNotification(context, 1)
                }
            }
        }
    }
}
