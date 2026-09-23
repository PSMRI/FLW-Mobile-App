package org.piramalswasthya.sakhi.model

import android.graphics.drawable.Drawable
import org.piramalswasthya.sakhi.helpers.Languages

data class Language(
    val id: Int,
    val lanFirstWord: String,
    val lanName: String,
    val lanSelectedView: Drawable,
    val lanUnselectedView: Drawable,
    val language: Languages,
    val isSelected: Boolean = false
)