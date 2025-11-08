package com.gf.mail.presentation.ui.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gf.mail.domain.model.EmailFolder
import com.gf.mail.domain.model.FolderType

/**
 * Bottom sheet for selecting a folder to move emails to
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectorBottomSheet(
    folders: List<EmailFolder>,
    isVisible: Boolean,
    selectedCount: Int,
    onDismiss: () -> Unit,
    onFolderSelected: (EmailFolder) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
            FolderSelectorContent(
                folders = folders,
                selectedCount = selectedCount,
                onFolderSelected = onFolderSelected,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Content of the folder selector bottom sheet
 */
@Composable
private fun FolderSelectorContent(
    folders: List<EmailFolder>,
    selectedCount: Int,
    onFolderSelected: (EmailFolder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        FolderSelectorHeader(selectedCount = selectedCount)

        Spacer(modifier = Modifier.height(16.dp))

        // Search box (future enhancement)
        // TODO: Add search functionality for folders

        // Folders list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Group folders by type
            val systemFolders = folders.filter { it.isSystem }
            val customFolders = folders.filter { !it.isSystem }

            // System folders section
            if (systemFolders.isNotEmpty()) {
                item {
                    Text(
                        text = "System Folders",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(systemFolders.sortedBy { it.sortOrder }) { folder ->
                    FolderSelectorItem(
                        folder = folder,
                        onClick = {
                            onFolderSelected(folder)
                            onDismiss()
                        }
                    )
                }
            }

            // Custom folders section
            if (customFolders.isNotEmpty()) {
                item {
                    Text(
                        text = "Custom Folders",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(customFolders.sortedBy { it.displayName }) { folder ->
                    FolderSelectorItem(
                        folder = folder,
                        onClick = {
                            onFolderSelected(folder)
                            onDismiss()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Header showing selected count
 */
@Composable
private fun FolderSelectorHeader(
    selectedCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.DriveFileMove,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Move $selectedCount email${if (selectedCount != 1) "s" else ""} to...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Individual folder selector item
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FolderSelectorItem(
    folder: EmailFolder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineText = {
            Text(
                text = folder.displayName.toString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingText = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (folder.unreadCount > 0) {
                    Text(
                        text = "${folder.unreadCount} unread",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${folder.totalCount} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = getFolderIcon(folder.type),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = getFolderIconColor(folder.type)
            )
        },
        modifier = modifier.clickable { onClick() }
    )
}

/**
 * Quick folder actions bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFolderActionsBottomSheet(
    folders: List<EmailFolder>,
    isVisible: Boolean,
    selectedCount: Int,
    onDismiss: () -> Unit,
    onMoveToFolder: (EmailFolder) -> Unit,
    onCopyToFolder: (EmailFolder) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
            QuickFolderActionsContent(
                folders = folders,
                selectedCount = selectedCount,
                onMoveToFolder = onMoveToFolder,
                onCopyToFolder = onCopyToFolder,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Content for quick folder actions
 */
@Composable
private fun QuickFolderActionsContent(
    folders: List<EmailFolder>,
    selectedCount: Int,
    onMoveToFolder: (EmailFolder) -> Unit,
    onCopyToFolder: (EmailFolder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoveSelector by remember { mutableStateOf(false) }
    var showCopySelector by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Quick action buttons
        val quickFolders = getQuickActionFolders(folders)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickFolders) { folder ->
                QuickActionItem(
                    folder = folder,
                    selectedCount = selectedCount,
                    onMoveClick = {
                        onMoveToFolder(folder)
                        onDismiss()
                    },
                    onCopyClick = {
                        onCopyToFolder(folder)
                        onDismiss()
                    }
                )
            }

            // More options
            item {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showMoveSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DriveFileMove, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Move to Other Folder...")
                }

                OutlinedButton(
                    onClick = { showCopySelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy to Other Folder...")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Move selector
    FolderSelectorBottomSheet(
        folders = folders,
        isVisible = showMoveSelector,
        selectedCount = selectedCount,
        onDismiss = { showMoveSelector = false },
        onFolderSelected = { folder ->
            onMoveToFolder(folder)
            showMoveSelector = false
            onDismiss()
        }
    )

    // Copy selector
    FolderSelectorBottomSheet(
        folders = folders,
        isVisible = showCopySelector,
        selectedCount = selectedCount,
        onDismiss = { showCopySelector = false },
        onFolderSelected = { folder ->
            onCopyToFolder(folder)
            showCopySelector = false
            onDismiss()
        }
    )
}

/**
 * Quick action item for common folders
 */
@Composable
private fun QuickActionItem(
    folder: EmailFolder,
    selectedCount: Int,
    onMoveClick: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getFolderIcon(folder.type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = getFolderIconColor(folder.type)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.displayName.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (folder.totalCount > 0) {
                        Text(
                            text = "${folder.totalCount} emails",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMoveClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DriveFileMove,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Move")
                }

                OutlinedButton(
                    onClick = onCopyClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.FileCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
            }
        }
    }
}

/**
 * Get folders for quick actions (most commonly used)
 */
private fun getQuickActionFolders(folders: List<EmailFolder>): List<EmailFolder> {
    val quickFolderTypes = listOf(
        FolderType.ARCHIVE,
        FolderType.TRASH,
        FolderType.SPAM
    )

    return folders.filter { folder ->
        folder.type in quickFolderTypes
    }.sortedBy {
        when (it.type) {
            FolderType.ARCHIVE -> 0
            FolderType.TRASH -> 1
            FolderType.SPAM -> 2
            else -> 999
        }
    }
}

/**
 * Get appropriate icon for folder type
 */
private fun getFolderIcon(type: FolderType): ImageVector {
    return when (type) {
        FolderType.INBOX -> Icons.Default.Inbox
        FolderType.SENT -> Icons.Default.Send
        FolderType.DRAFTS -> Icons.Default.Drafts
        FolderType.TRASH -> Icons.Default.Delete
        FolderType.SPAM -> Icons.Default.Block
        FolderType.ARCHIVE -> Icons.Default.Inventory
        FolderType.CUSTOM -> Icons.Default.Folder
    }
}

/**
 * Get appropriate color for folder icon
 */
@Composable
private fun getFolderIconColor(type: FolderType): Color {
    return when (type) {
        FolderType.INBOX -> MaterialTheme.colorScheme.primary
        FolderType.SENT -> MaterialTheme.colorScheme.secondary
        FolderType.DRAFTS -> MaterialTheme.colorScheme.tertiary
        FolderType.TRASH -> MaterialTheme.colorScheme.error
        FolderType.SPAM -> MaterialTheme.colorScheme.error
        FolderType.ARCHIVE -> MaterialTheme.colorScheme.outline
        FolderType.CUSTOM -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
