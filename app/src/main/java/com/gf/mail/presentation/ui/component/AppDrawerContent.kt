package com.gf.mail.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.AccountSummary
import com.gf.mail.presentation.navigation.GfmailDestination

/**
 * Enhanced drawer content with account switching
 */
@Composable
fun AppDrawerContent(
    currentAccount: Account?,
    availableAccounts: List<Account>,
    accountSummary: AccountSummary,
    currentDestination: String?,
    onDestinationClick: (String) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onAddAccountClick: () -> Unit,
    onManageAccountsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Account switcher section
        item {
            AccountSwitcher(
                currentAccount = currentAccount,
                availableAccounts = availableAccounts,
                accountSummary = accountSummary,
                onAccountSelected = onAccountSelected,
                onAddAccountClick = onAddAccountClick,
                onManageAccountsClick = onManageAccountsClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Navigation items
        item {
            DrawerNavigationSection(
                title = context.getString(com.gf.mail.R.string.nav_mail),
                items = listOf(
                    DrawerItem(
                        destination = GfmailDestination.Inbox.route,
                        icon = Icons.Default.Inbox,
                        label = context.getString(com.gf.mail.R.string.nav_inbox),
                        badge = null // TODO: Get real unread count from repository
                    ),
                    DrawerItem(
                        destination = GfmailDestination.Sent.route,
                        icon = Icons.Default.Send,
                        label = context.getString(com.gf.mail.R.string.nav_sent)
                    ),
                    DrawerItem(
                        destination = GfmailDestination.Drafts.route,
                        icon = Icons.Default.Drafts,
                        label = context.getString(com.gf.mail.R.string.nav_drafts)
                    ),
                    DrawerItem(
                        destination = GfmailDestination.Starred.route,
                        icon = Icons.Default.Star,
                        label = context.getString(com.gf.mail.R.string.nav_starred)
                    )
                ),
                currentDestination = currentDestination,
                onDestinationClick = onDestinationClick,
                enabled = currentAccount != null
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Other sections
        item {
            DrawerNavigationSection(
                title = context.getString(com.gf.mail.R.string.nav_tools),
                items = listOf(
                    DrawerItem(
                        destination = GfmailDestination.Search.route,
                        icon = Icons.Default.Search,
                        label = context.getString(com.gf.mail.R.string.nav_search)
                    ),
                    DrawerItem(
                        destination = GfmailDestination.Settings.route,
                        icon = Icons.Default.Settings,
                        label = context.getString(com.gf.mail.R.string.nav_settings)
                    )
                ),
                currentDestination = currentDestination,
                onDestinationClick = onDestinationClick
            )
        }

        // Account status section
        if (currentAccount == null) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                NoAccountCard(
                    onAddAccountClick = onAddAccountClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

/**
 * Navigation section with title and items
 */
@Composable
private fun DrawerNavigationSection(
    title: String,
    items: List<DrawerItem>,
    currentDestination: String?,
    onDestinationClick: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        items.forEach { item ->
            DrawerNavigationItem(
                item = item,
                isSelected = currentDestination == item.destination,
                enabled = enabled,
                onClick = { onDestinationClick(item.destination) }
            )
        }
    }
}

/**
 * Individual drawer navigation item
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DrawerNavigationItem(
    item: DrawerItem,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = null
            )
        },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    modifier = Modifier.weight(1f)
                )

                item.badge?.let { badge ->
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        selected = isSelected,
        onClick = if (enabled) onClick else { -> },
        modifier = modifier.padding(horizontal = 12.dp)
    )
}

/**
 * Card shown when no account is active
 */
@Composable
private fun NoAccountCard(
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = context.getString(com.gf.mail.R.string.no_account_active),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = context.getString(com.gf.mail.R.string.no_account_active_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddAccountClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(context.getString(com.gf.mail.R.string.nav_add_account))
            }
        }
    }
}

/**
 * Data class for drawer items
 */
data class DrawerItem(
    val destination: String,
    val icon: ImageVector,
    val label: String,
    val badge: String? = null
)
