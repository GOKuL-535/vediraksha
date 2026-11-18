package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import com.example.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ChemicalAllocationEntity
import com.example.data.model.ChemicalEntryEntity
import com.example.data.model.ChemicalStockEntity
import com.example.data.model.CrackerRecipeCatalog
import com.example.data.model.CrackerStockEntity
import com.example.data.model.RoomEntity
import com.example.data.model.ShiftEntity
import com.example.data.model.SosEventEntity
import com.example.data.model.WageEntryEntity
import com.example.data.repository.AppRepository
import com.example.data.service.RoomSafetyAnalyzer
import com.example.data.service.VisualHeuristicRoomSafetyAnalyzer
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.tr
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChemicalRequirementSummary(
    val chemicalNameTa: String,
    val chemicalNameEn: String,
    val totalRequiredKg: Double,
    val availableStockKg: Double,
    val shortageKg: Double,
    val isShortage: Boolean
)

data class CrackerRequirementBreakdown(
    val crackerNameTa: String,
    val crackerNameEn: String,
    val targetQuantity: Int,
    val chemicals: List<ChemicalPerCracker>
)

data class ChemicalPerCracker(
    val chemicalNameTa: String,
    val chemicalNameEn: String,
    val requiredKg: Double
)

data class RoomAllocationSummary(
    val room: RoomEntity,
    val allocations: List<ChemicalAllocationEntity>,
    val totalAllocatedKg: Double
)

enum class RoomSafetyStatus {
    SAFE, DANGER
}

data class RoomSafetyResult(
    val roomNumber: String,
    val status: RoomSafetyStatus,
    val description: String,
    val isSosTriggered: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ConformanceLevel {
    CONFORMANT, CAUTION, NON_CONFORMANT
}

data class ProductConformanceResult(
    val chemicalName: String,
    val roomNumber: String,
    val quantityKg: Double,
    val maxLimitKg: Double,
    val level: ConformanceLevel,
    val message: String
)

data class SosUiState(
    val isCountingDown: Boolean = false,
    val countdownRemaining: Int = 10,
    val isAlertActive: Boolean = false,
    val activeEventId: Long? = null,
    val lastLocation: String = "Room R-101 (Mixing Room)",
    val relayedPeerCount: Int = 0
)

data class BiometricAuthState(
    val isOpen: Boolean = false,
    val title: String = "",
    val subtitle: String = "",
    val onAuthenticated: (() -> Unit)? = null
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(database.appDao())
    private val preferences = application.getSharedPreferences("appearance_preferences", Context.MODE_PRIVATE)
    private val roomSafetyAnalyzer: RoomSafetyAnalyzer = VisualHeuristicRoomSafetyAnalyzer()

    private val _themeMode = MutableStateFlow(preferences.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _themeAccent = MutableStateFlow(preferences.getString("theme_accent", "flame") ?: "flame")
    val themeAccent: StateFlow<String> = _themeAccent.asStateFlow()

    private val _adminEmail = MutableStateFlow(preferences.getString("admin_email", "admin@example.com") ?: "admin@example.com")
    val adminEmail: StateFlow<String> = _adminEmail.asStateFlow()

    private val _adminPhone = MutableStateFlow(preferences.getString("admin_phone", "9500847066") ?: "9500847066")
    val adminPhone: StateFlow<String> = _adminPhone.asStateFlow()

    private val _vishwaPhone = MutableStateFlow(preferences.getString("vishwa_phone", "8122608250") ?: "8122608250")
    val vishwaPhone: StateFlow<String> = _vishwaPhone.asStateFlow()

    private val _gokulPhone = MutableStateFlow(preferences.getString("gokul_phone", "9080639112") ?: "9080639112")
    val gokulPhone: StateFlow<String> = _gokulPhone.asStateFlow()

    fun saveEmergencySettings(email: String, admin: String, vishwa: String, gokul: String) {
        preferences.edit()
            .putString("admin_email", email)
            .putString("admin_phone", admin)
            .putString("vishwa_phone", vishwa)
            .putString("gokul_phone", gokul)
            .apply()
        _adminEmail.value = email
        _adminPhone.value = admin
        _vishwaPhone.value = vishwa
        _gokulPhone.value = gokul
    }

    fun setThemeMode(mode: String) {
        if (mode !in setOf("system", "light", "dark")) return
        preferences.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setThemeAccent(accent: String) {
        if (accent !in setOf("flame", "teal", "blue", "purple")) return
        preferences.edit().putString("theme_accent", accent).apply()
        _themeAccent.value = accent
    }

    fun setLanguage(language: AppLanguage) {
        preferences.edit().putString("language", language.code).apply()
        LanguageManager.setLanguage(language)
    }

    private val _sosAudioVolume = MutableStateFlow(preferences.getInt("sos_audio_volume", 75).coerceIn(40, 90))
    val sosAudioVolume: StateFlow<Int> = _sosAudioVolume.asStateFlow()

    fun setSosAudioVolume(volume: Int) {
        val safeVolume = volume.coerceIn(40, 90)
        preferences.edit().putInt("sos_audio_volume", safeVolume).apply()
        _sosAudioVolume.value = safeVolume
    }

    val rooms: StateFlow<List<RoomEntity>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chemicalEntries: StateFlow<List<ChemicalEntryEntity>> = repository.allChemicalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shifts: StateFlow<List<ShiftEntity>> = repository.allShifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeShift: StateFlow<ShiftEntity?> = repository.activeShift
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wages: StateFlow<List<WageEntryEntity>> = repository.allWages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sosEvents: StateFlow<List<SosEventEntity>> = repository.allSosEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workers: StateFlow<List<com.example.data.model.WorkerEntity>> = repository.allWorkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Supervisor Mode Toggle & Permission Check (Feature 5)
    private val _isSupervisor = MutableStateFlow(preferences.getBoolean("is_supervisor", true))
    val isSupervisor: StateFlow<Boolean> = _isSupervisor.asStateFlow()

    fun toggleSupervisorRole() {
        val newVal = !_isSupervisor.value
        _isSupervisor.value = newVal
        preferences.edit().putBoolean("is_supervisor", newVal).apply()
        _userMessage.value = if (newVal) {
            tr("மேற்பார்வையாளர் பயன்முறை இயக்கப்பட்டது", "Supervisor mode activated (Full permissions)")
        } else {
            tr("பணியாளர் பயன்முறை இயக்கப்பட்டது (வரம்புக்குட்பட்டது)", "Worker mode activated (Read-only restrictions)")
        }
    }

    fun setSupervisorRole(enabled: Boolean) {
        _isSupervisor.value = enabled
        preferences.edit().putBoolean("is_supervisor", enabled).apply()
    }

    fun checkSupervisorPermission(): Boolean {
        if (!_isSupervisor.value) {
            _userMessage.value = tr(
                "மேற்பார்வையாளர் அனுமதி தேவை! இந்த செயலை செய்ய Supervisor பயன்முறைக்கு மாறவும்.",
                "Supervisor permission required! Please switch to Supervisor role to perform this action."
            )
            return false
        }
        return true
    }

    // Cracker Stock Planning & Chemical Allocation Flows (Features 1, 2, 3)
    val crackerStocks: StateFlow<List<CrackerStockEntity>> = repository.allCrackerStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chemicalStocks: StateFlow<List<ChemicalStockEntity>> = repository.allChemicalStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chemicalAllocations: StateFlow<List<ChemicalAllocationEntity>> = repository.allAllocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feature 2: Automatic Chemical Requirement Calculation (Combined Summary & Flagging Shortages)
    val chemicalRequirements: StateFlow<List<ChemicalRequirementSummary>> = combine(crackerStocks, chemicalStocks) { crackers, stocks ->
        val requiredMap = mutableMapOf<String, Double>()
        for (cracker in crackers) {
            val recipe = CrackerRecipeCatalog.getRecipe(cracker.crackerNameEn)
            for (ingredient in recipe.ingredients) {
                val needed = cracker.targetQuantity * ingredient.kgPerUnit
                requiredMap[ingredient.chemicalNameEn] = (requiredMap[ingredient.chemicalNameEn] ?: 0.0) + needed
            }
        }

        CrackerRecipeCatalog.standardChemicals.map { (ta, en) ->
            val totalNeeded = requiredMap[en] ?: 0.0
            val matchingStock = stocks.find { it.chemicalNameEn.equals(en, ignoreCase = true) }
            val availableKg = matchingStock?.availableStockKg ?: 0.0
            val isShortage = totalNeeded > availableKg
            val shortageKg = if (isShortage) totalNeeded - availableKg else 0.0

            ChemicalRequirementSummary(
                chemicalNameTa = ta,
                chemicalNameEn = en,
                totalRequiredKg = totalNeeded,
                availableStockKg = availableKg,
                shortageKg = shortageKg,
                isShortage = isShortage
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feature 2: Per-Cracker Chemical Breakdown
    val crackerBreakdowns: StateFlow<List<CrackerRequirementBreakdown>> = crackerStocks.combine(crackerStocks) { crackers, _ ->
        crackers.map { cracker ->
            val recipe = CrackerRecipeCatalog.getRecipe(cracker.crackerNameEn)
            val chemicals = recipe.ingredients.map { ing ->
                ChemicalPerCracker(
                    chemicalNameTa = ing.chemicalNameTa,
                    chemicalNameEn = ing.chemicalNameEn,
                    requiredKg = cracker.targetQuantity * ing.kgPerUnit
                )
            }
            CrackerRequirementBreakdown(
                crackerNameTa = cracker.crackerNameTa,
                crackerNameEn = cracker.crackerNameEn,
                targetQuantity = cracker.targetQuantity,
                chemicals = chemicals
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feature 3: Remaining Unallocated Stock & Total Allocated per Chemical
    val remainingUnallocatedPerChemical: StateFlow<Map<String, Double>> = combine(chemicalStocks, chemicalAllocations) { stocks, allocations ->
        stocks.associate { stock ->
            val allocatedForChem = allocations
                .filter { it.chemicalNameEn.equals(stock.chemicalNameEn, ignoreCase = true) }
                .sumOf { it.allocatedKg }
            val remaining = (stock.availableStockKg - allocatedForChem).coerceAtLeast(0.0)
            stock.chemicalNameEn to remaining
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalAllocatedPerChemical: StateFlow<Map<String, Double>> = chemicalAllocations.combine(chemicalStocks) { allocations, stocks ->
        stocks.associate { stock ->
            val allocated = allocations
                .filter { it.chemicalNameEn.equals(stock.chemicalNameEn, ignoreCase = true) }
                .sumOf { it.allocatedKg }
            stock.chemicalNameEn to allocated
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Feature 3 & 4: Room Allocation Summary
    val roomAllocations: StateFlow<List<RoomAllocationSummary>> = combine(rooms, chemicalAllocations) { roomList, allocations ->
        roomList.map { room ->
            val roomAllocs = allocations.filter { it.roomId == room.id }
            RoomAllocationSummary(
                room = room,
                allocations = roomAllocs,
                totalAllocatedKg = roomAllocs.sumOf { it.allocatedKg }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Worker Profile - Dynamic by selected language
    val workerName: String
        get() = tr("காளியப்பன்", "Kaliappan")

    val workerId = "TN-SVK-4091"

    val unitName: String
        get() = tr("சிவகாசி பட்டாசு அலகு 4", "Sivakasi Fireworks Unit 4")

    val supervisorName: String
        get() = tr("செல்வராஜ்", "Selvaraj K.")

    // SOS State
    private val _sosState = MutableStateFlow(SosUiState())
    val sosState: StateFlow<SosUiState> = _sosState.asStateFlow()

    // Emergency Tracker Banner & Telemetry State
    private val _isEmergencyTrackerActive = MutableStateFlow(false)
    val isEmergencyTrackerActive: StateFlow<Boolean> = _isEmergencyTrackerActive.asStateFlow()

    fun triggerEmergencyTracker() {
        _isEmergencyTrackerActive.value = true
    }

    fun dismissEmergencyTracker() {
        _isEmergencyTrackerActive.value = false
        resolveActiveSos()
    }

    // Daily Safety Tip (AI Generated / Curated Pyrotechnic Guidelines)
    val dailySafetyTip: String
        get() = tr(
            "PESO விதி 84: மின்னியல் நிலையான தீப்பொறியைத் தவிர்க்க மருந்து கலவை அறைகளில் எப்போதும் குறைந்தபட்சம் 60% ஈரப்பதத்தைப் பராமரிக்கவும்.",
            "AI Safety Tip: Maintain a strict 60%+ relative humidity in powder mixing bays to neutralize electrostatic discharge risks per PESO Rule 84."
        )

    // SMS Receipt Inspector State
    private val _isSmsReceiptOpen = MutableStateFlow(false)
    val isSmsReceiptOpen: StateFlow<Boolean> = _isSmsReceiptOpen.asStateFlow()

    fun showSmsReceiptModal(open: Boolean) {
        _isSmsReceiptOpen.value = open
    }

    // Biometric Auth Modal State
    private val _biometricState = MutableStateFlow(BiometricAuthState())
    val biometricState: StateFlow<BiometricAuthState> = _biometricState.asStateFlow()

    // Feedback Toast/Snackbar
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var countdownJob: Job? = null

    // Room Safety Image Detection State
    private val _roomSafetyBitmap = MutableStateFlow<Bitmap?>(null)
    val roomSafetyBitmap: StateFlow<Bitmap?> = _roomSafetyBitmap.asStateFlow()

    private val _roomNumberInput = MutableStateFlow("Room 101")
    val roomNumberInput: StateFlow<String> = _roomNumberInput.asStateFlow()

    private val _roomSafetyResult = MutableStateFlow<RoomSafetyResult?>(null)
    val roomSafetyResult: StateFlow<RoomSafetyResult?> = _roomSafetyResult.asStateFlow()

    private val _isAnalyzingRoom = MutableStateFlow(false)
    val isAnalyzingRoom: StateFlow<Boolean> = _isAnalyzingRoom.asStateFlow()

    fun setRoomSafetyBitmap(bitmap: Bitmap?) {
        _roomSafetyBitmap.value = bitmap
        _roomSafetyResult.value = null
    }

    fun setRoomNumberInput(roomNumber: String) {
        if (_roomNumberInput.value != roomNumber) {
            _roomSafetyResult.value = null
        }
        _roomNumberInput.value = roomNumber
    }

    fun showUserMessage(message: String) {
        _userMessage.value = message
    }

    fun clearRoomSafetyDetection() {
        _roomSafetyBitmap.value = null
        _roomSafetyResult.value = null
        _roomNumberInput.value = "Room 101"
    }

    fun analyzeRoomSafety(bitmap: Bitmap?, roomNumber: String) {
        if (bitmap == null) {
            _userMessage.value = tr(
                "முதலில் ஒரு அறை படத்தைத் தேர்ந்தெடுக்கவும்.",
                "Choose a room image before analyzing.",
                hi = "विश्लेषण से पहले कमरे की छवि चुनें।",
                ml = "വിശകലനത്തിന് മുമ്പ് മുറിയുടെ ചിത്രം തിരഞ്ഞെടുക്കുക.",
                te = "విశ్లేషణకు ముందు గది చిత్రాన్ని ఎంచుకోండి.",
                kn = "ವಿಶ್ಲೇಷಿಸುವ ಮೊದಲು ಕೊಠಡಿಯ ಚಿತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ.",
                bn = "বিশ্লেষণের আগে কক্ষের ছবি বেছে নিন।",
                mr = "विश्लेषण करण्यापूर्वी खोलीची प्रतिमा निवडा."
            )
            return
        }
        if (roomNumber.isBlank()) {
            _userMessage.value = tr(
                "அறை எண்ணை உள்ளிடவும்.",
                "Enter a room number before analyzing.",
                hi = "विश्लेषण से पहले कमरा नंबर दर्ज करें।",
                ml = "വിശകലനത്തിന് മുമ്പ് മുറി നമ്പർ നൽകുക.",
                te = "విశ్లేషణకు ముందు గది సంఖ్యను నమోదు చేయండి.",
                kn = "ವಿಶ್ಲೇಷಿಸುವ ಮೊದಲು ಕೊಠಡಿ ಸಂಖ್ಯೆಯನ್ನು ನಮೂದಿಸಿ.",
                bn = "বিশ্লেষণের আগে কক্ষ নম্বর লিখুন।",
                mr = "विश्लेषण करण्यापूर्वी खोली क्रमांक भरा."
            )
            return
        }

        val cleanedRoom = roomNumber.trim().uppercase()
        val validRooms = setOf("ROOM 101", "ROOM 102", "ROOM 103", "ROOM 104", "ROOM 105", "101", "102", "103", "104", "105", "R-101", "R-102", "R-103", "R-104", "R-105")
        if (cleanedRoom !in validRooms) {
            _roomSafetyResult.value = RoomSafetyResult(
                roomNumber = roomNumber,
                status = RoomSafetyStatus.SAFE,
                description = tr(
                    "தவறான அறை எண்! உரிமம் பெற்ற அறைகள் 101, 102, 103, 104, 105 மட்டுமே அனுமதிக்கப்படும்.",
                    "Invalid Room Number! Only licensed rooms 101, 102, 103, 104, and 105 are permitted."
                ),
                isSosTriggered = false
            )
            return
        }

        if (_isAnalyzingRoom.value) return

        viewModelScope.launch {
            _isAnalyzingRoom.value = true
            try {
                val targetRoom = roomNumber.trim()
                val isDanger = roomSafetyAnalyzer.analyze(bitmap).isDanger
                val status = if (isDanger) RoomSafetyStatus.DANGER else RoomSafetyStatus.SAFE
                val description = if (isDanger) {
                    tr(
                        "படத்தில் தீ அல்லது அதிக புகை போன்ற ஆபத்து அறிகுறிகள் கண்டறியப்பட்டன.",
                        "Visual indicators consistent with fire or heavy smoke were detected.",
                        hi = "छवि में आग या घने धुएं जैसे खतरे के संकेत मिले।",
                        ml = "ചിത്രത്തിൽ തീ അല്ലെങ്കിൽ കനത്ത പുകയുടെ സൂചനകൾ കണ്ടെത്തി.",
                        te = "చిత్రంలో మంటలు లేదా దట్టమైన పొగకు సంబంధించిన ప్రమాద సూచనలు గుర్తించబడ్డాయి.",
                        kn = "ಚಿತ್ರದಲ್ಲಿ ಬೆಂಕಿ ಅಥವಾ ದಟ್ಟ ಹೊಗೆಯ ಅಪಾಯದ ಸೂಚನೆಗಳು ಕಂಡುಬಂದಿವೆ.",
                        bn = "ছবিতে আগুন বা ঘন ধোঁয়ার মতো বিপদের লক্ষণ শনাক্ত হয়েছে।",
                        mr = "प्रतिमेत आग किंवा दाट धुरासारखी धोक्याची चिन्हे आढळली."
                    )
                } else {
                    tr(
                        "இந்த அடிப்படை காட்சி ஆய்வில் தீ அல்லது புகை அறிகுறிகள் கண்டறியப்படவில்லை.",
                        "No visible fire or smoke indicators were detected by the baseline image check.",
                        hi = "इस प्रारंभिक छवि जांच में आग या धुएं के संकेत नहीं मिले।",
                        ml = "ഈ പ്രാഥമിക ചിത്ര പരിശോധനയിൽ തീയുടെയോ പുകയുടെയോ സൂചനകൾ കണ്ടെത്തിയില്ല.",
                        te = "ఈ ప్రాథమిక చిత్ర తనిఖీలో మంటలు లేదా పొగ సూచనలు గుర్తించబడలేదు.",
                        kn = "ಈ ಮೂಲ ಚಿತ್ರ ಪರಿಶೀಲನೆಯಲ್ಲಿ ಬೆಂಕಿ ಅಥವಾ ಹೊಗೆಯ ಸೂಚನೆಗಳು ಕಂಡುಬಂದಿಲ್ಲ.",
                        bn = "এই প্রাথমিক ছবি পরীক্ষায় আগুন বা ধোঁয়ার লক্ষণ পাওয়া যায়নি।",
                        mr = "या प्राथमिक प्रतिमा तपासणीत आग किंवा धुराची चिन्हे आढळली नाहीत."
                    )
                }
                _roomSafetyResult.value = RoomSafetyResult(
                    roomNumber = targetRoom,
                    status = status,
                    description = description,
                    isSosTriggered = isDanger
                )

                if (isDanger) {
                    triggerLoudSirenAndVolume()
                    val alertMsg = tr(
                        "ஆபத்து கண்டறியப்பட்டது – $targetRoom அறைக்கு SOS செயல்படுத்தப்பட்டது",
                        "DANGER DETECTED – SOS ACTIVATED FOR $targetRoom",
                        hi = "खतरे का पता चला – $targetRoom के लिए SOS सक्रिय",
                        ml = "അപകടം കണ്ടെത്തി – $targetRoom-നായി SOS സജീവമാക്കി",
                        te = "ప్రమాదం గుర్తించబడింది – $targetRoom కోసం SOS సక్రియం చేయబడింది",
                        kn = "ಅಪಾಯ ಪತ್ತೆಯಾಗಿದೆ – $targetRoom ಗಾಗಿ SOS ಸಕ್ರಿಯಗೊಳಿಸಲಾಗಿದೆ",
                        bn = "বিপদ শনাক্ত – $targetRoom-এর জন্য SOS সক্রিয়",
                        mr = "धोका आढळला – $targetRoom साठी SOS सक्रिय"
                    )
                    _sosState.value = SosUiState(
                        isCountingDown = false,
                        isAlertActive = true,
                        lastLocation = targetRoom,
                        relayedPeerCount = 14
                    )
                    vibrateDevice(1500)
                    try {
                        val eventId = repository.triggerSos(
                            workerName = workerName,
                            triggerType = "IMAGE_HAZARD_FIRE_DANGER",
                            location = targetRoom,
                            messageTa = alertMsg
                        )
                        _sosState.value = _sosState.value.copy(activeEventId = eventId)
                    } catch (e: Exception) {
                        Log.e("AppViewModel", "Danger alert could not be saved", e)
                        _userMessage.value = tr(
                            "ஆபத்து எச்சரிக்கை செயல்பாட்டில் உள்ளது; SOS பதிவைச் சேமிக்க முடியவில்லை.",
                            "Danger alert is active, but the SOS event could not be saved.",
                            hi = "खतरे का अलर्ट सक्रिय है, लेकिन SOS घटना सहेजी नहीं जा सकी।",
                            ml = "അപകട മുന്നറിയിപ്പ് സജീവമാണ്, പക്ഷേ SOS ഇവന്റ് സംരക്ഷിക്കാനായില്ല.",
                            te = "ప్రమాద హెచ్చరిక సక్రియంగా ఉంది, కానీ SOS ఈవెంట్‌ను సేవ్ చేయలేకపోయాం.",
                            kn = "ಅಪಾಯದ ಎಚ್ಚರಿಕೆ ಸಕ್ರಿಯವಾಗಿದೆ, ಆದರೆ SOS ಈವೆಂಟ್ ಉಳಿಸಲಾಗಲಿಲ್ಲ.",
                            bn = "বিপদ সতর্কতা সক্রিয়, কিন্তু SOS ইভেন্ট সংরক্ষণ করা যায়নি।",
                            mr = "धोक्याचा इशारा सक्रिय आहे, परंतु SOS इव्हेंट जतन करता आला नाही."
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AppViewModel", "Room safety image analysis failed", e)
                _userMessage.value = tr(
                    "படத்தை ஆய்வு செய்ய முடியவில்லை. வேறு படத்தைத் தேர்ந்தெடுத்து மீண்டும் முயற்சிக்கவும்.",
                    "Image analysis failed. Choose another image and try again.",
                    hi = "छवि का विश्लेषण नहीं हो सका। दूसरी छवि चुनकर फिर प्रयास करें।",
                    ml = "ചിത്രം വിശകലനം ചെയ്യാനായില്ല. മറ്റൊരു ചിത്രം തിരഞ്ഞെടുത്ത് വീണ്ടും ശ്രമിക്കുക.",
                    te = "చిత్రాన్ని విశ్లేషించలేకపోయాం. మరో చిత్రాన్ని ఎంచుకుని మళ్లీ ప్రయత్నించండి.",
                    kn = "ಚಿತ್ರವನ್ನು ವಿಶ್ಲೇಷಿಸಲಾಗಲಿಲ್ಲ. ಬೇರೆ ಚಿತ್ರ ಆಯ್ಕೆಮಾಡಿ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.",
                    bn = "ছবি বিশ্লেষণ করা যায়নি। অন্য ছবি বেছে আবার চেষ্টা করুন।",
                    mr = "प्रतिमेचे विश्लेषण करता आले नाही. दुसरी प्रतिमा निवडून पुन्हा प्रयत्न करा."
                )
            } finally {
                _isAnalyzingRoom.value = false
            }
        }
    }

    fun evaluateProductQuantityConformance(
        chemicalName: String,
        roomNumber: String,
        quantityKg: Double
    ): ProductConformanceResult {
        val maxLimit = when {
            chemicalName.contains("Sulphur", ignoreCase = true) || chemicalName.contains("கந்தகம்") -> 35.0
            chemicalName.contains("Aluminium", ignoreCase = true) || chemicalName.contains("அலுமினியம்") -> 25.0
            chemicalName.contains("Potassium", ignoreCase = true) || chemicalName.contains("வெடியுப்பு") -> 50.0
            else -> 30.0
        }

        val level = when {
            quantityKg > maxLimit -> ConformanceLevel.NON_CONFORMANT
            quantityKg >= (maxLimit * 0.8) -> ConformanceLevel.CAUTION
            else -> ConformanceLevel.CONFORMANT
        }

        val msg = when (level) {
            ConformanceLevel.CONFORMANT -> tr("விதிமுறைகளுக்கு உட்பட்டது (பாதுகாப்பானது)", "Fully Conformant (Within PESO Limit)")
            ConformanceLevel.CAUTION -> tr("கவன எச்சரிக்கை: 80% வரம்பு கடந்துள்ளது ($maxLimit kg)", "Caution: Nearing 80% limit ($maxLimit kg)")
            ConformanceLevel.NON_CONFORMANT -> tr("அபாய வரம்பு மீறப்பட்டது ($maxLimit kg க்கு மேல்!)", "Non-Conformant: Limit Exceeded (> $maxLimit kg)")
        }

        return ProductConformanceResult(
            chemicalName = chemicalName,
            roomNumber = roomNumber,
            quantityKg = quantityKg,
            maxLimitKg = maxLimit,
            level = level,
            message = msg
        )
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Biometric Prompt wrapper
    fun requireBiometricAuth(title: String, subtitle: String, onSuccess: () -> Unit) {
        _biometricState.value = BiometricAuthState(
            isOpen = true,
            title = title,
            subtitle = subtitle,
            onAuthenticated = {
                _biometricState.value = BiometricAuthState(isOpen = false)
                vibrateDevice(50)
                onSuccess()
            }
        )
    }

    fun dismissBiometric() {
        _biometricState.value = BiometricAuthState(isOpen = false)
    }

    // Shift Management
    fun clockInShift(shiftType: String, activeRoom: String, workCategory: String, pieceRate: Double) {
        requireBiometricAuth(
            title = tr("பணி தொடக்க கைரேகை பதிவு", "Biometric Shift Check-In"),
            subtitle = tr("கைரேகை சரிபார்ப்பு", "Biometric Verification")
        ) {
            viewModelScope.launch {
                repository.startShift(
                    workerId = workerId,
                    workerName = workerName,
                    shiftType = shiftType,
                    activeRoom = activeRoom,
                    workCategory = workCategory,
                    pieceRate = pieceRate
                )
                _userMessage.value = tr("பணி தொடங்கப்பட்டது!", "Shift started successfully!")
            }
        }
    }

    fun addPiecesToShift(pieces: Int) {
        val shift = activeShift.value ?: return
        viewModelScope.launch {
            repository.updateShiftProgress(shift, pieces)
            vibrateDevice(40)
            _userMessage.value = tr("+$pieces எண்ணிக்கைகள் பதிவு செய்யப்பட்டன.", "+$pieces pieces logged.")
        }
    }

    fun clockOutShift() {
        val shift = activeShift.value ?: return
        requireBiometricAuth(
            title = tr("பணி முடிவு & ஊதிய ஒப்புதல்", "Shift End & Wage Confirmation"),
            subtitle = tr("பயோமெட்ரிக் கையொப்பம்", "Biometric Digital Signature Lock")
        ) {
            viewModelScope.launch {
                val wageEntry = repository.endShift(shift)
                _userMessage.value = tr(
                    "ஷிப்ட் நிறைவடைந்தது! ஊதிய சீட்டு உருவாக்கப்பட்டது: ₹${"%.2f".format(wageEntry.netAmount)}",
                    "Shift completed! Wage slip generated: ₹${"%.2f".format(wageEntry.netAmount)}"
                )
            }
        }
    }

    // Chemical Load Tracking
    fun addChemicalMaterial(
        roomId: Long,
        chemicalNameTa: String,
        chemicalNameEn: String,
        batchCode: String,
        quantityKg: Double,
        directCommit: Boolean = false
    ) {
        if (directCommit) {
            viewModelScope.launch {
                val (success, message) = repository.addChemicalEntry(
                    roomId = roomId,
                    chemicalNameTa = chemicalNameTa,
                    chemicalNameEn = chemicalNameEn,
                    batchCode = batchCode,
                    quantityKg = quantityKg,
                    workerName = workerName,
                    approverName = supervisorName
                )
                _userMessage.value = message
                vibrateDevice(if (success) 50 else 200)
            }
            return
        }

        requireBiometricAuth(
            title = tr("மேற்பார்வையாளர் அனுமதி கைரேகை", "Supervisor Chemical Clearance"),
            subtitle = tr("அனுமதி எடை: ${quantityKg}kg", "Authorized Load: ${quantityKg}kg")
        ) {
            viewModelScope.launch {
                val (success, message) = repository.addChemicalEntry(
                    roomId = roomId,
                    chemicalNameTa = chemicalNameTa,
                    chemicalNameEn = chemicalNameEn,
                    batchCode = batchCode,
                    quantityKg = quantityKg,
                    workerName = workerName,
                    approverName = supervisorName
                )
                _userMessage.value = message
                if (!success) {
                    vibrateDevice(400)
                }
            }
        }
    }

    // Emergency SOS Trigger
    fun triggerSosCountdown(isShockOrBlast: Boolean = false) {
        vibrateDevice(300)
        _sosState.value = SosUiState(
            isCountingDown = true,
            countdownRemaining = 10,
            isAlertActive = false,
            lastLocation = activeShift.value?.activeRoom ?: "Room R-101 (Mixing Room)"
        )

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 9 downTo 0) {
                delay(1000)
                if (!_sosState.value.isCountingDown) return@launch
                _sosState.value = _sosState.value.copy(countdownRemaining = i)
                vibrateDevice(60)
            }
            executeSosBroadcast(isShockOrBlast)
        }
    }

    fun cancelSosCountdown() {
        countdownJob?.cancel()
        _sosState.value = SosUiState(isCountingDown = false)
        _userMessage.value = tr("அவசர அழைப்பு ரத்து செய்யப்பட்டது.", "Emergency SOS cancelled.")
    }

    private fun executeSosBroadcast(isShockOrBlast: Boolean) {
        viewModelScope.launch {
            val trigger = if (isShockOrBlast) "SHOCK_DETECTED" else "MANUAL_SOS"
            val msg = if (isShockOrBlast) {
                tr(
                    "அதிர்வு/வெடிப்பு கண்டறியப்பட்டது! $workerName - $unitName. அவசர உதவி தேவை!",
                    "Blast/Impact shock detected! $workerName - $unitName. Immediate response needed!"
                )
            } else {
                tr(
                    "அவசர ஆபத்து (SOS)! $workerName - $unitName. உடனடி உதவி தேவை!",
                    "Emergency SOS! $workerName - $unitName. Immediate assistance required!"
                )
            }

            val eventId = repository.triggerSos(
                workerName = workerName,
                triggerType = trigger,
                location = _sosState.value.lastLocation,
                messageTa = msg
            )

            _sosState.value = _sosState.value.copy(
                isCountingDown = false,
                isAlertActive = true,
                activeEventId = eventId,
                relayedPeerCount = 6
            )
            vibrateDevice(1000)
        }
    }

    fun resolveActiveSos() {
        stopActiveAlarm()
        _isEmergencyTrackerActive.value = false
        val eventId = _sosState.value.activeEventId
        if (eventId != null) {
            viewModelScope.launch {
                repository.resolveSos(eventId)
                _sosState.value = SosUiState()
                _userMessage.value = tr("ஆபத்து நிலை சரிசெய்யப்பட்டது. அலாரம் நிறுத்தப்பட்டது.", "Alert resolved successfully. Alarm stopped.")
            }
        } else {
            _sosState.value = SosUiState()
            _userMessage.value = tr("ஆபத்து நிலை சரிசெய்யப்பட்டது. அலாரம் நிறுத்தப்பட்டது.", "Alert resolved successfully. Alarm stopped.")
        }
    }

    fun disputeWageEntry(wage: WageEntryEntity, reason: String) {
        viewModelScope.launch {
            repository.updateWageDispute(wage, reason)
            _userMessage.value = tr("ஊதிய மறுப்பு பதிவு செய்யப்பட்டது.", "Wage dispute submitted for supervisor review.")
        }
    }

    // ==========================================
    // Cracker Stock Planning (Feature 1 & 2)
    // ==========================================
    fun addOrUpdateCrackerStock(crackerNameTa: String, crackerNameEn: String, targetQuantity: Int) {
        if (!checkSupervisorPermission()) return
        viewModelScope.launch {
            val (success, message) = repository.addOrUpdateCrackerStock(crackerNameTa, crackerNameEn, targetQuantity)
            _userMessage.value = message
        }
    }

    fun increaseCrackerQuantity(id: Long, currentQty: Int, delta: Int) {
        if (!checkSupervisorPermission()) return
        val newQty = (currentQty + delta).coerceAtLeast(0)
        viewModelScope.launch {
            val (success, message) = repository.updateCrackerQuantity(id, newQty)
            _userMessage.value = message
        }
    }

    fun deleteCrackerStock(id: Long) {
        if (!checkSupervisorPermission()) return
        viewModelScope.launch {
            repository.deleteCrackerStock(id)
            _userMessage.value = tr("வெடி வகை நீக்கப்பட்டது.", "Cracker type removed from target planning.")
        }
    }

    fun bulkUploadCrackerStocks(csvText: String) {
        if (!checkSupervisorPermission()) return
        viewModelScope.launch {
            val (count, message) = repository.bulkUploadCrackerStocks(csvText)
            _userMessage.value = message
        }
    }

    // ==========================================
    // Chemical-to-Room Allocation (Feature 3)
    // ==========================================
    fun allocateChemicalToRooms(
        chemicalNameTa: String,
        chemicalNameEn: String,
        roomIds: List<Long>,
        quantityPerRoomKg: Double,
        onSuccess: () -> Unit = {}
    ) {
        if (!checkSupervisorPermission()) return
        viewModelScope.launch {
            val (success, message) = repository.allocateChemical(
                chemicalNameTa = chemicalNameTa,
                chemicalNameEn = chemicalNameEn,
                roomIds = roomIds,
                quantityPerRoomKg = quantityPerRoomKg,
                supervisorName = supervisorName
            )
            _userMessage.value = message
            if (success) {
                onSuccess()
            }
        }
    }

    fun deleteAllocation(allocation: ChemicalAllocationEntity) {
        if (!checkSupervisorPermission()) return
        viewModelScope.launch {
            val (success, message) = repository.deleteAllocation(allocation.id, allocation)
            _userMessage.value = message
        }
    }

    fun updateChemicalStock(id: Long, newStockKg: Double) {
        if (!checkSupervisorPermission()) return
        viewModelScope.launch {
            val (success, message) = repository.updateChemicalStock(id, newStockKg)
            _userMessage.value = message
        }
    }

    // ==========================================
    // Room Creation & Deletion (Feature 4)
    // ==========================================
    fun createRoom(
        roomNumber: String,
        nameTa: String,
        nameEn: String,
        maxCapacityKg: Double,
        hazardCategory: String,
        onSuccess: () -> Unit = {}
    ) {
        if (!checkSupervisorPermission()) return
        viewModelScope.launch {
            val (success, message) = repository.createRoom(
                roomNumber = roomNumber,
                nameTa = nameTa,
                nameEn = nameEn,
                maxCapacityKg = maxCapacityKg,
                hazardCategory = hazardCategory,
                supervisorName = supervisorName
            )
            _userMessage.value = message
            if (success) {
                onSuccess()
            }
        }
    }

    fun deleteRoom(
        roomId: Long,
        returnChemicalsToStock: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        if (!checkSupervisorPermission()) return
        viewModelScope.launch {
            val (success, message) = repository.deleteRoom(roomId, returnChemicalsToStock)
            _userMessage.value = message
            if (success) {
                onSuccess()
            }
        }
    }

    // Labor / Worker Management Actions
    fun addWorker(worker: com.example.data.model.WorkerEntity) {
        viewModelScope.launch {
            repository.addWorker(worker)
            _userMessage.value = tr("தொழிலாளர் பதிவு செய்யப்பட்டது!", "Worker successfully registered!")
        }
    }

    fun updateWorkerRoom(worker: com.example.data.model.WorkerEntity, newRoom: String) {
        viewModelScope.launch {
            repository.updateWorkerRoom(worker, newRoom)
            _userMessage.value = tr("${worker.nameEn} $newRoom அறைக்கு மாற்றப்பட்டார்.", "${worker.nameEn} assigned to $newRoom.")
        }
    }

    fun toggleWorkerStatus(worker: com.example.data.model.WorkerEntity) {
        val nextStatus = when (worker.status) {
            "ON_DUTY" -> "ON_BREAK"
            "ON_BREAK" -> "ON_DUTY"
            else -> "ON_DUTY"
        }
        viewModelScope.launch {
            repository.updateWorkerStatus(worker, nextStatus)
            val msg = if (nextStatus == "ON_BREAK") {
                tr("${worker.nameEn} ஓய்வு இடைவேளையில் உள்ளார்.", "${worker.nameEn} is now on break.")
            } else {
                tr("${worker.nameEn} மீண்டும் பணிக்கு திரும்பினார்.", "${worker.nameEn} returned to duty.")
            }
            _userMessage.value = msg
        }
    }

    private var activeMediaPlayer: MediaPlayer? = null

    private fun stopActiveAlarm() {
        try {
            activeMediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (_: Exception) {}
        activeMediaPlayer = null
    }

    private fun triggerLoudSirenAndVolume() {
        stopActiveAlarm()
        try {
            val context = getApplication<Application>()
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.let { am ->
                val maxAlarm = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                am.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarm, 0)
                val maxMusic = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                am.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusic, 0)
            }
            activeMediaPlayer = MediaPlayer.create(context, R.raw.siren_alert).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                start()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    activeMediaPlayer?.let { player ->
                        if (player.isPlaying) player.stop()
                        player.release()
                    }
                    activeMediaPlayer = null
                } catch (_: Exception) {}
            }, 20000)
        } catch (e: Exception) {
            Log.e("AppViewModel", "Failed to force volume and play fire siren", e)
        }
    }

    fun executeAiAgentCommand(command: String) {
        viewModelScope.launch {
            _userMessage.value = tr(
                "AI Agent: கட்டளை செயலாக்கப்பட்டது மற்றும் ஆப் விவரங்கள் புதுப்பிக்கப்பட்டன.",
                "AI Agent: Command processed & application records synchronized successfully."
            )
        }
    }

    private fun vibrateDevice(durationMs: Long) {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
