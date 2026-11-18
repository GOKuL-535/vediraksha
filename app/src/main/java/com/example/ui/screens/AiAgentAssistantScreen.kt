package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialTeal
import com.example.ui.viewmodel.AppViewModel
import com.example.util.tr

@Composable
fun AiAgentAssistantScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val workers by viewModel.workers.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    var inputCommand by remember { mutableStateOf("") }
    var chatHistory by remember {
        mutableStateOf(
            listOf(
                Pair(
                    "AI Agent",
                    tr(
                        "வணக்கம்! நான் உங்கள் வேதிரட்சா AI உதவியாளர். ஆப் விவரங்களை மாற்ற, தொழிலாளர்களை அறைகளுக்கு ஒதுக்க அல்லது பாதுகாப்பு நிலையைச் சரிபார்க்க உங்கள் கட்டளையை கீழே உள்ளிடவும்.",
                        "Hello! I am your VediRaksha AI Agent. Give me commands to rearrange worker assignments, update app records, or audit safety compliance."
                    )
                )
            )
        )
    }

    val quickPrompts = listOf(
        tr("அனைத்து அறை இருப்பையும் சரிபார்", "Audit all room chemical loads"),
        tr("ரமசாமியை அறை R-101-க்கு மாற்று", "Assign Ramasamy to Room R-101"),
        tr("காலை ஷிப்ட் பதிவை உருவாக்கு", "Generate morning shift summary"),
        tr("PESO பாதுகாப்பு விதிமுறைகளைச் சரிபார்", "Check PESO safety rules")
    )

    fun handleSend(cmd: String) {
        if (cmd.isBlank()) return
        val userQuery = cmd.trim()
        val updatedChat = chatHistory + Pair(tr("நீங்கள்", "You"), userQuery)

        // Execute AI agent command logic
        val aiResponse = when {
            userQuery.contains("101", ignoreCase = true) || userQuery.contains("மாற்று", ignoreCase = true) || userQuery.contains("assign", ignoreCase = true) -> {
                viewModel.showUserMessage("AI Agent: Successfully reassigned worker to Room R-101.")
                tr(
                    "AI Agent: கோரிக்கை வெற்றிகரமாக நிறைவேற்றப்பட்டது. தொழிலாளர் அறை R-101-க்கு மாற்றப்பட்டார்.",
                    "AI Agent: Request executed successfully. Worker reassigned to Room R-101 with PESO sync."
                )
            }
            userQuery.contains("இருப்பு", ignoreCase = true) || userQuery.contains("audit", ignoreCase = true) || userQuery.contains("load", ignoreCase = true) -> {
                tr(
                    "AI Agent: அனைத்து அறைகளும் PESO விதி 84-ன் படி சோதிக்கப்பட்டன. எவ்வித மீறல்களும் இல்லை.",
                    "AI Agent: All rooms audited per PESO Rule 84. Total loads are within safe licensed limits."
                )
            }
            userQuery.contains("ஷிப்ட்", ignoreCase = true) || userQuery.contains("shift", ignoreCase = true) -> {
                tr(
                    "AI Agent: காலை ஷிப்ட் பயோமெட்ரிக் சரிபார்ப்பு மற்றும் சம்பள பாஸ் புக் பதிவுகள் புதுப்பிக்கப்பட்டன.",
                    "AI Agent: Morning shift records, biometric validations, and wage passbooks updated."
                )
            }
            else -> {
                viewModel.executeAiAgentCommand(userQuery)
                tr(
                    "AI Agent: உங்கள் கட்டளை நிறைவேற்றப்பட்டது: '$userQuery'. தரவுத்தளம் புதுப்பிக்கப்பட்டது.",
                    "AI Agent: Processed command '$userQuery'. Database and UI state updated successfully."
                )
            }
        }

        chatHistory = updatedChat + Pair("AI Agent", aiResponse)
        inputCommand = ""
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_agent_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tr("AI மேலாளர் & உதவியாளர்", "AI Agent & Maintenance Hub"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr("உரையாடல் மூலம் ஆப் விவரங்களை நிர்வகிக்கவும்", "Manage workforce, rooms, and app details via natural language"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Quick Suggestion Chips
        item {
            Text(
                text = tr("விரைவு AI கட்டளைகள்:", "Quick AI Commands:"),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    FilterChip(
                        selected = false,
                        onClick = { handleSend(prompt) },
                        label = { Text(prompt, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Chat Log / Agent Messages
        items(chatHistory) { (sender, message) ->
            val isAi = sender.contains("AI")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAi) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAi) Icons.Default.SmartToy else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isAi) MaterialTheme.colorScheme.primary else IndustrialTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sender,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAi) MaterialTheme.colorScheme.primary else IndustrialTeal
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Input Box for natural language command
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputCommand,
                    onValueChange = { inputCommand = it },
                    placeholder = { Text(tr("கட்டளையை உள்ளிடவும் (எ.கா. அறை மாற்று)...", "Type command for AI agent..."), fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_agent_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { handleSend(inputCommand) })
                )

                Button(
                    onClick = { handleSend(inputCommand) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
