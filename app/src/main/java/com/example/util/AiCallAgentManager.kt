package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class EmergencyContact(
    val id: String,
    val name: String,
    val role: String,
    val phone: String
)

fun getEmergencyContacts(context: Context): List<EmergencyContact> {
    val prefs = context.getSharedPreferences("appearance_preferences", Context.MODE_PRIVATE)
    val admin = prefs.getString("admin_phone", "9500847066") ?: "9500847066"
    val vishwa = prefs.getString("vishwa_phone", "8122608250") ?: "8122608250"
    val gokul = prefs.getString("gokul_phone", "9080639112") ?: "9080639112"
    return listOf(
        EmergencyContact("admin", "Admin", "Station Administrator", admin),
        EmergencyContact("vishwa", "Vishwa", "Emergency Responder", vishwa),
        EmergencyContact("gokul", "Gokul", "Safety Officer", gokul)
    )
}

enum class AiCallScript(val id: String, val title: String, val description: String, val text: String) {
    SCRIPT_1_URGENT(
        id = "script_1",
        title = "Script 1: Urgent Station Dispatch",
        description = "Recommended for fire emergencies - Calls Vishwa, Gokul & Admin to station",
        text = "Emergency Alert for VediRaksha! High-priority notice. A critical fire hazard has been detected at the monitoring station. Repeat, fire caught at the station! Station status is in extreme danger. Urgent request for Vishwa, Gokul, and Admin: Please proceed to the station location immediately or dispatch emergency assistance! Immediate evacuation and response protocols are active. Take action now!"
    ),
    SCRIPT_2_DETAILED(
        id = "script_2",
        title = "Script 2: Multi-Responder Detailed Alert",
        description = "Detailed notice requesting immediate arrival at station",
        text = "Attention Admin, Vishwa, and Gokul. This is the VediRaksha automated emergency voice dispatch system. Fire hazard detected at the pyrotechnic station. Station is in severe danger. Immediate on-site presence or emergency team dispatch is urgently required. Please log into the dashboard or rush to the station location immediately."
    ),
    SCRIPT_3_INTERACTIVE(
        id = "script_3",
        title = "Script 3: Interactive DTMF Dispatch",
        description = "Prompts Vishwa, Gokul, or Admin input via DTMF keypad",
        text = "Urgent alert from VediRaksha system! Fire detected at the station. Station in critical danger. Vishwa, Gokul, Admin: Press 1 to acknowledge alert and confirm you are rushing to the station, or press 9 to repeat this message."
    )
}

enum class AiCallState {
    IDLE,
    DIALING,
    IN_CALL_SPEAKING,
    WAITING_FOR_INPUT,
    CALL_ENDED
}

class AiCallAgentManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val prefs = context.getSharedPreferences("appearance_preferences", Context.MODE_PRIVATE)

    private val _callState = MutableStateFlow(AiCallState.IDLE)
    val callState: StateFlow<AiCallState> = _callState.asStateFlow()

    private val _currentScript = MutableStateFlow(AiCallScript.SCRIPT_1_URGENT)
    val currentScript: StateFlow<AiCallScript> = _currentScript.asStateFlow()

    private val _targetPhoneNumber = MutableStateFlow(prefs.getString("admin_phone", "9500847066") ?: "9500847066")
    val targetPhoneNumber: StateFlow<String> = _targetPhoneNumber.asStateFlow()

    private val _activeContactName = MutableStateFlow("Admin")
    val activeContactName: StateFlow<String> = _activeContactName.asStateFlow()

    private val _lastAiResponse = MutableStateFlow("")
    val lastAiResponse: StateFlow<String> = _lastAiResponse.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("AiCallAgentManager", "Language not supported for TTS")
            } else {
                isTtsInitialized = true
                tts?.setSpeechRate(0.95f)
                tts?.setPitch(1.0f)
                setupProgressListener()
            }
        } else {
            Log.e("AiCallAgentManager", "TTS initialization failed: $status")
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _callState.value = AiCallState.IN_CALL_SPEAKING
            }

            override fun onDone(utteranceId: String?) {
                if (_currentScript.value == AiCallScript.SCRIPT_3_INTERACTIVE) {
                    _callState.value = AiCallState.WAITING_FOR_INPUT
                } else {
                    _callState.value = AiCallState.IN_CALL_SPEAKING
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _callState.value = AiCallState.IDLE
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _callState.value = AiCallState.IDLE
            }
        })
    }

    fun selectContact(contact: EmergencyContact) {
        _targetPhoneNumber.value = contact.phone
        _activeContactName.value = "${contact.name} (${contact.role})"
    }

    fun selectAllContacts() {
        val contacts = getEmergencyContacts(context)
        val allPhones = contacts.joinToString(", ") { it.phone }
        _targetPhoneNumber.value = allPhones
        _activeContactName.value = "All Responders (${contacts.joinToString(", ") { it.name }})"
    }

    fun setTargetPhoneNumber(phone: String, name: String = "Admin") {
        val savedAdmin = prefs.getString("admin_phone", phone) ?: phone
        _targetPhoneNumber.value = savedAdmin
        _activeContactName.value = name
    }

    fun selectScript(script: AiCallScript) {
        _currentScript.value = script
    }

    fun startAiVoiceCall(script: AiCallScript = _currentScript.value) {
        _currentScript.value = script
        _callState.value = AiCallState.DIALING
        _lastAiResponse.value = script.text

        speakText(script.text)
    }

    fun handleDtmfInput(digit: String) {
        when (digit) {
            "1" -> {
                val response = "Alert acknowledged! Emergency dispatch confirmed. Vishwa, Gokul, and Admin have been notified to rush to the station immediately. Stay safe!"
                _lastAiResponse.value = response
                speakText(response)
            }
            "9" -> {
                speakText(_currentScript.value.text)
            }
            else -> {
                val response = "Invalid key option. Press 1 to acknowledge, or press 9 to repeat the alert."
                _lastAiResponse.value = response
                speakText(response)
            }
        }
    }

    fun speakText(text: String) {
        if (isTtsInitialized) {
            _callState.value = AiCallState.IN_CALL_SPEAKING
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "vediraksha_ai_call_" + System.currentTimeMillis())
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "vediraksha_ai_call_" + System.currentTimeMillis())
        } else {
            Log.w("AiCallAgentManager", "TTS not initialized yet")
        }
    }

    fun stopCall() {
        tts?.stop()
        _callState.value = AiCallState.CALL_ENDED
    }

    fun dialPhoneIntent(phoneNumber: String = _targetPhoneNumber.value) {
        val firstPhone = phoneNumber.split(",").firstOrNull()?.trim() ?: "9500847066"
        val formattedNumber = firstPhone.replace(Regex("[^0-9+]"), "")
        val uri = Uri.parse("tel:$formattedNumber")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isTtsInitialized = false
    }
}
