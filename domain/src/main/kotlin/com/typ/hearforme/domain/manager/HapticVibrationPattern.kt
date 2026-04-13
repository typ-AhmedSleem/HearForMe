package com.typ.hearforme.domain.manager


sealed class HapticVibrationPattern(
    val pattern: LongArray,
    val repeatCount: Int = -1,
    val amplitude: IntArray,
) {

    /**
     * repeat: infinitely.
     * strength: medium.
     */
    data object InteractionPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 100),
        amplitude = intArrayOf(0, 255)
    )

    /**
     * repeat: infinitely.
     * strength: strong.
     */
    data object LowPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 200, 0, 200),
        amplitude = intArrayOf(0, 255, 0, 255)
    )

    /**
     * repeat: infinitely.
     * strength: strong.
     */
    data object NormalPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 300, 150, 300, 150, 300),
        amplitude = intArrayOf(0, 255, 0, 255, 0, 255)
    )

    /**
     * repeat: infinitely.
     * strength: strong.
     */
    data object HighPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 500, 100, 500, 100, 500, 100, 500),
        amplitude = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
    )

    /**
     * repeat: infinitely.
     * strength: strong.
     */
    data object CriticalPriority : HapticVibrationPattern(
        pattern = longArrayOf(0, 500, 50, 500, 50, 500, 50, 500, 50, 500),
        amplitude = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255),
    )

}