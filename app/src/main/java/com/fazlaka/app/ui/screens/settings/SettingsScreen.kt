package com.fazlaka.app.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.BuildConfig
import com.fazlaka.app.R
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection
import com.fazlaka.app.ui.components.SettingsItem
import com.fazlaka.app.ui.components.SettingsSection
import com.fazlaka.app.ui.components.VerticalSpacer
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity

    fun pickLocale(code: String) {
        viewModel.setLocale(code) {
            // Recreate applies the new locale instantly (attachBaseContext).
            activity?.recreate()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.st_hero_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        HeroSection(
            title = stringResource(R.string.st_hero_title),
            subtitle = stringResource(R.string.st_hero_sub),
            badge = stringResource(R.string.st_hero_badge),
            accent = HeroAccents.Settings,
            minHeight = 112.dp,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        VerticalSpacer(height = 12.dp)

        SettingsSection(title = stringResource(R.string.st_appearance)) {
            SettingsItem(
                icon = Icons.Default.DarkMode,
                label = stringResource(R.string.st_dark_mode),
                trailing = {
                    Switch(
                        checked = state.darkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedBorderColor = MaterialTheme.colorScheme.primary,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier.size(42.dp, 24.dp),
                    )
                },
            )
        }

        SettingsSection(title = stringResource(R.string.st_language)) {
            LanguageOption(stringResource(com.fazlaka.app.R.string.st_language_arabic), "ar", state.locale) { pickLocale("ar") }
            LanguageOption("Français", "fr", state.locale) { pickLocale("fr") }
            LanguageOption("English", "en", state.locale) { pickLocale("en") }
        }

        SettingsSection(title = stringResource(R.string.st_account_security)) {
            SettingsItem(
                icon = Icons.Default.Person,
                label = stringResource(R.string.st_edit_profile),
                onClick = { onNavigate(Routes.EDIT_PROFILE) },
            )
            SettingsItem(
                icon = Icons.Default.Security,
                label = stringResource(R.string.st_security_sessions),
                onClick = { onNavigate(Routes.SECURITY) },
            )
            SettingsItem(
                icon = Icons.Default.SupportAgent,
                label = stringResource(R.string.st_support),
                onClick = { onNavigate(Routes.SUPPORT) },
            )
            BiometricSetting()
        }

        SettingsSection(title = stringResource(R.string.st_about)) {
            SettingsItem(
                icon = Icons.Default.Info,
                label = stringResource(R.string.st_version),
                trailing = {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = null,
            )
        }

        VerticalSpacer(height = 24.dp)
        Text(
            text = stringResource(R.string.st_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        VerticalSpacer(height = 24.dp)
    }
}

@Composable
private fun LanguageOption(
    label: String,
    code: String,
    current: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            RadioButton(
                selected = current == code,
                onClick = { onClick() },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}
