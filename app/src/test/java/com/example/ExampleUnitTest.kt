package com.example

import com.example.data.model.TransactionEntity
import com.example.util.SmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testSmsParser_mellatDeposit_convertsRialToToman() {
        val sms = "بانک ملت: واریز مبلغ 3,500,000 ریال به حساب 8291. مانده: 18,200,000 ریال"
        val parsed = SmsParser.parse(sms, "BankMellat")
        assertNotNull(parsed)
        // 3,500,000 Rials divided by 10 = 350,000 Tomans
        assertEquals(350000.0, parsed!!.amount, 0.01)
        assertTrue(parsed.isIncoming)
        assertEquals("بانک ملت", parsed.bankName)
    }

    @Test
    fun testSmsParser_bluBankWithdrawal_samples() {
        val bluExpenseSms = """
            بلو
            برداشت پول
            میرمحمدرضا عزیز، 40,000,000 ریال از حساب شما پرید.
            موجودی: 1,353,705,475 ریال
            ۲۰:۱۱
            ۱۴۰۵.۰۷.۰۵
        """.trimIndent()
        val parsedExpense = SmsParser.parse(bluExpenseSms, "")
        assertNotNull(parsedExpense)
        // 40,000,000 Rials / 10 = 4,000,000 Tomans
        assertEquals(4000000.0, parsedExpense!!.amount, 0.01)
        assertFalse(parsedExpense.isIncoming)
        assertEquals("بلوبانک", parsedExpense.bankName)

        val bluDepositSms = """
            بلو
            واریز پول
            میرمحمدرضا عزیز، 48,900,000 ریال به حساب شما نشست.
            موجودی: 1,393,726,975 ریال
            ۱۸:۲۴
            ۱۴۰۵.۰۷.۰۵
        """.trimIndent()
        val parsedDeposit = SmsParser.parse(bluDepositSms, "")
        assertNotNull(parsedDeposit)
        // 48,900,000 Rials / 10 = 4,890,000 Tomans
        assertEquals(4890000.0, parsedDeposit!!.amount, 0.01)
        assertTrue(parsedDeposit.isIncoming)
        assertEquals("بلوبانک", parsedDeposit.bankName)
    }

    @Test
    fun testSmsParser_bankMelliDirectDeposit() {
        val melliSms = """
            حساب7703571824
            واریز50,000,000
            مانده335,547,577
            05/07/05-14:08
        """.trimIndent()
        val parsed = SmsParser.parse(melliSms, "BankMelli")
        assertNotNull(parsed)
        // 50,000,000 Rials / 10 = 5,000,000 Tomans
        assertEquals(5000000.0, parsed!!.amount, 0.01)
        assertTrue(parsed.isIncoming)
    }

    @Test
    fun testSmsParser_samanWithdrawalInToman() {
        val sms = "بانک سامان: برداشت مبلغ 450,000 تومان - خرید اینترنتی"
        val parsed = SmsParser.parse(sms, "SamanBank")
        assertNotNull(parsed)
        // Already explicit تومان -> 450,000
        assertEquals(450000.0, parsed!!.amount, 0.01)
        assertFalse(parsed.isIncoming)
        assertEquals("بانک سامان", parsed.bankName)
    }

    @Test
    fun testCommingledFundsCalculationRules() {
        // Rule 1: Deposit of shop money increases bank balance and shop debt
        val shopDeposit = TransactionEntity(
            amount = 10000000.0,
            isIncoming = true,
            type = TransactionEntity.TYPE_SHOP,
            status = TransactionEntity.STATUS_CONFIRMED
        )

        // Rule 2: Money spent for shop decreases bank balance and shop debt
        val shopExpense = TransactionEntity(
            amount = 2000000.0,
            isIncoming = false,
            type = TransactionEntity.TYPE_SHOP,
            status = TransactionEntity.STATUS_CONFIRMED
        )

        // Rule 3: Money spent for personal reasons decreases bank balance, debt unchanged
        val personalExpense = TransactionEntity(
            amount = 500000.0,
            isIncoming = false,
            type = TransactionEntity.TYPE_PERSONAL,
            status = TransactionEntity.STATUS_CONFIRMED
        )

        // Rule 4: Service provided to shop acts as salary, decreases debt without bank cash movement
        val serviceRendered = TransactionEntity(
            amount = 800000.0,
            isIncoming = false,
            type = TransactionEntity.TYPE_SERVICE,
            status = TransactionEntity.STATUS_CONFIRMED
        )

        val transactions = listOf(shopDeposit, shopExpense, personalExpense, serviceRendered)

        // Bank balance = 10,000,000 (deposit) - 2,000,000 (shop expense) - 500,000 (personal expense) = 7,500,000
        var bankBalance = 0.0
        var shopDebt = 0.0

        for (tx in transactions) {
            if (tx.type != TransactionEntity.TYPE_SERVICE) {
                if (tx.isIncoming) bankBalance += tx.amount else bankBalance -= tx.amount
            }

            when (tx.type) {
                TransactionEntity.TYPE_SHOP -> {
                    if (tx.isIncoming) shopDebt += tx.amount else shopDebt -= tx.amount
                }
                TransactionEntity.TYPE_SERVICE -> {
                    shopDebt -= tx.amount
                }
                TransactionEntity.TYPE_PERSONAL -> {
                    // Debt unchanged
                }
            }
        }

        assertEquals(7500000.0, bankBalance, 0.01)
        // Shop Debt = 10,000,000 - 2,000,000 - 800,000 = 7,200,000
        assertEquals(7200000.0, shopDebt, 0.01)
    }

    @Test
    fun testCurrencyFormatter_usesAsciiEnglishNumerals() {
        val formatted = com.example.ui.util.CurrencyFormatter.format(9195000.0)
        assertEquals("9,195,000 تومان", formatted)

        val zeroFormatted = com.example.ui.util.CurrencyFormatter.format(0.0)
        assertEquals("0 تومان", zeroFormatted)

        val numberFormatted = com.example.ui.util.CurrencyFormatter.formatNumber(42)
        assertEquals("42", numberFormatted)
    }

    @Test
    fun testSettlementReversionRuleOnEdit() {
        // A settled transaction (isCleared = true)
        val settledTransaction = TransactionEntity(
            id = 10,
            amount = 1500000.0,
            isIncoming = false,
            type = TransactionEntity.TYPE_SHOP,
            status = TransactionEntity.STATUS_CONFIRMED,
            isCleared = true
        )

        // Editing amount: should revert isCleared to false
        val newAmount = 1800000.0
        val amountChanged = settledTransaction.amount != newAmount
        val reverted = settledTransaction.isCleared && amountChanged
        val updatedTransaction = settledTransaction.copy(
            amount = newAmount,
            isCleared = if (reverted) false else settledTransaction.isCleared
        )

        assertTrue(reverted)
        assertFalse(updatedTransaction.isCleared)
        assertEquals(1800000.0, updatedTransaction.amount, 0.01)
    }

    @Test
    fun testSmsParser_rejectsPersonalMobileNumbers() {
        val personalSms = "سلام داداش 500000 تومن به کارتت زدم دستت درد نکنه"
        val parsed = SmsParser.parse(personalSms, "09121234567")
        org.junit.Assert.assertNull("Personal mobile numbers must be rejected", parsed)

        val parsedWithPlus98 = SmsParser.parse(personalSms, "+989351234567")
        org.junit.Assert.assertNull("Personal mobile numbers with +98 must be rejected", parsedWithPlus98)
    }

    @Test
    fun testSmsParser_rejectsOtpAndVerificationCodes() {
        val otp1 = "کد تایید ورود به دیوار: 58291"
        org.junit.Assert.assertNull(SmsParser.parse(otp1, "Divar"))

        val otp2 = "رمز یکبار مصرف بانک ملت: 591024 معتبر تا 120 ثانیه"
        org.junit.Assert.assertNull(SmsParser.parse(otp2, "BankMellat"))

        val otp3 = "رمز پویا: 728194 بانک صادرات"
        org.junit.Assert.assertNull(SmsParser.parse(otp3, "20000000"))
    }

    @Test
    fun testSmsParser_rejectsTelecomPromosAndJunk() {
        val promo1 = "همراه اول: ۵۰% تخفیف در خرید بسته اینترنت ۱۰ گیگابایت برای شما با قیمت ۳۵۰۰۰ تومان"
        org.junit.Assert.assertNull(SmsParser.parse(promo1, "HamrahAval"))

        val promo2 = "ایرانسل: مانده بسته اینترنت شما 500 مگابایت است. تمدید با ارسال 1"
        org.junit.Assert.assertNull(SmsParser.parse(promo2, "Irancell"))

        val spam = "وام فوری بدون ضامن تا سقف 50000000 تومان برای دریافت کلیک کنید"
        org.junit.Assert.assertNull(SmsParser.parse(spam, "10008888"))

        val randomText = "فردا ساعت 8 صبح 4 قطعه بفرست دفتر"
        org.junit.Assert.assertNull(SmsParser.parse(randomText, "Office"))
    }

    @Test
    fun testSmsParser_blubankPurchase() {
        val bluSms = "بلوبانک: خرید به مبلغ 85,000 تومان با کارت 1234. مانده: 415,000 تومان"
        val parsed = SmsParser.parse(bluSms, "BluBank")
        assertNotNull(parsed)
        assertEquals(85000.0, parsed!!.amount, 0.01)
        assertFalse(parsed.isIncoming)
        assertEquals("بلوبانک", parsed.bankName)
    }
}
