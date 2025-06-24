package org.piramalswasthya.sakhi.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import androidx.collection.lruCache
import androidx.core.graphics.withTranslation
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnitDTO
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object HelperUtil {

    private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.ENGLISH)

    fun getLocalizedResources(context: Context, currentLanguage: Languages): Resources {
        val desiredLocale = Locale(currentLanguage.symbol)
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(desiredLocale)
        val localizedContext: Context = context.createConfigurationContext(conf)
        return localizedContext.resources
    }

    fun getLocalizedContext(context: Context, currentLanguage: Languages): Context {
        val desiredLocale = Locale(currentLanguage.symbol)
        Locale.setDefault(desiredLocale)
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(desiredLocale)
        return context.createConfigurationContext(conf)
    }

    fun formatNumber(number: Int, languages: Languages): Int {
        val locale = Locale(languages.symbol)
        val numberFormatter = NumberFormat.getInstance(locale)
        return numberFormatter.format(number).replace(",", "").toInt()
    }

    fun getDateStringFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        dateLong?.let {
            val dateString = dateFormat.format(dateLong)
            return dateString
        } ?: run {
            return null
        }

    }

    /**
     * gets millis from given years months days
     */
    fun getDobFromAge(ageUnit: AgeUnitDTO): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, 0 - ageUnit.years)
        cal.add(Calendar.MONTH, 0 - ageUnit.months)
        cal.add(Calendar.DAY_OF_MONTH, 0 - ageUnit.days)
        return cal.timeInMillis
    }

    /**
     * gets millis for date in dd-MM-yyyy format
     */
    fun getLongFromDate(dateString: String): Long {
        val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val date = f.parse(dateString)
        return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
    }

    /**
     * gets age string like -- 2 years, 3 months or 3 months, 4 days
     */
    fun getAgeStrFromAgeUnit(ageUnitDTO: AgeUnitDTO): String {
        val str = StringBuilder("")

        if (ageUnitDTO.years >= 1) {
            str.append(ageUnitDTO.years)
            str.append(if (ageUnitDTO.years == 1) " Year" else " Years")
        }

        if (ageUnitDTO.months >= 1) {
            if (ageUnitDTO.years >= 1) str.append(", ")
            str.append(ageUnitDTO.months)
            str.append(if (ageUnitDTO.months == 1) " Month" else " Months")
        }

        if (ageUnitDTO.days >= 1 && ageUnitDTO.years < 1) {
            if (ageUnitDTO.months >= 1) str.append(", ")
            str.append(ageUnitDTO.days)
            str.append(if (ageUnitDTO.days == 1) " Day " else " Days ")
        }
        return str.toString()
    }

    fun getDiffYears(a: Calendar, b: Calendar): Int {
        var diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR)
        if (a.get(Calendar.MONTH) >= b.get(Calendar.MONTH)
            && a.get(
                Calendar.DAY_OF_MONTH
            ) > b.get(
                Calendar.DAY_OF_MONTH
            )
        ) {
            diff--
        }
        return diff
    }

    /**
     * calculates age in years, months, days from calendar and sets it to age dto
     */
    fun updateAgeDTO(ageUnitDTO: AgeUnitDTO, cal: Calendar) {
        val calNow = Calendar.getInstance()
        val calNowLocal = Calendar.getInstance()
        var years = calNow.get(Calendar.YEAR) - cal.get(Calendar.YEAR)
        var months = calNow.get(Calendar.MONTH) - cal.get(Calendar.MONTH)
        if (months < 0) {
            years -= 1
            months += 12
        }

        var days = calNow.get(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH)
        if (days < 0) {
            if (months == 0) {
                years -= 1
                months += 11
                days += 30
            } else {
                months -= 1
                days += 30
            }
        }
        calNowLocal.set(Calendar.YEAR, calNow.get(Calendar.YEAR) - years)
        calNowLocal.set(Calendar.MONTH, calNow.get(Calendar.MONTH) - months)
        ageUnitDTO.years = years
        ageUnitDTO.months = months
        ageUnitDTO.days = days
    }


    fun getTrackDate(long: Long?): String? {
        long?.let {
            return " on ${dateFormat.format(long)}"
        }
        return null
    }

    /**
     * get current date in yyyy-MM-dd HH:mm:ss format
     */
    fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        val dateString = dateFormat.format(millis)
        val timeString = timeFormat.format(millis)
        return "${dateString}T${timeString}.000Z"
    }

    /**
     * get date string in yyyy-MM-dd format from given long date
     */
    fun getDateStrFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        dateLong?.let {
            if (dateLong == 0L) return null
            val dateString = dateFormat.format(dateLong)
            val timeString = timeFormat.format(dateLong)
            return dateString
        } ?: run {
            return null
        }

    }

    fun getDateTimeStringFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        dateLong?.let {
            if (dateLong == 0L) return null
            val dateString = dateFormat.format(dateLong)
            val timeString = timeFormat.format(dateLong)
            return "${dateString}T${timeString}.000Z"
        } ?: run {
            return null
        }

    }

    fun getLongFromDateStr(dateString: String?): Long {
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = dateString?.let { f.parse(it) }
        return date?.time ?: 0L
    }

    fun getLongFromDateMDY(dateString: String?): Long {
        val f = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        val date = dateString?.let { f.parse(it) }
        return date?.time ?: 0L
    }

    fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null
    ) {

        val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-" +
                "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize"

        // The public constructor was deprecated in API level 28,
        // but the builder is only available from API level 23 onwards
        val staticLayout =
            StaticLayoutCache[cacheKey] ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(text, start, end, textPaint, width)
                    .setAlignment(alignment)
                    .setLineSpacing(spacingAdd, spacingMult)
                    .setIncludePad(includePad)
                    .setEllipsizedWidth(ellipsizedWidth)
                    .setEllipsize(ellipsize)
                    .build()
            } else {
                StaticLayout(
                    text, start, end, textPaint, width, alignment,
                    spacingMult, spacingAdd, includePad, ellipsize, ellipsizedWidth
                )
                    .apply { StaticLayoutCache[cacheKey] = this }
            }

        staticLayout.draw(this, x, y)
    }

    private fun StaticLayout.draw(canvas: Canvas, x: Float, y: Float) {
        canvas.withTranslation(x, y) {
            draw(this)
        }
    }

    private object StaticLayoutCache {

        private const val MAX_SIZE = 50 // Arbitrary max number of cached items
        private val cache = lruCache<String, StaticLayout>(MAX_SIZE)

        operator fun set(key: String, staticLayout: StaticLayout) {
            cache.put(key, staticLayout)
        }

        operator fun get(key: String): StaticLayout? {
            return cache[key]
        }
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    fun isValidName(name: String): Boolean {
        val regex = Regex("^[a-zA-Z][a-zA-Z\\s'-]*[a-zA-Z]$")
        return regex.matches(name.trim())
    }


    val allPagesContent = StringBuilder()
    fun saveApiResponseToDownloads(context: Context, fileName: String, content: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            Log.d("SAVE_FILE", "File saved to Downloads: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("SAVE_FILE", "Error saving to Downloads: ${e.message}")
        }
    }

    /*Delivery Outcome DB Check*/
    val deliveryOutcomeDBLog = StringBuilder()
    fun deliveryOutcomeDBLogMethod(context: Context, fileName: String, content: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            Log.d("SAVE_FILE", "File saved to Downloads: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("SAVE_FILE", "Error saving to Downloads: ${e.message}")
        }
    }
    val deliveryOutcomeUpdatePNCWorker = StringBuilder()
    fun deliveryOutcomeUpdatePNCWorkerMethod(context: Context, fileName: String, content: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            Log.d("SAVE_FILE", "File saved to Downloads: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("SAVE_FILE", "Error saving to Downloads: ${e.message}")
        }
    }
    val deliveryOutcomeRepo = StringBuilder()
    fun deliveryOutcomeRepoMethod(context: Context, fileName: String, content: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            Log.d("SAVE_FILE", "File saved to Downloads: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("SAVE_FILE", "Error saving to Downloads: ${e.message}")
        }
    }







}