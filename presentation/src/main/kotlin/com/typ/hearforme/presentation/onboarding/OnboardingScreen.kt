package com.typ.hearforme.presentation.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.hearforme.designsystem.theme.PrimaryBlue

@Composable
fun OnboardingScreen(
    step: Int,
    onNext: () -> Unit,
) {
    LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val microphoneGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (microphoneGranted) {
            onNext()
        }
    }

    val title = when (step) {
        1 -> "Sound Detection"
        2 -> "Instant Notifications"
        3 -> "Essential Permissions"
        else -> "You're All Set!"
    }

    val description = when (step) {
        1 -> "Our AI identifies warning sounds like baby crying, doorbells, and alarms in real-time."
        2 -> "Get notified through visual flashes, vibration patterns, and system alerts."
        3 -> "We need access to your microphone and notifications to keep you safe."
        else -> "Start hearing the world through your eyes and touch."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Graphic Placeholder
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (step) {
                    1 -> "🔍"
                    2 -> "🔔"
                    3 -> "🛡️"
                    else -> "🚀"
                },
                fontSize = 80.sp
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Progress Dots
        Row {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .padding(horizontal = 2.dp)
                        .background(
                            if (index + 1 == step) PrimaryBlue else Color.LightGray,
                            CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (step == 3) {
                    val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    // Android 14+ requires FOREGROUND_SERVICE_MICROPHONE at runtime
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        permissions.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
                    }
                    permissionLauncher.launch(permissions.toTypedArray())
                } else {
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                if (step < 4) "Next" else "Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
