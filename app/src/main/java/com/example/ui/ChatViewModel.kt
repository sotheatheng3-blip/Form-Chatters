package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessage
import com.example.data.ChatRepository
import com.example.data.ChatRoom
import com.example.data.UserProfile
import com.example.data.websocket.WebSocketManager
import com.example.data.websocket.WsConnectionStatus
import com.example.data.websocket.WsMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveCallState(
    val isActive: Boolean = false,
    val roomId: Int = 0,
    val contactName: String = "",
    val contactUsername: String = "",
    val isVideo: Boolean = false,
    val isIncoming: Boolean = false,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val callStatusText: String = "Connecting..."
)

class ChatViewModel(
    private val repository: ChatRepository,
    val webSocketManager: WebSocketManager = WebSocketManager()
) : ViewModel() {

    val chatRooms: StateFlow<List<ChatRoom>> = repository.allChatRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val wsConnectionStatus: StateFlow<WsConnectionStatus> = webSocketManager.connectionStatus
    val wsStatusDetail: StateFlow<String> = webSocketManager.statusDetail

    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    private var callTimerJob: Job? = null

    init {
        // Listen to incoming WebSocket messages
        viewModelScope.launch {
            webSocketManager.incomingMessages.collect { wsMsg ->
                handleIncomingWsMessage(wsMsg)
            }
        }
    }

    fun getMessagesForRoom(roomId: Int): StateFlow<List<ChatMessage>> {
        return repository.getMessagesForRoom(roomId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun searchMessages(roomId: Int, query: String): StateFlow<List<ChatMessage>> {
        return repository.searchMessages(roomId, query)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Connect WebSocket using personal account credentials
    fun connectWebSocket(url: String? = null) {
        viewModelScope.launch {
            val profile = repository.getUserProfileOnce() ?: UserProfile()
            val targetUrl = url ?: profile.wsServerUrl
            webSocketManager.connect(
                url = targetUrl,
                username = profile.username,
                name = profile.name
            )
        }
    }

    fun disconnectWebSocket() {
        webSocketManager.disconnect()
    }

    fun sendTestPing() {
        val profile = userProfile.value
        val ping = WsMessage(
            event = "PING",
            fromUsername = profile.username,
            fromName = profile.name,
            content = "Ping test at ${System.currentTimeMillis()}"
        )
        webSocketManager.sendWsMessage(ping)
    }

    // Add other real-user through Name and Username
    fun addRealUser(name: String, username: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val cleanUsername = if (username.startsWith("@")) username else "@$username"
            val existing = repository.findRoomByUsername(cleanUsername)
            if (existing != null) {
                onComplete(existing.id)
                return@launch
            }

            val newRoom = ChatRoom(
                name = name.trim(),
                username = cleanUsername.trim(),
                isGroup = false,
                lastMessage = "Started conversation with $cleanUsername",
                timestamp = System.currentTimeMillis(),
                presenceStatus = "online"
            )
            val newId = repository.insertChatRoom(newRoom).toInt()

            // Transmit user presence / hello via WebSocket if connected
            val profile = userProfile.value
            webSocketManager.sendWsMessage(
                WsMessage(
                    event = "USER_ONLINE",
                    fromUsername = profile.username,
                    fromName = profile.name,
                    toUsername = cleanUsername,
                    roomId = newId,
                    content = "Connected"
                )
            )

            onComplete(newId)
        }
    }

    // Personal Account Profile Update
    fun updateProfile(
        name: String,
        username: String,
        status: String,
        statusType: String,
        wsServerUrl: String
    ) {
        viewModelScope.launch {
            val cleanUsername = username.removePrefix("@").trim()
            val updated = UserProfile(
                id = 1,
                name = name.trim(),
                username = cleanUsername,
                status = status.trim(),
                statusType = statusType,
                wsServerUrl = wsServerUrl.trim()
            )
            repository.updateUserProfile(updated)
            // Reconnect websocket with new credentials
            webSocketManager.connect(updated.wsServerUrl, updated.username, updated.name)
        }
    }

    fun addReaction(messageId: Int, reaction: String) {
        viewModelScope.launch {
            repository.updateMessageReaction(messageId, reaction)
        }
    }

    // Messaging: Text
    fun sendTextMessage(roomId: Int, content: String) {
        sendMessageInternal(roomId = roomId, content = content, type = "text")
    }

    // Messaging: Voice Message
    fun sendVoiceMessage(roomId: Int, duration: String) {
        sendMessageInternal(
            roomId = roomId,
            content = "Voice message",
            type = "voice",
            duration = duration
        )
    }

    // Messaging: Picture
    fun sendPictureMessage(roomId: Int, imageUrl: String, caption: String = "") {
        sendMessageInternal(
            roomId = roomId,
            content = if (caption.isNotBlank()) caption else "Photo",
            type = "image",
            mediaUrl = imageUrl
        )
    }

    // Messaging: Video
    fun sendVideoMessage(roomId: Int, videoUrl: String, duration: String = "0:30") {
        sendMessageInternal(
            roomId = roomId,
            content = "Video message",
            type = "video",
            mediaUrl = videoUrl,
            duration = duration
        )
    }

    // Messaging: File / Document
    fun sendFileMessage(roomId: Int, fileName: String, fileSize: String = "1.8 MB") {
        sendMessageInternal(
            roomId = roomId,
            content = fileName,
            type = "file",
            fileName = fileName,
            fileSize = fileSize
        )
    }

    private fun sendMessageInternal(
        roomId: Int,
        content: String,
        type: String,
        mediaUrl: String? = null,
        fileName: String? = null,
        fileSize: String? = null,
        duration: String? = null
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val room = repository.getChatRoomById(roomId)
            val recipientUsername = room?.username ?: ""

            val localMessage = ChatMessage(
                roomId = roomId,
                senderName = profile.name,
                senderUsername = profile.username,
                content = content,
                type = type,
                mediaUrl = mediaUrl,
                fileName = fileName,
                fileSize = fileSize,
                duration = duration,
                isMine = true,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(localMessage)

            // Send via WebSocket
            val wsMsg = WsMessage(
                event = "CHAT_MESSAGE",
                fromUsername = profile.username,
                fromName = profile.name,
                toUsername = recipientUsername,
                roomId = roomId,
                messageType = type,
                content = content,
                mediaUrl = mediaUrl,
                fileName = fileName,
                fileSize = fileSize,
                duration = duration,
                timestamp = localMessage.timestamp
            )
            webSocketManager.sendWsMessage(wsMsg)
        }
    }

    // Voice & Video Call handling
    fun startCall(roomId: Int, isVideo: Boolean) {
        viewModelScope.launch {
            val room = repository.getChatRoomById(roomId) ?: return@launch
            val profile = userProfile.value

            _activeCall.value = ActiveCallState(
                isActive = true,
                roomId = roomId,
                contactName = room.name,
                contactUsername = room.username,
                isVideo = isVideo,
                isIncoming = false,
                durationSeconds = 0,
                callStatusText = "Ringing..."
            )

            // Broadcast call invite via WebSocket
            webSocketManager.sendWsMessage(
                WsMessage(
                    event = "CALL_INVITE",
                    fromUsername = profile.username,
                    fromName = profile.name,
                    toUsername = room.username,
                    roomId = roomId,
                    messageType = if (isVideo) "video_call" else "voice_call",
                    content = if (isVideo) "Incoming Video Call" else "Incoming Voice Call"
                )
            )

            // Simulate call connection after short ring
            delay(1500)
            _activeCall.value = _activeCall.value?.copy(callStatusText = "Connected")
            startCallTimer()
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let {
                    it.copy(durationSeconds = it.durationSeconds + 1)
                } ?: break
            }
        }
    }

    fun toggleMute() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleVideo() {
        _activeCall.value = _activeCall.value?.let { it.copy(isVideoEnabled = !it.isVideoEnabled) }
    }

    fun switchCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun endCall() {
        callTimerJob?.cancel()
        val current = _activeCall.value ?: return
        val durationFormatted = formatDuration(current.durationSeconds)
        val callTypeDesc = if (current.isVideo) "Video Call" else "Voice Call"
        val summary = "📞 $callTypeDesc ended ($durationFormatted)"

        viewModelScope.launch {
            // Insert call log into chat history
            repository.insertMessage(
                ChatMessage(
                    roomId = current.roomId,
                    senderName = userProfile.value.name,
                    senderUsername = userProfile.value.username,
                    content = summary,
                    type = "call",
                    duration = durationFormatted,
                    isMine = true
                )
            )

            // Transmit CALL_END over WebSocket
            webSocketManager.sendWsMessage(
                WsMessage(
                    event = "CALL_END",
                    fromUsername = userProfile.value.username,
                    fromName = userProfile.value.name,
                    toUsername = current.contactUsername,
                    roomId = current.roomId,
                    content = summary
                )
            )
        }
        _activeCall.value = null
    }

    private fun handleIncomingWsMessage(wsMsg: WsMessage) {
        viewModelScope.launch {
            val myUsername = userProfile.value.username
            // Ignore messages from self if echo server reflects them back
            if (wsMsg.fromUsername.equals(myUsername, ignoreCase = true) ||
                wsMsg.fromUsername.equals("@$myUsername", ignoreCase = true)) {
                return@launch
            }

            when (wsMsg.event) {
                "CHAT_MESSAGE" -> {
                    // Find or create room
                    val contactUsername = wsMsg.fromUsername
                    var room = repository.findRoomByUsername(contactUsername)
                    if (room == null) {
                        val newRoomId = repository.insertChatRoom(
                            ChatRoom(
                                name = wsMsg.fromName,
                                username = contactUsername,
                                isGroup = false,
                                lastMessage = wsMsg.content,
                                presenceStatus = "online"
                            )
                        ).toInt()
                        room = repository.getChatRoomById(newRoomId)
                    }

                    if (room != null) {
                        repository.insertMessage(
                            ChatMessage(
                                roomId = room.id,
                                senderName = wsMsg.fromName,
                                senderUsername = wsMsg.fromUsername,
                                content = wsMsg.content,
                                type = wsMsg.messageType,
                                mediaUrl = wsMsg.mediaUrl,
                                fileName = wsMsg.fileName,
                                fileSize = wsMsg.fileSize,
                                duration = wsMsg.duration,
                                isMine = false,
                                timestamp = wsMsg.timestamp
                            )
                        )
                    }
                }
                "CALL_INVITE" -> {
                    val isVideo = wsMsg.messageType == "video_call"
                    val room = repository.findRoomByUsername(wsMsg.fromUsername)
                    _activeCall.value = ActiveCallState(
                        isActive = true,
                        roomId = room?.id ?: 0,
                        contactName = wsMsg.fromName,
                        contactUsername = wsMsg.fromUsername,
                        isVideo = isVideo,
                        isIncoming = true,
                        callStatusText = "Incoming ${if (isVideo) "Video" else "Voice"} Call..."
                    )
                }
                "CALL_ANSWER" -> {
                    _activeCall.value = _activeCall.value?.copy(callStatusText = "Connected")
                    startCallTimer()
                }
                "CALL_END" -> {
                    endCall()
                }
                "USER_ONLINE" -> {
                    repository.updateRoomPresenceByUsername(wsMsg.fromUsername, "online")
                }
            }
        }
    }

    private fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    fun addInitialData() {
        viewModelScope.launch {
            if (chatRooms.value.isEmpty()) {
                val defaultProfile = UserProfile(
                    name = "Jordan Davis",
                    username = "jordan_d",
                    status = "Ready for calls & secure chat",
                    statusType = "online",
                    wsServerUrl = "local://relay"
                )
                repository.updateUserProfile(defaultProfile)

                val aliceId = repository.insertChatRoom(
                    ChatRoom(
                        name = "Sarah Miller",
                        username = "@sarah_m",
                        imageUrl = "avatar_alice",
                        lastMessage = "Voice message (0:24)",
                        presenceStatus = "online"
                    )
                ).toInt()

                val bobId = repository.insertChatRoom(
                    ChatRoom(
                        name = "Jordan Vance",
                        username = "@jordan_v",
                        imageUrl = "avatar_bob",
                        lastMessage = "project_brief_v2.pdf",
                        presenceStatus = "online"
                    )
                ).toInt()

                val designSyncId = repository.insertChatRoom(
                    ChatRoom(
                        name = "Design Sync 🎨",
                        username = "@design_team",
                        imageUrl = "group_icon",
                        lastMessage = "Alex: Check the new mobile prototypes...",
                        isGroup = true,
                        presenceStatus = "online"
                    )
                ).toInt()

                // Initial messages
                repository.insertMessage(
                    ChatMessage(
                        roomId = aliceId,
                        senderName = "Sarah Miller",
                        senderUsername = "@sarah_m",
                        content = "Voice message",
                        type = "voice",
                        duration = "0:24",
                        isMine = false
                    )
                )

                repository.insertMessage(
                    ChatMessage(
                        roomId = bobId,
                        senderName = "Jordan Vance",
                        senderUsername = "@jordan_v",
                        content = "project_brief_v2.pdf",
                        type = "file",
                        fileName = "project_brief_v2.pdf",
                        fileSize = "2.4 MB",
                        isMine = false
                    )
                )

                // Connect WebSocket automatically
                connectWebSocket(defaultProfile.wsServerUrl)
            }
        }
    }

    class Factory(
        private val repository: ChatRepository,
        private val webSocketManager: WebSocketManager = WebSocketManager()
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(repository, webSocketManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
