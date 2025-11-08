package com.gf.mail.presentation.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gf.mail.GfmailApplication
import com.gf.mail.domain.model.SecurityRecommendation
import com.gf.mail.domain.model.SecurityRecommendationPriority
import com.gf.mail.domain.model.SecurityRecommendationType
import com.gf.mail.presentation.viewmodel.SecuritySettingsViewModel
import com.gf.mail.utils.BiometricAvailability
import com.gf.mail.utils.PasswordStrengthLevel

/**
 * Security settings screen with comprehensive security controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dependencies = (context.applicationContext as GfmailApplication).dependencies
    val viewModel: SecuritySettingsViewModel = dependencies.createSecuritySettingsViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()

    // Local states for dialogs
    var showAutoLockTimeoutDialog by remember { mutableStateOf(false) }
    var showSessionTimeoutDialog by remember { mutableStateOf(false) }
    var showAddHostnameDialog by remember { mutableStateOf(false) }
    var showPasswordStrengthDialog by remember { mutableStateOf(false) }
    var showAuthenticationDialog by remember { mutableStateOf(false) }
    var showResetConfirmationDialog by remember { mutableStateOf(false) }

    // Handle session expiry
    LaunchedEffect(uiState.sessionExpired) {
        if (uiState.sessionExpired) {
            showAuthenticationDialog = true
        }
    }

    // Handle messages and errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // TODO: Show error snackbar
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            // TODO: Show success snackbar
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(com.gf.mail.R.string.security_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = context.getString(com.gf.mail.R.string.cd_back_button))
                    }
                },
                actions = {
                    // Reset security settings
                    IconButton(onClick = { showResetConfirmationDialog = true }) {
                        Icon(
                            Icons.Default.RestoreFromTrash,
                            contentDescription = "Reset Security Settings",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Status Overview
            item {
                SecurityStatusCard(
                    biometricAvailability = if (uiState.biometricAvailability) com.gf.mail.utils.BiometricAvailability.AVAILABLE else com.gf.mail.utils.BiometricAvailability.NO_HARDWARE,
                    hasSecureLockScreen = uiState.hasSecureLockScreen,
                    sessionExpired = uiState.sessionExpired,
                    onAuthenticateClick = { showAuthenticationDialog = true },
                    onCreateSessionClick = { viewModel.createSession() }
                )
            }

            // Security Recommendations
            if (recommendations.isNotEmpty()) {
                item {
                    SecurityRecommendationsSection(
                        recommendations = recommendations,
                        onRecommendationClick = { recommendation ->
                            when (recommendation.type) {
                                SecurityRecommendationType.BIOMETRIC_AUTH -> {
                                    viewModel.toggleBiometricAuthentication(true)
                                }
                                SecurityRecommendationType.AUTO_LOCK -> {
                                    viewModel.toggleAutoLock(true)
                                }
                                SecurityRecommendationType.CERTIFICATE_PINNING -> {
                                    viewModel.toggleCertificatePinning(true)
                                }
                                SecurityRecommendationType.DATA_ENCRYPTION -> {
                                    viewModel.toggleDataEncryption(true)
                                }
                                else -> {
                                    // Handle other recommendations
                                }
                            }
                        }
                    )
                }
            }

            // Authentication Section
            item {
                SecuritySection(
                        title = context.getString(com.gf.mail.R.string.security_section_authentication),
                    icon = Icons.Default.Fingerprint
                ) {
                    // Biometric Authentication
                    SwitchSettingsRow(
                        title = "Biometric",
                        subtitle = getBiometricStatusText(if (uiState.biometricAvailability) com.gf.mail.utils.BiometricAvailability.AVAILABLE else com.gf.mail.utils.BiometricAvailability.NO_HARDWARE),
                        icon = Icons.Default.Fingerprint,
                        checked = uiState.securitySettings?.biometricAuthEnabled ?: false,
                        enabled = uiState.biometricAvailability,
                        onCheckedChange = { enabled ->
                            if (enabled && context is FragmentActivity) {
                                viewModel.authenticateWithBiometrics()
                            } else {
                                viewModel.toggleBiometricAuthentication(enabled)
                            }
                        }
                    )

                    // Require biometric for sensitive operations
                    SwitchSettingsRow(
                        title = "Secure Sensitive Operations",
                        subtitle = "Require biometric authentication for sensitive actions",
                        icon = Icons.Default.Security,
                        checked = false, // TODO: Add requireBiometricForSensitiveOperations to SecuritySetting
                        enabled = uiState.securitySettings?.biometricAuthEnabled ?: false,
                        onCheckedChange = viewModel::toggleRequireBiometricForSensitiveOperations
                    )

                    // Manual authentication test
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.security_test_auth),
                        subtitle = "Test biometric or device credential authentication",
                        icon = Icons.Default.VpnKey,
                        onClick = { showAuthenticationDialog = true }
                    )
                }
            }

            // App Lock Section
            item {
                SecuritySection(
                    title = context.getString(com.gf.mail.R.string.security_section_app_lock),
                    icon = Icons.Default.Lock
                ) {
                    SwitchSettingsRow(
                        title = "Auto-Lock",
                        subtitle = "Lock app after ${uiState.securitySettings?.autoLockTimeout ?: 5} minutes of inactivity",
                        icon = Icons.Default.Timer,
                        checked = uiState.securitySettings?.autoLockEnabled ?: false,
                        onCheckedChange = { viewModel.toggleAutoLock(it) }
                    )

                    if (uiState.securitySettings?.autoLockEnabled == true) {
                        SettingsRow(
                            title = "Auto-Lock Timeout",
                            subtitle = "${uiState.securitySettings?.autoLockTimeout ?: 5} minutes",
                            icon = Icons.Default.Schedule,
                            onClick = { showAutoLockTimeoutDialog = true }
                        )
                    }
                }
            }

            // Data Protection Section
            item {
                SecuritySection(
                    title = context.getString(com.gf.mail.R.string.security_section_data_protection),
                    icon = Icons.Default.Shield
                ) {
                    SwitchSettingsRow(
                        title = "Encrypt Sensitive Data",
                        subtitle = "Encrypt passwords and sensitive information",
                        icon = Icons.Default.Lock,
                        checked = uiState.securitySettings?.advancedSecurityEnabled ?: false,
                        onCheckedChange = viewModel::toggleDataEncryption
                    )

                    SettingsRow(
                        title = "Password Strength Checker",
                        subtitle = "Test password security strength",
                        icon = Icons.Default.Password,
                        onClick = { showPasswordStrengthDialog = true }
                    )
                }
            }

            // Network Security Section
            item {
                SecuritySection(
                    title = context.getString(com.gf.mail.R.string.security_section_network_security),
                    icon = Icons.Default.NetworkCheck
                ) {
                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.security_certificate_pinning),
                        subtitle = "Protect against man-in-the-middle attacks",
                        icon = Icons.Default.Security,
                        checked = uiState.securitySettings?.advancedSecurityEnabled ?: false,
                        onCheckedChange = viewModel::toggleCertificatePinning
                    )

                    SettingsRow(
                        title = "Allowed Hostnames",
                        subtitle = "${uiState.securitySettings?.trustedHosts?.size ?: 0} hostnames configured",
                        icon = Icons.Default.DomainVerification,
                        onClick = { showAddHostnameDialog = true }
                    )

                    // Display allowed hostnames
                    uiState.securitySettings?.trustedHosts?.forEach { hostname ->
                        HostnameItem(
                            hostname = hostname,
                            onRemove = { viewModel.removeAllowedHostname(hostname) }
                        )
                    }
                }
            }

            // Session Management Section
            item {
                SecuritySection(
                    title = "Session Management",
                    icon = Icons.Default.AccessTime
                ) {
                    SettingsRow(
                        title = "Session Timeout",
                        subtitle = "${uiState.securitySettings?.sessionTimeout ?: 30} minutes",
                        icon = Icons.Default.Timer,
                        onClick = { showSessionTimeoutDialog = true }
                    )

                    SettingsRow(
                        title = "Create New Session",
                        subtitle = "Generate a new secure session",
                        icon = Icons.Default.Refresh,
                        onClick = { viewModel.createSession() }
                    )

                    SettingsRow(
                        title = "Validate Current Session",
                        subtitle = "Check if current session is valid",
                        icon = Icons.Default.CheckCircle,
                        onClick = { viewModel.validateSession() }
                    )

                    SettingsRow(
                        title = "End Current Session",
                        subtitle = "Invalidate current session",
                        icon = Icons.Default.Logout,
                        onClick = { viewModel.invalidateSession() }
                    )
                }
            }
        }
    }

    // Auto-Lock Timeout Selection Dialog
    if (showAutoLockTimeoutDialog) {
        AutoLockTimeoutSelectionDialog(
            currentTimeout = uiState.securitySettings?.autoLockTimeout ?: 5,
            onTimeoutSelected = { timeout ->
                viewModel.updateAutoLockTimeout(timeout)
                showAutoLockTimeoutDialog = false
            },
            onDismiss = { showAutoLockTimeoutDialog = false }
        )
    }

    // Session Timeout Selection Dialog
    if (showSessionTimeoutDialog) {
        SessionTimeoutSelectionDialog(
            currentTimeout = uiState.securitySettings?.sessionTimeout ?: 30,
            onTimeoutSelected = { timeout ->
                viewModel.updateSessionTimeout(timeout)
                showSessionTimeoutDialog = false
            },
            onDismiss = { showSessionTimeoutDialog = false }
        )
    }

    // Add Hostname Dialog
    if (showAddHostnameDialog) {
        AddHostnameDialog(
            allowedHostnames = uiState.securitySettings?.trustedHosts ?: emptyList(),
            onHostnameAdded = { hostname ->
                viewModel.addAllowedHostname(hostname)
                showAddHostnameDialog = false
            },
            onDismiss = { showAddHostnameDialog = false }
        )
    }

    // Password Strength Test Dialog
    if (showPasswordStrengthDialog) {
        PasswordStrengthDialog(
            onPasswordTest = { password -> 
                val strength = viewModel.validatePasswordStrength(password)
                when {
                    strength >= 80 -> com.gf.mail.utils.PasswordStrength.STRONG
                    strength >= 60 -> com.gf.mail.utils.PasswordStrength.MEDIUM
                    strength >= 40 -> com.gf.mail.utils.PasswordStrength.WEAK
                    else -> com.gf.mail.utils.PasswordStrength.VERY_WEAK
                }
            },
            onDismiss = { showPasswordStrengthDialog = false }
        )
    }

    // Authentication Dialog
    if (showAuthenticationDialog) {
        AuthenticationDialog(
            biometricAvailability = if (uiState.biometricAvailability) com.gf.mail.utils.BiometricAvailability.AVAILABLE else com.gf.mail.utils.BiometricAvailability.NO_HARDWARE,
            isAuthenticating = uiState.isAuthenticating,
            onBiometricAuth = {
                if (context is FragmentActivity) {
                    viewModel.authenticateWithBiometrics()
                }
            },
            onDeviceCredentialAuth = {
                if (context is FragmentActivity) {
                    viewModel.authenticateWithDeviceCredentials()
                }
            },
            onDismiss = {
                showAuthenticationDialog = false
                if (uiState.sessionExpired) {
                    viewModel.acknowledgeSessionExpiry()
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmationDialog = false },
            title = { Text("Reset Security Settings") },
            text = {
                Text(
                    "This will reset all security settings to their default values. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSecuritySettings()
                        showResetConfirmationDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(context.getString(com.gf.mail.R.string.accessibility_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SecurityStatusCard(
    biometricAvailability: BiometricAvailability,
    hasSecureLockScreen: Boolean,
    sessionExpired: Boolean,
    onAuthenticateClick: () -> Unit,
    onCreateSessionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (sessionExpired) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Security Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    if (sessionExpired) Icons.Default.Warning else Icons.Default.Security,
                    contentDescription = null,
                    tint = if (sessionExpired) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            if (sessionExpired) {
                Text(
                    text = "Session Expired",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Your session has expired. Please authenticate to continue using the app securely.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onAuthenticateClick) {
                        Text("Authenticate")
                    }
                    TextButton(onClick = onCreateSessionClick) {
                        Text("New Session")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SecurityStatusItem(
                        title = "Biometric",
                        status = when (biometricAvailability) {
                            BiometricAvailability.AVAILABLE -> "Available"
                            BiometricAvailability.NO_HARDWARE -> "Not Available"
                            BiometricAvailability.NONE_ENROLLED -> "Not Enrolled"
                            else -> "Unknown"
                        },
                        icon = Icons.Default.Fingerprint,
                        isGood = biometricAvailability == BiometricAvailability.AVAILABLE
                    )

                    SecurityStatusItem(
                        title = "Lock Screen",
                        status = if (hasSecureLockScreen) "Secure" else "Not Secure",
                        icon = Icons.Default.Lock,
                        isGood = hasSecureLockScreen
                    )

                    SecurityStatusItem(
                        title = "Session",
                        status = "Active",
                        icon = Icons.Default.CheckCircle,
                        isGood = true
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityStatusItem(
    title: String,
    status: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGood: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isGood) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SecurityRecommendationsSection(
    recommendations: List<SecurityRecommendation>,
    onRecommendationClick: (SecurityRecommendation) -> Unit
) {
    SecuritySection(
        title = "Security Recommendations",
        icon = Icons.Default.Lightbulb
    ) {
        recommendations.forEach { recommendation ->
            SecurityRecommendationCard(
                recommendation = recommendation,
                onClick = { onRecommendationClick(recommendation) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecurityRecommendationCard(
    recommendation: SecurityRecommendation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (recommendation.priority) {
                SecurityRecommendationPriority.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                SecurityRecommendationPriority.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = if (recommendation.actionRequired) onClick else { {} }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    when (recommendation.priority) {
                        SecurityRecommendationPriority.CRITICAL -> Icons.Default.Error
                        SecurityRecommendationPriority.HIGH -> Icons.Default.Warning
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium
            )

            if (recommendation.actionRequired) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClick) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun SecuritySection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            content()
        }
    }
}

@Composable
private fun HostnameItem(
    hostname: String,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = hostname,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Remove hostname",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Helper functions
private fun getBiometricStatusText(availability: BiometricAvailability): String {
    return when (availability) {
        BiometricAvailability.AVAILABLE -> "Available and ready to use"
        BiometricAvailability.NO_HARDWARE -> "No biometric hardware available"
        BiometricAvailability.HARDWARE_UNAVAILABLE -> "Biometric hardware unavailable"
        BiometricAvailability.NONE_ENROLLED -> "No biometrics enrolled"
        BiometricAvailability.SECURITY_UPDATE_REQUIRED -> "Security update required"
        BiometricAvailability.UNSUPPORTED -> "Not supported on this device"
        BiometricAvailability.UNKNOWN -> "Status unknown"
    }
}

// Dialog components would be implemented here following the same pattern
// as other dialogs in the app (simplified for brevity)

@Composable
private fun AutoLockTimeoutSelectionDialog(
    currentTimeout: Int,
    onTimeoutSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timeoutOptions = listOf(1, 2, 5, 10, 15, 30, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auto-Lock Timeout") },
        text = {
            Column {
                timeoutOptions.forEach { timeout ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = timeout == currentTimeout,
                            onClick = { onTimeoutSelected(timeout) }
                        )
                        Text(
                            text = "$timeout minute${if (timeout > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun SessionTimeoutSelectionDialog(
    currentTimeout: Int,
    onTimeoutSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timeoutOptions = listOf(5, 15, 30, 60, 120, 240)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Session Timeout") },
        text = {
            Column {
                timeoutOptions.forEach { timeout ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = timeout == currentTimeout,
                            onClick = { onTimeoutSelected(timeout) }
                        )
                        Text(
                            text = "$timeout minute${if (timeout > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHostnameDialog(
    allowedHostnames: List<String>,
    onHostnameAdded: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hostname by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Allowed Hostname") },
        text = {
            OutlinedTextField(
                value = hostname,
                onValueChange = { hostname = it },
                label = { Text("Hostname") },
                placeholder = { Text("example.com") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (hostname.isNotBlank() && !allowedHostnames.contains(hostname)) {
                        onHostnameAdded(hostname)
                    }
                },
                enabled = hostname.isNotBlank() && !allowedHostnames.contains(hostname)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordStrengthDialog(
    onPasswordTest: (String) -> com.gf.mail.utils.PasswordStrength,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    val passwordStrength = remember(password) {
        if (password.isNotEmpty()) onPasswordTest(password) else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Password Strength Test") },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Test Password") },
                    singleLine = true
                )

                passwordStrength?.let { strength ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Strength: ${strength.level.name.lowercase().replaceFirstChar {
                            it.uppercaseChar()
                        }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (strength.level) {
                            PasswordStrengthLevel.VERY_WEAK -> Color.Red
                            PasswordStrengthLevel.WEAK -> Color(0xFFFF9800)
                            PasswordStrengthLevel.MEDIUM -> Color(0xFFFFEB3B)
                            PasswordStrengthLevel.STRONG -> Color.Green
                        }
                    )

                    if (strength.missingRequirements.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Missing requirements:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        strength.missingRequirements.forEach { requirement ->
                            Text(
                                text = "• $requirement",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun AuthenticationDialog(
    biometricAvailability: BiometricAvailability,
    isAuthenticating: Boolean,
    onBiometricAuth: () -> Unit,
    onDeviceCredentialAuth: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (!isAuthenticating) onDismiss else { {} },
        title = { Text("Authentication Required") },
        text = {
            Column {
                Text("Choose your authentication method:")
                Spacer(modifier = Modifier.height(16.dp))

                if (biometricAvailability == BiometricAvailability.AVAILABLE) {
                    Button(
                        onClick = onBiometricAuth,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAuthenticating
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Biometric")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onDeviceCredentialAuth,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAuthenticating
                ) {
                    Icon(Icons.Default.Password, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use PIN/Pattern/Password")
                }
            }
        },
        confirmButton = {
            if (!isAuthenticating) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
