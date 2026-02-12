package com.typ.hearforme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
                val canStartService = hasCompletedOnboarding && isDetectionEnabled && hasPermissions()
                Log.d(
                    "HearForMe",
                    "Can start service: result='$canStartService' | hasCompletedOnboarding=$hasCompletedOnboarding | isDetectionEnabled=$isDetectionEnabled | hasPermissions=${hasPermissions()}"
                )
                if (canStartService) {
                    Log.d("HearForMe", "Starting service...")
                    startDetectionService()
                } else {
                    Log.d("HearForMe", "Stopping service...")
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
//        viewModel.startDetection()
        startService(Intent(this, SoundDetectionService::class.java))
    }

    private fun stopDetectionService() {
//        viewModel.stopDetection()
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