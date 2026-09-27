package com.example.util

import java.util.regex.Pattern

data class ParsedSms(
    val amount: Double, // Always stored in TOMANS (تومان)
    val isIncoming: Boolean,
    val bankName: String,
    val date: Long = System.currentTimeMillis()
)

object SmsParser {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    // Iranian banks exact keywords
    private val bankKeywords = listOf(
        "بانک ملت", "ملت", "بانک ملی", "ملی ایران", "بانک سامان", "سامان", "بانک تجارت", "تجارت",
        "بانک صادرات", "صادرات ایران", "بانک پاسارگاد", "پاسارگاد", "بانک پارسیان", "پارسیان",
        "بانک سپه", "سپه", "بلوبانک", "بلو بانک", "بلو", "blubank", "بانک کشاورزی", "کشاورزی",
        "بانک رفاه", "رفاه کارگران", "بانک رسالت", "قرض الحسنه رسالت", "بانک آینده", "آینده",
        "بانک سینا", "سینا", "بانک مسکن", "مسکن", "بانک شهر", "شهر", "مهر ایران", "قرض الحسنه مهر",
        "توسعه تعاون", "بانک اقتصاد نوین", "اقتصاد نوین", "پست بانک", "بانک دی", "دی",
        "بانک کارآفرین", "کارآفرین", "بانک سرمایه", "سرمایه", "بانک خاورمیانه", "خاورمیانه",
        "بانک گردشگری", "گردشگری", "بانک ایران زمین", "ایران زمین", "صنعت و معدن", "توسعه صادرات",
        "بانک مرکزی", "شاپرک", "به پرداخت", "سداد", "آسان پرداخت", "آپ", "سامان کیش", "ایران کیش"
    )

    private val bankSenderNames = listOf(
        "mellat", "melli", "saman", "tejarat", "saderat", "pasargad",
        "parsian", "sepah", "blu", "blubank", "keshavarzi", "refah",
        "resalat", "ayandeh", "sina", "maskan", "shahr", "mehr",
        "postbank", "dey", "sarmayeh", "enbank", "bpi", "bmi",
        "shaparak", "sadad", "asanpardakht", "behpardakht",
        // Known bank official shortcode numbers
        "20004000", "20004001", "20004002", "10006363", "200064140", "30004000",
        "200001818", "10001818", "20001818", "20000000", "30000000", "200016",
        "20009588", "30009588", "20008434", "30008434", "20008018", "30008018",
        "2000911", "3000911", "20000", "2000222", "300066", "200082", "200011"
    )

    // Non-transaction SMS blacklist (OTP, Promo, Marketing, Operators, Personal chat)
    private val nonBankBlacklist = listOf(
        // OTP & Auth
        "کد تایید", "کد تأیید", "کد تائید", "کد فعالسازی", "کد فعال سازی", "کد فعال‌سازی",
        "رمز یکبار مصرف", "رمز یکبارمصرف", "رمز پویا", "رمز اینترنتی", "کد ورود",
        "احراز هویت", "رمز دوم", "کد اعتبارسنجی", "کد ثبت نام", "کد موقت", "رمز موقت",
        "otp", "verification code", "login code", "security code", "active code",
        // Operators & Telecom
        "همراه اول", "ایرانسل", "رایتل", "شاتل", "بسته اینترنت", "بسته اینترنتی",
        "گیگابایت", "مگابایت", "شارژ مستقیم", "شارژ شگفت انگیز", "شارژ فوق العاده",
        "پایان بسته", "تمدید خودکار", "لغو ۱۱", "لغو 11", "ارسال عدد", "کد دستوری",
        "اعتبار مصرفی", "مشترک گرامی", "سیمکارت", "سیم کارت",
        // Apps & Rides & Shopping
        "snapp", "tapsi", "divar", "digikala", "اسنپ", "تپسی", "دیوار", "شیپور",
        "دیجی کالا", "دیجیکالا", "باسلام", "ترب", "ایمالز", "کافه بازار", "مایکت",
        "فیلیمو", "نماوا", "فیلم نت", "سفارش شما", "سفر اسنپ", "سفر تپسی",
        // Marketing & Ads
        "تخفیف", "کد تخفیف", "جشنواره", "شارژ رایگان", "قرعه کشی", "قرعه‌کشی",
        "برنده شدید", "برنده شوید", "جایزه", "وام فوری", "وام بدون ضامن", "تبلیغات",
        "ارسال رایگان", "حراج", "فروش ویژه", "فرصت طلایی", "کلیک کنید", "لینک زیر",
        "دعوت به همکاری", "استخدام", "عضویت", "نصب اپلیکیشن", "ثبت نام رایگان",
        // Utilities & Government
        "قبض برق", "قبض گاز", "قبض آب", "سامانه ثنا", "ابلاغیه الکترونیکی", "جریمه رانندگی", "خلافی"
    )

    fun normalizeDigits(input: String): String {
        var result = input
        for (i in 0..9) {
            result = result.replace(persianDigits[i], ('0' + i))
            result = result.replace(arabicDigits[i], ('0' + i))
        }
        return result
    }

    /**
     * Checks if sender address is a personal Iranian mobile number (e.g. 0912..., +98912...).
     * Iranian banks NEVER send automated banking transaction notifications from personal mobile phones.
     */
    fun isPersonalMobileNumber(sender: String): Boolean {
        val clean = sender.replace("+", "").replace("-", "").replace(" ", "").trim()
        if (clean.isEmpty()) return false
        return clean.matches(Regex("^(0|98|0098)?9\\d{9}$"))
    }

    /**
     * Checks whether an SMS genuinely comes from an Iranian bank transaction.
     */
    fun isBankTransactionSms(smsBody: String, sender: String = ""): Boolean {
        val lowerBody = smsBody.lowercase()
        val lowerSender = sender.lowercase().trim()

        // 1. If sender is a personal mobile number, it is 100% NOT an automated bank transaction
        if (isPersonalMobileNumber(lowerSender)) {
            return false
        }

        // 2. Reject if contains any blacklist words (OTP, promo, telecom, marketing)
        for (blacklistWord in nonBankBlacklist) {
            if (lowerBody.contains(blacklistWord)) {
                return false
            }
        }

        // 3. Must contain an explicit financial transaction action
        val hasDeposit = lowerBody.contains("واریز") ||
                lowerBody.contains("سود سپرده") ||
                lowerBody.contains("واریز حقوق") ||
                lowerBody.contains("واریز سود") ||
                lowerBody.contains("واریز یارانه") ||
                lowerBody.contains("نشست") ||
                lowerBody.contains("بستانکار") ||
                lowerBody.contains("انتقال از") ||
                lowerBody.contains("credit") ||
                lowerBody.contains("deposit")

        val hasWithdrawal = lowerBody.contains("برداشت") ||
                lowerBody.contains("انتقال به") ||
                lowerBody.contains("انتقال") ||
                lowerBody.contains("کارت به کارت") ||
                lowerBody.contains("پایا") ||
                lowerBody.contains("ساتنا") ||
                lowerBody.contains("پل") ||
                lowerBody.contains("پرید") ||
                lowerBody.contains("بدهکار") ||
                lowerBody.contains("debit")

        val hasPurchase = lowerBody.contains("خرید")

        if (!hasDeposit && !hasWithdrawal && !hasPurchase) {
            return false
        }

        // 4. Check for Iranian bank identity in body or sender OR banking structure
        var hasBankIdentity = false
        for (bank in bankKeywords) {
            if (lowerBody.contains(bank)) {
                hasBankIdentity = true
                break
            }
        }
        if (!hasBankIdentity && lowerSender.isNotBlank()) {
            for (bankSender in bankSenderNames) {
                if (lowerSender.contains(bankSender)) {
                    hasBankIdentity = true
                    break
                }
            }
        }

        // 5. Must have bank message structural indicators:
        // Must mention account, card, balance, or tracking/ref reference, or bank keyword
        val hasStructuralIndicator = lowerBody.contains("حساب") ||
                lowerBody.contains("کارت") ||
                lowerBody.contains("سپرده") ||
                lowerBody.contains("مانده") ||
                lowerBody.contains("موجودی") ||
                lowerBody.contains("پیگیری") ||
                lowerBody.contains("ارجاع") ||
                lowerBody.contains("مرجع") ||
                lowerBody.contains("شاپرک") ||
                lowerBody.contains("پایانه") ||
                lowerBody.contains("pos") ||
                hasBankIdentity

        if (!hasStructuralIndicator) {
            return false
        }

        // If no explicit bank keyword, we require strong banking structure (e.g. "حساب" + "واریز" + "مانده")
        if (!hasBankIdentity) {
            val strongStructure = (lowerBody.contains("حساب") || lowerBody.contains("کارت") || lowerBody.contains("سپرده")) &&
                    (lowerBody.contains("مانده") || lowerBody.contains("موجودی") || lowerBody.contains("پیگیری") || lowerBody.contains("مرجع"))
            if (!strongStructure) {
                return false
            }
        }

        return true
    }

    fun parse(smsBody: String, sender: String = ""): ParsedSms? {
        val normalized = normalizeDigits(smsBody)

        // Strict verification: only parse genuine bank transaction SMS
        if (!isBankTransactionSms(normalized, sender)) {
            return null
        }

        // Determine direction: incoming (deposit) vs outgoing (withdrawal/purchase)
        val lower = normalized.lowercase()
        val hasDeposit = lower.contains("واریز") ||
                lower.contains("سود سپرده") ||
                lower.contains("واریز حقوق") ||
                lower.contains("واریز سود") ||
                lower.contains("واریز یارانه") ||
                lower.contains("انتقال از") ||
                lower.contains("نشست") ||
                lower.contains("بستانکار") ||
                lower.contains("credit") ||
                lower.contains("deposit") ||
                (lower.contains("+") && !lower.contains("+98"))

        val hasWithdrawal = lower.contains("برداشت") ||
                lower.contains("خرید") ||
                lower.contains("انتقال به") ||
                lower.contains("کارمزد") ||
                lower.contains("کارت به کارت") ||
                lower.contains("پایانه") ||
                lower.contains("پرید") ||
                lower.contains("بدهکار") ||
                lower.contains("debit") ||
                lower.contains("spent") ||
                (lower.contains("-") && !Regex("""\d{2,4}-\d{2}""").containsMatchIn(lower))

        val isIncoming = when {
            hasDeposit && !hasWithdrawal -> true
            hasWithdrawal && !hasDeposit -> false
            hasDeposit -> true
            else -> false
        }

        // Extract transaction amount prioritizing explicit amount markers,
        // and strictly avoiding account balance ('مانده', 'موجودی') or card/account numbers.
        val amountPatterns = listOf(
            // Pattern 1: Explicit action followed by number (e.g. "مبلغ: 3,500,000", "واریز50,000,000", "برداشت 40,000,000")
            Pattern.compile("""(?:مبلغ|واریز|برداشت|خرید|پرید|نشست|انتقال)[\s:]*([0-9]{1,3}(?:[,،][0-9]{3})+|[0-9]{4,11})(?:\s*(?:ریال|تومان))?""", Pattern.CASE_INSENSITIVE),
            // Pattern 2: Number followed by "از حساب شما پرید" or "به حساب شما نشست"
            Pattern.compile("""([0-9]{1,3}(?:[,،][0-9]{3})+|[0-9]{4,11})\s*(?:ریال|تومان)?\s*(?:از حساب شما پرید|به حساب شما نشست)""", Pattern.CASE_INSENSITIVE),
            // Pattern 3: Signed "+3,500,000" or "-250,000"
            Pattern.compile("""[+-]\s*([0-9]{1,3}(?:[,،][0-9]{3})+|[0-9]{4,11})(?:\s*(?:ریال|تومان))?"""),
            // Pattern 4: Standard comma-formatted number with currency "1,250,000 ریال"
            Pattern.compile("""([0-9]{1,3}(?:[,،][0-9]{3})+)\s*(?:ریال|تومان)""", Pattern.CASE_INSENSITIVE),
            // Pattern 5: Plain number with currency "450000 ریال"
            Pattern.compile("""([0-9]{4,11})\s*(?:ریال|تومان)""", Pattern.CASE_INSENSITIVE)
        )

        var extractedAmount: Double? = null
        var explicitToman = false

        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(normalized)
            while (matcher.find()) {
                val matchStart = matcher.start()
                val matchedText = matcher.group(0) ?: ""

                // If not starting with a direct action keyword, verify not preceded by balance keywords
                val startsWithAction = matchedText.startsWith("مبلغ") ||
                        matchedText.startsWith("واریز") ||
                        matchedText.startsWith("برداشت") ||
                        matchedText.startsWith("خرید") ||
                        matchedText.startsWith("انتقال")

                if (!startsWithAction) {
                    val precedingText = normalized.substring(maxOf(0, matchStart - 15), matchStart)
                    if (precedingText.contains("مانده") || precedingText.contains("موجودی") ||
                        precedingText.contains("حساب") || precedingText.contains("کارت")
                    ) {
                        continue
                    }
                }

                val rawNum = matcher.group(1)?.replace(",", "")?.replace("،", "")?.replace(" ", "")?.trim()
                val parsed = rawNum?.toDoubleOrNull()
                if (parsed != null && parsed >= 1000) {
                    // Exclude 16-digit card numbers or 12-digit reference numbers without formatting
                    if (rawNum.length != 16 && rawNum.length != 12) {
                        if (matchedText.contains("تومان")) {
                            explicitToman = true
                        } else {
                            val surrounding = normalized.substring(
                                maxOf(0, matchStart - 5),
                                minOf(normalized.length, matcher.end() + 15)
                            )
                            if (surrounding.contains("تومان")) {
                                explicitToman = true
                            }
                        }
                        extractedAmount = parsed
                        break
                    }
                }
            }
            if (extractedAmount != null) {
                break
            }
        }

        // Fallback: search any pattern if not yet found
        if (extractedAmount == null) {
            for (pattern in amountPatterns) {
                val matcher = pattern.matcher(normalized)
                if (matcher.find()) {
                    val rawNum = matcher.group(1)?.replace(",", "")?.replace("،", "")?.replace(" ", "")?.trim()
                    val parsed = rawNum?.toDoubleOrNull()
                    if (parsed != null && parsed >= 1000 && rawNum.length != 16 && rawNum.length != 12) {
                        if (matcher.group(0)?.contains("تومان") == true) {
                            explicitToman = true
                        }
                        extractedAmount = parsed
                        break
                    }
                }
            }
        }

        if (extractedAmount == null || extractedAmount <= 0) {
            return null
        }

        // Bug 2: All bank SMS in Iran are in Rials (ریال). Accounting ledger is in Tomans (تومان).
        // If the SMS is in Rials (or default banking Rial), divide by 10 (remove one zero) to convert to Tomans.
        val finalAmountInTomans = if (explicitToman) {
            extractedAmount
        } else {
            extractedAmount / 10.0
        }

        val bankName = detectBank(normalized, sender)

        return ParsedSms(
            amount = finalAmountInTomans,
            isIncoming = isIncoming,
            bankName = bankName
        )
    }

    private fun detectBank(text: String, sender: String): String {
        val lower = text.lowercase()
        val senderLower = sender.lowercase()

        return when {
            lower.contains("ملت") || senderLower.contains("mellat") -> "بانک ملت"
            lower.contains("ملی") || senderLower.contains("melli") -> "بانک ملی"
            lower.contains("سامان") || senderLower.contains("saman") -> "بانک سامان"
            lower.contains("پاسارگاد") || senderLower.contains("pasargad") -> "بانک پاسارگاد"
            lower.contains("پارسیان") || senderLower.contains("parsian") -> "بانک پارسیان"
            lower.contains("تجارت") || senderLower.contains("tejarat") -> "بانک تجارت"
            lower.contains("صادرات") || senderLower.contains("saderat") -> "بانک صادرات"
            lower.contains("بلو") || lower.contains("blubank") || senderLower.contains("blu") -> "بلوبانک"
            lower.contains("سپه") || senderLower.contains("sepah") -> "بانک سپه"
            lower.contains("رسالت") || senderLower.contains("resalat") -> "بانک رسالت"
            lower.contains("کشاورزی") || senderLower.contains("keshavarzi") -> "بانک کشاورزی"
            lower.contains("رفاه") || senderLower.contains("refah") -> "بانک رفاه"
            lower.contains("آینده") || senderLower.contains("ayandeh") -> "بانک آینده"
            lower.contains("سینا") || senderLower.contains("sina") -> "بانک سینا"
            lower.contains("مسکن") || senderLower.contains("maskan") -> "بانک مسکن"
            lower.contains("شهر") || senderLower.contains("shahr") -> "بانک شهر"
            lower.contains("مهر ایران") || senderLower.contains("mehr") -> "بانک مهر ایران"
            else -> if (sender.isNotBlank()) sender else "پیامک بانکی"
        }
    }
}
