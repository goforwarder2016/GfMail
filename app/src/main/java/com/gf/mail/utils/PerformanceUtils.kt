package com.gf.mail.utils

import android.util.Log
import kotlin.system.measureTimeMillis

/**
 * Utility class for performance monitoring and optimization
 */
class PerformanceUtils {

    companion object {
        private const val TAG = "PerformanceUtils"
    }

    /**
     * Start app startup tracing
     */
    fun startAppStartupTracing() {
        Log.d(TAG, "App startup tracing started.")
    }

    /**
     * Measure execution time of a block
     */
    fun <T> measureTime(block: () -> T): T {
        var result: T
        val time = measureTimeMillis {
            result = block()
        }
        Log.d(TAG, "Execution time: ${time}ms")
        return result
    }

    /**
     * Log memory usage
     */
    fun logMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        Log.d(TAG, "Memory usage: ${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB")
    }

    /**
     * Check if device is low on memory
     */
    fun isLowMemory(): Boolean {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        return (usedMemory.toDouble() / maxMemory.toDouble()) > 0.8
    }
}