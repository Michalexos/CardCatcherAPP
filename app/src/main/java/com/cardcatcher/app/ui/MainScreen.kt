package com.cardcatcher.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardcatcher.app.data.TransactionType
import com.cardcatcher.app.domain.StatsWindow
import java.time.LocalDate

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var totalPrice by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var type by remember { mutableStateOf(TransactionType.PURCHASE) }

    LaunchedEffect(state.catalog) {
        if (productName.isBlank() && state.catalog.isNotEmpty()) {
            productName = state.catalog.first().name
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("CardCatcher", style = MaterialTheme.typography.headlineMedium)
            Text("Lokálny sklad sealed Pokémon TCG produktov")
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Produkt") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter(Char::isDigit) },
                        label = { Text("Počet kusov") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = totalPrice,
                        onValueChange = { totalPrice = it },
                        label = { Text("Celková cena (€)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Dátum (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { type = TransactionType.PURCHASE }) {
                            Text(if (type == TransactionType.PURCHASE) "Nákup ✓" else "Nákup")
                        }
                        Button(onClick = { type = TransactionType.SALE }) {
                            Text(if (type == TransactionType.SALE) "Predaj ✓" else "Predaj")
                        }
                    }

                    Button(
                        onClick = {
                            val parsedDate = runCatching { LocalDate.parse(date) }.getOrElse { LocalDate.now() }
                            viewModel.addTransaction(
                                productName = productName,
                                quantity = quantity.toIntOrNull() ?: 0,
                                totalPriceEur = totalPrice.toDoubleOrNull() ?: -1.0,
                                date = parsedDate,
                                type = type
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Uložiť transakciu")
                    }

                    if (!message.isNullOrBlank()) {
                        Text(message.orEmpty())
                        LaunchedEffect(message) { viewModel.clearMessage() }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Štatistiky")
                    StatsWindow.entries.forEach { window ->
                        val stats = state.stats[window] ?: return@forEach
                        Text(
                            "${window.name}: výdavky €${"%.2f".format(stats.spentEur)} | " +
                                "tržby €${"%.2f".format(stats.revenueEur)} | " +
                                "zisk €${"%.2f".format(stats.profitEur)}"
                        )
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Aktuálny sklad")
                    state.positions.forEach { position ->
                        Text("${position.productName}: ${position.quantityInStock} ks")
                    }
                }
            }
        }

        item {
            Text("História transakcií")
        }

        items(state.transactions) { tx ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(tx.productName)
                    Text("${tx.type} | ${tx.quantity} ks | €${"%.2f".format(tx.totalPriceEur)} | ${LocalDate.ofEpochDay(tx.dateEpochDay)}")
                }
            }
        }
    }
}
