package org.piramalswasthya.sakhi.helpers.dynamicMapper


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSubmitRequest
import java.text.SimpleDateFormat
import java.util.*

object FormSubmitRequestMapper {

    fun fromEntityList(entities: List<FormResponseJsonEntity>): List<FormSubmitRequest> {
        return entities.mapNotNull { fromEntity(it) }
    }

    fun fromEntity(entity: FormResponseJsonEntity): FormSubmitRequest? {
        return try {
            val jsonObj = JSONObject(entity.formDataJson)
            val fieldsObj = jsonObj.optJSONObject("fields")

            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val fieldsMap: Map<String, Any?> = Gson().fromJson(fieldsObj.toString(), type)

            FormSubmitRequest(
                formId = jsonObj.optString("formId"),
                beneficiaryId = jsonObj.optInt("beneficiaryId"),
                houseHoldId = jsonObj.optInt("houseHoldId"),
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
            input // fallback if not a date
        }
    }
}
