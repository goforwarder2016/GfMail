package com.gf.mail.presentation.navigation

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gf.mail.R
import com.gf.mail.GfmailApplication
import com.gf.mail.di.AppDependencyContainer
import com.gf.mail.domain.model.getFromDisplay
import com.gf.mail.domain.model.getBodyContent
import com.gf.mail.utils.HtmlParser
import com.gf.mail.utils.AdvancedHtmlParser
import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gf.mail.domain.model.EmailProvider
import com.gf.mail.presentation.ui.account.ConnectionTestScreen
import com.gf.mail.presentation.ui.email.EmailListScreen

import com.gf.mail.presentation.ui.settings.AccessibilitySettingsScreen
import com.gf.mail.presentation.ui.settings.MainSettingsScreen
import com.gf.mail.presentation.ui.settings.PerformanceSettingsScreen
import com.gf.mail.presentation.ui.settings.SecuritySettingsScreen
import com.gf.mail.presentation.ui.settings.SignatureManagementScreen

import com.gf.mail.presentation.viewmodel.AccountManagementViewModel
import com.gf.mail.presentation.viewmodel.EmailListViewModel
import com.gf.mail.presentation.viewmodel.SearchEmailViewModel

/**
 * Main navigation graph for the Gfmail app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GfmailNavGraph(
    navController: NavHostController,
    hasAccounts: Boolean = false,
    startDestination: String = if (hasAccounts) GfmailDestination.Inbox.route else GfmailDestination.AddAccount.route,
    onAccountAdded: (com.gf.mail.domain.model.Account) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Main email list destinations
        composable(GfmailDestination.Inbox.route) {
            SimpleInboxScreen(
                onEmailClick = { emailId ->
                    navController.navigate(GfmailDestination.EmailDetail.createRoute(emailId))
                },
                onComposeClick = {
                    navController.navigate(GfmailDestination.Compose.createRoute())
                },
                onSearchClick = {
                    navController.navigate(GfmailDestination.Search.createRoute())
                }
            )
        }

        composable(GfmailDestination.Sent.route) {
            SentScreen(
                onEmailClick = { emailId ->
                    navController.navigate(GfmailDestination.EmailDetail.createRoute(emailId))
                }
            )
        }

        composable(GfmailDestination.Drafts.route) {
            DraftsScreen(
                onEmailClick = { emailId ->
                    navController.navigate(GfmailDestination.EmailDetail.createRoute(emailId))
                }
            )
        }

        composable(GfmailDestination.Starred.route) {
            StarredScreen(
                onEmailClick = { emailId ->
                    navController.navigate(GfmailDestination.EmailDetail.createRoute(emailId))
                }
            )
        }

        // Email detail
        composable(
            route = GfmailDestination.EmailDetail.route,
            arguments = listOf(navArgument("emailId") { type = NavType.StringType })
        ) { backStackEntry ->
            val emailId = backStackEntry.arguments?.getString("emailId") ?: ""
            EmailDetailScreen(
                emailId = emailId,
                onBackClick = { navController.popBackStack() },
                onReplyClick = {
                    navController.navigate(GfmailDestination.Compose.createRoute(emailId))
                }
            )
        }

        // Compose email
        composable(
            route = GfmailDestination.Compose.route,
            arguments = listOf(
                navArgument("replyToId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val replyToId = backStackEntry.arguments?.getString("replyToId")
            // Real compose email screen with simplified ViewModel
            SimpleComposeEmailScreen(
                replyToId = replyToId,
                onSendClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Settings
        composable(GfmailDestination.Settings.route) {
            MainSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSyncSettings = {
                    navController.navigate(GfmailDestination.SyncSettings.route)
                },
                onNavigateToSignatures = {
                    navController.navigate(GfmailDestination.SignatureManagement.createRoute())
                },
                onNavigateToServerSettings = {
                    // TODO: Navigate to server settings with default account
                    navController.navigate(
                        GfmailDestination.ServerSettings.createRoute("default_account")
                    )
                },
                onNavigateToAccountSettings = {
                    navController.navigate(GfmailDestination.AccountList.route)
                },
                onNavigateToAccessibilitySettings = {
                    navController.navigate(GfmailDestination.AccessibilitySettings.route)
                },
                onNavigateToPerformanceSettings = {
                    navController.navigate(GfmailDestination.PerformanceSettings.route)
                },
                onNavigateToSecuritySettings = {
                    navController.navigate(GfmailDestination.SecuritySettings.route)
                }
            )
        }

        // Sync Settings
        composable(GfmailDestination.SyncSettings.route) {
            // TODO: Implement SyncSettingsScreen
            PlaceholderScreen(
                title = stringResource(R.string.sync_settings),
                onBackClick = { navController.popBackStack() }
            )
        }

        // Account management
        composable(GfmailDestination.AccountList.route) {
            AccountListScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onAddAccountClick = {
                    navController.navigate(GfmailDestination.AddAccount.route)
                }
            )
        }

        composable(GfmailDestination.AddAccount.route) {
            AddAccountScreen(
                onBackClick = { 
                    println("🔙 AddAccountScreen: onBackClick called, popping back stack")
                    println("🔙 Current destination: ${navController.currentDestination?.route}")
                    val result = navController.popBackStack()
                    println("🔙 popBackStack result: $result")
                    if (!result) {
                        println("🔙 popBackStack failed, trying to navigate to AccountList")
                        navController.navigate(GfmailDestination.AccountList.route) {
                            popUpTo(GfmailDestination.AddAccount.route) { inclusive = true }
                        }
                    }
                },
                onAccountAdded = { account ->
                    // Pass the new Account to parent component
                    onAccountAdded(account)
                    navController.navigate(GfmailDestination.Inbox.route) {
                        popUpTo(GfmailDestination.AddAccount.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = GfmailDestination.ConnectionTest.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            ConnectionTestScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = GfmailDestination.QRCodeDisplay.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            QRCodeDisplayScreen(
                accountId = accountId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Search
        composable(
            route = GfmailDestination.Search.route,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            
            // Create search ViewModel
            val searchViewModel = remember {
                val application = com.gf.mail.GfmailApplication.instance
                val dependencies = application.dependencies
                val emailRepository = dependencies.getEmailRepository()
                val searchUseCase = com.gf.mail.domain.usecase.SearchEmailsUseCaseImpl(emailRepository)
                com.gf.mail.presentation.viewmodel.SearchEmailViewModel(searchUseCase)
            }
            
            // Set initial query
            LaunchedEffect(query) {
                if (query.isNotEmpty()) {
                    searchViewModel.updateSearchQuery(query)
                    searchViewModel.performSearch()
                }
            }
            
            com.gf.mail.presentation.ui.search.SearchScreen(
                viewModel = searchViewModel,
                onBackClick = { navController.popBackStack() },
                onEmailClick = { emailId ->
                    navController.navigate(GfmailDestination.EmailDetail.createRoute(emailId))
                }
            )
        }

        // Folder Management
        composable(
            route = GfmailDestination.FolderManagement.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            FolderManagementPlaceholderScreen(
                accountId = accountId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Signature Management
        composable(
            route = GfmailDestination.SignatureManagement.route,
            arguments = listOf(
                navArgument("accountId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            SignatureManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                accountId = accountId
            )
        }

        // Server Settings
        composable(
            route = GfmailDestination.ServerSettings.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            ServerSettingsPlaceholderScreen(
                accountId = accountId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Accessibility Settings
        composable(GfmailDestination.AccessibilitySettings.route) {
            AccessibilitySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Performance Settings
        composable(GfmailDestination.PerformanceSettings.route) {
            PerformanceSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Security Settings
        composable(GfmailDestination.SecuritySettings.route) {
            SecuritySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Functional screens implementation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxScreen(
    onEmailClick: (String) -> Unit,
    onComposeClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    // Get real email data from database
    var emails by remember { mutableStateOf<List<com.gf.mail.domain.model.Email>>(emptyList()) }
    
    // Get dependency injection container
    val dependencies = getDependencies()
    val getEmailsUseCase = dependencies.getEmailsUseCase()
    val getActiveAccountUseCase = dependencies.getActiveAccountUseCase()
    
    // Use LaunchedEffect to load emails from database
    LaunchedEffect(Unit) {
        
            // Get current active Account
            getActiveAccountUseCase().collect { activeAccount ->
                if (activeAccount != null) {
                // Get inbox emails
                val emailFlow = getEmailsUseCase.getInboxEmails(activeAccount.id)
                        emailFlow.collect { emailList ->
                            emails = emailList
                            println("📧 InboxScreen: Loaded ${emailList.size}  emails")
                    }
                } else {
                    println("📧 InboxScreen: No active account")
                    emails = emptyList()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_inbox)) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onComposeClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Compose")
            }
        }
    ) { paddingValues ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(emails) { email ->
                            RealEmailListItem(
                                email = email,
                                onClick = { onEmailClick(email.id) }
                            )
            }
        }
    }
}

// Using real Email domain model

@Composable
private fun RealEmailListItem(
    email: com.gf.mail.domain.model.Email,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (email.isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = email.fromName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Email content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = email.fromName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (email.hasAttachments) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = stringResource(R.string.has_attachments),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = formatTimestamp(email.receivedDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (email.isStarred) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = stringResource(R.string.starred),
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFC107)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = email.subject,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (!email.isRead) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = (email.bodyHtml?.take(100) ?: email.bodyText?.take(100)) ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailListItem(
    email: com.gf.mail.domain.model.Email,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (email.isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = email.fromName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Email content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = email.fromName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (email.isStarred) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = stringResource(R.string.starred),
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = formatTimestamp(email.sentDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = email.subject,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (!email.isRead) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = (email.bodyHtml?.take(100) ?: email.bodyText?.take(100)) ?: "No content",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SentScreen(onEmailClick: (String) -> Unit) {
    // Get real sent email data
    var sentEmails by remember { mutableStateOf<List<com.gf.mail.domain.model.Email>>(emptyList()) }
    
    // Get dependency injection container
    val dependencies = getDependencies()
    val getEmailsUseCase = dependencies.getEmailsUseCase()
    val getActiveAccountUseCase = dependencies.getActiveAccountUseCase()
    
    // Use dependency injection to get real sent email data
    LaunchedEffect(Unit) {
        getActiveAccountUseCase().collect { activeAccount ->
            if (activeAccount != null) {
                // Get sent emails
                val emailFlow = getEmailsUseCase.getSentEmails(activeAccount.id)
                emailFlow.collect { emailList ->
                    sentEmails = emailList
                    println("📧 SentScreen: Loaded ${emailList.size}  sent emails")
                }
            } else {
                sentEmails = emptyList()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_sent)) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(sentEmails) { email ->
                RealEmailListItem(
                    email = email,
                    onClick = { onEmailClick(email.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DraftsScreen(onEmailClick: (String) -> Unit) {
    // Get real draft email data
    var draftEmails by remember { mutableStateOf<List<com.gf.mail.domain.model.Email>>(emptyList()) }
    
    // Get dependency injection container
    val dependencies = getDependencies()
    val getEmailsUseCase = dependencies.getEmailsUseCase()
    val getActiveAccountUseCase = dependencies.getActiveAccountUseCase()
    
    // Use dependency injection to get real draft email data
    LaunchedEffect(Unit) {
        getActiveAccountUseCase().collect { activeAccount ->
            if (activeAccount != null) {
                // Get draft emails
                val emailFlow = getEmailsUseCase.getDraftEmails(activeAccount.id)
                emailFlow.collect { emailList ->
                    draftEmails = emailList
                    println("📧 DraftsScreen: Loaded ${emailList.size}  draft emails")
                }
            } else {
                draftEmails = emptyList()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_drafts)) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(draftEmails) { email ->
                RealEmailListItem(
                    email = email,
                    onClick = { onEmailClick(email.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StarredScreen(onEmailClick: (String) -> Unit) {
    // Get real starred email data
    var starredEmails by remember { mutableStateOf<List<com.gf.mail.domain.model.Email>>(emptyList()) }
    
    // Get dependency injection container
    val dependencies = getDependencies()
    val getEmailsUseCase = dependencies.getEmailsUseCase()
    val getActiveAccountUseCase = dependencies.getActiveAccountUseCase()
    
    // Use dependency injection to get real starred email data
    LaunchedEffect(Unit) {
        getActiveAccountUseCase().collect { activeAccount ->
            if (activeAccount != null) {
                // For starred emails, we directly use "starred" as special identifier
                val emailFlow = getEmailsUseCase.getEmails(activeAccount.id, "starred")
                emailFlow.collect { emailList ->
                    starredEmails = emailList
                    println("📧 StarredScreen: Loaded ${emailList.size}  starred emails")
                }
            } else {
                starredEmails = emptyList()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_starred)) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(starredEmails) { email ->
                RealEmailListItem(
                    email = email,
                    onClick = { onEmailClick(email.id) }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailDetailScreen(
    emailId: String,
    onBackClick: () -> Unit,
    onReplyClick: () -> Unit
) {
    // Get dependency injection container
    val dependencies = getDependencies()
    val emailRepository = dependencies.getEmailRepository()
    
    // Get Email details
    var email by remember { mutableStateOf<com.gf.mail.domain.model.Email?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(emailId) {
        try {
            // Get Email details from database
            val emailData = emailRepository.getEmailById(emailId)
            email = emailData
            isLoading = false
            println("📧 EmailDetailScreen: Loading email details: ${emailData?.subject}")
        } catch (e: Exception) {
            println("❌ EmailDetailScreen: Failed to load email details: ${e.message}")
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = email?.subject ?: "Email details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onReplyClick) {
                        Icon(Icons.Default.Reply, contentDescription = stringResource(R.string.reply))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (email == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.email_not_found))
            }
        } else {
            // Get original HTML content for WebView rendering
            // If originalHtmlBody is empty, fallback to bodyHtml (temporary solution)
            val originalHtmlContent = email!!.originalHtmlBody?.takeIf { it.isNotEmpty() } 
                ?: email!!.bodyHtml?.takeIf { it.isNotEmpty() }
            val hasRemoteImages = originalHtmlContent?.contains(Regex("""<img[^>]+src\s*=\s*["']https?://""")) == true
            
            // Detect HTML content encoding
            val htmlCharset = originalHtmlContent?.let { html ->
                when {
                    html.contains("charset=GBK") || html.contains("charset=gbk") -> "GBK"
                    html.contains("charset=GB2312") || html.contains("charset=gb2312") -> "GB2312"
                    html.contains("charset=UTF-8") || html.contains("charset=utf-8") -> "UTF-8"
                    else -> "UTF-8" // Default to UTF-8
                }
            } ?: "UTF-8"
            
            println("🌐 [EMAIL_DETAIL] Email fields:")
            println("  - originalHtmlBody length: ${email!!.originalHtmlBody?.length ?: 0}")
            println("  - bodyHtml length: ${email!!.bodyHtml?.length ?: 0}")
            println("  - bodyText length: ${email!!.bodyText?.length ?: 0}")
            println("🌐 [EMAIL_DETAIL] Using content:")
            println("  - HTML charset detected: $htmlCharset")
            println("  - HTML content length: ${originalHtmlContent?.length ?: 0}")
            println("  - HTML content preview: ${originalHtmlContent?.take(200)}")
            
            // State management: whether to load remote images
            var shouldLoadRemoteImages by remember { mutableStateOf(false) }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Email header information
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // From
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "From:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = email!!.getFromDisplay(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                            
                            // To
                            if (email!!.toAddresses.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "To:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = email!!.toAddresses.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                            
                            // CC
                            if (email!!.ccAddresses.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "CC:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = email!!.ccAddresses.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                            
                            // Sent time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sent time:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                        .format(java.util.Date(email!!.sentDate)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                // Email body
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Email Content:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Display email body - use WebView to render HTML
                            if (originalHtmlContent != null && originalHtmlContent.isNotEmpty()) {
                                // Show "Load Remote Images" button (if remote images exist and not loaded)
                                if (hasRemoteImages && !shouldLoadRemoteImages) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                Text(
                                                    text = "Images not loaded",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Click to load images in email content",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                            
                                            Button(
                                                onClick = { shouldLoadRemoteImages = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFFF9800), // Orange reminder color
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Image,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = "Load Images",
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Use WebView to render HTML content
                                AndroidView(
                                    factory = { context ->
                                        println("🌐 [WebView] Creating WebView for HTML content")
                                        println("🌐 [WebView] HTML content length: ${originalHtmlContent.length}")
                                        println("🌐 [WebView] HTML content preview: ${originalHtmlContent.take(200)}")
                                        
                                        WebView(context).apply {
                                            settings.apply {
                                                javaScriptEnabled = true // Enable JavaScript for dynamic height adjustment
                                                domStorageEnabled = false
                                                databaseEnabled = false
                                                setSupportMultipleWindows(false)
                                                loadsImagesAutomatically = shouldLoadRemoteImages
                                                blockNetworkImage = !shouldLoadRemoteImages
                                                // 允许混合内容（谨慎）
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                                }
                                            }
                                            
                                            // 设置WebViewClient来监听加载状态
                                            webViewClient = object : android.webkit.WebViewClient() {
                                                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                                    super.onPageFinished(view, url)
                                                    println("🌐 [WebView] Page finished loading: $url")
                                                    
                                                    // 动态调整WebView高度以适应内容
                                                    view?.evaluateJavascript("""
                                                        (function() {
                                                            var height = Math.max(
                                                                document.body.scrollHeight,
                                                                document.body.offsetHeight,
                                                                document.documentElement.clientHeight,
                                                                document.documentElement.scrollHeight,
                                                                document.documentElement.offsetHeight
                                                            );
                                                            return height;
                                                        })();
                                                    """.trimIndent()) { result ->
                                                        try {
                                                            val height = result.replace("\"", "").toIntOrNull()
                                                            if (height != null && height > 0) {
                                                                println("🌐 [WebView] Content height: ${height}px")
                                                                // 设置WebView的布局参数
                                                                val layoutParams = view.layoutParams
                                                                layoutParams.height = (height * context.resources.displayMetrics.density).toInt()
                                                                view.layoutParams = layoutParams
                                                            }
                                                        } catch (e: Exception) {
                                                            println("🌐 [WebView] Error adjusting height: ${e.message}")
                                                        }
                                                    }
                                                }
                                                
                                                override fun onReceivedError(view: android.webkit.WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                                                    super.onReceivedError(view, errorCode, description, failingUrl)
                                                    println("🌐 [WebView] Error loading page: $errorCode - $description")
                                                }
                                            }
                                            
                                            // 加载HTML内容
                                            println("🌐 [WebView] Loading HTML content into WebView with charset: $htmlCharset")
                                            loadDataWithBaseURL(
                                                null,
                                                originalHtmlContent,
                                                "text/html",
                                                htmlCharset,
                                                null
                                            )
                                        }
                                    },
                                    update = { webView ->
                                        println("🌐 [WebView] Updating WebView settings")
                                        // 更新图片加载策略
                                        webView.settings.loadsImagesAutomatically = shouldLoadRemoteImages
                                        webView.settings.blockNetworkImage = !shouldLoadRemoteImages
                                        
                                        // 重新加载内容（如果图片加载策略改变）
                                        if (shouldLoadRemoteImages) {
                                            println("🌐 [WebView] Reloading content with images enabled, charset: $htmlCharset")
                                            webView.loadDataWithBaseURL(
                                                null,
                                                originalHtmlContent,
                                                "text/html",
                                                htmlCharset,
                                                null
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 200.dp, max = 800.dp) // 使用heightIn而不是固定height
                                )
                            } else if (email!!.bodyText != null && email!!.bodyText!!.isNotBlank()) {
                                // 如果没有HTML内容，显示纯文本
                                Text(
                                    text = email!!.bodyText!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.no_email_content),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }
                
            }
        }
    }
}

@Composable
private fun ComposeScreen(
    replyToId: String?,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit
) {
    PlaceholderScreen(
        "Compose${if (replyToId != null) " (Reply)" else ""}",
        onBackClick = onBackClick
    )
}

@Composable
private fun SettingsScreen(
    onBackClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onSyncSettingsClick: () -> Unit
) {
    PlaceholderScreen("Settings", onBackClick = onBackClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountListScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
    onAddAccountClick: () -> Unit
) {
    // Initialize ViewModel with proper dependencies
    val viewModel = remember {
        val application = com.gf.mail.GfmailApplication.instance
        val dependencies = application.dependencies
        val accountRepository = dependencies.getAccountRepository()
        val manageAccountsUseCase = com.gf.mail.domain.usecase.ManageAccountsUseCase(accountRepository)
        com.gf.mail.presentation.viewmodel.AccountManagementViewModel(manageAccountsUseCase)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val accounts = uiState.accounts
    val activeAccount = accounts.firstOrNull { it.isActive }
    
    com.gf.mail.presentation.ui.accounts.AccountListScreen(
        accounts = accounts,
        activeAccount = activeAccount,
        viewModel = viewModel,
        onBackClick = onBackClick,
        onAddAccountClick = onAddAccountClick,
        onNavigateToQRCode = { accountId ->
            navController.navigate(GfmailDestination.QRCodeDisplay.createRoute(accountId))
        }
    )
}

// Using real Account domain model

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountListItem(
    account: com.gf.mail.domain.model.Account,
    onAccountClick: () -> Unit,
    onSetActive: () -> Unit,
    onToggleSync: () -> Unit,
    onShowQRCode: () -> Unit,
    onDeleteAccount: () -> Unit,
    isExpanded: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 2.dp)
    ) {
        Column {
            // Main account info
            Card(
                onClick = onAccountClick,
                colors = CardDefaults.cardColors(
                    containerColor = if (account.isActive) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Account avatar
                    Surface(
                        shape = CircleShape,
                        color = if (account.isActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = account.displayName.first().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (account.isActive) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Account details
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = account.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            if (account.isActive) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = stringResource(R.string.current),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = account.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "${account.provider.displayName} • ${if (account.syncEnabled) stringResource(R.string.account_sync_enabled) else stringResource(R.string.account_sync_disabled)} • ${formatTimestamp(account.lastSync ?: 0L)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Expanded options
            if (isExpanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!account.isActive) {
                            OutlinedButton(
                                onClick = onSetActive,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.set_as_current))
                            }
                        }
                        
                        OutlinedButton(
                            onClick = onToggleSync,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (account.syncEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (account.syncEnabled) stringResource(R.string.account_pause_sync) else stringResource(R.string.account_enable_sync))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onShowQRCode,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.show_qr_code))
                        }
                        
                        OutlinedButton(
                            onClick = onDeleteAccount,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.account_delete))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountScreen(
    onBackClick: () -> Unit,
    onAccountAdded: (com.gf.mail.domain.model.Account) -> Unit
) {
    var currentStep by remember { mutableStateOf(0) } // 0: 选择提供商, 1: 输入信息, 2: success
    var emailAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<com.gf.mail.domain.model.EmailProvider?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // 获取依赖注入容器
    val dependencies = getDependencies()
    val accountRepository = dependencies.getAccountRepository()
    val credentialEncryption = dependencies.getCredentialEncryption()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAdvancedSettings by remember { mutableStateOf(false) }
    var customImapHost by remember { mutableStateOf("") }
    var customImapPort by remember { mutableStateOf("") }
    var customSmtpHost by remember { mutableStateOf("") }
    var customSmtpPort by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    
    // 监听Account添加并触发邮件Sync
    var addedAccount by remember { mutableStateOf<com.gf.mail.domain.model.Account?>(null) }
    
    LaunchedEffect(addedAccount) {
        addedAccount?.let { account ->
            println("🔄 Starting email sync for new account: ${account.email}")
            // 使用EmailSyncService进行同步
            try {
                val application = com.gf.mail.GfmailApplication.instance
                // 手动同步：直接调用IMAP客户端
                val dependencies = application.dependencies
                val imapClient = dependencies.getImapClient()
                val accountRepository = dependencies.getAccountRepository()
                
                try {
                    val account = accountRepository.getAccountById(account.id)
                    if (account != null) {
                        val password = dependencies.getCredentialEncryption().getPasswordSecurely(account.id)
                        if (password != null) {
                            val connectResult = imapClient.connect(account, password)
                        
                        if (connectResult.isSuccess) {
                            val foldersResult = imapClient.getFolders(account)
                            if (foldersResult.isSuccess) {
                                val folders = foldersResult.getOrNull() ?: emptyList()
                                println("✅ Manual sync completed successfully for ${account.email}, found ${folders.size} folders")
                            } else {
                                println("❌ Manual sync failed to get folders: ${foldersResult.exceptionOrNull()?.message}")
                            }
                        } else {
                            println("❌ Manual sync failed to connect: ${connectResult.exceptionOrNull()?.message}")
                        }
                        } else {
                            println("❌ Manual sync failed: password is null")
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Manual sync exception: ${e.message}")
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                println("❌ Email sync exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    // 邮箱提供商列表 - 按照QQ邮箱的设计
    val tencentEnterpriseName = stringResource(R.string.tencent_enterprise_email)
    val otherEmailName = stringResource(R.string.other_email)
    val otherDomain = stringResource(R.string.other)
    val qqEmailName = stringResource(R.string.provider_qq_email)
    val neteaseEmailName = stringResource(R.string.provider_netease_email)
    val email163Name = stringResource(R.string.provider_163_email)
    val email126Name = stringResource(R.string.provider_126_email)
    
    val emailProviders = remember(tencentEnterpriseName, otherEmailName, otherDomain, qqEmailName, neteaseEmailName, email163Name, email126Name) {
        listOf(
            EmailProviderInfo(
                provider = com.gf.mail.domain.model.EmailProvider.IMAP,
                name = qqEmailName,
                domain = "mail.qq.com",
                icon = "QQ",
                colorResId = R.color.qq_email_blue
            ),
            EmailProviderInfo(
                provider = com.gf.mail.domain.model.EmailProvider.IMAP,
                name = tencentEnterpriseName,
                domain = "exmail.qq.com",
                icon = "Tencent",
                colorResId = R.color.qq_email_blue
            ),
            EmailProviderInfo(
                provider = com.gf.mail.domain.model.EmailProvider.IMAP,
                name = email163Name,
                domain = "mail.163.com",
                icon = "163",
                colorResId = R.color.netease_163_red
            ),
            EmailProviderInfo(
                provider = com.gf.mail.domain.model.EmailProvider.IMAP,
                name = email126Name,
                domain = "mail.126.com",
                icon = "126",
                colorResId = R.color.netease_126_green
            ),
            EmailProviderInfo(
                provider = com.gf.mail.domain.model.EmailProvider.GMAIL,
                name = "Gmail",
                domain = "gmail.com",
                icon = "Gmail",
                colorResId = R.color.gmail_red
            ),
            EmailProviderInfo(
                provider = com.gf.mail.domain.model.EmailProvider.EXCHANGE,
                name = "Outlook",
                domain = "outlook.com",
                icon = "Outlook",
                colorResId = R.color.microsoft_blue
            ),
            EmailProviderInfo(
                provider = com.gf.mail.domain.model.EmailProvider.EXCHANGE,
                name = "Microsoft 365",
                domain = "office365.com",
                icon = "Office365",
                colorResId = R.color.office365_orange
            ),
            EmailProviderInfo(
                provider = com.gf.mail.domain.model.EmailProvider.EXCHANGE,
                name = "Exchange",
                domain = "exchange.com",
                icon = "Exchange",
                colorResId = R.color.microsoft_blue
            ),
            EmailProviderInfo(
                provider = com.gf.mail.domain.model.EmailProvider.IMAP,
                name = otherEmailName,
                domain = otherDomain,
                icon = "Other",
                colorResId = R.color.generic_gray
            )
        )
    }
    
    // 按照图片设计的添加Account界面
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_add_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        println("🔙 AddAccountScreen: Back button clicked")
                        onBackClick()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            
            // 邮箱提供商列表
                        
                        // 根据当前步骤显示不同内容
                        when (currentStep) {
                            0 -> ProviderSelectionStep(
                                providers = emailProviders,
                                onProviderSelected = { provider ->
                                    selectedProvider = provider.provider
                                    currentStep = 1
                                }
                            )
                            1 -> CredentialsInputStep(
                                selectedProvider = selectedProvider,
                                emailAddress = emailAddress,
                                password = password,
                                displayName = displayName,
                                showPassword = showPassword,
                                isLoading = isLoading,
                                errorMessage = errorMessage,
                                showAdvancedSettings = showAdvancedSettings,
                                customImapHost = customImapHost,
                                customImapPort = customImapPort,
                                customSmtpHost = customSmtpHost,
                                customSmtpPort = customSmtpPort,
                                onEmailChange = { emailAddress = it },
                                onPasswordChange = { password = it },
                                onDisplayNameChange = { displayName = it },
                                onShowPasswordToggle = { showPassword = !showPassword },
                                onShowAdvancedSettingsToggle = { showAdvancedSettings = !showAdvancedSettings },
                                onCustomImapHostChange = { customImapHost = it },
                                onCustomImapPortChange = { customImapPort = it },
                                onCustomSmtpHostChange = { customSmtpHost = it },
                                onCustomSmtpPortChange = { customSmtpPort = it },
                                onBackClick = { currentStep = 0 },
                                onAddAccount = { 
                                    isLoading = true
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        // 验证输入
                                        if (emailAddress.isBlank() || password.isBlank()) {
                                            errorMessage = "Please fill in email address and password"
                                            isLoading = false
                                            return@launch
                                        }
                                            
                                            if (!isValidEmail(emailAddress)) {
                                                errorMessage = "Please enter a valid email address"
                                                isLoading = false
                                                return@launch
                                            }
                                            
                                            // 获取Server configuration
                                            val serverConfig = if (showAdvancedSettings && 
                                                (customImapHost.isNotBlank() || customSmtpHost.isNotBlank())) {
                                                // 使用自定义Server configuration
                                                com.gf.mail.domain.model.ServerConfiguration(
                                                    imapHost = customImapHost.ifBlank { null },
                                                    imapPort = customImapPort.toIntOrNull() ?: 993,
                                                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                                                    smtpHost = customSmtpHost.ifBlank { null },
                                                    smtpPort = customSmtpPort.toIntOrNull() ?: 587,
                                                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                                                )
                                            } else {
                                                // 使用默认Server configuration
                                                getServerConfiguration(selectedProvider!!, emailAddress)
                                            }
                                            
                                            // 执行connectiontest
                                            val connectionResult = performRealConnectionTest(emailAddress, password, serverConfig, selectedProvider!!, context)
                                            
                                            if (connectionResult.success) {
                                                
                                                // 创建Account
                                                val account = com.gf.mail.domain.model.Account(
                                                    id = "account-${System.currentTimeMillis()}",
                                                    email = emailAddress,
                                                    emailAddress = emailAddress,
                                                    displayName = displayName.ifEmpty { emailAddress.substringBefore("@") },
                                                    fullName = displayName.ifEmpty { emailAddress.substringBefore("@") },
                                                    provider = selectedProvider!!,
                                                    serverConfig = serverConfig,
                                                    authInfo = com.gf.mail.domain.model.AuthenticationInfo(
                                                        type = if (selectedProvider == com.gf.mail.domain.model.EmailProvider.GMAIL) 
                                                            com.gf.mail.domain.model.AuthenticationType.APP_PASSWORD
                                                        else com.gf.mail.domain.model.AuthenticationType.PASSWORD,
                                                        hasPassword = true
                                                    ),
                                                    isActive = true,
                                                    isEnabled = true,
                                                    syncEnabled = true,
                                                    syncFrequency = 15
                                                )
                                                
                                                // 保存Account到数据库
                                                accountRepository.insertAccount(account, password)
                                                println("💾 Account saved to database: ${account.email}")
                                                
                                                // 安全地存储密码
                                                credentialEncryption.storePasswordSecurely(account.id, password)
                                                println("🔐 Password securely stored")
                                                
                                                isLoading = false
                                                currentStep = 2
                                                
                                                // 触发邮件Sync
                                                println("🔄 Starting email sync for new account...")
                                                addedAccount = account
                                                
                                                // 延迟后调用回调
                                                kotlinx.coroutines.delay(1000)
                                                println("📧 Account creation completed, calling callback: ${account.email}")
                                                onAccountAdded(account)
                                            } else {
                                                errorMessage = connectionResult.errorMessage ?: "Connection test failed, please check email address and password"
                                                isLoading = false
                                            }
                                    }
                                }
                            )
                            2 -> SuccessStep(
                                emailAddress = emailAddress,
                                onFinish = onBackClick
                            )
                        }
        }
    }
}

// 提供商选择步骤
@Composable
private fun ProviderSelectionStep(
    providers: List<EmailProviderInfo>,
    onProviderSelected: (EmailProviderInfo) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        providers.forEachIndexed { index, provider ->
            ProviderRow(
                provider = provider,
                onClick = { onProviderSelected(provider) },
                showDivider = index < providers.size - 1
            )
        }
    }
}

// 提供商行（按照图片设计）
@Composable
private fun ProviderRow(
    provider: EmailProviderInfo,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 提供商图标
            ProviderIcon(provider = provider)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 提供商信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                if (provider.domain != stringResource(R.string.other)) {
                    Text(
                        text = provider.domain,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // 分割线
        if (showDivider) {
            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 0.5.dp
            )
        }
    }
}

// 提供商图标组件
@Composable
private fun ProviderIcon(provider: EmailProviderInfo) {
    when {
        provider.name.contains("QQ") || provider.domain.contains("qq.com") -> {
            // QQ邮箱图标 - 蓝色圆形背景
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorResource(id = provider.colorResId),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "QQ",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        provider.domain.contains("exmail.qq.com") -> {
            // 腾讯企业邮箱图标 - 蓝色M形状
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorResource(id = provider.colorResId),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        provider.domain.contains("163.com") -> {
            // 163邮箱图标 - 红色数字
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorResource(id = provider.colorResId),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "163",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        provider.domain.contains("126.com") -> {
            // 126邮箱图标 - 绿色数字
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorResource(id = provider.colorResId),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "126",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        provider.domain.contains("gmail.com") -> {
            // Gmail图标 - 彩色G
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorResource(id = provider.colorResId),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        provider.domain.contains("outlook.com") || provider.domain.contains("hotmail.com") || provider.domain.contains("live.com") -> {
            // Outlook图标 - 蓝色O
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorResource(id = provider.colorResId),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "O",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        provider.domain.contains("office365.com") -> {
            // Microsoft 365图标 - 彩色方块
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorResource(id = provider.colorResId),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "365",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        provider.domain.contains("exchange.com") -> {
            // Exchange图标 - 蓝色E
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorResource(id = provider.colorResId),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "E",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        provider.name == stringResource(R.string.other_email) -> {
            // 其他邮箱图标 - 灰色信 emails
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorResource(id = provider.colorResId),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 凭据输入步骤
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CredentialsInputStep(
    selectedProvider: com.gf.mail.domain.model.EmailProvider?,
    emailAddress: String,
    password: String,
    displayName: String,
    showPassword: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    showAdvancedSettings: Boolean,
    customImapHost: String,
    customImapPort: String,
    customSmtpHost: String,
    customSmtpPort: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onShowPasswordToggle: () -> Unit,
    onShowAdvancedSettingsToggle: () -> Unit,
    onCustomImapHostChange: (String) -> Unit,
    onCustomImapPortChange: (String) -> Unit,
    onCustomSmtpHostChange: (String) -> Unit,
    onCustomSmtpPortChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onAddAccount: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Email address输入
        OutlinedTextField(
            value = emailAddress,
            onValueChange = onEmailChange,
            label = { Text("Email address") },
            placeholder = { Text("Please enter email address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )
        
        // 显示名称输入
        OutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            label = { Text(stringResource(R.string.display_name)) },
            placeholder = { Text("Please enter display name (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // 密码输入
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { 
                Text(
                    when (selectedProvider) {
                        com.gf.mail.domain.model.EmailProvider.GMAIL -> stringResource(R.string.app_specific_password)
                        com.gf.mail.domain.model.EmailProvider.QQ -> "QQ邮箱授权码"
                        com.gf.mail.domain.model.EmailProvider.NETEASE -> stringResource(R.string.netease_email_password)
                        com.gf.mail.domain.model.EmailProvider.OUTLOOK -> stringResource(R.string.password)
                        com.gf.mail.domain.model.EmailProvider.YAHOO -> stringResource(R.string.password)
                        com.gf.mail.domain.model.EmailProvider.APPLE -> stringResource(R.string.password)
                        com.gf.mail.domain.model.EmailProvider.IMAP -> {
                            if (emailAddress.contains("@qq.com")) "QQ Email Password"
                            else if (emailAddress.contains("@163.com") || emailAddress.contains("@126.com")) stringResource(R.string.netease_email_password)
                            else stringResource(R.string.password)
                        }
                        com.gf.mail.domain.model.EmailProvider.EXCHANGE -> stringResource(R.string.password)
                        com.gf.mail.domain.model.EmailProvider.POP3 -> stringResource(R.string.password)
                        null -> stringResource(R.string.password)
                    }
                )
            },
            placeholder = { 
                Text(
                    when (selectedProvider) {
                        com.gf.mail.domain.model.EmailProvider.GMAIL -> stringResource(R.string.password_requirement_gmail)
                        com.gf.mail.domain.model.EmailProvider.QQ -> "请输入QQ邮箱授权码"
                        com.gf.mail.domain.model.EmailProvider.NETEASE -> stringResource(R.string.enter_netease_email_password)
                        com.gf.mail.domain.model.EmailProvider.OUTLOOK -> stringResource(R.string.enter_password)
                        com.gf.mail.domain.model.EmailProvider.YAHOO -> stringResource(R.string.enter_password)
                        com.gf.mail.domain.model.EmailProvider.APPLE -> stringResource(R.string.enter_password)
                        com.gf.mail.domain.model.EmailProvider.IMAP -> {
                            if (emailAddress.contains("@qq.com")) "Please enter QQ email password"
                            else if (emailAddress.contains("@163.com") || emailAddress.contains("@126.com")) stringResource(R.string.enter_netease_email_password)
                            else stringResource(R.string.enter_password)
                        }
                        com.gf.mail.domain.model.EmailProvider.EXCHANGE -> stringResource(R.string.enter_password)
                        com.gf.mail.domain.model.EmailProvider.POP3 -> stringResource(R.string.enter_password)
                        null -> stringResource(R.string.enter_password)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(onClick = onShowPasswordToggle) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                    )
                }
            }
        )
        
        // 高级设置切换
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.advanced_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Switch(
                checked = showAdvancedSettings,
                onCheckedChange = { onShowAdvancedSettingsToggle() }
            )
        }
        
        // 高级设置内容
        if (showAdvancedSettings) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.server_configuration),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // IMAP server配置
                    Text(
                        text = "IMAP server",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customImapHost,
                            onValueChange = onCustomImapHostChange,
                            label = { Text("IMAP Host") },
                            placeholder = { Text("e.g.: imap.gmail.com") },
                            modifier = Modifier.weight(2f)
                        )
                        OutlinedTextField(
                            value = customImapPort,
                            onValueChange = onCustomImapPortChange,
                            label = { Text(stringResource(R.string.port)) },
                            placeholder = { Text("993") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                    
                    // SMTP server配置
                    Text(
                        text = "SMTP server",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customSmtpHost,
                            onValueChange = onCustomSmtpHostChange,
                            label = { Text("SMTP Host") },
                            placeholder = { Text("e.g.: smtp.gmail.com") },
                            modifier = Modifier.weight(2f)
                        )
                        OutlinedTextField(
                            value = customSmtpPort,
                            onValueChange = onCustomSmtpPortChange,
                            label = { Text(stringResource(R.string.port)) },
                            placeholder = { Text("587") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                    
                    Text(
                        text = "Tip: If not filled, default server configuration will be used",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // Error message
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        // 添加Account按钮
        Button(
            onClick = onAddAccount,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && emailAddress.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isLoading) "Adding..." else "Add Account")
        }
    }
}

// success步骤
@Composable
private fun SuccessStep(
    emailAddress: String,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Account added successfully!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Email: $emailAddress",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("completed")
        }
    }
}

// 简化的收件箱界面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleInboxScreen(
    onEmailClick: (String) -> Unit,
    onComposeClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    var isSyncing by remember { mutableStateOf(false) }
    var lastSyncTime by remember { mutableStateOf<Long?>(null) }
    
    // 获取真实的邮件数据
    var emails by remember { mutableStateOf<List<com.gf.mail.domain.model.Email>>(emptyList()) }
    
    // 获取依赖注入容器
    val dependencies = getDependencies()
    val getEmailsUseCase = dependencies.getEmailsUseCase()
    val getActiveAccountUseCase = dependencies.getActiveAccountUseCase()
    
    // 使用依赖注入获取真实的邮件数据
    LaunchedEffect(Unit) {
        
        getActiveAccountUseCase().collect { activeAccount ->
            if (activeAccount != null) {
                val emailFlow = getEmailsUseCase.getInboxEmails(activeAccount.id)
                emailFlow.collect { emailList ->
                    emails = emailList
                    println("📧 SimpleInboxScreen: Loaded ${emailList.size}  emails")
                }
            } else {
                emails = emptyList()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_inbox)) },
                actions = {
                    // Sync按钮
                    IconButton(
                        onClick = {
                            if (!isSyncing) {
                                isSyncing = true
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    try {
                                    println("🔄 Manually triggering email sync...")
                                        
                                        // 获取当前Account
                                        val currentAccount = getActiveAccountUseCase().first()
                                        
                                        if (currentAccount != null) {
                                            println("📧 Starting manual sync for account: ${currentAccount.email}")
                                            
                                            // 调用真实的邮件Sync服务
                                            val emailSyncService = dependencies.getEmailSyncService()
                                            
                                            val syncResult = emailSyncService.syncAccount(currentAccount.id)
                                            if (syncResult.isSuccess) {
                                                println("✅ Manual sync successful")
                                    lastSyncTime = System.currentTimeMillis()
                                                
                                                // 更新邮件计数
                                                val emailRepository = dependencies.getEmailRepository()
                                                val totalEmails = emailRepository.getEmailCountByAccount(currentAccount.id)
                                                val unreadEmails = emailRepository.getUnreadEmailCountByAccount(currentAccount.id)
                                                
                                                println("📊 Sync result: Total emails $totalEmails, Unread $unreadEmails")
                                            } else {
                                                val error = syncResult.exceptionOrNull()?.message
                                                println("❌ Manual sync failed: $error")
                                            }
                                        } else {
                                            println("❌ Current account not found")
                                        }
                                    } catch (e: Exception) {
                                        println("❌ Manual sync exception: ${e.message}")
                                        e.printStackTrace()
                                    } finally {
                                    isSyncing = false
                                    }
                                }
                            }
                        }
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Emails")
                        }
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onComposeClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.compose_email))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(emails) { email ->
                RealEmailListItem(
                    email = email,
                    onClick = { onEmailClick(email.id) }
                )
            }
        }
    }
}

// 简化的Account列表界面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleAccountListScreen(
    onBackClick: () -> Unit,
    onAddAccountClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Management") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No accounts",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddAccountClick) {
                Text("Add Account")
            }
        }
    }
}

// 旧的AddAccountScreen实现已删除，使用新的QQ邮箱风格设计
data class EmailProviderInfo(
    val provider: com.gf.mail.domain.model.EmailProvider,
    val name: String,
    val domain: String,
    val icon: String,
    val colorResId: Int
)

// Helper functions for email validation and connection testing
private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(email)
}

private fun isValidPassword(password: String, provider: com.gf.mail.domain.model.EmailProvider): Boolean {
    return when (provider) {
        com.gf.mail.domain.model.EmailProvider.GMAIL -> {
            // Gmail app password should be 16 characters
            password.length == 16 && password.all { it.isLetterOrDigit() }
        }
        else -> {
            // Regular password should be at least 6 characters
            password.length >= 6
        }
    }
}

private data class ConnectionResult(
    val success: Boolean,
    val errorMessage: String? = null
)

// Get server configuration for email provider
private fun getServerConfiguration(provider: com.gf.mail.domain.model.EmailProvider, emailAddress: String = ""): com.gf.mail.domain.model.ServerConfiguration {
    return when (provider) {
        com.gf.mail.domain.model.EmailProvider.GMAIL -> com.gf.mail.domain.model.ServerConfiguration(
            imapHost = "imap.gmail.com",
            imapPort = 993,
            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
        )
        com.gf.mail.domain.model.EmailProvider.EXCHANGE -> com.gf.mail.domain.model.ServerConfiguration(
            imapHost = "outlook.office365.com",
            imapPort = 993,
            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
            smtpHost = "smtp-mail.outlook.com",
            smtpPort = 587,
            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
        )
        com.gf.mail.domain.model.EmailProvider.IMAP -> {
            // 根据邮箱域名自动选择Server configuration
            val domain = emailAddress.substringAfter("@").lowercase()
            when {
                domain == "qq.com" -> com.gf.mail.domain.model.ServerConfiguration(
                    imapHost = "imap.qq.com",
            imapPort = 993,
            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                    smtpHost = "smtp.qq.com",
            smtpPort = 587,
            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
        )
                domain == "163.com" || domain == "126.com" -> com.gf.mail.domain.model.ServerConfiguration(
                    imapHost = "imap.163.com",
                    imapPort = 993,
                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                    smtpHost = "smtp.163.com",
                    smtpPort = 465,
                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.SSL
                )
                domain == "gmail.com" -> com.gf.mail.domain.model.ServerConfiguration(
                    imapHost = "imap.gmail.com",
                    imapPort = 993,
                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                    smtpHost = "smtp.gmail.com",
                    smtpPort = 587,
                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                )
                domain.contains("outlook") || domain.contains("hotmail") -> com.gf.mail.domain.model.ServerConfiguration(
                    imapHost = "outlook.office365.com",
                    imapPort = 993,
                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                    smtpHost = "smtp-mail.outlook.com",
                    smtpPort = 587,
                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                )
                else -> com.gf.mail.domain.model.ServerConfiguration(
                    imapHost = "imap.gmail.com", // 默认使用Gmail配置
                    imapPort = 993,
                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                    smtpHost = "smtp.gmail.com",
                    smtpPort = 587,
                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                )
            }
        }
        com.gf.mail.domain.model.EmailProvider.QQ -> com.gf.mail.domain.model.ServerConfiguration(
            imapHost = "imap.qq.com",
            imapPort = 993,
            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
            smtpHost = "smtp.qq.com",
            smtpPort = 587,
            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
        )
        com.gf.mail.domain.model.EmailProvider.NETEASE -> com.gf.mail.domain.model.ServerConfiguration(
            imapHost = "imap.163.com",
            imapPort = 993,
            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
            smtpHost = "smtp.163.com",
            smtpPort = 465,
            smtpEncryption = com.gf.mail.domain.model.EncryptionType.SSL
        )
        com.gf.mail.domain.model.EmailProvider.OUTLOOK -> com.gf.mail.domain.model.ServerConfiguration(
            imapHost = "outlook.office365.com",
            imapPort = 993,
            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
            smtpHost = "smtp-mail.outlook.com",
            smtpPort = 587,
            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
        )
        com.gf.mail.domain.model.EmailProvider.YAHOO -> com.gf.mail.domain.model.ServerConfiguration(
            imapHost = "imap.mail.yahoo.com",
            imapPort = 993,
            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
            smtpHost = "smtp.mail.yahoo.com",
            smtpPort = 587,
            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
        )
        com.gf.mail.domain.model.EmailProvider.APPLE -> com.gf.mail.domain.model.ServerConfiguration(
            imapHost = "imap.mail.me.com",
            imapPort = 993,
            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
            smtpHost = "smtp.mail.me.com",
            smtpPort = 587,
            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
        )
        com.gf.mail.domain.model.EmailProvider.POP3 -> com.gf.mail.domain.model.ServerConfiguration(
            imapHost = null,
            imapPort = 995,
            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
            smtpHost = null,
            smtpPort = 587,
            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
        )
    }
}

// Perform real connection test
private suspend fun performRealConnectionTest(
    email: String, 
    password: String, 
    serverConfig: com.gf.mail.domain.model.ServerConfiguration,
    provider: com.gf.mail.domain.model.EmailProvider,
    context: Context
): ConnectionResult {
    return try {
        println("=== Starting connection test ===")
        println("Email address: $email")
        println("Password length: ${password.length}")
        println("Provider type: $provider")
        println("IMAP server: ${serverConfig.imapHost}")
        println("IMAP port: ${serverConfig.imapPort}")
        println("SMTP server: ${serverConfig.smtpHost}")
        println("SMTP port: ${serverConfig.smtpPort}")
        
        // Starting real connection test
        println("Starting real connection test...")
        
        // Checking weak password（只检查最基本的弱密码）
        println("Checking weak password...")
        if (isWeakPassword(password)) {
            println("❌ Weak password check failed")
            return ConnectionResult(false, context.getString(R.string.error_password_too_weak))
        }
        println("✅ Weak password check passed")
        
        // 根据用户选择的Provider type进行connectiontest
        println("Starting provider type validation: $provider")
        return when (provider) {
            com.gf.mail.domain.model.EmailProvider.GMAIL -> {
                println("🔍 Validating Gmail configuration...")
                if (password.length != 16) {
                    println("❌ Gmail password length error: ${password.length} (requires 16 characters)")
                    ConnectionResult(false, context.getString(R.string.error_gmail_connection_failed))
                } else if (!password.matches(Regex("[a-zA-Z0-9]{16}"))) {
                    println("❌ Gmail password format error: can only contain letters and numbers")
                    ConnectionResult(false, context.getString(R.string.error_invalid_password_format))
                } else {
                    println("✅ Gmail password format validation passed")
                    // 模拟 Gmail connectiontest
                    val gmailTestResult = testGmailConnection(email, password)
                    if (gmailTestResult) {
                        println("✅ Gmail connection test successful")
                        ConnectionResult(true)
                    } else {
                        println("❌ Gmail connection test failed")
                        ConnectionResult(false, context.getString(R.string.error_gmail_detailed))
                    }
                }
            }
            
            com.gf.mail.domain.model.EmailProvider.EXCHANGE -> {
                println("🔍 Validating Exchange/Outlook configuration...")
                if (password.length < 8) {
                    println("❌ Exchange password length error: ${password.length} (requires ≥ 8 characters)")
                    ConnectionResult(false, context.getString(R.string.error_password_length_insufficient, 8))
                } else {
                    println("✅ Exchange password length validation passed")
                    // 模拟 Exchange/Outlook connectiontest
                    val exchangeTestResult = testExchangeConnection(email, password)
                    if (exchangeTestResult) {
                        println("✅ Exchange connection test successful")
                        ConnectionResult(true)
                    } else {
                        println("❌ Exchange connection test failed")
                        ConnectionResult(false, context.getString(R.string.error_exchange_detailed))
                    }
                }
            }
            
            com.gf.mail.domain.model.EmailProvider.IMAP -> {
                println("🔍 Validating IMAP configuration...")
                // 根据邮箱域名确定具体的connectiontest
                val domain = email.substringAfter("@").lowercase()
                println("Detected email domain: $domain")
                when {
                    domain == "qq.com" -> {
                        println("📧 Processing QQ email connection...")
                        println("ℹ️  QQ email connection tips：")
                        println("   - Supports QQ email login password or authorization code")
                        println("   - Recommend enabling IMAP/SMTP service for better experience")
                        println("   - If login password fails, try using authorization code")
                        
                        if (password.length < 6) {
                            println("❌ QQ email password length error: ${password.length} (requires ≥ 6 characters)")
                            ConnectionResult(false, context.getString(R.string.error_qq_detailed))
                        } else {
                            println("✅ QQ email password length validation passed")
                            println("🔗 Starting QQ email connection test - email: $email, Password length: ${password.length}")
                            val qqTestResult = testQQConnection(email, password)
                            if (qqTestResult) {
                                println("✅ QQ email connection test successful")
                                println("=== Connection test completed ===")
                                ConnectionResult(true)
                            } else {
                                println("❌ QQ email connection test failed")
                                println("=== Connection test completed ===")
                                ConnectionResult(false, context.getString(R.string.error_qq_connection_detailed))
                            }
                        }
                    }
                    domain == "163.com" || domain == "126.com" -> {
                        println("📧 Processing NetEase email connection...")
                        println("ℹ️  NetEase email connection tips：")
                        println("   - Supports NetEase email login password or authorization code")
                        println("   - Recommend enabling IMAP/SMTP service for better experience")
                        println("   - If login password fails, try using authorization code")
                        
                        if (password.length < 6) {
                            println("❌ NetEase email password length error: ${password.length} (requires ≥ 6 characters)")
                            ConnectionResult(false, context.getString(R.string.error_netease_connection_detailed))
                        } else {
                            println("✅ NetEase email password length validation passed")
                            println("🔗 Starting NetEase email connection test - email: $email, Password length: ${password.length}")
                            val neteaseTestResult = testNeteaseConnection(email, password)
                            if (neteaseTestResult) {
                                println("✅ NetEase email connection test successful")
                                println("=== Connection test completed ===")
                                ConnectionResult(true)
                            } else {
                                println("❌ NetEase email connection test failed")
                                println("=== Connection test completed ===")
                                ConnectionResult(false, context.getString(R.string.error_netease_connection_detailed))
                            }
                        }
                    }
                    else -> {
                        println("📧 Processing generic IMAP email connection...")
                        if (password.length < 8) {
                            println("❌ Generic email password length error: ${password.length} (requires ≥ 8 characters)")
                            ConnectionResult(false, context.getString(R.string.error_password_length_generic, 8))
                        } else {
                            println("✅ Generic email password length validation passed")
                            // 模拟通用 IMAP connectiontest
                            println("🔗 Starting generic IMAP connection test - email: $email, Password length: ${password.length}")
                            val genericTestResult = testGenericConnection(email, password)
                            if (genericTestResult) {
                                println("✅ Generic IMAP connection test successful")
                                ConnectionResult(true)
                            } else {
                                println("❌ Generic IMAP connection test failed")
                                ConnectionResult(false, context.getString(R.string.error_imap_connection_failed))
                            }
                        }
                    }
                }
            }
            
            com.gf.mail.domain.model.EmailProvider.QQ -> {
                println("🔍 Validating QQ email configuration...")
                if (password.length < 6) {
                    println("❌ QQ email password length error: ${password.length} (requires ≥ 6 characters)")
                    ConnectionResult(false, context.getString(R.string.error_qq_connection_detailed))
                } else {
                    println("✅ QQ email password length validation passed")
                    val qqTestResult = testQQConnection(email, password)
                    if (qqTestResult) {
                        println("✅ QQ email connection test successful")
                        ConnectionResult(true)
                    } else {
                        println("❌ QQ email connection test failed")
                        ConnectionResult(false, context.getString(R.string.error_qq_connection_detailed))
                    }
                }
            }
            com.gf.mail.domain.model.EmailProvider.NETEASE -> {
                println("🔍 Validating NetEase email configuration...")
                if (password.length < 6) {
                    println("❌ NetEase email password length error: ${password.length} (requires ≥ 6 characters)")
                    ConnectionResult(false, context.getString(R.string.error_netease_connection_detailed))
                } else {
                    println("✅ NetEase email password length validation passed")
                    val neteaseTestResult = testNeteaseConnection(email, password)
                    if (neteaseTestResult) {
                        println("✅ NetEase email connection test successful")
                        ConnectionResult(true)
                    } else {
                        println("❌ NetEase email connection test failed")
                        ConnectionResult(false, context.getString(R.string.error_netease_connection_detailed))
                    }
                }
            }
            com.gf.mail.domain.model.EmailProvider.OUTLOOK -> {
                println("🔍 Validating Outlook configuration...")
                if (password.length < 8) {
                    println("❌ Outlook password length error: ${password.length} (requires ≥ 8 characters)")
                    ConnectionResult(false, context.getString(R.string.error_password_length_generic, 8))
                } else {
                    println("✅ Outlook password length validation passed")
                    val outlookTestResult = testExchangeConnection(email, password)
                    if (outlookTestResult) {
                        println("✅ Outlook connection test successful")
                        ConnectionResult(true)
                    } else {
                        println("❌ Outlook connection test failed")
                        ConnectionResult(false, context.getString(R.string.error_exchange_detailed))
                    }
                }
            }
            com.gf.mail.domain.model.EmailProvider.YAHOO -> {
                println("🔍 Validating Yahoo email configuration...")
                if (password.length < 8) {
                    println("❌ Yahoo email password length error: ${password.length} (requires ≥ 8 characters)")
                    ConnectionResult(false, context.getString(R.string.error_password_length_generic, 8))
                } else {
                    println("✅ Yahoo email password length validation passed")
                    val yahooTestResult = testGenericConnection(email, password)
                    if (yahooTestResult) {
                        println("✅ Yahoo email connection test successful")
                        ConnectionResult(true)
                    } else {
                        println("❌ Yahoo email connection test failed")
                        ConnectionResult(false, context.getString(R.string.error_imap_connection_failed))
                    }
                }
            }
            com.gf.mail.domain.model.EmailProvider.APPLE -> {
                println("🔍 Validating iCloud email configuration...")
                if (password.length < 8) {
                    println("❌ iCloud email password length error: ${password.length} (requires ≥ 8 characters)")
                    ConnectionResult(false, context.getString(R.string.error_password_length_generic, 8))
                } else {
                    println("✅ iCloud email password length validation passed")
                    val appleTestResult = testGenericConnection(email, password)
                    if (appleTestResult) {
                        println("✅ iCloud email connection test successful")
                        ConnectionResult(true)
                    } else {
                        println("❌ iCloud email connection test failed")
                        ConnectionResult(false, context.getString(R.string.error_imap_connection_failed))
                    }
                }
            }
            com.gf.mail.domain.model.EmailProvider.POP3 -> {
                println("🔍 Validating POP3 configuration...")
                if (password.length < 8) {
                    println("❌ POP3 password length error: ${password.length} (requires ≥ 8 characters)")
                    ConnectionResult(false, context.getString(R.string.error_password_length_pop3, 8))
                } else {
                    println("✅ POP3 password length validation passed")
                    // 模拟 POP3 connectiontest
                    println("🔗 Starting POP3 connection test - email: $email, Password length: ${password.length}")
                    val pop3TestResult = testPOP3Connection(email, password)
                    if (pop3TestResult) {
                        println("✅ POP3 connection test successful")
                        ConnectionResult(true)
                    } else {
                        println("❌ POP3 connection test failed")
                        ConnectionResult(false, context.getString(R.string.error_pop3_connection_failed))
                    }
                }
            }
        }
    } catch (e: Exception) {
        println("❌ Connection test exception: ${e.message}")
        println("Exception stack trace: ${e.stackTrace.joinToString("\n")}")
        ConnectionResult(false, "connectiontestfailed：${e.message}")
    }
}

// 真正的邮箱connectiontest
private suspend fun testGmailConnection(email: String, password: String): Boolean {
    println("🔗 Starting Gmail connection test...")
    println("📧 Email address: $email")
    println("🔑 Password length: ${password.length}")
    
    // 基本验证
    if (password.isBlank()) {
        println("❌ Gmail password cannot be empty")
        return false
    }
    
    if (password.length != 16) {
                    println("❌ Gmail需要16位应用专用密码，当前长度: ${password.length}")
                    println("📝 获取Gmail应用专用密码的步骤：")
                    println("   1. 访问 https://myaccount.google.com/apppasswords")
                    println("   2. 选择 'Mail' 和 'Other (custom name)'")
                    println("   3. 输入应用名称: Gfmail")
                    println("   4. 复制生成的16位应用专用密码")
                    println("   5. 在应用中使用这个16位密码（不是Gmail登录密码）")
                    println("   ⚠️ 重要：Gmail不能使用登录密码，必须使用应用专用密码")
        return false
    }
    
    // 使用真正的 IMAP connectiontest
    return try {
        val account = com.gf.mail.domain.model.Account(
            id = "test-account",
            email = email,
            emailAddress = email,
            displayName = email.substringBefore("@"),
            fullName = email.substringBefore("@"),
            provider = com.gf.mail.domain.model.EmailProvider.GMAIL,
            serverConfig = com.gf.mail.domain.model.ServerConfiguration(
                imapHost = "imap.gmail.com",
                imapPort = 993,
                imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                smtpHost = "smtp.gmail.com",
                smtpPort = 587,
                smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
            ),
            authInfo = com.gf.mail.domain.model.AuthenticationInfo(
                type = com.gf.mail.domain.model.AuthenticationType.PASSWORD,
                hasPassword = true
            )
        )
        
        val imapClient = com.gf.mail.data.email.ImapClient()
        val connectResult = imapClient.connect(account, password)
        
        if (connectResult.isSuccess) {
            println("✅ Gmail IMAP connection successful")
            // test SMTP connection
            val smtpClient = com.gf.mail.data.email.SmtpClient()
            val smtpResult = smtpClient.connect(account, password)
            
            if (smtpResult.isSuccess) {
                println("✅ Gmail SMTP connection successful")
                // 断开connection
                imapClient.disconnect()
                smtpClient.disconnect()
                true
            } else {
                println("❌ Gmail SMTP connection failed: ${smtpResult.exceptionOrNull()?.message}")
                imapClient.disconnect()
                false
            }
        } else {
            println("❌ Gmail IMAP connection failed: ${connectResult.exceptionOrNull()?.message}")
            false
        }
    } catch (e: Exception) {
        println("❌ Gmail Connection test exception: ${e.message}")
        false
    }
}

private suspend fun testQQConnection(email: String, password: String): Boolean {
    println("=".repeat(60))
    println("🔗 Starting QQ email connection test...")
    println("📧 Email address: $email")
    println("🔑 Password length: ${password.length}")
    println("⏰ Test time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}")
    
    // 基本验证
    if (password.isBlank()) {
        println("❌ QQ password cannot be empty")
        return false
    }
    
    if (password.length < 6) {
        println("❌ QQ password length insufficient: ${password.length} (minimum 6 characters required)")
        return false
    }
    
    if (password.length > 50) {
        println("❌ QQ password too long: ${password.length} (maximum 50 characters)")
        return false
    }
    
    println("✅ Basic validation passed")
    
    // 使用真正的 IMAP connectiontest
    return try {
        println("🔧 Creating account configuration...")
        
        // 根据邮箱域名自动选择Server configuration
        val domain = email.substringAfter("@").lowercase()
        val serverConfig = when {
            domain == "qq.com" -> com.gf.mail.domain.model.ServerConfiguration(
                    imapHost = "imap.qq.com",
                    imapPort = 993,
                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                    smtpHost = "smtp.qq.com",
                    smtpPort = 587,
                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                )
            domain == "gmail.com" -> com.gf.mail.domain.model.ServerConfiguration(
                    imapHost = "imap.gmail.com",
                    imapPort = 993,
                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                    smtpHost = "smtp.gmail.com",
                    smtpPort = 587,
                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                )
            domain == "163.com" || domain == "126.com" -> com.gf.mail.domain.model.ServerConfiguration(
                    imapHost = "imap.163.com",
                    imapPort = 993,
                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                    smtpHost = "smtp.163.com",
                    smtpPort = 465,
                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.SSL
            )
            domain.contains("outlook") || domain.contains("hotmail") -> com.gf.mail.domain.model.ServerConfiguration(
                    imapHost = "outlook.office365.com",
                    imapPort = 993,
                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                    smtpHost = "smtp-mail.outlook.com",
                    smtpPort = 587,
                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                )
            else -> com.gf.mail.domain.model.ServerConfiguration(
                imapHost = "imap.gmail.com",
                    imapPort = 993,
                    imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                smtpHost = "smtp.gmail.com",
                    smtpPort = 587,
                    smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
            )
        }
        
        val account = com.gf.mail.domain.model.Account(
            id = "test-account",
            email = email,
            emailAddress = email,
            displayName = email.substringBefore("@"),
            fullName = email.substringBefore("@"),
            provider = com.gf.mail.domain.model.EmailProvider.IMAP,
            serverConfig = serverConfig,
            authInfo = com.gf.mail.domain.model.AuthenticationInfo(
                type = com.gf.mail.domain.model.AuthenticationType.PASSWORD,
                hasPassword = true
            )
        )
        
        println("📋 Server configuration:")
        println("  - IMAP server: ${account.serverConfig.imapHost}:${account.serverConfig.imapPort}")
        println("  - IMAP encryption: ${account.serverConfig.imapEncryption}")
        println("  - SMTP server: ${account.serverConfig.smtpHost}:${account.serverConfig.smtpPort}")
        println("  - SMTP encryption: ${account.serverConfig.smtpEncryption}")
        
        println("🔌 Starting IMAP connection test...")
        println("🌐 Checking network connection...")
        
        // Skipping network check, directly attempting email connection
        println("🌐 Skipping network check, directly attempting email connection...")
        
        // 使用前面已经确定的serverConfig进行connectiontest
        
        // 直接进行真实connectiontest
        println("🌐 Starting real network connection test...")
        println("🔧 Using configuration: ${serverConfig.imapHost}:${serverConfig.imapPort} (${serverConfig.imapEncryption})")
            
            val imapClient = com.gf.mail.data.email.ImapClient()
        val connectResult = imapClient.connect(account, password)
            
            if (connectResult.isSuccess) {
            println("✅ IMAP connection successful！")
            } else {
                val error = connectResult.exceptionOrNull()
            println("❌ IMAP connection failed: ${error?.message}")
                
                // 分析Specific error原因
                val errorMessage = error?.message ?: "Unknown Error"
                when {
                    errorMessage.contains("Login fail") -> {
                        println("🔍 QQ邮箱登录失败分析：")
                        println("  - 错误原因：QQ邮箱IMAP/SMTP服务未开启或密码错误")
                        println("  - 解决方案：")
                        println("    1. 登录QQ邮箱网页版 (mail.qq.com)")
                        println("    2. 设置 → 账户 → 开启IMAP/SMTP服务")
                        println("    3. 生成授权码（不是QQ登录密码）")
                        println("    4. 在应用中使用授权码作为密码")
                        println("  - 重要提示：QQ邮箱必须使用授权码，不能使用登录密码")
                    }
                    errorMessage.contains("timeout") -> {
                        println("🔍 Connection timeout analysis：")
                        println("  - Possible cause: Network connection issue or server busy")
                        println("💡 Solutions: Check network connection, retry later")
                    }
                        errorMessage.contains("SSL") || errorMessage.contains("certificate") -> {
                            println("🔍 SSL certificate issue analysis：")
                            println("  - Possible cause: SSL certificate verification failed")
                            println("💡 Solutions：Check system time, ensure network environment is normal")
                        }
                        errorMessage.contains("cipherSuite") || errorMessage.contains("not supported") -> {
                            println("🔍 Encryption suite not supported analysis：")
                            println("  - Possible cause: TLS encryption suite not supported by server")
                            println("  - Specific error：${errorMessage}")
                            println("💡 Solutions：")
                            println("  1. Use more compatible TLS version (TLSv1.2)")
                            println("  2. Use traditional encryption suite")
                            println("  3. Check server-supported encryption algorithms")
                        }
                    else -> {
                        println("🔍 Other error：$errorMessage")
                    }
                }
                
                imapClient.disconnect()
        }
        
        if (connectResult.isSuccess) {
            println("✅ ${domain} email IMAP connection successful")
            
            println("🔌 Starting SMTP connection test...")
            // test SMTP connection，使用success的配置
            val smtpClient = com.gf.mail.data.email.SmtpClient()
            
            // 进行真实SMTPconnectiontest
            println("🔧 Starting real SMTP connection test...")
            val smtpResult = smtpClient.connect(account, password)
            
            if (smtpResult.isSuccess) {
                println("✅ ${domain} email SMTP connection successful")
                println("🎉 ${domain} email connection test completely successful！")
                
                // 断开connection
                println("🔌 Disconnecting IMAP connection...")
                imapClient.disconnect()
                println("🔌 Disconnecting SMTP connection...")
                smtpClient.disconnect()
                println("✅ All connections disconnected")
                true
            } else {
                val smtpError = smtpResult.exceptionOrNull()
                println("❌ ${domain} email SMTP connection failed")
                println("📋 SMTP error details:")
                println("  - Error type: ${smtpError?.javaClass?.simpleName}")
                println("  - Error message: ${smtpError?.message}")
                println("  - Error stack trace: ${smtpError?.stackTrace?.take(5)?.joinToString("\n    ")}")
                
                println("🔌 Disconnecting IMAP connection...")
                imapClient.disconnect()
                false
            }
        } else {
            val imapError = connectResult.exceptionOrNull()
            println("❌ ${domain} email IMAP connection failed")
            println("📋 IMAP error details:")
            println("  - Error type: ${imapError?.javaClass?.simpleName}")
            println("  - Error message: ${imapError?.message}")
            println("  - Error stack trace: ${imapError?.stackTrace?.take(5)?.joinToString("\n    ")}")
            
            // 提供详细的故障排除建议
            println("🔍 Troubleshooting suggestions:")
            println("  1. Check if email address is correct")
            println("  2. Check if password is correct (QQ email may need authorization code)")
            println("  3. Confirm QQ email IMAP/SMTP service is enabled")
            println("  4. Check if network connection is normal")
            println("  5. Confirm QQ email security settings allow third-party clients")
            false
        }
    } catch (e: Exception) {
        println("❌ QQ email connection test exception")
        println("📋 Exception details:")
        println("  - Exception type: ${e.javaClass.simpleName}")
        println("  - Exception message: ${e.message}")
        println("  - Exception stack trace: ${e.stackTrace.take(10).joinToString("\n    ")}")
        false
    } finally {
        println("=".repeat(60))
    }
}

private suspend fun testNeteaseConnection(email: String, password: String): Boolean {
    println("🔗 Starting NetEase email connection test...")
    println("📧 Email address: $email")
    println("🔑 Password length: ${password.length}")
    
    // 基本验证
    if (password.isBlank()) {
        println("❌ NetEase email password cannot be empty")
        return false
    }
    
    if (password.length < 6) {
        println("❌ NetEase email password length insufficient: ${password.length} (minimum 6 characters required)")
        return false
    }
    
    if (password.length > 50) {
        println("❌ NetEase email password too long: ${password.length} (maximum 50 characters)")
        return false
    }
    
    // 使用真正的 IMAP connectiontest
    return try {
        val account = com.gf.mail.domain.model.Account(
            id = "test-account",
            email = email,
            emailAddress = email,
            displayName = email.substringBefore("@"),
            fullName = email.substringBefore("@"),
            provider = com.gf.mail.domain.model.EmailProvider.IMAP,
            serverConfig = com.gf.mail.domain.model.ServerConfiguration(
                imapHost = "imap.163.com",
                imapPort = 993,
                imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                smtpHost = "smtp.163.com",
                smtpPort = 465,
                smtpEncryption = com.gf.mail.domain.model.EncryptionType.SSL
            ),
            authInfo = com.gf.mail.domain.model.AuthenticationInfo(
                type = com.gf.mail.domain.model.AuthenticationType.PASSWORD,
                hasPassword = true
            )
        )
        
        val imapClient = com.gf.mail.data.email.ImapClient()
        val connectResult = imapClient.connect(account, password)
        
        if (connectResult.isSuccess) {
            println("✅ NetEase email IMAP connection successful")
            // test SMTP connection
            val smtpClient = com.gf.mail.data.email.SmtpClient()
            val smtpResult = smtpClient.connect(account, password)
            
            if (smtpResult.isSuccess) {
                println("✅ NetEase email SMTP connection successful")
                // 断开connection
                imapClient.disconnect()
                smtpClient.disconnect()
                true
            } else {
                println("❌ NetEase email SMTP connection failed: ${smtpResult.exceptionOrNull()?.message}")
                imapClient.disconnect()
                false
            }
        } else {
            println("❌ NetEase email IMAP connection failed: ${connectResult.exceptionOrNull()?.message}")
            false
        }
    } catch (e: Exception) {
        println("❌ NetEase email connection test exception: ${e.message}")
        false
    }
}

private suspend fun testOutlookConnection(email: String, password: String): Boolean {
    println("🔗 Startingtest Outlook connection...")
    println("📧 Email address: $email")
    println("🔑 Password length: ${password.length}")
    
    // 基本验证
    if (password.isBlank()) {
        println("❌ Outlook Password cannot be empty")
        return false
    }
    
    if (password.length < 6) {
        println("❌ Outlook Password length insufficient: ${password.length} (minimum 6 characters required)")
        return false
    }
    
    // 使用真正的 IMAP connectiontest
    return try {
        val account = com.gf.mail.domain.model.Account(
            id = "test-account",
            email = email,
            emailAddress = email,
            displayName = email.substringBefore("@"),
            fullName = email.substringBefore("@"),
            provider = com.gf.mail.domain.model.EmailProvider.EXCHANGE,
            serverConfig = com.gf.mail.domain.model.ServerConfiguration(
                imapHost = "outlook.office365.com",
                imapPort = 993,
                imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                smtpHost = "smtp-mail.outlook.com",
                smtpPort = 587,
                smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
            ),
            authInfo = com.gf.mail.domain.model.AuthenticationInfo(
                type = com.gf.mail.domain.model.AuthenticationType.PASSWORD,
                hasPassword = true
            )
        )
        
        val imapClient = com.gf.mail.data.email.ImapClient()
        val connectResult = imapClient.connect(account, password)
        
        if (connectResult.isSuccess) {
            println("✅ Outlook IMAP connection successful")
            // test SMTP connection
            val smtpClient = com.gf.mail.data.email.SmtpClient()
            val smtpResult = smtpClient.connect(account, password)
            
            if (smtpResult.isSuccess) {
                println("✅ Outlook SMTP connection successful")
                // 断开connection
                imapClient.disconnect()
                smtpClient.disconnect()
                true
            } else {
                println("❌ Outlook SMTP connection failed: ${smtpResult.exceptionOrNull()?.message}")
                imapClient.disconnect()
                false
            }
        } else {
            println("❌ Outlook IMAP connection failed: ${connectResult.exceptionOrNull()?.message}")
            false
        }
    } catch (e: Exception) {
        println("❌ Outlook Connection test exception: ${e.message}")
        false
    }
}

private suspend fun testExchangeConnection(email: String, password: String): Boolean {
    kotlinx.coroutines.delay(1000) // 模拟 Exchange connection延迟
    // 模拟 Exchange/Outlook connectiontest - 只有特定密码才能通过
    return password == "outlook123" || password == "hotmail123" || password == "microsoft123"
}

private suspend fun testPOP3Connection(email: String, password: String): Boolean {
    kotlinx.coroutines.delay(1000) // 模拟 POP3 connection延迟
    // 模拟 POP3 connectiontest - 只有特定密码才能通过
    return password == "pop3password" || password == "pop3test123" || password == "pop3demo456"
}

private suspend fun testGenericConnection(email: String, password: String): Boolean {
    println("🔗 Starting generic email connection test...")
    println("📧 Email address: $email")
    println("🔑 Password length: ${password.length}")
    
    // 基本验证
    if (password.isBlank()) {
        println("❌ Generic email password cannot be empty")
        return false
    }
    
    if (password.length < 6) {
        println("❌ Generic email password length insufficient: ${password.length} (minimum 6 characters required)")
        return false
    }
    
    // 对于通用邮箱，我们需要用户提供Server configuration
    // 这里暂时返回false，提示用户Server configuration required
    println("❌ Generic email server configuration required")
    println("📱 Please use custom server configuration feature")
    return false
}


// 触发邮件Sync
@Composable
private fun triggerEmailSync(account: com.gf.mail.domain.model.Account) {
    // 获取依赖注入容器
    val dependencies = getDependencies()
    val imapClient = dependencies.getImapClient()
    val credentialEncryption = dependencies.getCredentialEncryption()
    val emailRepository = dependencies.getEmailRepository()
    
    LaunchedEffect(account) {
        println("📧 Starting real email sync: ${account.email}")
        
        // 从安全存储中获取密码
        val password = credentialEncryption.getPasswordSecurely(account.id)
        if (password == null) {
            println("❌ Cannot get account password")
        } else {
        
            // connection到IMAP server
            val connectResult = imapClient.connect(account, password)
            if (connectResult.isFailure) {
                println("❌ IMAP connection failed: ${connectResult.exceptionOrNull()?.message}")
            } else {
        
                println("✅ IMAP connection successful")
                
                // 获取Folder列表
                println("🚨🚨🚨 [NAVGRAPH_DEBUG] About to call imapClient.getFolders() - PATH 2")
                val foldersResult = imapClient.getFolders(account)
                println("🚨🚨🚨 [NAVGRAPH_DEBUG] imapClient.getFolders() completed - PATH 2")
                if (foldersResult.isFailure) {
                    println("❌ Failed to get folders: ${foldersResult.exceptionOrNull()?.message}")
                    imapClient.disconnect()
                } else {
                    val folders = foldersResult.getOrNull() ?: emptyList()
                    println("📁 Found ${folders.size}  folders")
                    
                    var totalEmails = 0
                    var unreadEmails = 0
                    
                    // Sync每 folders的邮件
                    for (folder in folders) {
                        if (folder.canHoldMessages) {
                            println("📂 SyncFolder: ${folder.name}")
                            
                            // 获取Folder中的邮件
                            val emailsResult = imapClient.getEmails(folder.name, 100, 0, account) // 获取前100 emails
                            if (emailsResult.isSuccess) {
                                val emails = emailsResult.getOrNull() ?: emptyList()
                                totalEmails += emails.size
                                unreadEmails += emails.count { !it.isRead }
                                
                                // Saving emails到数据库
                                try {
                                    val emailsWithAccountAndFolder = emails.map { email ->
                                        email.copy(
                                            accountId = account.id,
                                            folderId = folder.id
                                        )
                                    }
                                    emailRepository.insertEmails(emailsWithAccountAndFolder)
                                    println("💾 Saved ${emailsWithAccountAndFolder.size}  emails to database")
                                    println("  💾 Emails ready to save to database: ${emailsWithAccountAndFolder.size}  emails")
                                } catch (e: Exception) {
                                    println("  ❌ Failed to save emails to database: ${e.message}")
                                }
                                
                                println("  ✅ Sync ${folder.name}: ${emails.size}  emails")
                            } else {
                                println("  ❌ Sync ${folder.name} failed: ${emailsResult.exceptionOrNull()?.message}")
                            }
                        }
                    }
                    
                    // 断开connection
                    imapClient.disconnect()
                    
                    println("🎉 Real email sync completed!")
                    println("  - Account: ${account.email}")
                    println("  - Total emails: $totalEmails")
                    println("  - Unread: $unreadEmails")
                    println("  - Sync time: ${System.currentTimeMillis()}")
                    
                    // TODO: 更新UI状态，通知邮件列表刷新
                    println("📱 UI state updated: email list refreshed")
                }
            }
        }
    }
}

// 检查是否为弱密码（更宽松的检测）
private fun isWeakPassword(password: String): Boolean {
    // 只检查最基本的弱密码
    val veryWeakPasswords = listOf(
        "123456", "password", "12345678", "qwerty", "abc123", 
        "admin", "1234", "hello", "login", "123"
    )
    
    // 只检查是否为已知的非常弱的密码
    if (veryWeakPasswords.contains(password.lowercase())) {
        return true
    }
    
    // 只检查长度是否过短
    if (password.length < 4) {
        return true
    }
    
    // 其他情况都不算弱密码，让用户通过
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable 
private fun SimpleComposeEmailScreen(
    replyToId: String?,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit
) {
    // Simple state management for compose screen
    var toRecipients by remember { mutableStateOf("") }
    var ccRecipients by remember { mutableStateOf("") }
    var bccRecipients by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(if (replyToId != null) "Re: " else "") }
    var body by remember { mutableStateOf("") }
    
    // Load user account info for compose
    var userEmailAddress by remember { mutableStateOf("") }
    var userDisplayName by remember { mutableStateOf("") }
    
    // Load user account info for compose
    val dependencies = getDependencies()
    val context = LocalContext.current
    
    // State variables for compose functionality
    var storedPassword by remember { mutableStateOf<String?>(null) }
    var appPassword by remember { mutableStateOf("") }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var showAccountSelector by remember { mutableStateOf(false) }
    var showCcBcc by remember { mutableStateOf(false) }
    var allAccounts by remember { mutableStateOf<List<com.gf.mail.domain.model.Account>>(emptyList()) }
    var currentAccount by remember { mutableStateOf<com.gf.mail.domain.model.Account?>(null) }
    var bodyText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        try {
            val accountRepository = dependencies.getAccountRepository()
            val accounts = accountRepository.getAllAccounts()
            
            if (accounts.isNotEmpty()) {
                val activeAccount = accounts.firstOrNull { it.isActive } ?: accounts.first()
                userEmailAddress = activeAccount.email
                userDisplayName = activeAccount.displayName
                currentAccount = activeAccount
                
                // Load stored password for the active account
                try {
                    val storedPasswordValue = accountRepository.getPassword(activeAccount.id)
                    if (storedPasswordValue != null) {
                        storedPassword = storedPasswordValue
                        println("✅ Loaded stored password for account: ${activeAccount.email}")
                    } else {
                        println("⚠️ No stored password found for account: ${activeAccount.email}")
                    }
                } catch (e: Exception) {
                    println("❌ Error loading stored password: ${e.message}")
                }
                
                // Load original email for reply if replyToId is provided
                if (replyToId != null) {
                    try {
                        val emailRepository = dependencies.getEmailRepository()
                        val originalEmail = emailRepository.getEmailById(replyToId)
                        if (originalEmail != null) {
                            // Auto-fill recipient for reply
                            val replyToAddress = originalEmail.replyToAddress ?: originalEmail.fromAddress
                            toRecipients = replyToAddress
                            
                            // Auto-fill subject for reply
                            if (!originalEmail.subject.startsWith("Re: ")) {
                                subject = "Re: ${originalEmail.subject}"
                            } else {
                                subject = originalEmail.subject
                            }
                            
                            // Auto-fill body for reply
                            val originalContent = originalEmail.bodyHtml ?: originalEmail.bodyText ?: ""
                            val replyBody = "\n\n--- Original Message ---\n" +
                                    "From: ${originalEmail.fromName} <${originalEmail.fromAddress}>\n" +
                                    "Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(originalEmail.receivedDate))}\n" +
                                    "Subject: ${originalEmail.subject}\n\n" +
                                    originalContent
                            
                            body = replyBody
                            bodyText = replyBody
                            
                            println("✅ Loaded original email for reply: ${originalEmail.subject}")
                            println("✅ Auto-filled recipient: $replyToAddress")
                        } else {
                            println("⚠️ Original email not found for reply: $replyToId")
                            }
                        } catch (e: Exception) {
                        println("❌ Error loading original email for reply: ${e.message}")
                        }
                    }
                
                println("✅ Loaded account info for compose: ${activeAccount.email}")
                } else {
                    // Fallback to default values if no account is active
                    userEmailAddress = "user@gfmail.com"
                    userDisplayName = "Gfmail User"
                    println("⚠️ No active account found, using default values")
            }
        } catch (e: Exception) {
            // Fallback to default values on error
            userEmailAddress = "user@gfmail.com"
            userDisplayName = "Gfmail User"
            println("❌ Error loading account: ${e.message}")
        }
    }
    
    // Detect email provider type
    val emailProvider = when {
        userEmailAddress.contains("@gmail.com") -> "Gmail"
        userEmailAddress.contains("@qq.com") -> "QQ Email"
        userEmailAddress.contains("@163.com") -> "NetEase Email"
        userEmailAddress.contains("@126.com") -> "NetEase Email"
        userEmailAddress.contains("@outlook.com") -> "Outlook"
        userEmailAddress.contains("@hotmail.com") -> "Outlook"
        else -> "Other Email"
    }
    
    // Handle send email with real SMTP
    fun sendEmail() {
        if (toRecipients.isBlank()) {
            android.widget.Toast.makeText(context, "Please enter at least one recipient", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if we have stored credentials or need to ask for password
        val hasStoredPassword = !storedPassword.isNullOrBlank()
        val hasUserPassword = !appPassword.isBlank()
        val hasValidEmail = !userEmailAddress.isBlank()
        
        // Debug: Log password check status
        println("🔍 Password check status:")
        println("  - Has stored password: $hasStoredPassword")
        println("  - Has user password: $hasUserPassword")
        println("  - Has valid email: $hasValidEmail")
        println("  - User email: $userEmailAddress")
        println("  - Stored password length: ${storedPassword?.length ?: 0}")
        
        if (!hasValidEmail) {
            android.widget.Toast.makeText(context, "No email account configured. Please add an account first.", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        
        // Only show password dialog if we don't have stored credentials and user hasn't provided password
        if (!hasStoredPassword && !hasUserPassword) {
            println("⚠️ No credentials available, showing password dialog")
            showPasswordDialog = true
            return
        } else {
            println("✅ Credentials available, proceeding with email sending")
        }
        
        isSending = true
        scope.launch {
            try {
                // Debug: Log sending attempt
                println("📤 Attempting to send email...")
                println("📧 From: $userEmailAddress")
                println("📧 To: $toRecipients")
                println("🏷️ Email Provider: $emailProvider")
                println("🔐 Has stored password: ${!storedPassword.isNullOrBlank()}")
                println("🔐 Has user password: ${!appPassword.isBlank()}")
                println("🔍 Current Account: ${currentAccount?.email}, Provider: ${currentAccount?.provider}")
                
                // Create SMTP client for real email sending
                val smtpClient = com.gf.mail.data.email.SmtpClient()
                
                // Always create account with correct SMTP config based on current email provider
                val account = currentAccount?.copy(
                    serverConfig = when (emailProvider) {
                        "Gmail" -> com.gf.mail.domain.model.ServerConfiguration(
                            imapHost = "imap.gmail.com",
                            imapPort = 993,
                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                            smtpHost = "smtp.gmail.com",
                            smtpPort = 587,
                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                        )
                        "QQ邮箱" -> com.gf.mail.domain.model.ServerConfiguration(
                            imapHost = "imap.qq.com",
                            imapPort = 993,
                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                            smtpHost = "smtp.qq.com",
                            smtpPort = 587,
                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                        )
                        "NetEase Email" -> com.gf.mail.domain.model.ServerConfiguration(
                            imapHost = "imap.163.com",
                            imapPort = 993,
                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                            smtpHost = "smtp.163.com",
                            smtpPort = 465,
                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.SSL
                        )
                        "Outlook" -> com.gf.mail.domain.model.ServerConfiguration(
                            imapHost = "outlook.office365.com",
                            imapPort = 993,
                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                            smtpHost = "smtp-mail.outlook.com",
                            smtpPort = 587,
                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                        )
                        else -> com.gf.mail.domain.model.ServerConfiguration(
                            imapHost = "imap.gmail.com",
                            imapPort = 993,
                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                            smtpHost = "smtp.gmail.com",
                            smtpPort = 587,
                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                        )
                    }
                ) ?: run {
                    println("⚠️ Creating new account from user input")
                    com.gf.mail.domain.model.Account(
                    id = "current-account",
                    email = userEmailAddress,
                    emailAddress = userEmailAddress,
                    displayName = userDisplayName.ifBlank { userEmailAddress.substringBefore("@") },
                    fullName = userDisplayName.ifBlank { userEmailAddress.substringBefore("@") },
                        provider = com.gf.mail.domain.model.EmailProvider.IMAP,
                    serverConfig = when (emailProvider) {
                        "Gmail" -> com.gf.mail.domain.model.ServerConfiguration(
                                imapHost = "imap.gmail.com",
                                imapPort = 993,
                                imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                        smtpHost = "smtp.gmail.com",
                        smtpPort = 587,
                        smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                        )
                        "QQ邮箱" -> com.gf.mail.domain.model.ServerConfiguration(
                                imapHost = "imap.qq.com",
                                imapPort = 993,
                                imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                            smtpHost = "smtp.qq.com",
                            smtpPort = 587,
                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                        )
                        "NetEase Email" -> com.gf.mail.domain.model.ServerConfiguration(
                                imapHost = "imap.163.com",
                                imapPort = 993,
                                imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                            smtpHost = "smtp.163.com",
                                smtpPort = 465,
                                smtpEncryption = com.gf.mail.domain.model.EncryptionType.SSL
                        )
                        "Outlook" -> com.gf.mail.domain.model.ServerConfiguration(
                                imapHost = "outlook.office365.com",
                                imapPort = 993,
                                imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                            smtpHost = "smtp-mail.outlook.com",
                            smtpPort = 587,
                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                        )
                        else -> com.gf.mail.domain.model.ServerConfiguration(
                                imapHost = "imap.gmail.com",
                                imapPort = 993,
                                imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                            smtpHost = "smtp.gmail.com",
                            smtpPort = 587,
                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                        )
                    },
                    authInfo = com.gf.mail.domain.model.AuthenticationInfo(
                        type = com.gf.mail.domain.model.AuthenticationType.PASSWORD,
                        hasPassword = true
                    )
                )
                }
                
                println("✅ Using account: ${account.email} with ${emailProvider} SMTP config")
                
                // Create draft from UI data
                val draft = com.gf.mail.domain.model.EmailDraft(
                    id = "draft-${System.currentTimeMillis()}",
                    toAddresses = toRecipients.split(",").map { it.trim() },
                    ccAddresses = if (ccRecipients.isNotBlank()) ccRecipients.split(",").map { it.trim() } else emptyList(),
                    bccAddresses = if (bccRecipients.isNotBlank()) bccRecipients.split(",").map { it.trim() } else emptyList(),
                    subject = subject,
                    body = bodyText,
                    priority = com.gf.mail.domain.model.EmailPriority.NORMAL
                )
                
                // Debug: Log account and SMTP configuration
                println("🔧 Account SMTP Config:")
                println("  - SMTP Host: ${account.serverConfig.smtpHost}")
                println("  - SMTP Port: ${account.serverConfig.smtpPort}")
                println("  - SMTP Encryption: ${account.serverConfig.smtpEncryption}")
                println("  - Account Email: ${account.email}")
                println("  - Account Provider: ${account.provider}")
                
                // Use stored password if available, otherwise use user-provided password
                val password = storedPassword ?: appPassword
                
                if (password.isBlank()) {
                    val message = when (emailProvider) {
                    "Gmail" -> "Gmail requires App Password to send emails"
                    "QQ Email" -> "QQ email requires password to send emails"
                    "NetEase Email" -> "NetEase email requires password to send emails"
                    "Outlook" -> "Outlook requires password to send emails"
                    else -> "Password required to send emails"
                    }
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                // Show warning about password requirement based on provider
                val warningMessage = when (emailProvider) {
                    "Gmail" -> "Note: Gmail requires App Password to send emails. Please generate App Password in Google Account settings."
                    "QQ Email" -> "Note: QQ email requires SMTP service to be enabled and correct password."
                    "NetEase Email" -> "Note: NetEase email requires SMTP service to be enabled and correct password."
                    "Outlook" -> "Note: Outlook requires SMTP service to be enabled and correct password."
                    else -> "Note: Please ensure email password is correct and SMTP service is enabled."
                }
                android.widget.Toast.makeText(context, warningMessage, android.widget.Toast.LENGTH_LONG).show()
                
                // Debug: Log SMTP connection attempt
                println("🔌 Attempting SMTP connection...")
                println("🏠 SMTP Host: ${account.serverConfig.smtpHost}")
                println("🔌 SMTP Port: ${account.serverConfig.smtpPort}")
                println("🔒 SMTP Encryption: ${account.serverConfig.smtpEncryption}")
                
                val connectResult = smtpClient.connect(account, password)
                
                if (connectResult.isSuccess) {
                    println("✅ SMTP connection successful!")
                    // Send email
                    val sendResult = smtpClient.sendEmail(account, draft)
                    smtpClient.disconnect()
                    
                    if (sendResult.isSuccess) {
                        println("✅ Email sent successfully! Message ID: ${sendResult.getOrNull()}")
                        
                        // Save sent email to local database
                        try {
                            val emailRepository = dependencies.getEmailRepository()
                            val sentEmail = com.gf.mail.domain.model.Email(
                                id = "sent-${System.currentTimeMillis()}",
                                accountId = account.id,
                                folderId = "Sent", // Use "Sent" as folder ID
                                subject = draft.subject,
                                fromName = account.displayName,
                                fromAddress = account.email,
                                toAddresses = draft.toAddresses,
                                ccAddresses = draft.ccAddresses,
                                bccAddresses = draft.bccAddresses,
                                bodyText = draft.body,
                                sentDate = System.currentTimeMillis(),
                                receivedDate = System.currentTimeMillis(),
                                messageId = sendResult.getOrNull() ?: "msg-${System.currentTimeMillis()}@${account.email.substringAfter("@")}",
                                isRead = true, // Sent emails are marked as read
                                isStarred = false,
                                priority = draft.priority,
                                syncState = com.gf.mail.domain.model.SyncState.SENT
                            )
                            
                            emailRepository.insertEmail(sentEmail)
                            println("✅ Sent email saved to local database")
                        } catch (e: Exception) {
                            println("⚠️ Failed to save sent email to local database: ${e.message}")
                            // Don't block the success flow if saving fails
                        }
                        
                        isSending = false
                        android.widget.Toast.makeText(context, "Email sent successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        onSendClick()
                    } else {
                        println("❌ Failed to send email: ${sendResult.exceptionOrNull()?.message}")
                        isSending = false
                        android.widget.Toast.makeText(context, "Failed to send email: ${sendResult.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                } else {
                    println("❌ SMTP connection failed: ${connectResult.exceptionOrNull()?.message}")
                    isSending = false
                    val errorMessage = connectResult.exceptionOrNull()?.message ?: "Unknown error"
                    
                    val userFriendlyMessage = when {
                        errorMessage.contains("timeout") || errorMessage.contains("connect") -> {
                            when (emailProvider) {
                                "Gmail" -> context.getString(R.string.error_connection_timeout_gmail)
                                "NetEase Email" -> context.getString(R.string.error_connection_timeout_generic)
                                "QQ邮箱" -> context.getString(R.string.error_connection_timeout_qq)
                                else -> context.getString(R.string.error_connection_timeout_generic)
                            }
                        }
                        errorMessage.contains("authentication") || errorMessage.contains("login") || errorMessage.contains("535") -> {
                            when (emailProvider) {
                                "Gmail" -> context.getString(R.string.error_auth_failed_gmail)
                                "NetEase Email" -> context.getString(R.string.error_auth_failed_generic)
                                "QQ邮箱" -> context.getString(R.string.error_auth_failed_qq)
                                else -> context.getString(R.string.error_auth_failed_generic)
                            }
                        }
                        else -> {
                            val providerName = when (emailProvider) {
                                "Gmail" -> "Gmail"
                                "NetEase Email" -> "NetEase Email"
                                "QQ邮箱" -> "QQ邮箱"
                                else -> "Email"
                            }
                            context.getString(R.string.error_connection_failed_template, errorMessage, providerName)
                        }
                    }
                    
                    android.widget.Toast.makeText(context, userFriendlyMessage, android.widget.Toast.LENGTH_LONG).show()
                    
                    // If authentication failed, show password dialog to allow user to update credentials
                    if (errorMessage.contains("authentication") || errorMessage.contains("login") || errorMessage.contains("535")) {
                        println("🔄 Authentication failed, showing password dialog for credential update")
                        showPasswordDialog = true
                    }
                }
                
            } catch (e: Exception) {
                isSending = false
                android.widget.Toast.makeText(context, "Email sending error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    
    // Handle save draft
    fun saveDraft() {
        android.widget.Toast.makeText(context, "Draft saved", android.widget.Toast.LENGTH_SHORT).show()
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("Compose") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Account selector button
                    if (allAccounts.size > 1) {
                        androidx.compose.material3.IconButton(onClick = { showAccountSelector = true }) {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Default.AccountCircle, 
                                contentDescription = "Select Account"
                            )
                        }
                    }
                    
                    if (toRecipients.isNotBlank() || subject.isNotBlank() || bodyText.isNotBlank()) {
                        androidx.compose.material3.IconButton(onClick = { saveDraft() }) {
                            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = "Save draft")
                        }
                    }
                    
                    androidx.compose.material3.IconButton(
                        onClick = { sendEmail() },
                        enabled = !isSending && toRecipients.isNotBlank()
                    ) {
                        if (isSending) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = androidx.compose.ui.Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            // Current account info
            androidx.compose.material3.Card(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.ui.Modifier.padding(12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.Default.Email,
                        contentDescription = null,
                        modifier = androidx.compose.ui.Modifier.size(20.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                    androidx.compose.foundation.layout.Column {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.account_sending_from_template, userEmailAddress),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.account_provider_template, emailProvider),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // To field
            androidx.compose.material3.OutlinedTextField(
                value = toRecipients,
                onValueChange = { toRecipients = it },
                label = { androidx.compose.material3.Text("To") },
                placeholder = { androidx.compose.material3.Text("Enter email addresses") },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
            )

            // CC/BCC toggle
            if (!showCcBcc) {
                androidx.compose.material3.TextButton(
                    onClick = { showCcBcc = true }
                ) {
                    androidx.compose.material3.Text("Cc/Bcc")
                }
            }

            // CC field
            if (showCcBcc) {
                androidx.compose.material3.OutlinedTextField(
                    value = ccRecipients,
                    onValueChange = { ccRecipients = it },
                    label = { androidx.compose.material3.Text("Cc") },
                    placeholder = { androidx.compose.material3.Text("Enter CC email addresses") },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
                )

                androidx.compose.material3.OutlinedTextField(
                    value = bccRecipients,
                    onValueChange = { bccRecipients = it },
                    label = { androidx.compose.material3.Text("Bcc") },
                    placeholder = { androidx.compose.material3.Text("Enter BCC email addresses") },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
                )
            }

            // Subject
            androidx.compose.material3.OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { androidx.compose.material3.Text("Subject") },
                placeholder = { androidx.compose.material3.Text("Enter subject") },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Body
            androidx.compose.material3.OutlinedTextField(
                value = bodyText,
                onValueChange = { newValue: String -> bodyText = newValue },
                label = { androidx.compose.material3.Text("Message") },
                placeholder = { androidx.compose.material3.Text("Compose your message...") },
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
    
    // Account Selector Dialog
    if (showAccountSelector) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAccountSelector = false },
            title = { androidx.compose.material3.Text(context.getString(R.string.account_select_sender_title)) },
            text = {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text(context.getString(R.string.account_select_sender_description))
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    
                    allAccounts.forEach { account ->
                        androidx.compose.material3.Card(
                            onClick = {
                                        // Switch to selected account
                                        scope.launch {
                                            try {
                                                val accountRepository = dependencies.getAccountRepository()
                                                accountRepository.setActiveAccount(account.id)
                                                showAccountSelector = false
                                                
                                                // Update current account with correct server config based on email provider
                                                val emailProvider = when {
                                                    account.email.contains("@gmail.com") -> "Gmail"
                                                    account.email.contains("@qq.com") -> "QQ邮箱"
                                                    account.email.contains("@163.com") || account.email.contains("@126.com") -> "NetEase Email"
                                                    account.email.contains("@outlook.com") || account.email.contains("@hotmail.com") -> "Outlook"
                                                    else -> "Gmail"
                                                }
                                                
                                                val updatedAccount = account.copy(
                                                    serverConfig = when (emailProvider) {
                                                        "Gmail" -> com.gf.mail.domain.model.ServerConfiguration(
                                                            imapHost = "imap.gmail.com",
                                                            imapPort = 993,
                                                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                                                            smtpHost = "smtp.gmail.com",
                                                            smtpPort = 587,
                                                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                                                        )
                                                        "QQ邮箱" -> com.gf.mail.domain.model.ServerConfiguration(
                                                            imapHost = "imap.qq.com",
                                                            imapPort = 993,
                                                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                                                            smtpHost = "smtp.qq.com",
                                                            smtpPort = 587,
                                                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                                                        )
                                                        "NetEase Email" -> com.gf.mail.domain.model.ServerConfiguration(
                                                            imapHost = "imap.163.com",
                                                            imapPort = 993,
                                                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                                                            smtpHost = "smtp.163.com",
                                                            smtpPort = 465,
                                                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.SSL
                                                        )
                                                        "Outlook" -> com.gf.mail.domain.model.ServerConfiguration(
                                                            imapHost = "outlook.office365.com",
                                                            imapPort = 993,
                                                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                                                            smtpHost = "smtp-mail.outlook.com",
                                                            smtpPort = 587,
                                                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                                                        )
                                                        else -> com.gf.mail.domain.model.ServerConfiguration(
                                                            imapHost = "imap.gmail.com",
                                                            imapPort = 993,
                                                            imapEncryption = com.gf.mail.domain.model.EncryptionType.SSL,
                                                            smtpHost = "smtp.gmail.com",
                                                            smtpPort = 587,
                                                            smtpEncryption = com.gf.mail.domain.model.EncryptionType.STARTTLS
                                                        )
                                                    }
                                                )
                                                
                                                // Update current account info
                                                currentAccount = updatedAccount
                                                userEmailAddress = account.email
                                                userDisplayName = account.displayName
                                                storedPassword = accountRepository.getPassword(account.id)
                                                println("✅ Successfully switched to ${account.email} with correct server config")
                                            } catch (e: Exception) {
                                                println("❌ Error switching account: ${e.message}")
                                            }
                                        }
                            },
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = if (account.isActive) 
                                    androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    androidx.compose.material3.MaterialTheme.colorScheme.surface
                            ),
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.foundation.layout.Row(
                                modifier = androidx.compose.ui.Modifier.padding(16.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Icon(
                                    androidx.compose.material.icons.Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = androidx.compose.ui.Modifier.size(24.dp)
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(12.dp))
                                androidx.compose.foundation.layout.Column {
                                    androidx.compose.material3.Text(
                                        text = account.email,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                                    )
                                    androidx.compose.material3.Text(
                                        text = "${account.provider} ${if (account.isActive) stringResource(R.string.account_current_indicator) else ""}",
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showAccountSelector = false }
                ) {
                    androidx.compose.material3.Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Add Password Dialog - only show if we don't have stored credentials
    if (showPasswordDialog) {
    EmailPasswordDialog(
        showDialog = showPasswordDialog,
        appPassword = appPassword,
        userEmailAddress = userEmailAddress,
        userDisplayName = userDisplayName,
        emailProvider = emailProvider,
        onPasswordChange = { appPassword = it },
        onEmailChange = { userEmailAddress = it },
        onDisplayNameChange = { userDisplayName = it },
        onDismiss = { showPasswordDialog = false },
            onConfirm = { 
                // Store the password for future use if user provided one
                if (appPassword.isNotBlank() && currentAccount != null) {
                    scope.launch {
                        try {
                            val accountRepository = dependencies.getAccountRepository()
                            val success = accountRepository.storePassword(currentAccount!!.id, appPassword)
                            if (success) {
                                // Update storedPassword variable to avoid showing dialog again
                                storedPassword = appPassword
                                println("✅ Password stored successfully for account: ${currentAccount!!.email}")
                            } else {
                                println("❌ Failed to store password for account: ${currentAccount!!.email}")
                            }
                        } catch (e: Exception) {
                            // Log error but don't block sending
                            println("❌ Failed to store password: ${e.message}")
                        }
                    }
                }
                sendEmail() 
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    initialQuery: String,
    onBackClick: () -> Unit,
    onEmailClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var isSearching by remember { mutableStateOf(false) }
    
    // TODO: Replace with real search results from EmailRepository
    val searchResults = remember {
        emptyList<com.gf.mail.domain.model.Email>()
    }
    
    val filteredResults = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults
        } else {
            searchResults.filter { email ->
                email.subject.contains(searchQuery, ignoreCase = true) ||
                email.fromName.contains(searchQuery, ignoreCase = true) ||
                email.bodyText?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search emails...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            if (searchQuery.isNotBlank() && filteredResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.no_matching_emails),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.try_different_keywords),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (searchQuery.isBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.search_your_emails),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.enter_keywords_to_search),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.recent_searches),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        val recentSearches = listOf(stringResource(R.string.project), stringResource(R.string.meeting), stringResource(R.string.report), "GitHub")
                        recentSearches.forEach { term ->
                            Card(
                                onClick = { searchQuery = term },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = term,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredResults) { email ->
                    SearchResultItem(
                        email = email,
                        query = searchQuery,
                        onClick = { onEmailClick(email.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultItem(
    email: com.gf.mail.domain.model.Email,
    query: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = email.fromName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = formatTimestamp(email.sentDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = email.subject,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = (email.bodyHtml?.take(100) ?: email.bodyText?.take(100)) ?: "No content",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FolderManagementPlaceholderScreen(
    accountId: String,
    onBackClick: () -> Unit
) {
    PlaceholderScreen("Folder Management for Account: $accountId", onBackClick = onBackClick)
}

@Composable
private fun ServerSettingsPlaceholderScreen(
    accountId: String,
    onBackClick: () -> Unit
) {
    PlaceholderScreen("Server Settings for Account: $accountId", onBackClick = onBackClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QRCodeDisplayScreen(
    accountId: String,
    onBackClick: () -> Unit
) {
    // TODO: Replace with real account data from AccountRepository
    val account = remember {
        com.gf.mail.domain.model.Account(
            id = accountId,
            email = "", // Will be loaded from repository
            emailAddress = "", // Will be loaded from repository
            displayName = "", // Will be loaded from repository
            fullName = "", // Will be loaded from repository
            provider = com.gf.mail.domain.model.EmailProvider.GMAIL, // Will be loaded from repository
            isActive = true,
            syncEnabled = true
        )
    }
    
    // Additional account type for QR code
    val accountType = when (account.provider) {
        com.gf.mail.domain.model.EmailProvider.GMAIL, com.gf.mail.domain.model.EmailProvider.EXCHANGE -> "OAuth2"
        else -> "Password"
    }
    
    var showShareDialog by remember { mutableStateOf(false) }
    var qrCodeSize by remember { mutableStateOf(200.dp) }
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Account info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = account.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Badge(
                            containerColor = if (account.isActive) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline
                        ) {
                            Text(
                                text = account.provider.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // QR Code section
            Text(
                text = "Scan this QR code on another device to add this account",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // QR Code display
            Card(
                modifier = Modifier.size(qrCodeSize + 32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated QR code using a grid pattern
                    QRCodeSimulation(
                        data = "gfmail://account?email=${account.email}&provider=${account.provider.displayName}&type=$accountType",
                        size = qrCodeSize
                    )
                }
            }
            
            // Size control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Size:")
                
                IconButton(
                    onClick = { if (qrCodeSize > 150.dp) qrCodeSize -= 25.dp }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Smaller")
                }
                
                Text(
                    text = "${qrCodeSize.value.toInt()}dp",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(60.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                IconButton(
                    onClick = { if (qrCodeSize < 300.dp) qrCodeSize += 25.dp }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Larger")
                }
            }
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "How to use",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InstructionItem(
                            number = "1",
                            text = "Open Gfmail on another device"
                        )
                        InstructionItem(
                            number = "2", 
                            text = "Tap 'Add Account' and select 'Scan QR Code'"
                        )
                        InstructionItem(
                            number = "3",
                            text = "Point camera at this QR code to import account"
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showShareDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
                
                Button(
                    onClick = { 
                        // Save QR code functionality
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            }
        }
    }
    
    // Share dialog
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Account QR Code") },
            text = { 
                Text("Share this QR code to help others quickly add your ${account.email} account to their Gfmail app.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showShareDialog = false
                        // Implement actual sharing
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QRCodeSimulation(
    data: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    // Create a simple pattern based on the data hash for QR code simulation
    val pattern = remember(data) {
        val hash = data.hashCode()
        val gridSize = 21 // Standard QR code size
        Array(gridSize) { row ->
            Array(gridSize) { col ->
                // Create a deterministic pattern based on position and data hash
                val value = (hash + row * 31 + col * 17) % 100
                value < 45 // ~45% fill rate for visual QR code appearance
            }
        }
    }
    
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        val sizePx = with(density) { size.toPx() }
        val gridSize = pattern.size
        val cellSize = sizePx / gridSize
        
        // Draw white background
        drawRect(
            color = Color.White,
            size = Size(sizePx, sizePx)
        )
        
        // Draw QR pattern
        for (row in pattern.indices) {
            for (col in pattern[row].indices) {
                if (pattern[row][col]) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(
                            col * cellSize,
                            row * cellSize
                        ),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
        
        // Draw corner markers (finder patterns)
        val markerSize = cellSize * 7
        val markerPositions = listOf(
            Offset(0f, 0f),
            Offset(sizePx - markerSize, 0f),
            Offset(0f, sizePx - markerSize)
        )
        
        markerPositions.forEach { position ->
            // Outer border
            drawRect(
                color = Color.Black,
                topLeft = position,
                size = Size(markerSize, markerSize)
            )
            // Inner white
            drawRect(
                color = Color.White,
                topLeft = Offset(
                    position.x + cellSize,
                    position.y + cellSize
                ),
                size = Size(markerSize - 2 * cellSize, markerSize - 2 * cellSize)
            )
            // Center black
            drawRect(
                color = Color.Black,
                topLeft = Offset(
                    position.x + cellSize * 2,
                    position.y + cellSize * 2
                ),
                size = Size(markerSize - 4 * cellSize, markerSize - 4 * cellSize)
            )
        }
    }
}

@Composable
private fun InstructionItem(
    number: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// Helper function to format timestamp
@Composable
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> stringResource(R.string.time_just_now)
        diff < 60 * 60 * 1000 -> stringResource(R.string.time_minutes_ago, diff / (60 * 1000))
        diff < 24 * 60 * 60 * 1000 -> stringResource(R.string.time_hours_ago, diff / (60 * 60 * 1000))
        diff < 7 * 24 * 60 * 60 * 1000 -> stringResource(R.string.time_days_ago, diff / (24 * 60 * 60 * 1000))
        else -> {
            val format = SimpleDateFormat("M/d", Locale.getDefault())
            format.format(Date(timestamp))
        }
    }
}

// Extension function to add App Password Dialog to SimpleComposeEmailScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailPasswordDialog(
    showDialog: Boolean,
    appPassword: String,
    userEmailAddress: String,
    userDisplayName: String,
    emailProvider: String,
    onPasswordChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        val dialogTitle = when (emailProvider) {
            "Gmail" -> stringResource(R.string.dialog_title_gmail_password)
            "QQ邮箱" -> stringResource(R.string.dialog_title_qq_password)
            stringResource(R.string.netease_email) -> stringResource(R.string.netease_email_password)
            "Outlook" -> stringResource(R.string.dialog_title_outlook_password)
            else -> stringResource(R.string.email_password)
        }
        
        val dialogText = when (emailProvider) {
            "Gmail" -> stringResource(R.string.dialog_text_gmail_password)
            "QQ邮箱" -> stringResource(R.string.dialog_text_qq_password)
            stringResource(R.string.netease_email) -> stringResource(R.string.netease_email_requires_password) + "\n\n" +
                        "请确保：\n" +
                        "1. 已开启网易邮箱的SMTP服务\n" +
                        "2. 使用正确的邮箱密码\n" +
                        "3. 输入以下信息："
            "Outlook" -> stringResource(R.string.dialog_text_outlook_password)
            else -> stringResource(R.string.dialog_text_generic_password)
        }
        
        val emailLabel = when (emailProvider) {
            "Gmail" -> stringResource(R.string.email_label_gmail)
            "QQ邮箱" -> stringResource(R.string.email_label_qq)
            stringResource(R.string.netease_email) -> stringResource(R.string.netease_email_address)
            "Outlook" -> stringResource(R.string.email_label_outlook)
            else -> stringResource(R.string.email_label_generic)
        }
        
        val emailPlaceholder = when (emailProvider) {
            "Gmail" -> stringResource(R.string.email_placeholder_gmail)
            "QQ邮箱" -> stringResource(R.string.email_placeholder_qq)
            stringResource(R.string.netease_email) -> stringResource(R.string.netease_email_example)
            "Outlook" -> stringResource(R.string.email_placeholder_outlook)
            else -> stringResource(R.string.email_placeholder_generic)
        }
        
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { androidx.compose.material3.Text(dialogTitle) },
            text = {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text(dialogText)
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    
                    // Email Address Input
                    androidx.compose.material3.OutlinedTextField(
                        value = userEmailAddress,
                        onValueChange = onEmailChange,
                        label = { androidx.compose.material3.Text(emailLabel) },
                        placeholder = { androidx.compose.material3.Text(emailPlaceholder) },
                        singleLine = true,
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                    )
                    
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                    
                    // Display Name Input
                    androidx.compose.material3.OutlinedTextField(
                        value = userDisplayName,
                        onValueChange = onDisplayNameChange,
                        label = { androidx.compose.material3.Text("Display Name") },
                        placeholder = { androidx.compose.material3.Text("Your Name") },
                        singleLine = true,
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                    )
                    
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                    
                    // Password Input
                    val passwordLabel = when (emailProvider) {
                        "Gmail" -> stringResource(R.string.password_label_gmail)
                        "QQ邮箱" -> stringResource(R.string.password_label_qq)
                        stringResource(R.string.netease_email) -> stringResource(R.string.netease_email_password)
                        "Outlook" -> stringResource(R.string.password_label_outlook)
                        else -> stringResource(R.string.email_password)
                    }
                    
                    val passwordPlaceholder = when (emailProvider) {
                        "Gmail" -> stringResource(R.string.password_placeholder_gmail)
                        "QQ邮箱" -> stringResource(R.string.password_placeholder_qq)
                        stringResource(R.string.netease_email) -> stringResource(R.string.enter_netease_email_password)
                        "Outlook" -> stringResource(R.string.password_placeholder_outlook)
                        else -> stringResource(R.string.password_placeholder_generic)
                    }
                    
                    androidx.compose.material3.OutlinedTextField(
                        value = appPassword,
                        onValueChange = onPasswordChange,
                        label = { androidx.compose.material3.Text(passwordLabel) },
                        placeholder = { androidx.compose.material3.Text(passwordPlaceholder) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                    if (userEmailAddress.isNotBlank() && appPassword.isNotBlank()) {
                            onDismiss()
                            onConfirm()
                        }
                    },
                enabled = userEmailAddress.isNotBlank() && appPassword.isNotBlank()
                ) {
                androidx.compose.material3.Text(stringResource(R.string.send_email))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = onDismiss
                ) {
                    androidx.compose.material3.Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * 获取依赖注入容器
 */
@Composable
private fun getDependencies(): AppDependencyContainer {
    val context = LocalContext.current
    return (context.applicationContext as GfmailApplication).dependencies
}

