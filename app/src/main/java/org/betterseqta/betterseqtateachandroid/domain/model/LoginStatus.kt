package org.betterseqta.betterseqtateachandroid.domain.model

sealed interface LoginStatus {
    data object LoggedOut : LoginStatus
    data object LoggingIn : LoginStatus
    data object LoggedIn : LoginStatus
    data class Error(val message: String) : LoginStatus
}
