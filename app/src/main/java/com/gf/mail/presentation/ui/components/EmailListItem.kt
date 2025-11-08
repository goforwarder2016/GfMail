package com.gf.mail.presentation.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gf.mail.domain.model.Email
import java.text.SimpleDateFormat
import java.util.*

/**
 * Standard email list item component
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmailListItem(
    email: Email,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    onSelectionChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Calculate if this is from today for time vs date display
    val isToday = remember(email.receivedDate) {
        val today = Calendar.getInstance()
        val emailDate = Calendar.getInstance().apply { timeInMillis = email.receivedDate }
        today.get(Calendar.YEAR) == emailDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == emailDate.get(Calendar.DAY_OF_YEAR)
    }

    val displayTime = remember(email.receivedDate, isToday) {
        if (isToday) {
            timeFormatter.format(Date(email.receivedDate))
        } else {
            dateFormatter.format(Date(email.receivedDate))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onSelectionChange(!isSelected) }
            ),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sender avatar
            SenderAvatar(
                senderName = email.fromName,
                isRead = email.isRead
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Email content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Sender and time row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = email.fromName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = if (email.isRead) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    // Time and indicators row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = displayTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Attachment indicator
                        if (email.hasAttachments) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Has attachments",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Star indicator
                        if (email.isStarred) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Starred",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFB000)
                            )
                        }
                    }
                }

                // Subject line
                Text(
                    text = email.subject.ifEmpty { "(No subject)" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Medium,
                    color = if (email.isRead) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Preview text (if available)
                if (!email.bodyText.isNullOrEmpty()) {
                    Text(
                        text = email.bodyText.take(100),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Selection checkbox (when in selection mode)
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SenderAvatar(
    senderName: String,
    isRead: Boolean,
    modifier: Modifier = Modifier
) {
    val initials = remember(senderName) {
        senderName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .take(2)
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = if (isRead) 0.6f else 1f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}
