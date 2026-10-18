package com.cardcatcher.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.cardcatcher.app.data.AppDatabase
import com.cardcatcher.app.data.InventoryRepository
import com.cardcatcher.app.data.MIGRATION_1_2
import com.cardcatcher.app.ui.MainScreen
import com.cardcatcher.app.ui.MainViewModel
import com.cardcatcher.app.ui.theme.CardCatcherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "cardcatcher.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

        val repository = InventoryRepository(database.inventoryDao())

        setContent {
            val vm: MainViewModel = viewModel(
                factory = MainViewModel.Factory(repository)
            )
            CardCatcherTheme {
                MainScreen(viewModel = vm)
            }
        }
    }
}
