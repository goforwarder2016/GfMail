package com.gf.mail.presentation.ui.accounts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gf.mail.domain.model.Account
import com.gf.mail.domain.model.EmailProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * QR Code display screen for sharing account configuration
 * Generates and displays QR codes containing email account setup information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeDisplayScreen(
    account: Account,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Generate QR code when screen loads
    LaunchedEffect(account) {
        scope.launch {
            try {
                isGenerating = true
                error = null
                val qrData = generateAccountQRData(account)
                val bitmap = generateQRCodeBitmap(qrData, context)
                qrBitmap = bitmap
            } catch (e: Exception) {
                error = "Failed to generate QR code: ${e.message}"
            } finally {
                isGenerating = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            qrBitmap?.let { bitmap ->
                                shareQRCode(context, bitmap, account)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isGenerating -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Generating QR Code...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                error != null -> {
                    // Error state
                    ErrorContent(
                        error = error!!,
                        onRetry = {
                            scope.launch {
                                try {
                                    isGenerating = true
                                    error = null
                                    val qrData = generateAccountQRData(account)
                                    val bitmap = generateQRCodeBitmap(qrData, context)
                                    qrBitmap = bitmap
                                } catch (e: Exception) {
                                    error = "Failed to generate QR code: ${e.message}"
                                } finally {
                                    isGenerating = false
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                qrBitmap != null -> {
                    // Success state - show QR code
                    QRCodeContent(
                        account = account,
                        qrBitmap = qrBitmap!!,
                        context = context,
                        scope = scope,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * QR code content display
 */
@Composable
private fun QRCodeContent(
    account: Account,
    qrBitmap: Bitmap,
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Account info header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = when (account.provider) {
                        com.gf.mail.domain.model.EmailProvider.GMAIL -> Icons.Default.Email
                        com.gf.mail.domain.model.EmailProvider.EXCHANGE -> Icons.Default.AccountCircle
                        else -> Icons.Default.Email
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = account.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // QR Code display
        Card(
            modifier = Modifier.size(280.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Account QR Code",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = "Share Your Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Others can scan this QR code to quickly set up the same email account on their device. The QR code contains server settings but not your password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    qrBitmap?.let { bitmap ->
                        scope.launch {
                            saveQRCodeToGallery(context, bitmap, account)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }

            Button(
                onClick = {
                    qrBitmap?.let { bitmap ->
                        shareQRCode(context, bitmap, account)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }
        }

        // Security notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = "Security Notice",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "This QR code contains server settings but not your password. Recipients will need to enter their own credentials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Error content display
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "QR Code Generation Failed",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

/**
 * Generate QR code data from account configuration
 */
private fun generateAccountQRData(account: Account): String {
    // Generate JSON format for comprehensive account data
    return buildString {
        append("GFMAIL-ACCOUNT:")
        append(account.email).append("|")
        append(account.displayName).append("|")
        append(account.provider.name).append("|")
        append(account.serverConfig.imapHost ?: "").append("|")
        append(account.serverConfig.imapPort).append("|")
        append(account.serverConfig.imapEncryption.name).append("|")
        append(account.serverConfig.smtpHost ?: "").append("|")
        append(account.serverConfig.smtpPort).append("|")
        append(account.serverConfig.smtpEncryption.name)
    }
}

/**
 * Generate QR code bitmap
 */
private suspend fun generateQRCodeBitmap(data: String, context: Context): Bitmap = withContext(
    Dispatchers.IO
) {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
    val width = bitMatrix.width
    val height = bitMatrix.height

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    bitmap
}

/**
 * QR Code data parser for email account configuration
 */
object QRCodeAccountParser {

    /**
     * Parse QR code data into account configuration
     * Supports various QR code formats for email setup
     */
    fun parseAccountQRCode(qrData: String): QRCodeAccountData? {
        return try {
            when {
                qrData.startsWith("mailto:") -> parseMailtoQRCode(qrData)
                qrData.startsWith("{") -> parseJsonQRCode(qrData)
                qrData.contains("@") && qrData.contains("|") -> parsePipeDelimitedQRCode(qrData)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseMailtoQRCode(qrData: String): QRCodeAccountData? {
        // Parse mailto: format QR codes
        // Format: mailto:user@example.com?server=imap.example.com&port=993
        val uri = android.net.Uri.parse(qrData)
        val email = uri.schemeSpecificPart?.substringBefore("?") ?: return null

        return QRCodeAccountData(
            email = email,
            provider = com.gf.mail.domain.model.EmailProvider.IMAP,
            displayName = email.substringBefore("@"),
            imapHost = uri.getQueryParameter("server"),
            imapPort = uri.getQueryParameter("port")?.toIntOrNull() ?: 993,
            smtpHost = uri.getQueryParameter("smtp"),
            smtpPort = uri.getQueryParameter("smtpPort")?.toIntOrNull() ?: 587
        )
    }

    private fun parseJsonQRCode(qrData: String): QRCodeAccountData? {
        // Parse JSON format QR codes
        // Expected JSON structure with email configuration
        return try {
            // This would use a JSON parser like Gson in a real implementation
            // TODO: Implement real QR code generation
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePipeDelimitedQRCode(qrData: String): QRCodeAccountData? {
        // Parse pipe-delimited format QR codes
        // Format: email@example.com|displayName|imap.server.com|993|smtp.server.com|587|password
        val parts = qrData.split("|")
        if (parts.size < 3) return null

        return QRCodeAccountData(
            email = parts[0],
            provider = com.gf.mail.domain.model.EmailProvider.IMAP,
            displayName = parts.getOrNull(1) ?: parts[0].substringBefore("@"),
            imapHost = parts.getOrNull(2),
            imapPort = parts.getOrNull(3)?.toIntOrNull() ?: 993,
            smtpHost = parts.getOrNull(4),
            smtpPort = parts.getOrNull(5)?.toIntOrNull() ?: 587
        )
    }
}


/**
 * Share QR code bitmap via Android share intent
 */
private fun shareQRCode(context: Context, bitmap: Bitmap, account: Account) {
    try {
        // Save bitmap to cache directory for sharing
        val cachePath = java.io.File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = java.io.File(cachePath, "qr_code_${account.email.replace("@", "_")}.png")

        val fileOutputStream = java.io.FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()

        // Create content URI
        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Create share intent
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
            putExtra(
                android.content.Intent.EXTRA_TEXT,
                "Email account setup for ${account.email}\nScan this QR code to configure your email client."
            )
            putExtra(
                android.content.Intent.EXTRA_SUBJECT,
                "Email Account QR Code - ${account.email}"
            )
            type = "image/png"
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share QR Code"))
    } catch (e: Exception) {
        // Handle sharing error
        android.widget.Toast.makeText(
            context,
            "Failed to share QR code: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

/**
 * Save QR code bitmap to device gallery
 */
private suspend fun saveQRCodeToGallery(context: Context, bitmap: Bitmap, account: Account) = withContext(
    kotlinx.coroutines.Dispatchers.IO
) {
    try {
        val filename = "gfmail_qr_${account.email.replace("@", "_")}_${System.currentTimeMillis()}.png"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_PICTURES + "/Gfmail"
                )
            }

            val uri = resolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let { imageUri ->
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        "QR code saved to gallery",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            // Use external storage for older Android versions
            val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_PICTURES
            )
            val gfmailDir = java.io.File(imagesDir, "Gfmail")
            if (!gfmailDir.exists()) {
                gfmailDir.mkdirs()
            }

            val file = java.io.File(gfmailDir, filename)
            val outputStream = java.io.FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            // Notify media scanner
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/png"),
                null
            )

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(
                    context,
                    "QR code saved to Pictures/Gfmail",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    } catch (e: Exception) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            android.widget.Toast.makeText(
                context,
                "Failed to save QR code: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}

/**
 * QR Code display screen that loads account by ID
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeDisplayScreen(
    accountId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var account by remember { mutableStateOf<Account?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load account by ID
    LaunchedEffect(accountId) {
        try {
            isLoading = true
            error = null
            
            // Get account from repository
            val application = com.gf.mail.GfmailApplication.instance
            val dependencies = application.dependencies
            val accountRepository = dependencies.getAccountRepository()
            
            val loadedAccount = accountRepository.getAccountById(accountId)
            if (loadedAccount != null) {
                account = loadedAccount
                println("✅ [QRCodeDisplayScreen] Loaded account: ${loadedAccount.email}")
            } else {
                error = "Account not found"
                println("❌ [QRCodeDisplayScreen] Account not found: $accountId")
            }
        } catch (e: Exception) {
            error = "Failed to load account: ${e.message}"
            println("❌ [QRCodeDisplayScreen] Failed to load account: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            // Loading state
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Loading account...")
                }
            }
        }
        
        error != null -> {
            // Error state
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
        }
        
        account != null -> {
            // Success state - show QR code
            QRCodeDisplayScreen(
                account = account!!,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
    }
}

/**
 * Data class for QR code account information
 */
data class QRCodeAccountData(
    val email: String,
    val provider: EmailProvider,
    val displayName: String? = null,
    val imapHost: String? = null,
    val imapPort: Int? = null,
    val smtpHost: String? = null,
    val smtpPort: Int? = null,
    val useSSL: Boolean = true
)
