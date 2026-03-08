package com.typ.hearforme.domain.interceptors

import com.typ.hearforme.domain.model.SoundEvent

/**
 * Defines a contract for intercepting and potentially transforming [SoundEvent] instances
 * within the classification pipeline.
 *
 * Interceptors can be used to augment event data, filter specific events, or apply
 * conditional logic before the event reaches its final destination.
 */
interface ClassifierInterceptor {

    /**
     * Intercepts the given [SoundEvent] to perform processing, transformation, or filtering.
     *
     * @param event The original sound event to be intercepted.
     * @return The processed or modified sound event to be passed to the next stage,
     *         or null if the event should be dropped.
     */
    fun intercept(event: SoundEvent): SoundEvent

}