package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.tr

@Composable
fun MoreHubScreen(
    onNavigateToLabors: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToWages: () -> Unit,
    onNavigateToAlertEscalation: () -> Unit = {},
    onNavigateToStockManagement: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = tr("மேலும் விருப்பங்கள்", "More"),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = tr("தொழிலாளர் மேலாண்மை மற்றும் பாதுகாப்பு அறிக்கைகள்", "Workforce management and safety reports"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            MoreHubCard(
                title = tr("தொழிலாளர் மேலாண்மை", "Workforce"),
                subtitle = tr("பணியாளர் பட்டியல் மற்றும் அறை ஒதுக்கீடு", "Workers, duty status, and room assignments"),
                icon = Icons.Default.Engineering,
                testTag = "more_labors",
                onClick = onNavigateToLabors
            )
        }
        item {
            MoreHubCard(
                title = tr("பாதுகாப்பு அறிக்கைகள்", "Safety reports"),
                subtitle = tr("PESO இணக்கம் மற்றும் செயல்பாட்டு தணிக்கை", "PESO compliance and operational audits"),
                icon = Icons.Default.Assessment,
                testTag = "more_reports",
                onClick = onNavigateToReports
            )
        }
        item {
            MoreHubCard(
                title = tr("சம்பளப் பாஸ் புக்", "Wage Passbook"),
                subtitle = tr("நாள் கூலி கணக்கு மற்றும் கையொப்ப பதிவு", "Daily piece-rate earnings and biometric signatures"),
                icon = Icons.Default.Receipt,
                testTag = "more_wages",
                onClick = onNavigateToWages
            )
        }
        item {
            MoreHubCard(
                title = tr("எச்சரிக்கை உயர்த்தும் கன்சோல்", "Alert Escalation Console"),
                subtitle = tr("மின்னஞ்சல், குறுஞ்செய்தி & தானியங்கி அழைப்பு சோதனைகள்", "Simulate and test multi-channel alert escalation flow"),
                icon = Icons.Default.NotificationsActive,
                testTag = "more_escalation",
                onClick = onNavigateToAlertEscalation
            )
        }
        item {
            MoreHubCard(
                title = tr("சரக்கு மேலாண்மை & இறக்குமதி வரலாறு", "Stock Management & Import History"),
                subtitle = tr("ரசாயன தொகுதி இறக்குமதி பதிவுகள் மற்றும் தணிக்கை", "Verified chemical import batches and audit logs"),
                icon = Icons.Default.Inventory,
                testTag = "more_stock_management",
                onClick = onNavigateToStockManagement
            )
        }
    }
}

@Composable
private fun MoreHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
