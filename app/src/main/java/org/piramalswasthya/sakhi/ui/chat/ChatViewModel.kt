package org.piramalswasthya.sakhi.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.NetworkResponse
import org.piramalswasthya.sakhi.model.chat.ChatMessage
import org.piramalswasthya.sakhi.model.chat.ChatSession
import org.piramalswasthya.sakhi.model.chat.SendMessageData
import org.piramalswasthya.sakhi.repositories.ChatRepository
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    // ── Session list (drawer) ──

    private val _sessions = MutableLiveData<NetworkResponse<List<ChatSession>>>(NetworkResponse.Idle())
    val sessions: LiveData<NetworkResponse<List<ChatSession>>> = _sessions

    // ── Current conversation ──

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _sendState = MutableLiveData<NetworkResponse<SendMessageData>>(NetworkResponse.Idle())
    val sendState: LiveData<NetworkResponse<SendMessageData>> = _sendState

    private val _disclaimer = MutableLiveData<String?>()
    val disclaimer: LiveData<String?> = _disclaimer

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    var currentSessionId: String? = null
        private set

    // ── Actions ──

    fun loadSessions() {
        viewModelScope.launch {
            _sessions.value = NetworkResponse.Loading()
            val result = chatRepository.getSessions()
            _sessions.value = when (result) {
                is NetworkResponse.Success -> NetworkResponse.Success(result.data!!.sessions)
                is NetworkResponse.Error -> NetworkResponse.Error(result.message ?: "Error")
                else -> result as NetworkResponse<List<ChatSession>>
            }
        }
    }

    fun switchSession(session: ChatSession) {
        currentSessionId = session.sessionId
        _disclaimer.value = null
        loadMessages(session.sessionId)
    }

    fun startNewChat() {
        currentSessionId = null
        _messages.value = emptyList()
        _disclaimer.value = null
        _sendState.value = NetworkResponse.Idle()
    }

    private fun loadMessages(sessionId: String) {
        viewModelScope.launch {
            _sendState.value = NetworkResponse.Loading()
            val result = chatRepository.getMessages(sessionId)
            when (result) {
                is NetworkResponse.Success -> {
                    _messages.value = result.data!!.messages
                    _sendState.value = NetworkResponse.Idle()
                }
                is NetworkResponse.Error -> {
                    _sendState.value = NetworkResponse.Error(result.message ?: "Error loading messages")
                }
                else -> {}
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // Optimistically add user message to the list
            val tempUserMsg = ChatMessage(
                messageId = "temp_${System.currentTimeMillis()}",
                role = "user",
                content = text,
                createdAt = ""
            )
            _messages.value = (_messages.value ?: emptyList()) + tempUserMsg
            _sendState.value = NetworkResponse.Loading()

            // Auto-create session if needed
            if (currentSessionId == null) {
                val createResult = chatRepository.createSession()
                when (createResult) {
                    is NetworkResponse.Success -> {
                        currentSessionId = createResult.data!!.sessionId
                        _disclaimer.value = createResult.data.disclaimer
                    }
                    is NetworkResponse.Error -> {
                        _sendState.value = NetworkResponse.Error(
                            createResult.message ?: "Failed to create session"
                        )
                        // Remove optimistic message
                        _messages.value = (_messages.value ?: emptyList()).dropLast(1)
                        return@launch
                    }
                    else -> return@launch
                }
            }

            // Send message
            val result = chatRepository.sendMessage(
                sessionId = currentSessionId!!,
                content = text
            )
            when (result) {
                is NetworkResponse.Success -> {
                    val data = result.data!!
                    // Replace temp message with real user message, add assistant message
                    val currentMessages = (_messages.value ?: emptyList()).toMutableList()
                    // Remove temp user message
                    if (currentMessages.isNotEmpty()) currentMessages.removeAt(currentMessages.lastIndex)
                    currentMessages.add(data.userMessage)
                    currentMessages.add(data.assistantMessage)
                    _messages.value = currentMessages
                    _sendState.value = NetworkResponse.Idle()
                    // Refresh sessions in background
                    loadSessions()
                }
                is NetworkResponse.Error -> {
                    _sendState.value = NetworkResponse.Error(result.message ?: "Failed to send")
                }
                else -> {}
            }
        }
    }

    fun sendAudioMessage(audioBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _sendState.value = NetworkResponse.Loading()

            // Auto-create session if needed
            if (currentSessionId == null) {
                val createResult = chatRepository.createSession()
                when (createResult) {
                    is NetworkResponse.Success -> {
                        currentSessionId = createResult.data!!.sessionId
                        _disclaimer.value = createResult.data.disclaimer
                    }
                    is NetworkResponse.Error -> {
                        _sendState.value = NetworkResponse.Error(
                            createResult.message ?: "Failed to create session"
                        )
                        return@launch
                    }
                    else -> return@launch
                }
            }

            // Add placeholder for user audio message
            val tempMsg = ChatMessage(
                messageId = "temp_audio_${System.currentTimeMillis()}",
                role = "user",
                content = "[Voice message]",
                contentType = "audio_transcript",
                createdAt = ""
            )
            _messages.value = (_messages.value ?: emptyList()) + tempMsg

            val result = chatRepository.sendAudioMessage(
                sessionId = currentSessionId!!,
                audioBytes = audioBytes,
                fileName = fileName
            )
            when (result) {
                is NetworkResponse.Success -> {
                    val data = result.data!!
                    val currentMessages = (_messages.value ?: emptyList()).toMutableList()
                    if (currentMessages.isNotEmpty()) currentMessages.removeAt(currentMessages.lastIndex)
                    currentMessages.add(data.userMessage)
                    currentMessages.add(data.assistantMessage)
                    _messages.value = currentMessages
                    _sendState.value = NetworkResponse.Idle()
                    loadSessions()
                }
                is NetworkResponse.Error -> {
                    _sendState.value = NetworkResponse.Error(result.message ?: "Audio send failed")
                }
                else -> {}
            }
        }
    }

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }
}
