package com.apkarthik1986.jewelstockmanager.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkarthik1986.jewelstockmanager.domain.model.ItemStatus
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusAvailable
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusAvailableContainer
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusRepair
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusRepairContainer
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusSold
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusSoldContainer
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusValidation
import com.apkarthik1986.jewelstockmanager.presentation.theme.StatusValidationContainer

/**
 * Color-coded pill badge that renders an item's current lifecycle status.
 * Green = Available, Red = Sold, Orange = Repair, Blue = Validation.
 */
@Composable
fun StatusBadge(
    status: ItemStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        ItemStatus.AVAILABLE -> StatusAvailableContainer to StatusAvailable
        ItemStatus.SOLD -> StatusSoldContainer to StatusSold
        ItemStatus.UNDER_REPAIR -> StatusRepairContainer to StatusRepair
        ItemStatus.UNDER_VALIDATION -> StatusValidationContainer to StatusValidation
    }

    Text(
        text = status.label,
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    )
}
