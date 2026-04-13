package com.example.seguimiento1.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun timeAgo(millis: Long): String {
        val diff = System.currentTimeMillis() - millis
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000

        return when {
            minutes < 1 -> "1 min"
            minutes < 60 -> "$minutes min"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.format(Date(millis))
            }
        }
    }

    fun isToday(millis: Long): Boolean {
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()
        cal.timeInMillis = millis
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(millis: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    }

    fun formatMonthYear(millis: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("es"))
        return sdf.format(Date(millis))
    }
}
