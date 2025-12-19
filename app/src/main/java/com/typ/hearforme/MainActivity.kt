package com.typ.hearforme

import MainNavigation
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.typ.hearforme.designsystem.theme.HearForMeTheme
import com.typ.hearforme.service.SoundDetectionService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HearForMeTheme {
                MainNavigation()
            }
        }

        startService(Intent(this, SoundDetectionService::class.java))
    }
}