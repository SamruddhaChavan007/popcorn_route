package com.example.popcornroute.userlibrary.domain.model

/** One title in one list. Kept whole so Undo can restore it with its original [addedAt]. */
data class ListMembership(
    val listId: String,
    val key: TitleKey,
    val addedAt: Long,
)
