package com.hilight.control.data

enum class HiLightEffect {
    SOLID,
    PULSE,
    BREATHE,
    WAVE,
    COMET,
    RAINBOW,
}

data class AppRule(
    val packageName: String,
    val enabled: Boolean = true,
    val colorArgb: Int = 0xFF4CAF50.toInt(),
    val effect: HiLightEffect = HiLightEffect.PULSE,
    val durationSeconds: Int = 12,
)
