package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChatRoom
import com.example.data.UserProfile
import com.example.data.websocket.WsConnectionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chatRooms: List<ChatRoom>,
    userProfile: UserProfile,
    wsStatus: WsConnectionStatus,
    onChatClicked: (Int) -> Unit,
    onProfileClicked: () -> Unit,
    onAddUser: (name: String, username: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedBottomNav by remember { mutableStateOf(0) }

    val filteredRooms = remember(chatRooms, searchQuery) {
        if (searchQuery.isBlank()) chatRooms
        else {
            val query = searchQuery.trim().lowercase()
            chatRooms.filter {
                it.name.lowercase().contains(query) ||
                it.username.lowercase().contains(query) ||
                it.lastMessage.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1C1B1F))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top App Bar row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onProfileClicked() }
                    ) {
                        // Personal Avatar with initials
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD0BCFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.name.take(2).uppercase().ifEmpty { "ME" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF381E72)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Whisper",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE6E1E5)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Padlock
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (userProfile.username.startsWith("@")) userProfile.username else "@${userProfile.username}",
                                fontSize = 12.sp,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }

                    // WebSocket indicator & Profile button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (wsColor, wsIcon) = when (wsStatus) {
                            WsConnectionStatus.CONNECTED -> Color(0xFF81C784) to Icons.Default.Wifi
                            WsConnectionStatus.CONNECTING -> Color(0xFFFFD54F) to Icons.Default.Wifi
                            else -> Color(0xFFE57373) to Icons.Default.WifiOff
                        }

                        // Live WebSocket status pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2B2930))
                                .border(1.dp, wsColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { onProfileClicked() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(wsColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (wsStatus == WsConnectionStatus.CONNECTED) "Live" else wsStatus.name.take(4),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = wsColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onProfileClicked,
                            modifier = Modifier.testTag("nav_profile_button")
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Personal Account",
                                tint = Color(0xFFCAC4D0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_chats_input"),
                    placeholder = { Text("Search by name, @username, or message...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFCAC4D0)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFFCAC4D0))
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2B2930),
                        unfocusedContainerColor = Color(0xFF2B2930),
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFFE6E1E5),
                        unfocusedTextColor = Color(0xFFE6E1E5),
                        cursorColor = Color(0xFFD0BCFF)
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_user_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add User")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1C1B1F),
                contentColor = Color(0xFFCAC4D0),
                modifier = Modifier.height(68.dp)
            ) {
                NavigationBarItem(
                    selected = selectedBottomNav == 0,
                    onClick = { selectedBottomNav = 0 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                    label = { Text("Chats", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = Color(0xFFE6E1E5),
                        indicatorColor = Color(0xFFE8DEF8),
                        unselectedIconColor = Color(0xFFCAC4D0),
                        unselectedTextColor = Color(0xFFCAC4D0)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 1,
                    onClick = { selectedBottomNav = 1 },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                    label = { Text("Calls", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = Color(0xFFE6E1E5),
                        indicatorColor = Color(0xFFE8DEF8),
                        unselectedIconColor = Color(0xFFCAC4D0),
                        unselectedTextColor = Color(0xFFCAC4D0)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 2,
                    onClick = { onProfileClicked() },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Account") },
                    label = { Text("Account", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = Color(0xFFE6E1E5),
                        indicatorColor = Color(0xFFE8DEF8),
                        unselectedIconColor = Color(0xFFCAC4D0),
                        unselectedTextColor = Color(0xFFCAC4D0)
                    )
                )
            }
        },
        containerColor = Color(0xFF1C1B1F),
        modifier = modifier
    ) { paddingValues ->
        if (filteredRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color(0xFF4A4458),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No users matching \"$searchQuery\"" else "No conversations yet",
                        color = Color(0xFFCAC4D0),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showAddUserDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        )
                    ) {
                        Text("Add User by Name & Username")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(filteredRooms, key = { it.id }) { room ->
                    ChatRoomItem(
                        room = room,
                        onClick = { onChatClicked(room.id) }
                    )
                }
            }
        }

        // Add Other Real-User Dialog
        if (showAddUserDialog) {
            AddRealUserDialog(
                onDismiss = { showAddUserDialog = false },
                onAddUser = { name, username ->
                    showAddUserDialog = false
                    onAddUser(name, username)
                }
            )
        }
    }
}

@Composable
fun AddRealUserDialog(
    onDismiss: () -> Unit,
    onAddUser: (String, String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2B2930),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Add Real User", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Add another real user via their Name and Username to start real-time messaging, voice & video calls.",
                    fontSize = 13.sp,
                    color = Color(0xFFCAC4D0)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        errorMessage = null
                    },
                    label = { Text("Contact Full Name") },
                    placeholder = { Text("e.g. Elena Rostova") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_user_name_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color(0xFF49454F),
                        focusedLabelColor = Color(0xFFD0BCFF),
                        unfocusedLabelColor = Color(0xFFCAC4D0),
                        focusedTextColor = Color(0xFFE6E1E5),
                        unfocusedTextColor = Color(0xFFE6E1E5)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
                        usernameInput = it
                        errorMessage = null
                    },
                    label = { Text("Unique Username") },
                    placeholder = { Text("e.g. elena_r") },
                    prefix = { Text("@", color = Color(0xFFD0BCFF)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_user_username_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color(0xFF49454F),
                        focusedLabelColor = Color(0xFFD0BCFF),
                        unfocusedLabelColor = Color(0xFFCAC4D0),
                        focusedTextColor = Color(0xFFE6E1E5),
                        unfocusedTextColor = Color(0xFFE6E1E5)
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFF2B8B5),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Quick Suggestion:", fontSize = 11.sp, color = Color(0xFF938F99))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Alice" to "alice_w", "David" to "david_k", "Sophia" to "sophia_tech").forEach { (n, u) ->
                        OutlinedButton(
                            onClick = {
                                nameInput = n
                                usernameInput = u
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0BCFF))
                        ) {
                            Text("@$u", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isBlank() || usernameInput.isBlank()) {
                        errorMessage = "Please enter both name and username"
                    } else {
                        onAddUser(nameInput, usernameInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                ),
                modifier = Modifier.testTag("confirm_add_user_button")
            ) {
                Text("Start Chat", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFCAC4D0))
            }
        }
    )
}

@Composable
fun ChatRoomItem(room: ChatRoom, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_item_${room.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with Presence indicator & E2EE badge
        val avatarShape = if (room.isGroup) RoundedCornerShape(16.dp) else CircleShape
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(avatarShape)
                    .background(Color(0xFF4A4458)),
                contentAlignment = Alignment.Center
            ) {
                val imageRes = when (room.imageUrl) {
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
                        text = room.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD0BCFF)
                    )
                }
            }

            // Online Presence indicator (bottom right)
            val presenceColor = when (room.presenceStatus) {
                "online" -> Color(0xFF81C784)
                "away" -> Color(0xFFFFD54F)
                "busy" -> Color(0xFFE57373)
                else -> Color(0xFF9E9E9E)
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .background(presenceColor, CircleShape)
                    .border(2.dp, Color(0xFF1C1B1F), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE6E1E5),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (room.username.isNotBlank()) {
                        Text(
                            text = if (room.username.startsWith("@")) room.username else "@${room.username}",
                            fontSize = 12.sp,
                            color = Color(0xFFD0BCFF),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "12:45 PM",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD0BCFF)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.lastMessage.ifEmpty { "No messages yet" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCAC4D0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFD0BCFF))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF381E72)
                    )
                }
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 82.dp, end = 16.dp),
        color = Color(0xFF49454F).copy(alpha = 0.35f)
    )
}
