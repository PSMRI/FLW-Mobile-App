package org.piramalswasthya.sakhi.model

import com.google.gson.annotations.SerializedName

data class FamilyMember(
    @SerializedName(value = "abhaId", alternate = ["abhId"])
    val abhId: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("age")
    val age: String? = null,

    @SerializedName(value = "dob_secc", alternate = ["dob"])
    val dob: String? = null,

    @SerializedName("block")
    val block: String? = null,

    @SerializedName("block_Code")
    val blockCode: String? = null,

    @SerializedName(value = "cardNo", alternate = ["cardno"])
    val cardNo: String? = null,

    @SerializedName("district")
    val district: String? = null,

    @SerializedName("district_Code")
    val districtCode: String? = null,

    @SerializedName(value = "familyid", alternate = ["familyId"])
    val familyId: String? = null,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("mobileNo")
    val mobileNo: String? = null,

    @SerializedName(value = "personName", alternate = ["name"])
    val name: String? = null,

    @SerializedName("rural_Urban")
    val ruralUrban: String? = null,

    @SerializedName("villagename")
    val villageName: String? = null,

    @SerializedName("village_Code")
    val villageCode: String? = null,

    @SerializedName(value = "vvs", alternate = ["vws"])
    val vws: String? = null,

    @SerializedName("ward")
    val ward: String? = null
) {
    /** True when at least one form-fillable field carries a usable (non-blank) value. */
    fun hasUsableData(): Boolean =
        !name.isNullOrBlank() ||
                !gender.isNullOrBlank() ||
                !mobileNo.isNullOrBlank() ||
                !dob.isNullOrBlank()
}