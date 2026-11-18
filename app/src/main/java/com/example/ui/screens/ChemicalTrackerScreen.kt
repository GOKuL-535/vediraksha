package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ChemicalAllocationEntity
import com.example.data.model.ChemicalEntryEntity
import com.example.data.model.ChemicalStockEntity
import com.example.data.model.CrackerRecipeCatalog
import com.example.data.model.CrackerStockEntity
import com.example.data.model.RoomEntity
import com.example.ui.components.ChemicalExposureMonitor
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyCrimson
import com.example.ui.theme.SafetyFlame
import com.example.ui.theme.SafetyGreen
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.ChemicalRequirementSummary
import com.example.ui.viewmodel.CrackerRequirementBreakdown
import com.example.ui.viewmodel.RoomAllocationSummary
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.tr
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ChemicalSectionTab(val titleTa: String, val titleEn: String) {
    PLANNING("வெடி இலக்கு & ரசாயனம்", "Crackers & Recipes"),
    ALLOCATION("அறை ஒதுக்கீடு", "Chemical Allocation"),
    ROOMS("அறைகள் & PESO அளவு", "Room Module & Meters"),
    SCAN_LOGS("QR பதிவு & வரலாறு", "QR Scan & Logs")
}

data class ChemicalPreset(
    val nameTa: String,
    val nameEn: String,
    val batchCode: String,
    val defaultKg: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChemicalTrackerScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val rooms by viewModel.rooms.collectAsState()
    val entries by viewModel.chemicalEntries.collectAsState()
    val isSupervisor by viewModel.isSupervisor.collectAsState()

    val crackerStocks by viewModel.crackerStocks.collectAsState()
    val chemicalStocks by viewModel.chemicalStocks.collectAsState()
    val chemicalAllocations by viewModel.chemicalAllocations.collectAsState()
    val chemicalRequirements by viewModel.chemicalRequirements.collectAsState()
    val crackerBreakdowns by viewModel.crackerBreakdowns.collectAsState()
    val remainingUnallocated by viewModel.remainingUnallocatedPerChemical.collectAsState()
    val roomAllocations by viewModel.roomAllocations.collectAsState()

    var activeTab by remember { mutableStateOf(ChemicalSectionTab.PLANNING) }

    // Dialog States
    var showScanDialog by remember { mutableStateOf(false) }
    var selectedRoomForScan by remember { mutableStateOf<RoomEntity?>(null) }

    var showManualCrackerDialog by remember { mutableStateOf(false) }
    var showBulkUploadDialog by remember { mutableStateOf(false) }
    var editingCrackerStock by remember { mutableStateOf<CrackerStockEntity?>(null) }

    var showAllocateDialog by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var roomToDelete by remember { mutableStateOf<RoomEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("chemical_tracker_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Supervisor Role & Mode Header Bar (Feature 5)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSupervisor) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                .background(if (isSupervisor) MaterialTheme.colorScheme.primary else IndustrialTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupervisorAccount,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isSupervisor) tr("மேற்பார்வையாளர் பயன்முறை", "SUPERVISOR ROLE")
                                    else tr("பணியாளர் பயன்முறை", "WORKER ROLE"),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSupervisor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isSupervisor) SafetyGreen.copy(alpha = 0.2f) else SafetyAmber.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isSupervisor) tr("அங்கீகரிக்கப்பட்டது", "AUTHORIZED")
                                        else tr("பார்வை மட்டும்", "READ-ONLY"),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSupervisor) SafetyGreen else SafetyAmber,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isSupervisor) tr("இலக்கு நிர்ணயம், ஒதுக்கீடு & அறை உருவாக்கம் இயங்குகிறது", "Stock upload, allocation & room management active")
                                else tr("மாற்றங்களைச் செய்ய Supervisor பயன்முறைக்கு மாறவும்", "Switch to Supervisor role to edit stock or allocate"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Role switch toggle for testing worker restriction
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isSupervisor,
                            onCheckedChange = { viewModel.toggleSupervisorRole() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("supervisor_role_toggle")
                        )
                    }
                }
            }
        }

        // 2. Navigation Section Filter Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ChemicalSectionTab.entries) { tab ->
                    val isSelected = activeTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeTab = tab },
                        label = {
                            Text(
                                text = tr(tab.titleTa, tab.titleEn),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            when (tab) {
                                ChemicalSectionTab.PLANNING -> Icon(Icons.Default.Inventory, null, Modifier.size(16.dp))
                                ChemicalSectionTab.ALLOCATION -> Icon(Icons.Default.Science, null, Modifier.size(16.dp))
                                ChemicalSectionTab.ROOMS -> Icon(Icons.Default.MeetingRoom, null, Modifier.size(16.dp))
                                ChemicalSectionTab.SCAN_LOGS -> Icon(Icons.Default.QrCodeScanner, null, Modifier.size(16.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }

        // =========================================================================
        // SECTION 1: CRACKER STOCK UPLOAD & CHEMICAL REQUIREMENT CALCULATION (Features 1 & 2)
        // =========================================================================
        if (activeTab == ChemicalSectionTab.PLANNING) {
            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (viewModel.checkSupervisorPermission()) {
                                showManualCrackerDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("manual_cracker_entry_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("+ வெடி வகை சேர்க்க", "+ Add Cracker"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (viewModel.checkSupervisorPermission()) {
                                showBulkUploadDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("bulk_upload_csv_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("CSV / Excel பதிவேற்றம்", "Bulk Upload"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Feature 1: Cracker Target Stocks Table
            item {
                CrackerStockTableCard(
                    stocks = crackerStocks,
                    onIncrease = { stock, delta ->
                        viewModel.increaseCrackerQuantity(stock.id, stock.targetQuantity, delta)
                    },
                    onEdit = { stock ->
                        if (viewModel.checkSupervisorPermission()) {
                            editingCrackerStock = stock
                        }
                    },
                    onDelete = { stock ->
                        if (viewModel.checkSupervisorPermission()) {
                            viewModel.deleteCrackerStock(stock.id)
                        }
                    }
                )
            }

            // Feature 2: Combined Chemical Requirements & Shortage Flagging
            item {
                CombinedChemicalRequirementsCard(requirements = chemicalRequirements)
            }

            // Feature 2: Per-Cracker Breakdown Card
            item {
                PerCrackerBreakdownCard(breakdowns = crackerBreakdowns)
            }
        }

        // =========================================================================
        // SECTION 2: CHEMICAL-TO-ROOM ALLOCATION (Feature 3)
        // =========================================================================
        if (activeTab == ChemicalSectionTab.ALLOCATION) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr("ரசாயன அறை ஒதுக்கீடு", "Chemical-to-Room Allocation"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = tr("கிடங்கிலிருந்து குறிப்பிட்ட அறைக்கு ரசாயனங்களை ஒதுக்கவும்", "Distribute warehouse chemicals to rooms safely"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    if (viewModel.checkSupervisorPermission()) {
                                        showAllocateDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("open_allocate_chemical_btn")
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tr("ஒதுக்கீடு செய்", "Allocate"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Unallocated Warehouse Stock Summary
            item {
                UnallocatedChemicalsCard(
                    chemicalStocks = chemicalStocks,
                    remainingUnallocated = remainingUnallocated
                )
            }

            // Room Allocations List
            item {
                Text(
                    text = tr("அறைகள் வாரியாக ஒதுக்கப்பட்ட ரசாயனங்கள்", "Allocated Chemicals by Room"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (chemicalAllocations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = tr("தற்போது எந்த ரசாயன ஒதுக்கீடும் இல்லை. 'ஒதுக்கீடு செய்' பொத்தானை அழுத்தவும்.", "No active room allocations. Click 'Allocate' above to distribute chemicals."),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(chemicalAllocations) { alloc ->
                    ChemicalAllocationItemCard(
                        allocation = alloc,
                        onDelete = {
                            if (viewModel.checkSupervisorPermission()) {
                                viewModel.deleteAllocation(alloc)
                            }
                        }
                    )
                }
            }
        }

        // =========================================================================
        // SECTION 3: ROOM MODULE & REAL-TIME LOAD METERS (Feature 4)
        // =========================================================================
        if (activeTab == ChemicalSectionTab.ROOMS) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("தொழிற்சாலை அறைகள்", "Factory Production Rooms"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = tr("PESO உரிமம் பெற்ற வரம்புகள் & கண்காணிப்பு", "PESO licensed capacities & live safety meters"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (viewModel.checkSupervisorPermission()) {
                                showCreateRoomDialog = true
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE4570B)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("create_new_room_btn")
                    ) {
                        Icon(Icons.Default.AddBusiness, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("புதிய அறை", "+ Create Room"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Room Cards with Delete Option
            items(rooms) { room ->
                ChemicalRoomCardWithManagement(
                    room = room,
                    onAddMaterial = {
                        selectedRoomForScan = room
                        showScanDialog = true
                    },
                    onDeleteRoom = {
                        if (viewModel.checkSupervisorPermission()) {
                            roomToDelete = room
                        }
                    }
                )
            }

            // Personal Exposure Monitor
            item {
                ChemicalExposureMonitor(
                    workerName = viewModel.workerName,
                    onTakeBreak = {
                        viewModel.requireBiometricAuth(
                            title = tr("சுத்தமான காற்று இடைவேளை", "Fresh Air Recovery Break"),
                            subtitle = tr("15 நிமிட திட்டமிடப்பட்ட மீட்பு இடைவேளை", "15-minute scheduled recovery pause")
                        ) {}
                    },
                    onReportSpike = {
                        viewModel.requireBiometricAuth(
                            title = tr("தூசி / ரசாயன அதிகரிப்பு அறிக்கை", "Report Chemical Dust Spike"),
                            subtitle = tr("மேற்பார்வையாளருக்கு எச்சரிக்கை அனுப்பப்படுகிறது", "Dispatching alert to supervisor")
                        ) {}
                    }
                )
            }
        }

        // =========================================================================
        // SECTION 4: SCAN & AUDIT HISTORY
        // =========================================================================
        if (activeTab == ChemicalSectionTab.SCAN_LOGS) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tr("ரசாயன QR ஸ்கேனர் & இடமாற்றம்", "Chemical QR Code Scan / Transfer"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = tr("டிஜிட்டல் முத்திரையுடன் கூடிய நேரடி இடமாற்ற பதிவு", "Tamper-proof SHA verification & PESO enforcement"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                selectedRoomForScan = rooms.firstOrNull()
                                showScanDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("scan_chemical_qr_btn")
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr("கண்டெய்னர் QR ஸ்கேன் செய் / இடமாற்று", "Scan Chemical QR / Transfer"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = tr("சமீபத்திய ரசாயன இடமாற்றங்கள் & SHA முத்திரைகள்", "Recent Chemical Transfers & Tamper Seals"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(entries.take(10)) { entry ->
                ChemicalEntryHistoryCard(entry = entry)
            }
        }
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================

    // 1. Manual Cracker Stock Dialog
    if (showManualCrackerDialog) {
        ManualCrackerEntryDialog(
            existingCracker = null,
            onDismiss = { showManualCrackerDialog = false },
            onSubmit = { ta, en, qty ->
                viewModel.addOrUpdateCrackerStock(ta, en, qty)
                showManualCrackerDialog = false
            }
        )
    }

    if (editingCrackerStock != null) {
        ManualCrackerEntryDialog(
            existingCracker = editingCrackerStock,
            onDismiss = { editingCrackerStock = null },
            onSubmit = { ta, en, qty ->
                viewModel.addOrUpdateCrackerStock(ta, en, qty)
                editingCrackerStock = null
            }
        )
    }

    // 2. Bulk CSV Upload Dialog
    if (showBulkUploadDialog) {
        BulkCsvUploadDialog(
            onDismiss = { showBulkUploadDialog = false },
            onUpload = { csvText ->
                viewModel.bulkUploadCrackerStocks(csvText)
                showBulkUploadDialog = false
            }
        )
    }

    // 3. Chemical-to-Room Allocation Dialog
    if (showAllocateDialog) {
        AllocateChemicalDialog(
            rooms = rooms,
            chemicalStocks = chemicalStocks,
            remainingUnallocated = remainingUnallocated,
            onDismiss = { showAllocateDialog = false },
            onConfirm = { chemTa, chemEn, roomIds, qtyPerRoom ->
                viewModel.allocateChemicalToRooms(
                    chemicalNameTa = chemTa,
                    chemicalNameEn = chemEn,
                    roomIds = roomIds,
                    quantityPerRoomKg = qtyPerRoom,
                    onSuccess = { showAllocateDialog = false }
                )
            }
        )
    }

    // 4. Create Room Dialog (Feature 4)
    if (showCreateRoomDialog) {
        CreateRoomDialog(
            existingRooms = rooms,
            onDismiss = { showCreateRoomDialog = false },
            onCreate = { number, nameTa, nameEn, capacity, hazard ->
                viewModel.createRoom(
                    roomNumber = number,
                    nameTa = nameTa,
                    nameEn = nameEn,
                    maxCapacityKg = capacity,
                    hazardCategory = hazard,
                    onSuccess = { showCreateRoomDialog = false }
                )
            }
        )
    }

    // 5. Delete Room Dialog with Warning & Return Option (Feature 4)
    roomToDelete?.let { room ->
        DeleteRoomDialog(
            room = room,
            onDismiss = { roomToDelete = null },
            onConfirmDelete = { returnStock ->
                viewModel.deleteRoom(
                    roomId = room.id,
                    returnChemicalsToStock = returnStock,
                    onSuccess = { roomToDelete = null }
                )
            }
        )
    }

    // 6. QR Scanner Dialog
    if (showScanDialog) {
        ChemicalScanDialog(
            rooms = rooms,
            initialRoom = selectedRoomForScan ?: rooms.firstOrNull(),
            onDismiss = { showScanDialog = false },
            onSubmit = { roomId, nameTa, nameEn, batch, quantity ->
                viewModel.addChemicalMaterial(
                    roomId = roomId,
                    chemicalNameTa = nameTa,
                    chemicalNameEn = nameEn,
                    batchCode = batch,
                    quantityKg = quantity,
                    directCommit = true
                )
                showScanDialog = false
            }
        )
    }
}

// =========================================================================
// CRACKER STOCK TABLE & SUMMARY COMPONENTS (Features 1 & 2)
// =========================================================================

@Composable
fun CrackerStockTableCard(
    stocks: List<CrackerStockEntity>,
    onIncrease: (CrackerStockEntity, Int) -> Unit,
    onEdit: (CrackerStockEntity) -> Unit,
    onDelete: (CrackerStockEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("வெடி இலக்கு இருப்பு பட்டியல்", "Cracker Target Stock Table"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${stocks.size} ${tr("வகைகள்", "types")}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (stocks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tr("வெடி இருப்பு இலக்குகள் எதுவும் இல்லை. புதியதைச் சேர்க்கவும்.", "No cracker targets entered. Click '+ Add Cracker' to start."),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Table Headers
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tr("வெடி வகை", "Cracker Type"), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text(tr("இலக்கு அளவு", "Target Qty"), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        Text(tr("விரைவு மாற்றம்", "Quick +"), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                        Text(tr("செயல்கள்", "Actions"), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Table Rows
                stocks.forEach { stock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = stock.crackerNameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = stock.crackerNameTa,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "%,d".format(stock.targetQuantity),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1.2f)
                        )

                        Row(
                            modifier = Modifier.weight(1.3f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable { onIncrease(stock, 500) }
                            ) {
                                Text("+500", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable { onIncrease(stock, 1000) }
                            ) {
                                Text("+1K", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }

                        Row(
                            modifier = Modifier.weight(0.9f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { onEdit(stock) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(15.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { onDelete(stock) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SafetyCrimson, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
fun CombinedChemicalRequirementsCard(requirements: List<ChemicalRequirementSummary>) {
    val hasAnyShortage = requirements.any { it.isShortage }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasAnyShortage) SafetyCrimson.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (hasAnyShortage) androidx.compose.foundation.BorderStroke(1.5.dp, SafetyCrimson.copy(alpha = 0.4f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tr("மொத்த ரசாயன தேவைகள் (Combined)", "Total Chemical Requirements"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = tr("வெடி இலக்குகளின் அடிப்படையில் தானியங்கி கணக்கீடு", "Auto-calculated across all cracker targets"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hasAnyShortage) {
                    Surface(
                        color = SafetyCrimson.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = SafetyCrimson, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr("இருப்பு பற்றாக்குறை!", "SHORTAGE FLAGGED"),
                                color = SafetyCrimson,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            requirements.forEach { req ->
                Surface(
                    color = if (req.isShortage) SafetyCrimson.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = req.chemicalNameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = req.chemicalNameTa,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "${tr("தேவை: ", "Need: ")}${"%.1f".format(req.totalRequiredKg)} kg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tr("இருப்பு: ", "Stock: ")}${"%.1f".format(req.availableStockKg)} kg",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = if (req.isShortage) SafetyCrimson.copy(alpha = 0.2f) else SafetyGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (req.isShortage) {
                                    "-${"%.1f".format(req.shortageKg)} kg ⚠️"
                                } else {
                                    "OK ✓"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (req.isShortage) SafetyCrimson else SafetyGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerCrackerBreakdownCard(breakdowns: List<CrackerRequirementBreakdown>) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tr("வெடி வாரியான ரசாயன செய்முறை விவரம்", "Per-Cracker Recipe Breakdown"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = tr("ஒவ்வொரு வெடி வகைக்கும் தேவையான ரசாயனம்", "Chemical formulas per cracker type"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    breakdowns.forEach { breakdown ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${breakdown.crackerNameEn} (${breakdown.crackerNameTa})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${"%,d".format(breakdown.targetQuantity)} ${tr("எண்ணிக்கை", "units")}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                breakdown.chemicals.forEach { chem ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "• ${chem.chemicalNameEn}", fontSize = 11.sp)
                                        Text(
                                            text = "${"%.2f".format(chem.requiredKg)} kg",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
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
}

// =========================================================================
// CHEMICAL ALLOCATION COMPONENTS (Feature 3)
// =========================================================================

@Composable
fun UnallocatedChemicalsCard(
    chemicalStocks: List<ChemicalStockEntity>,
    remainingUnallocated: Map<String, Double>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = tr("கிடங்கு இருப்பு & ஒதுக்கப்படாத அளவு", "Warehouse Stock & Unallocated Balance"),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            chemicalStocks.forEach { stock ->
                val remaining = remainingUnallocated[stock.chemicalNameEn] ?: stock.availableStockKg
                val allocated = (stock.availableStockKg - remaining).coerceAtLeast(0.0)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(text = stock.chemicalNameEn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        if (LanguageManager.currentLanguage != AppLanguage.ENGLISH) {
                            Text(text = stock.chemicalNameTa, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${tr("ஒதுக்கப்படாதது: ", "Unallocated: ")}${"%.1f".format(remaining)} kg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialTeal
                        )
                        Text(
                            text = "${tr("மொத்தம்: ", "Total: ")}${"%.1f".format(stock.availableStockKg)} kg | ${tr("ஒதுக்கப்பட்டது: ", "Alloc: ")}${"%.1f".format(allocated)} kg",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChemicalAllocationItemCard(
    allocation: ChemicalAllocationEntity,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
    val timeStr = dateFormat.format(Date(allocation.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = allocation.roomNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = allocation.chemicalNameEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${"%.1f".format(allocation.allocatedKg)} kg • ${allocation.allocatedBy}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeStr,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Allocation",
                    tint = SafetyCrimson,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// =========================================================================
// ROOM CARD WITH MANAGEMENT (Feature 4)
// =========================================================================

@Composable
fun ChemicalRoomCardWithManagement(
    room: RoomEntity,
    onAddMaterial: () -> Unit,
    onDeleteRoom: () -> Unit
) {
    val barColor = when {
        room.isDanger -> SafetyCrimson
        room.isWarning -> SafetyAmber
        else -> SafetyGreen
    }

    var taskStatus by remember { mutableStateOf("NOT_STARTED") } // "NOT_STARTED", "IN_PROGRESS", "FINISHED"
    var showChemSelectDialog by remember { mutableStateOf(false) }
    var selectedChemical by remember { mutableStateOf("Potassium Nitrate (KNO3)") }
    var taskQuantityKg by remember { mutableStateOf("10.0") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chemical_room_${room.roomNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = room.roomNumber,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr(room.nameTa, room.nameEn),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "${tr("அறை: ", "Room: ")}${room.roomNumber} • ${tr("பணி: ", "Type: ")}${room.hazardCategory}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Task Status Badge
                    Surface(
                        color = when (taskStatus) {
                            "IN_PROGRESS" -> IndustrialTeal.copy(alpha = 0.15f)
                            "FINISHED" -> SafetyGreen.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (taskStatus) {
                                "IN_PROGRESS" -> "Task: In Progress 🟢"
                                "FINISHED" -> "Task: Finished ✓"
                                else -> "Task: Not Started"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (taskStatus) {
                                "IN_PROGRESS" -> IndustrialTeal
                                "FINISHED" -> SafetyGreen
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDeleteRoom,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Room",
                            tint = SafetyCrimson.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Limit bar & statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tr(
                        "சுமை: ${"%.1f".format(room.currentTotalKg)} kg / வரம்பு: ${"%.0f".format(room.maxCapacityKg)} kg",
                        "Load: ${"%.1f".format(room.currentTotalKg)} kg / Limit: ${"%.0f".format(room.maxCapacityKg)} kg"
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${room.fillPercentage.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (room.fillPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Task Verification Buttons: Start Task vs Finish Task & Empty Room
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (taskStatus != "IN_PROGRESS") {
                    Button(
                        onClick = { showChemSelectDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = tr("பணி தொடங்கு", "▶ Start Task"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            taskStatus = "FINISHED"
                            // Room load emptied & noted in stock management log
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyCrimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = tr("பணி முடித்து அறை காலி செய்", "⏹ Finish & Empty Room"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onAddMaterial,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE4570B)),
                    border = BorderStroke(1.5.dp, Color(0xFFE4570B))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = tr("சரக்கு ஏற்று", "+ Add Load"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Chemical Selection & Allocation Dialog upon Start Task
    if (showChemSelectDialog) {
        Dialog(onDismissRequest = { showChemSelectDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = tr("அறைக்கு ரசாயனம் தேர்ந்தெடு & பணி தொடங்கு", "Select Chemicals & Start Task for ${room.roomNumber}"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(tr("தேர்ந்தெடுக்கப்பட்ட ரசாயனம்:", "Select Chemical Material:"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val availableChems = listOf(
                        "Potassium Nitrate (KNO3)",
                        "Sulphur Powder",
                        "Aluminium Flake Powder",
                        "Barium Nitrate",
                        "Charcoal Powder"
                    )

                    availableChems.forEach { chem ->
                        val isSelected = selectedChemical == chem
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedChemical = chem },
                            color = if (isSelected) IndustrialTeal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Science, null, tint = IndustrialTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(chem, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = taskQuantityKg,
                        onValueChange = { taskQuantityKg = it },
                        label = { Text(tr("ஒதுக்க வேண்டிய அளவு (kg)", "Allocated Quantity (kg)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showChemSelectDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(tr("ரத்து செய்", "Cancel"))
                        }

                        Button(
                            onClick = {
                                taskStatus = "IN_PROGRESS"
                                showChemSelectDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialTeal),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(tr("பணி தொடங்கு", "Start Task"))
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// DIALOG IMPLEMENTATIONS
// =========================================================================

@Composable
fun ManualCrackerEntryDialog(
    existingCracker: CrackerStockEntity?,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Int) -> Unit
) {
    val presets = CrackerRecipeCatalog.standardCrackerTypes
    var selectedPresetIndex by remember { mutableStateOf(0) }
    var isCustom by remember { mutableStateOf(false) }

    var customNameEn by remember { mutableStateOf(existingCracker?.crackerNameEn ?: "") }
    var customNameTa by remember { mutableStateOf(existingCracker?.crackerNameTa ?: "") }
    var quantityInput by remember { mutableStateOf(existingCracker?.targetQuantity?.toString() ?: "5000") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (existingCracker != null) tr("வெடி இலக்கு திருத்துக", "Edit Cracker Target")
                    else tr("வெடி வகை & இலக்கு சேர்க்க", "Add Cracker Target Stock"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (existingCracker == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isCustom,
                            onClick = { isCustom = false },
                            label = { Text(tr("நிலையான வகை", "Standard Types"), fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = isCustom,
                            onClick = { isCustom = true },
                            label = { Text(tr("தனிப்பயன்", "Custom Name"), fontSize = 12.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isCustom) {
                        Text(tr("வெடி வகையைத் தேர்ந்தெடுக்கவும்:", "Select Cracker Type:"), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(presets.indices.toList()) { idx ->
                                val pair = presets[idx]
                                FilterChip(
                                    selected = selectedPresetIndex == idx,
                                    onClick = { selectedPresetIndex = idx },
                                    label = { Text(pair.second, fontSize = 11.sp) }
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = customNameEn,
                            onValueChange = { customNameEn = it },
                            label = { Text(tr("வெடி பெயர் (English)", "Cracker Name (EN)")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customNameTa,
                            onValueChange = { customNameTa = it },
                            label = { Text(tr("வெடி பெயர் (தமிழ்)", "Cracker Name (TA)")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = "${existingCracker.crackerNameEn} (${existingCracker.crackerNameTa})",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = {
                        quantityInput = it
                        errorMessage = null
                    },
                    label = { Text(tr("இலக்கு எண்ணிக்கை (Target Quantity)", "Target Quantity (units)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMessage!!, color = SafetyCrimson, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(tr("ரத்து", "Cancel"))
                    }
                    Button(
                        onClick = {
                            val qty = quantityInput.toIntOrNull()
                            if (qty == null || qty < 0) {
                                errorMessage = tr("சரியான எண்ணிக்கையை உள்ளிடவும் (0 அல்லது அதற்கு மேல்)", "Please enter a valid non-negative quantity.")
                                return@Button
                            }

                            val (finalTa, finalEn) = if (existingCracker != null) {
                                Pair(existingCracker.crackerNameTa, existingCracker.crackerNameEn)
                            } else if (!isCustom) {
                                presets[selectedPresetIndex]
                            } else {
                                if (customNameEn.isBlank()) {
                                    errorMessage = tr("வெடி பெயரை உள்ளிடவும்", "Cracker name cannot be empty.")
                                    return@Button
                                }
                                Pair(customNameTa.ifBlank { customNameEn }, customNameEn)
                            }

                            onSubmit(finalTa, finalEn, qty)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(tr("சேமி", "Save"))
                    }
                }
            }
        }
    }
}

@Composable
fun BulkCsvUploadDialog(
    onDismiss: () -> Unit,
    onUpload: (String) -> Unit
) {
    val sampleTemplate = "Sparklers, 15000\nFlower Pots, 8000\nRockets, 5000\nGround Chakras, 12000\nSound Bombs, 7500"
    var csvText by remember { mutableStateOf(sampleTemplate) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = tr("CSV / Excel மொத்த பதிவேற்றம்", "Bulk Cracker Stock Upload"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = tr("வடிவம்: வெடி_பெயர், இலக்கு_எண்ணிக்கை", "Format: CrackerName, TargetQuantity (comma, tab or semicolon)"),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    label = { Text("CSV / Text Data") },
                    textStyle = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { csvText = sampleTemplate },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(tr("மாதிரி டெம்ப்ளேட்டை நிரப்பு", "Load Sample Sivakasi Batch Template"), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(tr("ரத்து", "Cancel"))
                    }
                    Button(
                        onClick = { onUpload(csvText) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(tr("இறக்குமதி செய்", "Import & Calculate"))
                    }
                }
            }
        }
    }
}

@Composable
fun AllocateChemicalDialog(
    rooms: List<RoomEntity>,
    chemicalStocks: List<ChemicalStockEntity>,
    remainingUnallocated: Map<String, Double>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<Long>, Double) -> Unit
) {
    var selectedChemIndex by remember { mutableStateOf(0) }
    val selectedChem = chemicalStocks.getOrNull(selectedChemIndex) ?: chemicalStocks.firstOrNull()

    var selectAllRooms by remember { mutableStateOf(false) }
    val selectedRoomIds = remember { mutableStateOf(setOf<Long>()) }

    var quantityInput by remember { mutableStateOf("10.0") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val unallocatedAvailable = selectedChem?.let { remainingUnallocated[it.chemicalNameEn] ?: it.availableStockKg } ?: 0.0
    val parsedQty = quantityInput.toDoubleOrNull() ?: 0.0
    val effectiveRoomIds = if (selectAllRooms) rooms.map { it.id }.toSet() else selectedRoomIds.value
    val totalToAllocate = parsedQty * effectiveRoomIds.size

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = tr("ரசாயனம் ஒதுக்குதல் (Allocate to Rooms)", "Allocate Chemical to Rooms"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Select Chemical
                Text(tr("ரசாயனத்தைத் தேர்ந்தெடுக்கவும்:", "Select Chemical:"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(chemicalStocks.indices.toList()) { idx ->
                        val chem = chemicalStocks[idx]
                        FilterChip(
                            selected = selectedChemIndex == idx,
                            onClick = { selectedChemIndex = idx },
                            label = { Text(chem.chemicalNameEn.split(" ")[0], fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                selectedChem?.let { chem ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(chem.chemicalNameEn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${tr("கிடைப்பது: ", "Available: ")}${"%.1f".format(unallocatedAvailable)} kg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = IndustrialTeal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity per Room
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = {
                        quantityInput = it
                        validationError = null
                    },
                    label = { Text(tr("ஒவ்வொரு அறைக்கும் அளவு (kg)", "Quantity per Room (kg)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Room Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tr("இலக்கு அறைகள்:", "Target Room(s):"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectAllRooms,
                            onCheckedChange = { selectAllRooms = it }
                        )
                        Text(tr("அனைத்து அறைகளும்", "All Rooms"), fontSize = 11.sp)
                    }
                }

                if (!selectAllRooms) {
                    rooms.forEach { room ->
                        val isChecked = selectedRoomIds.value.contains(room.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val current = selectedRoomIds.value.toMutableSet()
                                    if (isChecked) current.remove(room.id) else current.add(room.id)
                                    selectedRoomIds.value = current
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    val current = selectedRoomIds.value.toMutableSet()
                                    if (it) current.add(room.id) else current.remove(room.id)
                                    selectedRoomIds.value = current
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${room.roomNumber} (${room.nameEn}) - இடவசதி: ${"%.1f".format((room.maxCapacityKg - room.currentTotalKg).coerceAtLeast(0.0))} kg",
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Summary Calculation Preview
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tr("மொத்த ஒதுக்கீடு:", "Total To Allocate:"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${"%.1f".format(totalToAllocate)} kg (${effectiveRoomIds.size} ${tr("அறை", "rooms")})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalToAllocate > unallocatedAvailable) SafetyCrimson else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = validationError!!, color = SafetyCrimson, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(tr("ரத்து", "Cancel"))
                    }
                    Button(
                        onClick = {
                            if (effectiveRoomIds.isEmpty()) {
                                validationError = tr("தயவுசெய்து குறைந்தது ஒரு அறையைத் தேர்ந்தெடுக்கவும்", "Please select at least one room.")
                                return@Button
                            }
                            if (parsedQty <= 0.0) {
                                validationError = tr("0-க்கு அதிகமான சரியான எடையை உள்ளிடவும்", "Quantity must be greater than 0 kg.")
                                return@Button
                            }
                            if (totalToAllocate > unallocatedAvailable) {
                                validationError = tr("கிடைக்கும் இருப்பை விட கூடுதல் ஒதுக்கீடு செய்ய முடியாது! (இருப்பு: ${"%.1f".format(unallocatedAvailable)} kg)", "Over-allocation blocked! Exceeds available stock (${"%.1f".format(unallocatedAvailable)} kg).")
                                return@Button
                            }
                            showConfirmationDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(tr("சரிபார் & சேமி", "Confirm & Save"))
                    }
                }
            }
        }
    }

    if (showConfirmationDialog && selectedChem != null) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(tr("ஒதுக்கீட்டை உறுதிப்படுத்துக", "Confirm Allocation")) },
            text = {
                Text(
                    tr(
                        "${selectedChem.chemicalNameEn} ரசாயனத்தில் மொத்தம் ${"%.1f".format(totalToAllocate)} kg (${effectiveRoomIds.size} அறைகளுக்கு) ஒதுக்க விரும்புகிறீர்களா? இது கிடங்கு இருப்பிலிருந்து கழிக்கப்படும்.",
                        "Are you sure you want to allocate ${"%.1f".format(totalToAllocate)} kg of ${selectedChem.chemicalNameEn} across ${effectiveRoomIds.size} room(s)? This will deduct from warehouse inventory."
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        onConfirm(selectedChem.chemicalNameTa, selectedChem.chemicalNameEn, effectiveRoomIds.toList(), parsedQty)
                    }
                ) {
                    Text(tr("ஆம், உறுதிசெய்", "Yes, Confirm"))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmationDialog = false }) {
                    Text(tr("ரத்து", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun CreateRoomDialog(
    existingRooms: List<RoomEntity>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Double, String) -> Unit
) {
    var roomNumber by remember { mutableStateOf("R-${101 + existingRooms.size}") }
    var nameEn by remember { mutableStateOf("Mixing Bay") }
    var nameTa by remember { mutableStateOf("கலவை கூடம்") }
    var capacityInput by remember { mutableStateOf("30.0") }
    var selectedHazard by remember { mutableStateOf("HIGH") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val hazardOptions = listOf("CRITICAL", "HIGH", "MODERATE")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = tr("புதிய அறை உருவாக்கம்", "Create New Room"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = tr("PESO பாதுகாப்பு விதிகளின்படி புதிய உற்பத்தி அறையைச் சேர்க்கவும்", "Add a new room per PESO pyrotechnic guidelines"),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = {
                        roomNumber = it
                        errorMessage = null
                    },
                    label = { Text(tr("அறை எண் (எ.கா: R-106)", "Room Number (e.g. R-106)")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(tr("பணி வகை / பெயர் (English)", "Work Type / Name (EN)")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameTa,
                    onValueChange = { nameTa = it },
                    label = { Text(tr("பணி வகை / பெயர் (தமிழ்)", "Work Type / Name (TA)")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = capacityInput,
                    onValueChange = {
                        capacityInput = it
                        errorMessage = null
                    },
                    label = { Text(tr("அனுமதிக்கப்பட்ட கொள்ளளவு (kg)", "Licensed Capacity (kg)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(tr("ஆபத்து வகைப்பாடு:", "Hazard Category:"), fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    hazardOptions.forEach { hazard ->
                        FilterChip(
                            selected = selectedHazard == hazard,
                            onClick = { selectedHazard = hazard },
                            label = { Text(hazard, fontSize = 11.sp) }
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMessage!!, color = SafetyCrimson, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(tr("ரத்து", "Cancel"))
                    }
                    Button(
                        onClick = {
                            val cleanNo = roomNumber.trim()
                            if (cleanNo.isBlank()) {
                                errorMessage = tr("அறை எண் காலியாக இருக்கக்கூடாது", "Room number cannot be empty.")
                                return@Button
                            }
                            if (existingRooms.any { it.roomNumber.equals(cleanNo, ignoreCase = true) }) {
                                errorMessage = tr("இந்த அறை எண் ஏற்கனவே உள்ளது! வேறு எண்ணைப் பயன்படுத்தவும்.", "Room number already exists! Please use a unique number.")
                                return@Button
                            }
                            val cap = capacityInput.toDoubleOrNull()
                            if (cap == null || cap <= 0.0) {
                                errorMessage = tr("கொள்ளளவு 0-க்கு மேல் இருக்க வேண்டும்", "Capacity must be greater than 0 kg.")
                                return@Button
                            }

                            onCreate(cleanNo, nameTa, nameEn, cap, selectedHazard)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(tr("அறை உருவாக்கு", "Create Room"))
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteRoomDialog(
    room: RoomEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: (Boolean) -> Unit
) {
    val hasChemicals = room.currentTotalKg > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = SafetyCrimson,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(tr("அறை '${room.roomNumber}' நீக்குதலை உறுதிசெய்க", "Delete Room '${room.roomNumber}'?"))
        },
        text = {
            Column {
                if (hasChemicals) {
                    Text(
                        text = tr(
                            "எச்சரிக்கை: இந்த அறையில் தற்போது ${"%.1f".format(room.currentTotalKg)} kg ரசாயனம் ஒதுக்கப்பட்டுள்ளது!",
                            "WARNING: This room currently has ${"%.1f".format(room.currentTotalKg)} kg of chemical stock allocated!"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = SafetyCrimson
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr(
                            "அறையை நீக்கினால், ஒதுக்கப்பட்ட ரசாயனங்கள் மீண்டும் கிடங்கு இருப்புக்குத் திருப்பி அனுப்பப்படும்.",
                            "Deleting this room will automatically return the allocated chemicals back to the main warehouse inventory."
                        ),
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = tr(
                            "இந்த அறையை நீக்க விரும்புகிறீர்களா? இந்த நடவடிக்கை திரும்பப்பெற முடியாது.",
                            "Are you sure you want to delete this room? This action cannot be undone."
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmDelete(true) },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyCrimson)
            ) {
                Text(
                    if (hasChemicals) tr("இருப்புக்கு திருப்பி நீக்குக", "Return Stock & Delete")
                    else tr("நீக்குக", "Delete Room")
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(tr("ரத்து", "Cancel"))
            }
        }
    )
}

// =========================================================================
// EXISTING REUSABLE COMPOSABLES (PRESERVED)
// =========================================================================

@Composable
fun ChemicalRoomCard(
    room: RoomEntity,
    onAddMaterial: () -> Unit
) {
    ChemicalRoomCardWithManagement(
        room = room,
        onAddMaterial = onAddMaterial,
        onDeleteRoom = {}
    )
}

@Composable
fun ChemicalEntryHistoryCard(entry: ChemicalEntryEntity) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
    val timeStr = dateFormat.format(Date(entry.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr(entry.chemicalNameTa, entry.chemicalNameEn),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "+${entry.quantityKg} kg",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = if (entry.status == "BLOCKED") SafetyCrimson else SafetyFlame
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${tr("தொகுதி: ", "Batch: ")}${entry.batchCode}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$timeStr • ${entry.workerName}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = IndustrialTeal, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "SHA: ${entry.tamperHash.take(8)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndustrialTeal
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChemicalScanDialog(
    rooms: List<RoomEntity>,
    initialRoom: RoomEntity?,
    onDismiss: () -> Unit,
    onSubmit: (Long, String, String, String, Double) -> Unit
) {
    val presets = listOf(
        ChemicalPreset("பொட்டாசியம் நைட்ரேட் (வெடிமருந்து)", "Potassium Nitrate (KNO3)", "BATCH-KN-902", 5.0),
        ChemicalPreset("கந்தகப் பொடி (மஞ்சள் தூள்)", "Sulphur Powder", "BATCH-SL-310", 3.0),
        ChemicalPreset("அலுமினியம் பவுடர்", "Aluminium Flake Powder", "BATCH-AL-711", 4.5),
        ChemicalPreset("பேரியம் நைட்ரேட் (பச்சை ஒளி)", "Barium Nitrate", "BATCH-BA-404", 6.0),
        ChemicalPreset("கரிப்பொடி (சாம்பல் தூள்)", "Charcoal Powder", "BATCH-CH-112", 2.5)
    )

    val defaultRoom = initialRoom ?: rooms.firstOrNull() ?: RoomEntity(
        id = 1L,
        roomNumber = "R-101",
        nameTa = "கலவை கூடம்",
        nameEn = "Chemical Mixing Shed",
        maxCapacityKg = 15.0,
        currentTotalKg = 6.0,
        hazardCategory = "CRITICAL",
        supervisorName = "Selvam (Supervisor)"
    )

    var selectedRoom by remember { mutableStateOf(defaultRoom) }
    var selectedPresetIndex by remember { mutableStateOf(0) }
    val selectedPreset = presets[selectedPresetIndex]
    var quantityInput by remember { mutableStateOf(selectedPreset.defaultKg.toString()) }
    var isScanningActive by remember { mutableStateOf(false) }
    var scanSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Scanner beam animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    fun triggerScan() {
        isScanningActive = true
        val nextIdx = (selectedPresetIndex + 1) % presets.size
        selectedPresetIndex = nextIdx
        val newPreset = presets[nextIdx]
        quantityInput = newPreset.defaultKg.toString()
        scanSuccessMessage = "${newPreset.batchCode} • ${newPreset.nameEn.split(" ")[0]}"
        isScanningActive = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("chemical_scan_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tr("ரசாயன QR ஸ்கேனர்", "Chemical QR Code Scanner"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                fontSize = 16.sp
                            )
                            Text(
                                text = tr("பாதுகாப்பு வரம்பு சரிபார்ப்பு", "PESO Verified Chemical Inward"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Camera Scanner Viewfinder Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, if (isScanningActive) MaterialTheme.colorScheme.primary else IndustrialTeal.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable { triggerScan() },
                    color = Color(0xFF0F172A)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "QR Viewfinder",
                                tint = if (isScanningActive) MaterialTheme.colorScheme.primary else Color(0xFF38BDF8),
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { triggerScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isScanningActive) MaterialTheme.colorScheme.primary else IndustrialTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("scan_qr_camera_btn")
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("நேரடி QR ஸ்கேன் செய் (Scan)", "Scan Container Barcode / QR"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (scanSuccessMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SafetyGreen.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafetyGreen, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${tr("சரிபார்க்கப்பட்டது: ", "Verified: ")}${scanSuccessMessage}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SafetyGreen
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Room Selector
                Text(
                    text = tr("சேர்க்கப்பட வேண்டிய அறை:", "Target PESO Shed / Room:"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(rooms) { room ->
                        FilterChip(
                            selected = selectedRoom.id == room.id,
                            onClick = { selectedRoom = room },
                            label = { Text(text = "${room.roomNumber} (${room.currentTotalKg.toInt()}/${room.maxCapacityKg.toInt()} kg)", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chemical Presets
                Text(
                    text = tr("ரசாயனப் பொருள் விவரம்:", "Scanned Chemical Material:"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presets.indices.toList()) { idx ->
                        val preset = presets[idx]
                        FilterChip(
                            selected = selectedPresetIndex == idx,
                            onClick = {
                                selectedPresetIndex = idx
                                quantityInput = preset.defaultKg.toString()
                            },
                            label = { Text(text = preset.nameEn.split(" ")[0], fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    label = { Text(tr("எடை அளவு (கிலோவில்)", "Inward Quantity (kg)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                val enteredQty = quantityInput.toDoubleOrNull() ?: selectedPreset.defaultKg
                val projectedTotal = selectedRoom.currentTotalKg + enteredQty
                val willExceed = projectedTotal > selectedRoom.maxCapacityKg
                val willWarn = projectedTotal >= selectedRoom.maxCapacityKg * 0.8

                Surface(
                    color = if (willExceed) SafetyCrimson.copy(alpha = 0.15f) else if (willWarn) SafetyAmber.copy(alpha = 0.15f) else SafetyGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (willExceed) Icons.Default.Block else if (willWarn) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (willExceed) SafetyCrimson else if (willWarn) SafetyAmber else SafetyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (willExceed) {
                                    "NON-CONFORMANT / LIMIT EXCEEDED"
                                } else if (willWarn) {
                                    "CAUTION: NEARING 80% CAPACITY CEILING"
                                } else {
                                    "CONFORMANT (WITHIN PESO LIMIT)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (willExceed) SafetyCrimson else if (willWarn) SafetyAmber else SafetyGreen
                            )
                            Text(
                                text = if (willExceed) {
                                    tr("அனுமதிக்கப்பட்ட வரம்பை தாண்டுகிறது! (${"%.1f".format(projectedTotal)} > ${selectedRoom.maxCapacityKg} kg)", "Exceeds licensed PESO limit! (${"%.1f".format(projectedTotal)} > ${selectedRoom.maxCapacityKg} kg)")
                                } else if (willWarn) {
                                    tr("எச்சரிக்கை: 80% கொள்ளளவை எட்டுகிறது (${"%.1f".format(projectedTotal)} kg)", "Caution: Nearing 80% threshold (${"%.1f".format(projectedTotal)} kg)")
                                } else {
                                    tr("பாதுகாப்பான வரம்பில் உள்ளது (${"%.1f".format(projectedTotal)} / ${selectedRoom.maxCapacityKg} kg)", "Within safe limit (${"%.1f".format(projectedTotal)} / ${selectedRoom.maxCapacityKg} kg)")
                                },
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(tr("ரத்து", "Cancel"), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val finalQty = quantityInput.toDoubleOrNull() ?: selectedPreset.defaultKg
                            onSubmit(
                                selectedRoom.id,
                                selectedPreset.nameTa,
                                selectedPreset.nameEn,
                                selectedPreset.batchCode,
                                finalQty
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("submit_chemical_inward_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (willExceed) SafetyCrimson else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (willExceed) tr("வரம்பு மீறல் (Submit)", "Commit & Flag") else tr("உறுதிசெய் & பூட்டு", "Verify & Seal"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
