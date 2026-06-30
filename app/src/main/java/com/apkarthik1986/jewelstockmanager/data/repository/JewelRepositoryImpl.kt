package com.apkarthik1986.jewelstockmanager.data.repository

import com.apkarthik1986.jewelstockmanager.BuildConfig
import com.apkarthik1986.jewelstockmanager.data.local.dao.BoxConfigDao
import com.apkarthik1986.jewelstockmanager.data.local.dao.JewelItemDao
import com.apkarthik1986.jewelstockmanager.data.local.entity.BoxConfigEntity
import com.apkarthik1986.jewelstockmanager.data.local.entity.JewelItemEntity
import com.apkarthik1986.jewelstockmanager.data.remote.SheetsApiService
import com.apkarthik1986.jewelstockmanager.data.remote.dto.BatchUpdateRequest
import com.apkarthik1986.jewelstockmanager.data.remote.dto.ValueRange
import com.apkarthik1986.jewelstockmanager.domain.model.BoxConfig
import com.apkarthik1986.jewelstockmanager.domain.model.ItemStatus
import com.apkarthik1986.jewelstockmanager.domain.model.JewelItem
import com.apkarthik1986.jewelstockmanager.domain.repository.JewelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [JewelRepository].
 *
 * Data flow (offline-first, mirrors LedgerViewer strategy):
 *  1. All UI reads are served from Room (reactive Flows).
 *  2. syncFromRemote() pulls Sheets → Room; called by WorkManager or manual refresh.
 *  3. Status changes are written to Room first (isDirty = true), then pushed
 *     to Sheets via pushPendingUpdates().
 */
@Singleton
class JewelRepositoryImpl @Inject constructor(
    private val jewelItemDao: JewelItemDao,
    private val boxConfigDao: BoxConfigDao,
    private val sheetsApi: SheetsApiService
) : JewelRepository {

    // ── Jewel Items ──────────────────────────────────────────────────────────

    override fun getItemsByBox(boxNumber: String): Flow<List<JewelItem>> =
        jewelItemDao.getItemsByBox(boxNumber).map { it.map(JewelItemEntity::toDomain) }

    override fun getItemsByCategory(category: String): Flow<List<JewelItem>> =
        jewelItemDao.getItemsByCategory(category).map { it.map(JewelItemEntity::toDomain) }

    override fun getAllItems(): Flow<List<JewelItem>> =
        jewelItemDao.getAllItems().map { it.map(JewelItemEntity::toDomain) }

    override suspend fun updateItemStatus(itemId: String, newStatus: ItemStatus) {
        jewelItemDao.updateStatus(
            itemId = itemId,
            status = newStatus.label,
            updatedMs = System.currentTimeMillis()
        )
    }

    override suspend fun upsertItems(items: List<JewelItem>) {
        jewelItemDao.upsertAll(items.map { JewelItemEntity.fromDomain(it) })
    }

    // ── Box Configs ──────────────────────────────────────────────────────────

    override fun getBoxesByCategory(category: String): Flow<List<BoxConfig>> =
        boxConfigDao.getBoxesByCategory(category).map { it.map(BoxConfigEntity::toDomain) }

    override fun getAllBoxes(): Flow<List<BoxConfig>> =
        boxConfigDao.getAllBoxes().map { it.map(BoxConfigEntity::toDomain) }

    override suspend fun upsertBoxConfigs(boxes: List<BoxConfig>) {
        boxConfigDao.upsertAll(boxes.map { BoxConfigEntity.fromDomain(it) })
    }

    // ── Sync ─────────────────────────────────────────────────────────────────

    override suspend fun syncFromRemote(): Result<Unit> = runCatching {
        val spreadsheetId = BuildConfig.SPREADSHEET_ID
        val apiKey = BuildConfig.SHEETS_API_KEY

        // Pull items tab
        val itemsResponse = sheetsApi.getValues(spreadsheetId, "Items!A2:J", apiKey)
        val remoteItems = itemsResponse.values.mapIndexedNotNull { rowIdx, row ->
            parseItemRow(row, rowIdx + 2) // +2: header row is row 1, data starts row 2
        }
        jewelItemDao.upsertAll(remoteItems)

        // Pull boxes tab
        val boxesResponse = sheetsApi.getValues(spreadsheetId, "Boxes!A2:G", apiKey)
        val remoteBoxes = boxesResponse.values.mapIndexedNotNull { rowIdx, row ->
            parseBoxRow(row, rowIdx + 2)
        }
        boxConfigDao.upsertAll(remoteBoxes)
    }

    override suspend fun pushPendingUpdates(): Result<Unit> = runCatching {
        val dirtyItems = jewelItemDao.getDirtyItems()
        if (dirtyItems.isEmpty()) return@runCatching

        val spreadsheetId = BuildConfig.SPREADSHEET_ID
        val apiKey = BuildConfig.SHEETS_API_KEY

        val valueRanges = dirtyItems.mapNotNull { item ->
            if (item.sheetsRowIndex < 2) return@mapNotNull null
            // Column F (index 6) is status in the Items sheet
            ValueRange(
                range = "Items!F${item.sheetsRowIndex}",
                values = listOf(listOf(item.status))
            )
        }

        if (valueRanges.isEmpty()) return@runCatching

        sheetsApi.batchUpdate(
            spreadsheetId = spreadsheetId,
            request = BatchUpdateRequest(data = valueRanges),
            apiKey = apiKey
        )

        // Mark items as clean after successful push
        dirtyItems.forEach { jewelItemDao.clearDirty(it.id) }
    }

    // ── Row Parsing Helpers ──────────────────────────────────────────────────

    /**
     * Parses a row from "Items" sheet.
     * Expected columns: id, name, category, boxNumber, weightGrams, status,
     *                   description, imageUrl, lastUpdatedMs, (rowIndex unused)
     */
    private fun parseItemRow(row: List<String>, sheetRowIndex: Int): JewelItemEntity? {
        if (row.size < 6) return null
        return try {
            JewelItemEntity(
                id = row[0].trim(),
                name = row[1].trim(),
                category = row[2].trim(),
                boxNumber = row[3].trim(),
                weightGrams = row[4].trim().toDoubleOrNull() ?: 0.0,
                status = ItemStatus.fromLabel(row[5].trim()).label,
                description = row.getOrElse(6) { "" }.trim(),
                imageUrl = row.getOrElse(7) { "" }.trim(),
                lastUpdatedMs = row.getOrElse(8) { "0" }.trim().toLongOrNull()
                    ?: System.currentTimeMillis(),
                sheetsRowIndex = sheetRowIndex,
                isDirty = false
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parses a row from "Boxes" sheet.
     * Expected columns: boxNumber, category, tareWeightGrams, isActive, location,
     *                   lastUpdatedMs, (rowIndex unused)
     */
    private fun parseBoxRow(row: List<String>, sheetRowIndex: Int): BoxConfigEntity? {
        if (row.size < 3) return null
        return try {
            BoxConfigEntity(
                boxNumber = row[0].trim(),
                category = row[1].trim(),
                tareWeightGrams = row[2].trim().toDoubleOrNull() ?: 0.0,
                isActive = row.getOrElse(3) { "true" }.trim().lowercase() != "false",
                location = row.getOrElse(4) { "" }.trim(),
                lastUpdatedMs = row.getOrElse(5) { "0" }.trim().toLongOrNull()
                    ?: System.currentTimeMillis(),
                sheetsRowIndex = sheetRowIndex
            )
        } catch (e: Exception) {
            null
        }
    }
}
