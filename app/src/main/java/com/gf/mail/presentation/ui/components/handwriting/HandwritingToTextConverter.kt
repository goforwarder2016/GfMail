package com.gf.mail.presentation.ui.components.handwriting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gf.mail.GfmailApplication
import com.gf.mail.domain.model.HandwritingData
import com.gf.mail.presentation.viewmodel.HandwritingRecognitionViewModel

/**
 * Component for converting handwriting to text
 */
@Composable
fun HandwritingToTextConverter(
    modifier: Modifier = Modifier,
    onTextConverted: (String) -> Unit = {},
    initialText: String = ""
) {
    var handwritingData by remember { mutableStateOf<HandwritingData?>(null) }
    var convertedText by remember { mutableStateOf(initialText) }
    var showRecognitionDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Handwriting input area
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Draw your text:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                HandwritingView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    strokeColor = Color.Black,
                    strokeWidth = 3f,
                    backgroundColor = Color.White,
                    onHandwritingChanged = { data ->
                        handwritingData = data
                    },
                    showControls = true
                )
            }
        }
        
        // Convert button
        Button(
            onClick = {
                if (handwritingData != null) {
                    showRecognitionDialog = true
                }
            },
            enabled = handwritingData != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.TextFields, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Convert to Text")
        }
        
        // Converted text display
        if (convertedText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Converted Text:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = { convertedText = "" }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    
                    Text(
                        text = convertedText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onTextConverted(convertedText) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Use Text")
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                // Copy to clipboard functionality would go here
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }
                    }
                }
            }
        }
    }
    
    // Recognition dialog
    if (showRecognitionDialog) {
        HandwritingRecognitionDialog(
            handwritingData = handwritingData!!,
            onDismiss = { showRecognitionDialog = false },
            onTextRecognized = { text ->
                convertedText = text
                onTextConverted(text)
                showRecognitionDialog = false
            }
        )
    }
}

/**
 * Dialog for handwriting recognition
 */
@Composable
private fun HandwritingRecognitionDialog(
    handwritingData: HandwritingData,
    onDismiss: () -> Unit,
    onTextRecognized: (String) -> Unit
) {
    val context = LocalContext.current
    val dependencies = (context.applicationContext as GfmailApplication).dependencies
    val viewModel: HandwritingRecognitionViewModel = dependencies.createHandwritingRecognitionViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Start recognition when dialog opens
    LaunchedEffect(handwritingData) {
        viewModel.recognizeHandwriting(handwritingData)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recognizing Handwriting") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recognizing your handwriting...")
                    }
                }
                
                uiState.recognitionResult?.let { result ->
                    Text(
                        text = "Recognized text:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = result.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (result.confidence < 0.8f) {
                        Text(
                            text = "Confidence: ${(result.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (result.alternatives.isNotEmpty()) {
                        Text(
                            text = "Alternatives:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        result.alternatives.forEach { alternative ->
                            Text(
                                text = "• $alternative",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                uiState.error?.let { error ->
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    uiState.recognitionResult?.let { result ->
                        onTextRecognized(result.text)
                    }
                },
                enabled = uiState.recognitionResult != null && !uiState.isLoading
            ) {
                Text("Use Text")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
