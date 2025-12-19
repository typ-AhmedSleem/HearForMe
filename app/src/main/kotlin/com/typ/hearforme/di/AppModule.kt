package com.typ.hearforme.di

import com.typ.hearforme.ai.MediaPipeAudioClassifier
import com.typ.hearforme.data.repository.SettingsRepositoryImpl
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.policy.DefaultDetectionPolicy
import com.typ.hearforme.domain.policy.DetectionPolicy
import com.typ.hearforme.domain.repository.SettingsRepository
import com.typ.hearforme.manager.AlertManagerImpl
import com.typ.hearforme.presentation.dashboard.DashboardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Domain
    single<DetectionPolicy> { DefaultDetectionPolicy() }
    single<AlertManager> { AlertManagerImpl(androidContext(), get()) }

    // Presentation
    viewModel { DashboardViewModel(get()) }

    // Data
    single<SettingsRepository> { SettingsRepositoryImpl(androidContext()) }

    // AI
    single<AudioClassifier> { MediaPipeAudioClassifier(androidContext()) }
}
