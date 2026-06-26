package com.ferbotz.aurapix.data.local

import androidx.room.Room
import androidx.room.RoomDatabase

actual fun databaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = AndroidAppContext.require()
    val dbFile = context.getDatabasePath(DB_FILE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}
