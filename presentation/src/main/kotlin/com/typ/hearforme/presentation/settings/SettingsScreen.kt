package com.typ.hearforme.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.hearforme.designsystem.theme.PrimaryBlue
import com.typ.hearforme.presentation.dashboard.getEmojiForType
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sound Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Global Detection Toggle
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Listening", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Continuous background detection",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isGlobalDetectionEnabled,
                        onCheckedChange = { viewModel.toggleGlobalDetection(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = PrimaryBlue)
                    )
                }
            }

            // Hardware Alerts Card
            Text("Alert Methods", style = MaterialTheme.typography.labelLarge, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            SettingsCard {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SettingToggleItem(
                        title = "Flashlight Strobe",
                        subtitle = "Blink flash on detection",
                        checked = uiState.isFlashlightEnabled,
                        onCheckedChange = { viewModel.toggleFlashlight(it) }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingToggleItem(
                        title = "Vibration Pattern",
                        subtitle = "Haptic feedback for alerts",
                        checked = uiState.isVibrationEnabled,
                        onCheckedChange = { viewModel.toggleVibration(it) }
                    )
                }
            }

            // Per-Sound Customization
            Text("Detection Sensitivity", style = MaterialTheme.typography.labelLarge, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            uiState.soundSettings.forEach { setting ->
                SoundSettingItem(
                    setting = setting,
                    onToggle = { viewModel.toggleSoundType(setting.type, it) },
                    onSensitivityChange = { viewModel.updateSensitivity(setting.type, it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun SettingToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = PrimaryBlue)
        )
    }
}

@Composable
fun SoundSettingItem(
    setting: SoundSetting,
    onToggle: (Boolean) -> Unit,
    onSensitivityChange: (Float) -> Unit,
) {
    SettingsCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getEmojiForType(setting.type), fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(setting.type.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = setting.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedTrackColor = PrimaryBlue)
                )
            }

            if (setting.isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sensitivity", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Slider(
                        value = setting.sensitivity,
                        onValueChange = onSensitivityChange,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
                    )
                }
            }
        }
    }
}
