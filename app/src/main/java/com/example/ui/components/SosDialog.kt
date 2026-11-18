package com.example.ui.components

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.PhoneInTalk
import com.example.ui.theme.SafetyCrimson
import com.example.ui.viewmodel.SosUiState
import com.example.util.tr
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay

@Composable
fun SosModalDialog(
    sosState: SosUiState,
    audioVolume: Int,
    onCancelCountdown: () -> Unit,
    onResolveAlert: () -> Unit
) {
    if (!sosState.isCountingDown && !sosState.isAlertActive) return

    var showAiCallDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_scale"
    )

    val context = LocalContext.current
    LaunchedEffect(sosState.isAlertActive, audioVolume) {
        if (sosState.isAlertActive) {
            var player: MediaPlayer? = null
            var toneGenerator: ToneGenerator? = null
            try {
                val audioResource = context.resources.getIdentifier(
                    "siren_alert",
                    "raw",
                    context.packageName
                ).takeIf { it != 0 } ?: context.resources.getIdentifier(
                    "danger_sos",
                    "raw",
                    context.packageName
                )
                if (audioResource != 0) {
                    player = MediaPlayer.create(context, audioResource)
                    if (player != null) {
                        val volume = audioVolume.coerceIn(0, 100) / 100f
                        player.isLooping = true
                        player.setVolume(volume, volume)
                        player.start()
                    } else {
                        Log.e("EmergencyAudio", "Unable to open res/raw/danger_sos.mp3")
                    }
                }

                if (player == null) {
                    toneGenerator = ToneGenerator(
                        AudioManager.STREAM_ALARM,
                        audioVolume.coerceIn(40, 90)
                    )
                    while (true) {
                        toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 2000)
                        delay(2400)
                    }
                } else {
                    awaitCancellation()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("EmergencyAudio", "Emergency audio playback failed", e)
            } finally {
                player?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
                toneGenerator?.let {
                    it.stopTone()
                    it.release()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (sosState.isCountingDown) onCancelCountdown()
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("sos_modal_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (sosState.isCountingDown) {
                    // 10-Second Cancel Window
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(beaconScale),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { sosState.countdownRemaining / 10f },
                            modifier = Modifier.size(100.dp),
                            color = SafetyCrimson,
                            strokeWidth = 8.dp,
                            trackColor = Color(0xFFFFCDD2)
                        )
                        Text(
                            text = "${sosState.countdownRemaining}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = SafetyCrimson
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = tr("அவசர எச்சரிக்கை தொடங்குகிறது!", "Emergency SOS Triggering!"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SafetyCrimson
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = tr(
                            "தவறுதலாக அழுத்தப்பட்டால், கீழே உள்ள பொத்தானை அழுத்தி உடனடியாக ரத்து செய்யவும்.",
                            "If triggered by mistake, press the button below within 10 seconds to cancel."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onCancelCountdown,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF374151),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("cancel_sos_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("ரத்து செய் (Cancel)", "Cancel Alarm"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // ACTIVE BROADCAST ALERT
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SafetyCrimson),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert Active",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = tr("ஆபத்து சமிக்ஞை பரப்பப்படுகிறது!", "SOS Alert Broadcasted!"),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = SafetyCrimson
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Broadcast Details Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tr("இடம்: ${sosState.lastLocation}", "Location: ${sosState.lastLocation}"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Sensors, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tr("அருகிலுள்ள 6 தொழிலாளர் போன்களுக்கு பகிரப்பட்டது (P2P Mesh)", "Relayed to 6 nearby phones via Offline Mesh"),
                                    fontSize = 12.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CellTower, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tr("மேற்பார்வையாளர் & 108 ஆம்புலன்ஸுக்கு SMS அனுப்பப்பட்டது", "SMS Dispatched to Supervisor & 108 Ambulance"),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showAiCallDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafetyCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = "AI Call", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🤖 AI Call Agent (Vishwa, Gokul & Admin)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onResolveAlert,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("resolve_sos_btn")
                    ) {
                        Text(
                            text = tr("உதவி கிடைத்தது / ஆபத்து தீர்ந்தது", "Help Received / Resolve Alert"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        if (showAiCallDialog) {
            AiCallAgentDialog(
                onDismiss = { showAiCallDialog = false },
                initialPhone = "9500847066"
            )
        }
    }
}
