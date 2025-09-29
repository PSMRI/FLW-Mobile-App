package org.piramalswasthya.sakhi.model

import android.net.Uri

data class PreviewItem(
    val label: String,
    val value: String,
    val isImage: Boolean = false,
    val imageUri: Uri? = null
)

