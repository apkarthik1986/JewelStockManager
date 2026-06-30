package com.apkarthik1986.jewelstockmanager.domain.model

/**
 * Aggregated weight summary for a specific box.
 * Computed in real-time from the active items inside the box.
 */
data class BoxWeightSummary(
    val boxNumber: String,
    val category: String,
    val tareWeightGrams: Double,
    val totalJewelWeightGrams: Double,
    val grossTotalWeightGrams: Double = tareWeightGrams + totalJewelWeightGrams,
    val activeItemCount: Int
)
