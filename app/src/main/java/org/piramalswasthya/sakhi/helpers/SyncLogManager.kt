package org.piramalswasthya.sakhi.helpers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
        private const val EMIT_DELAY_MS = 250L
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val buffer = ArrayDeque<SyncLogEntry>(MAX_BUFFER_SIZE)
    private var nextId = 0L
    private var emitJob: Job? = null
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
            scheduleEmit()
        }
        fileWriter.writeLog(level, tag, message)
    }

    private fun scheduleEmit() {
        if (emitJob?.isActive == true) return
        emitJob = scope.launch {
            delay(EMIT_DELAY_MS)
            val snapshot = synchronized(buffer) { buffer.toList() }
            _logs.value = snapshot
        }
    }

    fun clearLogs() {
        synchronized(buffer) {
            emitJob?.cancel()
            buffer.clear()
            _logs.value = emptyList()
        }
    }
}
