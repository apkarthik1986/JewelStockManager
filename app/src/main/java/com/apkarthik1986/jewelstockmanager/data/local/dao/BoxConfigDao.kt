package com.apkarthik1986.jewelstockmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apkarthik1986.jewelstockmanager.data.local.entity.BoxConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxConfigDao {

    @Query("SELECT * FROM box_configs WHERE category = :category AND isActive = 1 ORDER BY boxNumber ASC")
    fun getBoxesByCategory(category: String): Flow<List<BoxConfigEntity>>

    @Query("SELECT * FROM box_configs WHERE isActive = 1 ORDER BY category ASC, boxNumber ASC")
    fun getAllActiveBoxes(): Flow<List<BoxConfigEntity>>

    @Query("SELECT * FROM box_configs ORDER BY category ASC, boxNumber ASC")
    fun getAllBoxes(): Flow<List<BoxConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(boxes: List<BoxConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(box: BoxConfigEntity)

    @Query("DELETE FROM box_configs")
    suspend fun deleteAll()
}
