package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.text.isDigitsOnly
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream


object ImageUtils {
    suspend fun saveBenImageFromCameraToStorage(
        context: Context, uriString: String, benId: Long
    ): String {
        return withContext(Dispatchers.IO) {
            val targetFile = File(context.filesDir, "${benId}.jpeg").also { it.createNewFile() }
            val os: OutputStream = FileOutputStream(targetFile)
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
                os.write(it.readBytes())
                os.flush()
            }
            Timber.d("Uncompressed target file :->$targetFile ${targetFile.length()}")
            val compressedFile = Compressor.compress(context, targetFile) {
                quality(80)
            }
            removeAllTemporaryBenImages(context)
            compressedFile.renameTo(targetFile)
            Uri.fromFile(targetFile).toString()
        }
    }

    private fun removeAllTemporaryBenImages(context: Context) {
        context.cacheDir.absoluteFile.listFiles { file ->
            file.name.startsWith(Konstants.tempBenImagePrefix)
        }?.forEach {
            it.delete()
        }
    }

    private fun removeAllStoredBenImages(context: Context) {
        context.filesDir.absoluteFile.listFiles { file ->
            file.name.isDigitsOnly() && file.name.endsWith("jpeg")
        }?.forEach {
            it.delete()
        }
    }

    fun removeAllBenImages(context: Context) {
        removeAllStoredBenImages(context)
        removeAllTemporaryBenImages(context)
    }

    suspend fun saveBenImageFromServerToStorage(
        context: Context, encodedString: String, benId: Long
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputByteArray = Base64.decode(encodedString, Base64.DEFAULT)
                val targetFile = File(context.filesDir, "${benId}.jpeg").also { it.createNewFile() }
                FileOutputStream(targetFile).use {
                    it.write(inputByteArray)
                    it.flush()
                }
                val compressedFile = Compressor.compress(context, targetFile) {
                    quality(80)
                }
                compressedFile.renameTo(targetFile)
                Timber.d("Compressed target file :->$targetFile ${targetFile.length()}")
                Uri.fromFile(targetFile).toString()

            } catch (e: java.lang.Exception) {
                Timber.d("Compress failed with error $e ${e.localizedMessage} ${e.stackTrace}")
                null
            }
        }
    }

    fun getEncodedStringForBenImage(context: Context, beneficiaryId: Long): String? {
        return File(context.filesDir, "${beneficiaryId}.jpeg").takeIf { it.exists() }?.run {
            val inputStream = FileInputStream(this)
            val byteArray = inputStream.readBytes()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

    }

    fun renameImage(context: Context, oldBenId: Long, newBenId: Long): String? {
        val originalFile = File(context.filesDir, "${oldBenId}.jpeg")
        if (!originalFile.exists())
            return null
        val renamedFile = File(context.filesDir, "${newBenId}.jpeg")
        originalFile.apply {
            if (this.exists())
                this.renameTo(renamedFile)

        }
        return Uri.fromFile(renamedFile).toString()
    }


}