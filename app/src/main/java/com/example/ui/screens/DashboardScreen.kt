package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.ui.viewmodel.RoomSafetyStatus
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoomEntity
import com.example.ui.components.ChemicalExposureMonitor
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyGreen
import com.example.ui.viewmodel.AppViewModel
import com.example.util.tr
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.ExperimentalComposeUiApi

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToRooms: () -> Unit,
    onNavigateToShifts: () -> Unit,
    onNavigateToWages: () -> Unit,
    onNavigateToLabors: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rooms by viewModel.rooms.collectAsState()
    val activeShift by viewModel.activeShift.collectAsState()
    val wages by viewModel.wages.collectAsState()
    val chemicalEntries by viewModel.chemicalEntries.collectAsState()

    val totalWageAccrued = wages.sumOf { it.netAmount } + (activeShift?.calculatedWage ?: 0.0)
    val hasWarningRoom = rooms.any { it.isWarning }
    val hasDangerRoom = rooms.any { it.isDanger }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // -1. LIVE EMERGENCY DISPATCH TELEMETRY & SIREN BANNER
        item {
            EmergencyTrackerBanner(viewModel = viewModel)
        }

        // -0.5 AI DAILY PYROTECHNIC SAFETY TIP
        item {
            DailySafetyTipCard(viewModel = viewModel)
        }

        // 0. ROOM SAFETY DETECTION FEATURE (AI Camera & Image Hazard Analysis)
        item {
            RoomSafetyDetectionCard(viewModel = viewModel)
        }

        // 1. REAL-TIME FACTORY SAFETY STATUS BANNER (Normal Compact Size)
        item {
            val statusColor = when {
                hasDangerRoom -> SafetyCrimson
                hasWarningRoom -> SafetyAmber
                else -> SafetyGreen
            }
            val statusTitle = when {
                hasDangerRoom -> tr("அபாய நிலை: அனுமதிக்கப்பட்ட வரம்பு தாண்டப்பட்டது!", "CRITICAL: PESO Room Limit Exceeded!")
                hasWarningRoom -> tr("கவன எச்சரிக்கை: அறை வரம்பு 80% கடந்துள்ளது", "CAUTION: Room Load Above 80%")
                else -> tr("தொழிற்சாலை நிலை: பாதுகாப்பானது", "Unit Safety: Fully Compliant")
            }
            val statusDesc = when {
                hasDangerRoom -> tr("உடனடியாக மேற்பார்வையாளரை தொடர்பு கொள்ளவும்", "Halt transfers into flagged rooms immediately")
                hasWarningRoom -> tr("அறை R-102 இல் கூடுதல் மருந்து சேர்க்க தடை", "Room R-102 nearing threshold, monitor closely")
                else -> tr("அனைத்து 5 அறைகளும் விதிகளுக்குள் செயல்படுகின்றன", "All 5 rooms within licensed PESO limits")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("safety_status_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = statusColor.copy(alpha = 0.10f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(statusColor, statusColor.copy(alpha = 0.4f))))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(statusColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasDangerRoom) Icons.Default.Dangerous else if (hasWarningRoom) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = statusColor,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = statusDesc,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // 2. REAL-TIME SHIFT STATUS CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_shift_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("இன்றைய பணி நிலை", "Shift Live Status"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (activeShift != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tr("செயலில் உள்ளது", "Active"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafetyGreen
                                )
                            }
                        } else {
                            Text(
                                text = tr("தொடங்கப்படவில்லை", "Not Clocked In"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeShift != null) {
                        val shift = activeShift!!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = tr("வேலை வகை", "Work Task"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when (shift.workCategory) {
                                        "Bijili Cracker Rolling" -> tr("பிஜிலி பட்டாசு சுற்றல்", "Bijili Cracker Rolling")
                                        "Flowerpot Chemical Filling" -> tr("பூந்தொட்டி நிரப்புதல்", "Flowerpot Chemical Filling")
                                        "Sparkler Chemical Dipping" -> tr("கம்பி மத்தாப்பு நனைத்தல்", "Sparkler Chemical Dipping")
                                        "Fuse Cutting & Twisting" -> tr("திரி வெட்டுதல் & முறுக்குதல்", "Fuse Cutting & Twisting")
                                        else -> shift.workCategory
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${tr("அறை: ", "Room: ")}${shift.activeRoom}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = tr("தயாரான எண்ணிக்கை", "Pieces Rolled"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${shift.piecesProduced}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = IndustrialTeal
                                    )
                                )
                                Text(
                                    text = "₹${"%.2f".format(shift.calculatedWage)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Piece Adder Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.addPiecesToShift(50) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 44.dp)
                                    .testTag("add_50_pieces_btn"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text("+50", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.addPiecesToShift(100) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 44.dp)
                                    .testTag("add_100_pieces_btn"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text("+100", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = onNavigateToShifts,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 44.dp)
                                    .testTag("manage_shift_btn"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = tr("ஷிப்ட் விவரம்", "Shift View"),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        // Clock In Call to Action
                        Text(
                            text = tr(
                                "இன்றைய காலை ஷிப்ட் இன்னும் தொடங்கப்படவில்லை. கைரேகை மூலம் தொடங்கவும்.",
                                "You haven't clocked into today's shift yet. Check-in with biometrics to start."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateToShifts,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .testTag("start_shift_cta_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("பணி தொடங்க (Clock In)", "Start Shift"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. KEY METRICS GRID
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Metric 1: Wages Earned
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToWages() }
                        .testTag("dashboard_wages_metric_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = SafetyGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr("மொத்த ஊதியம்", "Wages Total"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹${"%.0f".format(totalWageAccrued)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = tr("டிஜிட்டல் முத்திரை பூட்டப்பட்டது", "Digitally Sealed"),
                            fontSize = 10.sp,
                            color = SafetyGreen
                        )
                    }
                }

                // Metric 2: Monitored Chemical Rooms
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToRooms() }
                        .testTag("dashboard_rooms_metric_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = IndustrialTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr("அறைகள் கண்காணிப்பு", "Active Rooms"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${rooms.size} " + tr("அறைகள்", "Rooms"),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = tr("PESO உரிம வரம்பு", "PESO Limit Tracked"),
                            fontSize = 10.sp,
                            color = IndustrialTeal
                        )
                    }
                }
            }
        }

        // 3B. WORKER CHEMICAL EXPOSURE MONITOR COMPONENT
        item {
            ChemicalExposureMonitor(
                workerName = viewModel.workerName,
                onTakeBreak = {
                    viewModel.requireBiometricAuth(
                        title = tr("சுத்த காற்று இடைவேளை", "Fresh Air Detox Break"),
                        subtitle = tr("15 நிமிட நச்சு நீக்கும் ஓய்வு பதிவு", "15-minute scheduled recovery pause")
                    ) {}
                },
                onReportSpike = {
                    viewModel.requireBiometricAuth(
                        title = tr("தூசு / ஆவி அதிகரிப்பு எச்சரிக்கை", "Report Chemical Dust Spike"),
                        subtitle = tr("மேற்பார்வையாளர் கவனத்திற்கு அனுப்பப்படுகிறது", "Dispatching ventilation alert to supervisor")
                    ) {}
                }
            )
        }

        // 3C. LABORERS & WORKFORCE MANAGEMENT PREVIEW CARD
        item {
            val workers by viewModel.workers.collectAsState()
            val onDutyCount = workers.count { it.status == "ON_DUTY" }
            val onBreakCount = workers.count { it.status == "ON_BREAK" }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_labors_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(IndustrialNavy.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = IndustrialNavy,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tr("தொழிலாளர் மேலாண்மை", "Labors & Workforce Management"),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tr("PESO அறை வரம்பு & வருகை", "PESO Shed Capacity & Roster"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SafetyGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = tr("பாதுகாப்பானது", "Compliant"),
                                color = SafetyGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: On Duty
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$onDutyCount",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SafetyGreen
                                )
                                Text(
                                    text = tr("பணியில்", "On Duty"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        // Stat 2: On Break
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$onBreakCount",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SafetyAmber
                                )
                                Text(
                                    text = tr("ஓய்வில்", "On Break"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        // Stat 3: Total Registered
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${workers.size}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = IndustrialNavy
                                )
                                Text(
                                    text = tr("மொத்தம்", "Total"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Manage Labors Button (word-based sizing)
                    Button(
                        onClick = onNavigateToLabors,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag("manage_labors_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialNavy),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("தொழிலாளர் பட்டியல் & அறை ஒதுக்கீடு >", "Manage Laborers & Shed Roster >"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 4. ROOM-WISE CHEMICAL LIMIT PROGRESS SUMMARY
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("அறை மருந்து அளவு நிலவரம்", "Live Chemical Room Limits"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = tr("அனைத்தும் பார்க்க >", "View All >"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToRooms() }
                )
            }
        }

        items(rooms.take(3)) { room ->
            RoomMeterCard(room = room, onClick = onNavigateToRooms)
        }

        // 5. SHOCK DETECTION & BLAST SENSOR SIMULATION TEST
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shock_detection_test_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("வெடிப்பு / அதிர்வு சோதனை சென்சார்", "Shock / Blast Detection AI Test"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr(
                            "கை தவறி விழுவதையும், உண்மையான வெடி அதிர்வையும் வேறுபடுத்தும் ஆன்-டிவைஸ் சோதனை.",
                            "On-device classifier that differentiates normal drops from genuine high-g impacts."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.triggerSosCountdown(isShockOrBlast = true) },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag("simulate_shock_blast_btn"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("அதிர்வு / வெடிப்பு கண்டறிதலை சோதிக்க", "Simulate Impact / Blast Alarm"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoomMeterCard(
    room: RoomEntity,
    onClick: () -> Unit
) {
    val barColor = when {
        room.isDanger -> SafetyCrimson
        room.isWarning -> SafetyAmber
        else -> SafetyGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("room_meter_card_${room.roomNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = room.roomNumber,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr(room.nameTa, room.nameEn),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "${tr("அறை எண்: ", "Room: ")}${room.roomNumber} • ${tr("வரம்பு: ", "PESO Limit: ")}${"%.0f".format(room.maxCapacityKg)} kg",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${"%.1f".format(room.currentTotalKg)} / ${"%.0f".format(room.maxCapacityKg)} kg",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = barColor
                    )
                    Text(
                        text = "${room.fillPercentage.toInt()}% " + tr("நிறைவு", "Filled"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = barColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (room.fillPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
fun RoomSafetyDetectionCard(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val roomBitmap by viewModel.roomSafetyBitmap.collectAsState()
    val roomNumberInput by viewModel.roomNumberInput.collectAsState()
    val roomSafetyResult by viewModel.roomSafetyResult.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingRoom.collectAsState()
    val scope = rememberCoroutineScope()

    val onImageSelected: (Uri, android.view.DragAndDropPermissions?) -> Unit = remember(context, viewModel, scope) {
        { uri, permissions ->
            viewModel.setRoomSafetyBitmap(null)
            scope.launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        decodeRoomSafetyImage(context, uri)
                    }
                    viewModel.setRoomSafetyBitmap(bitmap)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("RoomSafetyDetection", "Unable to load selected room image", e)
                    viewModel.showUserMessage(
                        tr(
                            "படத்தைத் திறக்க முடியவில்லை. JPG அல்லது PNG படத்தைத் தேர்ந்தெடுக்கவும்.",
                            "Couldn't open that image. Select a JPG or PNG file.",
                            hi = "छवि नहीं खुली। JPG या PNG फ़ाइल चुनें।",
                            ml = "ചിത്രം തുറക്കാനായില്ല. JPG അല്ലെങ്കിൽ PNG ഫയൽ തിരഞ്ഞെടുക്കുക.",
                            te = "చిత్రాన్ని తెరవలేకపోయాం. JPG లేదా PNG ఫైల్‌ను ఎంచుకోండి.",
                            kn = "ಚಿತ್ರ ತೆರೆಯಲಾಗಲಿಲ್ಲ. JPG ಅಥವಾ PNG ಫೈಲ್ ಆಯ್ಕೆಮಾಡಿ.",
                            bn = "ছবি খোলা যায়নি। JPG বা PNG ফাইল বেছে নিন।",
                            mr = "प्रतिमा उघडता आली नाही. JPG किंवा PNG फाइल निवडा."
                        )
                    )
                } finally {
                    permissions?.release()
                }
            }
        }
    }
    val dropTarget = remember(onImageSelected) {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val nativeEvent = event.toAndroidDragEvent()
                val clipData = nativeEvent.clipData ?: return false
                if (clipData.itemCount == 0) return false
                val uri = clipData.getItemAt(0).uri ?: return false
                val permissions = (context as? Activity)?.requestDragAndDropPermissions(nativeEvent)
                onImageSelected(uri, permissions)
                return true
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it, null) }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("room_safety_detection_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("அறை பாதுகாப்பு ஆய்வு", "Room Safety Detection"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tr(
                                "அறை படத்தைப் பதிவேற்றி ஆபத்துகளைச் சரிபார்க்கவும்",
                                "Upload a room image to check for hazards",
                                hi = "खतरों की जांच के लिए कमरे की छवि अपलोड करें",
                                ml = "അപകടങ്ങൾ പരിശോധിക്കാൻ മുറിയുടെ ചിത്രം അപ്‌ലോഡ് ചെയ്യുക",
                                te = "ప్రమాదాలను తనిఖీ చేయడానికి గది చిత్రాన్ని అప్‌लोड చేయండి",
                                kn = "ಅಪಾಯಗಳನ್ನು ಪರಿಶೀಲಿಸಲು ಕೊಠಡಿಯ ಚಿತ್ರವನ್ನು ಅಪ್‌ಲೋಡ್ ಮಾಡಿ",
                                bn = "বিপদ পরীক্ষা করতে কক্ষের ছবি আপলোড করুন",
                                mr = "धोक्यांची तपासणी करण्यासाठी खोलीची प्रतिमा अपलोड करा"
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = IndustrialTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tr("காட்சி ஆய்வு", "VISUAL CHECK"),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = IndustrialTeal,
                        maxLines = 1
                    )
                }
            }

            // Room Number Dropdown (Rooms 101 - 105)
            var expanded by remember { mutableStateOf(false) }
            val roomsList = listOf("Room 101", "Room 102", "Room 103", "Room 104", "Room 105")

            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = roomNumberInput,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(tr("அறை எண்", "Room Number"), fontSize = 12.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    roomsList.forEach { room ->
                        DropdownMenuItem(
                            text = { Text(room) },
                            onClick = {
                                viewModel.setRoomNumberInput(room)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Image Selection / Drag-Drop Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { event ->
                            event.toAndroidDragEvent().clipDescription?.hasMimeType("image/*") == true
                        },
                        target = dropTarget
                    )
                    .testTag("room_image_picker"),
                contentAlignment = Alignment.Center
            ) {
                if (roomBitmap != null) {
                    Image(
                        bitmap = roomBitmap!!.asImageBitmap(),
                        contentDescription = tr("பதிவேற்றிய அறை முன்னோட்டம்", "Uploaded room image preview"),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = tr(
                                "படத்தை மாற்ற தட்டவும்",
                                "Tap to change image",
                                hi = "बदलने के लिए छवि पर टैप करें",
                                ml = "മാറ്റാൻ ചിത്രത്തിൽ ടാപ്പ് ചെയ്യുക",
                                te = "మార్చడానికి చిత్రాన్ని నొక్కండి",
                                kn = "ಬದಲಾಯಿಸಲು ಚಿತ್ರವನ್ನು ಟ್ಯಾಪ್ ಮಾಡಿ",
                                bn = "পরিবর্তন করতে ছবিতে ট্যাপ করুন",
                                mr = "बदलण्यासाठी प्रतिमेवर टॅप करा"
                            ),
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = tr(
                                "படத்தை இங்கே இழுத்து விடவும் அல்லது கீழே தேர்ந்தெடுக்கவும்",
                                "Drag an image here or choose one below",
                                hi = "छवि यहां खींचें या नीचे चुनें",
                                ml = "ചിത്രം ഇവിടെ വലിച്ചിടുക അല്ലെങ്കിൽ താഴെ തിരഞ്ഞെടുക്കുക",
                                te = "చిత్రాన్ని ఇక్కడికి లాగండి లేదా కింద ఎంచుకోండి",
                                kn = "ಚಿತ್ರವನ್ನು ಇಲ್ಲಿ ಎಳೆಯಿರಿ ಅಥವಾ ಕೆಳಗೆ ಆಯ್ಕೆಮಾಡಿ",
                                bn = "এখানে ছবি টেনে আনুন বা নিচে বেছে নিন",
                                mr = "प्रतिमा येथे ड्रॅग करा किंवा खाली निवडा"
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tr(
                                "PNG அல்லது JPG தொழிற்சாலை அறை படங்கள்",
                                "PNG or JPG factory room images",
                                hi = "PNG या JPG फैक्टरी कक्ष की छवियां",
                                ml = "PNG അല്ലെങ്കിൽ JPG ഫാക്ടറി മുറി ചിത്രങ്ങൾ",
                                te = "PNG లేదా JPG ఫ్యాక్టరీ గది చిత్రాలు",
                                kn = "PNG ಅಥವಾ JPG ಕಾರ್ಖಾನೆ ಕೊಠಡಿ ಚಿತ್ರಗಳು",
                                bn = "PNG বা JPG কারখানার কক্ষের ছবি",
                                mr = "PNG किंवा JPG कारखाना खोलीच्या प्रतिमा"
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(tr("படத்தைத் தேர்ந்தெடு", "Choose Image"), fontSize = 13.sp)
                }
            }


            // Action Buttons: Analyze & Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.analyzeRoomSafety(roomBitmap, roomNumberInput) },
                    modifier = Modifier.weight(1.5f),
                    enabled = !isAnalyzing && roomBitmap != null && roomNumberInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("ஆய்வு செய்யப்படுகிறது...", "Analyzing..."), fontSize = 12.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("அறை பாதுகாப்பை ஆய்வு செய்", "Analyze Room Safety"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.clearRoomSafetyDetection() },
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(tr("மீட்டமை", "Reset"), fontSize = 12.sp)
                }
            }

            // Detection Result Card
            roomSafetyResult?.let { result ->
                val isDanger = result.status == RoomSafetyStatus.DANGER
                val badgeColor = if (isDanger) SafetyCrimson else SafetyGreen
                val badgeText = if (isDanger) {
                    "🔴 " + tr("ஆபத்து கண்டறியப்பட்டது", "DANGER DETECTED")
                } else {
                    "🟢 " + tr("அறை பாதுகாப்பாக உள்ளது", "ROOM SAFE")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.12f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(badgeColor, badgeColor.copy(alpha = 0.5f))))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tr(
                                "அறை எண்: ${result.roomNumber}",
                                "Room Number: ${result.roomNumber}",
                                hi = "कमरा नंबर: ${result.roomNumber}",
                                ml = "മുറി നമ്പർ: ${result.roomNumber}",
                                te = "గది సంఖ్య: ${result.roomNumber}",
                                kn = "ಕೊಠಡಿ ಸಂಖ್ಯೆ: ${result.roomNumber}",
                                bn = "কক্ষ নম্বর: ${result.roomNumber}",
                                mr = "खोली क्रमांक: ${result.roomNumber}"
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = badgeColor
                        ) {
                            Text(
                                text = badgeText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Text(
                            text = result.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isDanger) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = SafetyCrimson
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Dangerous, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = tr(
                                                "ஆபத்து கண்டறியப்பட்டது – ${result.roomNumber} அறைக்கு SOS செயல்படுத்தப்பட்டது",
                                                "DANGER DETECTED – SOS ACTIVATED FOR ${result.roomNumber}",
                                                hi = "खतरे का पता चला – ${result.roomNumber} के लिए SOS सक्रिय",
                                                ml = "അപകടം കണ്ടെത്തി – ${result.roomNumber}-നായി SOS സജീവമാക്കി",
                                                te = "ప్రమాదం గుర్తించబడింది – ${result.roomNumber} కోసం SOS సక్రియం చేయబడింది",
                                                kn = "ಅಪಾಯ ಪತ್ತೆಯಾಗಿದೆ – ${result.roomNumber} ಗಾಗಿ SOS ಸಕ್ರಿಯಗೊಳಿಸಲಾಗಿದೆ",
                                                bn = "বিপদ শনাক্ত – ${result.roomNumber}-এর জন্য SOS সক্রিয়",
                                                mr = "धोका आढळला – ${result.roomNumber} साठी SOS सक्रिय"
                                            ),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Text(
                                        text = tr(
                                            "SOS நிலை: செயலில் உள்ளது",
                                            "SOS Status: ACTIVE",
                                            hi = "SOS स्थिति: सक्रिय",
                                            ml = "SOS നില: സജീവം",
                                            te = "SOS స్థితి: സక్రియం",
                                            kn = "SOS ಸ್ಥಿತಿ: ಸಕ್ರಿಯ",
                                            bn = "SOS অবস্থা: সক্রিয়",
                                            mr = "SOS स्थिती: सक्रिय"
                                        ),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailySafetyTipCard(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_safety_tip_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(IndustrialTeal, SafetyAmber)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = IndustrialTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("AI தினசரி பாதுகாப்பு குறிப்பு", "AI Daily Pyrotechnic Safety Tip"),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SafetyAmber.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "PESO GUIDELINE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = SafetyAmber
                    )
                }
            }

            Text(
                text = viewModel.dailySafetyTip,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmergencyTrackerBanner(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val isEmergencyActive by viewModel.isEmergencyTrackerActive.collectAsState()

    if (!isEmergencyActive) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_tracker_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SafetyCrimson.copy(alpha = 0.15f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SafetyCrimson, SafetyAmber)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SafetyCrimson),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dangerous,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tr("நேரலை அவசர தீயணைப்பு & மீட்பு ரேடார்", "Live Fire Station Dispatch Tracker"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = SafetyCrimson
                        )
                        Text(
                            text = tr("Sivakasi Fire Unit 2 En Route • ETA: 3 mins", "Response Vehicle Dispatched • Distance: 1.8 km"),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Instant "Noticed" Acknowledgment Button
                Button(
                    onClick = { viewModel.dismissEmergencyTracker() },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = tr("கவனித்தேன் (Noticed)", "Noticed"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Dispatch Telemetry Grid
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = tr("அவசர உதவி எண்", "Hotline"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "📞 101 / 220101", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndustrialTeal)
                    }
                }
            }
        }
    }
}

private fun decodeRoomSafetyImage(context: android.content.Context, uri: Uri): Bitmap {
    val decoded = if (Build.VERSION.SDK_INT < 28) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: throw IllegalArgumentException("The selected image could not be opened.")
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "The selected file is not a valid image." }
        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > 1024) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IllegalArgumentException("The selected image could not be decoded.")
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val scale = minOf(1f, 1024f / maxOf(info.size.width, info.size.height))
            decoder.setTargetSize(
                (info.size.width * scale).toInt().coerceAtLeast(1),
                (info.size.height * scale).toInt().coerceAtLeast(1)
            )
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
        }
    }
    val scale = minOf(1f, 1024f / maxOf(decoded.width, decoded.height))
    if (scale >= 1f) return decoded
    return Bitmap.createScaledBitmap(
        decoded,
        (decoded.width * scale).toInt().coerceAtLeast(1),
        (decoded.height * scale).toInt().coerceAtLeast(1),
        true
    ).also { decoded.recycle() }
}
