package org.piramalswasthya.sakhi.model

data class SyncLogEntry(
    val id: Long,
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String
)

enum class LogLevel { DEBUG, INFO, WARN, ERROR }
