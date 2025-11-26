package org.piramalswasthya.sakhi.utils


import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StringMappingUtil {

    // Mapping of Indian language digits to English digits
    private val digitMap = mapOf(
        '०' to '0', '१' to '1', '२' to '2', '३' to '3', '४' to '4',  // Hindi/Marathi
        '५' to '5', '६' to '6', '७' to '7', '८' to '8', '९' to '9',

        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',  // Bengali/Assamese
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    /** Converts any string containing Indian-language digits to English digits */
    fun convertDigits(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        return input.map { digitMap[it] ?: it }.joinToString("")
    }

    /** Convert date dd-MM-yyyy (with language digits) → yyyy-MM-dd */
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertDate(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        return try {
                val english = convertDigits(input)
                val inFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val outFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                LocalDate.parse(english, inFmt).format(outFmt)
            } catch (e: Exception) {
                ""
            }
    }

    /** Recursively convert Strings, Numbers, Lists, Maps */
    fun toEnglishDigits(data: Any?): Any? {
        return when (data) {
            is String -> convertDigits(data)
            is Number -> convertDigits(data.toString())
            is List<*> -> data.map { toEnglishDigits(it) }
            is Array<*> -> data.map { toEnglishDigits(it) }.toTypedArray()
            is Map<*, *> -> data.mapValues { toEnglishDigits(it.value) }
            else -> data
        }
    }
}
