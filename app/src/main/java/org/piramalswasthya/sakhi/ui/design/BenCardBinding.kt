package org.piramalswasthya.sakhi.ui.design

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.Gender

/*
 * Redesign (Figma "FLW App Redesign" › 05 · Household members, "Beneficiary card"): display-only helpers for the
 * white beneficiary card. They read the same fields the old card showed; nothing here changes data or behaviour.
 */

/** Avatar initials: first letter of the first two words of the name ("Ramesh Kumar" → "RK"; "baby of Kavita" → "K"). */
@BindingAdapter("dsInitials")
fun TextView.setDsInitials(name: String?) {
    // Phone pass 2026-11-12: newborns are named "baby of <mother>" — skip that prefix so the avatar isn't always "BO"
    text = name.orEmpty().trim().replace(Regex("^baby\\s+of\\s+", RegexOption.IGNORE_CASE), "").split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
}

/** Sync badge pill: Synced (green) / Not synced (amber) / Syncing (blue); hidden when the screen hides sync state. */
@BindingAdapter("dsSyncBadge")
fun TextView.setDsSyncBadge(state: SyncState?) {
    if (state == null) {
        visibility = View.GONE
        return
    }
    visibility = View.VISIBLE
    val (label, bg, fg, icon) = when (state) {
        SyncState.SYNCED -> Quad(R.string.ds_synced, R.color.ds_success_container, R.color.ds_success, R.drawable.ic_ds_check_circle)
        SyncState.UNSYNCED -> Quad(R.string.ds_not_synced, R.color.ds_warning_container, R.color.ds_warning, R.drawable.ic_ds_cloud_off)
        SyncState.SYNCING -> Quad(R.string.ds_syncing, R.color.ds_primary_container, R.color.ds_primary, R.drawable.ic_ds_sync)
    }
    setText(label)
    val fgColor = ContextCompat.getColor(context, fg)
    setTextColor(fgColor)
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, bg))
    val iconPx = (14 * resources.displayMetrics.density).toInt()
    val drawable = ContextCompat.getDrawable(context, icon)?.mutate()?.apply { setBounds(0, 0, iconPx, iconPx) }
    setCompoundDrawablesRelative(drawable, null, null, null)
    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(fgColor))
}

private data class Quad(val label: Int, val bg: Int, val fg: Int, val icon: Int)

/** Beneficiary ID on a white card (the old benIdText painted it white for the blue card). */
@BindingAdapter("dsBenIdText")
fun TextView.setDsBenIdText(benId: Long?) {
    benId ?: return
    if (benId < 0L) {
        text = "Pending Sync"
        setTextColor(ContextCompat.getColor(context, R.color.ds_warning))
    } else {
        text = benId.toString()
        setTextColor(ContextCompat.getColor(context, R.color.ds_on_surface))
    }
}

/** Same rule as the old "rchId" adapter, but GONE instead of INVISIBLE so an empty grid cell leaves no gap. */
@BindingAdapter("dsRchIdVisible")
fun View.setDsRchIdVisible(ben: BenBasicDomain?) {
    ben ?: return
    val gender = ben.gender
    visibility =
        if (gender == Gender.FEMALE.name || (gender == Gender.MALE.name && ben.ageInt < Konstants.minAgeForGenBen))
            View.VISIBLE
        else
            View.GONE
}
