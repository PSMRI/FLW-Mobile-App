package org.piramalswasthya.sakhi.database.room.dao

/**
 * LIFO (latest created/updated first) order used by beneficiary list queries.
 * SQLite ignores ORDER BY on views without LIMIT, so list queries must apply this explicitly.
 */
object BenListOrder {
    const val LIFO =
        " ORDER BY MAX(IFNULL(updatedDate, 0), IFNULL(createdDate, 0), IFNULL(regDate, 0)) DESC, benId DESC"
    const val LIFO_BEN =
        " ORDER BY MAX(IFNULL(ben.updatedDate, 0), IFNULL(ben.createdDate, 0), IFNULL(ben.regDate, 0)) DESC, ben.benId DESC"
    const val LIFO_B =
        " ORDER BY MAX(IFNULL(b.updatedDate, 0), IFNULL(b.createdDate, 0), IFNULL(b.regDate, 0)) DESC, b.benId DESC"
    const val LIFO_AFTER_ALIVE =
        """, MAX(IFNULL(updatedDate, 0), IFNULL(createdDate, 0), IFNULL(regDate, 0)) DESC, benId DESC"""
}
