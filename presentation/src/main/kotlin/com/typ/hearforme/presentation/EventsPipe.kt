package com.typ.hearforme.presentation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class Event {
    object None : Event()
    data class Error(val title: String, val message: String) : Event()
}

object EventsPipe {
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    fun sendEvent(event: Event) {
        _events.tryEmit(event)
    }
}
