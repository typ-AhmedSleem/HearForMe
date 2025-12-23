package com.typ.hearforme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.hearforme.designsystem.theme.HearForMeTheme
import com.typ.hearforme.di.appModule
import com.typ.hearforme.presentation.main.MainViewModel
import com.typ.hearforme.presentation.navigation.MainNavigation
import com.typ.hearforme.presentation.preview.PreviewContainer
import com.typ.hearforme.service.SoundDetectionService
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
            val isDetectionEnabled by viewModel.isDetectionEnabled.collectAsStateWithLifecycle()

            LaunchedEffect(hasCompletedOnboarding, isDetectionEnabled) {
                if (hasCompletedOnboarding && isDetectionEnabled && hasPermissions()) {
                    startDetectionService()
                } else {
                    stopDetectionService()
                }
            }

            HearForMeTheme {
                MainNavigation()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startDetectionService() {
        val intent = Intent(this, SoundDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopDetectionService() {
        stopService(Intent(this, SoundDetectionService::class.java))
    }
}

@Preview
@Composable
private fun MainActivityPreview() {
    PreviewContainer(modules = listOf(appModule)) {
        MainNavigation()
    }
}