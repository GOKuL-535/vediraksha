package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Slider
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.IndustrialTeal
import com.example.ui.theme.SafetyFlame
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.tr

@Composable
fun SettingsDialog(
    isOpen: Boolean,
    themeMode: String,
    themeAccent: String,
    onThemeModeChange: (String) -> Unit,
    onThemeAccentChange: (String) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    sosAudioVolume: Int,
    onSosAudioVolumeChange: (Int) -> Unit,
    updateStatusMessage: String,
    isCheckingForUpdates: Boolean,
    onCheckForUpdates: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var biometricLockEnabled by remember { mutableStateOf(true) }
    var shockDetectionEnabled by remember { mutableStateOf(true) }
    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with custom CrackerGearIcon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CrackerGearIcon(modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tr("அமைப்புகள்", "Settings", hi = "सेटिंग्स", ml = "ക്രമീകരണങ്ങൾ", te = "సెట్టింగులు", kn = "ಸೆಟ್ಟಿಂಗ್‌ಗಳು"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                fontSize = 17.sp
                            )
                            Text(
                                text = tr("மொழி & ஆலை விருப்பங்கள்", "Language & Safety Preferences", hi = "भाषा व सुरक्षा प्राथमिकताएं", ml = "ഭാഷയും സുരക്ഷയും"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Settings")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: ALL INDIAN LANGUAGES DROPDOWN
                Text(
                    text = tr("மொழி தேர்வு", "Select Language", hi = "भाषा चुनें", ml = "ഭാഷ തിരഞ്ഞെടുക്കുക", te = "భాషను ఎంచుకోండి", kn = "ಭಾಷೆ ಆಯ್ಕೆಮಾಡಿ"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown Box Trigger
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { isLanguageDropdownExpanded = true }
                            .testTag("language_dropdown_trigger"),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${LanguageManager.currentLanguage.nativeName} (${LanguageManager.currentLanguage.displayName})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = tr("மாற்ற இங்கே தட்டவும்", "Tap to change language", hi = "बदलने के लिए टैप करें", ml = "മാറ്റാൻ ടാപ്പ് ചെയ്യുക"),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Dropdown Menu displaying all Indian Languages
                    DropdownMenu(
                        expanded = isLanguageDropdownExpanded,
                        onDismissRequest = { isLanguageDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .heightIn(max = 360.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .testTag("settings_language_dropdown")
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = LanguageManager.currentLanguage == lang
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = lang.nativeName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = lang.displayName,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onLanguageChange(lang)
                                    isLanguageDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("settings_dropdown_item_${lang.code}")
                            )
                            if (lang != AppLanguage.entries.last()) {
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = tr("தோற்றம்", "Appearance"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tr("வண்ணத் தோற்றம்", "Theme"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "light" to tr("ஒளி", "Light"),
                        "dark" to tr("இருள்", "Dark"),
                        "system" to tr("சாதனம்", "System")
                    ).forEach { (mode, label) ->
                        val selected = themeMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onThemeModeChange(mode) }
                                .testTag("theme_mode_$mode"),
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = tr("உச்சரிப்பு நிறம்", "Accent color"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "flame" to SafetyFlame,
                        "teal" to Color(0xFF00796B),
                        "blue" to Color(0xFF1565C0),
                        "purple" to Color(0xFF6A4C93)
                    ).forEach { (accent, color) ->
                        val selected = themeAccent == accent
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { onThemeAccentChange(accent) }
                            .semantics {
                                role = Role.RadioButton
                                this.selected = selected
                                contentDescription = tr("உச்சரிப்பு நிறம்: $accent", "Accent color: $accent")
                            }
                            .testTag("theme_accent_$accent"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (selected) 30.dp else 24.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = tr(
                        "அவசர ஒலி அளவு",
                        "Emergency audio volume",
                        hi = "आपातकालीन ऑडियो वॉल्यूम",
                        ml = "അടിയന്തര ശബ്ദനില",
                        te = "అత్యవసర ఆడియో వాల్యూమ్",
                        kn = "ತುರ್ತು ಧ್ವನಿ ಮಟ್ಟ",
                        bn = "জরুরি অডিও ভলিউম",
                        mr = "आपत्कालीन ऑडिओ आवाज"
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = sosAudioVolume.toFloat(),
                    onValueChange = { onSosAudioVolumeChange(it.toInt()) },
                    valueRange = 40f..90f,
                    steps = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_audio_volume_slider")
                )
                Text(
                    text = "$sosAudioVolume%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCheckForUpdates,
                    enabled = !isCheckingForUpdates,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("check_for_app_updates"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCheckingForUpdates) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        tr(
                            "செயலி புதுப்பிப்புகளைச் சரிபார்க்கவும்",
                            "Check for app updates",
                            hi = "ऐप अपडेट देखें",
                            ml = "ആപ്പ് അപ്‌ഡേറ്റുകൾ പരിശോധിക്കുക",
                            te = "యాప్ అప్‌డేట్‌ల కోసం తనిఖీ చేయండి",
                            kn = "ಆ್ಯಪ್ ನವೀಕರಣಗಳನ್ನು ಪರಿಶೀಲಿಸಿ",
                            bn = "অ্যাপ আপডেট দেখুন",
                            mr = "अॅप अपडेट तपासा"
                        )
                    )
                }
                Text(
                    text = updateStatusMessage,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_update_status")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 2: WORKER & FACTORY PROFILE
                Text(
                    text = tr("தொழிலாளர் & ஆலை விவரங்கள்", "Worker & Factory Details", hi = "श्रमिक व कारखाना विवरण", ml = "തൊഴിലാളി വിവരങ്ങൾ"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = IndustrialTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr("தொழிலாளர்: காளியப்பன் (TN-SVK-4091)", "Worker: Kaliappan (TN-SVK-4091)", hi = "श्रमिक: कालियप्पन (TN-SVK-4091)", ml = "തൊഴിലാളി: കാളിയപ്പൻ (TN-SVK-4091)"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = IndustrialNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr("அலகு: சிவகாசி பட்டாசு அலகு 4", "Unit: Sivakasi Fireworks Unit 4", hi = "इकाई: शिवकाशी आतिशबाजी इकाई 4", ml = "യൂണിറ്റ്: ശിവകാശി ഫയർവർക്സ് യൂണിറ്റ് 4"),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PESO License: PESO/EXP/TN/2024/9912",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 3: SAFETY PREFERENCES
                Text(
                    text = tr("பாதுகாப்பு விருப்பங்கள்", "Safety & Biometric Settings", hi = "सुरक्षा व बायोमेट्रिक सेटिंग्स", ml = "സുരക്ഷ ക്രമീകരണങ്ങൾ"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Biometric Lock toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = IndustrialTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tr("கைரேகை அங்கீகாரம்", "Biometric Authentication", hi = "बायोमेट्रिक सत्यापन", ml = "ബയോമെട്രിക് പ്രാമാണീകരണം"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = tr("ஷிப்ட் & ஊதிய பதிவை பூட்ட", "Required for shift and wage locking", hi = "शिफ्ट व मजदूरी सुरक्षा के लिए आवश्यक"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Switch(
                            checked = biometricLockEnabled,
                            onCheckedChange = { biometricLockEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = IndustrialTeal, checkedTrackColor = IndustrialTeal.copy(alpha = 0.3f))
                        )
                    }

                    // Haptic & Vibration toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tr("அதிர்வு எச்சரிக்கை", "Haptic Alert Pulses", hi = "कंपन चेतावनी", ml = "വൈബ്രേഷൻ മുന്നറിയിപ്പ്"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = tr("ஆபத்து & பொத்தான் அதிர்வு", "Vibrate on limits & emergency SOS", hi = "सीमा पार व आपातकाल में कंपन"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Switch(
                            checked = hapticFeedbackEnabled,
                            onCheckedChange = { hapticFeedbackEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        )
                    }

                    // Shock detection sensor toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tr("தானியங்கி வெடிப்பு சென்சார்", "Blast Impact Classifier", hi = "विस्फोट व झटका संवेदक", ml = "സ്ഫോടന സെൻസർ"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = tr("ஆன்-டிவைஸ் அதிர்வு மாதிரி", "Background fall/blast shock sensor", hi = "गिरावट व शॉक अलर्ट"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Switch(
                            checked = shockDetectionEnabled,
                            onCheckedChange = { shockDetectionEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE11D48), checkedTrackColor = Color(0xFFE11D48).copy(alpha = 0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("save_and_close_settings_btn")
                ) {
                    Text(
                        text = tr("முடிந்தது", "Save & Close", hi = "सहेजें व बंद करें", ml = "സംരക്ഷിക്കുക", te = "సేవ్ & మూసివేయి", kn = "ಉಳಿಸಿ ಮತ್ತು ಮುಚ್ಚಿ"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
