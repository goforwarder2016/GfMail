package com.gf.mail.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gf.mail.domain.model.AccountSummary
import com.gf.mail.presentation.navigation.GfmailDestination
import com.gf.mail.presentation.navigation.GfmailNavGraph
import com.gf.mail.presentation.ui.component.AppDrawerContent
import kotlinx.coroutines.launch

/**
 * Main app composable with navigation drawer and responsive layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GfmailApp() {
    // TODO: Replace with proper dependency injection when Hilt is configured
    // val mainViewModel: MainViewModel = hiltViewModel()
    // TODO: Replace with proper Hilt ViewModel injection
    // val mainViewModel: MainViewModel = viewModel()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current

    // Get current destination
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    // TODO: Observe ViewModel state when properly implemented
    // val uiState by mainViewModel.uiState.collectAsState()
    // val currentAccount by mainViewModel.currentAccount.collectAsState()
    // val accountSummary by mainViewModel.accountSummary.collectAsState()

    // Real user account state
    var currentUserAccount by remember { mutableStateOf<com.gf.mail.domain.model.Account?>(null) }
    var availableAccounts by remember { mutableStateOf<List<com.gf.mail.domain.model.Account>>(emptyList()) }
    var emailCount by remember { mutableStateOf(0) }
    var unreadCount by remember { mutableStateOf(0) }
    var lastSyncTime by remember { mutableStateOf<Long?>(null) }
    
    // Load accounts from repository
    LaunchedEffect(Unit) {
        try {
            println("🔄 Loading account list...")
            
            // 获取依赖注入容器
            val application = com.gf.mail.GfmailApplication.instance
            val dependencies = application.dependencies
            val accountRepository = dependencies.getAccountRepository()
            
            // 加载所有账号
            val accounts = accountRepository.getAllAccounts()
            println("📧 Loaded ${accounts.size} accounts from database")
            
            if (accounts.isNotEmpty()) {
                // 获取当前活跃账号
                val activeAccount = accounts.firstOrNull { it.isActive } ?: accounts.first()
                currentUserAccount = activeAccount
                availableAccounts = accounts
                
                println("✅ Active account: ${activeAccount.email}")
                println("📋 Available accounts: ${accounts.map { it.email }}")
            } else {
                println("⚠️ No accounts found in database")
                currentUserAccount = null
                availableAccounts = emptyList()
            }
        } catch (e: Exception) {
            println("❌ Failed to load accounts: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Get account from state
    val currentAccount = currentUserAccount
    val hasAccounts = availableAccounts.isNotEmpty() || currentAccount != null
    
    // Simulate email sync when account is added
    LaunchedEffect(currentAccount) {
        if (currentAccount != null) {
            println("🚨🚨🚨 [APP_CRITICAL] GfmailApp sync started - version: 2025-01-08-v5")
            println("🚨🚨🚨 [APP_CRITICAL] current time: ${System.currentTimeMillis()}")
            println("🔄 Detected new account, starting real email sync...")
            
            // 使用EmailSyncService进行同步
            try {
                // 获取依赖注入容器
                val application = com.gf.mail.GfmailApplication.instance
                val dependencies = application.dependencies
                val emailSyncService = dependencies.getEmailSyncService()
                
                println("📧 Starting real email sync using EmailSyncService: ${currentAccount.email}")
                
                // 使用EmailSyncService进行同步
                val syncResult = emailSyncService.syncAccount(currentAccount.id)
                if (syncResult.isSuccess) {
                    println("✅ EmailSyncService sync completed successfully")
                    
                    // 更新UI状态
                    emailCount = 0 // 从数据库获取实际数量
                    unreadCount = 0 // 从数据库获取实际数量
                    lastSyncTime = System.currentTimeMillis()
                } else {
                    println("❌ EmailSyncService sync failed: ${syncResult.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                println("❌ Email sync exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    val accountSummary = AccountSummary(
        id = currentAccount?.id ?: "temp",
        email = currentAccount?.email ?: "",
        displayName = currentAccount?.displayName ?: "No Account",
        provider = currentAccount?.provider ?: com.gf.mail.domain.model.EmailProvider.IMAP,
        isActive = currentAccount?.isActive ?: false,
        unreadCount = unreadCount,
        totalEmails = emailCount,
        lastSyncTime = lastSyncTime,
        syncStatus = if (currentAccount != null) com.gf.mail.domain.model.SyncStatus.SYNCING else com.gf.mail.domain.model.SyncStatus.IDLE,
        hasError = false,
        errorMessage = null
    )

    // Determine if we should use permanent drawer (tablets) or modal drawer (phones)
    val useModalDrawer = configuration.screenWidthDp < 840

    if (useModalDrawer) {
        // Modal drawer for smaller screens
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                    AppDrawerContent(
                        currentAccount = currentAccount,
                        availableAccounts = availableAccounts,
                        accountSummary = accountSummary,
                        currentDestination = currentDestination,
                        onDestinationClick = { destination ->
                            navController.navigate(destination) {
                                // Clear back stack when navigating to main destinations
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        onAccountSelected = { account ->
                            // Implement account switching
                            scope.launch {
                                try {
                                    val application = com.gf.mail.GfmailApplication.instance
                                    val dependencies = application.dependencies
                                    val accountRepository = dependencies.getAccountRepository()
                                    
                                    // Set the selected account as active
                                    accountRepository.setActiveAccount(account.id)
                                    
                                    // Update local state
                                    currentUserAccount = account
                                    
                                    println("✅ Switched to account: ${account.email}")
                                } catch (e: Exception) {
                                    println("❌ Failed to switch account: ${e.message}")
                                }
                            }
                        },
                        onAddAccountClick = {
                            navController.navigate(GfmailDestination.AddAccount.route)
                            scope.launch { drawerState.close() }
                        },
                        onManageAccountsClick = {
                            navController.navigate(GfmailDestination.AccountList.route)
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            GfmailContent(
                onMenuClick = { scope.launch { drawerState.open() } },
                navController = navController,
                hasAccounts = hasAccounts,
                onAccountAdded = { account -> 
                    currentUserAccount = account
                    availableAccounts = listOf(account)
                    println("✅ Account added: ${account.email}")
                }
            )
        }
    } else {
        // Permanent drawer for larger screens
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(modifier = Modifier.width(300.dp)) {
                    AppDrawerContent(
                        currentAccount = currentAccount,
                        availableAccounts = availableAccounts,
                        accountSummary = accountSummary,
                        currentDestination = currentDestination,
                        onDestinationClick = { destination ->
                            navController.navigate(destination) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onAccountSelected = { account ->
                            // Implement account switching
                            scope.launch {
                                try {
                                    val application = com.gf.mail.GfmailApplication.instance
                                    val dependencies = application.dependencies
                                    val accountRepository = dependencies.getAccountRepository()
                                    
                                    // Set the selected account as active
                                    accountRepository.setActiveAccount(account.id)
                                    
                                    // Update local state
                                    currentUserAccount = account
                                    
                                    println("✅ Switched to account: ${account.email}")
                                } catch (e: Exception) {
                                    println("❌ Failed to switch account: ${e.message}")
                                }
                            }
                        },
                        onAddAccountClick = {
                            navController.navigate(GfmailDestination.AddAccount.route)
                        },
                        onManageAccountsClick = {
                            navController.navigate(GfmailDestination.AccountList.route)
                        }
                    )
                }
            }
        ) {
            GfmailContent(
                onMenuClick = null, // No menu button needed for permanent drawer
                navController = navController,
                hasAccounts = hasAccounts,
                onAccountAdded = { account -> 
                    currentUserAccount = account
                    availableAccounts = listOf(account)
                    println("✅ Account added: ${account.email}")
                }
            )
        }
    }
}

/**
 * Main content area with top bar and navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GfmailContent(
    onMenuClick: (() -> Unit)?,
    navController: androidx.navigation.NavHostController,
    hasAccounts: Boolean,
    onAccountAdded: (com.gf.mail.domain.model.Account) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gfmail") },
                navigationIcon = {
                    if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(GfmailDestination.Search.createRoute())
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            GfmailNavGraph(
                navController = navController,
                hasAccounts = hasAccounts,
                onAccountAdded = onAccountAdded
            )
        }
    }
}
