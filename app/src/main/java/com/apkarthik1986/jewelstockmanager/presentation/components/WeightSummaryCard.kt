package com.apkarthik1986.jewelstockmanager.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkarthik1986.jewelstockmanager.domain.model.BoxWeightSummary

/**
 * Displays the three weight metrics for a selected box:
 * Tare Weight | Total Jewel Weight | Gross Total
 */
@Composable
fun WeightSummaryCard(
    summary: BoxWeightSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Box ${summary.boxNumber} — ${summary.category}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeightMetric(
                    label = "Tare Weight",
                    valueGrams = summary.tareWeightGrams
                )
                WeightMetric(
                    label = "Jewel Weight",
                    valueGrams = summary.totalJewelWeightGrams
                )
                WeightMetric(
                    label = "Gross Total",
                    valueGrams = summary.grossTotalWeightGrams,
                    emphasized = true
                )
            }
        }
    }
}

@Composable
private fun WeightMetric(
    label: String,
    valueGrams: Double,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "%.2f g".format(valueGrams),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}
