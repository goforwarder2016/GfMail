package com.gf.mail.domain.model

/**
 * Enum representing the steps in the add account flow
 */
enum class AddAccountStep {
    SELECT_PROVIDER,
    ENTER_CREDENTIALS,
    OAUTH_AUTHENTICATION,
    PASSWORD_AUTHENTICATION,
    MANUAL_SERVER_CONFIG,
    TEST_CONNECTION,
    COMPLETED
}