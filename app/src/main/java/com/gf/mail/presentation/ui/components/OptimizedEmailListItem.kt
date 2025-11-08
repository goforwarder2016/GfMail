package com.gf.mail.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gf.mail.domain.model.Email
import com.gf.mail.domain.model.EmailPriority
import com.gf.mail.utils.AccessibilityUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Optimized Email List Item with enhanced accessibility and Material Design 3
 */
@Composable
fun OptimizedEmailListItem(
    email: Email,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    showSelectionCheckbox: Boolean = false,
    onClick: () -> Unit,
    onSelectionChange: (Boolean) -> Unit = {},
    onLongClick: () -> Unit = {},
    onStarClick: () -> Unit = {}
) {
    val contentDescription = buildString {
        append("Email from ${email.fromName}")
        if (!email.isRead) append(", unread")
        if (email.isStarred) append(", starred")
        if (email.hasAttachments) append(", has attachments")
        append(". Subject: ${email.subject}")
        append(". Received: ${formatEmailDate(email.receivedDate)}")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .semantics {
                this.role = Role.Button
                this.contentDescription = contentDescription
                this.selected = isSelected
            }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (email.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Selection checkbox
            if (showSelectionCheckbox) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChange,
                    modifier = Modifier
                        .size(24.dp)
                        .semantics {
                            this.role = Role.Checkbox
                            this.contentDescription = if (isSelected) "Selected" else "Not selected"
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Avatar or priority indicator
            EmailAvatar(
                email = email,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Email content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From name
                    Text(
                        text = email.fromName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Time and actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatEmailTime(email.receivedDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Star button
                        IconButton(
                            onClick = onStarClick,
                            modifier = Modifier
                                .size(24.dp)
                                .semantics {
                                    this.role = Role.Button
                                    this.contentDescription = if (email.isStarred) "Remove star" else "Add star"
                                }
                        ) {
                            Icon(
                                imageVector = if (email.isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = null,
                                tint = if (email.isStarred) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subject
                Text(
                    text = email.subject.ifEmpty { "(No Subject)" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Preview text
                Text(
                    text = email.bodyText?.take(100) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Footer with attachments and labels
                if (email.hasAttachments || email.labels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (email.hasAttachments) {
                            Icon(
                                imageVector = Icons.Filled.AttachFile,
                                contentDescription = "Has attachments",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Labels
                        email.labels.take(2).forEach { label ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.semantics {
                                    this.contentDescription = "Label: $label"
                                }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Email Avatar component with accessibility support
 */
@Composable
private fun EmailAvatar(
    email: Email,
    modifier: Modifier = Modifier
) {
    val initials = email.fromName
        .split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercaseChar() ?: "" }
        .joinToString("")

    Surface(
        modifier = modifier
            .size(40.dp)
            .semantics {
                contentDescription = "Avatar for ${email.fromName}"
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Format email date for display
 */
private fun formatEmailDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 24 * 60 * 60 * 1000 -> { // Less than 24 hours
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        diff < 7 * 24 * 60 * 60 * 1000 -> { // Less than 7 days
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }
        else -> { // More than 7 days
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
    }
}

/**
 * Format email time for display
 */
private fun formatEmailTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 60 * 1000 -> "Now" // Less than 1 minute
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m" // Less than 1 hour
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h" // Less than 24 hours
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}