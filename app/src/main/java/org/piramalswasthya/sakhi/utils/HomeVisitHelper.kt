package org.piramalswasthya.sakhi.utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.HomeVisitDomain
import java.text.SimpleDateFormat
import java.util.*

object HomeVisitHelper  {
    private const val HOME_VISIT_FORM_ID = "anc_form_001"
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun isHomeVisit(entity: ANCFormResponseJsonEntity): Boolean {
        return entity.formId == HOME_VISIT_FORM_ID
    }

    fun extractVisitNumber(formDataJson: String): Int {
        return try {
            val jsonObject = gson.fromJson(formDataJson, JsonObject::class.java)
            jsonObject.get("visitNumber")?.asInt ?: 1
        } catch (e: Exception) {
            1
        }
    }

    fun getVisitDate(entity: ANCFormResponseJsonEntity): Long {
        return try {
            dateFormat.parse(entity.visitDate)?.time ?: entity.createdAt
        } catch (e: Exception) {
            entity.createdAt
        }
    }


    fun getSortedHomeVisits(
        formResponses: List<ANCFormResponseJsonEntity>
    ): List<HomeVisitDomain> {
        val homeVisitEntities = formResponses.filter { isHomeVisit(it) }

        val sortedEntities = homeVisitEntities.sortedBy { getVisitDate(it) }

        return sortedEntities.mapIndexed { index, entity ->
            HomeVisitDomain.fromEntity(entity, index + 1)
        }
    }

    fun getNextVisitNumber(formResponses: List<ANCFormResponseJsonEntity>): Int {
        val homeVisits = formResponses.filter { isHomeVisit(it) }
        return homeVisits.size + 1
    }
}