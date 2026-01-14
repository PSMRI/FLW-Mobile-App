package org.piramalswasthya.sakhi.utils
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.Base64
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.collection.lruCache
import androidx.core.content.FileProvider
import androidx.core.graphics.withTranslation
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.LayoutMediaOptionsBinding
import org.piramalswasthya.sakhi.databinding.LayoutViewMediaBinding
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnitDTO
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
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

    fun getDateStringFromLongStraight(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
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

        if (ageUnitDTO.days >= 1 /*&& ageUnitDTO.years < 1*/) {
            if (ageUnitDTO.years >= 1 || ageUnitDTO.months >= 1) str.append(", ")
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

    fun parseDateToMillis(dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
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

    fun getYearRange(timeMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 31)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return start to end
    }

    fun populateAntraTable(
        context: Context,
        tableLayout: TableLayout,
        visits: List<EligibleCoupleTrackingCache>
    ) {
        val headerCount = 1
        if (tableLayout.childCount > headerCount) {
            tableLayout.removeViews(headerCount, tableLayout.childCount - headerCount)
        }

        val lp = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(4, 4, 4, 4)
        }

        fun createBox(text: String?) = TextView(context).apply {
            this.text = text ?: "-"
            layoutParams = lp
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.drawable.editbox_background)
        }

        visits.forEach { visit ->
            tableLayout.addView(
                TableRow(context).apply {
                    listOf(
                        visit.antraDose,
                        visit.dateOfAntraInjection,
                        visit.dueDateOfAntraInjection
                    ).forEach { addView(createBox(it)) }
                }
            )
        }
    }

    fun detectExtAndMime(bytes: ByteArray): Pair<String, String> {
        if (bytes.size >= 4) {
            if (bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x44.toByte() && bytes[3] == 0x46.toByte()) {
                return "pdf" to "application/pdf"
            }
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
                return "jpg" to "image/jpeg"
            }
            if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) {
                return "png" to "image/png"
            }
        }
        return "bin" to "application/octet-stream"
    }
    fun showReminderDialog(
        title: String,
        message: String,
        positiveText: String,
        negativeText: String? = null,
        onPositive: (() -> Unit)? = null,
        onNegative: (() -> Unit)? = null,
        cancelable: Boolean = false,
        context: Context
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .apply {
                setPositiveButton(positiveText) { dialog, _ ->
                    dialog.dismiss(); onPositive?.invoke()
                }
                negativeText?.let {
                    setNegativeButton(it) { dialog, _ ->
                        dialog.dismiss(); onNegative?.invoke()
                    }
                }
            }
            .setCancelable(cancelable)
            .show()
    }

    fun Context.showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    fun getFileName(uri: android.net.Uri, appContext: Context): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
            }
        } else {
            uri.path?.let { path -> File(path).name }
        }
    }
    fun MutableMap<Int, Uri?>.hasUploadedFile(): Boolean =
        values.any { it != null }

    fun Context.createTempImageUri(): Uri {
        val tmpFile = File.createTempFile("uwin_img_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        return FileProvider.getUriForFile(this, "$packageName.provider", tmpFile)
    }
    fun copyToTemp(uri: android.net.Uri, nameHint: String, appContext: Context): File? {
        return try {
            val suffix = nameHint.substringAfterLast('.', missingDelimiterValue = "")
            val temp = if (suffix.isNotEmpty()) File.createTempFile("maa_upload_", ".${suffix}", appContext.cacheDir) else File.createTempFile("maa_upload_", null, appContext.cacheDir)
            appContext.contentResolver.openInputStream(uri)?.use { ins ->
                FileOutputStream(temp).use { outs -> ins.copyTo(outs) }
            }
            temp
        } catch (_: Exception) { null }
    }

    fun compressImageToTemp(uri: Uri, nameHint: String, appContext: Context): File? {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            appContext.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            val (srcW, srcH) = opts.outWidth to opts.outHeight
            if (srcW <= 0 || srcH <= 0) return copyToTemp(uri, nameHint,appContext)
            val maxDim = 1280
            var sample = 1
            while (srcW / sample > maxDim || srcH / sample > maxDim) sample *= 2
            val opts2 = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = appContext.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts2) } ?: return copyToTemp(uri, nameHint, appContext)
            val temp = File.createTempFile("maa_img_", ".jpg", appContext.cacheDir)
            FileOutputStream(temp).use { fos -> bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos) }
            temp
        } catch (_: Exception) { null }
    }

    fun Context.showImageDialog(uri: Uri) {
        val binding = LayoutViewMediaBinding.inflate(LayoutInflater.from(this))
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(binding.root)
            .setCancelable(true)
            .create()
        Glide.with(this).load(uri).placeholder(R.drawable.ic_person)
            .into(binding.viewImage)

        binding.btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun convertToServerDate(local: String?): String? {
        if (local.isNullOrBlank()) return null
        val parts = local.split("-")
        if (parts.size != 3) return local
        return "${parts[2]}-${parts[1]}-${parts[0]}"
    }

    fun Context.showMediaOptionsDialog(
        onCameraClick: () -> Unit,
        onGalleryClick: () -> Unit
    ) {
        if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            val binding = LayoutMediaOptionsBinding.inflate(LayoutInflater.from(this))
            binding.btnPdf.visibility = View.GONE

            val dialog = MaterialAlertDialogBuilder(this)
                .setView(binding.root)
                .setCancelable(true)
                .create()

            binding.btnCamera.setOnClickListener { dialog.dismiss(); onCameraClick() }
            binding.btnGallery.setOnClickListener { dialog.dismiss(); onGalleryClick() }
            binding.btnCancel.setOnClickListener { dialog.dismiss() }

            dialog.show()

        }

    }

    fun Context.showUploadReminderDialog(
        message: String,
        onNo: () -> Unit
    ) {
        showReminderDialog(
            title = getString(R.string.reminder),
            message = message,
            positiveText = getString(R.string.yes_dialog),
            negativeText = getString(R.string.no_dialog),
            onPositive = {},
            onNegative = onNo,
            context = this
        )
    }








    fun base64ToTempFile(base64: String, cacheDir: File, context: Context): Uri? {
        return runCatching {
            val base64Data = base64.substringAfter(",", base64)
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val (ext, _) = detectExtAndMime(bytes)
            val file = File(cacheDir, "uwin_${System.currentTimeMillis()}.$ext")
            file.writeBytes(bytes)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        }.getOrNull()
    }

     fun getMaleRelationId(relToHeadId: Int): Int {
        return when (relToHeadId) {
        // add husbands
            18 -> 8      // daughter in law and son
            16 -> 19     // grand daughter and other
            14 -> 12     // mother in law and father in law
            12 -> 11     // grand mother and grand father
            10 -> 16     // daughter and son in law
            1 -> 1     // Mother
            19 -> 4     // Mother

            else -> 19
        }
    }

     fun getFemaleRelationId(relToHeadId: Int): Int {
        // add wife
        return when (relToHeadId) {

            9 -> 17     // son- daugther in law
            2 -> 0     // father and mother
            7 -> 17     // nephew and daughter in law
            17 -> 10    // son in law and Daughter
            15 -> 19     // grand son and other
            13 -> 13     // father in law and mother in law
            11 -> 12     // father in law and mother in law
            19 -> 5    // father in law and mother in law

            else -> 19
        }
    }

    fun convertToLocalDate(server: String?): String? {
        if (server.isNullOrBlank()) return null
        val parts = server.split("-")
        if (parts.size != 3) return server
        return "${parts[2]}-${parts[1]}-${parts[0]}"
    }

    fun getMimeFromUri(uri: Uri): String {
        val path = uri.toString().lowercase()
        return when {
            path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
            path.endsWith(".png") -> "image/png"
            path.endsWith(".pdf") -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    fun launchCamera(context: Context): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    fun launchFilePicker(launcher: ActivityResultLauncher<Intent>) {
        val mimeTypes = arrayOf("image/jpeg", "image/png", "application/pdf")
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        val chooser = Intent.createChooser(intent, "Select File (PDF, JPG, PNG)")
        launcher.launch(chooser)
    }

    fun Context.getFileSizeInMB(uri: Uri): Double? {
        return try {
            contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val sizeInBytes = pfd.statSize
                if (sizeInBytes > 0) sizeInBytes / (1024.0 * 1024.0) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fileToBase64(file: File): String {
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun showPickerDialog(
        context: Context,
        onCameraSelected: () -> Unit,
        onFileSelected: () -> Unit
    ) {
        val options = arrayOf("Take Photo", "Choose File (PDF / Image)")
        AlertDialog.Builder(context)
            .setTitle("Select File")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onCameraSelected()
                    1 -> onFileSelected()
                }
            }
            .show()
    }

    fun checkAndShowMUACAlert(context: Context, muacValue: String): Boolean {
        val muac = muacValue.toFloatOrNull() ?: return false

        if (muac <= 11.5f) {
            showAlertDialog(
                context,
                "SAM Case Detected",
                "MUAC is $muac cm. Please refer the Child to NRC as SAM case."
            )
            return true
        }
        return false
    }

    fun checkAndShowWeightForHeightAlert(context: Context, status: String): Boolean {
        if (status == "SAM") {
            showAlertDialog(
                context,
                "SAM Case Detected",
                "Weight-for-Height Status is SAM. Please refer the Child to NRC as SAM case."
            )
            return true
        }
        return false
    }

    fun checkAndShowSAMAlert(context: Context, fieldId: String, value: Any?): Boolean {
        return when (fieldId) {
            "muac" -> {
                val muacValue = when (value) {
                    is String -> value
                    is Number -> value.toString()
                    else -> null
                }
                muacValue?.let { checkAndShowMUACAlert(context, it) } ?: false
            }
            "weight_for_height_status" -> {
                val status = value?.toString()
                status?.let { checkAndShowWeightForHeightAlert(context, it) } ?: false
            }
            else -> false
        }
    }

    private fun showAlertDialog(context: Context, title: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    fun parseSelections(rawValue: String?, entries: Array<String>?): List<String> {
        val raw = rawValue?.trim() ?: return emptyList()
        if (raw.isEmpty()) return emptyList()

        if (raw.startsWith("[") && raw.endsWith("]")) {
            try {
                val arr = JSONArray(raw)
                val result = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i, "").trim()
                    if (item.isNotEmpty()) result.add(item)
                }
                if (result.isNotEmpty()) return result
            } catch (_: Exception) {
            }
        }

        if (raw.contains(",")) {
            return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

        if (raw.contains("|")) {
            return raw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        }

        val found = mutableListOf<Pair<Int, String>>()
        val lowerRaw = raw.lowercase()

        for (entry in entries!!) {
            val idx = lowerRaw.indexOf(entry.lowercase())
            if (idx >= 0) found.add(idx to entry)
        }

        if (found.isNotEmpty()) {
            return found.sortedBy { it.first }.map { it.second }
        }

        return listOf(raw)
    }

}