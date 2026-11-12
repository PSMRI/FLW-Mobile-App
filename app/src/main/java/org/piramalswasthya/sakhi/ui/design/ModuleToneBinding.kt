package org.piramalswasthya.sakhi.ui.design

import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import org.piramalswasthya.sakhi.R

/**
 * Redesign (Figma "FLW App Redesign" › 03 · Home – Modules, "Module tile"): each module has its own
 * tone pair (light background + dark foreground). The tone is picked from the module's icon so the
 * IconDataset stays untouched. Unknown icons fall back to the household (brand blue) tone.
 */
object ModuleTone {

    data class Tone(@ColorRes val bg: Int, @ColorRes val fg: Int)

    private val HOUSEHOLD = Tone(R.color.ds_module_household_bg, R.color.ds_module_household_fg)
    private val PEOPLE = Tone(R.color.ds_module_people_bg, R.color.ds_module_people_fg)
    private val COUPLE = Tone(R.color.ds_module_couple_bg, R.color.ds_module_couple_fg)
    private val MATERNAL = Tone(R.color.ds_module_maternal_bg, R.color.ds_module_maternal_fg)
    private val CHILD = Tone(R.color.ds_module_child_bg, R.color.ds_module_child_fg)
    private val DISEASE = Tone(R.color.ds_module_disease_bg, R.color.ds_module_disease_fg)
    private val CARE = Tone(R.color.ds_module_care_bg, R.color.ds_module_care_fg)
    private val IMMUNIZATION = Tone(R.color.ds_module_immunization_bg, R.color.ds_module_immunization_fg)
    private val VILLAGE = Tone(R.color.ds_module_village_bg, R.color.ds_module_village_fg)
    private val RECORDS = Tone(R.color.ds_module_records_bg, R.color.ds_module_records_fg)

    // Home grid modules, tones as in the Figma frame
    private val byIcon: Map<Int, Tone> = mapOf(
        R.drawable.ic__hh to HOUSEHOLD,
        R.drawable.ic__ben to PEOPLE,
        R.drawable.ic__eligible_couple to COUPLE,
        R.drawable.ic__maternal_health to MATERNAL,
        R.drawable.ic__child_care to CHILD,
        R.drawable.disease_control_icon to DISEASE,
        R.drawable.referral to CARE,
        R.drawable.ic_vaccines to IMMUNIZATION,
        R.drawable.ic__hrp to DISEASE,
        R.drawable.ic__general_op to RECORDS,
        R.drawable.ic__death to RECORDS,
        R.drawable.ic__village_level_form to VILLAGE,
    )

    fun of(@DrawableRes icon: Int): Tone = byIcon[icon] ?: HOUSEHOLD
}

/** Icon square: module tone background + icon tinted with the module foreground. */
@BindingAdapter("dsModuleIcon")
fun ImageView.setDsModuleIcon(@DrawableRes icon: Int) {
    val tone = ModuleTone.of(icon)
    setImageResource(icon)
    imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, tone.fg))
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, tone.bg))
}

/** Count text in the module foreground colour. */
@BindingAdapter("dsModuleToneText")
fun TextView.setDsModuleToneText(@DrawableRes icon: Int) {
    setTextColor(ContextCompat.getColor(context, ModuleTone.of(icon).fg))
}

/** Module tile title on one flowing line: the old centred tiles used "\n" inside titles ("All\nHousehold"). */
@BindingAdapter("dsTileTitle")
fun TextView.setDsTileTitle(title: String?) {
    // text = title?.replace('\n', ' ')
    // Phone pass 2026-11-12: a long hyphenated word ("Non-Communicable") was split mid-word — allow a break after "-"
    text = title?.replace('\n', ' ')?.replace("-", "-\u200B")
}
