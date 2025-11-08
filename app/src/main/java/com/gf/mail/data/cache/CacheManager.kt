package com.gf.mail.data.cache

import android.content.Context
import android.util.LruCache
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.model.Account
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central cache manager for optimizing app performance
 */
@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "CacheManager"
        private const val MAX_MEMORY_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
        private const val MAX_EMAIL_CACHE_SIZE = 1000
        private const val MAX_FOLDER_CACHE_SIZE = 100
        private const val CACHE_EXPIRY_TIME = 5 * 60 * 1000L // 5 minutes
    }
    
    // Memory caches
    private val emailCache = LruCache<String, CachedEmail>(MAX_EMAIL_CACHE_SIZE)
    private val folderCache = LruCache<String, CachedFolder>(MAX_FOLDER_CACHE_SIZE)
    private val accountCache = LruCache<String, CachedAccount>(50)
    
    // Cache statistics
    private val _cacheStats = MutableStateFlow(CacheStats())
    val cacheStats: StateFlow<CacheStats> = _cacheStats.asStateFlow()
    
    // Cache expiry tracking
    private val cacheTimestamps = ConcurrentHashMap<String, Long>()
    
    /**
     * Cache email data
     */
    fun cacheEmail(email: Email, accountId: String) {
        val cacheKey = "${accountId}_${email.id}"
        val cachedEmail = CachedEmail(
            email = email,
            timestamp = System.currentTimeMillis()
        )
        emailCache.put(cacheKey, cachedEmail)
        cacheTimestamps[cacheKey] = System.currentTimeMillis()
        updateCacheStats()
    }
    
    /**
     * Get cached email
     */
    fun getCachedEmail(emailId: String, accountId: String): Email? {
        val cacheKey = "${accountId}_${emailId}"
        val cachedEmail = emailCache.get(cacheKey)
        
        if (cachedEmail != null && !isExpired(cacheKey)) {
            return cachedEmail.email
        }
        
        // Remove expired cache entry
        if (cachedEmail != null) {
            emailCache.remove(cacheKey)
            cacheTimestamps.remove(cacheKey)
        }
        
        return null
    }
    
    /**
     * Cache folder data
     */
    fun cacheFolder(folder: EmailFolder, accountId: String) {
        val cacheKey = "${accountId}_${folder.id}"
        val cachedFolder = CachedFolder(
            folder = folder,
            timestamp = System.currentTimeMillis()
        )
        folderCache.put(cacheKey, cachedFolder)
        cacheTimestamps[cacheKey] = System.currentTimeMillis()
        updateCacheStats()
    }
    
    /**
     * Get cached folder
     */
    fun getCachedFolder(folderId: String, accountId: String): EmailFolder? {
        val cacheKey = "${accountId}_${folderId}"
        val cachedFolder = folderCache.get(cacheKey)
        
        if (cachedFolder != null && !isExpired(cacheKey)) {
            return cachedFolder.folder
        }
        
        // Remove expired cache entry
        if (cachedFolder != null) {
            folderCache.remove(cacheKey)
            cacheTimestamps.remove(cacheKey)
        }
        
        return null
    }
    
    /**
     * Cache account data
     */
    fun cacheAccount(account: Account) {
        val cachedAccount = CachedAccount(
            account = account,
            timestamp = System.currentTimeMillis()
        )
        accountCache.put(account.id, cachedAccount)
        cacheTimestamps[account.id] = System.currentTimeMillis()
        updateCacheStats()
    }
    
    /**
     * Get cached account
     */
    fun getCachedAccount(accountId: String): Account? {
        val cachedAccount = accountCache.get(accountId)
        
        if (cachedAccount != null && !isExpired(accountId)) {
            return cachedAccount.account
        }
        
        // Remove expired cache entry
        if (cachedAccount != null) {
            accountCache.remove(accountId)
            cacheTimestamps.remove(accountId)
        }
        
        return null
    }
    
    /**
     * Clear all caches
     */
    fun clearAllCaches() {
        emailCache.evictAll()
        folderCache.evictAll()
        accountCache.evictAll()
        cacheTimestamps.clear()
        updateCacheStats()
    }
    
    /**
     * Clear cache for specific account
     */
    fun clearAccountCache(accountId: String) {
        // Remove all cache entries for this account
        val keysToRemove = mutableListOf<String>()
        
        // Remove email cache entries
        for (key in emailCache.snapshot().keys) {
            if (key.startsWith("${accountId}_")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { emailCache.remove(it) }
        
        // Remove folder cache entries
        keysToRemove.clear()
        for (key in folderCache.snapshot().keys) {
            if (key.startsWith("${accountId}_")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { folderCache.remove(it) }
        
        // Remove account cache entry
        accountCache.remove(accountId)
        
        // Remove timestamps
        cacheTimestamps.keys.removeAll { it.startsWith("${accountId}_") || it == accountId }
        
        updateCacheStats()
    }
    
    /**
     * Clean expired cache entries
     */
    fun cleanExpiredEntries() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cacheTimestamps.filter { (_, timestamp) ->
            currentTime - timestamp > CACHE_EXPIRY_TIME
        }.keys
        
        expiredKeys.forEach { key ->
            emailCache.remove(key)
            folderCache.remove(key)
            accountCache.remove(key)
            cacheTimestamps.remove(key)
        }
        
        updateCacheStats()
    }
    
    /**
     * Get cache size in bytes
     */
    fun getCacheSize(): Long {
        return (emailCache.size() + folderCache.size() + accountCache.size()).toLong()
    }
    
    /**
     * Check if cache entry is expired
     */
    private fun isExpired(key: String): Boolean {
        val timestamp = cacheTimestamps[key] ?: return true
        return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME
    }
    
    /**
     * Update cache statistics
     */
    private fun updateCacheStats() {
        _cacheStats.value = CacheStats(
            emailCacheSize = emailCache.size(),
            folderCacheSize = folderCache.size(),
            accountCacheSize = accountCache.size(),
            totalCacheSize = getCacheSize(),
            hitRate = calculateHitRate()
        )
    }
    
    /**
     * Calculate cache hit rate (simplified)
     */
    private fun calculateHitRate(): Float {
        // This is a simplified calculation
        // In a real implementation, you would track hits and misses
        return 0.85f // Placeholder
    }
}

/**
 * Cached email data
 */
data class CachedEmail(
    val email: Email,
    val timestamp: Long
)

/**
 * Cached folder data
 */
data class CachedFolder(
    val folder: EmailFolder,
    val timestamp: Long
)

/**
 * Cached account data
 */
data class CachedAccount(
    val account: Account,
    val timestamp: Long
)

/**
 * Cache statistics
 */
data class CacheStats(
    val emailCacheSize: Int = 0,
    val folderCacheSize: Int = 0,
    val accountCacheSize: Int = 0,
    val totalCacheSize: Long = 0,
    val hitRate: Float = 0f
)