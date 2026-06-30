package com.apkarthik1986.jewelstockmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.apkarthik1986.jewelstockmanager.presentation.navigation.JewelNavGraph
import com.apkarthik1986.jewelstockmanager.presentation.theme.JewelStockManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JewelStockManagerTheme {
                JewelNavGraph()
            }
        }
    }
}
