package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WageEntryEntity
import com.example.ui.theme.SafetyCrimson
import com.example.util.tr

@Composable
fun DisputeDialog(
    wageEntry: WageEntryEntity?,
    onDismiss: () -> Unit,
    onSubmitDispute: (WageEntryEntity, String) -> Unit
) {
    if (wageEntry == null) return

    val sampleReasons = listOf(
        tr("எண்ணிக்கை குறைவாக உள்ளது", "Piece count is lower than actual"),
        tr("தேவையற்ற கழிவு பிடித்தம்", "Incorrect deduction applied"),
        tr("ஊதிய விகிதம் குறைவு", "Piece rate mismatch"),
        tr("கூடுதல் நேரம் சேர்க்கப்படவில்லை", "Overtime not logged")
    )

    var selectedReason by remember { mutableStateOf(sampleReasons[0]) }
    var additionalNotes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("dispute_dialog_card"),
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
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SafetyCrimson,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = tr("ஊதிய மறுப்பு / சர்ச்சை பதிவு", "Wage Dispute Assistant"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = tr(
                        "தேதி: ${wageEntry.date} | பதிவு: ${wageEntry.workTypeTa} (₹${wageEntry.netAmount})",
                        "Entry: ${wageEntry.date} | ${wageEntry.workTypeEn} (₹${wageEntry.netAmount})"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = tr("காரணத்தை தேர்வு செய்யவும்:", "Select reason:"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                sampleReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = { selectedReason = reason },
                            label = { Text(text = reason, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = { Text(tr("கூடுதல் விவரம் (விருப்பப்பட்டால்)", "Additional details (Optional)")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dispute_notes_input"),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

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
                            val finalReason = if (additionalNotes.isNotBlank()) "$selectedReason: $additionalNotes" else selectedReason
                            onSubmitDispute(wageEntry, finalReason)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyCrimson),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_dispute_confirm_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(tr("சமர்ப்பி", "Submit Dispute"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
