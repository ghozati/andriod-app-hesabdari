package com.example.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

    fun format(amount: Double, withCurrency: Boolean = true): String {
        val rawFormatted = formatter.format(kotlin.math.abs(amount))
        val prefix = if (amount < 0) "− " else ""
        return if (withCurrency) "$prefix$rawFormatted تومان" else "$prefix$rawFormatted"
    }

    fun formatNumber(number: Int): String {
        return formatter.format(number)
    }

    fun formatNumber(number: Long): String {
        return formatter.format(number)
    }

    // Keep function signature for backward compatibility, but always return ASCII numbers
    fun toPersianDigits(text: String): String {
        return text
    }
}
