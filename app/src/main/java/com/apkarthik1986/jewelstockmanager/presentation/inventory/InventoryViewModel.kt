package com.apkarthik1986.jewelstockmanager.presentation.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkarthik1986.jewelstockmanager.domain.model.BoxConfig
import com.apkarthik1986.jewelstockmanager.domain.model.BoxWeightSummary
import com.apkarthik1986.jewelstockmanager.domain.model.ItemStatus
import com.apkarthik1986.jewelstockmanager.domain.model.JewelItem
import com.apkarthik1986.jewelstockmanager.domain.usecase.GetBoxItemsUseCase
import com.apkarthik1986.jewelstockmanager.domain.usecase.GetBoxesForCategoryUseCase
import com.apkarthik1986.jewelstockmanager.domain.usecase.UpdateItemStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Known jewelry categories. In a production app these would be driven by
 * the BoxConfig data from the repository. Provided here as sensible defaults.
 */
val JEWELRY_CATEGORIES = listOf("Rings", "Necklaces", "Bangles", "Earrings", "Bracelets", "Pendants")

data class InventoryUiState(
    val categories: List<String> = JEWELRY_CATEGORIES,
    val selectedCategory: String = "",
    val availableBoxes: List<BoxConfig> = emptyList(),
    val selectedBoxNumber: String = "",
    val weightSummary: BoxWeightSummary? = null,
    val boxItems: List<JewelItem> = emptyList(),
    val isBoxLoading: Boolean = false,
    val statusUpdateError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBoxesForCategoryUseCase: GetBoxesForCategoryUseCase,
    private val getBoxItemsUseCase: GetBoxItemsUseCase,
    private val updateItemStatusUseCase: UpdateItemStatusUseCase
) : ViewModel() {

    // Restore from nav args if navigated directly to a box
    private val _selectedCategory = MutableStateFlow(
        savedStateHandle.get<String>("category") ?: ""
    )
    private val _selectedBoxNumber = MutableStateFlow(
        savedStateHandle.get<String>("boxNumber") ?: ""
    )
    private val _statusUpdateError = MutableStateFlow<String?>(null)

    // Dynamic box list for the chosen category
    private val _availableBoxes: StateFlow<List<BoxConfig>> = _selectedCategory
        .flatMapLatest { category ->
            if (category.isBlank()) flowOf(emptyList())
            else getBoxesForCategoryUseCase(category)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Box contents (weight summary + item list) for the selected box
    private val _boxContents = _selectedBoxNumber
        .flatMapLatest { boxNum ->
            if (boxNum.isBlank()) flowOf(null)
            else getBoxItemsUseCase(boxNum).map { it as GetBoxItemsUseCase.BoxContents? }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<InventoryUiState> = combine(
        _selectedCategory,
        _availableBoxes,
        _selectedBoxNumber,
        _boxContents,
        _statusUpdateError
    ) { category, boxes, boxNum, contents, error ->
        InventoryUiState(
            selectedCategory = category,
            availableBoxes = boxes,
            selectedBoxNumber = boxNum,
            weightSummary = contents?.summary,
            boxItems = contents?.items ?: emptyList(),
            isBoxLoading = boxNum.isNotBlank() && contents == null,
            statusUpdateError = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryUiState()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _selectedBoxNumber.value = ""  // Reset box when category changes
    }

    fun selectBox(boxNumber: String) {
        _selectedBoxNumber.value = boxNumber
    }

    /**
     * Optimistic status update:
     *  1. Room DB updated immediately → Flow emits → UI reflects weight change instantly.
     *  2. WorkManager will push the dirty row to Sheets when network is available.
     */
    fun updateItemStatus(itemId: String, newStatus: ItemStatus) {
        viewModelScope.launch {
            _statusUpdateError.value = null
            try {
                updateItemStatusUseCase(itemId, newStatus)
            } catch (e: Exception) {
                _statusUpdateError.value = "Failed to update status: ${e.message}"
            }
        }
    }

    fun dismissError() {
        _statusUpdateError.value = null
    }
}
