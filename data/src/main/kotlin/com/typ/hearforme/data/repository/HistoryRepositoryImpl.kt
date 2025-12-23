package com.typ.hearforme.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.typ.hearforme.data.HistoryDatabase
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import com.typ.hearforme.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(
    private val database: HistoryDatabase,
) : HistoryRepository {

    private val queries = database.historyQueries

    override fun getHistory(): Flow<List<SoundEvent>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    SoundEvent(
                        type = SoundType.fromLabel(entity.type),
                        confidence = entity.confidence.toFloat(),
                        timestamp = entity.timestamp
                    )
                }
            }
    }

    override suspend fun saveEvent(event: SoundEvent) {
        queries.insertEvent(
            type = event.type.label,
            confidence = event.confidence.toDouble(),
            timestamp = event.timestamp,
            metadata = null
        )
    }

    override suspend fun clearHistory() {
        queries.deleteAll()
    }
}
