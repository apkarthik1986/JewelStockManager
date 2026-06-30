package com.apkarthik1986.jewelstockmanager.domain.model

/**
 * Domain model representing a storage box container.
 * Each box belongs to one jewelry category and holds its own tare weight.
 */
data class BoxConfig(
    val boxNumber: String,
    val category: String,
    val tareWeightGrams: Double,
    val isActive: Boolean = true,
    val location: String = "",
    val lastUpdatedMs: Long = System.currentTimeMillis(),
    val sheetsRowIndex: Int = -1
)
