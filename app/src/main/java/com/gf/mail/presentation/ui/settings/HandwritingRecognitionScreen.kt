package com.gf.mail.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.gf.mail.presentation.ui.components.handwriting.HandwritingView
import com.gf.mail.presentation.viewmodel.HandwritingRecognitionViewModel

/**
 * Screen for testing handwriting recognition functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandwritingRecognitionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dependencies = (context.applicationContext as GfmailApplication).dependencies
    val viewModel: HandwritingRecognitionViewModel = dependencies.createHandwritingRecognitionViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var handwritingData by remember { mutableStateOf<HandwritingData?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Handwriting Recognition") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Handwriting input area
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Draw your handwriting:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    HandwritingView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
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
            
            // Recognition controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        handwritingData?.let { data ->
                            viewModel.recognizeHandwriting(data)
                        }
                    },
                    enabled = handwritingData != null && !uiState.isRecognizing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Recognize Text")
                }
                
                OutlinedButton(
                    onClick = {
                        handwritingData = null
                        viewModel.clearRecognition()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
            
            // Recognition results
            if (uiState.recognitionResult != null) {
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
                        Text(
                            text = "Recognition Result:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val result = uiState.recognitionResult
                        if (result != null) {
                            Text(
                                text = result.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Text(
                                text = "Confidence: ${(result.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
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
                    }
                }
            }
            
            // Error display
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Loading indicator
            if (uiState.isRecognizing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recognizing...")
                }
            }
        }
    }
}
