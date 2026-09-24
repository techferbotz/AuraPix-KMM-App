package com.ferbotz.aurapix.creation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CreationDao {

    /** Every cached creation, newest first — all the pages loaded so far. */
    @Query("SELECT * FROM creations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CreationEntity>>

    @Query("SELECT COUNT(*) FROM creations")
    suspend fun count(): Int

    /** The ids of the [limit] newest cached creations, newest first. */
    @Query("SELECT id FROM creations ORDER BY createdAt DESC LIMIT :limit")
    suspend fun newestIds(limit: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CreationEntity>)

    @Query("DELETE FROM creations WHERE id = :id")
    suspend fun deleteById(id: String)

    /** `createdAt` is ISO-8601 UTC, which sorts as text in time order. */
    @Query("DELETE FROM creations WHERE createdAt < :createdAt")
    suspend fun deleteOlderThan(createdAt: String)

    @Query("DELETE FROM creations")
    suspend fun clear()
}
