package com.gf.mail.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gf.mail.GfmailApplication
import com.gf.mail.domain.model.EmailSignature
import com.gf.mail.presentation.ui.components.showErrorMessage
import com.gf.mail.presentation.ui.components.showSuccessMessage
import com.gf.mail.presentation.viewmodel.SignatureManagementViewModel
import com.gf.mail.presentation.viewmodel.SignatureType

/**
 * Screen for managing email signatures
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureManagementScreen(
    onNavigateBack: () -> Unit,
    accountId: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dependencies = (context.applicationContext as GfmailApplication).dependencies
    val viewModel: SignatureManagementViewModel = dependencies.createSignatureManagementViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load signatures for account or all signatures
    LaunchedEffect(accountId) {
        if (accountId != null) {
            viewModel.loadSignaturesForAccount(accountId)
        }
    }

    // Handle messages and errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            showErrorMessage(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            showSuccessMessage(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (accountId != null) "Account Signatures" else "Email Signatures"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.startCreatingSignature() }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Signature")
                    }

                    if (uiState.signatures.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                // TODO: Show search/filter options
                            }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.signatures.isEmpty() && !uiState.isLoading) {
                FloatingActionButton(
                    onClick = { viewModel.startCreatingSignature() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Signature")
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
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.signatures.isEmpty() -> {
                    EmptySignaturesState(
                        onCreateSignature = { viewModel.startCreatingSignature() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Show default signature first if exists
                        uiState.defaultSignature?.let { defaultSignature ->
                            item {
                                SignatureCard(
                                    signature = defaultSignature,
                                    isDefault = true,
                                    onEdit = { viewModel.startEditingSignature(defaultSignature) },
                                    onDelete = { viewModel.deleteSignature(defaultSignature.id) },
                                    onSetDefault = { /* Already default */ },
                                    onDuplicate = {
                                        viewModel.duplicateSignature(
                                            defaultSignature.id,
                                            "${defaultSignature.name} (Copy)"
                                        )
                                    }
                                )
                            }
                        }

                        // Show other signatures
                        val otherSignatures = uiState.signatures.filter { !it.isDefault }
                        items(otherSignatures) { signature ->
                            SignatureCard(
                                signature = signature,
                                isDefault = false,
                                onEdit = { viewModel.startEditingSignature(signature) },
                                onDelete = { viewModel.deleteSignature(signature.id) },
                                onSetDefault = {
                                    viewModel.setDefaultSignature(signature.id, accountId)
                                },
                                onDuplicate = {
                                    viewModel.duplicateSignature(
                                        signature.id,
                                        "${signature.name} (Copy)"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create signature dialog
    if (uiState.isCreatingSignature) {
        CreateSignatureDialog(
            uiState = uiState,
            onNameChange = viewModel::updateNewSignatureName,
            onContentChange = viewModel::updateNewSignatureContent,
            onTypeChange = viewModel::setSignatureType,
            onHtmlToggle = viewModel::toggleNewSignatureHtml,
            onSave = { setAsDefault ->
                viewModel.saveNewSignature(accountId, setAsDefault)
            },
            onCancel = viewModel::cancelCreatingSignature
        )
    }

    // Edit signature dialog
    if (uiState.editingSignature != null) {
        EditSignatureDialog(
            uiState = uiState,
            onNameChange = viewModel::updateEditingSignatureName,
            onContentChange = viewModel::updateEditingSignatureContent,
            onHtmlToggle = viewModel::toggleEditingSignatureHtml,
            onSave = viewModel::saveEditingSignature,
            onCancel = viewModel::cancelEditingSignature
        )
    }
}

@Composable
private fun EmptySignaturesState(
    onCreateSignature: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.Edit,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "No Signatures",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Create your first email signature to personalize your messages",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onCreateSignature
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Signature")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignatureCard(
    signature: EmailSignature,
    isDefault: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with name and default badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = signature.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (isDefault) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Default") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                // Signature type indicator
                if (signature.hasHandwritingData) {
                    Icon(
                        Icons.Default.Gesture,
                        contentDescription = "Handwriting signature",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (signature.isHtml) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = "HTML signature",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Content preview
            if (signature.hasHandwritingData) {
                Text(
                    text = "Handwriting signature (${signature.handwritingWidth}x${signature.handwritingHeight})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = signature.content.take(100) + if (signature.content.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Account info
            if (signature.accountId != null) {
                Text(
                    text = "Account-specific signature",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Global signature",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                if (!isDefault) {
                    TextButton(onClick = onSetDefault) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set Default")
                    }
                }

                TextButton(onClick = onDuplicate) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duplicate")
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSignatureDialog(
    uiState: com.gf.mail.presentation.viewmodel.SignatureManagementUiState,
    onNameChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onTypeChange: (SignatureType) -> Unit,
    onHtmlToggle: (Boolean) -> Unit,
    onSave: (Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var setAsDefault by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Create Signature") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Signature name
                OutlinedTextField(
                    value = uiState.newSignatureName,
                    onValueChange = onNameChange,
                    label = { Text("Signature Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Signature type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.newSignatureType == SignatureType.TEXT,
                        onClick = { onTypeChange(SignatureType.TEXT) },
                        label = { Text("Text") },
                        leadingIcon = {
                            Icon(Icons.Default.TextFields, contentDescription = null)
                        }
                    )

                    FilterChip(
                        selected = uiState.newSignatureType == SignatureType.HANDWRITING,
                        onClick = { onTypeChange(SignatureType.HANDWRITING) },
                        label = { Text("Handwriting") },
                        leadingIcon = {
                            Icon(Icons.Default.Gesture, contentDescription = null)
                        }
                    )
                }

                // Content based on type
                when (uiState.newSignatureType) {
                    SignatureType.TEXT -> {
                        OutlinedTextField(
                            value = uiState.newSignatureContent,
                            onValueChange = onContentChange,
                            label = { Text("Signature Content") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 6,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.newSignatureIsHtml,
                                onCheckedChange = onHtmlToggle
                            )
                            Text("Use HTML formatting")
                        }
                    }

                    SignatureType.HANDWRITING -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Gesture,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "Handwriting feature coming soon",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Set as default option
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = setAsDefault,
                        onCheckedChange = { setAsDefault = it }
                    )
                    Text("Set as default signature")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(setAsDefault) },
                enabled = uiState.canSaveNewSignature
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSignatureDialog(
    uiState: com.gf.mail.presentation.viewmodel.SignatureManagementUiState,
    onNameChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onHtmlToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Edit Signature") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Signature name
                OutlinedTextField(
                    value = uiState.editingSignatureName,
                    onValueChange = onNameChange,
                    label = { Text("Signature Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Only show content editing for text signatures
                if (!uiState.editingSignature?.hasHandwritingData!!) {
                    OutlinedTextField(
                        value = uiState.editingSignatureContent,
                        onValueChange = onContentChange,
                        label = { Text("Signature Content") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 6
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.editingSignatureIsHtml,
                            onCheckedChange = onHtmlToggle
                        )
                        Text("Use HTML formatting")
                    }
                } else {
                    Text(
                        text = "Handwriting signatures can only have their name changed. To modify the signature content, create a new handwriting signature.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = uiState.canSaveEditingSignature ||
                    (uiState.editingSignature?.hasHandwritingData == true && uiState.editingSignatureName.isNotBlank())
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}
