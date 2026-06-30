package com.apkarthik1986.jewelstockmanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.apkarthik1986.jewelstockmanager.domain.model.BoxConfig

/**
 * Room entity for a storage box container. Maps 1-to-1 with a Sheets row (boxes tab).
 */
@Entity(
    tableName = "box_configs",
    indices = [Index(value = ["category"])]
)
data class BoxConfigEntity(
    @PrimaryKey val boxNumber: String,
    val category: String,
    val tareWeightGrams: Double,
    val isActive: Boolean = true,
    val location: String = "",
    val lastUpdatedMs: Long = System.currentTimeMillis(),
    val sheetsRowIndex: Int = -1
) {
    fun toDomain() = BoxConfig(
        boxNumber = boxNumber,
        category = category,
        tareWeightGrams = tareWeightGrams,
        isActive = isActive,
        location = location,
        lastUpdatedMs = lastUpdatedMs,
        sheetsRowIndex = sheetsRowIndex
    )

    companion object {
        fun fromDomain(box: BoxConfig) = BoxConfigEntity(
            boxNumber = box.boxNumber,
            category = box.category,
            tareWeightGrams = box.tareWeightGrams,
            isActive = box.isActive,
            location = box.location,
            lastUpdatedMs = box.lastUpdatedMs,
            sheetsRowIndex = box.sheetsRowIndex
        )
    }
}
