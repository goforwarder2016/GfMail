package com.gf.mail.di.modules

import com.gf.mail.data.cache.CacheManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing cache-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    
    @Provides
    @Singleton
    fun provideCacheManager(cacheManager: CacheManager): CacheManager {
        return cacheManager
    }
}