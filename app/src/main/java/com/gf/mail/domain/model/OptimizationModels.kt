package com.gf.mail.domain.model

/**
 * Optimization result sealed class
 */
sealed class OptimizationResult {
    data class Loading(val message: String) : OptimizationResult()
    data class Progress(val message: String, val percentage: Int) : OptimizationResult()
    data class Success(val message: String, val details: Map<String, Any> = emptyMap()) : OptimizationResult()
    data class Error(val message: String, val error: Throwable? = null) : OptimizationResult()
    data class Warning(val message: String, val details: Map<String, Any> = emptyMap()) : OptimizationResult()
}