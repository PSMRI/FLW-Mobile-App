package org.piramalswasthya.sakhi.helpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.piramalswasthya.sakhi.model.LogLevel
import org.piramalswasthya.sakhi.model.SyncLogEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncLogManager @Inject constructor(
    private val fileWriter: SyncLogFileWriter
) {

    companion object {
        private const val MAX_BUFFER_SIZE = 500
    }

    private val buffer = ArrayDeque<SyncLogEntry>(MAX_BUFFER_SIZE)
    private var nextId = 0L
    private val _logs = MutableStateFlow<List<SyncLogEntry>>(emptyList())
    val logs: StateFlow<List<SyncLogEntry>> = _logs.asStateFlow()

    fun addLog(level: LogLevel, tag: String, message: String) {
        synchronized(buffer) {
            if (buffer.size >= MAX_BUFFER_SIZE) {
                buffer.removeFirst()
            }
            buffer.addLast(
                SyncLogEntry(
                    id = nextId++,
                    timestamp = System.currentTimeMillis(),
                    level = level,
                    tag = tag,
                    message = message
                )
            )
            _logs.value = buffer.toList()
        }
        fileWriter.writeLog(level, tag, message)
    }

    fun clearLogs() {
        synchronized(buffer) {
            buffer.clear()
            _logs.value = emptyList()
        }
    }
}
