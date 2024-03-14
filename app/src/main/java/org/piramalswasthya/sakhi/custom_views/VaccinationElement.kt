package org.piramalswasthya.sakhi.custom_views
/*

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemImmunizationStatusBinding
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.model.VaccineClickListener
import timber.log.Timber


class VaccinationElement : LinearLayout {

    private val nameWidth = resources.getDimensionPixelSize(R.dimen.status_name_width)
    private val itemWidth = resources.getDimensionPixelSize(R.dimen.status_button_width)

    private lateinit var data: ImmunizationDetailsDomain

    constructor(
        context: Context?,
    ) : super(context)

    constructor(
        context: Context?,
        attrs: AttributeSet?,
    ) : super(context, attrs)

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr)

    fun setData(data: ImmunizationDetailsDomain?, clickListener: VaccineClickListener?) {
        Timber.d("Set Data Called!!!")
        this.removeAllViews()
        val nameLayoutParam = LayoutParams(nameWidth, LayoutParams.WRAP_CONTENT)
        val itemLayoutParam = LayoutParams(itemWidth, itemWidth)
        data?.let { this.data = it }
        val nameTxt = inflate(this.context, R.layout.rv_item_immunization_base, null)
        (nameTxt as TextView).text = data?.name
        this.addView(nameTxt, nameLayoutParam)
        Timber.d("Ben Name Added to linear layout!")
        data?.vaccineStateList?.forEach { vaccine ->
            val item = RvItemImmunizationStatusBinding.inflate(LayoutInflater.from(context))
            item.vaccine = vaccine
            item.benId = this.data.benId
            item.clickListener = clickListener
            this.addView(item.root, itemLayoutParam)

        }


    }

}*/
