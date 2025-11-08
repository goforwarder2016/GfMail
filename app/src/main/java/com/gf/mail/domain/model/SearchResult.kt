package com.gf.mail.domain.model

/**
 * Data class representing search results
 */
data class SearchResult(
    val emails: List<Email>,
    val totalCount: Int,
    val searchTime: Long
)

/**
 * Data class representing search filters
 */
data class SearchFilters(
    val from: String? = null,
    val to: String? = null,
    val subject: String? = null,
    val hasAttachments: Boolean? = null,
    val isRead: Boolean? = null,
    val isStarred: Boolean? = null,
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val folder: String? = null,
    val priority: EmailPriority? = null,
    val offset: Int = 0,
    val limit: Int = 50
)

/**
 * Data class representing search suggestions
 */
data class SearchSuggestion(
    val text: String,
    val type: SuggestionType,
    val count: Int = 0
)

/**
 * Enum representing suggestion types
 */
enum class SuggestionType {
    RECENT,
    POPULAR,
    CONTACT,
    FOLDER
}