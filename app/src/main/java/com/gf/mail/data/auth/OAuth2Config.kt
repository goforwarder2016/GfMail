package com.gf.mail.data.auth

import com.gf.mail.domain.model.EmailProvider
import com.gf.mail.domain.model.OAuth2ProviderConfig

/**
 * OAuth2 configuration for different email providers
 * This contains the real OAuth2 credentials for production use
 */
object OAuth2Config {

    /**
     * Gmail OAuth2 Configuration
     *
     * To get these credentials:
     * 1. Go to Google Cloud Console (https://console.cloud.google.com/)
     * 2. Create a new project or select existing one
     * 3. Enable Gmail API
     * 4. Go to "Credentials" > "Create Credentials" > "OAuth 2.0 Client ID"
     * 5. Choose "Android" as application type
     * 6. Add your package name: com.gf.mail
     * 7. Add SHA-1 fingerprint of your debug/release keystore
     */
    object Gmail {
        // TODO: Replace with your actual Gmail OAuth2 credentials
        const val CLIENT_ID = "your_gmail_client_id.apps.googleusercontent.com"
        const val CLIENT_SECRET = "" // Not needed for native Android apps
        const val SCOPE = "https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/gmail.modify"
        const val AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        const val TOKEN_URL = "https://oauth2.googleapis.com/token"
    }

    /**
     * Microsoft OAuth2 Configuration
     *
     * To get these credentials:
     * 1. Go to Azure Portal (https://portal.azure.com/)
     * 2. Navigate to "Azure Active Directory" > "App registrations"
     * 3. Click "New registration"
     * 4. Choose "Accounts in any organizational directory and personal Microsoft accounts"
     * 5. Add redirect URI: com.gf.mail://oauth2callback
     * 6. After creation, note down the Application (client) ID
     */
    object Microsoft {
        // TODO: Replace with your actual Microsoft OAuth2 credentials
        const val CLIENT_ID = "your_microsoft_client_id"
        const val TENANT_ID = "common" // Use "common" for personal and work accounts
        const val SCOPE = "https://outlook.office.com/IMAP.AccessAsUser.All https://outlook.office.com/SMTP.Send offline_access"
        const val AUTH_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
        const val TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token"
    }

    /**
     * Common OAuth2 Parameters
     */
    const val REDIRECT_URI = "com.gf.mail://oauth2callback"
    const val RESPONSE_TYPE = "code"
    const val ACCESS_TYPE = "offline"
    const val PROMPT = "consent"

    /**
     * Get OAuth2 configuration for a specific provider
     */
    fun getConfig(provider: EmailProvider): OAuth2ProviderConfig? {
        return when (provider) {
            EmailProvider.GMAIL -> OAuth2ProviderConfig(
                clientId = Gmail.CLIENT_ID,
                clientSecret = Gmail.CLIENT_SECRET,
                scope = Gmail.SCOPE,
                authUrl = Gmail.AUTH_URL,
                tokenUrl = Gmail.TOKEN_URL,
                isDemoMode = false
            )

            EmailProvider.EXCHANGE -> OAuth2ProviderConfig(
                clientId = Microsoft.CLIENT_ID,
                clientSecret = "", // Microsoft uses PKCE for native apps
                scope = Microsoft.SCOPE,
                authUrl = Microsoft.AUTH_URL,
                tokenUrl = Microsoft.TOKEN_URL,
                isDemoMode = false
            )

            else -> null
        }
    }

    /**
     * Check if OAuth2 is properly configured for a provider
     */
    fun isConfigured(provider: EmailProvider): Boolean {
        val config = getConfig(provider) ?: return false
        return when (provider) {
            EmailProvider.GMAIL -> config.clientId != "your_gmail_client_id.apps.googleusercontent.com"
            EmailProvider.EXCHANGE -> config.clientId != "your_microsoft_client_id"
            else -> false
        }
    }
}


