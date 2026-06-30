package com.apkarthik1986.jewelstockmanager.domain.usecase

import com.apkarthik1986.jewelstockmanager.domain.model.BoxWeightSummary
import com.apkarthik1986.jewelstockmanager.domain.model.JewelItem
import com.apkarthik1986.jewelstockmanager.domain.repository.JewelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Aggregates box metadata and items into a [BoxWeightSummary] plus item list.
 * Weight calculation rules:
 *  - AVAILABLE and UNDER_VALIDATION statuses contribute to totalJewelWeight.
 *  - SOLD and UNDER_REPAIR are excluded.
 */
class GetBoxItemsUseCase @Inject constructor(
    private val repository: JewelRepository
) {
    data class BoxContents(
        val summary: BoxWeightSummary,
        val items: List<JewelItem>
    )

    operator fun invoke(boxNumber: String): Flow<BoxContents> {
        val itemsFlow = repository.getItemsByBox(boxNumber)
        val boxesFlow = repository.getAllBoxes()

        return combine(itemsFlow, boxesFlow) { items, boxes ->
            val box = boxes.firstOrNull { it.boxNumber == boxNumber }
            val activeItems = items.filter { it.status.isWeightActive }
            val totalJewelWeight = activeItems.sumOf { it.weightGrams }
            val tareWeight = box?.tareWeightGrams ?: 0.0

            BoxContents(
                summary = BoxWeightSummary(
                    boxNumber = boxNumber,
                    category = box?.category ?: "",
                    tareWeightGrams = tareWeight,
                    totalJewelWeightGrams = totalJewelWeight,
                    grossTotalWeightGrams = tareWeight + totalJewelWeight,
                    activeItemCount = activeItems.size
                ),
                items = items.sortedBy { it.id }
            )
        }
    }
}
