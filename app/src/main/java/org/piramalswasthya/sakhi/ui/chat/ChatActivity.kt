package org.piramalswasthya.sakhi.ui.chat

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.ActivityChatBinding
import org.piramalswasthya.sakhi.helpers.AudioRecorderHelper
import org.piramalswasthya.sakhi.helpers.NetworkResponse

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var messageAdapter: ChatMessageAdapter
    private lateinit var sessionAdapter: ChatSessionAdapter
    private lateinit var audioRecorder: AudioRecorderHelper

    private var typingAnimator: ObjectAnimator? = null

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                toggleRecording()
            } else {
                Toast.makeText(this, R.string.audio_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        audioRecorder = AudioRecorderHelper(this)

        setupToolbar()
        setupMessageList()
        setupSessionDrawer()
        setupInputBar()
        observeViewModel()

        viewModel.loadSessions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.btnHistory.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun setupMessageList() {
        messageAdapter = ChatMessageAdapter { faq ->
            // User tapped a FAQ chip — send it as a message
            binding.etMessage.setText(faq)
            sendMessage()
        }

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupSessionDrawer() {
        sessionAdapter = ChatSessionAdapter { session ->
            viewModel.switchSession(session)
            sessionAdapter.setSelectedSession(session.sessionId)
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.rvSessions.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = sessionAdapter
        }

        binding.btnNewChat.setOnClickListener {
            viewModel.startNewChat()
            sessionAdapter.setSelectedSession(null)
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    private fun setupInputBar() {
        binding.etMessage.addTextChangedListener {
            val hasText = !it.isNullOrBlank()
            binding.btnSend.visibility = if (hasText) View.VISIBLE else View.GONE
            binding.btnMic.visibility = if (hasText) View.GONE else View.VISIBLE
        }
        // Initial state: show mic, hide send
        binding.btnSend.visibility = View.GONE
        binding.btnMic.visibility = View.VISIBLE

        binding.btnSend.setOnClickListener { sendMessage() }

        binding.btnMic.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) {
                toggleRecording()
            } else {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return
        binding.etMessage.text?.clear()
        viewModel.sendMessage(text)
    }

    private fun toggleRecording() {
        if (audioRecorder.isRecording) {
            // Stop recording and send
            val audioBytes = audioRecorder.stopRecording()
            val fileName = audioRecorder.getOutputFileName() ?: "voice.m4a"
            viewModel.setRecording(false)
            if (audioBytes != null) {
                viewModel.sendAudioMessage(audioBytes, fileName)
            } else {
                Toast.makeText(this, R.string.recording_failed, Toast.LENGTH_SHORT).show()
            }
        } else {
            // Start recording
            audioRecorder.startRecording()
            viewModel.setRecording(true)
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            val hasMessages = messages.isNotEmpty()
            binding.layoutEmptyState.visibility = if (hasMessages) View.GONE else View.VISIBLE
            binding.rvMessages.visibility = if (hasMessages) View.VISIBLE else View.GONE

            messageAdapter.submitList(messages) {
                if (hasMessages) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.sendState.observe(this) { state ->
            when (state) {
                is NetworkResponse.Loading -> showTypingIndicator(true)
                is NetworkResponse.Error -> {
                    showTypingIndicator(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> showTypingIndicator(false)
            }
        }

        viewModel.sessions.observe(this) { state ->
            when (state) {
                is NetworkResponse.Success -> {
                    val sessions = state.data ?: emptyList()
                    sessionAdapter.submitList(sessions)
                    binding.tvNoSessions.visibility =
                        if (sessions.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvSessions.visibility =
                        if (sessions.isEmpty()) View.GONE else View.VISIBLE
                }
                is NetworkResponse.Error -> {
                    binding.tvNoSessions.visibility = View.VISIBLE
                    binding.rvSessions.visibility = View.GONE
                }
                else -> {}
            }
        }

        viewModel.disclaimer.observe(this) { text ->
            if (text.isNullOrBlank()) {
                binding.cardDisclaimer.visibility = View.GONE
            } else {
                binding.cardDisclaimer.visibility = View.VISIBLE
                binding.tvDisclaimer.text = text
            }
        }

        viewModel.isRecording.observe(this) { recording ->
            if (recording) {
                binding.btnMic.setImageResource(R.drawable.baseline_stop_24)
                binding.btnMic.imageTintList =
                    ContextCompat.getColorStateList(this, R.color.colorRecording)
                binding.etMessage.hint = getString(R.string.recording)
                binding.etMessage.isEnabled = false
            } else {
                binding.btnMic.setImageResource(R.drawable.baseline_mic_24)
                binding.btnMic.imageTintList =
                    ContextCompat.getColorStateList(this, R.color.colorPrimary)
                binding.etMessage.hint = getString(R.string.type_message)
                binding.etMessage.isEnabled = true
            }
        }
    }

    private fun showTypingIndicator(show: Boolean) {
        // We use a simple animation on the send button to indicate loading
        if (show) {
            binding.btnSend.isEnabled = false
            binding.btnMic.isEnabled = false
            // Pulse animation on the last message area
            startTypingAnimation()
        } else {
            binding.btnSend.isEnabled = true
            binding.btnMic.isEnabled = true
            stopTypingAnimation()
        }
    }

    private fun startTypingAnimation() {
        // Add a temporary "typing" message
        val currentMessages = viewModel.messages.value ?: emptyList()
        val typingMsg = org.piramalswasthya.sakhi.model.chat.ChatMessage(
            messageId = "typing_indicator",
            role = "assistant",
            content = "...",
            createdAt = ""
        )
        messageAdapter.submitList(currentMessages + typingMsg) {
            binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    private fun stopTypingAnimation() {
        // The typing indicator gets removed when real messages are set
        typingAnimator?.cancel()
        typingAnimator = null
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (audioRecorder.isRecording) {
            audioRecorder.cancelRecording()
        }
    }
}
