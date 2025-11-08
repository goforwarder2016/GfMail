package com.gf.mail.presentation.ui.attachment

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gf.mail.data.storage.FilePickerService
import com.gf.mail.domain.model.EmailAttachment
import kotlinx.coroutines.launch

/**
 * Dialog for picking and managing attachments
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPickerDialog(
    isVisible: Boolean,
    currentAttachments: List<EmailAttachment>,
    onDismiss: () -> Unit,
    onAttachmentsSelected: (List<Uri>) -> Unit,
    onAttachmentRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val filePickerService = remember { FilePickerService(context) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAttachmentsSelected(uris)
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.8f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    AttachmentDialogHeader(
                        onDismiss = onDismiss,
                        onAddFiles = {
                            filePickerLauncher.launch("*/*")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content
                    if (currentAttachments.isEmpty()) {
                        EmptyAttachmentsState(
                            onAddFiles = {
                                filePickerLauncher.launch("*/*")
                            }
                        )
                    } else {
                        AttachmentsList(
                            attachments = currentAttachments,
                            onRemoveAttachment = onAttachmentRemove,
                            filePickerService = filePickerService
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog header with title and actions
 */
@Composable
private fun AttachmentDialogHeader(
    onDismiss: () -> Unit,
    onAddFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Attachments",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        Row {
            IconButton(onClick = onAddFiles) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add files",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
}

/**
 * Empty state when no attachments
 */
@Composable
private fun EmptyAttachmentsState(
    onAddFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            Text(
                text = "No attachments yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Tap the + button to add files\nMaximum 20MB total size",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onAddFiles,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Files")
            }
        }
    }
}

/**
 * List of current attachments
 */
@Composable
private fun AttachmentsList(
    attachments: List<EmailAttachment>,
    onRemoveAttachment: (String) -> Unit,
    filePickerService: FilePickerService,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Summary
        AttachmentsSummary(attachments = attachments, filePickerService = filePickerService)

        Spacer(modifier = Modifier.height(8.dp))

        // List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attachments) { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    onRemove = { onRemoveAttachment(attachment.id) },
                    filePickerService = filePickerService
                )
            }
        }
    }
}

/**
 * Summary of attachments
 */
@Composable
private fun AttachmentsSummary(
    attachments: List<EmailAttachment>,
    filePickerService: FilePickerService,
    modifier: Modifier = Modifier
) {
    val totalSize = attachments.sumOf { it.size }
    val formattedSize = filePickerService.formatFileSize(totalSize)
    val maxSize = filePickerService.formatFileSize(FilePickerService.MAX_TOTAL_ATTACHMENTS_SIZE)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${attachments.size} attachment(s)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "$formattedSize / $maxSize",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress = (totalSize.toFloat() / FilePickerService.MAX_TOTAL_ATTACHMENTS_SIZE),
            modifier = Modifier.fillMaxWidth(),
            color = if (totalSize > FilePickerService.MAX_TOTAL_ATTACHMENTS_SIZE) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

/**
 * Individual attachment item
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AttachmentItem(
    attachment: EmailAttachment,
    onRemove: () -> Unit,
    filePickerService: FilePickerService,
    modifier: Modifier = Modifier
) {
    val fileInfo = remember(attachment) {
        filePickerService.getAttachmentFileInfo(attachment)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Text(
                text = fileInfo.icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 12.dp)
            )

            // File info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileInfo.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (attachment.size > FilePickerService.MAX_ATTACHMENT_SIZE) {
                        AssistChip(
                            onClick = { /* no-op */ },
                            label = {
                                Text("Too large", style = MaterialTheme.typography.labelSmall)
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.height(20.dp)
                        )
                    }

                    if (!fileInfo.exists) {
                        AssistChip(
                            onClick = { /* no-op */ },
                            label = {
                                Text("Missing", style = MaterialTheme.typography.labelSmall)
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove attachment",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * File picker service extension function for getting attachment file info
 */
private fun FilePickerService.getAttachmentFileInfo(attachment: EmailAttachment): AttachmentFileInfo {
    return AttachmentFileInfo(
        exists = attachment.uri?.let { fileExists(it.toString()) } ?: false,
        size = attachment.uri?.let { getFileSize(it.toString()) } ?: attachment.size,
        formattedSize = formatFileSize(attachment.size),
        icon = getFileIcon(attachment.mimeType),
        isImage = attachment.mimeType.startsWith("image/"),
        isDocument = attachment.mimeType.startsWith("application/") ||
            attachment.mimeType.startsWith("text/"),
        isMedia = attachment.mimeType.startsWith("audio/") ||
            attachment.mimeType.startsWith("video/")
    )
}

private data class AttachmentFileInfo(
    val exists: Boolean,
    val size: Long,
    val formattedSize: String,
    val icon: String,
    val isImage: Boolean,
    val isDocument: Boolean,
    val isMedia: Boolean
)
