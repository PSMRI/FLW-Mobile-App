package org.piramalswasthya.sakhi.contracts

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import org.piramalswasthya.sakhi.R
import java.util.Locale


class SpeechToTextContract : ActivityResultContract<Unit, String>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
        return intent
    }

    /**
     * Some devices ship without a speech recognizer; launching the intent there throws
     * ActivityNotFoundException. Resolve it first and, if nothing can handle it, return an empty
     * result straight away (same as a cancelled dictation) instead of launching.
     */
    override fun getSynchronousResult(context: Context, input: Unit): SynchronousResult<String>? {
        val canRecognize = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .resolveActivity(context.packageManager) != null
        if (canRecognize) return null
        Toast.makeText(context, R.string.speech_to_text_unavailable, Toast.LENGTH_SHORT).show()
        return SynchronousResult("")
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        return intent?.takeIf { resultCode == RESULT_OK }?.let {
            it.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )?.first()
        } ?: ""

    }
}