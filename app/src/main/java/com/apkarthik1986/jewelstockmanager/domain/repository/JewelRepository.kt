package com.apkarthik1986.jewelstockmanager.domain.repository

import com.apkarthik1986.jewelstockmanager.domain.model.BoxConfig
import com.apkarthik1986.jewelstockmanager.domain.model.ItemStatus
import com.apkarthik1986.jewelstockmanager.domain.model.JewelItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining the contract between domain and data layers.
 * Mirrors the LedgerViewer repository abstraction pattern.
 */
interface JewelRepository {

    // ── Jewel Items ──────────────────────────────────────────────────────────

    /** Stream all items for a given box number. */
    fun getItemsByBox(boxNumber: String): Flow<List<JewelItem>>

    /** Stream all items for a given category. */
    fun getItemsByCategory(category: String): Flow<List<JewelItem>>

    /** Stream all jewelry items. */
    fun getAllItems(): Flow<List<JewelItem>>

    /** Update the status of a single item (local + queues remote write). */
    suspend fun updateItemStatus(itemId: String, newStatus: ItemStatus)

    /** Upsert items from remote into local cache. */
    suspend fun upsertItems(items: List<JewelItem>)

    // ── Box Configs ──────────────────────────────────────────────────────────

    /** Stream boxes filtered by category. */
    fun getBoxesByCategory(category: String): Flow<List<BoxConfig>>

    /** Stream all active boxes. */
    fun getAllBoxes(): Flow<List<BoxConfig>>

    /** Upsert box configs from remote into local cache. */
    suspend fun upsertBoxConfigs(boxes: List<BoxConfig>)

    // ── Sync ─────────────────────────────────────────────────────────────────

    /**
     * Pull fresh data from Google Sheets and populate the local Room cache.
     * Returns true if sync succeeded.
     */
    suspend fun syncFromRemote(): Result<Unit>

    /**
     * Push any pending local mutations back to the Google Sheets spreadsheet.
     */
    suspend fun pushPendingUpdates(): Result<Unit>
}
