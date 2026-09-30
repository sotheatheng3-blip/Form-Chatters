package com.example.data

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allChatRooms: Flow<List<ChatRoom>> = chatDao.getAllChatRooms()
    val userProfile: Flow<UserProfile?> = chatDao.getUserProfile()
    
    fun getMessagesForRoom(roomId: Int): Flow<List<ChatMessage>> = chatDao.getMessagesForRoom(roomId)

    fun searchMessages(roomId: Int, query: String): Flow<List<ChatMessage>> = chatDao.searchMessages(roomId, query)

    suspend fun getChatRoomById(id: Int): ChatRoom? = chatDao.getChatRoomById(id)

    suspend fun findRoomByUsername(username: String): ChatRoom? = chatDao.findRoomByUsername(username)

    suspend fun updateMessageReaction(messageId: Int, reaction: String?) {
        chatDao.updateMessageReaction(messageId, reaction)
    }

    suspend fun updateRoomLastMessage(roomId: Int, lastMessage: String, timestamp: Long) {
        chatDao.updateRoomLastMessage(roomId, lastMessage, timestamp)
    }

    suspend fun insertMessage(message: ChatMessage): Long {
        val id = chatDao.insertMessage(message)
        val preview = when (message.type) {
            "voice" -> "🎙️ Voice message (${message.duration ?: "0:15"})"
            "image" -> "📷 Picture"
            "video" -> "🎥 Video (${message.duration ?: "0:30"})"
            "file" -> "📄 ${message.fileName ?: "File"}"
            "call" -> message.content
            else -> message.content
        }
        chatDao.updateRoomLastMessage(message.roomId, preview, message.timestamp)
        return id
    }

    suspend fun insertChatRoom(chatRoom: ChatRoom): Long {
        return chatDao.insertChatRoom(chatRoom)
    }

    suspend fun updateRoomPresence(roomId: Int, status: String) {
        chatDao.updateRoomPresence(roomId, status)
    }

    suspend fun updateRoomPresenceByUsername(username: String, status: String) {
        chatDao.updateRoomPresenceByUsername(username, status)
    }

    suspend fun getUserProfileOnce(): UserProfile? {
        return chatDao.getUserProfileOnce()
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        chatDao.updateUserProfile(profile)
    }
}
