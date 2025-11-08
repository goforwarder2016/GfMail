package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.PerformanceMode
import com.gf.mail.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing performance optimization settings
 */
class ManagePerformanceOptimizationUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Get current performance mode
     */
    suspend fun getPerformanceMode(): Flow<PerformanceMode> {
        // TODO: Implement get performance mode
        return kotlinx.coroutines.flow.flowOf(PerformanceMode.BALANCED)
    }

    /**
     * Set performance mode
     */
    suspend fun setPerformanceMode(mode: PerformanceMode) {
        // TODO: Implement set performance mode
    }
}