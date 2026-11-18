package com.example

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.AppUpdateUiState
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel
import com.example.util.LanguageManager
import com.example.util.tr
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var updateLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var updateFlowRunning = false
    private var promptedUpdateVersion: Int? = null
    private var updateUiState by mutableStateOf(
        AppUpdateUiState(message = tr("புதுப்பிப்புகள் தானாகச் சரிபார்க்கப்படும்.", "Updates are checked automatically."))
    )

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                updateUiState = AppUpdateUiState(
                    message = tr("புதுப்பிப்பு பதிவிறக்கப்பட்டது; நிறுவப்படுகிறது...", "Update downloaded; installing..."),
                    isChecking = true
                )
                appUpdateManager.completeUpdate()
                    .addOnFailureListener { error ->
                        Log.e("PlayInAppUpdate", "Could not complete downloaded update", error)
                        updateUiState = AppUpdateUiState(
                            message = tr(
                                "புதுப்பிப்பு பதிவிறக்கப்பட்டது. நிறுவ மீண்டும் திறக்கவும்.",
                                "Update downloaded. Reopen the app to finish installing."
                            )
                        )
                    }
            }
            InstallStatus.FAILED -> {
                updateFlowRunning = false
                updateUiState = AppUpdateUiState(
                    message = tr("புதுப்பிப்பை நிறுவ முடியவில்லை.", "The update could not be installed.")
                )
            }
            InstallStatus.CANCELED -> {
                updateFlowRunning = false
                updateUiState = AppUpdateUiState(
                    message = tr("புதுப்பிப்பு ரத்து செய்யப்பட்டது.", "The update was cancelled.")
                )
            }
            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.initialize(this)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        updateLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            updateFlowRunning = false
            updateUiState = when (result.resultCode) {
                Activity.RESULT_OK -> AppUpdateUiState(
                    message = tr("புதுப்பிப்பு பதிவிறக்கம் தொடங்கியது.", "Update download started."),
                    isChecking = true
                )
                Activity.RESULT_CANCELED -> AppUpdateUiState(
                    message = tr("புதுப்பிப்பு ரத்து செய்யப்பட்டது.", "The update was cancelled.")
                )
                else -> AppUpdateUiState(
                    message = tr("புதுப்பிப்பைத் தொடங்க முடியவில்லை.", "Could not start the update.")
                )
            }
        }
        appUpdateManager.registerListener(installStateListener)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val accent by viewModel.themeAccent.collectAsState()
            MyApplicationTheme(themeMode = themeMode, accent = accent) {
                MainScreen(
                    viewModel = viewModel,
                    updateUiState = updateUiState,
                    onCheckForUpdates = { checkForAppUpdate(userInitiated = true) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::appUpdateManager.isInitialized) return
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                when {
                    info.installStatus() == InstallStatus.DOWNLOADED -> completeDownloadedUpdate()
                    info.installStatus() == InstallStatus.DOWNLOADING ||
                        info.installStatus() == InstallStatus.PENDING ||
                        info.installStatus() == InstallStatus.INSTALLING -> Unit
                    info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                        startUpdateFlow(info)
                    else -> checkForAppUpdate(userInitiated = false, existingInfo = info)
                }
            }
            .addOnFailureListener { error ->
                Log.w("PlayInAppUpdate", "Automatic Play update check failed", error)
            }
    }

    override fun onDestroy() {
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.unregisterListener(installStateListener)
        }
        super.onDestroy()
    }

    private fun checkForAppUpdate(userInitiated: Boolean, existingInfo: AppUpdateInfo? = null) {
        if (updateFlowRunning) return
        updateUiState = AppUpdateUiState(
            message = tr("புதுப்பிப்புகளைச் சரிபார்க்கிறது...", "Checking for updates..."),
            isChecking = true
        )

        if (existingInfo != null) {
            handleUpdateInfo(existingInfo, userInitiated)
            return
        }

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info -> handleUpdateInfo(info, userInitiated) }
            .addOnFailureListener { error ->
                Log.w("PlayInAppUpdate", "Play update check failed", error)
                updateUiState = AppUpdateUiState(
                    message = tr(
                        "Google Play-இல் புதுப்பிப்புகளைச் சரிபார்க்க முடியவில்லை.",
                        "Couldn't check for updates on Google Play."
                    )
                )
            }
    }

    private fun handleUpdateInfo(info: AppUpdateInfo, userInitiated: Boolean) {
        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
            updateUiState = AppUpdateUiState(
                message = tr("நீங்கள் சமீபத்திய பதிப்பைப் பயன்படுத்துகிறீர்கள்.", "You're using the latest version.")
            )
            return
        }
        if (!userInitiated && promptedUpdateVersion == info.availableVersionCode()) {
            updateUiState = AppUpdateUiState(
                message = tr("புதிய புதுப்பிப்பு Google Play-இல் உள்ளது.", "An update is available on Google Play.")
            )
            return
        }
        if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            updateUiState = AppUpdateUiState(
                message = tr(
                    "புதுப்பிப்பு உள்ளது; Google Play Store-இல் இருந்து புதுப்பிக்கவும்.",
                    "An update is available. Update from the Google Play Store."
                )
            )
            return
        }
        promptedUpdateVersion = info.availableVersionCode()
        startUpdateFlow(info)
    }

    private fun startUpdateFlow(info: AppUpdateInfo) {
        if (updateFlowRunning) return
        updateFlowRunning = true
        updateUiState = AppUpdateUiState(
            message = tr("Google Play புதுப்பிப்பு தொடங்குகிறது...", "Starting the Google Play update..."),
            isChecking = true
        )
        try {
            val flowStarted = appUpdateManager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            )
            if (!flowStarted) {
                updateFlowRunning = false
                updateUiState = AppUpdateUiState(
                    message = tr(
                        "புதுப்பிப்பைத் தொடங்க முடியவில்லை. Google Play Store-ஐத் திறக்கவும்.",
                        "Could not start the update. Open the Google Play Store."
                    )
                )
            }
        } catch (error: IntentSender.SendIntentException) {
            Log.e("PlayInAppUpdate", "Could not launch Play update UI", error)
            updateFlowRunning = false
            updateUiState = AppUpdateUiState(
                message = tr(
                    "புதுப்பிப்பைத் தொடங்க முடியவில்லை. பின்னர் மீண்டும் முயற்சிக்கவும்.",
                    "Could not start the update. Please try again later."
                )
            )
        }
    }

    private fun completeDownloadedUpdate() {
        appUpdateManager.completeUpdate()
            .addOnFailureListener { error ->
                Log.e("PlayInAppUpdate", "Could not finish downloaded update", error)
                updateUiState = AppUpdateUiState(
                    message = tr(
                        "புதுப்பிப்பு பதிவிறக்கப்பட்டது. நிறுவ மீண்டும் திறக்கவும்.",
                        "Update downloaded. Reopen the app to finish installing."
                    )
                )
            }
    }
}
