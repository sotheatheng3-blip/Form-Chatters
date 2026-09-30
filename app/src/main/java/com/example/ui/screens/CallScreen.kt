package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.ActiveCallState

@Composable
fun CallDialog(
    callState: ActiveCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    onAnswerCall: () -> Unit
) {
    Dialog(
        onDismissRequest = onEndCall,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        CallScreenContent(
            callState = callState,
            onMuteToggle = onMuteToggle,
            onSpeakerToggle = onSpeakerToggle,
            onVideoToggle = onVideoToggle,
            onSwitchCamera = onSwitchCamera,
            onEndCall = onEndCall,
            onAnswerCall = onAnswerCall
        )
    }
}

@Composable
fun CallScreenContent(
    callState: ActiveCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    onAnswerCall: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val durationText = "%02d:%02d".format(callState.durationSeconds / 60, callState.durationSeconds % 60)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141218)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (callState.isVideo && callState.isVideoEnabled) {
                // Video background simulation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF2B2930), Color(0xFF1C1B1F), Color(0xFF0F0E13))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A4458)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = callState.contactName.take(1).uppercase(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD0BCFF)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connected Remote Stream (HD)",
                            color = Color(0xFFCAC4D0),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Self Video Picture-in-Picture Preview (Top Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 48.dp, end = 20.dp)
                            .size(width = 110.dp, height = 160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF211F26))
                            .border(2.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Camera On",
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (callState.isFrontCamera) "Front" else "Rear",
                                fontSize = 11.sp,
                                color = Color(0xFFE6E1E5)
                            )
                        }
                    }
                }
            } else {
                // Audio Call Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1C1B1F), Color(0xFF141218))
                            )
                        )
                )
            }

            // Top Header Info
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 54.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Call Type Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF381E72))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (callState.isVideo) "VIDEO CALL · E2EE" else "VOICE CALL · E2EE",
                        color = Color(0xFFD0BCFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = callState.contactName,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1E5)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = callState.contactUsername,
                    fontSize = 15.sp,
                    color = Color(0xFFD0BCFF)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (callState.callStatusText == "Connected") durationText else callState.callStatusText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (callState.callStatusText == "Connected") Color(0xFFB6F2BA) else Color(0xFFCAC4D0)
                )
            }

            // Center Avatar (when audio call or video disabled)
            if (!callState.isVideo || !callState.isVideoEnabled) {
                Box(
                    modifier = Modifier.align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Ring
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFFD0BCFF).copy(alpha = 0.15f))
                    )
                    // Inner Circle
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4A4458)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = callState.contactName.take(1).uppercase(),
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                    }
                }
            }

            // Bottom Call Action Controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 44.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (callState.isIncoming && callState.callStatusText.startsWith("Incoming")) {
                    // Incoming Call: Answer or Decline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingActionButton(
                            onClick = onEndCall,
                            containerColor = Color(0xFFB3261E),
                            contentColor = Color.White,
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Decline", modifier = Modifier.size(32.dp))
                        }

                        FloatingActionButton(
                            onClick = onAnswerCall,
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Answer", modifier = Modifier.size(32.dp))
                        }
                    }
                } else {
                    // Active in-call controls row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF1C1B1F).copy(alpha = 0.9f))
                            .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute button
                        IconButton(
                            onClick = onMuteToggle,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (callState.isMuted) Color(0xFF49454F) else Color(0xFF2B2930),
                                contentColor = if (callState.isMuted) Color(0xFFF2B8B5) else Color(0xFFE6E1E5)
                            ),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (callState.isMuted) "Unmute" else "Mute"
                            )
                        }

                        // Speaker button
                        IconButton(
                            onClick = onSpeakerToggle,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (callState.isSpeakerOn) Color(0xFFD0BCFF) else Color(0xFF2B2930),
                                contentColor = if (callState.isSpeakerOn) Color(0xFF381E72) else Color(0xFFE6E1E5)
                            ),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Speaker"
                            )
                        }

                        // Video toggle (if video call)
                        if (callState.isVideo) {
                            IconButton(
                                onClick = onVideoToggle,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (!callState.isVideoEnabled) Color(0xFF49454F) else Color(0xFF2B2930),
                                    contentColor = if (!callState.isVideoEnabled) Color(0xFFF2B8B5) else Color(0xFFE6E1E5)
                                ),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    imageVector = if (callState.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = "Toggle Video"
                                )
                            }

                            // Switch Camera
                            IconButton(
                                onClick = onSwitchCamera,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color(0xFF2B2930),
                                    contentColor = Color(0xFFE6E1E5)
                                ),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cameraswitch,
                                    contentDescription = "Switch Camera"
                                )
                            }
                        }

                        // End Call Red Button
                        FloatingActionButton(
                            onClick = onEndCall,
                            containerColor = Color(0xFFB3261E),
                            contentColor = Color.White,
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
