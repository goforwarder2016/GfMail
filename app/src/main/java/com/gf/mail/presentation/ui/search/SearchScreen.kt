package com.gf.mail.presentation.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.usecase.SearchSuggestion
import com.gf.mail.domain.usecase.SearchSuggestionType
import com.gf.mail.presentation.ui.components.EmailListItem
import com.gf.mail.presentation.ui.components.showErrorMessage
import com.gf.mail.presentation.viewmodel.SearchEmailViewModel
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Email search screen with suggestions and filters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchEmailViewModel,
    onBackClick: () -> Unit,
    onEmailClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // Search filters state
    var showFilters by remember { mutableStateOf(false) }
    var currentFilters by remember { mutableStateOf(SearchFilters()) }

    // Auto focus search field when screen opens
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search header
        SearchHeader(
            searchQuery = uiState.searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            onSearch = {
                scope.launch {
                    viewModel.performSearch()
                }
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            onBackClick = onBackClick,
            onClearQuery = viewModel::clearSearch,
            onShowFilters = { showFilters = true },
            hasActiveFilters = hasActiveFilters(currentFilters),
            isLoading = uiState.isLoading,
            focusRequester = focusRequester
        )

        // Content based on state
        when {
            // Show suggestions while typing
            uiState.showSuggestions && uiState.suggestions.isNotEmpty() -> {
                SearchSuggestions(
                    suggestions = uiState.suggestions,
                    onSuggestionClick = { suggestion ->
                        viewModel.selectSuggestion(suggestion)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            }

            // Show search results
            uiState.isSearchActive && uiState.searchResults.isNotEmpty() -> {
                SearchResults(
                    emails = getFilteredEmails(uiState.searchResults, currentFilters),
                    totalCount = uiState.totalResultsCount,
                    searchTime = uiState.searchTime,
                    hasMoreResults = uiState.hasMoreResults,
                    isLoadingMore = uiState.isLoadingMore,
                    onEmailClick = onEmailClick,
                    onLoadMore = viewModel::loadMoreResults,
                    onRefresh = viewModel::refreshResults
                )
            }

            // Show empty state for search
            uiState.isSearchActive && uiState.searchResults.isEmpty() && !uiState.isLoading -> {
                SearchEmptyState(
                    query = uiState.searchQuery,
                    onClearSearch = viewModel::clearSearch
                )
            }

            // Show initial state (recent searches, popular terms)
            !uiState.isSearchActive -> {
                SearchInitialState(
                    recentSearches = uiState.recentSearches,
                    popularTerms = uiState.popularSearchTerms,
                    onRecentSearchClick = { query ->
                        viewModel.selectRecentSearch(query)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    onClearRecentSearches = viewModel::clearRecentSearches
                )
            }

            // Show loading state
            uiState.isLoading -> {
                SearchLoadingState()
            }
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            scope.launch {
                showErrorMessage(error)
                viewModel.clearError()
            }
        }
    }

    // Search filters dialog
    SearchFiltersDialog(
        isVisible = showFilters,
        currentFilters = currentFilters,
        availableFolders = emptyList(), // TODO: Get available folders
        onFiltersChanged = { currentFilters = it },
        onDismiss = { showFilters = false },
        onApply = { filters ->
            currentFilters = filters
            showFilters = false
            // Re-apply search with filters
            if (uiState.isSearchActive) {
                viewModel.refreshResults()
            }
        },
        onClear = {
            currentFilters = SearchFilters()
            // Re-apply search without filters
            if (uiState.isSearchActive) {
                viewModel.refreshResults()
            }
        }
    )
}

/**
 * Check if there are active filters
 */
private fun hasActiveFilters(filters: SearchFilters): Boolean {
    return filters.isRead != null ||
        filters.isStarred != null ||
        filters.hasAttachments != null ||
        filters.priority != null ||
        filters.sender.isNotBlank() ||
        filters.folder != null ||
        filters.dateFrom != null ||
        filters.dateTo != null
}

/**
 * Apply filters to email list
 */
private fun getFilteredEmails(emails: List<Email>, filters: SearchFilters): List<Email> {
    return emails.filter { email ->
        (filters.isRead == null || email.isRead == filters.isRead) &&
            (filters.isStarred == null || email.isStarred == filters.isStarred) &&
            (filters.hasAttachments == null || email.hasAttachments == filters.hasAttachments) &&
            (filters.priority == null || email.priority == filters.priority) &&
            (
                filters.sender.isBlank() || email.fromAddress.contains(
                    filters.sender,
                    ignoreCase = true
                ) ||
                    email.fromName.contains(filters.sender, ignoreCase = true)
                ) &&
            // TODO: Add folder filtering
            // TODO: Add date range filtering
            true
    }
}

/**
 * Search header with input field and controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBackClick: () -> Unit,
    onClearQuery: () -> Unit,
    onShowFilters: () -> Unit,
    hasActiveFilters: Boolean,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search emails...") },
                singleLine = true,
                leadingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                // colors = TextFieldDefaults.colors(
                //     unfocusedBorderColor = Color.Transparent,
                //     focusedBorderColor = MaterialTheme.colorScheme.primary
                // )
            )

            // Filters button
            IconButton(
                onClick = onShowFilters,
                enabled = !isLoading
            ) {
                Badge(
                    containerColor = if (hasActiveFilters) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color.Transparent
                    }
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (hasActiveFilters) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Search button
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = onSearch,
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Search suggestions list
 */
@Composable
private fun SearchSuggestions(
    suggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(suggestions) { suggestion ->
            SearchSuggestionItem(
                suggestion = suggestion,
                onClick = { onSuggestionClick(suggestion) }
            )
        }
    }
}

/**
 * Individual search suggestion item
 */
@Composable
private fun SearchSuggestionItem(
    suggestion: SearchSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon based on suggestion type
        Icon(
            imageVector = when (suggestion.type) {
                SearchSuggestionType.SENDER -> Icons.Default.Person
                SearchSuggestionType.SUBJECT -> Icons.Default.Subject
                SearchSuggestionType.CONTENT -> Icons.Default.Description
                SearchSuggestionType.DATE -> Icons.Default.DateRange
                SearchSuggestionType.ATTACHMENT -> Icons.Default.Attachment
            },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            suggestion.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            Icons.Default.CallMade,
            contentDescription = "Use suggestion",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Search results list
 */
@Composable
private fun SearchResults(
    emails: List<Email>,
    totalCount: Int,
    searchTime: Long,
    hasMoreResults: Boolean,
    isLoadingMore: Boolean,
    onEmailClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Results header
        SearchResultsHeader(
            totalCount = totalCount,
            searchTime = searchTime,
            onRefresh = onRefresh
        )

        // Results list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(emails) { email ->
                EmailListItem(
                    email = email,
                    onClick = { onEmailClick(email.id) },
                    isSelected = false,
                    onSelectionChange = { }
                )
            }

            // Load more button
            if (hasMoreResults) {
                item {
                    if (isLoadingMore) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onLoadMore,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Load More Results")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Search results header with count and stats
 */
@Composable
private fun SearchResultsHeader(
    totalCount: Int,
    searchTime: Long,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$totalCount results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Search completed in ${searchTime}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh results"
                )
            }
        }
    }
}

/**
 * Empty search state
 */
@Composable
private fun SearchEmptyState(
    query: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No results found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No emails found for \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(onClick = onClearSearch) {
            Text("Clear Search")
        }
    }
}

/**
 * Initial search state with recent searches and popular terms
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchInitialState(
    recentSearches: List<String>,
    popularTerms: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Recent searches
        if (recentSearches.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        TextButton(onClick = onClearRecentSearches) {
                            Text("Clear All")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    recentSearches.forEach { search ->
                        RecentSearchItem(
                            search = search,
                            onClick = { onRecentSearchClick(search) }
                        )
                    }
                }
            }
        }

        // Popular search terms
        if (popularTerms.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Popular Searches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Popular terms as chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularTerms.take(6).forEach { term ->
                            AssistChip(
                                onClick = { onRecentSearchClick(term) },
                                label = { Text(term) }
                            )
                        }
                    }
                }
            }
        }

        // Search tips
        item {
            SearchTips()
        }
    }
}

/**
 * Recent search item
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RecentSearchItem(
    search: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = search,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Icon(
            Icons.Default.CallMade,
            contentDescription = "Search again",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Search tips section
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchTips(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Search Tips",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            val tips = listOf(
                "Use quotes for exact phrases: \"meeting tomorrow\"",
                "Search by sender: from:sender@domain.com",
                "Find emails with attachments: has:attachment",
                "Search by date: after:2023-01-01 before:2023-12-31"
            )

            tips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Loading state for search
 */
@Composable
private fun SearchLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Searching...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
