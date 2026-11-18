package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Masks
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyGreen
import com.example.util.tr

data class ChemicalExposureItem(
    val nameEn: String,
    val nameTa: String,
    val currentLevelMgM3: Double,
    val maxPermissibleMgM3: Double,
    val hazardTypeEn: String,
    val hazardTypeTa: String
) {
    val percentage: Float
        get() = (currentLevelMgM3 / maxPermissibleMgM3).toFloat().coerceIn(0f, 1f)

    val isWarning: Boolean
        get() = percentage >= 0.5f

    val isDanger: Boolean
        get() = percentage >= 0.8f
}

/**
 * Screen component displaying worker chemical exposure levels, Permissible Exposure Limit (PEL)
 * progress bar visual, real-time chemical aerosol breakdown, and safety alerts.
 */
@Composable
fun ChemicalExposureMonitor(
    workerName: String,
    modifier: Modifier = Modifier,
    onTakeBreak: () -> Unit = {},
    onReportSpike: () -> Unit = {}
) {
    var expandedDetails by remember { mutableStateOf(false) }

    val chemicals = remember {
        listOf(
            ChemicalExposureItem(
                nameEn = "Aluminium Flake Dust (Fine Flash)",
                nameTa = "அலுமினியம் துகள் தூள்",
                currentLevelMgM3 = 1.35,
                maxPermissibleMgM3 = 5.00,
                hazardTypeEn = "Flammable Respiratory Dust",
                hazardTypeTa = "எரியக்கூடிய சுவாச தூசு"
            ),
            ChemicalExposureItem(
                nameEn = "Sulphur Particulate Aerosol",
                nameTa = "கந்தக நுண்தூள்",
                currentLevelMgM3 = 3.40,
                maxPermissibleMgM3 = 10.00,
                hazardTypeEn = "Acidic Irritant Dust",
                hazardTypeTa = "அமில சுவாச எரிச்சல்"
            ),
            ChemicalExposureItem(
                nameEn = "Potassium Nitrate (KNO3 Dust)",
                nameTa = "வெடியுப்பு தூள்",
                currentLevelMgM3 = 2.65,
                maxPermissibleMgM3 = 5.00,
                hazardTypeEn = "Oxidizer Particulate",
                hazardTypeTa = "ஆக்சிஜனேற்ற நுண்துகள்"
            ),
            ChemicalExposureItem(
                nameEn = "Barium Nitrate (Green Sparkler)",
                nameTa = "பேரியம் நைட்ரேட்",
                currentLevelMgM3 = 0.12,
                maxPermissibleMgM3 = 0.50,
                hazardTypeEn = "Toxic Heavy Metal Salt",
                hazardTypeTa = "நச்சு கன உலோக உப்பு"
            )
        )
    }

    // Average cumulative exposure percentage
    val overallExposure = chemicals.map { it.percentage }.average().toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = overallExposure,
        animationSpec = tween(durationMillis = 900),
        label = "exposure_progress"
    )

    val statusColor = when {
        overallExposure >= 0.8f -> SafetyCrimson
        overallExposure >= 0.5f -> SafetyAmber
        else -> SafetyGreen
    }

    val statusText = when {
        overallExposure >= 0.8f -> tr("ஆபத்தான வெளிப்பாடு (உடனடி ஓய்வு தேவை)", "CRITICAL EXPOSURE (Immediate Pause Required)", hi = "गंभीर जोखिम", ml = "അപകടകരമായ എക്സ്പോഷർ")
        overallExposure >= 0.5f -> tr("மிதமான அளவு (மாஸ்க் கட்டாயம்)", "MODERATE LOAD (Respirator Mandatory)", hi = "मध्यम जोखिम", ml = "മിതമായ അളവ്")
        else -> tr("பாதுகாப்பான அளவு (PEL வரம்பிற்குள்)", "SAFE EXPOSURE ZONE (Within PEL Limits)", hi = "सुरक्षित क्षेत्र", ml = "സുരക്ഷിത മേഖല")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chemical_exposure_monitor_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & OSHA / PESO Compliance Badge
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(IndustrialTeal.copy(alpha = 0.15f))
                            .border(1.dp, IndustrialTeal.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = "Air Exposure",
                            tint = IndustrialTeal,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = tr("ரசாயன வெளிப்பாடு அளவீடு", "Chemical Exposure Level", hi = "रासायनिक जोखिम स्तर", ml = "കെമിക്കൽ എക്സ്പോഷർ ലെവൽ", te = "రసాయన ఎక్స్పోజర్ స్థాయి", kn = "ರಾಸಾಯನಿಕ ಮಾನ್ಯತೆ ಮಟ್ಟ"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            fontSize = 15.sp
                        )
                        Text(
                            text = tr("8-மணி நேர ஷிப்ட் TWA கண்காணிப்பு", "8-Hour Shift TWA Air Exposure", hi = "8-घंटे शिफ्ट TWA निगरानी", ml = "8-മണിക്കൂർ ഷിഫ്റ്റ് TWA"),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${(overallExposure * 100).toInt()}% PEL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Visual Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${(overallExposure * 100).toInt()}% / 100%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(statusColor)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Threshold Scale Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0% (Clean)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "50% (Caution)", fontSize = 9.sp, color = SafetyAmber)
                    Text(text = "100% (PEL Max)", fontSize = 9.sp, color = SafetyCrimson)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ACTIVE SAFETY ALERTS FOR THIS WORKER
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Masks,
                            contentDescription = null,
                            tint = SafetyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("N95 / FFP2 மாஸ்க் உறுதிப்படுத்தப்பட்டது", "N95 Dust Mask Fitted & Verified", hi = "N95 डस्ट मास्क सक्रिय", ml = "N95 മാസ്ക് സജീവം"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = IndustrialTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("அறை R-101 காற்றோட்ட விசிறி இயங்குகிறது (450 CFM)", "Exhaust Ventilation Active (450 CFM)", hi = "निकास वेंटिलेशन चालू (450 CFM)", ml = "എക്സ്ഹോസ്റ്റ് വെന്റിലേഷൻ സജീവം"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("மருந்து அறை பணி நேரம்: 2 மணி 30 நிமிடம் (அதிகபட்சம் 4 மணி)", "Zone Time: 2h 30m / Max 4h recommended", hi = "क्षेत्र कार्य समय: 2 घंटे 30 मिनट"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Expandable Chemical-by-Chemical Dust & Aerosol Breakdown
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedDetails = !expandedDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr(
                        if (expandedDetails) "ரசாயன விபரங்களை மறைக்க" else "ரசாயன அளவீட்டு விபரங்களை பார்க்க",
                        if (expandedDetails) "Hide Chemical Breakdown" else "View Individual Chemical Breakdown",
                        hi = if (expandedDetails) "रासायनिक विवरण छुपाएं" else "प्रत्येक रसायन का स्तर देखें",
                        ml = if (expandedDetails) "വിശദാംശങ്ങൾ മറയ്ക്കുക" else "വ്യക്തിഗത അളവുകൾ കാണുക"
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialTeal
                )
                Text(
                    text = if (expandedDetails) "▲" else "▼",
                    fontSize = 11.sp,
                    color = IndustrialTeal
                )
            }

            if (expandedDetails) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    chemicals.forEach { item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (item.isDanger) SafetyCrimson.copy(alpha = 0.3f) else Color.Transparent
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tr(item.nameTa, item.nameEn),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = tr(item.hazardTypeTa, item.hazardTypeEn),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${item.currentLevelMgM3} / ${item.maxPermissibleMgM3} mg/m³",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isDanger) SafetyCrimson else if (item.isWarning) SafetyAmber else SafetyGreen
                                        )
                                        Text(
                                            text = "${(item.percentage * 100).toInt()}% PEL",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { item.percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (item.isDanger) SafetyCrimson else if (item.isWarning) SafetyAmber else SafetyGreen,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Take Fresh Air Break & Report Dust Incident
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTakeBreak,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("fresh_air_break_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = SafetyGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr("சுத்த காற்று ஓய்வு", "Fresh Air Break", hi = "विश्राम लें", ml = "വിശ്രമം"),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onReportSpike,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("report_chemical_dust_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CrisisAlert,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr("தூசு எச்சரிக்கை", "Report Dust Spike", hi = "धूल अलर्ट रिपोर्ट", ml = "അലേർട്ട് റിപ്പോർട്ട്"),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
