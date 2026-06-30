package com.apkarthik1986.jewelstockmanager.domain.usecase

import com.apkarthik1986.jewelstockmanager.domain.model.ItemStatus
import com.apkarthik1986.jewelstockmanager.domain.repository.JewelRepository
import javax.inject.Inject

/**
 * Updates an item's status and propagates the change:
 *  1. Writes to local Room DB immediately (optimistic update).
 *  2. Queues a WorkManager task to sync the change back to Google Sheets.
 */
class UpdateItemStatusUseCase @Inject constructor(
    private val repository: JewelRepository
) {
    suspend operator fun invoke(itemId: String, newStatus: ItemStatus) {
        repository.updateItemStatus(itemId, newStatus)
    }
}
