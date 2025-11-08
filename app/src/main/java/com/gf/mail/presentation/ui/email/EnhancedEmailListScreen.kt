package com.gf.mail.presentation.ui.email

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.repository.BatchOperationResult
import com.gf.mail.presentation.viewmodel.BatchEmailOperationsViewModel
import com.gf.mail.presentation.viewmodel.EmailListViewModel



/**
 * Enhanced email list screen with batch operations support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedEmailListScreen(
    onEmailClick: (String) -> Unit,
    onComposeClick: () -> Unit,
    emailListViewModel: EmailListViewModel? = null,
    batchOperationsViewModel: BatchEmailOperationsViewModel? = null
) {
    val context = LocalContext.current
    val emailListVM = emailListViewModel ?: remember {
        com.gf.mail.di.DependencyContainer(context).getEmailListViewModel()
    }
    val batchOpsVM = batchOperationsViewModel ?: remember {
        com.gf.mail.di.DependencyContainer(context).getBatchEmailOperationsViewModel()
    }
    val emailListState by emailListVM.uiState.collectAsStateWithLifecycle()
    val batchState by batchOpsVM.uiState.collectAsStateWithLifecycle()
    val selectedEmails by batchOpsVM.selectedEmails.collectAsStateWithLifecycle()
    
    // Collect operation results
    LaunchedEffect(Unit) {
        batchOpsVM.operationResult.collect { result ->
            // Handle operation results (show snackbar, etc.)
            when (result) {
                is BatchOperationResult.Success -> {
                    // Show success message
                }
                is BatchOperationResult.Error -> {
                    // Show error message
                }
                else -> {}
            }
        }
    }
    
    Scaffold(
        topBar = {
            if (batchState.hasSelection) {
                BatchOperationsTopBar(
                    selectedCount = batchState.selectedCount,
                    onClearSelection = { batchOpsVM.clearSelection() },
                    onMarkAsRead = { batchOpsVM.markSelectedAsRead() },
                    onMarkAsUnread = { batchOpsVM.markSelectedAsUnread() },
                    onDelete = { batchOpsVM.deleteSelectedEmails() },
                    onStar = { batchOpsVM.starSelectedEmails() },
                    onArchive = { batchOpsVM.archiveSelectedEmails() },
                    onSpam = { batchOpsVM.markSelectedAsSpam() }
                )
            } else {
                RegularTopBar(
                    onComposeClick = onComposeClick
                )
            }
        },
        floatingActionButton = {
            if (!batchState.hasSelection) {
                FloatingActionButton(
                    onClick = onComposeClick,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Compose")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                emailListState.isLoading -> {
                    LoadingState()
                }
                emailListState.emails.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    EmailList(
                        emails = emailListState.emails,
                        selectedEmails = selectedEmails,
                        onEmailClick = onEmailClick,
                        onEmailLongClick = { emailId ->
                            batchOpsVM.toggleEmailSelection(emailId)
                        },
                        onEmailSelectionToggle = { emailId ->
                            batchOpsVM.toggleEmailSelection(emailId)
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            
            // Show progress indicator during batch operations
            if (batchState.isLoading) {
                BatchOperationProgress(
                    progress = batchState.progress,
                    total = batchState.totalProgress,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegularTopBar(
    onComposeClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Inbox") },
        actions = {
            IconButton(onClick = onComposeClick) {
                Icon(Icons.Default.Add, contentDescription = "Compose")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchOperationsTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onMarkAsRead: () -> Unit,
    onMarkAsUnread: () -> Unit,
    onDelete: () -> Unit,
    onStar: () -> Unit,
    onArchive: () -> Unit,
    onSpam: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                text = "$selectedCount selected",
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear selection")
            }
        },
        actions = {
            IconButton(onClick = onMarkAsRead) {
                Icon(Icons.Default.MarkEmailRead, contentDescription = "Mark as read")
            }
            IconButton(onClick = onMarkAsUnread) {
                Icon(Icons.Default.MarkEmailUnread, contentDescription = "Mark as unread")
            }
            IconButton(onClick = onStar) {
                Icon(Icons.Default.Star, contentDescription = "Star")
            }
            IconButton(onClick = onArchive) {
                Icon(Icons.Default.Archive, contentDescription = "Archive")
            }
            IconButton(onClick = onSpam) {
                Icon(Icons.Default.Report, contentDescription = "Mark as spam")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    )
}

@Composable
private fun EmailList(
    emails: List<Email>,
    selectedEmails: Set<String>,
    onEmailClick: (String) -> Unit,
    onEmailLongClick: (String) -> Unit,
    onEmailSelectionToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = emails,
            key = { it.id }
        ) { email ->
            EnhancedEmailListItem(
                email = email,
                isSelected = email.id in selectedEmails,
                onClick = { onEmailClick(email.id) },
                onLongClick = { onEmailLongClick(email.id) },
                onSelectionToggle = { onEmailSelectionToggle(email.id) }
            )
        }
    }
}

@Composable
private fun EnhancedEmailListItem(
    email: Email,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectionToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionToggle() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Email content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = email.fromName.ifEmpty { email.fromAddress },
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = email.subject,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = email.bodyText?.take(100) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Email status indicators
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (email.isStarred) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Starred",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (!email.isRead) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = "Unread",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Email,
                contentDescription = "No emails",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No emails",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BatchOperationProgress(
    progress: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                progress = if (total > 0) progress.toFloat() / total.toFloat() else 0f,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Processing $progress of $total",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}