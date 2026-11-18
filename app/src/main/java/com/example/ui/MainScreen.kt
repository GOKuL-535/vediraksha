package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BiometricDialog
import com.example.ui.components.EmergencyMapsRadarDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SosModalDialog
import com.example.ui.components.TopSafetyBar
import com.example.ui.screens.ChemicalTrackerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LaborsManagementScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ShiftManagementScreen
import com.example.ui.screens.WagePassbookScreen
import com.example.ui.screens.AiAgentAssistantScreen
import com.example.ui.screens.AlertEscalationScreen
import com.example.ui.screens.StockManagementScreen
import com.example.ui.viewmodel.AppViewModel
import com.example.util.tr

data class AppUpdateUiState(
    val message: String,
    val isChecking: Boolean = false
)

enum class NavTab(
    val titleTa: String,
    val titleEn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    DASHBOARD("முகப்பு", "Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_dashboard"),
    CHEMICALS("அறைகள்", "Rooms", Icons.Filled.Science, Icons.Outlined.Science, "tab_chemicals"),
    SHIFTS("பணி", "Shifts", Icons.Filled.Work, Icons.Outlined.Work, "tab_shifts"),
    AI_ASSISTANT("AI மேலாளர்", "AI Agent", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_ai_assistant"),
    MORE("மேலும்", "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz, "tab_more")
}

private enum class MoreDestination { LABORS, REPORTS, WAGES, ALERT_ESCALATION, STOCK_MANAGEMENT }

@Composable
fun MainScreen(
    viewModel: AppViewModel,
    updateUiState: AppUpdateUiState = AppUpdateUiState(""),
    onCheckForUpdates: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(NavTab.DASHBOARD) }
    var moreDestination by remember { mutableStateOf<MoreDestination?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showEmergencyMapsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val sosState by viewModel.sosState.collectAsState()
    val biometricState by viewModel.biometricState.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val activeShift by viewModel.activeShift.collectAsState()
    val sosAudioVolume by viewModel.sosAudioVolume.collectAsState()

    // Handle back button
    BackHandler(enabled = currentTab != NavTab.DASHBOARD) {
        if (currentTab == NavTab.MORE && moreDestination != null) {
            moreDestination = null
        } else {
            currentTab = NavTab.DASHBOARD
        }
    }

    // Show Snackbar messages
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopSafetyBar(
                workerName = viewModel.workerName,
                unitName = viewModel.unitName,
                onSosClick = { viewModel.triggerSosCountdown() },
                onSettingsClick = { showSettingsDialog = true },
                onOpenMaps = { showEmergencyMapsDialog = true },
                showBackButton = currentTab != NavTab.DASHBOARD,
                onBackClick = {
                    if (currentTab == NavTab.MORE && moreDestination != null) {
                        moreDestination = null
                    } else {
                        currentTab = NavTab.DASHBOARD
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                NavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentTab = tab
                            moreDestination = null
                        },
                        icon = {
                            if (tab == NavTab.SHIFTS && activeShift != null) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(6.dp)
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tr(tab.titleTa, tab.titleEn),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tr(tab.titleTa, tab.titleEn),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tr(tab.titleTa, tab.titleEn),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRooms = { currentTab = NavTab.CHEMICALS },
                    onNavigateToShifts = { currentTab = NavTab.SHIFTS },
                    onNavigateToWages = {
                        currentTab = NavTab.MORE
                        moreDestination = MoreDestination.WAGES
                    },
                    onNavigateToLabors = {
                        currentTab = NavTab.MORE
                        moreDestination = MoreDestination.LABORS
                    }
                )
                NavTab.CHEMICALS -> ChemicalTrackerScreen(viewModel = viewModel)
                NavTab.SHIFTS -> ShiftManagementScreen(viewModel = viewModel)
                NavTab.AI_ASSISTANT -> AiAgentAssistantScreen(viewModel = viewModel)
                NavTab.MORE -> when (moreDestination) {
                    MoreDestination.LABORS -> LaborsManagementScreen(
                        viewModel = viewModel,
                        onOpenEmergencyMaps = { showEmergencyMapsDialog = true }
                    )
                    MoreDestination.REPORTS -> ReportsScreen(viewModel = viewModel)
                    MoreDestination.WAGES -> WagePassbookScreen(viewModel = viewModel)
                    MoreDestination.ALERT_ESCALATION -> AlertEscalationScreen(viewModel = viewModel)
                    MoreDestination.STOCK_MANAGEMENT -> StockManagementScreen(viewModel = viewModel)
                    null -> MoreHubScreen(
                        onNavigateToLabors = { moreDestination = MoreDestination.LABORS },
                        onNavigateToReports = { moreDestination = MoreDestination.REPORTS },
                        onNavigateToWages = { moreDestination = MoreDestination.WAGES },
                        onNavigateToAlertEscalation = { moreDestination = MoreDestination.ALERT_ESCALATION },
                        onNavigateToStockManagement = { moreDestination = MoreDestination.STOCK_MANAGEMENT }
                    )
                }
            }
        }
    }

    // Emergency Maps Radar Dialog
    if (showEmergencyMapsDialog) {
        EmergencyMapsRadarDialog(
            onDismiss = { showEmergencyMapsDialog = false }
        )
    }

    // Settings Dialog for Language & Safety Preferences
    SettingsDialog(
        isOpen = showSettingsDialog,
        themeMode = viewModel.themeMode.collectAsState().value,
        themeAccent = viewModel.themeAccent.collectAsState().value,
        onThemeModeChange = viewModel::setThemeMode,
        onThemeAccentChange = viewModel::setThemeAccent,
        onLanguageChange = viewModel::setLanguage,
        sosAudioVolume = sosAudioVolume,
        onSosAudioVolumeChange = viewModel::setSosAudioVolume,
        updateStatusMessage = updateUiState.message,
        isCheckingForUpdates = updateUiState.isChecking,
        onCheckForUpdates = onCheckForUpdates,
        onDismiss = { showSettingsDialog = false }
    )

    // Modal Overlays for SOS and Biometric verification
    SosModalDialog(
        sosState = sosState,
        audioVolume = sosAudioVolume,
        onCancelCountdown = { viewModel.cancelSosCountdown() },
        onResolveAlert = { viewModel.resolveActiveSos() }
    )

    BiometricDialog(
        state = biometricState,
        onDismiss = { viewModel.dismissBiometric() }
    )
}
