package com.gf.mail.presentation.ui.accounts

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.AuthenticationType
import com.gf.mail.domain.model.EmailProvider
import com.gf.mail.presentation.viewmodel.AccountManagementViewModel

/**
 * Account list screen showing all configured accounts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountListScreen(
    accounts: List<Account>,
    activeAccount: Account?,
    viewModel: AccountManagementViewModel,
    onBackClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onNavigateToQRCode: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (accounts.size < 3) {
                        IconButton(onClick = onAddAccountClick) {
                            Icon(Icons.Default.Add, contentDescription = "Add Account")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (accounts.size < 3) {
                FloatingActionButton(
                    onClick = onAddAccountClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Account")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                accounts.isEmpty() -> {
                    EmptyAccountsState(
                        onAddAccountClick = onAddAccountClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            AccountsHeader(
                                accountCount = accounts.size,
                                maxAccounts = 3
                            )
                        }

                        items(accounts) { account ->
                            AccountItem(
                                account = account,
                                isActive = account.id == activeAccount?.id,
                                onSetActive = { viewModel.setActiveAccount(account.id) },
                                onDelete = { showDeleteDialog = account },
                                onNavigateToQRCode = onNavigateToQRCode,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { account ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Account") },
            text = {
                Text(
                    "Are you sure you want to delete the account for ${account.email}? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(account.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Empty state when no accounts are configured
 */
@Composable
private fun EmptyAccountsState(
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = context.getString(com.gf.mail.R.string.no_accounts),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = context.getString(com.gf.mail.R.string.no_accounts_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onAddAccountClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(context.getString(com.gf.mail.R.string.nav_add_account))
        }
    }
}

/**
 * Header showing account count and limit
 */
@Composable
private fun AccountsHeader(
    accountCount: Int,
    maxAccounts: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = context.getString(com.gf.mail.R.string.accounts_count, accountCount, maxAccounts),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (accountCount >= maxAccounts) {
                    Text(
                        text = context.getString(com.gf.mail.R.string.max_accounts_reached),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = context.getString(com.gf.mail.R.string.can_add_more_accounts, maxAccounts - accountCount, if (maxAccounts - accountCount == 1) "" else "s"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Individual account item
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AccountItem(
    account: Account,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToQRCode: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        elevation = if (isActive) {
            CardDefaults.cardElevation(defaultElevation = 4.dp)
        } else {
            CardDefaults.cardElevation(defaultElevation = 1.dp)
        },
        colors = if (isActive) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Provider icon
            Icon(
                imageVector = when (account.provider) {
                    EmailProvider.GMAIL -> Icons.Default.Email
                    EmailProvider.EXCHANGE -> Icons.Default.AccountCircle
                    EmailProvider.QQ -> Icons.Default.Email
                    EmailProvider.NETEASE -> Icons.Default.Email
                    EmailProvider.OUTLOOK -> Icons.Default.Email
                    EmailProvider.YAHOO -> Icons.Default.Email
                    EmailProvider.APPLE -> Icons.Default.Email
                    EmailProvider.IMAP, EmailProvider.POP3 -> Icons.Default.Email
                },
                contentDescription = account.provider.displayName,
                modifier = Modifier.size(40.dp),
                tint = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Account details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = account.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Text(
                    text = account.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Provider badge
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = account.provider.displayName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Auth type badge
                    Badge(
                        containerColor = when (account.authInfo.type) {
                            AuthenticationType.OAUTH2 -> Color(0xFF4CAF50)
                            AuthenticationType.APP_PASSWORD -> Color(0xFFFF9800)
                            AuthenticationType.PASSWORD -> Color(0xFF9C27B0)
                        }
                    ) {
                        Text(
                            text = when (account.authInfo.type) {
                                AuthenticationType.OAUTH2 -> "OAuth2"
                                AuthenticationType.APP_PASSWORD -> "App Password"
                                AuthenticationType.PASSWORD -> "Password"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }

                    // Status indicators
                    if (!account.isEnabled) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = "Disabled",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    } else if (account.authInfo.isTokenExpired()) {
                        Badge(
                            containerColor = Color(0xFFFF9800)
                        ) {
                            Text(
                                text = "Token Expired",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    } else if (!account.syncEnabled) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.outline
                        ) {
                            Text(
                                text = "Sync Off",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Menu button
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!isActive) {
                        DropdownMenuItem(
                            text = { Text("Set as Active") },
                            onClick = {
                                onSetActive()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            // TODO: Navigate to account settings
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    )

                    onNavigateToQRCode?.let { qrCodeNavigation ->
                        DropdownMenuItem(
                            text = { Text("Share QR Code") },
                            onClick = {
                                qrCodeNavigation(account.id)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.QrCode, contentDescription = null)
                            }
                        )
                    }

                    Divider()

                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}
