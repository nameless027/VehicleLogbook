package com.fuellog.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Форматирование значений для отображения (русский формат). */
object Fmt {

    private val ruLocale = Locale("ru")
    private val symbols = DecimalFormatSymbols.getInstance(ruLocale)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", ruLocale)
    private val grouped = DecimalFormat("#,##0.##", symbols)
    private val oneDecimal = DecimalFormat("0.0", symbols)
    private val plain = DecimalFormat("0.##", symbols)

    fun date(date: LocalDate): String = date.format(dateFormatter)

    /** 48200 -> "48.200" */
    fun odometer(value: Int): String {
        val s = value.toString()
        val sb = StringBuilder()
        var count = 0
        for (i in s.length - 1 downTo 0) {
            sb.append(s[i])
            count++
            if (count % 3 == 0 && i != 0) sb.append('.')
        }
        return sb.reverse().toString()
    }

    fun number(value: Double): String = grouped.format(value)

    fun plain(value: Double): String = plain.format(value)

    fun money(value: Double): String = "${number(value)} ₽"

    fun liters(value: Double): String = "${number(value)} л"

    fun quantity(value: Double): String = "${plain(value)} шт"

    fun consumption(value: Double): String = "${oneDecimal.format(value)} л/100 км"

    fun pricePerLiter(value: Double): String = "${number(value)} ₽/л"
}

/** Разбор числа, введённого пользователем: принимает и «.», и «,». */
fun String.parseDoubleFlexible(): Double? {
    val cleaned = trim()
        .replace("\u00A0", "")
        .replace(" ", "")
        .replace(",", ".")
    return cleaned.toDoubleOrNull()
}

/** Оставляет только цифры и один десятичный разделитель при вводе. */
fun sanitizeDecimalInput(raw: String): String {
    val builder = StringBuilder(raw.length)
    var hasSeparator = false
    for (ch in raw) {
        when {
            ch.isDigit() -> builder.append(ch)
            (ch == '.' || ch == ',') && !hasSeparator -> {
                builder.append(ch)
                hasSeparator = true
            }
        }
    }
    return builder.toString()
}