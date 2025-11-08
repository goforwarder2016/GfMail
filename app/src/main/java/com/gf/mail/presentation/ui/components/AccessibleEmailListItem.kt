package com.gf.mail.presentation.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gf.mail.R
import com.gf.mail.domain.model.Email
import com.gf.mail.utils.AccessibilityUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Accessible email list item component with WCAG 2.1 AA compliance
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccessibleEmailListItem(
    email: Email,
    isSelected: Boolean = false,
    onEmailClick: (Email) -> Unit,
    onEmailLongClick: (Email) -> Unit = {},
    onStarClick: (Email) -> Unit = {},
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Calculate if this is from today for time vs date display
    val isToday = remember(email.receivedAt) {
        val today = Calendar.getInstance()
        val emailDate = Calendar.getInstance().apply { timeInMillis = email.receivedAt }
        today.get(Calendar.YEAR) == emailDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == emailDate.get(Calendar.DAY_OF_YEAR)
    }

    val displayTime = remember(email.receivedAt, isToday) {
        if (isToday) {
            timeFormatter.format(Date(email.receivedAt))
        } else {
            dateFormatter.format(Date(email.receivedAt))
        }
    }

    // Enhanced touch target for accessibility
    val touchTargetSize = if (accessibilitySettings.largeTouchTargets) {
        AccessibilityUtils.getAccessibleTouchTargetSize()
    } else {
        56.dp
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = touchTargetSize)
            .combinedClickable(
                onClick = { onEmailClick(email) },
                onLongClick = { onEmailLongClick(email) }
            )
            .semantics(mergeDescendants = true) {
                // Comprehensive accessibility description
                contentDescription = buildAccessibilityDescription(email, displayTime)
                stateDescription = buildStateDescription(email, isSelected)
                role = Role.Button

                // Custom actions for accessibility
                customActions = listOf(
                    CustomAccessibilityAction(
                        label = if (email.isStarred) "Remove star" else "Add star",
                        action = {
                            onStarClick(email)
                            true
                        }
                    ),
                    CustomAccessibilityAction(
                        label = "Select email",
                        action = {
                            onEmailLongClick(email)
                            true
                        }
                    )
                )
            }
            .then(
                if (accessibilitySettings.focusIndicator && isSelected) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                        RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = if (accessibilitySettings.largeTouchTargets) 12.dp else 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sender avatar with accessibility support
            SenderAvatar(
                senderName = email.fromName,
                isRead = email.isRead,
                accessibilitySettings = accessibilitySettings
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
                        style = if (accessibilitySettings.largeText) {
                            MaterialTheme.typography.bodyLarge.copy(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.2f
                            )
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = if (accessibilitySettings.highContrast) {
                            if (email.isRead) Color.Black else Color.Black
                        } else {
                            if (email.isRead) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
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
                            style = if (accessibilitySettings.largeText) {
                                MaterialTheme.typography.bodySmall.copy(
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize * 1.2f
                                )
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                            color = if (accessibilitySettings.highContrast) {
                                Color.Black.copy(alpha = 0.8f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        // Attachment indicator
                        if (email.hasAttachments) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = null, // Description handled by parent semantics
                                modifier = Modifier.size(16.dp),
                                tint = if (accessibilitySettings.highContrast) {
                                    Color.Black
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                // Subject line
                Text(
                    text = email.subject.ifEmpty { context.getString(R.string.email_subject) },
                    style = if (accessibilitySettings.largeText) {
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.2f
                        )
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Medium,
                    color = if (accessibilitySettings.highContrast) {
                        if (email.isRead) Color.Black.copy(alpha = 0.8f) else Color.Black
                    } else {
                        if (email.isRead) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Preview text (if available)
                if (!email.bodyText.isNullOrEmpty()) {
                    Text(
                        text = email.bodyText.take(150),
                        style = if (accessibilitySettings.largeText) {
                            MaterialTheme.typography.bodySmall.copy(
                                fontSize = MaterialTheme.typography.bodySmall.fontSize * 1.2f
                            )
                        } else {
                            MaterialTheme.typography.bodySmall
                        },
                        color = if (accessibilitySettings.highContrast) {
                            Color.Black.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Star button with accessibility support
            AccessibleStarButton(
                isStarred = email.isStarred,
                onStarClick = { onStarClick(email) },
                accessibilitySettings = accessibilitySettings
            )
        }
    }
}

@Composable
private fun SenderAvatar(
    senderName: String,
    isRead: Boolean,
    accessibilitySettings: AccessibilitySettings,
    modifier: Modifier = Modifier
) {
    val initials = remember(senderName) {
        senderName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .take(2)
    }

    val backgroundColor = if (accessibilitySettings.highContrast) {
        if (isRead) Color.Gray else MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = if (isRead) 0.6f else 1f)
    }

    Box(
        modifier = modifier
            .size(if (accessibilitySettings.largeTouchTargets) 44.dp else 40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .semantics {
                contentDescription = "Avatar for $senderName"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = if (accessibilitySettings.largeText) {
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.1f
                )
            } else {
                MaterialTheme.typography.bodySmall
            },
            color = if (accessibilitySettings.highContrast) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onPrimary
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AccessibleStarButton(
    isStarred: Boolean,
    onStarClick: () -> Unit,
    accessibilitySettings: AccessibilitySettings,
    modifier: Modifier = Modifier
) {
    val touchTargetSize = if (accessibilitySettings.largeTouchTargets) {
        AccessibilityUtils.getAccessibleTouchTargetSize()
    } else {
        40.dp
    }

    IconButton(
        onClick = onStarClick,
        modifier = modifier
            .size(touchTargetSize)
            .semantics {
                contentDescription = if (isStarred) {
                    "Remove star from email"
                } else {
                    "Add star to email"
                }
                role = Role.Button
            }
    ) {
        Icon(
            imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = null, // Handled by parent semantics
            tint = if (accessibilitySettings.highContrast) {
                if (isStarred) Color(0xFFFFB000) else Color.Black
            } else {
                if (isStarred) {
                    Color(0xFFFFB000)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            },
            modifier = Modifier.size(if (accessibilitySettings.largeTouchTargets) 24.dp else 20.dp)
        )
    }
}

/**
 * Build comprehensive accessibility description for email
 */
private fun buildAccessibilityDescription(email: Email, displayTime: String): String {
    return buildString {
        append("Email from ${email.fromName}. ")

        if (email.subject.isNotEmpty()) {
            append("Subject: ${email.subject}. ")
        }

        if (!email.bodyText.isNullOrEmpty()) {
            append("Preview: ${email.bodyText.take(100)}. ")
        }

        if (email.hasAttachments) {
            append("Has attachments. ")
        }

        if (email.isStarred) {
            append("Starred. ")
        }

        append("${if (email.isRead) "Read" else "Unread"}. ")
        append("Received $displayTime.")
    }
}

/**
 * Build state description for accessibility
 */
private fun buildStateDescription(email: Email, isSelected: Boolean): String {
    return buildString {
        if (!email.isRead) append("Unread ")
        if (email.isStarred) append("Starred ")
        if (email.hasAttachments) append("Has attachments ")
        if (isSelected) append("Selected ")
    }.trim()
}

/**
 * Data class for accessibility settings
 */
data class AccessibilitySettings(
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val largeTouchTargets: Boolean = false,
    val focusIndicator: Boolean = true,
    val reduceMotion: Boolean = false
)
