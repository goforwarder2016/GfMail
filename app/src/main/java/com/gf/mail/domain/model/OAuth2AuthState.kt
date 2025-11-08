package com.gf.mail.domain.model

/**
 * OAuth2 authentication state
 */
enum class OAuth2AuthState {
    IDLE,
    AUTHENTICATING,
    SUCCESS,
    FAILED,
    CANCELLED
}