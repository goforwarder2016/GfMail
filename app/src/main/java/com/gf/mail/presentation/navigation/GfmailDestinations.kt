package com.gf.mail.presentation.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.gf.mail.R

/**
 * Navigation destinations for the Gfmail app
 */
sealed class GfmailDestination(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector? = null
) {
    fun getTitle(context: Context): String = context.getString(titleResId)
    // Main app destinations
    object Inbox : GfmailDestination(
        route = "inbox",
        titleResId = R.string.nav_inbox,
        icon = Icons.Filled.Email
    )

    object Sent : GfmailDestination(
        route = "sent",
        titleResId = R.string.nav_sent,
        icon = Icons.Filled.Send
    )

    object Drafts : GfmailDestination(
        route = "drafts",
        titleResId = R.string.nav_drafts,
        icon = Icons.Filled.Edit
    )

    object Starred : GfmailDestination(
        route = "starred",
        titleResId = R.string.nav_starred,
        icon = Icons.Filled.Star
    )

    object Settings : GfmailDestination(
        route = "settings",
        titleResId = R.string.nav_settings,
        icon = Icons.Filled.Settings
    )

    object Search : GfmailDestination(
        route = "search?query={query}",
        titleResId = R.string.nav_search,
        icon = Icons.Filled.Search
    ) {
        fun createRoute(query: String = "") = "search?query=$query"
    }

    // Detail destinations
    object EmailDetail : GfmailDestination(
        route = "email_detail/{emailId}",
        titleResId = R.string.email_subject
    ) {
        fun createRoute(emailId: String) = "email_detail/$emailId"
    }

    object Compose : GfmailDestination(
        route = "compose?replyToId={replyToId}",
        titleResId = R.string.nav_compose
    ) {
        fun createRoute(replyToId: String? = null) =
            if (replyToId != null) "compose?replyToId=$replyToId" else "compose"
    }

    // Account management
    object AccountList : GfmailDestination(
        route = "accounts",
        titleResId = R.string.nav_accounts
    )

    object AddAccount : GfmailDestination(
        route = "add_account",
        titleResId = R.string.nav_add_account
    )

    object ConnectionTest : GfmailDestination(
        route = "connection_test/{accountId}",
        titleResId = R.string.connection_tests
    ) {
        fun createRoute(accountId: String) = "connection_test/$accountId"
    }

    object QRCodeDisplay : GfmailDestination(
        route = "qr_code/{accountId}",
        titleResId = R.string.qr_code_display
    ) {
        fun createRoute(accountId: String) = "qr_code/$accountId"
    }

    // Search (duplicate removed - already defined above)

    // Folder Management
    object FolderManagement : GfmailDestination(
        route = "folder_management/{accountId}",
        titleResId = R.string.folder_management
    ) {
        fun createRoute(accountId: String) = "folder_management/$accountId"
    }

    // Sync Settings
    object SyncSettings : GfmailDestination(
        route = "sync_settings",
        titleResId = R.string.settings_sync_settings
    )

    // Signature Management
    object SignatureManagement : GfmailDestination(
        route = "signature_management?accountId={accountId}",
        titleResId = R.string.settings_email_signatures
    ) {
        fun createRoute(accountId: String? = null) =
            if (accountId != null) "signature_management?accountId=$accountId" else "signature_management"
    }

    // Server Settings
    object ServerSettings : GfmailDestination(
        route = "server_settings/{accountId}",
        titleResId = R.string.settings_server_settings
    ) {
        fun createRoute(accountId: String) = "server_settings/$accountId"
    }

    // Accessibility Settings
    object AccessibilitySettings : GfmailDestination(
        route = "accessibility_settings",
        titleResId = R.string.accessibility_title
    )

    // Performance Settings
    object PerformanceSettings : GfmailDestination(
        route = "performance_settings",
        titleResId = R.string.performance_settings
    )

    // Security Settings
    object SecuritySettings : GfmailDestination(
        route = "security_settings",
        titleResId = R.string.security_settings
    )
}

/**
 * Navigation drawer destinations
 */
val drawerDestinations = listOf(
    GfmailDestination.Inbox,
    GfmailDestination.Starred,
    GfmailDestination.Sent,
    GfmailDestination.Drafts
)

/**
 * Bottom navigation bar destinations (if used on smaller screens)
 */
val bottomNavDestinations = listOf(
    GfmailDestination.Inbox,
    GfmailDestination.Sent,
    GfmailDestination.Starred,
    GfmailDestination.Settings
)
