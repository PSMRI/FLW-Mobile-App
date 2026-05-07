package org.piramalswasthya.sakhi.helpers

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*


@Suppress("DEPRECATION")
class MyContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, applicationContext: Context, language: String): ContextWrapper {
            updateBaseContextLocale(language,context)
          /*  val config: Configuration = context.resources.configuration
            val sysLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getSystemLocale(config)
            } else {
                getSystemLocaleLegacy(config)
            }
            if (language != "" && !sysLocale.language.equals(language)) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, locale)
                } else {
                    setSystemLocaleLegacy(config, locale)
                }
                context.resources
                    .updateConfiguration(config, context.resources.displayMetrics)
                applicationContext.resources
                    .updateConfiguration(config, applicationContext.resources.displayMetrics)
            }*/



            return MyContextWrapper(context)
        }

        private fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun getSystemLocale(config: Configuration): Locale {
            return config.locales.get(0)
        }

        private fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun setSystemLocale(config: Configuration, locale: Locale?) {
            config.setLocale(locale)
        }



        fun updateBaseContextLocale(selectedLang:String,context: Context): Context? {
            val locale = Locale(selectedLang)
            Locale.setDefault(locale)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                updateResourcesLocale(context, locale)
            } else updateResourcesLocaleLegacy(context, locale)
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun updateResourcesLocale(context: Context, locale: Locale): Context? {
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)

        }

        fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context? {
            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration
            configuration.locale = locale
            resources.updateConfiguration(configuration, resources.displayMetrics)
            return context
        }



    }

}