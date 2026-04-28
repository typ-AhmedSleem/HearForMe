package com.typ.hearforme.domain.manager


sealed class HapticVibrationPattern(
    val pattern: LongArray,
    val repeatCount: Int = -1,
    val amplitude: IntArray,
) {

    /**
     * repeat: infinitely.
     * strength: strong.
     */
    data object InteractionPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 50),
        amplitude = intArrayOf(0, 255)
    )

    /**
     * repeat: infinitely.
     * strength: strong.
     */
    data object LowPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 250, 50, 250),
        repeatCount = 0,
        amplitude = intArrayOf(0, 200, 0, 200)
    )

    /**
     * repeat: infinitely.
     * strength: strong.
     */
    data object NormalPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 300, 100, 300, 100, 300),
        repeatCount = 0,
        amplitude = intArrayOf(0, 220, 0, 220, 0, 220)
    )

    /**
     * repeat: infinitely.
     * strength: strong.
     */
    data object HighPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 400, 100, 400, 100, 400, 100, 400),
        repeatCount = 0,
        amplitude = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
    )

    /**
     * repeat: infinitely.
     * strength: strong.
     */
    data object CriticalPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 600, 50, 600, 50, 600, 50, 600, 50, 600),
        repeatCount = 0,
        amplitude = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255),
    )

}