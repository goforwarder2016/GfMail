package com.gf.mail.domain.usecase

import com.gf.mail.domain.model.Email
import kotlinx.coroutines.flow.Flow
// import javax.inject.Inject

/**
 * Use case interface for searching emails locally and on server
 */
interface SearchEmailsUseCase {

    /**
     * Search emails locally
     */
    suspend fun searchLocalEmails(
        query: String,
        accountId: String? = null
    ): List<Email>

    /**
     * Search emails locally as Flow for reactive updates
     */
    fun searchLocalEmailsFlow(
        query: String,
        accountId: String? = null
    ): Flow<List<Email>>

    /**
     * Search emails in a specific folder locally
     */
    suspend fun searchEmailsInFolder(
        query: String,
        folderId: String
    ): List<Email>

    /**
     * Search emails on server (requires IMAP connection)
     */
    suspend fun searchServerEmails(
        query: String,
        accountId: String,
        folderId: String
    ): Result<List<Email>>

    /**
     * Combined search: local first, then server if needed
     */
    suspend fun searchEmails(
        query: String,
        accountId: String? = null,
        searchServer: Boolean = true
    ): SearchResult

    /**
     * Search suggestions based on query
     */
    suspend fun getSearchSuggestions(
        query: String,
        accountId: String? = null,
        limit: Int = 10
    ): List<SearchSuggestion>

    /**
     * Get recent searches
     */
    suspend fun getRecentSearches(limit: Int = 10): List<String>

    /**
     * Save search query to recent searches
     */
    suspend fun saveSearchQuery(query: String)

    /**
     * Clear recent searches
     */
    suspend fun clearRecentSearches()
}

/**
 * Search result data class
 */
data class SearchResult(
    val localResults: List<Email>,
    val serverResults: List<Email>,
    val isServerSearchComplete: Boolean,
    val totalCount: Int = localResults.size + serverResults.size
)

/**
 * Search suggestion data class
 */
data class SearchSuggestion(
    val text: String,
    val type: SearchSuggestionType,
    val description: String? = null
)

/**
 * Search suggestion types
 */
enum class SearchSuggestionType {
    SENDER,
    SUBJECT,
    CONTENT,
    DATE,
    ATTACHMENT
}
