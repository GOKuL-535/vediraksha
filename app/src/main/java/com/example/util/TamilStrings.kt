package com.example.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Context

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val shortBadge: String
) {
    ENGLISH("en", "English", "English", "EN"),
    TAMIL("ta", "Tamil", "தமிழ்", "தமிழ்"),
    HINDI("hi", "Hindi", "हिन्दी", "हिन्दी"),
    MALAYALAM("ml", "Malayalam", "മലയാളം", "മലയാളം"),
    TELUGU("te", "Telugu", "తెలుగు", "తెలుగు"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ", "ಕನ್ನಡ"),
    BENGALI("bn", "Bengali", "বাংলা", "বাংলা"),
    MARATHI("mr", "Marathi", "मराठी", "मराठी")
}

object LanguageManager {
    var currentLanguage by mutableStateOf(AppLanguage.ENGLISH)

    fun initialize(context: Context) {
        val savedCode = context.getSharedPreferences("appearance_preferences", Context.MODE_PRIVATE)
            .getString("language", AppLanguage.ENGLISH.code)
        currentLanguage = AppLanguage.entries.firstOrNull { it.code == savedCode } ?: AppLanguage.ENGLISH
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
    }

    fun toggleLanguage() {
        val all = AppLanguage.entries
        val nextIndex = (all.indexOf(currentLanguage) + 1) % all.size
        currentLanguage = all[nextIndex]
    }
}

// Built-in Indian dictionary translations for automated translation fallback
private val translationsDictionary: Map<String, Map<AppLanguage, String>> = mapOf(
    "Safe" to mapOf(
        AppLanguage.ENGLISH to "Safe",
        AppLanguage.TAMIL to "பாதுகாப்பானது",
        AppLanguage.HINDI to "सुरक्षित",
        AppLanguage.MALAYALAM to "സുരക്ഷിതം",
        AppLanguage.TELUGU to "సురక్షితం",
        AppLanguage.KANNADA to "ಸುರಕ್ಷಿತ",
        AppLanguage.BENGALI to "নিরাপদ",
        AppLanguage.MARATHI to "सुरक्षित"
    ),
    "Room Safety Detection" to mapOf(
        AppLanguage.ENGLISH to "Room Safety Detection",
        AppLanguage.TAMIL to "அறை பாதுகாப்பு கண்டறிதல்",
        AppLanguage.HINDI to "कमरे की सुरक्षा जांच",
        AppLanguage.MALAYALAM to "മുറി സുരക്ഷാ പരിശോധന",
        AppLanguage.TELUGU to "గది భద్రత తనిఖీ",
        AppLanguage.KANNADA to "ಕೊಠಡಿ ಸುರಕ್ಷತಾ ಪರಿಶೀಲನೆ",
        AppLanguage.BENGALI to "কক্ষের নিরাপত্তা পরীক্ষা",
        AppLanguage.MARATHI to "खोली सुरक्षा तपासणी"
    ),
    "Room Number" to mapOf(
        AppLanguage.ENGLISH to "Room Number",
        AppLanguage.TAMIL to "அறை எண்",
        AppLanguage.HINDI to "कमरा नंबर",
        AppLanguage.MALAYALAM to "മുറി നമ്പർ",
        AppLanguage.TELUGU to "గది సంఖ్య",
        AppLanguage.KANNADA to "ಕೊಠಡಿ ಸಂಖ್ಯೆ",
        AppLanguage.BENGALI to "কক্ষ নম্বর",
        AppLanguage.MARATHI to "खोली क्रमांक"
    ),
    "Choose Image" to mapOf(
        AppLanguage.ENGLISH to "Choose Image",
        AppLanguage.TAMIL to "படத்தைத் தேர்ந்தெடு",
        AppLanguage.HINDI to "छवि चुनें",
        AppLanguage.MALAYALAM to "ചിത്രം തിരഞ്ഞെടുക്കുക",
        AppLanguage.TELUGU to "చిత్రాన్ని ఎంచుకోండి",
        AppLanguage.KANNADA to "ಚಿತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ",
        AppLanguage.BENGALI to "ছবি বেছে নিন",
        AppLanguage.MARATHI to "प्रतिमा निवडा"
    ),
    "Analyze Room Safety" to mapOf(
        AppLanguage.ENGLISH to "Analyze Room Safety",
        AppLanguage.TAMIL to "அறை பாதுகாப்பை ஆய்வு செய்",
        AppLanguage.HINDI to "कमरे की सुरक्षा जांचें",
        AppLanguage.MALAYALAM to "മുറി സുരക്ഷ പരിശോധിക്കുക",
        AppLanguage.TELUGU to "గది భద్రతను విశ్లేషించండి",
        AppLanguage.KANNADA to "ಕೊಠಡಿ ಸುರಕ್ಷತೆಯನ್ನು ವಿಶ್ಲೇಷಿಸಿ",
        AppLanguage.BENGALI to "কক্ষের নিরাপত্তা বিশ্লেষণ করুন",
        AppLanguage.MARATHI to "खोलीच्या सुरक्षिततेचे विश्लेषण करा"
    ),
    "Analyzing..." to mapOf(
        AppLanguage.ENGLISH to "Analyzing...",
        AppLanguage.TAMIL to "ஆய்வு செய்யப்படுகிறது...",
        AppLanguage.HINDI to "विश्लेषण हो रहा है...",
        AppLanguage.MALAYALAM to "വിശകലനം ചെയ്യുന്നു...",
        AppLanguage.TELUGU to "విశ్లేషిస్తోంది...",
        AppLanguage.KANNADA to "ವಿಶ್ಲೇಷಿಸಲಾಗುತ್ತಿದೆ...",
        AppLanguage.BENGALI to "বিশ্লেষণ করা হচ্ছে...",
        AppLanguage.MARATHI to "विश्लेषण सुरू आहे..."
    ),
    "Reset" to mapOf(
        AppLanguage.ENGLISH to "Reset",
        AppLanguage.TAMIL to "மீட்டமை",
        AppLanguage.HINDI to "रीसेट",
        AppLanguage.MALAYALAM to "പുനഃസജ്ജമാക്കുക",
        AppLanguage.TELUGU to "రీసెట్",
        AppLanguage.KANNADA to "ಮರುಹೊಂದಿಸಿ",
        AppLanguage.BENGALI to "রিসেট",
        AppLanguage.MARATHI to "रीसेट"
    ),
    "DANGER DETECTED" to mapOf(
        AppLanguage.ENGLISH to "DANGER DETECTED",
        AppLanguage.TAMIL to "ஆபத்து கண்டறியப்பட்டது",
        AppLanguage.HINDI to "खतरे का पता चला",
        AppLanguage.MALAYALAM to "അപകടം കണ്ടെത്തി",
        AppLanguage.TELUGU to "ప్రమాదం గుర్తించబడింది",
        AppLanguage.KANNADA to "ಅಪಾಯ ಪತ್ತೆಯಾಗಿದೆ",
        AppLanguage.BENGALI to "বিপদ শনাক্ত হয়েছে",
        AppLanguage.MARATHI to "धोका आढळला"
    ),
    "ROOM SAFE" to mapOf(
        AppLanguage.ENGLISH to "ROOM SAFE",
        AppLanguage.TAMIL to "அறை பாதுகாப்பாக உள்ளது",
        AppLanguage.HINDI to "कमरा सुरक्षित है",
        AppLanguage.MALAYALAM to "മുറി സുരക്ഷിതമാണ്",
        AppLanguage.TELUGU to "గది సురక్షితంగా ఉంది",
        AppLanguage.KANNADA to "ಕೊಠಡಿ ಸುರಕ್ಷಿತವಾಗಿದೆ",
        AppLanguage.BENGALI to "কক্ষ নিরাপদ",
        AppLanguage.MARATHI to "खोली सुरक्षित आहे"
    ),
    "VediRaksha" to mapOf(
        AppLanguage.ENGLISH to "VediRaksha",
        AppLanguage.TAMIL to "வெடிரக்ஷா",
        AppLanguage.HINDI to "वेदीरक्षा",
        AppLanguage.MALAYALAM to "വെടിരക്ഷ",
        AppLanguage.TELUGU to "వెడిరక్ష",
        AppLanguage.KANNADA to "ವೆಡಿರಕ್ಷಾ",
        AppLanguage.BENGALI to "ভেদিরক্ষা",
        AppLanguage.MARATHI to "वेदीरक्षा"
    ),
    "Settings" to mapOf(
        AppLanguage.ENGLISH to "Settings",
        AppLanguage.TAMIL to "அமைப்புகள்",
        AppLanguage.HINDI to "सेटिंग्स",
        AppLanguage.MALAYALAM to "ക്രമീകരണങ്ങൾ",
        AppLanguage.TELUGU to "సెట్టింగులు",
        AppLanguage.KANNADA to "ಸೆಟ್ಟಿಂಗ್‌ಗಳು",
        AppLanguage.BENGALI to "সেটিংস",
        AppLanguage.MARATHI to "सेटिंग्ज"
    ),
    "Select Language" to mapOf(
        AppLanguage.ENGLISH to "Select Language",
        AppLanguage.TAMIL to "மொழி தேர்வு",
        AppLanguage.HINDI to "भाषा चुनें",
        AppLanguage.MALAYALAM to "ഭാഷ തിരഞ്ഞെടുക്കുക",
        AppLanguage.TELUGU to "భాషను ఎంచుకోండి",
        AppLanguage.KANNADA to "ಭಾಷೆ ಆಯ್ಕೆಮಾಡಿ",
        AppLanguage.BENGALI to "ভাষা নির্বাচন করুন",
        AppLanguage.MARATHI to "भाषा निवडा"
    ),
    "Dashboard" to mapOf(
        AppLanguage.ENGLISH to "Dashboard",
        AppLanguage.TAMIL to "முகப்பு",
        AppLanguage.HINDI to "डैशबोर्ड",
        AppLanguage.MALAYALAM to "ഡാഷ്‌ബോർഡ്",
        AppLanguage.TELUGU to "డాష్‌బోర్డ్",
        AppLanguage.KANNADA to "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್",
        AppLanguage.BENGALI to "ড্যাশবোর্ড",
        AppLanguage.MARATHI to "डॅशबोर्ड"
    ),
    "Chemicals" to mapOf(
        AppLanguage.ENGLISH to "Chemicals",
        AppLanguage.TAMIL to "அறைகள்",
        AppLanguage.HINDI to "रसायन",
        AppLanguage.MALAYALAM to "കെമിക്കൽസ്",
        AppLanguage.TELUGU to "రసాయనాలు",
        AppLanguage.KANNADA to "ರಾಸಾಯನಿಕ",
        AppLanguage.BENGALI to "রাসায়নিক",
        AppLanguage.MARATHI to "रसायने"
    ),
    "Home" to mapOf(
        AppLanguage.ENGLISH to "Home",
        AppLanguage.TAMIL to "முகப்பு",
        AppLanguage.HINDI to "होम",
        AppLanguage.MALAYALAM to "ഹോം",
        AppLanguage.TELUGU to "హోమ్",
        AppLanguage.KANNADA to "ಹೋಮ್",
        AppLanguage.BENGALI to "হোম",
        AppLanguage.MARATHI to "होम"
    ),
    "Rooms" to mapOf(
        AppLanguage.ENGLISH to "Rooms",
        AppLanguage.TAMIL to "அறைகள்",
        AppLanguage.HINDI to "कक्ष",
        AppLanguage.MALAYALAM to "മുറികൾ",
        AppLanguage.TELUGU to "గదులు",
        AppLanguage.KANNADA to "ಕೊಠಡಿ",
        AppLanguage.BENGALI to "কক্ষ",
        AppLanguage.MARATHI to "खोली"
    ),
    "Shifts" to mapOf(
        AppLanguage.ENGLISH to "Shifts",
        AppLanguage.TAMIL to "பணி",
        AppLanguage.HINDI to "शिफ्ट",
        AppLanguage.MALAYALAM to "ഷിഫ്റ്റ്",
        AppLanguage.TELUGU to "షిఫ్ట్",
        AppLanguage.KANNADA to "ಶಿಫ್ಟ್",
        AppLanguage.BENGALI to "শিফট",
        AppLanguage.MARATHI to "शिफ्ट"
    ),
    "Labors" to mapOf(
        AppLanguage.ENGLISH to "Labors",
        AppLanguage.TAMIL to "ஆட்கள்",
        AppLanguage.HINDI to "श्रमिक",
        AppLanguage.MALAYALAM to "തൊഴിലാളി",
        AppLanguage.TELUGU to "కార్మికులు",
        AppLanguage.KANNADA to "ಕಾರ್ಮಿಕರು",
        AppLanguage.BENGALI to "শ্রমিক",
        AppLanguage.MARATHI to "कामगार"
    ),
    "Wages" to mapOf(
        AppLanguage.ENGLISH to "Wages",
        AppLanguage.TAMIL to "சம்பளம்",
        AppLanguage.HINDI to "वेतन",
        AppLanguage.MALAYALAM to "വേതനം",
        AppLanguage.TELUGU to "జీతం",
        AppLanguage.KANNADA to "ವೇತನ",
        AppLanguage.BENGALI to "মজুরি",
        AppLanguage.MARATHI to "वेतन"
    ),
    "Passbook" to mapOf(
        AppLanguage.ENGLISH to "Passbook",
        AppLanguage.TAMIL to "பாஸ்புக்",
        AppLanguage.HINDI to "पासबुक",
        AppLanguage.MALAYALAM to "പാസ്ബുക്ക്",
        AppLanguage.TELUGU to "పాస్‌బుక్",
        AppLanguage.KANNADA to "ಪಾಸ್‌ಬುಕ್",
        AppLanguage.BENGALI to "পাসবুক",
        AppLanguage.MARATHI to "पासबुक"
    ),
    "Reports" to mapOf(
        AppLanguage.ENGLISH to "Reports",
        AppLanguage.TAMIL to "அறிக்கை",
        AppLanguage.HINDI to "रिपोर्ट",
        AppLanguage.MALAYALAM to "റിപ്പോർട്ട്",
        AppLanguage.TELUGU to "నివేదికలు",
        AppLanguage.KANNADA to "ವರದಿಗಳು",
        AppLanguage.BENGALI to "রিপোর্ট",
        AppLanguage.MARATHI to "अहवाल"
    ),
    "Clock In" to mapOf(
        AppLanguage.ENGLISH to "Clock In",
        AppLanguage.TAMIL to "பணி தொடங்க",
        AppLanguage.HINDI to "चेक-इन",
        AppLanguage.MALAYALAM to "ചെക്ക്-ഇൻ",
        AppLanguage.TELUGU to "చెక్-ఇన్",
        AppLanguage.KANNADA to "ಚೆಕ್-ಇನ್",
        AppLanguage.BENGALI to "চেক-ইন",
        AppLanguage.MARATHI to "चेक-इन"
    ),
    "Clock Out" to mapOf(
        AppLanguage.ENGLISH to "Clock Out",
        AppLanguage.TAMIL to "பணி முடிக்க",
        AppLanguage.HINDI to "चेक-आउट",
        AppLanguage.MALAYALAM to "ചെക്ക്-ഔട്ട്",
        AppLanguage.TELUGU to "చెక్-అవుట్",
        AppLanguage.KANNADA to "ಚೆಕ್-ಔಟ್",
        AppLanguage.BENGALI to "চেক-আউট",
        AppLanguage.MARATHI to "चेक-आउट"
    ),
    "On Duty" to mapOf(
        AppLanguage.ENGLISH to "On Duty",
        AppLanguage.TAMIL to "பணியில்",
        AppLanguage.HINDI to "ड्यूटी पर",
        AppLanguage.MALAYALAM to "ഡ്യൂട്ടിയിൽ",
        AppLanguage.TELUGU to "డ్యూటీలో",
        AppLanguage.KANNADA to "ಡ್ಯೂಟಿಯಲ್ಲಿ",
        AppLanguage.BENGALI to "ডিউটিতে",
        AppLanguage.MARATHI to "कामावर"
    ),
    "On Break" to mapOf(
        AppLanguage.ENGLISH to "On Break",
        AppLanguage.TAMIL to "ஓய்வில்",
        AppLanguage.HINDI to "ब्रेक पर",
        AppLanguage.MALAYALAM to "ഇടവേളയിൽ",
        AppLanguage.TELUGU to "విరామం",
        AppLanguage.KANNADA to "ವಿರಾಮ",
        AppLanguage.BENGALI to "বিরতিতে",
        AppLanguage.MARATHI to "ब्रेकवर"
    ),
    "Total" to mapOf(
        AppLanguage.ENGLISH to "Total",
        AppLanguage.TAMIL to "மொத்தம்",
        AppLanguage.HINDI to "कुल",
        AppLanguage.MALAYALAM to "ആകെ",
        AppLanguage.TELUGU to "మొత్తం",
        AppLanguage.KANNADA to "ಒಟ್ಟು",
        AppLanguage.BENGALI to "মোট",
        AppLanguage.MARATHI to "एकूण"
    )
)

/**
 * Universal translation function with English-safe fallback for unfinished translations.
 */
fun tr(
    tamilText: String,
    englishText: String,
    hi: String? = null,
    ml: String? = null,
    te: String? = null,
    kn: String? = null,
    bn: String? = null,
    mr: String? = null
): String {
    return when (LanguageManager.currentLanguage) {
        AppLanguage.ENGLISH -> englishText
        AppLanguage.TAMIL -> tamilText
        AppLanguage.HINDI -> hi ?: translationsDictionary[englishText]?.get(AppLanguage.HINDI) ?: englishText
        AppLanguage.MALAYALAM -> ml ?: translationsDictionary[englishText]?.get(AppLanguage.MALAYALAM) ?: englishText
        AppLanguage.TELUGU -> te ?: translationsDictionary[englishText]?.get(AppLanguage.TELUGU) ?: englishText
        AppLanguage.KANNADA -> kn ?: translationsDictionary[englishText]?.get(AppLanguage.KANNADA) ?: englishText
        AppLanguage.BENGALI -> bn ?: translationsDictionary[englishText]?.get(AppLanguage.BENGALI) ?: englishText
        AppLanguage.MARATHI -> mr ?: translationsDictionary[englishText]?.get(AppLanguage.MARATHI) ?: englishText
    }
}
