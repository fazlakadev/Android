package com.fazlaka.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.fazlaka.app.analytics.AnalyticsTracker
import com.fazlaka.app.analytics.Screens
import com.fazlaka.app.core.location.LocationProvider
import com.fazlaka.app.ui.components.UpdateDialog
import com.fazlaka.app.ui.navigation.FazlakaNavGraph
import com.fazlaka.app.ui.theme.FazlakaTheme
import com.fazlaka.app.ui.viewmodel.MainViewModel
import com.fazlaka.app.ui.viewmodel.UpdateState
import com.fazlaka.app.ui.viewmodel.UpdateViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationProvider: LocationProvider

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    private var pendingNotificationData by mutableStateOf<Map<String, String>?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* granted or denied — no action needed, Android handles silently */ }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        val tag = newBase
            .getSharedPreferences("fazlaka_locale_sync", android.content.Context.MODE_PRIVATE)
            .getString("locale", "ar") ?: "ar"
        val locale = java.util.Locale.forLanguageTag(tag)
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        analyticsTracker.logScreenView(Screens.HOME)
        locationProvider.start()
        requestNotificationPermissionIfNeeded()

        pendingNotificationData = extractNotificationData(intent)

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val sessionState by viewModel.uiState.collectAsStateWithLifecycle()

            val updateViewModel: UpdateViewModel = hiltViewModel()
            val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                updateViewModel.checkForUpdates()
            }

            FazlakaTheme(darkTheme = sessionState.darkMode) {
                val startDestination = remember(sessionState.isLoggedIn, sessionState.onboarded) {
                    when {
                        sessionState.isLoggedIn -> com.fazlaka.app.ui.navigation.Routes.MAIN
                        sessionState.onboarded -> com.fazlaka.app.ui.navigation.Routes.LOGIN
                        else -> com.fazlaka.app.ui.navigation.Routes.ONBOARDING
                    }
                }
                FazlakaNavGraph(
                    startDestination = startDestination,
                    notificationData = pendingNotificationData,
                    onNotificationHandled = { pendingNotificationData = null },
                )

                val updateInfo = updateViewModel.currentUpdateInfo.collectAsStateWithLifecycle().value
                when (val state = updateState) {
                    is UpdateState.Available, is UpdateState.Downloading, is UpdateState.DownloadProgress -> {
                        if (updateInfo != null) {
                            UpdateDialog(
                                updateInfo = updateInfo,
                                currentVersion = updateViewModel.currentVersion,
                                onDownload = { updateViewModel.downloadAndInstall() },
                                onDismiss = { updateViewModel.dismissUpdate() },
                                onSkip = { updateViewModel.skipVersion() },
                                isDownloading = state is UpdateState.Downloading || state is UpdateState.DownloadProgress,
                                downloadProgress = if (state is UpdateState.DownloadProgress) state.progress else 0f,
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationData = extractNotificationData(intent)
    }

    private fun extractNotificationData(intent: Intent?): Map<String, String>? {
        if (intent == null) return null
        val type = intent.getStringExtra("notification_type") ?: return null
        return buildMap {
            put("type", type)
            intent.getStringExtra("notification_id")?.let { put("notificationId", it) }
            intent.getStringExtra("content_type")?.let { put("contentType", it) }
            intent.getStringExtra("content_id")?.let { put("contentId", it) }
            intent.getStringExtra("conversation_id")?.let { put("conversationId", it) }
            intent.getStringExtra("user_id")?.let { put("userId", it) }
        }
    }
}
