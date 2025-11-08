package com.gf.mail.presentation.ui.accounts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.gf.mail.domain.model.*
import com.gf.mail.presentation.viewmodel.AccountManagementViewModel
import com.gf.mail.presentation.viewmodel.AccountManagementViewModel.AccountManagementUiState
import com.gf.mail.domain.model.AddAccountStep
import kotlinx.coroutines.launch

/**
 * Add account screen with step-by-step flow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    viewModel: AccountManagementViewModel,
    uiState: AccountManagementUiState,
    onBackClick: () -> Unit,
    onAccountAdded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // OAuth2 launcher
    val oauth2Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // OAuth2 result will be handled via deep link
        viewModel.clearOAuth2Intent()
    }

    // Launch OAuth2 intent when available
    LaunchedEffect(uiState.oauth2Intent) {
        uiState.oauth2Intent?.let { intent ->
            oauth2Launcher.launch(intent)
        }
    }

    // Handle success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onAccountAdded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.gf.mail.R.string.account_add_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.addAccountStep == AddAccountStep.SELECT_PROVIDER) {
                            onBackClick()
                        } else {
                            viewModel.cancelAddAccount()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.addAccountStep) {
                AddAccountStep.SELECT_PROVIDER -> {
                    ProviderSelectionStep(
                        onProviderSelected = viewModel::selectProvider,
                        onQRCodeDisplayClick = viewModel::startQRCodeDisplay,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AddAccountStep.COMPLETED -> {
                    QRCodeDisplayForSetupScreen(
                        onBackClick = { viewModel.cancelAddAccount() },
                        onManualEntry = { viewModel.cancelAddAccount() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AddAccountStep.ENTER_CREDENTIALS -> {
                    EmailInputStep(
                        emailAddress = uiState.emailAddress,
                        onEmailChanged = viewModel::setEmailAddress,
                        onContinue = {
                            if (uiState.selectedProvider in listOf(
                                    EmailProvider.GMAIL,
                                    EmailProvider.EXCHANGE
                                )) {
                                uiState.selectedProvider?.let { provider ->
                                    viewModel.startOAuth2Authentication(provider)
                                }
                            } else {
                                // Go to password input for other providers (IMAP/POP3)
                                viewModel.showPasswordInput()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AddAccountStep.OAUTH_AUTHENTICATION -> {
                    OAuth2AuthStep(
                        provider = uiState.selectedProvider ?: EmailProvider.GMAIL,
                        emailAddress = uiState.emailAddress,
                        isLoading = uiState.isLoading,
                        onStartAuth = { 
                            uiState.selectedProvider?.let { provider ->
                                viewModel.startOAuth2Authentication(provider)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AddAccountStep.PASSWORD_AUTHENTICATION -> {
                    PasswordInputStep(
                        provider = uiState.selectedProvider ?: EmailProvider.IMAP,
                        emailAddress = uiState.emailAddress,
                        isAppPassword = uiState.isAppPassword,
                        onAppPasswordChanged = viewModel::setAppPassword,
                        onAuthenticate = { _ -> viewModel.authenticateWithPassword() },
                        onShowManualConfig = viewModel::showManualServerConfig,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AddAccountStep.MANUAL_SERVER_CONFIG -> {
                    ServerConfigStep(
                        serverConfig = uiState.customServerConfig,
                        onServerConfigChanged = viewModel::updateServerConfig,
                        onTestConnection = { _, _ ->
                            viewModel.testConnection()
                        },
                        onContinue = { _ ->
                            viewModel.authenticateWithPassword()
                        },
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    // Fallback
                    ProviderSelectionStep(
                        onProviderSelected = viewModel::selectProvider,
                        onQRCodeDisplayClick = viewModel::startQRCodeDisplay,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp) // 避免覆盖TopAppBar
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            scope.launch {
                // Show snackbar
            }
        }
    }
}

/**
 * Step 1: Provider selection
 */
@Composable
private fun ProviderSelectionStep(
    onProviderSelected: (EmailProvider) -> Unit,
    onQRCodeDisplayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(com.gf.mail.R.string.choose_email_provider),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(com.gf.mail.R.string.select_email_provider_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // QR Code Display option
        QRCodeDisplayCard(
            onClick = onQRCodeDisplayClick
        )

        // Manual entry section
        Text(
            text = stringResource(com.gf.mail.R.string.or_choose_manual_entry),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        // Gmail
        ProviderCard(
            provider = EmailProvider.GMAIL,
            title = context.getString(com.gf.mail.R.string.account_gmail),
            subtitle = stringResource(com.gf.mail.R.string.gmail_subtitle),
            icon = Icons.Default.Email,
            authMethod = stringResource(com.gf.mail.R.string.gmail_auth_method),
            authColor = Color(0xFF4CAF50),
            onClick = { onProviderSelected(EmailProvider.GMAIL) }
        )

        // Outlook/Exchange
        ProviderCard(
            provider = EmailProvider.EXCHANGE,
            title = context.getString(com.gf.mail.R.string.account_exchange),
            subtitle = stringResource(com.gf.mail.R.string.account_exchange_subtitle),
            icon = Icons.Default.AccountCircle,
            authMethod = stringResource(com.gf.mail.R.string.account_exchange_auth_method),
            authColor = Color(0xFF4CAF50),
            onClick = { onProviderSelected(EmailProvider.EXCHANGE) }
        )

        // Generic IMAP
        ProviderCard(
            provider = EmailProvider.IMAP,
            title = stringResource(com.gf.mail.R.string.account_other_imap),
            subtitle = stringResource(com.gf.mail.R.string.custom_email_provider_subtitle),
            icon = Icons.Default.Email,
            authMethod = context.getString(com.gf.mail.R.string.account_password),
            authColor = Color(0xFF9C27B0),
            onClick = { onProviderSelected(EmailProvider.IMAP) }
        )
    }
}

/**
 * Provider selection card
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ProviderCard(
    provider: EmailProvider,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    authMethod: String,
    authColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Badge(
                    containerColor = authColor,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = authMethod,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Step 2: Email input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailInputStep(
    emailAddress: String,
    onEmailChanged: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(com.gf.mail.R.string.enter_email_address_label),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = emailAddress,
            onValueChange = onEmailChanged,
            label = { Text(stringResource(com.gf.mail.R.string.email_address_label)) },
            placeholder = { Text("example@gmail.com") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onContinue,
            enabled = emailAddress.contains("@"),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(com.gf.mail.R.string.continue_button))
        }
    }
}

/**
 * Step 3: OAuth2 authentication
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OAuth2AuthStep(
    provider: EmailProvider,
    emailAddress: String,
    isLoading: Boolean,
    onStartAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = when (provider) {
                EmailProvider.GMAIL -> Icons.Default.Email
                EmailProvider.EXCHANGE -> Icons.Default.AccountCircle
                else -> Icons.Default.Lock
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(com.gf.mail.R.string.secure_authentication),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Sign in to $emailAddress using ${provider.displayName}'s secure authentication",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onStartAuth,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign in with ${provider.displayName}")
            }
        }
    }
}

/**
 * Step 4: Password input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordInputStep(
    provider: EmailProvider,
    emailAddress: String,
    isAppPassword: Boolean,
    onAppPasswordChanged: (Boolean) -> Unit,
    onAuthenticate: (String) -> Unit,
    onShowManualConfig: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(com.gf.mail.R.string.enter_password_label),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "For $emailAddress",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(if (isAppPassword) stringResource(com.gf.mail.R.string.app_password_label) else stringResource(com.gf.mail.R.string.password_label)) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.Close else Icons.Default.Person,
                        contentDescription = if (showPassword) stringResource(com.gf.mail.R.string.hide_password) else stringResource(com.gf.mail.R.string.show_password)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // App password toggle for supported providers
        if (provider == EmailProvider.GMAIL) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAppPassword,
                    onCheckedChange = onAppPasswordChanged
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "This is an app-specific password",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = { onAuthenticate(password) },
            enabled = password.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(stringResource(com.gf.mail.R.string.sign_in))
        }

        TextButton(
            onClick = onShowManualConfig,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(com.gf.mail.R.string.manual_server_configuration))
        }
    }
}

/**
 * Step 5: Server configuration
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ServerConfigStep(
    serverConfig: ServerConfiguration?,
    onServerConfigChanged: (ServerConfiguration) -> Unit,
    onTestConnection: (String, ServerConfiguration) -> Unit,
    onContinue: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var imapHost by remember { mutableStateOf(serverConfig?.imapHost ?: "") }
    var imapPort by remember { mutableStateOf(serverConfig?.imapPort?.toString() ?: "993") }
    var imapEncryption by remember {
        mutableStateOf(serverConfig?.imapEncryption ?: EncryptionType.SSL)
    }

    var smtpHost by remember { mutableStateOf(serverConfig?.smtpHost ?: "") }
    var smtpPort by remember { mutableStateOf(serverConfig?.smtpPort?.toString() ?: "587") }
    var smtpEncryption by remember {
        mutableStateOf(serverConfig?.smtpEncryption ?: EncryptionType.STARTTLS)
    }

    // Update server config when values change
    LaunchedEffect(imapHost, imapPort, imapEncryption, smtpHost, smtpPort, smtpEncryption) {
        val port1 = imapPort.toIntOrNull() ?: 993
        val port2 = smtpPort.toIntOrNull() ?: 587
        onServerConfigChanged(
            ServerConfiguration(
                imapHost = imapHost.takeIf { it.isNotBlank() },
                imapPort = port1,
                imapEncryption = imapEncryption,
                smtpHost = smtpHost.takeIf { it.isNotBlank() },
                smtpPort = port2,
                smtpEncryption = smtpEncryption
            )
        )
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(com.gf.mail.R.string.server_configuration),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        // IMAP Settings
        Text(
            text = "IMAP Settings (Incoming Mail)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = imapHost,
            onValueChange = { imapHost = it },
            label = { Text(stringResource(com.gf.mail.R.string.imap_server)) },
            placeholder = { Text("imap.example.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = imapPort,
                onValueChange = { imapPort = it },
                label = { Text("Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            // Encryption dropdown would go here
        }

        // SMTP Settings
        Text(
            text = "SMTP Settings (Outgoing Mail)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        OutlinedTextField(
            value = smtpHost,
            onValueChange = { smtpHost = it },
            label = { Text(stringResource(com.gf.mail.R.string.smtp_server)) },
            placeholder = { Text("smtp.example.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = smtpPort,
                onValueChange = { smtpPort = it },
                label = { Text("Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        // Password input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(com.gf.mail.R.string.password)) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.Close else Icons.Default.Person,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Test connection button
        OutlinedButton(
            onClick = {
                serverConfig?.let { config ->
                    onTestConnection(password, config)
                }
            },
            enabled = password.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(com.gf.mail.R.string.test_connection))
        }

        // Continue button
        Button(
            onClick = { onContinue(password) },
            enabled = password.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(com.gf.mail.R.string.add_account))
        }
    }
}

/**
 * QR Code display card for sharing account configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QRCodeDisplayCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // QR Code display icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(com.gf.mail.R.string.display_qr_code),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(com.gf.mail.R.string.show_qr_code_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = stringResource(com.gf.mail.R.string.share_setup),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * QR Code display screen for account setup sharing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QRCodeDisplayForSetupScreen(
    onBackClick: () -> Unit,
    onManualEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAccountType by remember { mutableStateOf("Gmail") }
    var emailAddress by remember { mutableStateOf("example@gmail.com") }
    var qrCodeSize by remember { mutableStateOf(180.dp) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.gf.mail.R.string.display_qr_code)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(com.gf.mail.R.string.generate_qr_code_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = stringResource(com.gf.mail.R.string.generate_qr_code_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // Account configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(com.gf.mail.R.string.account_configuration),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Account type selection
                    Text(
                        text = "Account Type:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedAccountType == "Gmail",
                            onClick = { 
                                selectedAccountType = "Gmail"
                                emailAddress = "example@gmail.com"
                            },
                            label = { Text(stringResource(com.gf.mail.R.string.account_gmail)) }
                        )
                        FilterChip(
                            selected = selectedAccountType == "Outlook",
                            onClick = { 
                                selectedAccountType = "Outlook"
                                emailAddress = "example@outlook.com"
                            },
                            label = { Text(stringResource(com.gf.mail.R.string.outlook)) }
                        )
                        FilterChip(
                            selected = selectedAccountType == "IMAP",
                            onClick = { 
                                selectedAccountType = "IMAP"
                                emailAddress = "example@company.com"
                            },
                            label = { Text("IMAP") }
                        )
                    }
                    
                    // Email address
                    OutlinedTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        label = { Text(stringResource(com.gf.mail.R.string.email_address_label)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
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
                    QRCodeSimulationForSetup(
                        accountType = selectedAccountType,
                        email = emailAddress,
                        size = qrCodeSize
                    )
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
                            text = stringResource(com.gf.mail.R.string.how_to_use_qr_code),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Text(
                        text = "1. Open Gfmail on the target device\n" +
                              "2. Tap 'Add Account' → 'Scan QR Code'\n" +
                              "3. Point the camera at this QR code\n" +
                              "4. Complete the authentication process",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onManualEntry,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(com.gf.mail.R.string.manual_setup))
                }
                
                Button(
                    onClick = { 
                        // In a real implementation, this would save or share the QR code
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(com.gf.mail.R.string.share))
                }
            }
        }
    }
}

/**
 * QR Code simulation for account setup
 */
@Composable
private fun QRCodeSimulationForSetup(
    accountType: String,
    email: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    // Create QR code data string
    val qrData = "gfmail://setup?type=$accountType&email=$email&auto=true"
    
    // Use the same QR simulation logic as the main QR display screen
    val pattern = remember(qrData) {
        val hash = qrData.hashCode()
        val gridSize = 21
        Array(gridSize) { row ->
            Array(gridSize) { col ->
                val value = (hash + row * 31 + col * 17) % 100
                value < 45
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
        
        // Draw corner markers
        val markerSize = cellSize * 7
        val markerPositions = listOf(
            Offset(0f, 0f),
            Offset(sizePx - markerSize, 0f),
            Offset(0f, sizePx - markerSize)
        )
        
        markerPositions.forEach { position ->
            drawRect(
                color = Color.Black,
                topLeft = position,
                size = Size(markerSize, markerSize)
            )
            drawRect(
                color = Color.White,
                topLeft = Offset(position.x + cellSize, position.y + cellSize),
                size = Size(markerSize - 2 * cellSize, markerSize - 2 * cellSize)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(position.x + cellSize * 2, position.y + cellSize * 2),
                size = Size(markerSize - 4 * cellSize, markerSize - 4 * cellSize)
            )
        }
    }
}
