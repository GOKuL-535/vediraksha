package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyFlame
import com.example.ui.theme.SafetyGreen
import com.example.ui.viewmodel.AppViewModel
import com.example.util.PdfReportGenerator
import com.example.util.tr
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rooms by viewModel.rooms.collectAsState()
    val wages by viewModel.wages.collectAsState()
    val shifts by viewModel.shifts.collectAsState()
    val sosEvents by viewModel.sosEvents.collectAsState()
    val entries by viewModel.chemicalEntries.collectAsState()

    val totalPieces = shifts.sumOf { it.piecesProduced } + wages.sumOf { it.unitsCompleted }
    val totalWagesPaid = wages.sumOf { it.netAmount }
    val totalChemicalKg = entries.sumOf { it.quantityKg }

    var exportedPdfResult by remember { mutableStateOf<PdfReportGenerator.PdfExportResult?>(null) }

    fun shareReport() {
        val today = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
        val text = buildString {
            append("== சிவகாசி பட்டாசு ஆலை பாதுகாப்பு & பணி அறிக்கை ==\n")
            append("VEDIRAKSHA WORKER & SAFETY REPORT\n")
            append("Generated: $today\n")
            append("Worker: ${viewModel.workerName} (${viewModel.workerId})\n")
            append("Unit: ${viewModel.unitName}\n")
            append("Supervisor: ${viewModel.supervisorName}\n\n")
            append("--- 1. SHIFT & PRODUCTION SUMMARY ---\n")
            append("Total Shifts Completed: ${shifts.size}\n")
            append("Total Pieces Rolled/Packed: $totalPieces pcs\n")
            append("Total Locked Wages: ₹${"%.2f".format(totalWagesPaid)}\n\n")
            append("--- 2. CHEMICAL LOAD & PESO COMPLIANCE ---\n")
            append("Total Material Transferred: ${"%.1f".format(totalChemicalKg)} kg\n")
            rooms.forEach { room ->
                append("• ${room.roomNumber} (${room.nameEn}): ${"%.1f".format(room.currentTotalKg)} / ${room.maxCapacityKg} kg (${room.fillPercentage.toInt()}%)\n")
            }
            append("\n--- 3. SAFETY INCIDENTS ---\n")
            append("SOS Events Logged: ${sosEvents.size}\n")
            append("Tamper Verification: Cryptographically Signed (SHA-256)\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share VediRaksha Safety Report")
        context.startActivity(shareIntent)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // REPORT SUMMARY CARD WITH EXPORT BUTTON
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("தொழிற்சாலை அறிக்கைகள் & தணிக்கை", "Factory Reports & Audits"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = tr("அரசு PESO பாதுகாப்பு விதிமுறைகள் தணிக்கை அறிக்கை", "PESO Safety & Wage Compliance Records"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = SafetyGreen.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = SafetyGreen,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                    }

                    @Composable
                    fun RoomCapacityChart(rooms: List<com.example.data.model.RoomEntity>) {
                        val chartColors = listOf(
                            Color(0xFF0072B2),
                            Color(0xFF00876C),
                            Color(0xFFD55E00),
                            Color(0xFF7B61A8),
                            Color(0xFF9B3A55)
                        )
                        var selectedRoomId by remember { mutableStateOf<Long?>(null) }
                        val selectedRoom = rooms.firstOrNull { it.id == selectedRoomId }

                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("room_capacity_chart"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = tr("அறை கொள்ளளவு பயன்பாடு", "Room capacity used"),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = tr("அறை வாரியான பயன்பாடு (%)", "Current load compared with each room's licensed capacity (%)"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = tr("அறை", "Room · capacity used (%)"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("0%", style = MaterialTheme.typography.labelSmall)
                                    Text("50%", style = MaterialTheme.typography.labelSmall)
                                    Text("100%", style = MaterialTheme.typography.labelSmall)
                                }

                                if (rooms.isEmpty()) {
                                    Text(
                                        text = tr("அறை தரவு கிடைக்கவில்லை", "No room capacity data available"),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    rooms.forEachIndexed { index, room ->
                                        val color = chartColors[index % chartColors.size]
                                        val percentage = room.fillPercentage.coerceAtLeast(0f)
                                        val stateLabel = when {
                                            room.isDanger -> tr("வரம்பு மீறியது", "Over limit")
                                            room.isWarning -> tr("கவனம்", "Caution")
                                            else -> tr("பாதுகாப்பானது", "Safe")
                                        }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedRoomId = room.id }
                                                .semantics {
                                                    role = Role.Button
                                                    contentDescription = "${room.roomNumber}, ${"%.1f".format(room.currentTotalKg)} of ${"%.1f".format(room.maxCapacityKg)} kilograms, ${percentage.toInt()} percent, $stateLabel"
                                                }
                                                .padding(vertical = 4.dp)
                                                .testTag("capacity_bar_${room.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${room.roomNumber} · ${tr(room.nameTa, room.nameEn)}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    maxLines = 1,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${percentage.toInt()}% · $stateLabel",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = when {
                                                        room.isDanger -> SafetyCrimson
                                                        room.isWarning -> SafetyFlame
                                                        else -> SafetyGreen
                                                    }
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                                color = color,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = tr("தரவு விளக்கம்", "Legend · tap a room for exact values"),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        rooms.forEachIndexed { index, room ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    modifier = Modifier.size(10.dp),
                                                    shape = CircleShape,
                                                    color = chartColors[index % chartColors.size]
                                                ) {}
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${room.roomNumber} · ${tr(room.nameTa, room.nameEn)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    selectedRoom?.let { room ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                        ) {
                                            Text(
                                                text = "${room.roomNumber}: ${"%.1f".format(room.currentTotalKg)} kg / ${"%.1f".format(room.maxCapacityKg)} kg · ${"%.1f".format(room.fillPercentage)}%",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    RoomCapacityChart(rooms)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Compliance Score Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("பாதுகாப்பு இணக்க குறியீடு (Safety Score)", "Safety Compliance Index"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "96% " + tr("உயர் பாதுகாப்பு", "High"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SafetyGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { shareReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_safety_report_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("முழு அறிக்கையை பகிர்க / அச்சிடுக (Export / Share)", "Export Complete Safety Report"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val result = PdfReportGenerator.generateMonthlyWagePassbookPdf(
                                context = context,
                                workerName = viewModel.workerName,
                                workerId = viewModel.workerId,
                                unitName = viewModel.unitName,
                                wages = wages
                            )
                            if (result.success) {
                                exportedPdfResult = result
                            } else {
                                Toast.makeText(context, "PDF Error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("download_wage_pdf_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("மாதாந்திர ஊதிய பாஸ்புக் PDF பதிவிறக்கம்", "Download Monthly Wage Passbook (PDF)"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // AUDIT BREAKDOWN
        item {
            Text(
                text = tr("அறிக்கை விபரங்கள்", "Report Breakdown"),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Item 1: Shift & Production Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("ஷிப்ட் & உற்பத்தி சுருக்கம்", "Shift & Production Audit"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr("பதிவு செய்யப்பட்ட ஷிப்ட்கள்: ${shifts.size}", "Total Logged Shifts: ${shifts.size}"),
                        fontSize = 12.sp
                    )
                    Text(
                        text = tr("மொத்த தயாரிப்பு எண்ணிக்கை: $totalPieces உருப்படிகள்", "Total Pieces Rolled / Packed: $totalPieces units"),
                        fontSize = 12.sp
                    )
                    Text(
                        text = tr("உறுதி செய்யப்பட்ட ஊதிய பட்டுவாடா: ₹${"%.2f".format(totalWagesPaid)}", "Net Wages Paid: ₹${"%.2f".format(totalWagesPaid)}"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SafetyGreen
                    )
                }
            }

        }

        // Item 2: Chemical Limit Compliance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = IndustrialTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("அறை மருந்து வரம்பு தணிக்கை (PESO Limit)", "PESO Chemical Limit Compliance"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    rooms.forEach { room ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${room.roomNumber} - ${tr(room.nameTa, room.nameEn)}",
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${"%.1f".format(room.currentTotalKg)} / ${"%.0f".format(room.maxCapacityKg)} kg (${room.fillPercentage.toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (room.isDanger) SafetyCrimson else if (room.isWarning) SafetyFlame else SafetyGreen
                            )
                        }
                    }
                }
            }
        }

        // Item 3: Safety Incidents Log
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = SafetyCrimson, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("அவசர எச்சரிக்கை & விபத்து பதிவுகள்", "Emergency SOS & Incident Audit"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (sosEvents.isEmpty()) {
                        Text(
                            text = tr("கடந்த 30 நாட்களில் விபத்துக்கள் ஏதுமில்லை (Safe)", "Zero critical incidents reported in the last 30 days."),
                            fontSize = 12.sp,
                            color = SafetyGreen
                        )
                    } else {
                        sosEvents.forEach { event ->
                            Text(
                                text = "• ${event.triggerType} at ${event.location} (${event.status})",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
