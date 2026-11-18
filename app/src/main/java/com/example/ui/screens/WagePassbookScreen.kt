package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WageEntryEntity
import com.example.ui.components.DisputeDialog
import com.example.ui.components.PdfExportSuccessDialog
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyGreen
import com.example.ui.viewmodel.AppViewModel
import com.example.util.PdfReportGenerator
import com.example.util.tr

@Composable
fun WagePassbookScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wages by viewModel.wages.collectAsState()
    var selectedDisputeWage by remember { mutableStateOf<WageEntryEntity?>(null) }
    var showAiExplainer by remember { mutableStateOf(false) }
    var exportedPdfResult by remember { mutableStateOf<PdfReportGenerator.PdfExportResult?>(null) }

    val totalGross = wages.sumOf { it.grossAmount }
    val totalDeductions = wages.sumOf { it.deductions }
    val totalNet = wages.sumOf { it.netAmount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("wage_passbook_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // PASSBOOK HEADER CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("passbook_balance_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IndustrialNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("தொழிலாளர் ஊதிய பாஸ்புக்", "Tamper-Proof Wage Passbook"),
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = viewModel.workerName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Cryptographic seal badge
                        Surface(
                            color = IndustrialTeal.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SHA-256 SEAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2DD4BF)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = tr("நிகர ஊதிய இருப்பு (உறுதி செய்யப்பட்டது)", "Total Verified Net Wages"),
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "₹${"%.2f".format(totalNet)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = tr("மொத்த வருவாய்", "Gross Earned"), fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(text = "₹${"%.2f".format(totalGross)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text(text = tr("பாதுகாப்பு சாதன கழிவுகள்", "Deductions"), fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(text = "₹${"%.2f".format(totalDeductions)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = tr("பதிவுகள்", "Entries"), fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(text = "${wages.size} " + tr("நாட்கள்", "Days"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF86EFAC))
                        }
                    }
                }
            }
        }

        // EXPORT WAGE PASSBOOK AS PDF REPORT CARD
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_pdf_report_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF Report",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = tr("மாதாந்திர ஊதிய அறிக்கை PDF", "Monthly Wage PDF Report", hi = "मासिक वेतन PDF रिपोर्ट", ml = "പ്രതിമാസ വേതന PDF"),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                fontSize = 13.sp
                            )
                            Text(
                                text = tr("தனிப்பட்ட பதிவிற்கு PDF பதிவிறக்கம்", "Export & save locally for personal records", hi = "व्यक्तिगत रिकॉर्ड हेतु सहेजें"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

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
                                Toast.makeText(context, "Export error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialNavy),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("export_pdf_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("பதிவிறக்கம்", "Export PDF", hi = "डाउनलोड", ml = "ഡൗൺലോഡ്"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // AI PLAIN-LANGUAGE EXPLAINER CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAiExplainer = !showAiExplainer }
                    .testTag("ai_wage_explainer_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assistant, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr("எளிய தமிழ் ஊதிய விளக்கம்", "Plain-Language Wage Summary"),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            text = if (showAiExplainer) tr("மறைக்க ▲", "Hide ▲") else tr("விளக்கம் ▼", "View ▼"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (showAiExplainer) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = tr(
                                "வணக்கம் காளியப்பன், நீங்கள் மொத்தம் ${wages.size} வேலை நாட்களில் பணியாற்றியுள்ளீர்கள். பிஜிலி பட்டாசு மற்றும் பூந்தொட்டி தயாரிப்பில் உங்களது மொத்த ஊதியம் ₹${"%.0f".format(totalGross)} ஆகும். இதில் கையுறை பாதுகாப்பு உபகரண கழிவு ₹${"%.0f".format(totalDeductions)} போக, உங்கள் வங்கிக்கு வழங்க ஒப்புதல் பெற்ற நிகர தொகை ₹${"%.0f".format(totalNet)} ஆகும். அனைத்து பதிவுகளும் மேற்பார்வையாளர் செல்வராஜ் அவர்களின் கைரேகையுடன் பூட்டப்பட்டுள்ளது.",
                                "Hello Kaliappan, you have worked ${wages.size} shifts. Total gross earnings from Bijili rolling and Flowerpot filling are ₹${"%.0f".format(totalGross)}. After safety gear deductions of ₹${"%.0f".format(totalDeductions)}, your locked net wages are ₹${"%.0f".format(totalNet)}. All entries are signed by supervisor Selvaraj."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // TAMPER-PROOF ENTRIES LIST
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("பூட்டப்பட்ட ஊதிய பதிவுகள் (மாற்ற முடியாதவை)", "Immutable Wage Entries"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = tr("சர்ச்சை கிளப்ப தட்டவும்", "Tap entry to dispute"),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(wages) { wage ->
            WageSlipCard(
                wage = wage,
                onDisputeClick = { selectedDisputeWage = wage }
            )
        }
    }

    if (selectedDisputeWage != null) {
        DisputeDialog(
            wageEntry = selectedDisputeWage,
            onDismiss = { selectedDisputeWage = null },
            onSubmitDispute = { entry, reason ->
                viewModel.disputeWageEntry(entry, reason)
                selectedDisputeWage = null
            }
        )
    }

    if (exportedPdfResult != null) {
        PdfExportSuccessDialog(
            result = exportedPdfResult!!,
            onDismiss = { exportedPdfResult = null },
            onOpenPdf = {
                exportedPdfResult?.file?.let { file ->
                    PdfReportGenerator.openPdf(context, file)
                }
            },
            onSharePdf = {
                exportedPdfResult?.file?.let { file ->
                    PdfReportGenerator.sharePdf(context, file, viewModel.workerName)
                }
            }
        )
    }
}

@Composable
fun WageSlipCard(
    wage: WageEntryEntity,
    onDisputeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wage_slip_card_${wage.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Date & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = wage.date,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    color = if (wage.status == "DISPUTED") SafetyCrimson.copy(alpha = 0.15f) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (wage.status == "DISPUTED") tr("மறுப்பு கோரப்பட்டது", "DISPUTED") else tr("பூட்டப்பட்டது", "LOCKED & VERIFIED"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (wage.status == "DISPUTED") SafetyCrimson else SafetyGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Work details & pieces
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = tr(wage.workTypeTa, wage.workTypeEn),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${wage.unitsCompleted} " + tr("எண்ணிக்கை", "units") + " @ ₹${wage.unitRate}/" + tr("ஒன்றுக்கு", "unit"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${"%.2f".format(wage.netAmount)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = IndustrialNavy
                    )
                    if (wage.deductions > 0) {
                        Text(
                            text = "-₹${"%.2f".format(wage.deductions)} " + tr("கழிவு", "ded."),
                            fontSize = 10.sp,
                            color = SafetyCrimson
                        )
                    }
                }
            }

            if (wage.status == "DISPUTED" && wage.disputeReason != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SafetyCrimson.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tr("மறுப்பு காரணம்: ", "Reason: ") + wage.disputeReason,
                        fontSize = 11.sp,
                        color = SafetyCrimson,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cryptographic Fingerprint Seals
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = IndustrialTeal, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SHA: ${wage.digitalSignatureHash.take(16)}...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialTeal
                        )
                    }
                    Text(
                        text = tr("தொழிலாளர் & மேற்பார்வையாளர் கைரேகை உறுதி", "Dual Biometric Confirmed"),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (wage.status != "DISPUTED") {
                    OutlinedButton(
                        onClick = onDisputeClick,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(44.dp).testTag("dispute_wage_btn_${wage.id}")
                    ) {
                        Text(text = tr("மறுப்பு / சர்ச்சை", "Dispute"), fontSize = 10.sp, color = SafetyCrimson)
                    }
                }
            }
        }
    }
}
