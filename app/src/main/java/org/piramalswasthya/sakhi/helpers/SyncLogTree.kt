package org.piramalswasthya.sakhi.helpers

import android.util.Log
import org.piramalswasthya.sakhi.model.LogLevel
import timber.log.Timber

class SyncLogTree(
    private val syncLogManager: SyncLogManager
) : Timber.Tree() {

    companion object {
        private val SYNC_TAG_KEYWORDS = listOf(
            "Worker", "Sync", "Push", "Pull", "Amrit"
        )
        private val BRACKET_PATTERN = Regex("\\[\\w+Worker]")
    }

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.DEBUG
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!isSyncRelated(tag, message)) return

        val level = when {
            priority >= Log.ERROR -> LogLevel.ERROR
            priority >= Log.WARN -> LogLevel.WARN
            priority >= Log.INFO -> LogLevel.INFO
            else -> LogLevel.DEBUG
        }

        val fullMessage = if (t != null) "$message: ${t.message}" else message
        syncLogManager.addLog(level, tag ?: "Sync", fullMessage)
    }

    private fun isSyncRelated(tag: String?, message: String): Boolean {
        if (tag != null && SYNC_TAG_KEYWORDS.any { tag.contains(it, ignoreCase = true) }) {
            return true
        }
        if (BRACKET_PATTERN.containsMatchIn(message)) {
            return true
        }
        return false
    }
}
