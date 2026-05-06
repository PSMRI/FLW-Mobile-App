package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import org.piramalswasthya.sakhi.utils.Log
import java.util.*

@Suppress("DEPRECATION")
class MyContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {

        fun wrap(context: Context, applicationContext: Context, language: String): ContextWrapper {

            val locale = if (language == "bn") {
                Locale("bn", "IN")
            } else {
                Locale(language)
            }

            Locale.setDefault(locale)
            Log.e("LANG_DEBUG", Locale.getDefault().toString())
            val config = Configuration(context.resources.configuration)

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
                val newContext = context.createConfigurationContext(config)
                MyContextWrapper(newContext)
            } else {
                config.locale = locale
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                MyContextWrapper(context)
            }
        }
    }
}