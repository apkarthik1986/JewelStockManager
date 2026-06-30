package com.apkarthik1986.jewelstockmanager.domain.usecase

import com.apkarthik1986.jewelstockmanager.domain.model.BoxConfig
import com.apkarthik1986.jewelstockmanager.domain.repository.JewelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns the list of active boxes for the chosen category.
 * Feeds the dependent dropdown on the Inventory screen.
 */
class GetBoxesForCategoryUseCase @Inject constructor(
    private val repository: JewelRepository
) {
    operator fun invoke(category: String): Flow<List<BoxConfig>> =
        repository.getBoxesByCategory(category)
}
