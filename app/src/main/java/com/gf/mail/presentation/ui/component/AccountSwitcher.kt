package com.gf.mail.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.AccountSummary
import com.gf.mail.domain.model.EmailProvider

/**
 * Account switcher component for selecting between multiple accounts
 */
@Composable
fun AccountSwitcher(
    currentAccount: Account?,
    availableAccounts: List<Account>,
    accountSummary: AccountSummary,
    onAccountSelected: (Account) -> Unit,
    onAddAccountClick: () -> Unit,
    onManageAccountsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAccountPicker by remember { mutableStateOf(false) }

    // Current account display
    AccountSwitcherButton(
        currentAccount = currentAccount,
        accountSummary = accountSummary,
        availableAccounts = availableAccounts,
        onClick = {
            if (availableAccounts.size > 1) {
                showAccountPicker = true
            }
        },
        modifier = modifier
    )

    // Account picker dialog
    if (showAccountPicker) {
        AccountPickerDialog(
            currentAccount = currentAccount,
            availableAccounts = availableAccounts,
            accountSummary = accountSummary,
            onAccountSelected = { account ->
                onAccountSelected(account)
                showAccountPicker = false
            },
            onAddAccountClick = {
                onAddAccountClick()
                showAccountPicker = false
            },
            onManageAccountsClick = {
                onManageAccountsClick()
                showAccountPicker = false
            },
            onDismiss = { showAccountPicker = false }
        )
    }
}

/**
 * Button to show current account and open account picker
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AccountSwitcherButton(
    currentAccount: Account?,
    accountSummary: AccountSummary,
    availableAccounts: List<Account>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account avatar
            AccountAvatar(
                account = currentAccount,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Account info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentAccount?.displayName ?: "No Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentAccount?.email ?: "Add an account to get started",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Show dropdown indicator if multiple accounts available
            if (availableAccounts.size > 1) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Switch account",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Account picker dialog
 */
@Composable
private fun AccountPickerDialog(
    currentAccount: Account?,
    availableAccounts: List<Account>,
    accountSummary: AccountSummary,
    onAccountSelected: (Account) -> Unit,
    onAddAccountClick: () -> Unit,
    onManageAccountsClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Switch Account",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Account count info
                Text(
                    text = "${availableAccounts.size}/10 accounts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Account list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableAccounts) { account ->
                        AccountPickerItem(
                            account = account,
                            isSelected = account.id == currentAccount?.id,
                            onClick = { onAccountSelected(account) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (availableAccounts.size < 10) {
                        OutlinedButton(
                            onClick = onAddAccountClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Account")
                        }
                    }

                    Button(
                        onClick = onManageAccountsClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage")
                    }
                }
            }
        }
    }
}

/**
 * Individual account item in picker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountPickerItem(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (isSelected) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccountAvatar(
                account = account,
                size = 32.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = account.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = account.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Account avatar component
 */
@Composable
private fun AccountAvatar(
    account: Account?,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                when (account?.provider) {
                    EmailProvider.GMAIL -> Color(0xFF4285F4)
                    EmailProvider.EXCHANGE -> Color(0xFF0078D4)
                    EmailProvider.QQ -> Color(0xFF12B7F5)
                    EmailProvider.NETEASE -> Color(0xFFE60012)
                    EmailProvider.OUTLOOK -> Color(0xFF0078D4)
                    EmailProvider.YAHOO -> Color(0xFF7B0099)
                    EmailProvider.APPLE -> Color(0xFF007AFF)
                    EmailProvider.IMAP -> Color(0xFF9C27B0)
                    EmailProvider.POP3 -> Color(0xFF795548)
                    null -> MaterialTheme.colorScheme.primary
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (account != null) {
            Text(
                text = account.displayName.take(2).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = "No account",
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}
