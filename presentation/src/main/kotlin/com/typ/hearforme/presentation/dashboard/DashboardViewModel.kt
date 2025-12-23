package com.typ.hearforme.presentation.dashboard

import androidx.lifecycle.ViewModel
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.model.SoundEvent
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel(
    private val alertManager: AlertManager,
) : ViewModel() {

    val activeAlert: StateFlow<SoundEvent?> = alertManager.activeAlert
    val history: StateFlow<List<SoundEvent>> = alertManager.history

    fun dismissAlert() {
        alertManager.dismissAlert()
    }
}
