package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_rooms")
data class ChatRoom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val username: String = "",
    val isGroup: Boolean = false,
    val imageUrl: String? = null,
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val presenceStatus: String = "online", // "online", "away", "busy", "offline"
    val unreadCount: Int = 0
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomId: Int,
    val senderName: String,
    val senderUsername: String = "",
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "text", // "text", "voice", "image", "video", "file", "call"
    val mediaUrl: String? = null,
    val fileName: String? = null,
    val fileSize: String? = null,
    val duration: String? = null,
    val isMine: Boolean = false,
    val reaction: String? = null,
    val parentMessageId: Int? = null
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Jordan Davis",
    val username: String = "jordan_d",
    val status: String = "Available for calls & chats",
    val statusType: String = "online", // "online", "away", "busy", "offline"
    val avatarUrl: String? = null,
    val wsServerUrl: String = "local://relay"
)
