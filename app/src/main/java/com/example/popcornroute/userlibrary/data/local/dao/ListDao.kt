package com.example.popcornroute.userlibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.popcornroute.userlibrary.data.local.entity.ListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {

    /** Creates the user's Watchlist row if it is missing; does nothing if it exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ensureWatchlist(watchlist: ListEntity): Long

    /**
     * Creates a custom list. Throws `SQLiteConstraintException` if the name is already used, so the
     * caller can report a duplicate-name error instead of silently dropping the list.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(list: ListEntity)

    /**
     * Renames a custom list. Returns the number of rows changed: 0 if the list does not exist or is
     * the built-in Watchlist. Throws `SQLiteConstraintException` if the new name is already used.
     */
    @Query(
        "UPDATE list SET name = :name, nameKey = :nameKey " +
            "WHERE userId = :userId AND listId = :listId AND isBuiltIn = 0",
    )
    suspend fun rename(userId: String, listId: String, name: String, nameKey: String): Int

    /** Deletes a custom list; its items cascade. Never deletes the built-in Watchlist. */
    @Query("DELETE FROM list WHERE userId = :userId AND listId = :listId AND isBuiltIn = 0")
    suspend fun deleteCustom(userId: String, listId: String): Int

    /** All of the user's lists: the Watchlist first, then custom lists oldest first. */
    @Query("SELECT * FROM list WHERE userId = :userId ORDER BY isBuiltIn DESC, createdAt ASC")
    fun observeLists(userId: String): Flow<List<ListEntity>>

    @Query("DELETE FROM list WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
}
