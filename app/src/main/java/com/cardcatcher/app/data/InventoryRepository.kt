package com.cardcatcher.app.data

import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val dao: InventoryDao) {
    val transactions: Flow<List<InventoryTransactionEntity>> = dao.observeTransactions()
    val catalog: Flow<List<CatalogProductEntity>> = dao.observeCatalog()

    suspend fun seedCatalog() {
        dao.upsertCatalog(
            listOf(
                CatalogProductEntity("Scarlet & Violet: Prismatic Evolutions Super Premium Collection", 220.0),
                CatalogProductEntity("151 Elite Trainer Box", 69.0),
                CatalogProductEntity("Paldean Fates Tin", 29.0),
                CatalogProductEntity("Surging Sparks Booster Box", 129.0)
            )
        )
    }

    suspend fun addTransaction(
        productName: String,
        quantity: Int,
        totalPriceEur: Double,
        dateEpochDay: Long,
        type: TransactionType
    ) {
        dao.insertTransaction(
            InventoryTransactionEntity(
                productName = productName,
                quantity = quantity,
                totalPriceEur = totalPriceEur,
                dateEpochDay = dateEpochDay,
                type = type
            )
        )
    }

    suspend fun deleteTransaction(transactionId: Long) {
        dao.deleteTransaction(transactionId)
    }

    suspend fun addCatalogProduct(productName: String, marketPriceEur: Double) {
        dao.upsertCatalogProduct(
            CatalogProductEntity(
                name = productName,
                marketPriceEur = marketPriceEur
            )
        )
    }
}
