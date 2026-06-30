package com.apkarthik1986.jewelstockmanager.domain.usecase

import com.apkarthik1986.jewelstockmanager.domain.repository.JewelRepository
import javax.inject.Inject

/**
 * Triggers a full two-way sync:
 * 1. Pull latest rows from Google Sheets into Room DB.
 * 2. Push any locally pending mutations back to Sheets.
 */
class SyncInventoryUseCase @Inject constructor(
    private val repository: JewelRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        val pullResult = repository.syncFromRemote()
        if (pullResult.isFailure) return pullResult
        return repository.pushPendingUpdates()
    }
}
