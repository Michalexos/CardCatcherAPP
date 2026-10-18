package com.cardcatcher.app.domain

import com.cardcatcher.app.data.InventoryTransactionEntity
import com.cardcatcher.app.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StatsCalculatorTest {

    @Test
    fun `calculates all time and today stats`() {
        val now = LocalDate.of(2026, 10, 18)
        val tx = listOf(
            InventoryTransactionEntity(
                productName = "Prismatic Evolutions SPC",
                quantity = 3,
                totalPriceEur = 146.0,
                dateEpochDay = now.toEpochDay(),
                type = TransactionType.PURCHASE
            ),
            InventoryTransactionEntity(
                productName = "Prismatic Evolutions SPC",
                quantity = 1,
                totalPriceEur = 90.0,
                dateEpochDay = now.toEpochDay(),
                type = TransactionType.SALE
            )
        )

        val stats = StatsCalculator.build(tx, now)

        assertEquals(146.0, stats.getValue(StatsWindow.TODAY).spentEur, 0.001)
        assertEquals(90.0, stats.getValue(StatsWindow.TODAY).revenueEur, 0.001)
        assertEquals(-56.0, stats.getValue(StatsWindow.ALL_TIME).profitEur, 0.001)
    }

    @Test
    fun `three month window excludes older transactions`() {
        val now = LocalDate.of(2026, 10, 18)
        val tx = listOf(
            InventoryTransactionEntity(
                productName = "151 ETB",
                quantity = 2,
                totalPriceEur = 100.0,
                dateEpochDay = now.minusMonths(1).toEpochDay(),
                type = TransactionType.PURCHASE
            ),
            InventoryTransactionEntity(
                productName = "151 ETB",
                quantity = 1,
                totalPriceEur = 70.0,
                dateEpochDay = now.minusMonths(4).toEpochDay(),
                type = TransactionType.SALE
            )
        )

        val stats = StatsCalculator.build(tx, now)

        assertEquals(100.0, stats.getValue(StatsWindow.THREE_MONTHS).spentEur, 0.001)
        assertEquals(0.0, stats.getValue(StatsWindow.THREE_MONTHS).revenueEur, 0.001)
        assertEquals(70.0, stats.getValue(StatsWindow.ALL_TIME).revenueEur, 0.001)
    }
}
