package com.gf.mail.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.gf.mail.presentation.viewmodel.SettingsViewModel
import com.gf.mail.utils.LocaleUtils
import com.gf.mail.utils.LanguageManager
import com.gf.mail.utils.LanguageTestUtils

/**
 * Main settings screen with all setting categories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSyncSettings: () -> Unit,
    onNavigateToSignatures: () -> Unit,
    onNavigateToServerSettings: () -> Unit,
    onNavigateToAccountSettings: () -> Unit,
    onNavigateToAccessibilitySettings: () -> Unit = {},
    onNavigateToPerformanceSettings: () -> Unit = {},
    onNavigateToSecuritySettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = remember {
        com.gf.mail.di.DependencyContainer(context).getSettingsViewModel()
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Local state for dialogs
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

            // Handle messages and errors
            LaunchedEffect(uiState.error) {
                uiState.error?.let {
                    // TODO: Show error snackbar
                }
            }
            
            // Debug: Show current language status
            LaunchedEffect(Unit) {
                LanguageTestUtils.testCurrentLanguage(context)
            }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            // TODO: Show success snackbar
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(com.gf.mail.R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = context.getString(com.gf.mail.R.string.cd_back_button))
                    }
                },
                actions = {
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
            // General Settings Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.settings_general),
                    icon = Icons.Default.Settings
                ) {
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_theme),
                        subtitle = uiState.themeDisplayName,
                        icon = Icons.Default.Palette,
                        onClick = {
                            showThemeDialog = true
                        }
                    )

                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_language),
                        subtitle = LocaleUtils.getLocalizedLanguageDisplayName(
                            context,
                            uiState.settings.language
                        ),
                        icon = Icons.Default.Language,
                        onClick = {
                            showLanguageDialog = true
                        }
                    )
                }
            }

            // Account Settings Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.settings_accounts),
                    icon = Icons.Default.AccountCircle
                ) {
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_manage_accounts),
                        subtitle = context.getString(com.gf.mail.R.string.settings_manage_accounts_subtitle),
                        icon = Icons.Default.Person,
                        onClick = onNavigateToAccountSettings
                    )

                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_server_settings),
                        subtitle = context.getString(com.gf.mail.R.string.settings_server_settings_subtitle),
                        icon = Icons.Default.Storage,
                        onClick = onNavigateToServerSettings
                    )
                }
            }

            // Email Settings Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.settings_email),
                    icon = Icons.Default.Email
                ) {
                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_auto_mark_as_read),
                        subtitle = context.getString(com.gf.mail.R.string.settings_auto_mark_as_read_subtitle, uiState.settings.autoMarkAsReadDelay),
                        icon = Icons.Default.MarkEmailRead,
                        checked = uiState.settings.autoMarkAsRead,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.settings
                            val updatedSettings = currentSettings.copy(autoMarkAsRead = checked)
                            viewModel.updateEmailSettings(updatedSettings)
                        }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_show_preview_text),
                        subtitle = context.getString(com.gf.mail.R.string.settings_show_preview_text_subtitle, uiState.settings.previewLines),
                        icon = Icons.Default.Preview,
                        checked = uiState.settings.showPreviewText,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.settings
                            val updatedSettings = currentSettings.copy(showPreviewText = checked)
                            viewModel.updateEmailSettings(updatedSettings)
                        }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_show_images),
                        subtitle = context.getString(com.gf.mail.R.string.settings_show_images_subtitle),
                        icon = Icons.Default.Image,
                        checked = uiState.settings.showImages,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.settings
                            val updatedSettings = currentSettings.copy(showImages = checked)
                            viewModel.updateEmailSettings(updatedSettings)
                        }
                    )

                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_email_signatures),
                        subtitle = context.getString(com.gf.mail.R.string.settings_email_signatures_subtitle),
                        icon = Icons.Default.Edit,
                        onClick = onNavigateToSignatures
                    )
                }
            }

            // Display Settings Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.settings_display),
                    icon = Icons.Default.DisplaySettings
                ) {
                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_show_avatars),
                        subtitle = context.getString(com.gf.mail.R.string.settings_show_avatars_subtitle),
                        icon = Icons.Default.AccountCircle,
                        checked = uiState.settings.showAvatars,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.settings
                            val updatedSettings = currentSettings.copy(showAvatars = checked)
                            viewModel.updateDisplaySettings(updatedSettings)
                        }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_show_unread_badge),
                        subtitle = context.getString(com.gf.mail.R.string.settings_show_unread_badge_subtitle),
                        icon = Icons.Default.Circle,
                        checked = uiState.settings.showUnreadBadge,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.settings
                            val updatedSettings = currentSettings.copy(showUnreadBadge = checked)
                            viewModel.updateDisplaySettings(updatedSettings)
                        }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_group_by_date),
                        subtitle = context.getString(com.gf.mail.R.string.settings_group_by_date_subtitle),
                        icon = Icons.Default.DateRange,
                        checked = uiState.settings.groupByDate,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.settings
                            val updatedSettings = currentSettings.copy(groupByDate = checked)
                            viewModel.updateDisplaySettings(updatedSettings)
                        }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_compact_view),
                        subtitle = context.getString(com.gf.mail.R.string.settings_compact_view_subtitle),
                        icon = Icons.Default.ViewList,
                        checked = uiState.settings.useCompactView,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.settings
                            val updatedSettings = currentSettings.copy(useCompactView = checked)
                            viewModel.updateDisplaySettings(updatedSettings)
                        }
                    )
                }
            }

            // Sync & Notifications Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.settings_sync_notifications),
                    icon = Icons.Default.Sync
                ) {
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_sync_settings),
                        subtitle = context.getString(com.gf.mail.R.string.settings_sync_notifications_subtitle),
                        icon = Icons.Default.CloudSync,
                        onClick = onNavigateToSyncSettings
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_notifications),
                        subtitle = context.getString(com.gf.mail.R.string.settings_enable_notifications),
                        icon = Icons.Default.Notifications,
                        checked = uiState.settings.globalNotificationsEnabled,
                        onCheckedChange = { checked ->
                            viewModel.toggleNotifications(checked)
                        }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_background_sync),
                        subtitle = context.getString(com.gf.mail.R.string.settings_background_sync_subtitle, uiState.syncIntervalDisplayName),
                        icon = Icons.Default.Sync,
                        checked = uiState.settings.backgroundSyncEnabled,
                        onCheckedChange = { checked ->
                            viewModel.toggleBackgroundSync(checked)
                        }
                    )
                }
            }

            // Security Settings Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.settings_security_privacy),
                    icon = Icons.Default.Security
                ) {
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_security_privacy),
                        subtitle = context.getString(com.gf.mail.R.string.settings_security_privacy_subtitle),
                        icon = Icons.Default.Security,
                        onClick = onNavigateToSecuritySettings
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_biometric_auth),
                        subtitle = context.getString(com.gf.mail.R.string.settings_biometric_auth_subtitle),
                        icon = Icons.Default.Fingerprint,
                        checked = uiState.settings.biometricAuthEnabled,
                        onCheckedChange = { checked ->
                            viewModel.toggleBiometricAuth(checked)
                        }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_auto_lock),
                        subtitle = context.getString(com.gf.mail.R.string.settings_auto_lock_subtitle, uiState.autoLockDelayDisplayName),
                        icon = Icons.Default.Lock,
                        checked = uiState.settings.autoLockEnabled,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.securitySettings
                            val updatedSettings = currentSettings.copy(autoLockEnabled = checked)
                            viewModel.updateSecuritySettings(updatedSettings)
                        }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_block_external_images),
                        subtitle = context.getString(com.gf.mail.R.string.settings_block_external_images_subtitle),
                        icon = Icons.Default.Block,
                        checked = uiState.settings.blockExternalImages,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.securitySettings
                            val updatedSettings = currentSettings.copy(blockExternalImages = checked)
                            viewModel.updateSecuritySettings(updatedSettings)
                        }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_warn_unsafe_links),
                        subtitle = context.getString(com.gf.mail.R.string.settings_warn_unsafe_links_subtitle),
                        icon = Icons.Default.Warning,
                        checked = uiState.settings.warnUnsafeLinks,
                        onCheckedChange = { checked ->
                            val currentSettings = uiState.securitySettings
                            val updatedSettings = currentSettings.copy(warnUnsafeLinks = checked)
                            viewModel.updateSecuritySettings(updatedSettings)
                        }
                    )
                }
            }

            // Storage Settings Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.settings_storage),
                    icon = Icons.Default.Storage
                ) {
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_cache_size),
                        subtitle = uiState.cacheSizeFormatted,
                        icon = Icons.Default.Folder,
                        onClick = {
                            viewModel.loadCacheSize()
                        }
                    )

                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_clear_cache),
                        subtitle = context.getString(com.gf.mail.R.string.settings_clear_cache_subtitle),
                        icon = Icons.Default.ClearAll,
                        onClick = {
                            viewModel.clearCache()
                        }
                    )

                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_optimize_storage),
                        subtitle = context.getString(com.gf.mail.R.string.settings_optimize_storage_subtitle),
                        icon = Icons.Default.CleaningServices,
                        onClick = {
                            viewModel.optimizeStorage()
                        }
                    )
                }
            }

            // Data Management Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.settings_data_management),
                    icon = Icons.Default.ImportExport
                ) {
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_export_settings),
                        subtitle = context.getString(com.gf.mail.R.string.settings_export_settings_subtitle),
                        icon = Icons.Default.FileUpload,
                        onClick = {
                            viewModel.exportSettings()
                        }
                    )

                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_import_settings),
                        subtitle = context.getString(com.gf.mail.R.string.settings_import_settings_subtitle),
                        icon = Icons.Default.FileDownload,
                        onClick = {
                            // TODO: Show file picker for import
                        }
                    )

                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_reset_defaults),
                        subtitle = context.getString(com.gf.mail.R.string.settings_reset_defaults_subtitle),
                        icon = Icons.Default.RestoreFromTrash,
                        onClick = {
                            // TODO: Show confirmation dialog
                            viewModel.resetToDefaults()
                        }
                    )
                }
            }

            // Accessibility Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.accessibility_title),
                    icon = Icons.Default.Accessibility
                ) {
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.accessibility_title),
                        subtitle = context.getString(com.gf.mail.R.string.settings_accessibility_subtitle),
                        icon = Icons.Default.AccessibilityNew,
                        onClick = onNavigateToAccessibilitySettings
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_high_contrast_mode),
                        subtitle = context.getString(com.gf.mail.R.string.settings_high_contrast_mode_subtitle),
                        icon = Icons.Default.Contrast,
                        checked = false, // TODO: Get from accessibility settings
                        onCheckedChange = { /* TODO: Update accessibility settings */ }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.accessibility_large_text),
                        subtitle = context.getString(com.gf.mail.R.string.settings_large_text_subtitle),
                        icon = Icons.Default.FormatSize,
                        checked = false, // TODO: Get from accessibility settings
                        onCheckedChange = { /* TODO: Update accessibility settings */ }
                    )
                }
            }

            // Performance Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.performance_settings),
                    icon = Icons.Default.Speed
                ) {
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.performance_settings),
                        subtitle = context.getString(com.gf.mail.R.string.settings_performance_subtitle),
                        icon = Icons.Default.Tune,
                        onClick = onNavigateToPerformanceSettings
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.performance_auto_optimization),
                        subtitle = context.getString(com.gf.mail.R.string.settings_auto_optimization_subtitle),
                        icon = Icons.Default.AutoMode,
                        checked = true, // TODO: Get from performance settings
                        onCheckedChange = { /* TODO: Update performance settings */ }
                    )

                    SwitchSettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_battery_saver_mode),
                        subtitle = context.getString(com.gf.mail.R.string.settings_battery_saver_mode_subtitle),
                        icon = Icons.Default.BatteryAlert,
                        checked = false, // TODO: Get from performance settings
                        onCheckedChange = { /* TODO: Update performance settings */ }
                    )
                }
            }

            // About Section
            item {
                SettingsSection(
                    title = context.getString(com.gf.mail.R.string.settings_about),
                    icon = Icons.Default.Info
                ) {
                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_app_version),
                        subtitle = "1.0.0", // TODO: Get from BuildConfig
                        icon = Icons.Default.Apps,
                        onClick = { }
                    )

                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_privacy_policy),
                        subtitle = context.getString(com.gf.mail.R.string.settings_privacy_policy_subtitle),
                        icon = Icons.Default.PrivacyTip,
                        onClick = {
                            // TODO: Open privacy policy
                        }
                    )

                    SettingsRow(
                        title = context.getString(com.gf.mail.R.string.settings_terms_of_service),
                        subtitle = context.getString(com.gf.mail.R.string.settings_terms_of_service_subtitle),
                        icon = Icons.Default.Article,
                        onClick = {
                            // TODO: Open terms of service
                        }
                    )
                }
            }
        }
    }

    // Language selection dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = when (uiState.languageSetting) {
                com.gf.mail.domain.model.LanguageSetting.SYSTEM_DEFAULT -> "system"
                com.gf.mail.domain.model.LanguageSetting.ENGLISH -> "en"
                com.gf.mail.domain.model.LanguageSetting.CHINESE_SIMPLIFIED -> "zh-CN"
                com.gf.mail.domain.model.LanguageSetting.CHINESE_TRADITIONAL -> "zh-TW"
                com.gf.mail.domain.model.LanguageSetting.JAPANESE -> "ja"
                com.gf.mail.domain.model.LanguageSetting.KOREAN -> "ko"
            },
            onLanguageSelected = { languageCode ->
                val languageSetting = when (languageCode) {
                    "system" -> com.gf.mail.domain.model.LanguageSetting.SYSTEM_DEFAULT
                    "en" -> com.gf.mail.domain.model.LanguageSetting.ENGLISH
                    "zh-CN" -> com.gf.mail.domain.model.LanguageSetting.CHINESE_SIMPLIFIED
                    "zh-TW" -> com.gf.mail.domain.model.LanguageSetting.CHINESE_TRADITIONAL
                    "ja" -> com.gf.mail.domain.model.LanguageSetting.JAPANESE
                    "ko" -> com.gf.mail.domain.model.LanguageSetting.KOREAN
                    else -> com.gf.mail.domain.model.LanguageSetting.ENGLISH
                }
                // Use immediate language update
                viewModel.updateLanguageImmediate(languageSetting, context)
                showLanguageDialog = false
                
                // Restart activity to apply language changes immediately
                if (context is androidx.activity.ComponentActivity) {
                    LanguageManager.restartActivity(context)
                }
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Theme selection dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.settings.theme,
            onThemeSelected = { theme ->
                viewModel.updateTheme(theme, context)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SwitchSettingsRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                onCheckedChange = onCheckedChange
            )
        }
    }
}
