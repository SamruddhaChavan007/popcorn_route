package com.example.popcornroute.userlibrary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.popcornroute.userlibrary.data.local.dao.FavoriteDao
import com.example.popcornroute.userlibrary.data.local.dao.LibraryActionsDao
import com.example.popcornroute.userlibrary.data.local.dao.ListDao
import com.example.popcornroute.userlibrary.data.local.dao.ListItemDao
import com.example.popcornroute.userlibrary.data.local.dao.TitleCacheDao
import com.example.popcornroute.userlibrary.data.local.dao.WatchedDao
import com.example.popcornroute.userlibrary.data.local.entity.FavoriteEntity
import com.example.popcornroute.userlibrary.data.local.entity.ListEntity
import com.example.popcornroute.userlibrary.data.local.entity.ListItemEntity
import com.example.popcornroute.userlibrary.data.local.entity.TitleCacheEntity
import com.example.popcornroute.userlibrary.data.local.entity.WatchedEntity

@Database(
    entities = [
        FavoriteEntity::class,
        WatchedEntity::class,
        ListEntity::class,
        ListItemEntity::class,
        TitleCacheEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class UserLibraryDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchedDao(): WatchedDao
    abstract fun listDao(): ListDao
    abstract fun listItemDao(): ListItemDao
    abstract fun titleCacheDao(): TitleCacheDao
    abstract fun libraryActionsDao(): LibraryActionsDao

    companion object {
        const val NAME = "user_library.db"

        /**
         * Add to the database builder with `addCallback`. Room cannot declare CHECK constraints, so
         * the rule "isBuiltIn is true exactly for the Watchlist ID" is enforced with triggers.
         */
        val callback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.execSQL(builtInTrigger("list_built_in_insert", "INSERT"))
                db.execSQL(builtInTrigger("list_built_in_update", "UPDATE OF listId, isBuiltIn"))
            }
        }

        private fun builtInTrigger(name: String, event: String) =
            "CREATE TRIGGER IF NOT EXISTS $name BEFORE $event ON `list` " +
                "WHEN NEW.isBuiltIn != (NEW.listId = 'watchlist') " +
                "BEGIN SELECT RAISE(ABORT, 'isBuiltIn must be true exactly for the watchlist'); END"
    }
}