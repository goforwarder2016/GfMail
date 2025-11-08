package com.gf.mail.presentation.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gf.mail.GfmailApplication
import com.gf.mail.domain.usecase.ManagePerformanceOptimizationUseCase
import com.gf.mail.presentation.viewmodel.PerformanceOptimizationViewModel
import com.gf.mail.domain.model.*
import com.gf.mail.data.performance.PerformanceMetrics
import com.gf.mail.utils.PerformanceUtils

/**
 * Format bytes for display
 */
private fun formatBytes(bytes: Float): String {
    return when {
        bytes < 1024 -> "${bytes.toInt()} B"
        bytes < 1024 * 1024 -> "${(bytes / 1024).toInt()} KB"
        bytes < 1024 * 1024 * 1024 -> "${(bytes / (1024 * 1024)).toInt()} MB"
        else -> "${(bytes / (1024 * 1024 * 1024)).toInt()} GB"
    }
}

/**
 * Performance optimization settings screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dependencies = (context.applicationContext as GfmailApplication).dependencies
    val viewModel: PerformanceOptimizationViewModel = dependencies.createPerformanceOptimizationViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val performanceMetrics by viewModel.performanceMetrics.collectAsStateWithLifecycle()

    // Local states
    var showPerformanceModeDialog by remember { mutableStateOf(false) }
    var showImageQualityDialog by remember { mutableStateOf(false) }
    var showSyncFrequencyDialog by remember { mutableStateOf(false) }
    var showOptimizationRecommendations by remember { mutableStateOf(false) }
    var showAutoOptimizeDialog by remember { mutableStateOf(false) }

    // Load recommendations when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadOptimizationRecommendations()
        viewModel.getBatteryOptimizationSuggestions()
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
                title = { Text("Performance Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = context.getString(com.gf.mail.R.string.cd_back_button))
                    }
                },
                actions = {
                    // Auto optimize button
                    IconButton(
                        onClick = { showAutoOptimizeDialog = true },
                        enabled = !uiState.isOptimizing
                    ) {
                        if (uiState.isOptimizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Auto Optimize",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
            // Performance Overview Card
            item {
                PerformanceOverviewCard(
                    summary = uiState.performanceSummary,
                    metrics = performanceMetrics ?: com.gf.mail.data.performance.PerformanceMetrics(),
                    onRefresh = { viewModel.loadPerformanceSummary() }
                )
            }

            // Performance Mode Section
            item {
                PerformanceSection(
                    title = "Performance Mode",
                    icon = Icons.Default.Speed
                ) {
                    PerformanceModeCard(
                        currentMode = uiState.settings.performanceMode,
                        onClick = { showPerformanceModeDialog = true }
                    )

                    SwitchSettingsRow(
                        title = "Auto Optimization",
                        subtitle = "Automatically adjust settings based on usage patterns",
                        icon = Icons.Default.AutoMode,
                        checked = uiState.settings.autoOptimization,
                        onCheckedChange = viewModel::toggleAutoOptimization
                    )

                    SwitchSettingsRow(
                        title = "Battery Saver Mode",
                        subtitle = "Optimize for maximum battery life",
                        icon = Icons.Default.BatteryAlert,
                        checked = uiState.settings.batterySaverMode,
                        onCheckedChange = viewModel::toggleBatterySaverMode
                    )
                }
            }

            // UI Performance Section
            item {
                PerformanceSection(
                    title = "UI Performance",
                    icon = Icons.Default.Smartphone
                ) {
                    SwitchSettingsRow(
                        title = "Reduce Animations",
                        subtitle = "Minimize animations for better performance",
                        icon = Icons.Default.MotionPhotosOff,
                        checked = uiState.settings.reduceAnimations,
                        onCheckedChange = viewModel::toggleReduceAnimations
                    )

                    SettingsRow(
                        title = "Image Quality",
                        subtitle = "${uiState.settings.imageQuality.name.lowercase().replaceFirstChar {
                            it.uppercaseChar()
                        }} quality images",
                        icon = Icons.Default.Image,
                        onClick = { showImageQualityDialog = true }
                    )
                }
            }

            // Data & Sync Section
            item {
                PerformanceSection(
                    title = "Data & Sync",
                    icon = Icons.Default.DataUsage
                ) {
                    SettingsRow(
                        title = "Sync Frequency",
                        subtitle = getSyncFrequencyDescription(uiState.settings.syncFrequency),
                        icon = Icons.Default.Sync,
                        onClick = { showSyncFrequencyDialog = true }
                    )
                }
            }

            // Memory & Storage Section
            item {
                PerformanceSection(
                    title = "Memory & Storage",
                    icon = Icons.Default.Memory
                ) {
                    SwitchSettingsRow(
                        title = "Memory Optimization",
                        subtitle = "Actively manage memory usage",
                        icon = Icons.Default.CleaningServices,
                        checked = uiState.settings.memoryOptimization,
                        onCheckedChange = viewModel::toggleMemoryOptimization
                    )

                    SwitchSettingsRow(
                        title = "Startup Optimization",
                        subtitle = "Optimize app startup time",
                        icon = Icons.Default.RocketLaunch,
                        checked = uiState.settings.startupOptimization,
                        onCheckedChange = viewModel::toggleStartupOptimization
                    )
                }
            }

            // Recommendations Section
            if (uiState.recommendations.isNotEmpty()) {
                item {
                    PerformanceSection(
                        title = context.getString(com.gf.mail.R.string.performance_recommendations),
                        icon = Icons.Default.Lightbulb
                    ) {
                        uiState.recommendations.forEach { recommendation ->
                            RecommendationCard(
                                recommendation = recommendation,
                                actionsToTake = uiState.actionsToTake,
                                onApply = {
                                    // TODO: Apply specific recommendation
                                }
                            )
                        }
                    }
                }
            }

            // Battery Optimization Suggestions
            uiState.batteryOptimizationSuggestions?.let { suggestions ->
                if (suggestions.isNotEmpty()) {
                    item {
                        BatteryOptimizationCard(
                            suggestions = com.gf.mail.domain.model.BatteryOptimizationResult(
                                suggestions = suggestions,
                                actionsToTake = uiState.actionsToTake,
                                estimatedSavings = 0.0f
                            ),
                            onApplyAll = {
                                // TODO: Apply battery optimization suggestions
                            }
                        )
                    }
                }
            }

            // Advanced Settings Section
            item {
                PerformanceSection(
                    title = context.getString(com.gf.mail.R.string.performance_settings),
                    icon = Icons.Default.Tune
                ) {
                    SettingsRow(
                        title = "Export Settings",
                        subtitle = "Save performance settings to file",
                        icon = Icons.Default.FileUpload,
                        onClick = { viewModel.exportPerformanceSettings() }
                    )

                    SettingsRow(
                        title = "Import Settings",
                        subtitle = "Load performance settings from file",
                        icon = Icons.Default.FileDownload,
                        onClick = {
                            // TODO: Show file picker for import
                        }
                    )

                    SettingsRow(
                        title = "Reset to Defaults",
                        subtitle = "Restore all performance settings to defaults",
                        icon = Icons.Default.RestoreFromTrash,
                        onClick = { viewModel.resetToDefaults() }
                    )
                }
            }
        }
    }

    // Performance Mode Selection Dialog
    if (showPerformanceModeDialog) {
        PerformanceModeSelectionDialog(
            currentMode = uiState.settings.performanceMode,
            onModeSelected = { mode ->
                viewModel.updatePerformanceMode(mode)
                showPerformanceModeDialog = false
            },
            onDismiss = { showPerformanceModeDialog = false }
        )
    }

    // Image Quality Selection Dialog
    if (showImageQualityDialog) {
        ImageQualitySelectionDialog(
            currentQuality = uiState.settings.imageQuality,
            onQualitySelected = { quality ->
                viewModel.updateImageQuality(quality)
                showImageQualityDialog = false
            },
            onDismiss = { showImageQualityDialog = false }
        )
    }

    // Sync Frequency Selection Dialog
    if (showSyncFrequencyDialog) {
        SyncFrequencySelectionDialog(
            currentFrequency = uiState.settings.syncFrequency,
            onFrequencySelected = { frequency ->
                viewModel.updateSyncFrequency(frequency)
                showSyncFrequencyDialog = false
            },
            onDismiss = { showSyncFrequencyDialog = false }
        )
    }

    // Auto Optimization Confirmation Dialog
    if (showAutoOptimizeDialog) {
        AlertDialog(
            onDismissRequest = { showAutoOptimizeDialog = false },
            title = { Text("Auto Optimize Performance") },
            text = {
                Text(
                    "This will automatically apply performance optimizations based on your device's current state and usage patterns. Continue?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.performAutoOptimization()
                        showAutoOptimizeDialog = false
                    }
                ) {
                    Text(context.getString(com.gf.mail.R.string.performance_optimize))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutoOptimizeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PerformanceOverviewCard(
    summary: PerformanceSummary?,
    metrics: com.gf.mail.data.performance.PerformanceMetrics,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    text = "Performance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            summary?.let { s ->
                Column {
                    PerformanceRatingBadge(rating = when {
                        s.overallScore >= 90f -> com.gf.mail.domain.model.PerformanceRating.EXCELLENT
                        s.overallScore >= 75f -> com.gf.mail.domain.model.PerformanceRating.GOOD
                        s.overallScore >= 60f -> com.gf.mail.domain.model.PerformanceRating.FAIR
                        s.overallScore >= 40f -> com.gf.mail.domain.model.PerformanceRating.POOR
                        else -> com.gf.mail.domain.model.PerformanceRating.CRITICAL
                    })

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PerformanceMetric(
                            title = "Startup",
                            value = "${s.overallScore}ms",
                            icon = Icons.Default.RocketLaunch,
                            isGood = s.overallScore > 80
                        )

                        PerformanceMetric(
                            title = "Memory",
                            value = formatBytes(s.memoryUsage),
                            icon = Icons.Default.Memory,
                            isGood = s.memoryUsage < 100 * 1024 * 1024 // 100MB
                        )

                        PerformanceMetric(
                            title = "Frame Rate",
                            value = "${String.format("%.1f", s.overallScore)} fps",
                            icon = Icons.Default.Speed,
                            isGood = s.overallScore >= 54f // 90% of 60fps
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetric(
    title: String,
    value: String,
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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun PerformanceRatingBadge(rating: PerformanceRating) {
    val (color, text) = when (rating) {
        PerformanceRating.EXCELLENT -> Color(0xFF4CAF50) to "Excellent"
        PerformanceRating.GOOD -> Color(0xFF8BC34A) to "Good"
        PerformanceRating.FAIR -> Color(0xFFFF9800) to "Fair"
        PerformanceRating.POOR -> Color(0xFFFF5722) to "Poor"
        PerformanceRating.CRITICAL -> Color(0xFFD32F2F) to "Critical"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PerformanceModeCard(
    currentMode: com.gf.mail.domain.model.PerformanceMode,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Current Mode: ${currentMode.name.lowercase().replaceFirstChar {
                        it.uppercaseChar()
                    }}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = getPerformanceModeDescription(currentMode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: com.gf.mail.domain.model.PerformanceRecommendation,
    actionsToTake: List<String>,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (recommendation.priority) {
                com.gf.mail.domain.model.PerformanceAlertSeverity.HIGH -> MaterialTheme.colorScheme.errorContainer
                com.gf.mail.domain.model.PerformanceAlertSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
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
                    Icons.Default.Warning,
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

            if (actionsToTake.isNotEmpty()) {
                Text(
                    text = "Suggested actions:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                actionsToTake.forEach { action ->
                    Text(
                        text = "• $action",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onApply) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun BatteryOptimizationCard(
    suggestions: com.gf.mail.domain.model.BatteryOptimizationResult,
    onApplyAll: () -> Unit
) {
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
                    Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Battery Optimization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Estimated ${(suggestions.estimatedSavings * 100).toInt()}% battery life improvement",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            suggestions.actionsToTake.forEach { action ->
                Text(
                    text = "• $action",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onApplyAll) {
                    Text("Apply All")
                }
            }
        }
    }
}

@Composable
private fun PerformanceSection(
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

// Helper functions for descriptions
private fun getPerformanceModeDescription(
    mode: com.gf.mail.domain.model.PerformanceMode
): String =
    when (mode) {
        com.gf.mail.domain.model.PerformanceMode.HIGH_PERFORMANCE -> "Maximum performance, higher battery usage"
        com.gf.mail.domain.model.PerformanceMode.BALANCED -> "Balanced performance and battery life"
        com.gf.mail.domain.model.PerformanceMode.BATTERY_SAVER -> "Maximum battery life, reduced performance"
    }

private fun getSyncFrequencyDescription(
    frequency: com.gf.mail.domain.model.SyncFrequency
): String =
    when (frequency) {
        com.gf.mail.domain.model.SyncFrequency.EVERY_15_MINUTES -> "Every 15 minutes (balanced)"
        com.gf.mail.domain.model.SyncFrequency.EVERY_30_MINUTES -> "Every 30 minutes (battery friendly)"
        com.gf.mail.domain.model.SyncFrequency.EVERY_HOUR -> "Every hour (maximum battery savings)"
        com.gf.mail.domain.model.SyncFrequency.MANUAL -> "Manual sync only"
        com.gf.mail.domain.model.SyncFrequency.EVERY_2_HOURS -> "Every 2 hours"
        com.gf.mail.domain.model.SyncFrequency.EVERY_4_HOURS -> "Every 4 hours"
        com.gf.mail.domain.model.SyncFrequency.EVERY_8_HOURS -> "Every 8 hours"
        com.gf.mail.domain.model.SyncFrequency.EVERY_12_HOURS -> "Every 12 hours"
        com.gf.mail.domain.model.SyncFrequency.DAILY -> "Daily"
    }

private fun getOptimizationActionDescription(action: com.gf.mail.domain.model.OptimizationAction): String =
    when (action) {
        com.gf.mail.domain.model.OptimizationAction.CLEAR_CACHE -> "Clear cache"
        com.gf.mail.domain.model.OptimizationAction.REDUCE_ANIMATIONS -> "Reduce animations"
        com.gf.mail.domain.model.OptimizationAction.DISABLE_BACKGROUND_SYNC -> "Disable background sync"
        com.gf.mail.domain.model.OptimizationAction.OPTIMIZE_IMAGES -> "Optimize images"
        com.gf.mail.domain.model.OptimizationAction.REDUCE_NOTIFICATIONS -> "Reduce notifications"
        com.gf.mail.domain.model.OptimizationAction.ENABLE_BATTERY_OPTIMIZATION -> "Enable battery optimization"
    }

// Dialog components would be implemented here similar to the existing pattern
// For brevity, I'm not including all dialog implementations but they would follow
// the same pattern as LanguageSelectionDialog and ThemeSelectionDialog

@Composable
private fun PerformanceModeSelectionDialog(
    currentMode: com.gf.mail.domain.model.PerformanceMode,
    onModeSelected: (com.gf.mail.domain.model.PerformanceMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Performance Mode") },
        text = {
            Column {
                com.gf.mail.domain.model.PerformanceMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = { onModeSelected(mode) }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = mode.name.lowercase().replaceFirstChar {
                                    it.uppercaseChar()
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = getPerformanceModeDescription(mode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ImageQualitySelectionDialog(
    currentQuality: com.gf.mail.domain.model.ImageQuality,
    onQualitySelected: (com.gf.mail.domain.model.ImageQuality) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Image Quality") },
        text = {
            Column {
                com.gf.mail.domain.model.ImageQuality.values().forEach { quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = quality == currentQuality,
                            onClick = { onQualitySelected(quality) }
                        )
                        Text(
                            text = quality.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SyncFrequencySelectionDialog(
    currentFrequency: com.gf.mail.domain.model.SyncFrequency,
    onFrequencySelected: (com.gf.mail.domain.model.SyncFrequency) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Sync Frequency") },
        text = {
            Column {
                com.gf.mail.domain.model.SyncFrequency.values().forEach { frequency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = frequency == currentFrequency,
                            onClick = { onFrequencySelected(frequency) }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = frequency.name.lowercase().replaceFirstChar {
                                    it.uppercaseChar()
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = getSyncFrequencyDescription(frequency),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
