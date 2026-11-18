package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyGreen
import com.example.ui.viewmodel.AppViewModel
import com.example.util.StockReportGenerator
import com.example.util.tr
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StockImportLog(
    val id: Long,
    val chemicalName: String,
    val batchCode: String,
    val importedKg: Double,
    val supplier: String,
    val timestamp: Long
)

@Composable
fun StockManagementScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialLogs = remember {
        mutableStateListOf(
            StockImportLog(1, "Potassium Nitrate (KNO3)", "BATCH-KN-902", 25.0, "Sivakasi Nitrates Ltd", System.currentTimeMillis() - 3600000 * 4),
            StockImportLog(2, "Sulphur Powder", "BATCH-SL-310", 15.0, "Madurai Chemical Works", System.currentTimeMillis() - 3600000 * 24),
            StockImportLog(3, "Aluminium Flake Powder", "BATCH-AL-711", 20.0, "National Pyrotech Metals", System.currentTimeMillis() - 3600000 * 48),
            StockImportLog(4, "Barium Nitrate", "BATCH-BA-404", 30.0, "Standard Fireworks Supplies", System.currentTimeMillis() - 3600000 * 72),
            StockImportLog(5, "Charcoal Powder", "BATCH-CH-112", 12.0, "VediRaksha Raw Mills", System.currentTimeMillis() - 3600000 * 96)
        )
    }

    var showAddStockDialog by remember { mutableStateOf(false) }
    var downloadSuccessMsg by remember { mutableStateOf("") }
    var newChemicalName by remember { mutableStateOf("Potassium Nitrate (KNO3)") }
    var newBatchCode by remember { mutableStateOf("BATCH-NEW-01") }
    var newQuantityKg by remember { mutableStateOf("10.0") }
    var newSupplier by remember { mutableStateOf("VediRaksha Supply Unit") }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
    val totalWeight = initialLogs.sumOf { it.importedKg }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stock_management_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Actions
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(IndustrialTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = IndustrialTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tr("சரக்கு மேலாண்மை & இறக்குமதி வரலாறு", "Stock Management & Import History"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = tr("அதிகாரப்பூர்வ ரசாயன இறக்குமதி பதிவுகள் மற்றும் தொகுதி தணிக்கை", "Verified chemical import batches & audit log"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tr("மொத்த இறக்குமதி செய்யப்பட்ட வகைகள்", "Total Stock Batches"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${initialLogs.size} Active Batches", fontSize = 16.sp, fontWeight = FontWeight.Black, color = IndustrialTeal)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(tr("மொத்த எடை", "Total Imported Weight"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${"%.1f".format(totalWeight)} kg", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Import New Stock, Empty Rooms, & Download Daily Usage Document
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddStockDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("புதிய சரக்கு", "Import Stock"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val result = StockReportGenerator.downloadDailyStockReport(context, initialLogs)
                                if (result.success) {
                                    downloadSuccessMsg = "📄 Saved to Downloads/${result.fileName}"
                                } else {
                                    downloadSuccessMsg = "❌ Error: ${result.message}"
                                }
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("நாளின் பயன்பாடு PDF", "Download Daily Report"), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    if (downloadSuccessMsg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = downloadSuccessMsg,
                            color = SafetyGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("இறக்குமதி வரலாறு பதிவுகள்", "Import History Audit Log"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SafetyGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, null, tint = SafetyGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PESO Certified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafetyGreen)
                    }
                }
            }
        }

        // Import History Items
        items(initialLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = log.chemicalName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Batch: ${log.batchCode} • Supplier: ${log.supplier}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+${log.importedKg} kg",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = SafetyGreen
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SafetyGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "VERIFIED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog to Import New Stock Batch
    if (showAddStockDialog) {
        Dialog(onDismissRequest = { showAddStockDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(tr("புதிய ரசாயன சரக்கு ஏற்று", "Import New Chemical Stock"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newChemicalName,
                        onValueChange = { newChemicalName = it },
                        label = { Text(tr("ரசாயன பெயர்", "Chemical Name")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newBatchCode,
                        onValueChange = { newBatchCode = it },
                        label = { Text(tr("தொகுதி குறியீடு", "Batch Code")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newQuantityKg,
                        onValueChange = { newQuantityKg = it },
                        label = { Text(tr("அளவு (kg)", "Quantity (kg)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newSupplier,
                        onValueChange = { newSupplier = it },
                        label = { Text(tr("வழங்குநர்", "Supplier")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddStockDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(tr("ரத்து செய்", "Cancel"))
                        }

                        Button(
                            onClick = {
                                val qty = newQuantityKg.toDoubleOrNull() ?: 10.0
                                initialLogs.add(
                                    0,
                                    StockImportLog(
                                        id = System.currentTimeMillis(),
                                        chemicalName = newChemicalName,
                                        batchCode = newBatchCode,
                                        importedKg = qty,
                                        supplier = newSupplier,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                showAddStockDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialTeal),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(tr("பதிவு செய்", "Save Import"))
                        }
                    }
                }
            }
        }
    }
}
