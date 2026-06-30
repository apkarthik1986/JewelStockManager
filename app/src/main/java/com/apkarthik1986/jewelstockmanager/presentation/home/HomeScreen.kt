package com.apkarthik1986.jewelstockmanager.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkarthik1986.jewelstockmanager.R
import com.apkarthik1986.jewelstockmanager.domain.model.BoxConfig
import com.apkarthik1986.jewelstockmanager.presentation.components.StatusBadge
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusAvailable
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusRepair

/**
 * Home screen — overview of all categories and boxes with sync status.
 * TopAppBar with cloud sync indicator; pull-to-refresh triggers a manual sync.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onBoxSelected: (category: String, boxNumber: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()

    // Show error in snackbar
    LaunchedEffect(uiState.syncError) {
        uiState.syncError?.let { snackbarHostState.showSnackbar(it) }
    }

    // Tie pull-to-refresh to ViewModel
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(Unit) { viewModel.refresh() }
    }
    LaunchedEffect(uiState.isSyncing) {
        if (!uiState.isSyncing) pullRefreshState.endRefresh()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    SyncStatusIndicator(
                        isSyncing = uiState.isSyncing,
                        hasError = uiState.syncError != null
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val groupedBoxes = uiState.boxes
                    .filter { it.isActive }
                    .groupBy { it.category }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (groupedBoxes.isEmpty()) {
                        item {
                            EmptyInventoryHint()
                        }
                    } else {
                        groupedBoxes.forEach { (category, boxes) ->
                            item(key = "header_$category") {
                                CategoryHeader(category = category, boxCount = boxes.size)
                            }
                            items(boxes, key = { it.boxNumber }) { box ->
                                BoxSummaryRow(
                                    box = box,
                                    itemCount = uiState.items.count { it.boxNumber == box.boxNumber },
                                    onClick = { onBoxSelected(category, box.boxNumber) }
                                )
                            }
                            item(key = "spacer_$category") { Spacer(Modifier.height(4.dp)) }
                        }
                    }
                }
            }

            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun SyncStatusIndicator(isSyncing: Boolean, hasError: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 12.dp)
    ) {
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Syncing…",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                imageVector = if (hasError) Icons.Default.CloudOff else Icons.Default.Cloud,
                contentDescription = if (hasError) "Sync error" else "Synced",
                tint = if (hasError) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun CategoryHeader(category: String, boxCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "$boxCount box${if (boxCount != 1) "es" else ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxSummaryRow(
    box: BoxConfig,
    itemCount: Int,
    onClick: () -> Unit
) {
    androidx.compose.material3.ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Box ${box.boxNumber}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (box.location.isNotBlank()) {
                    Text(
                        text = box.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$itemCount items",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Tare: ${"%.1f".format(box.tareWeightGrams)}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun EmptyInventoryHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "No inventory data",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = "Pull down to sync from Google Sheets",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}
