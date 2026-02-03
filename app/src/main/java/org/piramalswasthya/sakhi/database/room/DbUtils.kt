package org.piramalswasthya.sakhi.database.room


/**
 * Note : Never ever change the order of enum states declared. It'll mess everything up.
 */
enum class SyncState {
    UNSYNCED,
    SYNCING,
    SYNCED,
    DRAFT
}
