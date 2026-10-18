package com.cardcatcher.app.data

import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val dao: InventoryDao) {
    val transactions: Flow<List<InventoryTransactionEntity>> = dao.observeTransactions()
    val knownProducts: Flow<List<String>> = dao.observeKnownProducts()

    suspend fun addTransaction(
        productName: String,
        quantity: Int,
        totalPriceEur: Double,
        dateEpochDay: Long,
        type: TransactionType,
        imageUri: String?
    ) {
        dao.insertTransaction(
            InventoryTransactionEntity(
                productName = productName,
                quantity = quantity,
                totalPriceEur = totalPriceEur,
                dateEpochDay = dateEpochDay,
                type = type,
                imageUri = imageUri
            )
        )
    }

    suspend fun deleteTransaction(transactionId: Long) {
        dao.deleteTransaction(transactionId)
    }

    suspend fun getImageUriForProduct(productName: String): String? =
        dao.getImageUriForProduct(productName)
}
