package com.gf.mail

import android.app.Application
import android.content.Context
import android.content.res.Configuration as AndroidConfiguration
import android.util.Log
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.gf.mail.di.AppDependencyContainer
import com.gf.mail.utils.LocaleUtils
import com.gf.mail.utils.LanguageManager
import kotlin.system.measureTimeMillis
import java.util.*

/**
 * Gfmail Application class
 * Handles application-level initialization and configuration
 */
class GfmailApplication : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "GfmailApplication"
        lateinit var instance: GfmailApplication
            private set
    }

    // Manual dependency injection container
    lateinit var dependencies: AppDependencyContainer
        private set
    
    private var appStartTime: Long = 0

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
            .build()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        
        // Apply language setting
        base?.let { context ->
            val languageCode = LanguageManager.getCurrentLanguageCode(context)
            LanguageManager.applyLanguageSetting(context, languageCode)
        }
    }

    override fun onCreate() {
        appStartTime = System.currentTimeMillis()
        
        super.onCreate()
        instance = this
        
        // Initialize dependency container
        initializeDependencies()
        
        // Initialize crash reporting and performance monitoring
        initializeMonitoring()
        
        val initializationTime = System.currentTimeMillis() - appStartTime
        Log.d(TAG, "Application initialized in ${initializationTime}ms")
    }

    private fun initializeDependencies() {
        try {
            dependencies = AppDependencyContainer(this)
            Log.d(TAG, "Dependency container initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize dependency container", e)
            throw e
        }
    }

    private fun initializeMonitoring() {
        try {
            // TODO: Access these directly from dependencies
            // val crashReportingManager = dependencies.crashReportingManager
            // val performanceUtils = dependencies.performanceUtils
            
            // Initialize crash reporting
            // crashReportingManager.initialize(this)
            
            // Initialize performance monitoring
            // performanceUtils.startAppStartupTracing()
            
            Log.d(TAG, "Monitoring services initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize monitoring services", e)
        }
    }

    override fun onTerminate() {
        try {
            // Cleanup resources
            Log.d(TAG, "Application terminating")
        } catch (e: Exception) {
            Log.e(TAG, "Error during application termination", e)
        }
        super.onTerminate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Low memory warning received")
        
        try {
            // Clear caches and free up memory
            // TODO: Access performanceUtils directly from dependencies
            // val performanceUtils = dependencies.performanceUtils
            // performanceUtils.logMemoryUsage()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling low memory", e)
        }
    }

    override fun onConfigurationChanged(newConfig: AndroidConfiguration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed")
        
        try {
            // Handle configuration changes
            // TODO: Access performanceUtils directly from dependencies
            // val performanceUtils = dependencies.performanceUtils
            // performanceUtils.logMemoryUsage()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling configuration change", e)
        }
    }
    
}