package com.example.popcornroute.userlibrary.domain.model

import java.util.Locale

/** Why a custom list name was rejected. */
sealed interface ListNameError {
    data object Empty : ListNameError
    data object TooLong : ListNameError

    /** "Watchlist" is reserved for the built-in list. */
    data object Reserved : ListNameError

    /** Another list of the user already has this name, ignoring case and surrounding whitespace. */
    data object Duplicate : ListNameError
}

/** Custom list name rules from USER_LIBRARY_RULES.md. */
object ListName {
    const val MAX_LENGTH = 50

    fun normalize(raw: String): String = raw.trim()

    /** Case-folded form used to compare names for uniqueness. */
    fun keyOf(raw: String): String = normalize(raw).lowercase(Locale.ROOT)

    /** Returns the first problem with [raw], or null if it is a valid custom list name. */
    fun validate(raw: String): ListNameError? {
        val name = normalize(raw)
        return when {
            name.isEmpty() -> ListNameError.Empty
            name.length > MAX_LENGTH -> ListNameError.TooLong
            keyOf(name) == LibraryList.WATCHLIST_ID -> ListNameError.Reserved
            else -> null
        }
    }
}
