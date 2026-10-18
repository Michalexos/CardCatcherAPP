package com.cardcatcher.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [InventoryTransactionEntity::class, CatalogProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
}
