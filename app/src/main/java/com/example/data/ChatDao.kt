package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_rooms ORDER BY timestamp DESC")
    fun getAllChatRooms(): Flow<List<ChatRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(chatRoom: ChatRoom): Long

    @Query("SELECT * FROM chat_rooms WHERE id = :id LIMIT 1")
    suspend fun getChatRoomById(id: Int): ChatRoom?

    @Query("SELECT * FROM chat_rooms WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun findRoomByUsername(username: String): ChatRoom?

    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(roomId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long
    
    @Query("UPDATE chat_rooms SET lastMessage = :lastMessage, timestamp = :timestamp WHERE id = :roomId")
    suspend fun updateRoomLastMessage(roomId: Int, lastMessage: String, timestamp: Long)

    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId AND content LIKE '%' || :query || '%' ORDER BY timestamp ASC")
    fun searchMessages(roomId: Int, query: String): Flow<List<ChatMessage>>

    @Query("UPDATE chat_messages SET reaction = :reaction WHERE id = :messageId")
    suspend fun updateMessageReaction(messageId: Int, reaction: String?)

    @Query("UPDATE chat_rooms SET presenceStatus = :status WHERE id = :roomId")
    suspend fun updateRoomPresence(roomId: Int, status: String)

    @Query("UPDATE chat_rooms SET presenceStatus = :status WHERE LOWER(username) = LOWER(:username)")
    suspend fun updateRoomPresenceByUsername(username: String, status: String)

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserProfile(profile: UserProfile)
}
