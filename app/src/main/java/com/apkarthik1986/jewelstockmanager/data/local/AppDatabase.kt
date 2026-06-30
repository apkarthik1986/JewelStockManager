package com.apkarthik1986.jewelstockmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apkarthik1986.jewelstockmanager.data.local.dao.BoxConfigDao
import com.apkarthik1986.jewelstockmanager.data.local.dao.JewelItemDao
import com.apkarthik1986.jewelstockmanager.data.local.entity.BoxConfigEntity
import com.apkarthik1986.jewelstockmanager.data.local.entity.JewelItemEntity

@Database(
    entities = [JewelItemEntity::class, BoxConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jewelItemDao(): JewelItemDao
    abstract fun boxConfigDao(): BoxConfigDao

    companion object {
        const val DATABASE_NAME = "jewel_stock_db"
    }
}
