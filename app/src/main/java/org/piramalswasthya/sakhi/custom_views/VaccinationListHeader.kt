package org.piramalswasthya.sakhi.custom_views

/*


class VaccinationListHeader : LinearLayout {

    private lateinit var data : ImmunizationDetailsHeader

    private val nameWidth = resources.getDimensionPixelSize(R.dimen.status_name_width)
    private val itemWidth = resources.getDimensionPixelSize(R.dimen.status_button_width)

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

    fun setData(data: ImmunizationDetailsHeader){
        Timber.d("Set Data Called!!!")
        val nameLayoutParam = LayoutParams(nameWidth, LayoutParams.WRAP_CONTENT)
        val itemLayoutParam = LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT)
        this.data = data
        val nameTxt = inflate(this.context, R.layout.rv_item_immunization_base, null)
        (nameTxt as TextView).text = "Name"
        nameTxt.gravity = Gravity.CENTER
//        nameTxt.layoutParams = itemLayoutParam
        this.addView(nameTxt,nameLayoutParam)
        Timber.d("Ben Name Added to linear layout!")
        data.list.forEach {
            val item =  inflate(this.context, R.layout.rv_item_immunization_status_header, null)
            (item as TextView).text = it
            item.gravity = Gravity.CENTER
//            val item = inflate(this.context, R.layout.rv_item_immunization_status, null)
            this.addView(item,itemLayoutParam)
        }




    }

}*/
