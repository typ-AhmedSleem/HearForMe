package com.typ.hearforme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.typ.hearforme.designsystem.theme.HearForMeTheme
import com.typ.hearforme.di.appModule
import com.typ.hearforme.presentation.navigation.MainNavigation
import com.typ.hearforme.presentation.preview.PreviewContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HearForMeTheme {
                MainNavigation()
            }
        }
    }
}

@Preview
@Composable
private fun MainActivityPreview() {
    PreviewContainer(modules = listOf(appModule)) {
        MainNavigation()
    }
}