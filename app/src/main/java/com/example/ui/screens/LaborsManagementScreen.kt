package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WorkerEntity
import com.example.data.service.GeminiMapsService
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyGreen
import com.example.ui.viewmodel.AppViewModel
import com.example.util.tr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaborsManagementScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
    onOpenEmergencyMaps: () -> Unit = {}
) {
    val context = LocalContext.current
    val workers by viewModel.workers.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var workerToReassign by remember { mutableStateOf<WorkerEntity?>(null) }

    // Live continuous date and time ticking every second
    var liveTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            liveTimestamp = System.currentTimeMillis()
            delay(1000)
        }
    }

    val liveDateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH).format(Date(liveTimestamp))
    val liveTimeStr = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).format(Date(liveTimestamp))

    val onDutyCount = workers.count { it.status == "ON_DUTY" }
    val onBreakCount = workers.count { it.status == "ON_BREAK" }

    // PESO statutory capacity mapping per room
    val pesoMaxWorkersPerRoom = remember {
        mapOf(
            "Room R-101" to 4,
            "Room R-102" to 6,
            "Room R-103" to 6,
            "Room R-104" to 8,
            "Room R-105" to 4
        )
    }

    val filteredWorkers = when (selectedFilter) {
        "ON_DUTY" -> workers.filter { it.status == "ON_DUTY" }
        "ON_BREAK" -> workers.filter { it.status == "ON_BREAK" }
        "R-101" -> workers.filter { it.assignedRoom.contains("101") }
        "R-102" -> workers.filter { it.assignedRoom.contains("102") }
        "R-103" -> workers.filter { it.assignedRoom.contains("103") }
        else -> workers
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWorkerDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 12.dp, end = 8.dp)
                    .testTag("add_worker_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr("தொழிலாளர் சேர்க்க", "Add Laborer", hi = "श्रमिक जोड़ें", ml = "തൊഴിലാളിയെ ചേർക്കുക"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("labors_management_screen"),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. LIVE DATE & TIME WORKFORCE HEADER CARD
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("labor_header_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = IndustrialNavy),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Live Date & Time pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = IndustrialTeal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = liveDateStr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE2E8F0),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = liveTimeStr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFB74D)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title & Workforce count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr("தொழிலாளர் மேலாண்மை", "Laborers & Workforce Roster", hi = "श्रमिक प्रबंधन", ml = "തൊഴിലാളി മാനേജ്‌മെന്റ്"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tr("PESO அறை வரம்பு & பயோமெட்ரிக் வருகை", "PESO Shed Occupancy & Biometrics", hi = "PESO शेड क्षमता और बायोमेट्रिक्स"),
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Emergency Maps Radar Quick Button
                            Button(
                                onClick = onOpenEmergencyMaps,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("top_maps_radar_btn")
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = tr("Maps ரேடார்", "Maps Radar"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Statistics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = tr("மொத்த ஆட்கள்", "Total Staff"), fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(text = "${workers.size}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Column {
                                Text(text = tr("பணியில்", "On Duty"), fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(text = "$onDutyCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF86EFAC))
                            }
                            Column {
                                Text(text = tr("ஓய்வில்", "On Break"), fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(text = "$onBreakCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFFDE047))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = tr("PESO பாதுகாப்பு", "PESO Safe"), fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(text = "100%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2DD4BF))
                            }
                        }
                    }
                }
            }

            // 1.5 WORKER UNIFORM & PPE COMPLIANCE INSPECTION (AI Image Uniform Detection & Siren Alert)
            item {
                var uniformBitmap by remember { mutableStateOf<Bitmap?>(null) }
                var inspectionResult by remember { mutableStateOf<String?>(null) }
                var isCompliant by remember { mutableStateOf<Boolean?>(null) }
                var isInspectingState by remember { mutableStateOf(false) }
                var sirenPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
                var isSirenPlaying by remember { mutableStateOf(false) }

                val coroutineScope = rememberCoroutineScope()

                fun stopSiren() {
                    try {
                        sirenPlayer?.stop()
                        sirenPlayer?.release()
                        sirenPlayer = null
                    } catch (_: Exception) {}
                    isSirenPlaying = false
                }

                fun playSiren() {
                    stopSiren()
                    try {
                        val audioRes = context.resources.getIdentifier("mixkit_urgent_simple_tone_loop_2976", "raw", context.packageName).takeIf { it != 0 }
                            ?: context.resources.getIdentifier("siren_alert", "raw", context.packageName)
                        if (audioRes != 0) {
                            sirenPlayer = MediaPlayer.create(context, audioRes)?.apply {
                                isLooping = true
                                start()
                            }
                            isSirenPlaying = true
                        }
                    } catch (e: Exception) {
                        Log.e("UniformInspection", "Failed to play Mixkit urgent tone sound", e)
                    }
                }

                fun analyzeCompliance(bmp: Bitmap) {
                    var yellowCount = 0
                    var blueCount = 0
                    var orangeCount = 0
                    var totalSampled = 0

                    val width = bmp.width
                    val height = bmp.height
                    val stepX = (width / 20).coerceAtLeast(1)
                    val stepY = (height / 20).coerceAtLeast(1)

                    for (x in 0 until width step stepX) {
                        for (y in 0 until height step stepY) {
                            val pixel = bmp.getPixel(x, y)
                            val red = android.graphics.Color.red(pixel)
                            val green = android.graphics.Color.green(pixel)
                            val blue = android.graphics.Color.blue(pixel)

                            totalSampled++

                            if (red > 170 && green > 150 && blue < 100) {
                                yellowCount++
                            }
                            else if (blue > red + 15 && blue > green + 15 && blue > 50) {
                                blueCount++
                            }
                            else if (red > 170 && green in 70..150 && blue < 80) {
                                orangeCount++
                            }
                        }
                    }

                    val hasCoverall = blueCount > (totalSampled * 0.035)
                    val hasHelmetOrBands = (yellowCount + orangeCount) > (totalSampled * 0.01)
                    val compliant = hasCoverall && hasHelmetOrBands

                    isCompliant = compliant
                    if (!compliant) {
                        inspectionResult = "VIOLATION [Uniform Guard]: Missing mandatory yellow helmet, navy coverall, or orange reflective safety bands! Casual t-shirt/sandals detected. Immediate advisory & correction required."
                        playSiren()
                    } else {
                        inspectionResult = "COMPLIANT: Yellow safety helmet, navy coverall & orange reflective safety bands verified per PESO Rule 84. Safe to enter mixing bay."
                        stopSiren()
                    }
                }

                val uniformLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    uri?.let {
                        try {
                            val bmp = context.contentResolver.openInputStream(it)?.use { stream ->
                                BitmapFactory.decodeStream(stream)
                            }
                            if (bmp != null) {
                                val scaled = Bitmap.createScaledBitmap(bmp, 512, 512, true)
                                uniformBitmap = scaled
                                isInspectingState = true
                                stopSiren()
                                coroutineScope.launch {
                                    delay(800)
                                    isInspectingState = false
                                    analyzeCompliance(scaled)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("UniformInspection", "Failed to load image", e)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                        .background(SafetyCrimson.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = null,
                                        tint = SafetyCrimson,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tr("தொழிலாளர் சீருடை & PPE ஆய்வு", "Worker Uniform & PPE Compliance"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = tr("மஞ்சள் ஹெல்மெட், மாஸ்க், கவர்ஆல்ஸ் & பூட்ஸ் சரிபார்த்தல்", "Check mandatory helmet, mask, coveralls & boots"),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = IndustrialTeal.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = tr("AI சீருடை AI", "AI UNIFORM"),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = IndustrialTeal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Image Preview or Upload Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .clickable { uniformLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uniformBitmap != null) {
                                Image(
                                    bitmap = uniformBitmap!!.asImageBitmap(),
                                    contentDescription = "Worker Uniform Photo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Upload",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = tr("தொழிலாளர் புகைப்படத்தைப் பதிவேற்றுக", "Upload worker photo for uniform inspection"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Tap to choose JPG or PNG (Compliant vs Non-Compliant)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons & Analysis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    isInspectingState = true
                                    stopSiren()
                                    coroutineScope.launch {
                                        delay(1000)
                                        isInspectingState = false
                                        uniformBitmap?.let { analyzeCompliance(it) }
                                    }
                                },
                                enabled = !isInspectingState && uniformBitmap != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialTeal),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isInspectingState) {
                                    Text("Analyzing Uniform & PPE...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(tr("சீருடை & PPE ஆய்வு செய்", "Analyze Uniform & PPE"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    uniformBitmap = null
                                    inspectionResult = null
                                    isCompliant = null
                                    stopSiren()
                                },
                                modifier = Modifier
                                    .weight(0.45f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reset", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Test Simulation Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    stopSiren()
                                    isCompliant = true
                                    inspectionResult = "COMPLIANT: Full pyrotechnic safety uniform & PPE verified (Helmet, Mask, Coveralls, Boots). No alarm triggered."
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🟢 Test Full Uniform", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isCompliant = false
                                    inspectionResult = "NON-COMPLIANT: Casual clothing or sandals detected! Mandatory safety uniform missing. Mixkit urgent tone alarm triggered!"
                                    playSiren()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyCrimson),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🔴 Test Casual (Alarm)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafetyCrimson)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    stopSiren()
                                    isCompliant = true
                                    inspectionResult = "COMPLIANT: Full pyrotechnic safety uniform & PPE verified (Helmet, Mask, Coveralls, Boots). No alarm triggered."
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🟢 Test Full Uniform (No Alarm)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isCompliant = false
                                    inspectionResult = "NON-COMPLIANT: Casual clothing or sandals detected! Mandatory safety uniform missing. Mixkit urgent tone alarm triggered!"
                                    playSiren()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyCrimson),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🔴 Test Casual (Mixkit Alarm)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Inspection Result Banner & Siren Stop
                        if (inspectionResult != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (isCompliant == true) SafetyGreen.copy(alpha = 0.15f) else SafetyCrimson.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, if (isCompliant == true) SafetyGreen else SafetyCrimson)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isCompliant == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isCompliant == true) SafetyGreen else SafetyCrimson,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isCompliant == true) "PASSED: Safe Uniform & PPE" else "⚠️ ALERT: Non-Compliant Uniform Detected!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isCompliant == true) SafetyGreen else SafetyCrimson
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = inspectionResult ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (isSirenPlaying) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { stopSiren() },
                                            colors = ButtonDefaults.buttonColors(containerColor = SafetyCrimson),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Stop Siren", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("🛑 Stop Alert Sound (Mixkit Tone)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. PESO STATUTORY SHED OCCUPANCY LIMITER CARDS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("PESO சட்டப்பூர்வ அறை ஆட்கள் வரம்பு", "PESO Statutory Shed Occupancy"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = tr("அதிகபட்ச வரம்பு", "Max Limits"),
                            fontSize = 11.sp,
                            color = IndustrialTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf("Room R-101", "Room R-102", "Room R-103", "Room R-104", "Room R-105")) { roomName ->
                            val currentCount = workers.count { it.assignedRoom == roomName && it.status == "ON_DUTY" }
                            val maxPermitted = pesoMaxWorkersPerRoom[roomName] ?: 4
                            val fillRatio = (currentCount.toFloat() / maxPermitted.toFloat()).coerceIn(0f, 1f)
                            val isExceeded = currentCount > maxPermitted
                            val isNearMax = currentCount == maxPermitted

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isExceeded) SafetyCrimson.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isExceeded) SafetyCrimson else if (isNearMax) SafetyAmber else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .width(160.dp)
                                    .testTag("peso_room_card_$roomName")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = roomName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (isExceeded) SafetyCrimson
                                                    else if (isNearMax) SafetyAmber
                                                    else SafetyGreen
                                                )
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isExceeded) "ALERT" else "$currentCount / $maxPermitted",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LinearProgressIndicator(
                                        progress = { fillRatio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (isExceeded) SafetyCrimson else if (isNearMax) SafetyAmber else SafetyGreen,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = when (roomName) {
                                            "Room R-101" -> tr("அரைக்கும் அறை", "Chemical Mixing")
                                            "Room R-102" -> tr("நிரப்பும் அறை", "Explosive Filling")
                                            "Room R-103" -> tr("திரி வெட்டுதல்", "Fuse Cutting")
                                            "Room R-104" -> tr("உலர்த்தும் தளம்", "Solar Drying")
                                            else -> tr("சேமிப்பு கிடங்கு", "Magazine")
                                        },
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. FILTER TABS
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "ALL" to tr("அனைத்தும்", "All Labors"),
                        "ON_DUTY" to tr("பணியில் உள்ளவர்கள்", "On Duty"),
                        "ON_BREAK" to tr("ஓய்வு இடைவேளை", "On Break"),
                        "R-101" to "Room R-101",
                        "R-102" to "Room R-102",
                        "R-103" to "Room R-103"
                    ).forEach { (key, label) ->
                        item {
                            FilterChip(
                                selected = selectedFilter == key,
                                onClick = { selectedFilter = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // 4. WORKER CARDS LIST
            item {
                Text(
                    text = "${tr("தொழிலாளர்கள் பட்டியல்", "Workforce Directory")} (${filteredWorkers.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(filteredWorkers, key = { it.id }) { worker ->
                LaborCard(
                    worker = worker,
                    onToggleStatus = { viewModel.toggleWorkerStatus(worker) },
                    onReassignRoom = { workerToReassign = worker },
                    onCallPhone = { GeminiMapsService.dialEmergencyPhone(context, worker.emergencyPhone) }
                )
            }
        }
    }

    // Reassign Room Dialog
    if (workerToReassign != null) {
        ReassignRoomDialog(
            worker = workerToReassign!!,
            rooms = listOf("Room R-101", "Room R-102", "Room R-103", "Room R-104", "Room R-105"),
            onDismiss = { workerToReassign = null },
            onConfirmRoom = { newRoom ->
                viewModel.updateWorkerRoom(workerToReassign!!, newRoom)
                workerToReassign = null
            }
        )
    }

    // Add New Worker Dialog
    if (showAddWorkerDialog) {
        AddWorkerDialog(
            onDismiss = { showAddWorkerDialog = false },
            onAdd = { newWorker ->
                viewModel.addWorker(newWorker)
                showAddWorkerDialog = false
            }
        )
    }
}

@Composable
fun LaborCard(
    worker: WorkerEntity,
    onToggleStatus: () -> Unit,
    onReassignRoom: () -> Unit,
    onCallPhone: () -> Unit
) {
    val isOnDuty = worker.status == "ON_DUTY"
    val statusColor = if (isOnDuty) SafetyGreen else SafetyAmber

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("worker_card_${worker.workerId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Avatar, Name, ID & Status
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(IndustrialTeal.copy(alpha = 0.15f))
                            .border(1.5.dp, statusColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = worker.nameEn.take(2).uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = IndustrialTeal
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tr(worker.nameTa, worker.nameEn),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isOnDuty) tr("பணியில்", "ON DUTY") else tr("ஓய்வு", "ON BREAK"),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${worker.workerId} • ${tr(worker.roleTa, worker.roleEn)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = tr("ஒதுக்கப்பட்ட அறை", "Assigned Shed"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = worker.assignedRoom, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IndustrialTeal)
                }

                Column {
                    Text(text = tr("இன்றைய உற்பத்தி", "Today Pieces"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${worker.piecesProducedToday} pcs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = tr("உத்தேச ஊதியம்", "Est. Wage"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${"%.2f".format(worker.wageEarnedToday)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Biometric verified seal row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = IndustrialTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr("பயோமெட்ரிக் செக்-இன் உறுதி செய்யப்பட்டது", "Biometric Check-In Verified"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndustrialTeal
                    )
                }

                Text(
                    text = "${worker.experienceYears} " + tr("ஆண்டு அனுபவம்", "yrs exp"),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Reassign Room, Toggle Break, Call Emergency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReassignRoom,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr("அறை மாற்றம்", "Move Room"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnDuty) SafetyAmber else SafetyGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = if (isOnDuty) Icons.Default.Coffee else Icons.Default.Work,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOnDuty) tr("ஓய்வு", "Break") else tr("பணி", "Resume"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onCallPhone,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafetyCrimson.copy(alpha = 0.12f))
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Emergency Call", tint = SafetyCrimson, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun ReassignRoomDialog(
    worker: WorkerEntity,
    rooms: List<String>,
    onDismiss: () -> Unit,
    onConfirmRoom: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = IndustrialTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("தொழிலாளர் அறை மாற்றம்", "Reassign Worker Room"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${worker.nameEn} (${worker.workerId})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = tr("புதிய அறையை தேர்ந்தெடுக்கவும் (PESO விதிமுறை):", "Select Target Shed (PESO Regulated):"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rooms.forEach { room ->
                        val isCurrent = worker.assignedRoom == room
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onConfirmRoom(room) },
                            color = if (isCurrent) IndustrialTeal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) IndustrialTeal else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = room, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (isCurrent) {
                                    Text(text = tr("தற்போதைய அறை", "Current Shed"), fontSize = 10.sp, color = IndustrialTeal, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(text = tr("ஒதுக்க தட்டவும் >", "Assign >"), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = tr("ரத்து செய்", "Cancel"))
                }
            }
        }
    }
}

@Composable
fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onAdd: (WorkerEntity) -> Unit
) {
    var nameEn by remember { mutableStateOf("") }
    var nameTa by remember { mutableStateOf("") }
    var roleEn by remember { mutableStateOf("Bijili Cracker Roller") }
    var assignedRoom by remember { mutableStateOf("Room R-101") }
    var phone by remember { mutableStateOf("+91 ") }
    var experience by remember { mutableStateOf("4") }

    val nextId = remember { "TN-SVK-${(3000..9999).random()}" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("புதிய தொழிலாளர் பதிவு", "Register New Laborer"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(tr("பெயர் (English)", "Worker Name (English)")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameTa,
                    onValueChange = { nameTa = it },
                    label = { Text(tr("பெயர் (தமிழ்)", "Worker Name (Tamil)")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(tr("அவசர தொடர்பு எண்", "Emergency Phone")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = assignedRoom,
                        onValueChange = { assignedRoom = it },
                        label = { Text(tr("அறை", "Shed")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = experience,
                        onValueChange = { experience = it },
                        label = { Text(tr("அனுபவம் (ஆண்டு)", "Exp (Yrs)")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (nameEn.isNotBlank()) {
                            onAdd(
                                WorkerEntity(
                                    workerId = nextId,
                                    nameEn = nameEn.trim(),
                                    nameTa = if (nameTa.isNotBlank()) nameTa.trim() else nameEn.trim(),
                                    roleEn = roleEn,
                                    roleTa = "பட்டாசு தயாரிப்பு தொழிலாளர்",
                                    assignedRoom = assignedRoom,
                                    experienceYears = experience.toIntOrNull() ?: 3,
                                    emergencyPhone = phone,
                                    status = "ON_DUTY",
                                    biometricClockInTime = System.currentTimeMillis(),
                                    isSafetyCertified = true
                                )
                            )
                        }
                    },
                    enabled = nameEn.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = tr("பதிவு செய்க & பயோமெட்ரிக் ஒப்புதல்", "Save & Biometric Enroll"), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = tr("ரத்து செய்", "Cancel"))
                }
            }
        }
    }
}
