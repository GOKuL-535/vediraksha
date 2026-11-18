package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ShiftEntity
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyGreen
import com.example.ui.viewmodel.AppViewModel
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.tr
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TaskCategoryOption(
    val titleTa: String,
    val titleEn: String,
    val defaultPieceRate: Double,
    val defaultRoom: String
)

@Composable
fun ShiftManagementScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val activeShift by viewModel.activeShift.collectAsState()
    val shifts by viewModel.shifts.collectAsState()

    var showClockInDialog by remember { mutableStateOf(false) }

    // Live seconds timer for active shift
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeShift) {
        while (activeShift != null) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("shift_management_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Active Shift or Clock In Banner
        item {
            if (activeShift != null) {
                val shift = activeShift!!
                val elapsedMillis = (currentTime - shift.clockInTime).coerceAtLeast(0L)
                val hours = elapsedMillis / (1000 * 60 * 60)
                val minutes = (elapsedMillis / (1000 * 60)) % 60
                val seconds = (elapsedMillis / 1000) % 60

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_shift_running_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = SafetyGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tr("தற்போது செயலில் உள்ள ஷிப்ட்", "Automated Shift In Progress"),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${when(shift.shiftType) { "MORNING" -> tr("காலை", "Morning") "AFTERNOON" -> tr("பிற்பகல்", "Afternoon") else -> tr("இரவு", "Night") }} ${tr("ஷிப்ட்", "Shift")} • ${shift.activeRoom}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Digital Timer badge
                            Surface(
                                color = IndustrialNavy,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "%02d:%02d:%02d".format(hours, minutes, seconds),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Production summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = tr("பணி விவரம்", "Task Category"),
                                    fontSize = 11.sp,
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
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = tr("விகிதம்: ₹${shift.pieceRate} / எண்ணிக்கை", "Rate: ₹${shift.pieceRate} / unit"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = tr("செய்த எண்ணிக்கை", "Pieces Output"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${shift.piecesProduced}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = IndustrialTeal
                                    )
                                )
                                Text(
                                    text = tr("உத்தேச ஊதியம்: ₹${"%.2f".format(shift.calculatedWage)}", "Est. Wage: ₹${"%.2f".format(shift.calculatedWage)}"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Biometric verification seal badge
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = IndustrialTeal.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = IndustrialTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tr(
                                        "பயோமெட்ரிக் செக்-இன் சரிபார்க்கப்பட்டது: SHA-${shift.clockInBiometricHash.take(12)}",
                                        "Biometric Check-In Verified: SHA-${shift.clockInBiometricHash.take(12)}"
                                    ),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IndustrialTeal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Piece Adder Buttons
                        Text(
                            text = tr("எண்ணிக்கை சேர்க்கவும்:", "Fast Quantity Log:"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(25, 50, 100, 250).forEach { qty ->
                                OutlinedButton(
                                    onClick = { viewModel.addPiecesToShift(qty) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 44.dp)
                                        .testTag("add_${qty}_shift_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text("+$qty", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Clock Out & Lock Wage Button (word-based sizing)
                        Button(
                            onClick = { viewModel.clockOutShift() },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyCrimson),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .testTag("clock_out_shift_btn")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("ஷிப்ட் நிறைவு செய்க & ஊதியத்தை பூட்டுக", "Clock-Out & Lock Wage (Biometric)"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                // Not Clocked In Banner (Normal Size)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = tr("ஷிப்ட் தொடங்கப்படவில்லை", "No Shift Active"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = tr(
                                "உங்கள் ஷிப்டை பயோமெட்ரிக் அங்கீகாரத்துடன் தொடங்கவும். நேரமும் எண்ணிக்கையும் தானாக கண்காணிக்கப்படும்.",
                                "Clock into your shift with biometric verification. Pieces and shift hours are automatically tracked."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showClockInDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .testTag("open_clock_in_dialog_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("பயோமெட்ரிக் செக்-இன் (Check-In)", "Biometric Shift Check-In"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Automated Rest & Safety Notice
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        tint = IndustrialTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tr("கட்டாய பாதுகாப்பு இடைவேளை விதி", "Mandatory Chemical Rest Notice"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = tr(
                                "ஒவ்வொரு 3 மணி நேர பணிக்கும் பின் 15 நிமிடம் உலர்ந்த காற்றில் ஓய்வெடுக்க வேண்டும்.",
                                "A 15-minute fresh air rest interval is legally required every 3 hours."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Shift History List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("முந்தைய ஷிப்ட் பதிவுகள்", "Past Shift Log & Output"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${shifts.size} " + tr("பதிவுகள்", "Records"),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val completedShifts = shifts.filter { it.status == "COMPLETED" }
        if (completedShifts.isEmpty()) {
            item {
                Text(
                    text = tr("முந்தைய ஷிப்ட் பதிவுகள் ஏதுமில்லை.", "No completed shifts recorded yet."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(completedShifts) { shift ->
                ShiftHistoryCard(shift = shift)
            }
        }
    }

    if (showClockInDialog) {
        ClockInDialog(
            onDismiss = { showClockInDialog = false },
            onClockIn = { shiftType, room, task, rate ->
                viewModel.clockInShift(
                    shiftType = shiftType,
                    activeRoom = room,
                    workCategory = task,
                    pieceRate = rate
                )
                showClockInDialog = false
            }
        )
    }
}

@Composable
fun ShiftHistoryCard(shift: ShiftEntity) {
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    val startStr = dateFormat.format(Date(shift.clockInTime))
    val endStr = shift.clockOutTime?.let { dateFormat.format(Date(it)) } ?: "--:--"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${shift.shiftDate} • ${when(shift.shiftType) { "MORNING" -> tr("காலை", "Morning") "AFTERNOON" -> tr("பிற்பகல்", "Afternoon") else -> tr("இரவு", "Night") }} ${tr("ஷிப்ட்", "Shift")}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${when (shift.workCategory) {
                        "Bijili Cracker Rolling" -> tr("பிஜிலி பட்டாசு சுற்றல்", "Bijili Cracker Rolling")
                        "Flowerpot Chemical Filling" -> tr("பூந்தொட்டி நிரப்புதல்", "Flowerpot Chemical Filling")
                        "Sparkler Chemical Dipping" -> tr("கம்பி மத்தாப்பு நனைத்தல்", "Sparkler Chemical Dipping")
                        "Fuse Cutting & Twisting" -> tr("திரி வெட்டுதல் & முறுக்குதல்", "Fuse Cutting & Twisting")
                        else -> shift.workCategory
                    }} (${shift.activeRoom})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$startStr - $endStr",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${shift.piecesProduced} " + tr("எண்ணிக்கை", "pcs"),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = IndustrialTeal
                )
                Text(
                    text = "₹${"%.2f".format(shift.calculatedWage)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafetyGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = tr("பூட்டப்பட்டது", "Locked"),
                        fontSize = 10.sp,
                        color = SafetyGreen
                    )
                }
            }
        }

        // Room DB Biometric Seal Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = IndustrialTeal,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Room DB • " + if (shift.clockInBiometricHash.isNotBlank()) "SHA: ${shift.clockInBiometricHash.take(10)}" else tr("பயோமெட்ரிக் உறுதி", "Biometric Signed"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialTeal
                )
            }
            Text(
                text = if (shift.durationMinutes > 0) "${shift.durationMinutes / 60}h ${shift.durationMinutes % 60}m" else tr("முழு ஷிப்ட்", "Full Shift"),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInDialog(
    onDismiss: () -> Unit,
    onClockIn: (String, String, String, Double) -> Unit
) {
    val tasks = listOf(
        TaskCategoryOption("பிஜிலி பட்டாசு சுற்றல்", "Bijili Cracker Rolling", 0.40, "Room R-101"),
        TaskCategoryOption("பூந்தொட்டி மருந்து நிரப்புதல்", "Flowerpot Chemical Filling", 0.65, "Room R-102"),
        TaskCategoryOption("கம்பி மத்தாப்பு நனைத்தல்", "Sparkler Chemical Dipping", 0.45, "Room R-104"),
        TaskCategoryOption("திரி வெட்டுதல் & முறுக்குதல்", "Fuse Cutting & Twisting", 0.35, "Room R-103")
    )

    var selectedShiftType by remember { mutableStateOf("MORNING") }
    var selectedTask by remember { mutableStateOf(tasks[0]) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("clock_in_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("ஷிப்ட் பயோமெட்ரிக் தொடக்கம்", "Biometric Shift Check-In"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = tr("ஷிப்ட் நேரம்:", "Shift Timing:"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MORNING" to tr("காலை", "Morning"), "AFTERNOON" to tr("பிற்பகல்", "Afternoon"), "NIGHT" to tr("இரவு", "Night")).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedShiftType == key,
                            onClick = { selectedShiftType = key },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = tr("செய்ய வேண்டிய பணி வகை:", "Task Category:"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tasks.forEach { task ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTask = task },
                            color = if (selectedTask.titleEn == task.titleEn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (LanguageManager.currentLanguage == AppLanguage.ENGLISH) task.titleEn else task.titleTa,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    if (LanguageManager.currentLanguage != AppLanguage.ENGLISH) {
                                        Text(
                                            text = "${task.titleEn} (${task.defaultRoom})",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Text(
                                            text = task.defaultRoom,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "₹${task.defaultPieceRate}/unit",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(tr("ரத்து", "Cancel"))
                    }

                    Button(
                        onClick = {
                            onClockIn(
                                selectedShiftType,
                                selectedTask.defaultRoom,
                                selectedTask.titleEn,
                                selectedTask.defaultPieceRate
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialTeal),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_shift_clockin_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("கைரேகையுடன் தொடங்கு", "Clock In"), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
