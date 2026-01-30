package org.piramalswasthya.sakhi.utils

import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
object CampaignDateUtil {

    fun parseDateToLocalDate(dateStr: String): java.time.LocalDate? {
        return try {
            val dateFormats = listOf(
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd"
            )
            
            for (format in dateFormats) {
                try {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
                    return java.time.LocalDate.parse(dateStr, formatter)
                } catch (e: Exception) {
                    // Try next format
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun getYearFromDate(dateStr: String): Int {
        return try {
            val date = parseDateToLocalDate(dateStr)
            if (date != null) {
                return date.year
            }

            if (dateStr.length >= 4 && (dateStr[4] == '-' || dateStr[4] == '/')) {
                dateStr.take(4).toInt()
            } 
            else if (dateStr.length >= 10) {
                val lastSeparatorIndex = maxOf(
                    dateStr.lastIndexOf('-'),
                    dateStr.lastIndexOf('/')
                )
                if (lastSeparatorIndex > 0 && lastSeparatorIndex < dateStr.length - 4) {
                    dateStr.substring(lastSeparatorIndex + 1).toInt()
                } else {
                    0
                }
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun isMonthsCompleted(lastDate: String, months: Int): Boolean {
        return try {
            val last = parseDateToLocalDate(lastDate) ?: return false

            val nextAllowed = last.plusMonths(months.toLong())
            val today = java.time.LocalDate.now()

            today.isAfter(nextAllowed) || today.isEqual(nextAllowed)
        } catch (e: Exception) {
            false
        }
    }
}
