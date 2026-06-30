package com.apkarthik1986.jewelstockmanager.domain.model

/**
 * Represents the lifecycle status of a jewelry piece.
 * Weight contribution logic:
 * - AVAILABLE contributes to the box total jewel weight.
 * - SOLD and UNDER_REPAIR are excluded from the active weight total.
 * - UNDER_VALIDATION is treated as active (still in the box, pending confirmation).
 */
enum class ItemStatus(val label: String, val isWeightActive: Boolean) {
    AVAILABLE("Available", true),
    SOLD("Sold", false),
    UNDER_REPAIR("Under Repair", false),
    UNDER_VALIDATION("Under Validation", true);

    companion object {
        fun fromLabel(label: String): ItemStatus =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: AVAILABLE
    }
}
