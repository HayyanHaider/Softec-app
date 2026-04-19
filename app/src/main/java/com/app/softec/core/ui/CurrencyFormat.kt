package com.app.softec.core.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatCurrencyWithPrefix(
    amount: Double,
    prefix: String
): String {
    val sanitizedPrefix = prefix.trim().take(3)
    val decimalFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.getDefault()))
    return if (sanitizedPrefix.isBlank()) {
        decimalFormat.format(amount)
    } else {
        "$sanitizedPrefix ${decimalFormat.format(amount)}"
    }
}
