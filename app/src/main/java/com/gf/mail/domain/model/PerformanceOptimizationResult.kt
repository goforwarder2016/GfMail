package com.gf.mail.domain.model

/**
 * Data class representing the result of performance optimization
 */
data class PerformanceOptimizationResult(
    val success: Boolean,
    val optimizationsApplied: List<String>,
    val performanceGain: Float,
    val memorySaved: Long = 0L,
    val batterySaved: Float = 0f,
    val errors: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)