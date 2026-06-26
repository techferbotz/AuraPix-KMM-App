package com.ferbotz.aurapix.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CreationDao {

    @Query("SELECT * FROM creations ORDER BY createdAt DESC LIMIT 100")
    fun observeAll(): Flow<List<CreationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CreationEntity>)

    @Query("DELETE FROM creations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM creations")
    suspend fun clear()
}
