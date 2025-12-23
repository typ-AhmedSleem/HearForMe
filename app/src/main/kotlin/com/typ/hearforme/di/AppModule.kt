package com.typ.hearforme.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.typ.hearforme.ai.MediaPipeAudioClassifier
import com.typ.hearforme.communication.AndroidSpeechToTextEngine
import com.typ.hearforme.communication.AndroidTextToSpeechEngine
import com.typ.hearforme.data.HistoryDatabase
import com.typ.hearforme.data.repository.HistoryRepositoryImpl
import com.typ.hearforme.data.repository.SettingsRepositoryImpl
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.communication.SpeechToTextEngine
import com.typ.hearforme.domain.communication.TextToSpeechEngine
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.manager.HapticEngine
import com.typ.hearforme.domain.repository.HistoryRepository
import com.typ.hearforme.domain.repository.SettingsRepository
import com.typ.hearforme.manager.AlertManagerImpl
import com.typ.hearforme.manager.HapticEngineImpl
import com.typ.hearforme.presentation.communication.CommunicationViewModel
import com.typ.hearforme.presentation.dashboard.DashboardViewModel
import com.typ.hearforme.presentation.main.MainViewModel
import com.typ.hearforme.presentation.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single<HistoryDatabase> {
        val driver = AndroidSqliteDriver(HistoryDatabase.Schema, androidContext(), "history.db")
        HistoryDatabase(driver)
    }

    // Managers
    single<HapticEngine> { HapticEngineImpl(androidContext()) }
    single<AlertManager> { AlertManagerImpl(androidContext(), get(), get(), get()) }

    // Presentation
    viewModel { MainViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get()) }
    viewModel { CommunicationViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }

    // Data
    single<SettingsRepository> { SettingsRepositoryImpl(androidContext()) }
    single<HistoryRepository> { HistoryRepositoryImpl(get()) }

    // AI
    single<AudioClassifier> { MediaPipeAudioClassifier(androidContext()) }

    // Communication
    single<SpeechToTextEngine> { AndroidSpeechToTextEngine(androidContext()) }
    single<TextToSpeechEngine> { AndroidTextToSpeechEngine(androidContext()) }
}
