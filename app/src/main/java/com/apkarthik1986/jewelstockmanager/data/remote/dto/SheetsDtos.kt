package com.apkarthik1986.jewelstockmanager.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Google Sheets API v4 response wrapper for spreadsheet values.
 */
data class SheetValuesResponse(
    @SerializedName("range") val range: String = "",
    @SerializedName("majorDimension") val majorDimension: String = "ROWS",
    @SerializedName("values") val values: List<List<String>> = emptyList()
)

data class BatchUpdateRequest(
    @SerializedName("valueInputOption") val valueInputOption: String = "USER_ENTERED",
    @SerializedName("data") val data: List<ValueRange>
)

data class ValueRange(
    @SerializedName("range") val range: String,
    @SerializedName("majorDimension") val majorDimension: String = "ROWS",
    @SerializedName("values") val values: List<List<String>>
)

data class BatchUpdateResponse(
    @SerializedName("spreadsheetId") val spreadsheetId: String = "",
    @SerializedName("totalUpdatedRows") val totalUpdatedRows: Int = 0
)
