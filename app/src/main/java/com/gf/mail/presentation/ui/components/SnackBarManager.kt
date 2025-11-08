package com.gf.mail.presentation.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Centralized SnackBar management system for the application
 */
object SnackBarManager {
    private val _messages = MutableSharedFlow<SnackBarMessage>()
    val messages: Flow<SnackBarMessage> = _messages.asSharedFlow()

    /**
     * Show a success message
     */
    suspend fun showSuccess(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        _messages.emit(
            SnackBarMessage(
                message = message,
                type = SnackBarType.SUCCESS,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }

    /**
     * Show an error message
     */
    suspend fun showError(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        _messages.emit(
            SnackBarMessage(
                message = message,
                type = SnackBarType.ERROR,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }

    /**
     * Show a warning message
     */
    suspend fun showWarning(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        _messages.emit(
            SnackBarMessage(
                message = message,
                type = SnackBarType.WARNING,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }

    /**
     * Show an info message
     */
    suspend fun showInfo(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        _messages.emit(
            SnackBarMessage(
                message = message,
                type = SnackBarType.INFO,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }
}

/**
 * SnackBar message data
 */
data class SnackBarMessage(
    val message: String,
    val type: SnackBarType = SnackBarType.INFO,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short
)

/**
 * SnackBar message types
 */
enum class SnackBarType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

/**
 * Composable for handling SnackBar messages
 */
@Composable
fun SnackBarHandler(
    snackBarHostState: SnackbarHostState,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    LaunchedEffect(Unit) {
        SnackBarManager.messages.collect { message ->
            val result = snackBarHostState.showSnackbar(
                message = message.message,
                actionLabel = message.actionLabel,
                duration = message.duration
            )

            if (result == SnackbarResult.ActionPerformed) {
                message.onAction?.invoke()
            }
        }
    }
}

/**
 * Styled SnackBarHost with different colors for different message types
 */
@Composable
fun StyledSnackBarHost(
    hostState: SnackbarHostState,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackBarData ->
        // Determine snackbar colors based on message content
        val (containerColor, contentColor) = when {
            snackBarData.visuals.message.contains("success", ignoreCase = true) ||
                snackBarData.visuals.message.contains("saved", ignoreCase = true) ||
                snackBarData.visuals.message.contains("created", ignoreCase = true) ||
                snackBarData.visuals.message.contains("updated", ignoreCase = true) -> {
                Color(0xFF4CAF50) to Color.White // Green
            }
            snackBarData.visuals.message.contains("error", ignoreCase = true) ||
                snackBarData.visuals.message.contains("failed", ignoreCase = true) ||
                snackBarData.visuals.message.contains("cannot", ignoreCase = true) -> {
                MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
            }
            snackBarData.visuals.message.contains("warning", ignoreCase = true) ||
                snackBarData.visuals.message.contains("please", ignoreCase = true) -> {
                Color(0xFFFF9800) to Color.White // Orange
            }
            else -> MaterialTheme.colorScheme.inverseSurface to MaterialTheme.colorScheme.inverseOnSurface
        }

        Snackbar(
            snackbarData = snackBarData,
            containerColor = containerColor,
            contentColor = contentColor
        )
    }
}

/**
 * Extension function to easily show SnackBar messages in ViewModels
 */
suspend fun showSuccessMessage(message: String) {
    SnackBarManager.showSuccess(message)
}

suspend fun showErrorMessage(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    SnackBarManager.showError(message, actionLabel, onAction)
}

suspend fun showWarningMessage(message: String) {
    SnackBarManager.showWarning(message)
}

suspend fun showInfoMessage(message: String) {
    SnackBarManager.showInfo(message)
}
