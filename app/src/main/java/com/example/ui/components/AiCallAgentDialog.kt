package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyGreen
import com.example.util.AiCallAgentManager
import com.example.util.AiCallScript
import com.example.util.AiCallState
import com.example.util.getEmergencyContacts

@Composable
fun AiCallAgentDialog(
    onDismiss: () -> Unit,
    initialPhone: String = "9500847066"
) {
    val context = LocalContext.current
    val agentManager = remember { AiCallAgentManager(context) }

    DisposableEffect(Unit) {
        agentManager.setTargetPhoneNumber(initialPhone, "Admin")
        agentManager.selectScript(AiCallScript.SCRIPT_1_URGENT)
        onDispose {
            agentManager.release()
        }
    }

    val callState by agentManager.callState.collectAsState()
    val targetPhone by agentManager.targetPhoneNumber.collectAsState()
    val activeContactName by agentManager.activeContactName.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ai")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = {
            agentManager.stopCall()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SafetyCrimson.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "AI Voice Agent",
                                tint = SafetyCrimson,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "VediRaksha AI Call Agent",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Emergency Station Dispatch",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = {
                        agentManager.stopCall()
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Responder Contacts Selector Chips
                Text(
                    text = "SELECT EMERGENCY RESPONDERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All Responders Chip
                    val isAllSelected = targetPhone.contains(",")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agentManager.selectAllContacts() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAllSelected) SafetyCrimson.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = if (isAllSelected) BorderStroke(1.5.dp, SafetyCrimson) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = SafetyCrimson, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("📢 Broadcast to All 3 Responders", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SafetyCrimson)
                                Text("Admin, Vishwa & Gokul (Simultaneous Voice Alert)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Individual Contact Chips
                    getEmergencyContacts(context).forEach { contact ->
                        val isSelected = !isAllSelected && targetPhone.contains(contact.phone)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { agentManager.selectContact(contact) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) IndustrialTeal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, IndustrialTeal) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = IndustrialTeal, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("${contact.name} • ${contact.role}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("+91 ${contact.phone}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { agentManager.dialPhoneIntent(contact.phone) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dial", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Live Voice Call Visualizer & Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (callState == AiCallState.IN_CALL_SPEAKING) SafetyCrimson.copy(alpha = 0.1f)
                            else IndustrialTeal.copy(alpha = 0.08f)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .scale(if (callState == AiCallState.IN_CALL_SPEAKING) pulseScale else 1.0f)
                                .clip(CircleShape)
                                .background(
                                    when (callState) {
                                        AiCallState.IN_CALL_SPEAKING -> SafetyCrimson
                                        AiCallState.WAITING_FOR_INPUT -> SafetyAmber
                                        AiCallState.DIALING -> IndustrialTeal
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (callState) {
                                    AiCallState.IN_CALL_SPEAKING -> Icons.AutoMirrored.Filled.VolumeUp
                                    AiCallState.WAITING_FOR_INPUT -> Icons.Default.GraphicEq
                                    AiCallState.DIALING -> Icons.Default.PhoneInTalk
                                    else -> Icons.Default.Mic
                                },
                                contentDescription = "AI Call",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = when (callState) {
                                AiCallState.DIALING -> "Dialing $activeContactName..."
                                AiCallState.IN_CALL_SPEAKING -> "🤖 AI Calling: $activeContactName"
                                AiCallState.WAITING_FOR_INPUT -> "⌨️ Waiting for Keypress (1 or 9)..."
                                AiCallState.CALL_ENDED -> "Call Ended"
                                else -> "Target: $activeContactName"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when (callState) {
                                AiCallState.IN_CALL_SPEAKING -> SafetyCrimson
                                AiCallState.WAITING_FOR_INPUT -> SafetyAmber
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "\"Emergency Alert for VediRaksha! Fire hazard detected at monitoring station. Vishwa, Gokul & Admin: Please proceed to station immediately!\"",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            agentManager.startAiVoiceCall(AiCallScript.SCRIPT_1_URGENT)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyCrimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play TTS", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Start AI Call Alert", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    if (callState == AiCallState.IN_CALL_SPEAKING || callState == AiCallState.WAITING_FOR_INPUT) {
                        Button(
                            onClick = { agentManager.stopCall() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call")
                        }
                    }
                }
            }
        }
    }
}
