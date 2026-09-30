package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.ChatRepository
import com.example.ui.ChatViewModel
import com.example.ui.screens.CallDialog
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ChatRoomScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var repository: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "chat-database"
        ).fallbackToDestructiveMigration().build()
        repository = ChatRepository(database.chatDao())

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1C1B1F)
                ) {
                    ChatApp(repository)
                }
            }
        }
    }
}

@Composable
fun ChatApp(repository: ChatRepository) {
    val navController = rememberNavController()
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(repository))

    val chatRooms by viewModel.chatRooms.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val wsStatus by viewModel.wsConnectionStatus.collectAsState()
    val wsStatusDetail by viewModel.wsStatusDetail.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.addInitialData()
    }

    // Full screen overlay for active Voice / Video Call
    activeCall?.let { callState ->
        CallDialog(
            callState = callState,
            onMuteToggle = { viewModel.toggleMute() },
            onSpeakerToggle = { viewModel.toggleSpeaker() },
            onVideoToggle = { viewModel.toggleVideo() },
            onSwitchCamera = { viewModel.switchCamera() },
            onEndCall = { viewModel.endCall() },
            onAnswerCall = { /* In-call answer logic */ }
        )
    }

    NavHost(navController = navController, startDestination = "chat_list") {
        composable("chat_list") {
            ChatListScreen(
                chatRooms = chatRooms,
                userProfile = userProfile,
                wsStatus = wsStatus,
                onChatClicked = { roomId -> navController.navigate("chat_room/$roomId") },
                onProfileClicked = { navController.navigate("profile") },
                onAddUser = { name, username ->
                    viewModel.addRealUser(name, username) { newRoomId ->
                        navController.navigate("chat_room/$newRoomId")
                    }
                }
            )
        }

        composable(
            "chat_room/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.IntType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getInt("roomId") ?: return@composable
            val room = chatRooms.find { it.id == roomId }
            val messages by viewModel.getMessagesForRoom(roomId).collectAsState()

            ChatRoomScreen(
                room = room,
                messages = messages,
                onSendMessage = { text -> viewModel.sendTextMessage(roomId, text) },
                onSendVoice = { duration -> viewModel.sendVoiceMessage(roomId, duration) },
                onSendPicture = { url, caption -> viewModel.sendPictureMessage(roomId, url, caption) },
                onSendVideo = { url, duration -> viewModel.sendVideoMessage(roomId, url, duration) },
                onSendFile = { name, size -> viewModel.sendFileMessage(roomId, name, size) },
                onStartCall = { isVideo -> viewModel.startCall(roomId, isVideo) },
                onReactionAdded = { msgId, reaction -> viewModel.addReaction(msgId, reaction) },
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                userProfile = userProfile,
                wsStatus = wsStatus,
                wsStatusDetail = wsStatusDetail,
                onSaveProfile = { name, username, status, statusType, wsUrl ->
                    viewModel.updateProfile(name, username, status, statusType, wsUrl)
                },
                onConnectWebSocket = { url -> viewModel.connectWebSocket(url) },
                onDisconnectWebSocket = { viewModel.disconnectWebSocket() },
                onTestPing = { viewModel.sendTestPing() },
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
