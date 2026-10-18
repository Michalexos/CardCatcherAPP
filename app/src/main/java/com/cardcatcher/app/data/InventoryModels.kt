package com.cardcatcher.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    PURCHASE,
    SALE
}

@Entity(tableName = "transactions")
data class InventoryTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val quantity: Int,
    val totalPriceEur: Double,
    val dateEpochDay: Long,
    val type: TransactionType,
    val imageUri: String? = null
)
