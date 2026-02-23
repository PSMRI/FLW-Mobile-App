package org.piramalswasthya.sakhi.helpers

import android.util.Log
import org.piramalswasthya.sakhi.model.LogLevel
import timber.log.Timber

/**
 * Intercepts Timber logs and routes sync-related entries to [SyncLogManager]
 * for display in the Sync Dashboard logs tab.
 *
 * Extends [Timber.DebugTree] (not [Timber.Tree]) so that auto-generated tags
 * from the calling class name are available. Without this, tags are null for
 * most Timber calls and the keyword filter drops everything.
 */
class SyncLogTree(
    private val syncLogManager: SyncLogManager
) : Timber.DebugTree() {

    companion object {
        private val SYNC_TAG_KEYWORDS = listOf(
            "Worker", "Sync", "Push", "Pull", "Amrit", "Repo"
        )
        private val SYNC_MESSAGE_KEYWORDS = listOf(
            "sync", "push", "pull", "worker", "batch",
            "beneficiary", "amrit", "upload", "download"
        )

        // Safety net: promote log level when message content indicates an error
        // but the caller used the wrong Timber method (e.g. Timber.d for errors).
        private val ERROR_CONTENT_PATTERNS = listOf(
            "exception", "error occurred", "something bad happened",
            "socket timeout", "constraint"
        )
        private val FAILURE_CONTENT_PATTERNS = listOf(
            "failed", "worker failed"
        )
        private val FAILURE_EXCLUSIONS = listOf(
            "failed out of", "succeeded", "synced"
        )
    }

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.DEBUG
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!isSyncRelated(tag, message)) return

        // Write sync-related logs to logcat via DebugTree.log().
        // In debug builds this duplicates what DebugTree already outputs, but
        // in release builds (where DebugTree is not planted) this is the ONLY
        // path to logcat — essential for field debugging with adb.
        super.log(priority, tag, message, t)

        val level = when {
            priority >= Log.ERROR -> LogLevel.ERROR
            priority >= Log.WARN -> LogLevel.WARN
            priority >= Log.INFO -> LogLevel.INFO
            else -> LogLevel.DEBUG
        }

        val fullMessage = if (t != null) "$message: ${t.message}" else message
        val effectiveLevel = promoteIfError(level, fullMessage, t)
        syncLogManager.addLog(effectiveLevel, tag ?: "Sync", fullMessage)
    }

    private fun promoteIfError(level: LogLevel, message: String, t: Throwable?): LogLevel {
        if (t != null && level.ordinal < LogLevel.ERROR.ordinal) return LogLevel.ERROR
        if (level.ordinal >= LogLevel.WARN.ordinal) return level

        val lower = message.lowercase()
        if (ERROR_CONTENT_PATTERNS.any { lower.contains(it) }) return LogLevel.ERROR
        if (FAILURE_CONTENT_PATTERNS.any { lower.contains(it) }) {
            if (FAILURE_EXCLUSIONS.none { lower.contains(it) }) return LogLevel.WARN
        }
        return level
    }

    private fun isSyncRelated(tag: String?, message: String): Boolean {
        if (tag != null && SYNC_TAG_KEYWORDS.any { tag.contains(it, ignoreCase = true) }) {
            return true
        }
        if (SYNC_MESSAGE_KEYWORDS.any { message.contains(it, ignoreCase = true) }) {
            return true
        }
        return false
    }
}
