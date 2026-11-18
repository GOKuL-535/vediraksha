package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AiCallAgentDialog
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyGreen
import com.example.ui.viewmodel.AppViewModel
import com.example.util.tr
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EscalationAlertItem(val id: String, val title: String, val src: String)
data class EscalationStepItem(
    val id: String,
    val label: String,
    var detail: String,
    var status: String, // pending, running, ok, fail, skip
    var progress: Float? = null
)
data class LogEntry(val time: String, val channel: String, val message: String, val isError: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertEscalationScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val alerts = listOf(
        EscalationAlertItem("cpu", "CPU usage above 95%", "web-prod-02"),
        EscalationAlertItem("pay", "Payment API returning 5xx errors", "checkout-api"),
        EscalationAlertItem("disk", "Disk 92% full on db-primary", "db-primary"),
        EscalationAlertItem("lag", "Replica lag over 60 s", "db-replica-1"),
        EscalationAlertItem("peso", "Mixing Shed R-101 PESO limit breached (>15kg)", "room-101-sensor")
    )

    var selectedAlertIndex by remember { mutableIntStateOf(0) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val vmAdminEmail by viewModel.adminEmail.collectAsState()
    val vmAdminPhone by viewModel.adminPhone.collectAsState()
    val vmVishwaPhone by viewModel.vishwaPhone.collectAsState()
    val vmGokulPhone by viewModel.gokulPhone.collectAsState()

    var adminEmail by remember(vmAdminEmail) { mutableStateOf(vmAdminEmail) }
    var adminPhone by remember(vmAdminPhone) { mutableStateOf(vmAdminPhone) }
    var vishwaPhone by remember(vmVishwaPhone) { mutableStateOf(vmVishwaPhone) }
    var gokulPhone by remember(vmGokulPhone) { mutableStateOf(vmGokulPhone) }
    var saveSuccessMessage by remember { mutableStateOf("") }

    var waitSecs by remember { mutableIntStateOf(12) }
    var callTries by remember { mutableIntStateOf(3) }
    var gapSecs by remember { mutableIntStateOf(4) }

    var showAiCallDialog by remember { mutableStateOf(false) }

    var simulateEmailOk by remember { mutableStateOf(true) }
    var simulateSmsOk by remember { mutableStateOf(true) }
    var simulateAnswerOn by remember { mutableIntStateOf(2) } // 1, 2, 3, 0

    var isRunning by remember { mutableStateOf(false) }
    var isAcked by remember { mutableStateOf(false) }
    var pillState by remember { mutableStateOf("IDLE") } // IDLE, ESCALATING, ACK, REACHED, UNREACHABLE
    var phoneState by remember { mutableStateOf("STANDBY") } // STANDBY, RINGING, ANSWERED, MISSED

    var steps by remember { mutableStateOf<List<EscalationStepItem>>(emptyList()) }
    var logs by remember { mutableStateOf<List<LogEntry>>(emptyList()) }

    fun addLog(channel: String, msg: String, isError: Boolean = false) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(Date())
        logs = listOf(LogEntry(timeStr, channel, msg, isError)) + logs
    }

    fun initSteps() {
        steps = listOf(
            EscalationStepItem("raised", "Alert raised", "Starts when an alert fires", "pending"),
            EscalationStepItem("email", "Email", adminEmail, "pending"),
            EscalationStepItem("sms", "SMS", "+91 $adminPhone", "pending"),
            EscalationStepItem("wait", "Waiting for acknowledgement", "${waitSecs}s window", "pending"),
            EscalationStepItem("call", "Auto-call", "Up to $callTries tries, ${gapSecs}s apart", "pending"),
            EscalationStepItem("outcome", "Outcome", "Acknowledged, reached or unreachable", "pending")
        )
    }

    LaunchedEffect(Unit) {
        initSteps()
    }

    // Phone Ringing animation
    val infiniteTransition = rememberInfiniteTransition(label = "phone_ring")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "ring_scale"
    )

    // Escalation async runner loop
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        isAcked = false
        pillState = "ESCALATING"
        phoneState = "STANDBY"

        val currentAlert = alerts[selectedAlertIndex]
        initSteps()

        // 1. Alert raised
        steps = steps.map { if (it.id == "raised") it.copy(status = "ok", detail = "${currentAlert.title} on ${currentAlert.src}") else it }
        addLog("ALERT", "${currentAlert.title} on ${currentAlert.src}")

        // 2. Email & SMS sending
        var okCount = 0
        if (isRunning && !isAcked) {
            steps = steps.map { if (it.id == "email") it.copy(status = "running", detail = "Sending to $adminEmail") else it }
            delay(900)
            if (isAcked) return@LaunchedEffect
            if (simulateEmailOk) {
                okCount++
                steps = steps.map { if (it.id == "email") it.copy(status = "ok", detail = "Delivered to $adminEmail") else it }
                addLog("EMAIL", "Delivered to $adminEmail")
            } else {
                steps = steps.map { if (it.id == "email") it.copy(status = "fail", detail = "Provider rejected message") else it }
                addLog("EMAIL", "Failed to send to $adminEmail", isError = true)
            }
        }

        if (isRunning && !isAcked) {
            steps = steps.map { if (it.id == "sms") it.copy(status = "running", detail = "Sending to +91 $adminPhone") else it }
            delay(900)
            if (isAcked) return@LaunchedEffect
            if (simulateSmsOk) {
                okCount++
                steps = steps.map { if (it.id == "sms") it.copy(status = "ok", detail = "Delivered to +91 $adminPhone") else it }
                addLog("SMS", "Delivered to +91 $adminPhone")
            } else {
                steps = steps.map { if (it.id == "sms") it.copy(status = "fail", detail = "Provider rejected SMS") else it }
                addLog("SMS", "Failed to send to +91 $adminPhone", isError = true)
            }
        }

        // 3. Waiting for ACK
        if (isRunning && !isAcked) {
            if (okCount > 0) {
                for (s in waitSecs downTo 1) {
                    if (isAcked) break
                    val prog = (waitSecs - s + 1).toFloat() / waitSecs
                    steps = steps.map { if (it.id == "wait") it.copy(status = "running", detail = "${s}s left", progress = prog) else it }
                    delay(1000)
                }
                if (isAcked) return@LaunchedEffect
                steps = steps.map { if (it.id == "wait") it.copy(status = "fail", detail = "No acknowledgement after ${waitSecs}s", progress = null) else it }
                addLog("ALERT", "No acknowledgement, escalating to call")
            } else {
                steps = steps.map { if (it.id == "wait") it.copy(status = "skip", detail = "Skipped, email & SMS both failed") else it }
                addLog("ALERT", "Email & SMS failed, escalating to call")
            }
        }

        // 4. Auto-calls
        var reached = false
        if (isRunning && !isAcked) {
            for (tryNo in 1..callTries) {
                if (isAcked) break
                phoneState = "RINGING"
                addLog("CALL", "Dialing +91 $adminPhone (try $tryNo)")
                delay(2500)
                if (isAcked) break

                val isAnswered = simulateAnswerOn > 0 && tryNo >= simulateAnswerOn
                if (isAnswered) {
                    phoneState = "ANSWERED"
                    addLog("CALL", "Admin answered on try $tryNo")
                    steps = steps.map { if (it.id == "outcome") it.copy(status = "ok", detail = "Admin reached by phone") else it }
                    pillState = "REACHED"
                    reached = true
                    isRunning = false
                    break
                } else {
                    phoneState = "MISSED"
                    addLog("CALL", "No answer on try $tryNo", isError = true)
                    if (tryNo < callTries) {
                        delay(gapSecs * 1000L)
                    }
                }
            }
        }

        if (!reached && !isAcked) {
            steps = steps.map { if (it.id == "outcome") it.copy(status = "fail", detail = "Admin unreachable after $callTries calls") else it }
            addLog("ALERT", "Admin unreachable after $callTries calls", isError = true)
            pillState = "UNREACHABLE"
            isRunning = false
        }
    }

    fun handleAck() {
        if (!isRunning) return
        isAcked = true
        isRunning = false
        pillState = "ACK"
        phoneState = "STANDBY"
        steps = steps.map {
            when (it.status) {
                "running" -> it.copy(status = "skip", detail = "Cancelled, admin acknowledged", progress = null)
                "pending" -> it.copy(status = "skip", detail = "Not needed")
                else -> it
            }
        }.map { if (it.id == "outcome") it.copy(status = "ok", detail = "Acknowledged by admin") else it }
        addLog("ACK", "Admin acknowledged the alert")
    }

    fun handleReset() {
        isRunning = false
        isAcked = false
        pillState = "IDLE"
        phoneState = "STANDBY"
        logs = emptyList()
        initSteps()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("alert_escalation_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Status Pill
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alert Escalation Console",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Email & SMS first -> Auto-call fallback if unacknowledged",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Status Pill
                        val (pillColor, pillBg) = when (pillState) {
                            "ESCALATING" -> Pair(SafetyAmber, SafetyAmber.copy(alpha = 0.2f))
                            "ACK", "REACHED" -> Pair(SafetyGreen, SafetyGreen.copy(alpha = 0.2f))
                            "UNREACHABLE" -> Pair(SafetyCrimson, SafetyCrimson.copy(alpha = 0.2f))
                            else -> Pair(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                        }

                        Surface(
                            color = pillBg,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(pillColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pillState,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = pillColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 1: Trigger Alert & Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TRIGGER AN ALERT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = alerts[selectedAlertIndex].title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Alert Event") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            alerts.forEachIndexed { idx, item ->
                                DropdownMenuItem(
                                    text = { Text("${item.title} (${item.src})") },
                                    onClick = {
                                        selectedAlertIndex = idx
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                handleReset()
                                isRunning = true
                            },
                            enabled = !isRunning && adminPhone.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Trigger Alert", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { handleAck() },
                            enabled = isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Acknowledge", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { handleReset() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text("Reset", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Card 2: Emergency Responders Display & AI Call Agent
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Emergency Responders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Admin, Vishwa & Gokul (3 Contacts)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAiCallDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyCrimson),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = "AI Call Agent", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SafetyCrimson.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = SafetyCrimson, modifier = Modifier.padding(6.dp).size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Admin: ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("+91 $vmAdminPhone", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = SafetyCrimson, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = IndustrialTeal.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = IndustrialTeal, modifier = Modifier.padding(6.dp).size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Vishwa (Responder): ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("+91 $vmVishwaPhone", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = IndustrialTeal, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SafetyAmber.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = SafetyAmber, modifier = Modifier.padding(6.dp).size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Gokul (Safety Officer): ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("+91 $vmGokulPhone", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = SafetyAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Escalation Flow Steps
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ESCALATION FLOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    steps.forEach { step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (markIcon, markColor) = when (step.status) {
                                "ok" -> Pair(Icons.Default.Check, SafetyGreen)
                                "fail" -> Pair(Icons.Default.Close, SafetyCrimson)
                                "running" -> Pair(Icons.Default.NotificationsActive, SafetyAmber)
                                else -> Pair(null, MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, markColor, CircleShape)
                                    .background(markColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (markIcon != null) {
                                    Icon(markIcon, contentDescription = null, tint = markColor, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("-", fontSize = 12.sp, color = markColor)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = step.detail,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (step.progress != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { step.progress ?: 0f },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = SafetyAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Settings & Simulation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EMERGENCY CONTACTS & SETTINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Admin Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = adminPhone,
                        onValueChange = { adminPhone = it.filter { c -> c.isDigit() } },
                        label = { Text("Admin Mobile (+91 9500847066)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = vishwaPhone,
                        onValueChange = { vishwaPhone = it.filter { c -> c.isDigit() } },
                        label = { Text("Vishwa Responder Mobile (+91 8122608250)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = gokulPhone,
                        onValueChange = { gokulPhone = it.filter { c -> c.isDigit() } },
                        label = { Text("Gokul Safety Officer Mobile (+91 9080639112)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.saveEmergencySettings(adminEmail, adminPhone, vishwaPhone, gokulPhone)
                            saveSuccessMessage = "✅ Settings saved & synced across all pages!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Emergency Settings", fontWeight = FontWeight.Bold)
                    }

                    if (saveSuccessMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = saveSuccessMessage,
                            color = SafetyGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SIMULATION & CHANNELS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = simulateEmailOk,
                            onCheckedChange = { simulateEmailOk = it }
                        )
                        Text("Email is delivered", fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = simulateSmsOk,
                            onCheckedChange = { simulateSmsOk = it }
                        )
                        Text("SMS is delivered", fontSize = 13.sp)
                    }
                }
            }
        }

        // Event Log
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EVENT LOG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (logs.isEmpty()) {
                        Text(
                            text = "Events appear here when an alert fires.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        logs.forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.time,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    color = if (log.isError) SafetyCrimson.copy(alpha = 0.2f) else IndustrialTeal.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = log.channel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (log.isError) SafetyCrimson else IndustrialTeal,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = log.message,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAiCallDialog) {
        AiCallAgentDialog(
            onDismiss = { showAiCallDialog = false },
            initialPhone = adminPhone
        )
    }
}
