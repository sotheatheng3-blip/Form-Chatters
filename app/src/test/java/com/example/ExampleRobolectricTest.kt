package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChatMessage
import com.example.data.ChatRoom
import com.example.data.UserProfile
import com.example.data.websocket.WsMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ChatPlatform", appName)
  }

  @Test
  fun `verify user profile with username handle`() {
    val profile = UserProfile(
      name = "Jordan Davis",
      username = "jordan_d",
      status = "Online and encrypted",
      statusType = "online"
    )
    assertEquals("Jordan Davis", profile.name)
    assertEquals("jordan_d", profile.username)
    assertEquals("online", profile.statusType)
  }

  @Test
  fun `verify real user chat room creation`() {
    val room = ChatRoom(
      id = 1,
      name = "Elena Rostova",
      username = "@elena_r",
      lastMessage = "Hey! Let's sync.",
      presenceStatus = "online"
    )
    assertEquals("Elena Rostova", room.name)
    assertEquals("@elena_r", room.username)
    assertEquals("online", room.presenceStatus)
  }

  @Test
  fun `verify message types including media and calls`() {
    val textMsg = ChatMessage(roomId = 1, senderName = "Me", content = "Hello", type = "text")
    val voiceMsg = ChatMessage(roomId = 1, senderName = "Me", content = "Voice message", type = "voice", duration = "0:15")
    val photoMsg = ChatMessage(roomId = 1, senderName = "Me", content = "Photo", type = "image", mediaUrl = "preset://UI")
    val videoMsg = ChatMessage(roomId = 1, senderName = "Me", content = "Video message", type = "video", duration = "0:45")
    val fileMsg = ChatMessage(roomId = 1, senderName = "Me", content = "specs.pdf", type = "file", fileName = "specs.pdf", fileSize = "3.2 MB")
    val callMsg = ChatMessage(roomId = 1, senderName = "Me", content = "📞 Video Call ended (02:14)", type = "call", duration = "02:14")

    assertEquals("text", textMsg.type)
    assertEquals("voice", voiceMsg.type)
    assertEquals("0:15", voiceMsg.duration)
    assertEquals("image", photoMsg.type)
    assertEquals("video", videoMsg.type)
    assertEquals("file", fileMsg.type)
    assertEquals("call", callMsg.type)
  }

  @Test
  fun `verify websocket message payload creation`() {
    val wsMsg = WsMessage(
      event = "CHAT_MESSAGE",
      fromUsername = "jordan_d",
      fromName = "Jordan Davis",
      toUsername = "@elena_r",
      roomId = 1,
      messageType = "video_call",
      content = "Call initiated"
    )
    assertEquals("CHAT_MESSAGE", wsMsg.event)
    assertEquals("jordan_d", wsMsg.fromUsername)
    assertEquals("@elena_r", wsMsg.toUsername)
    assertEquals("video_call", wsMsg.messageType)
  }
}
