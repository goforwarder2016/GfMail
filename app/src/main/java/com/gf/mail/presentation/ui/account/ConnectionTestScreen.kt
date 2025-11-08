package com.gf.mail.presentation.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gf.mail.GfmailApplication
import com.gf.mail.R
import com.gf.mail.domain.model.*
import com.gf.mail.presentation.viewmodel.ConnectionTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionTestScreen(
    accountId: String,
    onNavigateBack: () -> Unit,
    actualViewModel: ConnectionTestViewModel? = null
) {
    val context = LocalContext.current
    val dependencies = (context.applicationContext as GfmailApplication).dependencies
    val actualViewModel = actualViewModel ?: dependencies.createConnectionTestViewModel()
    val uiState by actualViewModel.uiState.collectAsState()

    LaunchedEffect(accountId) {
        actualViewModel.loadAccount(accountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.connection_test)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Info Card
            uiState.account?.let { account ->
                AccountInfoCard(account = account)
            }

            // Test Results
            uiState.testResult?.let { result ->
                ConnectionTestResultCard(result = result)
            }

            // Error Message
            uiState.error?.let { error ->
                ErrorCard(error = error)
            }

            // Password Input Section
            if (uiState.showPasswordField) {
                PasswordInputCard(
                    password = uiState.enteredPassword ?: "",
                    onPasswordChange = actualViewModel::updatePassword,
                    isLoading = uiState.isLoading
                )
            }

            // Test Actions
            TestActionsSection(
                isLoading = uiState.isLoading,
                onTestConnection = { actualViewModel.testConnection() },
                onTestImap = { actualViewModel.testImapOnly() },
                onTestSmtp = { actualViewModel.testSmtpOnly() }
            )

            // Manual Settings Override
            if (uiState.showManualSettings) {
                ManualSettingsCard(
                    serverConfig = uiState.manualServerConfig,
                    onServerConfigChange = actualViewModel::updateManualServerConfig,
                    onTestManualSettings = actualViewModel::testManualSettings,
                    isLoading = uiState.isLoading
                )
            }

            // Toggle Manual Settings
            OutlinedButton(
                onClick = { actualViewModel.toggleManualSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (uiState.showManualSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (uiState.showManualSettings) {
                        stringResource(R.string.hide_manual_settings)
                    } else {
                        stringResource(R.string.show_manual_settings)
                    }
                )
            }
        }
    }
}

@Composable
private fun AccountInfoCard(account: Account) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.account_information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.email_address))
                Text(account.email, fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.provider))
                Text(account.provider.name, fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.authentication))
                Text(account.authInfo.type.name, fontWeight = FontWeight.Medium)
            }

            // Server Configuration Summary
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.server_configuration),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            account.serverConfig.imapHost?.let { host ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("IMAP")
                    Text("$host:${account.serverConfig.imapPort}")
                }
            }

            account.serverConfig.smtpHost?.let { host ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SMTP")
                    Text("$host:${account.serverConfig.smtpPort}")
                }
            }
        }
    }
}

@Composable
private fun ConnectionTestResultCard(result: ConnectionTestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isSuccessful) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
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
                    imageVector = if (result.isSuccessful) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (result.isSuccessful) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Text(
                    text = if (result.isSuccessful) {
                        stringResource(R.string.connection_successful)
                    } else {
                        stringResource(R.string.connection_failed)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (result.isSuccessful) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }

            // IMAP Result
            result.imapResult?.let { imapResult ->
                ConnectionResultItem(
                    title = "IMAP",
                    result = imapResult
                )
            }

            // SMTP Result
            result.smtpResult?.let { smtpResult ->
                ConnectionResultItem(
                    title = "SMTP",
                    result = smtpResult
                )
            }

            // Overall Error Message
            result.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ConnectionResultItem(
    title: String,
    result: ConnectionResult
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (result.isSuccessful) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (result.isSuccessful) Color.Green else Color.Red,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$title - ${result.host}:${result.port}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        result.responseMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 24.dp)
            )
        }

        result.errorMessage?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red,
                modifier = Modifier.padding(start = 24.dp)
            )
        }

        if (result.testDurationMs > 0) {
            Text(
                text = stringResource(R.string.test_duration, result.testDurationMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun TestActionsSection(
    isLoading: Boolean,
    onTestConnection: () -> Unit,
    onTestImap: () -> Unit,
    onTestSmtp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.connection_tests),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Test All Connections
            Button(
                onClick = onTestConnection,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.test_all_connections))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Test IMAP Only
                OutlinedButton(
                    onClick = onTestImap,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("IMAP")
                }

                // Test SMTP Only
                OutlinedButton(
                    onClick = onTestSmtp,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMTP")
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ManualSettingsCard(
    serverConfig: ServerConfiguration,
    onServerConfigChange: (ServerConfiguration) -> Unit,
    onTestManualSettings: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.manual_server_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // IMAP Settings
            Text(
                text = "IMAP Settings",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = serverConfig.imapHost ?: "",
                onValueChange = { onServerConfigChange(serverConfig.copy(imapHost = it)) },
                label = { Text("IMAP Host") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serverConfig.imapPort.toString(),
                onValueChange = {
                    val port = it.toIntOrNull() ?: 993
                    onServerConfigChange(serverConfig.copy(imapPort = port))
                },
                label = { Text("IMAP Port") },
                modifier = Modifier.fillMaxWidth()
            )

            // SMTP Settings
            Text(
                text = "SMTP Settings",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = serverConfig.smtpHost ?: "",
                onValueChange = { onServerConfigChange(serverConfig.copy(smtpHost = it)) },
                label = { Text("SMTP Host") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serverConfig.smtpPort.toString(),
                onValueChange = {
                    val port = it.toIntOrNull() ?: 587
                    onServerConfigChange(serverConfig.copy(smtpPort = port))
                },
                label = { Text("SMTP Port") },
                modifier = Modifier.fillMaxWidth()
            )

            // Test Manual Settings
            Button(
                onClick = onTestManualSettings,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.test_manual_settings))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PasswordInputCard(
    password: String,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Password Verification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enter your account password to test the connection:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                placeholder = { Text("Enter your email password") },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            )
            
            Text(
                text = "Password validation: Password must be at least 3 characters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
