package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import timber.log.Timber
import java.io.File

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var _isRecording = false
    val isRecording: Boolean get() = _isRecording

    fun startRecording(): File {
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        outputFile = file

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(96000)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        _isRecording = true
        return file
    }

    fun stopRecording(): ByteArray? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording = false
            outputFile?.readBytes()
        } catch (e: Exception) {
            Timber.e(e, "Error stopping recording")
            cancelRecording()
            null
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
            // Ignore - recorder may not have started
        }
        mediaRecorder = null
        _isRecording = false
        outputFile?.delete()
        outputFile = null
    }

    fun getOutputFileName(): String? = outputFile?.name
}
