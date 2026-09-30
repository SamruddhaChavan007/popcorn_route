package com.example.popcornroute.auth.domain.error

/** Authentication failures exposed without leaking Firebase-specific types. */
sealed interface AuthError {
    data object NameRequired : AuthError
    data object EmailRequired : AuthError
    data object InvalidEmail : AuthError
    data object PasswordRequired : AuthError

    /** Firebase rejected the password using the policy configured for the project. */
    data class WeakPassword(val reason: String?) : AuthError

    data object EmailAlreadyInUse : AuthError

    /** Wrong password or unknown email; deliberately not distinguished so accounts cannot be probed. */
    data object InvalidCredentials : AuthError

    data object AccountDisabled : AuthError

    data object NetworkUnavailable : AuthError
    data object TooManyRequests : AuthError
    data object ProfileUpdateFailed : AuthError
    data object Unknown : AuthError
}
