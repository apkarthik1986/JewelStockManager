package com.apkarthik1986.jewelstockmanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.apkarthik1986.jewelstockmanager.domain.model.ItemStatus
import com.apkarthik1986.jewelstockmanager.domain.model.JewelItem

/**
 * Room entity for a jewelry item. Maps 1-to-1 with a Sheets row (items tab).
 */
@Entity(
    tableName = "jewel_items",
    indices = [Index(value = ["boxNumber"]), Index(value = ["category"])]
)
data class JewelItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val boxNumber: String,
    val weightGrams: Double,
    val status: String,            // serialized as ItemStatus.name
    val description: String = "",
    val imageUrl: String = "",
    val lastUpdatedMs: Long = System.currentTimeMillis(),
    val sheetsRowIndex: Int = -1,
    val isDirty: Boolean = false   // true when a local change is pending push
) {
    fun toDomain() = JewelItem(
        id = id,
        name = name,
        category = category,
        boxNumber = boxNumber,
        weightGrams = weightGrams,
        status = ItemStatus.fromLabel(status),
        description = description,
        imageUrl = imageUrl,
        lastUpdatedMs = lastUpdatedMs,
        sheetsRowIndex = sheetsRowIndex
    )

    companion object {
        fun fromDomain(item: JewelItem, isDirty: Boolean = false) = JewelItemEntity(
            id = item.id,
            name = item.name,
            category = item.category,
            boxNumber = item.boxNumber,
            weightGrams = item.weightGrams,
            status = item.status.label,
            description = item.description,
            imageUrl = item.imageUrl,
            lastUpdatedMs = item.lastUpdatedMs,
            sheetsRowIndex = item.sheetsRowIndex,
            isDirty = isDirty
        )
    }
}
