package com.cardcatcher.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cardcatcher.app.data.CatalogProductEntity
import com.cardcatcher.app.data.InventoryRepository
import com.cardcatcher.app.data.InventoryTransactionEntity
import com.cardcatcher.app.data.TransactionType
import com.cardcatcher.app.domain.InventorySummary
import com.cardcatcher.app.domain.PeriodStats
import com.cardcatcher.app.domain.ProductPosition
import com.cardcatcher.app.domain.StatsCalculator
import com.cardcatcher.app.domain.StatsWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    val state: StateFlow<UiState> = combine(
        repository.catalog,
        repository.transactions
    ) { catalog, transactions ->
        UiState(
            catalog = catalog,
            transactions = transactions,
            positions = InventorySummary.currentPositions(transactions),
            stats = StatsCalculator.build(transactions)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState()
    )

    init {
        viewModelScope.launch {
            repository.seedCatalog()
        }
    }

    fun addTransaction(
        productName: String,
        quantity: Int,
        totalPriceEur: Double,
        date: LocalDate,
        type: TransactionType
    ) {
        if (productName.isBlank() || quantity <= 0 || totalPriceEur < 0.0) {
            _message.value = "Skontroluj hodnoty formulára"
            return
        }

        viewModelScope.launch {
            repository.addTransaction(
                productName = productName,
                quantity = quantity,
                totalPriceEur = totalPriceEur,
                dateEpochDay = date.toEpochDay(),
                type = type
            )
            _message.value = "Transakcia uložená"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}

data class UiState(
    val catalog: List<CatalogProductEntity> = emptyList(),
    val transactions: List<InventoryTransactionEntity> = emptyList(),
    val positions: List<ProductPosition> = emptyList(),
    val stats: Map<StatsWindow, PeriodStats> = emptyMap()
)
