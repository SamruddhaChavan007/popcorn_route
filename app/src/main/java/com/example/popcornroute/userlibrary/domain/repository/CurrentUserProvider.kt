package com.example.popcornroute.userlibrary.domain.repository

/** Identifies whose library to read and write, without exposing the auth SDK. */
interface CurrentUserProvider {
    /** The signed-in user's ID, or null when nobody is signed in. */
    val userId: String?
}

/** The library only exists for a signed-in user; calling it signed out is a programming error. */
fun CurrentUserProvider.requireUserId(): String =
    checkNotNull(userId) { "User library accessed while signed out" }
