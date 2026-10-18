package com.cardcatcher.app.domain

import com.cardcatcher.app.data.InventoryTransactionEntity
import com.cardcatcher.app.data.TransactionType

data class ProductPosition(
    val productName: String,
    val quantityInStock: Int,
    val totalSpentEur: Double,
    val totalRevenueEur: Double
)

object InventorySummary {
    fun currentPositions(transactions: List<InventoryTransactionEntity>): List<ProductPosition> {
        val grouped = transactions.groupBy { it.productName }
        return grouped.map { (name, txs) ->
            val purchasedUnits = txs.filter { it.type == TransactionType.PURCHASE }.sumOf { it.quantity }
            val soldUnits = txs.filter { it.type == TransactionType.SALE }.sumOf { it.quantity }
            ProductPosition(
                productName = name,
                quantityInStock = purchasedUnits - soldUnits,
                totalSpentEur = txs.filter { it.type == TransactionType.PURCHASE }.sumOf { it.totalPriceEur },
                totalRevenueEur = txs.filter { it.type == TransactionType.SALE }.sumOf { it.totalPriceEur }
            )
        }.sortedBy { it.productName }
    }
}
