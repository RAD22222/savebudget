package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
)

object CurrencyUtils {

    val supportedCurrencies: List<CurrencyOption> = listOf(
        CurrencyOption("", "", "None (No symbol)"),
        CurrencyOption("USD", "$", "US Dollar"),
        CurrencyOption("EUR", "€", "Euro"),
        CurrencyOption("GBP", "£", "British Pound"),
        CurrencyOption("INR", "₹", "Indian Rupee"),
        CurrencyOption("JPY", "¥", "Japanese Yen"),
        CurrencyOption("CAD", "CA$", "Canadian Dollar"),
        CurrencyOption("AUD", "A$", "Australian Dollar"),
        CurrencyOption("CHF", "CHF", "Swiss Franc"),
        CurrencyOption("CNY", "¥", "Chinese Yuan"),
        CurrencyOption("KRW", "₩", "South Korean Won"),
        CurrencyOption("BRL", "R$", "Brazilian Real"),
        CurrencyOption("MXN", "MX$", "Mexican Peso"),
        CurrencyOption("RUB", "₽", "Russian Ruble"),
        CurrencyOption("UAH", "₴", "Ukrainian Hryvnia"),
        CurrencyOption("PLN", "zł", "Polish Zloty"),
        CurrencyOption("TRY", "₺", "Turkish Lira"),
        CurrencyOption("IDR", "Rp", "Indonesian Rupiah"),
        CurrencyOption("SGD", "S$", "Singapore Dollar"),
        CurrencyOption("AED", "د.إ", "UAE Dirham"),
        CurrencyOption("BDT", "৳", "Bangladeshi Taka"),
        CurrencyOption("PHP", "₱", "Philippine Peso"),
        CurrencyOption("VND", "₫", "Vietnamese Dong")
    )

    private fun buildNumberFormatter(hasDecimals: Boolean): DecimalFormat {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        return if (hasDecimals) {
            DecimalFormat("#,##0.##", symbols)
        } else {
            DecimalFormat("#,##0", symbols)
        }
    }

    /**
     * Formats an amount cleanly like Buckwheat ("1,000", "100", "45.5", "-20")
     * with optional currency symbol.
     */
    fun formatAmount(amount: Double, currencySymbol: String = ""): String {
        val isNegative = amount < -0.004
        val absVal = abs(amount)
        val rounded = kotlin.math.round(absVal * 100.0) / 100.0
        val hasFraction = abs(rounded - floor(rounded)) > 0.004
        val formatted = buildNumberFormatter(hasFraction).format(rounded)
        val prefix = if (isNegative) "-" else ""
        return if (currencySymbol.isEmpty()) {
            "$prefix$formatted"
        } else {
            "$prefix$currencySymbol$formatted"
        }
    }

    /** Formats raw user keypad string while typing (preserves trailing dot or trailing zero after dot). */
    fun formatInputDisplay(rawInput: String, currencySymbol: String = ""): String {
        if (rawInput.isEmpty()) return ""
        val parts = rawInput.split(".")
        val intPart = parts[0]
        val formattedInt = try {
            if (intPart.isEmpty()) "0"
            else buildNumberFormatter(false).format(intPart.toLong())
        } catch (_: NumberFormatException) {
            intPart
        }
        val combined = if (parts.size > 1) {
            "$formattedInt.${parts[1]}"
        } else {
            formattedInt
        }
        return if (currencySymbol.isEmpty()) combined else "$currencySymbol$combined"
    }
}
