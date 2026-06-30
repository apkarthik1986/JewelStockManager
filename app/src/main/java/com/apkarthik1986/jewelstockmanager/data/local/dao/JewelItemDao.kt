package com.apkarthik1986.jewelstockmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.apkarthik1986.jewelstockmanager.data.local.entity.JewelItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JewelItemDao {

    @Query("SELECT * FROM jewel_items WHERE boxNumber = :boxNumber ORDER BY id ASC")
    fun getItemsByBox(boxNumber: String): Flow<List<JewelItemEntity>>

    @Query("SELECT * FROM jewel_items WHERE category = :category ORDER BY id ASC")
    fun getItemsByCategory(category: String): Flow<List<JewelItemEntity>>

    @Query("SELECT * FROM jewel_items ORDER BY category ASC, boxNumber ASC, id ASC")
    fun getAllItems(): Flow<List<JewelItemEntity>>

    @Query("SELECT * FROM jewel_items WHERE isDirty = 1")
    suspend fun getDirtyItems(): List<JewelItemEntity>

    @Query("UPDATE jewel_items SET status = :status, lastUpdatedMs = :updatedMs, isDirty = 1 WHERE id = :itemId")
    suspend fun updateStatus(itemId: String, status: String, updatedMs: Long = System.currentTimeMillis())

    @Query("UPDATE jewel_items SET isDirty = 0 WHERE id = :itemId")
    suspend fun clearDirty(itemId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<JewelItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: JewelItemEntity)

    @Query("DELETE FROM jewel_items")
    suspend fun deleteAll()
}
