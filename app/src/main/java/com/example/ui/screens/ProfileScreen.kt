package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.data.websocket.WsConnectionStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    wsStatus: WsConnectionStatus,
    wsStatusDetail: String,
    onSaveProfile: (name: String, username: String, status: String, statusType: String, wsUrl: String) -> Unit,
    onConnectWebSocket: (String) -> Unit,
    onDisconnectWebSocket: () -> Unit,
    onTestPing: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nameState by remember(userProfile.name) { mutableStateOf(userProfile.name) }
    var usernameState by remember(userProfile.username) { mutableStateOf(userProfile.username) }
    var statusState by remember(userProfile.status) { mutableStateOf(userProfile.status) }
    var statusTypeState by remember(userProfile.statusType) { mutableStateOf(userProfile.statusType) }
    var wsUrlState by remember(userProfile.wsServerUrl) { mutableStateOf(userProfile.wsServerUrl) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Personal Account",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE6E1E5)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFCAC4D0)
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Profile Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD0BCFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nameState.take(2).uppercase().ifEmpty { "ME" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF381E72)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = nameState.ifEmpty { "My Account" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1E5)
                        )
                        Text(
                            text = if (usernameState.startsWith("@")) usernameState else "@$usernameState",
                            fontSize = 14.sp,
                            color = Color(0xFFD0BCFF),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusState,
                            fontSize = 12.sp,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Account Information
            Text(
                text = "ACCOUNT DETAILS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFFD0BCFF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = nameState,
                        onValueChange = { nameState = it },
                        label = { Text("Display Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedLabelColor = Color(0xFFD0BCFF),
                            unfocusedLabelColor = Color(0xFFCAC4D0),
                            focusedTextColor = Color(0xFFE6E1E5),
                            unfocusedTextColor = Color(0xFFE6E1E5)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = usernameState,
                        onValueChange = { usernameState = it },
                        label = { Text("Unique Username") },
                        prefix = { Text("@", color = Color(0xFFD0BCFF)) },
                        supportingText = { Text("Other real users can add you with this handle", color = Color(0xFF938F99)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_username_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedLabelColor = Color(0xFFD0BCFF),
                            unfocusedLabelColor = Color(0xFFCAC4D0),
                            focusedTextColor = Color(0xFFE6E1E5),
                            unfocusedTextColor = Color(0xFFE6E1E5)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = statusState,
                        onValueChange = { statusState = it },
                        label = { Text("Status Message") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedLabelColor = Color(0xFFD0BCFF),
                            unfocusedLabelColor = Color(0xFFCAC4D0),
                            focusedTextColor = Color(0xFFE6E1E5),
                            unfocusedTextColor = Color(0xFFE6E1E5)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Presence Status", fontSize = 13.sp, color = Color(0xFFCAC4D0))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("online", "away", "busy", "offline").forEach { type ->
                            FilterChip(
                                selected = statusTypeState == type,
                                onClick = { statusTypeState = type },
                                label = { Text(type.replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD0BCFF),
                                    selectedLabelColor = Color(0xFF381E72),
                                    containerColor = Color(0xFF1C1B1F),
                                    labelColor = Color(0xFFCAC4D0)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: WebSocket Connection
            Text(
                text = "WEBSOCKET CONNECTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFFD0BCFF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Status Badge Banner
                    val statusBg = when (wsStatus) {
                        WsConnectionStatus.CONNECTED -> Color(0xFF1E3A2F)
                        WsConnectionStatus.CONNECTING -> Color(0xFF3E3618)
                        WsConnectionStatus.ERROR -> Color(0xFF3B1E1E)
                        WsConnectionStatus.DISCONNECTED -> Color(0xFF211F26)
                    }
                    val statusColor = when (wsStatus) {
                        WsConnectionStatus.CONNECTED -> Color(0xFF81C784)
                        WsConnectionStatus.CONNECTING -> Color(0xFFFFD54F)
                        WsConnectionStatus.ERROR -> Color(0xFFE57373)
                        WsConnectionStatus.DISCONNECTED -> Color(0xFFB0BEC5)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBg)
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (wsStatus) {
                                WsConnectionStatus.CONNECTED -> Icons.Default.Wifi
                                WsConnectionStatus.CONNECTING -> Icons.Default.Refresh
                                else -> Icons.Default.WifiOff
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WebSocket: ${wsStatus.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = statusColor
                            )
                            Text(
                                text = wsStatusDetail,
                                fontSize = 11.sp,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = wsUrlState,
                        onValueChange = { wsUrlState = it },
                        label = { Text("Server URL (ws:// or wss://)") },
                        supportingText = { Text("Supports wss://echo.websocket.events or custom socket server", color = Color(0xFF938F99)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedLabelColor = Color(0xFFD0BCFF),
                            unfocusedLabelColor = Color(0xFFCAC4D0),
                            focusedTextColor = Color(0xFFE6E1E5),
                            unfocusedTextColor = Color(0xFFE6E1E5)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Presets:", fontSize = 11.sp, color = Color(0xFF938F99))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { wsUrlState = "local://relay" },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0BCFF))
                        ) {
                            Text("Local Relay (Dev)", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { wsUrlState = "wss://ws.postman-echo.com/raw" },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCAC4D0))
                        ) {
                            Text("Postman Echo", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (wsStatus == WsConnectionStatus.CONNECTED) {
                            OutlinedButton(
                                onClick = onDisconnectWebSocket,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF2B8B5))
                            ) {
                                Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Disconnect")
                            }

                            Button(
                                onClick = {
                                    onTestPing()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Ping message transmitted over WebSocket!")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4A4458),
                                    contentColor = Color(0xFFE6E1E5)
                                )
                            ) {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Ping")
                            }
                        } else {
                            Button(
                                onClick = {
                                    onConnectWebSocket(wsUrlState)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Connecting WebSocket...")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD0BCFF),
                                    contentColor = Color(0xFF381E72)
                                )
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connect WebSocket", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Account Profile
            Button(
                onClick = {
                    onSaveProfile(nameState, usernameState, statusState, statusTypeState, wsUrlState)
                    scope.launch {
                        snackbarHostState.showSnackbar("Personal Account updated & synced!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
