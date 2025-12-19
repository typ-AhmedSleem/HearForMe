package com.typ.hearforme.di

import com.typ.hearforme.ai.MediaPipeAudioClassifier
import com.typ.hearforme.data.repository.SettingsRepositoryImpl
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.policy.DefaultDetectionPolicy
import com.typ.hearforme.domain.policy.DetectionPolicy
import com.typ.hearforme.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Domain
    single<DetectionPolicy> { DefaultDetectionPolicy() }

    // Data
    single<SettingsRepository> { SettingsRepositoryImpl(androidContext()) }

    // AI
    single<AudioClassifier> { MediaPipeAudioClassifier(androidContext()) }
}
