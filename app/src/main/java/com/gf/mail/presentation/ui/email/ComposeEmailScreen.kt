package com.gf.mail.presentation.ui.email

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gf.mail.domain.model.EmailPriority
import com.gf.mail.domain.model.EmailSignature
import com.gf.mail.presentation.ui.attachment.AttachmentPickerDialog
import com.gf.mail.presentation.ui.attachment.AttachmentPreviewCard
import com.gf.mail.presentation.ui.components.showErrorMessage
import com.gf.mail.presentation.ui.components.showSuccessMessage
import com.gf.mail.presentation.viewmodel.ComposeEmailViewModel
import com.gf.mail.domain.model.ComposeMode
import kotlinx.coroutines.launch

/**
 * Email composition screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeEmailScreen(
    viewModel: ComposeEmailViewModel,
    mode: ComposeMode = ComposeMode.NEW,
    emailId: String? = null,
    replyAll: Boolean = false,
    onSentSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Attachment picker state
    var showAttachmentPicker by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addAttachments(uris)
        }
    }

    // Initialize compose when screen opens
    LaunchedEffect(mode, emailId, replyAll) {
        viewModel.initializeCompose(mode, emailId, replyAll)
    }

    // Handle success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("sent") == true) {
            onSentSuccess()
        }
    }

    Scaffold(
        topBar = {
            ComposeTopBar(
                onBackClick = onBackClick,
                onSendClick = { viewModel.sendEmail() },
                onSaveDraftClick = { viewModel.saveDraft() },
                onAttachClick = {
                    showAttachmentPicker = true
                },
                isSending = uiState.isSending,
                isSavingDraft = uiState.isSavingDraft,
                hasContent = uiState.toAddresses.isNotEmpty() ||
                    uiState.subject.isNotBlank() ||
                    uiState.body.isNotBlank()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Recipients section
                    RecipientsSection(
                        toAddresses = uiState.toAddresses,
                        ccAddresses = uiState.ccAddresses,
                        bccAddresses = uiState.bccAddresses,
                        onToChanged = viewModel::updateToAddresses,
                        onCcChanged = viewModel::updateCcAddresses,
                        onBccChanged = viewModel::updateBccAddresses,
                        recipientSuggestions = uiState.recipientSuggestions,
                        showSuggestions = uiState.showRecipientSuggestions,
                        onQuerySuggestions = viewModel::getRecipientSuggestions,
                        onHideSuggestions = viewModel::hideRecipientSuggestions,
                        validationErrors = uiState.validationErrors
                    )

                    // Subject
                    SubjectField(
                        subject = uiState.subject,
                        onSubjectChanged = viewModel::updateSubject,
                        validationError = uiState.validationErrors["subject"]
                    )

                    // Priority selector
                    PrioritySelector(
                        priority = uiState.priority,
                        onPriorityChanged = viewModel::updatePriority
                    )

                    // Attachments
                    if (uiState.attachments.isNotEmpty()) {
                        AttachmentPreviewCard(
                            attachments = uiState.attachments,
                            onRemoveAttachment = viewModel::removeAttachment,
                            onViewAll = { showAttachmentPicker = true }
                        )
                    }

                    // Signature selector
                    if (uiState.availableSignatures.isNotEmpty()) {
                        SignatureSelector(
                            signatures = uiState.availableSignatures,
                            selectedSignature = uiState.selectedSignature ?: EmailSignature(
                                id = "",
                                name = "",
                                content = ""
                            ),
                            onSignatureSelected = { signature -> 
                                signature?.let { viewModel.selectSignature(it) }
                            },
                            showSelector = uiState.showSignatureSelector,
                            onToggleSelector = { viewModel.toggleSignatureSelector() }
                        )
                    }

                    // Body content
                    BodyEditor(
                        bodyText = uiState.body,
                        onBodyChanged = viewModel::updateBodyText,
                        modifier = Modifier.weight(1f)
                    )

                    // Auto-save indicator
                    if (uiState.lastAutoSaveTime > 0) {
                        AutoSaveIndicator(
                            lastSaveTime = uiState.lastAutoSaveTime,
                            isAutoSaving = uiState.isSavingDraft
                        )
                    }
                }
            }
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            scope.launch {
                showErrorMessage(error)
                viewModel.clearError()
            }
        }
    }

    // Success handling
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            scope.launch {
                showSuccessMessage(message)
                viewModel.clearSuccess()
            }
        }
    }

    // Attachment picker dialog
    AttachmentPickerDialog(
        isVisible = showAttachmentPicker,
        currentAttachments = uiState.attachments,
        onDismiss = { showAttachmentPicker = false },
        onAttachmentsSelected = { uris ->
            viewModel.addAttachments(uris)
            showAttachmentPicker = false
        },
        onAttachmentRemove = { attachmentId ->
            viewModel.removeAttachment(attachmentId)
        }
    )
}

/**
 * Top bar for compose screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeTopBar(
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onSaveDraftClick: () -> Unit,
    onAttachClick: () -> Unit,
    isSending: Boolean,
    isSavingDraft: Boolean,
    hasContent: Boolean,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text("Compose") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onAttachClick) {
                Icon(Icons.Default.Add, contentDescription = "Attach file")
            }

            if (hasContent) {
                IconButton(
                    onClick = onSaveDraftClick,
                    enabled = !isSavingDraft
                ) {
                    if (isSavingDraft) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "Save draft")
                    }
                }
            }

            IconButton(
                onClick = onSendClick,
                enabled = !isSending && hasContent
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Recipients section with To, CC, BCC fields
 */
@Composable
private fun RecipientsSection(
    toAddresses: List<String>,
    ccAddresses: List<String>,
    bccAddresses: List<String>,
    onToChanged: (List<String>) -> Unit,
    onCcChanged: (List<String>) -> Unit,
    onBccChanged: (List<String>) -> Unit,
    recipientSuggestions: List<String>,
    showSuggestions: Boolean,
    onQuerySuggestions: (String) -> Unit,
    onHideSuggestions: () -> Unit,
    validationErrors: Map<String, String>,
    modifier: Modifier = Modifier
) {
    var showCcBcc by remember {
        mutableStateOf(ccAddresses.isNotEmpty() || bccAddresses.isNotEmpty())
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // To field
        RecipientField(
            label = "To",
            recipients = toAddresses,
            onRecipientsChanged = onToChanged,
            onQuerySuggestions = onQuerySuggestions,
            isError = validationErrors.containsKey("to") || validationErrors.containsKey(
                "addresses"
            ),
            errorMessage = validationErrors["to"] ?: validationErrors["addresses"]
        )

        // CC/BCC toggle
        if (!showCcBcc) {
            TextButton(
                onClick = { showCcBcc = true }
            ) {
                Text("Cc/Bcc")
            }
        }

        // CC field
        if (showCcBcc) {
            RecipientField(
                label = "Cc",
                recipients = ccAddresses,
                onRecipientsChanged = onCcChanged,
                onQuerySuggestions = onQuerySuggestions
            )
        }

        // BCC field
        if (showCcBcc) {
            RecipientField(
                label = "Bcc",
                recipients = bccAddresses,
                onRecipientsChanged = onBccChanged,
                onQuerySuggestions = onQuerySuggestions
            )
        }

        // Suggestions dropdown
        if (showSuggestions && recipientSuggestions.isNotEmpty()) {
            RecipientSuggestions(
                suggestions = recipientSuggestions,
                onSuggestionSelected = { suggestion ->
                    // Add to To field for simplicity
                    onToChanged(toAddresses + suggestion)
                    onHideSuggestions()
                },
                onDismiss = onHideSuggestions
            )
        }
    }
}

/**
 * Individual recipient field
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RecipientField(
    label: String,
    recipients: List<String>,
    onRecipientsChanged: (List<String>) -> Unit,
    onQuerySuggestions: (String) -> Unit = {},
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { text ->
                inputText = text
                onQuerySuggestions(text)

                // Add email when comma or semicolon is typed
                if (text.contains(",") || text.contains(";")) {
                    val newEmail = text.substringBefore(",").substringBefore(";").trim()
                    if (newEmail.isNotBlank() && newEmail.contains("@")) {
                        onRecipientsChanged(recipients + newEmail)
                        inputText = ""
                    }
                }
            },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = isError,
            supportingText = {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Display current recipients as chips
        if (recipients.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(recipients) { recipient ->
                    RecipientChip(
                        email = recipient,
                        onRemove = {
                            onRecipientsChanged(recipients - recipient)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Recipient chip component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipientChip(
    email: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { /* Do nothing */ },
        label = {
            Text(
                text = email,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(12.dp)
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Recipient suggestions dropdown
 */
@Composable
private fun RecipientSuggestions(
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            suggestions.take(5).forEach { suggestion ->
                TextButton(
                    onClick = { onSuggestionSelected(suggestion) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = suggestion,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Subject field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectField(
    subject: String,
    onSubjectChanged: (String) -> Unit,
    validationError: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = subject,
        onValueChange = onSubjectChanged,
        label = { Text("Subject") },
        singleLine = true,
        isError = validationError != null,
        supportingText = {
            if (validationError != null) {
                Text(
                    text = validationError,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Priority selector
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrioritySelector(
    priority: EmailPriority,
    onPriorityChanged: (EmailPriority) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Priority:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )

        FilterChip(
            selected = priority != EmailPriority.NORMAL,
            onClick = { expanded = true },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (priority) {
                        EmailPriority.HIGH -> {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Red
                            )
                            Text("High")
                        }
                        EmailPriority.LOW -> {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Blue
                            )
                            Text("Low")
                        }
                        EmailPriority.NORMAL -> {
                            Text("Normal")
                        }
                    }
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("High Priority") },
                onClick = {
                    onPriorityChanged(EmailPriority.HIGH)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Normal Priority") },
                onClick = {
                    onPriorityChanged(EmailPriority.NORMAL)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Low Priority") },
                onClick = {
                    onPriorityChanged(EmailPriority.LOW)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Blue
                    )
                }
            )
        }
    }
}

/**
 * Attachments section
 */
@Composable
private fun AttachmentsSection(
    attachments: List<com.gf.mail.domain.model.EmailAttachment>,
    onRemoveAttachment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Attachments",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            attachments.forEach { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    onRemove = { onRemoveAttachment(attachment.id) }
                )
            }
        }
    }
}

/**
 * Individual attachment item
 */
@Composable
private fun AttachmentItem(
    attachment: com.gf.mail.domain.model.EmailAttachment,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatFileSize(attachment.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove attachment",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Body editor field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BodyEditor(
    bodyText: String,
    onBodyChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = bodyText,
        onValueChange = onBodyChanged,
        label = { Text("Message") },
        placeholder = { Text("Compose your message...") },
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Auto-save indicator
 */
@Composable
private fun AutoSaveIndicator(
    lastSaveTime: Long,
    isAutoSaving: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAutoSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.dp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Saving...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val timeText = remember(lastSaveTime) {
                val minutes = (System.currentTimeMillis() - lastSaveTime) / 60000
                when {
                    minutes < 1 -> "Saved just now"
                    minutes < 60 -> "Saved ${minutes}m ago"
                    else -> "Saved ${minutes / 60}h ago"
                }
            }

            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Signature selector component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignatureSelector(
    signatures: List<EmailSignature>,
    selectedSignature: EmailSignature?,
    onSignatureSelected: (EmailSignature?) -> Unit,
    showSelector: Boolean,
    onToggleSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Signature",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(
                    onClick = onToggleSelector,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = selectedSignature?.name ?: "None",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Icon(
                        if (showSelector) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Signature selector
            if (showSelector) {
                Spacer(modifier = Modifier.height(8.dp))

                // Option to remove signature
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSignatureSelected(null) }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedSignature == null,
                        onClick = { onSignatureSelected(null) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No signature",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Available signatures
                signatures.forEach { signature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSignatureSelected(signature) }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSignature?.id == signature.id,
                            onClick = { onSignatureSelected(signature) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = signature.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = when (signature.type) {
                                    EmailSignature.SignatureType.TEXT -> "Text signature"
                                    EmailSignature.SignatureType.HTML -> "HTML signature"
                                    EmailSignature.SignatureType.HANDWRITTEN -> "Handwritten signature"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format file size for display
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
