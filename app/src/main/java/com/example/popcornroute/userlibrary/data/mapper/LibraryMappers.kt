package com.example.popcornroute.userlibrary.data.mapper

import com.example.popcornroute.userlibrary.data.local.dao.MarkAsWatchedOutcome
import com.example.popcornroute.userlibrary.data.local.entity.ListEntity
import com.example.popcornroute.userlibrary.data.local.entity.ListItemEntity
import com.example.popcornroute.userlibrary.data.local.entity.TitleCacheEntity
import com.example.popcornroute.userlibrary.data.local.relation.LibraryTitleRow
import com.example.popcornroute.userlibrary.domain.model.LibraryList
import com.example.popcornroute.userlibrary.domain.model.LibraryTitle
import com.example.popcornroute.userlibrary.domain.model.ListMembership
import com.example.popcornroute.userlibrary.domain.model.ListName
import com.example.popcornroute.userlibrary.domain.model.MarkAsWatchedResult
import com.example.popcornroute.userlibrary.domain.model.MediaType
import com.example.popcornroute.userlibrary.domain.model.TitleKey
import com.example.popcornroute.userlibrary.domain.model.TitleSummary

fun LibraryTitleRow.toDomain(): LibraryTitle = LibraryTitle(
    key = TitleKey(MediaType.fromValue(mediaType), tmdbId),
    savedAt = savedAt,
    summary = if (title != null && cachedAt != null) {
        TitleSummary(title, posterPath, releaseYear, rating, cachedAt)
    } else {
        null
    },
)

fun ListEntity.toDomain(): LibraryList = LibraryList(
    id = listId,
    name = name,
    isBuiltIn = isBuiltIn,
    createdAt = createdAt,
)

fun ListItemEntity.toDomain(): ListMembership = ListMembership(
    listId = listId,
    key = TitleKey(MediaType.fromValue(mediaType), tmdbId),
    addedAt = addedAt,
)

fun ListMembership.toEntity(userId: String): ListItemEntity = ListItemEntity(
    userId = userId,
    listId = listId,
    mediaType = key.mediaType.value,
    tmdbId = key.tmdbId,
    addedAt = addedAt,
)

fun MarkAsWatchedOutcome.toDomain(): MarkAsWatchedResult = MarkAsWatchedResult(
    changed = changed,
    removedFromWatchlist = removedWatchlistItem?.toDomain(),
)

fun TitleSummary.toCacheEntity(key: TitleKey): TitleCacheEntity = TitleCacheEntity(
    mediaType = key.mediaType.value,
    tmdbId = key.tmdbId,
    title = title,
    posterPath = posterPath,
    releaseYear = releaseYear,
    rating = rating,
    cachedAt = cachedAt,
)

/** New custom list row; the caller must have validated [name] with [ListName.validate]. */
fun newCustomListEntity(userId: String, listId: String, name: String, now: Long): ListEntity {
    val trimmed = ListName.normalize(name)
    return ListEntity(
        userId = userId,
        listId = listId,
        name = trimmed,
        nameKey = ListName.keyOf(trimmed),
        isBuiltIn = false,
        createdAt = now,
    )
}

/** The user's built-in Watchlist row. */
fun newWatchlistEntity(userId: String, now: Long): ListEntity = ListEntity(
    userId = userId,
    listId = LibraryList.WATCHLIST_ID,
    name = LibraryList.WATCHLIST_NAME,
    nameKey = ListName.keyOf(LibraryList.WATCHLIST_NAME),
    isBuiltIn = true,
    createdAt = now,
)
