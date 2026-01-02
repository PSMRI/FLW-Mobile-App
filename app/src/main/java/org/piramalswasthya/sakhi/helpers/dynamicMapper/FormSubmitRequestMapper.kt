package org.piramalswasthya.sakhi.helpers.dynamicMapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSubmitRequest
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity
import java.text.SimpleDateFormat
import java.util.*

object FormSubmitRequestMapper {

    fun fromEntity(entity: FormResponseJsonEntity, userName: String): FormSubmitRequest? {
        return mapCommon(entity.formDataJson, userName)
    }
    fun fromEntity(entity: MosquitoNetFormResponseJsonEntity, userName: String): FormSubmitRequest? {
        return mapCommon(entity.formDataJson, userName)
    }
    fun fromEntity(entity: BenIfaFormResponseJsonEntity, userName: String): FormSubmitRequest? {
            return mapCommon(entity.formDataJson, userName)
        }

    fun fromEntity(entity: FormResponseJsonEntityHBYC, userName: String): FormSubmitRequest? {
        return mapCommon(entity.formDataJson, userName)
    }
    fun fromEntity(entity: EyeSurgeryFormResponseJsonEntity, userName: String): FormSubmitRequest? {
        return mapCommon(entity.formDataJson, userName)
    }
    fun fromEntity(entity: CUFYFormResponseJsonEntity, userName: String): FormSubmitRequest? {
            return mapCommon(entity.formDataJson, userName)
    }
    fun fromEntity(entity: FilariaMDAFormResponseJsonEntity, userName: String): FormSubmitRequest? {
            return mapCommon(entity.formDataJson, userName)
    }
    fun formEntity(entity: ANCFormResponseJsonEntity, userName: String): FormSubmitRequest? {
        return  mapCommon(entity.formDataJson,userName)
    }

    private fun mapCommon(formDataJson: String, userName: String): FormSubmitRequest? {
        return try {
            val jsonObj = JSONObject(formDataJson)
            val fieldsObj = jsonObj.optJSONObject("fields")

            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val fieldsMap: Map<String, Any?> = Gson().fromJson(fieldsObj.toString(), type)

            FormSubmitRequest(
                userName = userName,
                formId = jsonObj.optString("formId"),
                beneficiaryId = jsonObj.optLong("beneficiaryId"),
                houseHoldId = jsonObj.optLong("houseHoldId"),
                visitDate = jsonObj.optString("visitDate"),
                fields = fieldsMap
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun convertDateToIso(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input
        }
    }
}
