package com.Gumbatov.wallet.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.Gumbatov.wallet.models.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    // IGNORE: a duplicate tag name is skipped instead of crashing the app
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT * FROM tags ORDER BY name")
    fun getAll(): Flow<List<Tag>>
}