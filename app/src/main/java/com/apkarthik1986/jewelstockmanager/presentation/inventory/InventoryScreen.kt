package com.apkarthik1986.jewelstockmanager.presentation.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkarthik1986.jewelstockmanager.R
import com.apkarthik1986.jewelstockmanager.presentation.components.JewelItemRow
import com.apkarthik1986.jewelstockmanager.presentation.components.WeightSummaryCard

/**
 * Inventory screen with:
 *  1. Category dropdown (primary selector)
 *  2. Dependent Box Number dropdown (filters by category)
 *  3. Weight Summary card (tare / jewel / gross)
 *  4. LazyColumn of items with inline status change
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBack: () -> Unit,
    initialCategory: String = "",
    initialBoxNumber: String = "",
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Apply nav args on first composition
    LaunchedEffect(initialCategory, initialBoxNumber) {
        if (initialCategory.isNotBlank()) viewModel.selectCategory(initialCategory)
        if (initialBoxNumber.isNotBlank()) viewModel.selectBox(initialBoxNumber)
    }

    LaunchedEffect(uiState.statusUpdateError) {
        uiState.statusUpdateError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.inventory_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Category dropdown ─────────────────────────────────────────
            item(key = "category_dropdown") {
                CategoryDropdown(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::selectCategory
                )
            }

            // ── Box number dropdown (dependent on category) ───────────────
            item(key = "box_dropdown") {
                BoxDropdown(
                    boxes = uiState.availableBoxes.map { it.boxNumber },
                    selectedBox = uiState.selectedBoxNumber,
                    enabled = uiState.selectedCategory.isNotBlank(),
                    onBoxSelected = viewModel::selectBox
                )
            }

            // ── Weight summary card ───────────────────────────────────────
            uiState.weightSummary?.let { summary ->
                item(key = "weight_summary") {
                    WeightSummaryCard(summary = summary)
                }
            }

            // ── Box loading indicator ─────────────────────────────────────
            if (uiState.isBoxLoading) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // ── Items list ────────────────────────────────────────────────
            if (!uiState.isBoxLoading && uiState.selectedBoxNumber.isNotBlank()) {
                if (uiState.boxItems.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_items_in_box),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    item(key = "items_header") {
                        Text(
                            text = "${uiState.boxItems.size} item${if (uiState.boxItems.size != 1) "s" else ""} in box",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(uiState.boxItems, key = { it.id }) { item ->
                        JewelItemRow(
                            item = item,
                            onStatusChange = { newStatus ->
                                viewModel.updateItemStatus(item.id, newStatus)
                            }
                        )
                    }
                    item(key = "bottom_spacer") { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// ── Dropdown helpers ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = selectedCategory.ifBlank { stringResource(R.string.select_category) },
            onValueChange = {},
            readOnly = true,
            label = { Text("Item Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        expanded = false
                        onCategorySelected(category)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxDropdown(
    boxes: List<String>,
    selectedBox: String,
    enabled: Boolean,
    onBoxSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = selectedBox.ifBlank {
                if (enabled) stringResource(R.string.select_box)
                else stringResource(R.string.select_category_first)
            },
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Box Number") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) }
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            if (boxes.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "No boxes for this category",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    onClick = { expanded = false }
                )
            } else {
                boxes.forEach { boxNum ->
                    DropdownMenuItem(
                        text = { Text("Box $boxNum") },
                        onClick = {
                            expanded = false
                            onBoxSelected(boxNum)
                        }
                    )
                }
            }
        }
    }
}
