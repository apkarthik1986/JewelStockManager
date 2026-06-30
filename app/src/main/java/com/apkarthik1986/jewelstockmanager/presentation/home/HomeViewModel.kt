package com.apkarthik1986.jewelstockmanager.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkarthik1986.jewelstockmanager.domain.model.BoxConfig
import com.apkarthik1986.jewelstockmanager.domain.model.JewelItem
import com.apkarthik1986.jewelstockmanager.domain.repository.JewelRepository
import com.apkarthik1986.jewelstockmanager.domain.usecase.SyncInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val items: List<JewelItem> = emptyList(),
    val boxes: List<BoxConfig> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val lastSyncTimeMs: Long? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: JewelRepository,
    private val syncInventoryUseCase: SyncInventoryUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _syncError = MutableStateFlow<String?>(null)
    private val _lastSyncTimeMs = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllItems(),
        repository.getAllBoxes(),
        _isSyncing,
        _syncError,
        _lastSyncTimeMs
    ) { items, boxes, isSyncing, syncError, lastSync ->
        HomeUiState(
            items = items,
            boxes = boxes,
            isLoading = false,
            isSyncing = isSyncing,
            syncError = syncError,
            lastSyncTimeMs = lastSync
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    /**
     * Manual pull-to-refresh sync — enqueues a one-shot WorkManager task so
     * sync survives app backgrounding (mirrors LedgerViewer's refresh strategy).
     */
    fun refresh() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            val result = syncInventoryUseCase()
            _isSyncing.value = false
            if (result.isSuccess) {
                _lastSyncTimeMs.value = System.currentTimeMillis()
            } else {
                _syncError.value = result.exceptionOrNull()?.message ?: "Sync failed"
            }
        }
    }
}
