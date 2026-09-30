package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.ChatMessage
import com.example.data.ChatRoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    room: ChatRoom?,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onSendVoice: (duration: String) -> Unit,
    onSendPicture: (url: String, caption: String) -> Unit,
    onSendVideo: (url: String, duration: String) -> Unit,
    onSendFile: (fileName: String, fileSize: String) -> Unit,
    onStartCall: (isVideo: Boolean) -> Unit,
    onReactionAdded: (Int, String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showVoiceRecordDialog by remember { mutableStateOf(false) }
    var showPictureDialog by remember { mutableStateOf(false) }
    var showVideoDialog by remember { mutableStateOf(false) }
    var showFileDialog by remember { mutableStateOf(false) }
    var selectedPreviewImage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar
                        Box(modifier = Modifier.size(40.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4A4458)),
                                contentAlignment = Alignment.Center
                            ) {
                                val imageRes = when (room?.imageUrl) {
                                    "avatar_alice" -> R.drawable.avatar_alice_1781835473536
                                    "avatar_bob" -> R.drawable.avatar_bob_1781835486355
                                    "group_icon" -> R.drawable.group_icon_1781835497981
                                    else -> null
                                }
                                if (imageRes != null) {
                                    Image(
                                        painter = painterResource(id = imageRes),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = room?.name?.take(1)?.uppercase() ?: "U",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD0BCFF)
                                    )
                                }
                            }
                            // Presence dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(Color(0xFF81C784), CircleShape)
                                    .border(1.5.dp, Color(0xFF1C1B1F), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = room?.name ?: "Chat",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (room?.username?.isNotBlank() == true) {
                                    Text(
                                        text = if (room.username.startsWith("@")) room.username else "@${room.username}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFD0BCFF)
                                    )
                                    Text(
                                        text = " · ",
                                        fontSize = 12.sp,
                                        color = Color(0xFFCAC4D0)
                                    )
                                }
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFCAC4D0),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "E2EE",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCAC4D0)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFCAC4D0)
                        )
                    }
                },
                actions = {
                    // Voice Call Button
                    IconButton(
                        onClick = { onStartCall(false) },
                        modifier = Modifier.testTag("start_voice_call_button")
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = Color(0xFFD0BCFF)
                        )
                    }
                    // Video Call Button
                    IconButton(
                        onClick = { onStartCall(true) },
                        modifier = Modifier.testTag("start_video_call_button")
                    ) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = Color(0xFFD0BCFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1B1F)
                )
            )
        },
        containerColor = Color(0xFF1C1B1F),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onReactionAdded = { reaction -> onReactionAdded(msg.id, reaction) },
                        onImageClicked = { url -> selectedPreviewImage = url }
                    )
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF2B2930))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Plus Attachment Button
                IconButton(
                    onClick = { showAttachmentSheet = true },
                    modifier = Modifier.testTag("attach_menu_button")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Attach Media",
                        tint = Color(0xFFD0BCFF)
                    )
                }

                // Text Input Field
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    placeholder = { Text("Message...", color = Color(0xFF938F99), fontSize = 14.sp) },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFFE6E1E5),
                        unfocusedTextColor = Color(0xFFE6E1E5),
                        cursorColor = Color(0xFFD0BCFF)
                    )
                )

                // Send or Quick Actions (Mic / Photo)
                if (textInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onSendMessage(textInput.trim())
                            textInput = ""
                        },
                        modifier = Modifier.testTag("send_message_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color(0xFFD0BCFF)
                        )
                    }
                } else {
                    // Quick Voice Note button
                    IconButton(
                        onClick = { showVoiceRecordDialog = true },
                        modifier = Modifier.testTag("quick_voice_note_button")
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Record Voice Note",
                            tint = Color(0xFFCAC4D0)
                        )
                    }
                    // Quick Camera/Picture button
                    IconButton(
                        onClick = { showPictureDialog = true },
                        modifier = Modifier.testTag("quick_picture_button")
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Send Photo",
                            tint = Color(0xFFCAC4D0)
                        )
                    }
                }
            }
        }

        // Attachments Bottom Sheet
        if (showAttachmentSheet) {
            AttachmentBottomSheet(
                onDismiss = { showAttachmentSheet = false },
                onSelectPicture = {
                    showAttachmentSheet = false
                    showPictureDialog = true
                },
                onSelectVideo = {
                    showAttachmentSheet = false
                    showVideoDialog = true
                },
                onSelectFile = {
                    showAttachmentSheet = false
                    showFileDialog = true
                },
                onSelectVoice = {
                    showAttachmentSheet = false
                    showVoiceRecordDialog = true
                }
            )
        }

        // Voice Recording Dialog
        if (showVoiceRecordDialog) {
            VoiceRecordingDialog(
                onDismiss = { showVoiceRecordDialog = false },
                onSend = { duration ->
                    showVoiceRecordDialog = false
                    onSendVoice(duration)
                }
            )
        }

        // Send Picture Dialog
        if (showPictureDialog) {
            SendPictureDialog(
                onDismiss = { showPictureDialog = false },
                onSend = { url, caption ->
                    showPictureDialog = false
                    onSendPicture(url, caption)
                }
            )
        }

        // Send Video Dialog
        if (showVideoDialog) {
            SendVideoDialog(
                onDismiss = { showVideoDialog = false },
                onSend = { url, duration ->
                    showVideoDialog = false
                    onSendVideo(url, duration)
                }
            )
        }

        // Send File Dialog
        if (showFileDialog) {
            SendFileDialog(
                onDismiss = { showFileDialog = false },
                onSend = { name, size ->
                    showFileDialog = false
                    onSendFile(name, size)
                }
            )
        }

        // Full Screen Image Preview Modal
        selectedPreviewImage?.let { imageUrl ->
            Dialog(onDismissRequest = { selectedPreviewImage = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1C1B1F))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Photo Preview", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { selectedPreviewImage = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFCAC4D0))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF7E5265), Color(0xFF381E72))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onReactionAdded: (String) -> Unit,
    onImageClicked: (String) -> Unit
) {
    val isMine = message.isMine
    var showEmojiPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        // Message Card
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMine) 18.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 18.dp
                    )
                )
                .background(if (isMine) Color(0xFF4A4458) else Color(0xFF2B2930))
                .border(
                    width = 1.dp,
                    color = if (isMine) Color(0xFFD0BCFF).copy(alpha = 0.2f) else Color(0xFF49454F).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMine) 18.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 18.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                when (message.type) {
                    "voice" -> {
                        VoiceMessageBubbleContent(duration = message.duration ?: "0:15")
                    }
                    "image" -> {
                        PictureBubbleContent(
                            caption = message.content,
                            onClick = { onImageClicked(message.mediaUrl ?: "") }
                        )
                    }
                    "video" -> {
                        VideoBubbleContent(duration = message.duration ?: "0:30")
                    }
                    "file" -> {
                        FileBubbleContent(
                            fileName = message.fileName ?: message.content,
                            fileSize = message.fileSize ?: "1.8 MB"
                        )
                    }
                    "call" -> {
                        CallBubbleContent(summary = message.content)
                    }
                    else -> {
                        Text(
                            text = message.content,
                            color = Color(0xFFE6E1E5),
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom row: Time + read check
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "12:45 PM",
                        fontSize = 10.sp,
                        color = Color(0xFFCAC4D0).copy(alpha = 0.7f)
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Delivered",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Reaction Badge or Action
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp).padding(top = 2.dp)
        ) {
            if (message.reaction != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2B2930))
                        .border(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = message.reaction, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            TextButton(
                onClick = { showEmojiPicker = !showEmojiPicker },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Text("+ React", fontSize = 11.sp, color = Color(0xFFD0BCFF))
            }
        }

        // Quick Emoji Bar
        if (showEmojiPicker) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF211F26))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("👍", "❤️", "🔥", "😂", "👏", "🎉").forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clickable {
                                onReactionAdded(emoji)
                                showEmojiPicker = false
                            }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceMessageBubbleContent(duration: String) {
    var isPlaying by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(220.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFD0BCFF))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color(0xFF381E72),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Waveform simulation
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(24.dp)
            ) {
                val heights = listOf(8, 16, 22, 12, 18, 24, 14, 20, 10, 16, 22, 14, 8)
                heights.forEachIndexed { i, h ->
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(h.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPlaying && i < 6) Color(0xFFD0BCFF) else Color(0xFFCAC4D0))
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Voice note", fontSize = 11.sp, color = Color(0xFFCAC4D0))
                Text(duration, fontSize = 11.sp, color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PictureBubbleContent(caption: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(230.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFD8E4).copy(alpha = 0.3f), Color(0xFFD0BCFF).copy(alpha = 0.4f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tap to view full photo", fontSize = 11.sp, color = Color(0xFFE6E1E5))
            }
        }

        if (caption.isNotBlank() && caption != "Photo") {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = caption,
                fontSize = 14.sp,
                color = Color(0xFFE6E1E5)
            )
        }
    }
}

@Composable
fun VideoBubbleContent(duration: String) {
    var isPlaying by remember { mutableStateOf(false) }

    Column(modifier = Modifier.width(230.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2B2930), Color(0xFF141218))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD0BCFF))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color(0xFF381E72),
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(duration, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Video message", fontSize = 12.sp, color = Color(0xFFCAC4D0))
    }
}

@Composable
fun FileBubbleContent(fileName: String, fileSize: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(220.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF381E72)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = Color(0xFFD0BCFF),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE6E1E5),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = fileSize,
                fontSize = 11.sp,
                color = Color(0xFFCAC4D0)
            )
        }

        Icon(
            Icons.Default.Download,
            contentDescription = "Download",
            tint = Color(0xFFD0BCFF),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CallBubbleContent(summary: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Call,
            contentDescription = null,
            tint = Color(0xFF81C784),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = summary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE6E1E5)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onSelectPicture: () -> Unit,
    onSelectVideo: () -> Unit,
    onSelectFile: () -> Unit,
    onSelectVoice: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2B2930),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Send Content",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE6E1E5)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOption(
                    title = "Picture",
                    icon = Icons.Default.Image,
                    bgColor = Color(0xFF7E5265),
                    onClick = onSelectPicture
                )
                AttachmentOption(
                    title = "Video",
                    icon = Icons.Default.Videocam,
                    bgColor = Color(0xFF381E72),
                    onClick = onSelectVideo
                )
                AttachmentOption(
                    title = "File",
                    icon = Icons.Default.Description,
                    bgColor = Color(0xFF2A4B6B),
                    onClick = onSelectFile
                )
                AttachmentOption(
                    title = "Voice Note",
                    icon = Icons.Default.Mic,
                    bgColor = Color(0xFF1E513F),
                    onClick = onSelectVoice
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AttachmentOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 12.sp, color = Color(0xFFCAC4D0))
    }
}

// Dialog: Voice Recording with live counter
@Composable
fun VoiceRecordingDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var seconds by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(true) }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            seconds += 1
        }
    }

    val formatted = "%02d:%02d".format(seconds / 60, seconds % 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2B2930),
        title = {
            Text("Voice Message Recording", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(if (isRecording) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(Color(0xFFB3261E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formatted,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                )

                Text(
                    text = if (isRecording) "Recording live voice note..." else "Recording paused",
                    fontSize = 13.sp,
                    color = Color(0xFFCAC4D0)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDuration = if (seconds == 0) "0:05" else formatted
                    onSend(finalDuration)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                )
            ) {
                Text("Send Voice Note", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFCAC4D0))
            }
        }
    )
}

// Dialog: Send Picture
@Composable
fun SendPictureDialog(
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf("UI Prototype Screenshot") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2B2930),
        title = { Text("Send Picture", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Choose photo to transmit:", fontSize = 13.sp, color = Color(0xFFCAC4D0))
                Spacer(modifier = Modifier.height(10.dp))

                listOf("UI Prototype Screenshot", "App Architecture Diagram", "Camera Snapshot").forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPreset = preset }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (selectedPreset == preset) Color(0xFFD0BCFF) else Color(0xFF49454F))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(preset, color = Color(0xFFE6E1E5), fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color(0xFF49454F),
                        focusedTextColor = Color(0xFFE6E1E5),
                        unfocusedTextColor = Color(0xFFE6E1E5)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend("preset://$selectedPreset", caption) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                )
            ) {
                Text("Send Photo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFCAC4D0)) }
        }
    )
}

// Dialog: Send Video
@Composable
fun SendVideoDialog(
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var selectedVideo by remember { mutableStateOf("Design Demo Video (0:45)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2B2930),
        title = { Text("Send Video Message", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Select video clip to send:", fontSize = 13.sp, color = Color(0xFFCAC4D0))
                Spacer(modifier = Modifier.height(10.dp))

                listOf("Design Demo Video (0:45)", "Code Walkthrough (1:15)", "Screen Recording (0:30)").forEach { clip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVideo = clip }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (selectedVideo == clip) Color(0xFFD0BCFF) else Color(0xFF49454F))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(clip, color = Color(0xFFE6E1E5), fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = if (selectedVideo.contains("1:15")) "1:15" else if (selectedVideo.contains("0:45")) "0:45" else "0:30"
                    onSend("video://$selectedVideo", duration)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                )
            ) {
                Text("Send Video", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFCAC4D0)) }
        }
    )
}

// Dialog: Send File
@Composable
fun SendFileDialog(
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var fileName by remember { mutableStateOf("project_specs_v3.pdf") }
    var fileSize by remember { mutableStateOf("3.2 MB") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2B2930),
        title = { Text("Send File / Document", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                listOf(
                    "project_specs_v3.pdf" to "3.2 MB",
                    "client_contract_signed.docx" to "1.1 MB",
                    "assets_archive_2026.zip" to "14.8 MB"
                ).forEach { (fName, fSize) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                fileName = fName
                                fileSize = fSize
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (fileName == fName) Color(0xFFD0BCFF) else Color(0xFF49454F))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(fName, color = Color(0xFFE6E1E5), fontSize = 14.sp)
                            Text(fSize, color = Color(0xFFCAC4D0), fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(fileName, fileSize) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                )
            ) {
                Text("Send File", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFCAC4D0)) }
        }
    )
}
