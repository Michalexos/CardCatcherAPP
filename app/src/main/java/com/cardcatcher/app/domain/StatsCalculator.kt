package com.cardcatcher.app.domain

import com.cardcatcher.app.data.InventoryTransactionEntity
import com.cardcatcher.app.data.TransactionType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class StatsWindow {
    TODAY,
    WEEK,
    MONTH,
    THREE_MONTHS,
    YEAR,
    ALL_TIME
}

data class PeriodStats(
    val spentEur: Double,
    val revenueEur: Double,
    val profitEur: Double,
    val purchasedUnits: Int,
    val soldUnits: Int
)

object StatsCalculator {
    fun build(
        transactions: List<InventoryTransactionEntity>,
        now: LocalDate = LocalDate.now()
    ): Map<StatsWindow, PeriodStats> {
        val withDate = transactions.map { it to LocalDate.ofEpochDay(it.dateEpochDay) }

        val starts = mapOf(
            StatsWindow.TODAY to now,
            StatsWindow.WEEK to now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            StatsWindow.MONTH to now.withDayOfMonth(1),
            StatsWindow.THREE_MONTHS to now.minusMonths(2).withDayOfMonth(1),
            StatsWindow.YEAR to now.withDayOfYear(1)
        )

        return StatsWindow.entries.associateWith { window ->
            val scoped = if (window == StatsWindow.ALL_TIME) {
                withDate
            } else {
                withDate.filter { (_, date) -> !date.isBefore(starts.getValue(window)) }
            }
            scoped.toStats()
        }
    }

    private fun List<Pair<InventoryTransactionEntity, LocalDate>>.toStats(): PeriodStats {
        var spent = 0.0
        var revenue = 0.0
        var purchased = 0
        var sold = 0

        for ((tx, _) in this) {
            when (tx.type) {
                TransactionType.PURCHASE -> {
                    spent += tx.totalPriceEur
                    purchased += tx.quantity
                }

                TransactionType.SALE -> {
                    revenue += tx.totalPriceEur
                    sold += tx.quantity
                }
            }
        }

        return PeriodStats(
            spentEur = spent,
            revenueEur = revenue,
            profitEur = revenue - spent,
            purchasedUnits = purchased,
            soldUnits = sold
        )
    }
}
