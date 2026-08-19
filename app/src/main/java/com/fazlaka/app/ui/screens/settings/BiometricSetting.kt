package com.fazlaka.app.ui.screens.settings

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.size
import com.fazlaka.app.ui.components.SettingsItem
import com.fazlaka.app.ui.screens.auth.BiometricHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.biometricDataStore by preferencesDataStore(name = "fazlaka_biometric")

private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")

@HiltViewModel
class BiometricSettingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val biometricEnabled: StateFlow<Boolean> = context.biometricDataStore.data
        .map { it[KEY_BIOMETRIC_ENABLED] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.biometricDataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
        }
    }
}

/**
 * Toggle switch that lets the user enable / disable biometric login.
 * The preference is persisted via DataStore.
 */
@Composable
fun BiometricSetting(
    modifier: Modifier = Modifier,
    viewModel: BiometricSettingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val enabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()

    SettingsItem(
        icon = Icons.Default.Fingerprint,
        label = "Biometric login",
        trailing = {
            Switch(
                checked = enabled,
                onCheckedChange = { desired ->
                    if (desired && !BiometricHelper.canAuthenticate(context)) {
                        // Device doesn't support biometrics — ignore toggle
                        return@Switch
                    }
                    viewModel.setEnabled(desired)
                },
                colors = SwitchDefaults.colors(
                    checkedBorderColor = MaterialTheme.colorScheme.primary,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                modifier = Modifier.size(42.dp, 24.dp),
            )
        },
        modifier = modifier,
    )
}
