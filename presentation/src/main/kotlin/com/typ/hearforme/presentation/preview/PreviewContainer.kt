package com.typ.hearforme.presentation.preview

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.typ.hearforme.designsystem.theme.HearForMeTheme
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.Module

@Composable
fun PreviewContainer(
    darkTheme: Boolean = isSystemInDarkTheme(),
    modules: List<Module> = emptyList(),
    content: @Composable () -> Unit,
) {
    val ctx = LocalContext.current
    startKoinSafely(ctx, modules)

    HearForMeTheme(darkTheme = darkTheme) {
        content()
    }
}

private fun startKoinSafely(ctx: Context, modules: List<Module>) {
    runCatching {
        startKoin {
            androidLogger()
            androidContext(ctx)
            modules(modules)
        }
    }
}