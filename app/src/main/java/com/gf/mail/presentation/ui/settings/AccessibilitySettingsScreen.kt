package com.gf.mail.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gf.mail.GfmailApplication
import com.gf.mail.R
import com.gf.mail.domain.model.AccessibilityRecommendation
import com.gf.mail.domain.model.AccessibilityRecommendationPriority
import com.gf.mail.domain.model.AccessibilityRecommendationType
import com.gf.mail.domain.model.AccessibilitySettings
import com.gf.mail.presentation.viewmodel.AccessibilitySettingsViewModel

/**
 * Screen for managing accessibility settings and compliance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dependencies = (context.applicationContext as GfmailApplication).dependencies
    val viewModel: AccessibilitySettingsViewModel = dependencies.createAccessibilitySettingsViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = {
                    Text(
                        text = context.getString(com.gf.mail.R.string.accessibility_title),
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = context.getString(R.string.cd_back_button)
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.applyAutoSettings() },
                        modifier = Modifier.semantics {
                            contentDescription = "Apply automatic accessibility settings"
                        }
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null)
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
            // Accessibility Status Section
            item {
                AccessibilityStatusSection(
                    isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                    isTalkBackEnabled = uiState.isTalkBackEnabled
                )
            }

            // Recommendations Section
            if (uiState.recommendations.isNotEmpty()) {
                item {
                    AccessibilityRecommendationsSection(
                        recommendations = uiState.recommendations,
                        onApplyRecommendation = { recommendation ->
                            viewModel.applyRecommendation(recommendation)
                        }
                    )
                }
            }

            // Visual Accessibility Section
            item {
                AccessibilitySection(
                    title = "Visual Accessibility",
                    icon = Icons.Default.Visibility
                ) {
                    AccessibilitySwitchRow(
                        title = "High Contrast Mode",
                        subtitle = "Increases contrast for better visibility",
                        icon = Icons.Default.Contrast,
                        checked = uiState.settings.highContrastMode,
                        onCheckedChange = { viewModel.updateHighContrastMode(it) }
                    )

                    AccessibilitySwitchRow(
                        title = "Large Text",
                        subtitle = "Increases text size throughout the app",
                        icon = Icons.Default.FormatSize,
                        checked = uiState.settings.largeTextMode,
                        onCheckedChange = { viewModel.updateLargeTextMode(it) }
                    )

                    AccessibilitySwitchRow(
                        title = "Color Blind Friendly",
                        subtitle = "Uses colors that are easier to distinguish",
                        icon = Icons.Default.Palette,
                        checked = uiState.settings.colorBlindFriendly,
                        onCheckedChange = { viewModel.updateColorBlindFriendly(it) }
                    )

                    AccessibilitySwitchRow(
                        title = "Focus Indicator",
                        subtitle = "Shows visual focus indicators for keyboard navigation",
                        icon = Icons.Default.CropFree,
                        checked = uiState.settings.focusIndicator,
                        onCheckedChange = { viewModel.updateFocusIndicator(it) }
                    )
                }
            }

            // Motor Accessibility Section
            item {
                AccessibilitySection(
                    title = "Motor Accessibility",
                    icon = Icons.Default.TouchApp
                ) {
                    AccessibilitySwitchRow(
                        title = "Large Touch Targets",
                        subtitle = "Makes buttons and interactive elements larger",
                        icon = Icons.Default.TouchApp,
                        checked = uiState.settings.touchTargetSize > 1.0f,
                        onCheckedChange = { viewModel.updateTouchTargetSize(it) }
                    )

                    AccessibilitySwitchRow(
                        title = "Keyboard Navigation",
                        subtitle = "Enables full keyboard navigation support",
                        icon = Icons.Default.Keyboard,
                        checked = uiState.settings.keyboardNavigation,
                        onCheckedChange = { viewModel.updateKeyboardNavigation(it) }
                    )

                    AccessibilitySwitchRow(
                        title = "Reduce Motion",
                        subtitle = "Reduces animations and transitions",
                        icon = Icons.Default.SlowMotionVideo,
                        checked = uiState.settings.reduceMotion,
                        onCheckedChange = { viewModel.updateReduceMotion(it) }
                    )
                }
            }

            // Screen Reader Section
            item {
                AccessibilitySection(
                    title = "Screen Reader Support",
                    icon = Icons.Default.RecordVoiceOver
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
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
                                    if (uiState.isTalkBackEnabled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (uiState.isTalkBackEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "TalkBack Status",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (uiState.isTalkBackEnabled) {
                                            "TalkBack is enabled and active"
                                        } else {
                                            "TalkBack is not enabled"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!uiState.isTalkBackEnabled) {
                                Text(
                                    text = "To enable TalkBack, go to Settings → Accessibility → TalkBack on your device.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Actions Section
            item {
                AccessibilitySection(
                    title = context.getString(com.gf.mail.R.string.accessibility_actions),
                    icon = Icons.Default.Settings
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.resetSettings() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.RestoreFromTrash,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(context.getString(com.gf.mail.R.string.accessibility_reset))
                        }

                        Button(
                            onClick = { viewModel.applyAutoSettings() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto Configure")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessibilityStatusSection(
    isAccessibilityEnabled: Boolean,
    isTalkBackEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Accessibility Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isAccessibilityEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Accessibility Services",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isAccessibilityEnabled) "Active" else "Not active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    if (isTalkBackEnabled) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isTalkBackEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Screen Reader (TalkBack)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isTalkBackEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AccessibilityRecommendationsSection(
    recommendations: List<AccessibilityRecommendation>,
    onApplyRecommendation: (AccessibilityRecommendation) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )

            recommendations.forEach { recommendation ->
                AccessibilityRecommendationItem(
                    recommendation = recommendation,
                    onApply = { onApplyRecommendation(recommendation) }
                )
            }
        }
    }
}

@Composable
private fun AccessibilityRecommendationItem(
    recommendation: AccessibilityRecommendation,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = when (recommendation.priority) {
            AccessibilityRecommendationPriority.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(
                alpha = 0.3f
            )
            AccessibilityRecommendationPriority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.3f
            )
            AccessibilityRecommendationPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant
            AccessibilityRecommendationPriority.CRITICAL -> MaterialTheme.colorScheme.error.copy(
                alpha = 0.3f
            )
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                when (recommendation.type) {
                    AccessibilityRecommendationType.VISUAL -> Icons.Default.Visibility
                    AccessibilityRecommendationType.MOTOR -> Icons.Default.TouchApp
                    AccessibilityRecommendationType.COGNITIVE -> Icons.Default.Psychology
                    AccessibilityRecommendationType.AUDITORY -> Icons.Default.VolumeUp
                    AccessibilityRecommendationType.SPEECH -> Icons.Default.RecordVoiceOver
                    AccessibilityRecommendationType.LANGUAGE -> Icons.Default.Translate
                },
                contentDescription = null,
                tint = when (recommendation.priority) {
                    AccessibilityRecommendationPriority.CRITICAL -> MaterialTheme.colorScheme.error
                    AccessibilityRecommendationPriority.HIGH -> MaterialTheme.colorScheme.error
                    AccessibilityRecommendationPriority.MEDIUM -> MaterialTheme.colorScheme.primary
                    AccessibilityRecommendationPriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onApply) {
                Text("Apply")
            }
        }
    }
}

@Composable
private fun AccessibilitySection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
            }

            content()
        }
    }
}

@Composable
private fun AccessibilitySwitchRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics {
                    contentDescription = "$title, ${if (checked) "enabled" else "disabled"}"
                }
            )
        }
    }
}
