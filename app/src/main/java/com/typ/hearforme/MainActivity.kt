package com.typ.hearforme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.typ.hearforme.designsystem.theme.HearForMeTheme
import com.typ.hearforme.di.appModule
import com.typ.hearforme.presentation.navigation.MainNavigation
import com.typ.hearforme.service.SoundDetectionService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

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

@Preview
@Composable
private fun MainActivityPreview() {
    val ctx = LocalContext.current
    startKoin {
        androidLogger()
        androidContext(ctx)
        modules(appModule)
    }
    HearForMeTheme {
        MainNavigation()
    }
}