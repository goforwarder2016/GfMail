package com.gf.mail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.usecase.SearchEmailsUseCaseImpl
import com.gf.mail.domain.usecase.SearchSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for email search
 */
class SearchEmailViewModel(
    private val searchEmailsUseCase: SearchEmailsUseCaseImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchEmailUiState())
    val uiState: StateFlow<SearchEmailUiState> = _uiState.asStateFlow()

    fun searchEmails(query: String, accountId: Long? = null) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val searchResult = searchEmailsUseCase.searchEmails(query, accountId?.toString())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    searchResults = searchResult.localResults
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun searchEmailsInFolder(query: String, folderId: Long) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val results = searchEmailsUseCase.searchEmailsInFolder(query, folderId.toString())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    searchResults = results
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun clearSearchResults() {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun performSearch() {
        searchEmails(_uiState.value.searchQuery)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun selectSuggestion(suggestion: SearchSuggestion) {
        _uiState.value = _uiState.value.copy(searchQuery = suggestion.text)
    }

    fun loadMoreResults() {
        // TODO: Implement load more results
    }

    fun refreshResults() {
        performSearch()
    }

    fun selectRecentSearch(search: String) {
        _uiState.value = _uiState.value.copy(searchQuery = search)
    }

    fun clearRecentSearches() {
        // TODO: Implement clear recent searches
    }

    data class SearchEmailUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchResults: List<Email> = emptyList(),
        val searchQuery: String = "",
        val showSuggestions: Boolean = false,
        val suggestions: List<SearchSuggestion> = emptyList(),
        val isSearchActive: Boolean = false,
        val totalResultsCount: Int = 0,
        val searchTime: Long = 0L,
        val hasMoreResults: Boolean = false,
        val isLoadingMore: Boolean = false,
        val recentSearches: List<String> = emptyList(),
        val popularSearchTerms: List<String> = emptyList()
    )
}