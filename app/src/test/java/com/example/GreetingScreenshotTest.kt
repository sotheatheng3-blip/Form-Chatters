package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.ChatRoom
import com.example.ui.screens.ChatRoomItem
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun chat_room_item_screenshot() {
    val sampleRoom = ChatRoom(
      id = 1,
      name = "Sarah Miller",
      username = "@sarah_m",
      lastMessage = "Voice message (0:24)",
      presenceStatus = "online"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        ChatRoomItem(room = sampleRoom, onClick = {})
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/chat_room_item.png")
  }
}
