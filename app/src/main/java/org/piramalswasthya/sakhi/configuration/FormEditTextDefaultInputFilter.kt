package org.piramalswasthya.sakhi.configuration

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

object FormEditTextDefaultInputFilter : InputFilter {

    private val pattern = Pattern.compile("[A-Z ]+")

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {

        return if (source.matches(pattern.toRegex())) null else ""
//        if (source == "") {
//            return source;
//        }
//        val matcher = pattern.matcher(source)
//        return if (matcher.matches()) {
//            source
//        } else matcher.group(0)
    }
}