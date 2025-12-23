package com.typ.hearforme.domain.repository

import com.typ.hearforme.domain.model.SoundEvent
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(): Flow<List<SoundEvent>>
    suspend fun saveEvent(event: SoundEvent)
    suspend fun clearHistory()
}
