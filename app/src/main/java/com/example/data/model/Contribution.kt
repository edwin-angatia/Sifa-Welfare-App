package com.example.data.model

import java.util.Calendar

data class MonthContribution(
    val monthIndex: Int, // 1..12
    val monthName: String,
    val monthShort: String,
    val amountKsh: Int = 100,
    val isPaid: Boolean,
    val isCurrentMonth: Boolean
)

object ContributionHelper {
    val MONTH_NAMES = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val MONTH_SHORTS = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun getYearMonths(paidMonths: List<Int>): List<MonthContribution> {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        return (1..12).map { m ->
            MonthContribution(
                monthIndex = m,
                monthName = MONTH_NAMES[m - 1],
                monthShort = MONTH_SHORTS[m - 1],
                amountKsh = 100,
                isPaid = paidMonths.contains(m),
                isCurrentMonth = m == currentMonth
            )
        }
    }

    fun getSalutation(fullName: String?): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val (greeting, emoji) = when (hour) {
            in 5..11 -> Pair("Good morning", "🌅")
            in 12..16 -> Pair("Good afternoon", "☀️")
            in 17..20 -> Pair("Good evening", "🌇")
            else -> Pair("Good evening", "🌙")
        }
        val cleanName = fullName?.trim() ?: ""
        val firstName = if (cleanName.isNotBlank()) {
            cleanName.split("\\s+".toRegex()).firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Member"
        } else {
            "Member"
        }
        return "$greeting, $firstName $emoji"
    }

    fun getMonthName(month: Int): String {
        return MONTH_NAMES.getOrNull(month - 1) ?: "Month $month"
    }
}
