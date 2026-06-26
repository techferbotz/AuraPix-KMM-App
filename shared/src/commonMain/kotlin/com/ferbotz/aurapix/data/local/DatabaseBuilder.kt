package com.ferbotz.aurapix.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

internal const val DB_FILE_NAME = "aurapix.db"

/** Finalizes a platform [RoomDatabase.Builder] into a ready [AppDatabase]. */
fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setDriver(BundledSQLiteDriver())
        // Dispatchers.IO is JVM-only; Default is the cross-platform (commonMain) choice.
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()

/** Platform database builder: Android uses the app context, iOS the documents directory. */
expect fun databaseBuilder(): RoomDatabase.Builder<AppDatabase>
