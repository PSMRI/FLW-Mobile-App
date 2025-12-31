package org.piramalswasthya.sakhi.model

import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import java.text.SimpleDateFormat
import java.util.Locale

data class HomeVisitDomain(
    val id: Int,
    val benId: Long,
    val visitNumber: Int,
    val visitDate: Long,
    val visitDateString: String,
    val formDataJson: String,
    val syncState: SyncState,
    val isSynced: Boolean
) {
    companion object {
        fun fromEntity(entity: ANCFormResponseJsonEntity, visitNumber: Int): HomeVisitDomain {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val visitDate = try {
                dateFormat.parse(entity.visitDate)?.time ?: entity.createdAt
            } catch (e: Exception) {
                entity.createdAt
            }

            return HomeVisitDomain(
                id = entity.id,
                benId = entity.benId,
                visitNumber = visitNumber,
                visitDate = visitDate,
                visitDateString =  entity.visitDate,
                formDataJson = entity.formDataJson,
                syncState = if (entity.isSynced) SyncState.SYNCED else SyncState.UNSYNCED,
                isSynced = entity.isSynced
            )
        }
    }
}