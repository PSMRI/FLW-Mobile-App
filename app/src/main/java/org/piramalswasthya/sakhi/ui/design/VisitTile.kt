package org.piramalswasthya.sakhi.ui.design

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import org.piramalswasthya.sakhi.R

/**
 * Redesign visit tile (HBYC months / HBNC days): done = success container + success text, not done = white card with
 * outline and primary text. Replaces the solid green / red setBackgroundColor (which also dropped the card corners).
 */
fun dsVisitTile(card: MaterialCardView, label: TextView, done: Boolean) {
    val ctx = card.context
    card.setCardBackgroundColor(ContextCompat.getColor(ctx, if (done) R.color.ds_success_container else R.color.ds_surface_container))
    card.strokeColor = ContextCompat.getColor(ctx, if (done) R.color.ds_success_container else R.color.ds_outline_variant)
    label.setTextColor(ContextCompat.getColor(ctx, if (done) R.color.ds_success else R.color.ds_primary))
}
