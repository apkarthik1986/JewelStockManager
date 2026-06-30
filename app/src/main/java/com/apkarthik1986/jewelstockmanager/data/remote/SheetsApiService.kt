package com.apkarthik1986.jewelstockmanager.data.remote

import com.apkarthik1986.jewelstockmanager.data.remote.dto.BatchUpdateRequest
import com.apkarthik1986.jewelstockmanager.data.remote.dto.BatchUpdateResponse
import com.apkarthik1986.jewelstockmanager.data.remote.dto.SheetValuesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service for Google Sheets API v4.
 *
 * Sheets tabs / named ranges expected in the spreadsheet:
 *  - "Items!A:J"  → jewel items (id, name, category, boxNumber, weight, status, description, imageUrl, lastUpdated, rowIndex)
 *  - "Boxes!A:G"  → box configs (boxNumber, category, tareWeight, isActive, location, lastUpdated, rowIndex)
 *
 * Authentication:
 *  - Read: API key (public sheets)
 *  - Write: ****** token injected via OkHttp interceptor
 */
interface SheetsApiService {

    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("key") apiKey: String
    ): SheetValuesResponse

    @POST("v4/spreadsheets/{spreadsheetId}/values:batchUpdate")
    suspend fun batchUpdate(
        @Path("spreadsheetId") spreadsheetId: String,
        @Body request: BatchUpdateRequest,
        @Query("key") apiKey: String
    ): BatchUpdateResponse
}
