package com.apkarthik1986.jewelstockmanager.domain.model

/**
 * Domain model representing a single jewelry item in the inventory.
 * Decoupled from database entities and remote DTOs.
 */
data class JewelItem(
    val id: String,
    val name: String,
    val category: String,
    val boxNumber: String,
    val weightGrams: Double,
    val status: ItemStatus,
    val description: String = "",
    val imageUrl: String = "",
    val lastUpdatedMs: Long = System.currentTimeMillis(),
    val sheetsRowIndex: Int = -1
)
