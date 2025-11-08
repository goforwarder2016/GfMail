package com.gf.mail.domain.model

/**
 * Performance monitoring models
 */

/**
 * Performance mode enum
 */
enum class PerformanceMode {
    BALANCED,
    HIGH_PERFORMANCE,
    BATTERY_SAVER
}

/**
 * Performance metrics data class
 */
data class PerformanceMetrics(
    val cpuUsage: Float,
    val memoryUsage: Float,
    val batteryUsage: Float,
    val networkLatency: Long,
    val syncTime: Long,
    val lastUpdated: Long
)

/**
 * Operation statistics
 */
data class OperationStats(
    val operationName: String,
    val averageTime: Long,
    val maxTime: Long,
    val minTime: Long,
    val totalCount: Int,
    val successCount: Int,
    val failureCount: Int,
    val lastExecuted: Long
)

/**
 * Performance alert
 */
data class PerformanceAlert(
    val id: String,
    val type: PerformanceAlertType,
    val severity: PerformanceAlertSeverity,
    val message: String,
    val timestamp: Long,
    val isResolved: Boolean
)

/**
 * Performance alert type
 */
enum class PerformanceAlertType {
    HIGH_CPU_USAGE,
    HIGH_MEMORY_USAGE,
    HIGH_BATTERY_USAGE,
    SLOW_SYNC,
    NETWORK_TIMEOUT,
    STORAGE_FULL
}

/**
 * Performance alert severity
 */
enum class PerformanceAlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Performance event
 */
data class PerformanceEvent(
    val id: String,
    val type: PerformanceEventType,
    val timestamp: Long,
    val duration: Long,
    val success: Boolean,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Performance event type
 */
enum class PerformanceEventType {
    EMAIL_SYNC,
    EMAIL_SEND,
    EMAIL_DELETE,
    ACCOUNT_AUTHENTICATION,
    FOLDER_SYNC,
    ATTACHMENT_DOWNLOAD,
    SEARCH_OPERATION,
    UI_RENDER
}

/**
 * Image quality enum
 */
enum class ImageQuality {
    LOW,
    MEDIUM,
    HIGH,
    ORIGINAL
}

/**
 * Sync frequency enum
 */
enum class SyncFrequency {
    MANUAL,
    EVERY_15_MINUTES,
    EVERY_30_MINUTES,
    EVERY_HOUR,
    EVERY_2_HOURS,
    EVERY_4_HOURS,
    EVERY_8_HOURS,
    EVERY_12_HOURS,
    DAILY
}

/**
 * Performance settings data class
 */
data class PerformanceSettings(
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
    val autoOptimization: Boolean = false,
    val batterySaverMode: Boolean = false,
    val reduceAnimations: Boolean = false,
    val imageQuality: ImageQuality = ImageQuality.MEDIUM,
    val syncFrequency: SyncFrequency = SyncFrequency.EVERY_30_MINUTES,
    val memoryOptimization: Boolean = false,
    val startupOptimization: Boolean = false
)

/**
 * Performance summary data class
 */
data class PerformanceSummary(
    val overallScore: Float,
    val cpuUsage: Float,
    val memoryUsage: Float,
    val batteryUsage: Float,
    val recommendations: List<String>
)

/**
 * Performance recommendation data class
 */
data class PerformanceRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val priority: PerformanceAlertSeverity,
    val actionRequired: Boolean = false
)

/**
 * Battery optimization result data class
 */
data class BatteryOptimizationResult(
    val suggestions: List<String>,
    val actionsToTake: List<String>,
    val estimatedSavings: Float
)