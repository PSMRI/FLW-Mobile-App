package org.piramalswasthya.sakhi.configuration

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern


object DecimalDigitsInputFilter :
    InputFilter {
    var regex: Pattern = Pattern.compile("[0-9]{1,3}\\.[0-9]")

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {
        if (source == "") {
            return source
        }
        if (source.toString().matches(regex.toRegex())) {
            return source
        }
        return ""
    }
}