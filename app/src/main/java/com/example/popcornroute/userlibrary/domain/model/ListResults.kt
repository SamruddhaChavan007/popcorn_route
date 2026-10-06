package com.example.popcornroute.userlibrary.domain.model

sealed interface CreateListResult {
    data class Success(val list: LibraryList) : CreateListResult
    data class Failure(val error: ListNameError) : CreateListResult
}

sealed interface RenameListResult {
    data object Success : RenameListResult
    data class Failure(val error: ListNameError) : RenameListResult

    /** The list does not exist, or it is the built-in Watchlist, which cannot be renamed. */
    data object NotAllowed : RenameListResult
}
