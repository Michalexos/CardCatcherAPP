package com.cardcatcher.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardcatcher.app.data.CatalogProductEntity
import com.cardcatcher.app.data.InventoryTransactionEntity
import com.cardcatcher.app.data.TransactionType
import com.cardcatcher.app.domain.PeriodStats
import com.cardcatcher.app.domain.ProductPosition
import com.cardcatcher.app.domain.StatsWindow
import com.cardcatcher.app.ui.theme.Loss
import com.cardcatcher.app.ui.theme.Mint
import com.cardcatcher.app.ui.theme.Navy
import com.cardcatcher.app.ui.theme.Pink
import com.cardcatcher.app.ui.theme.Profit
import com.cardcatcher.app.ui.theme.Purple
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Currency
import java.util.Locale

private val slovakLocale = Locale("sk", "SK")
private val eurFormatter: NumberFormat = NumberFormat.getCurrencyInstance(slovakLocale).apply {
    currency = Currency.getInstance("EUR")
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

private enum class MainTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Prehľad", Icons.Default.ShowChart),
    INVENTORY("Sklad", Icons.Default.Inventory2),
    TRANSACTIONS("Transakcie", Icons.Default.ListAlt),
    CATALOG("Katalóg", Icons.Default.Storefront)
}

private enum class PricingMode(val label: String) {
    PER_PIECE("Cena za kus"),
    TOTAL("Celková cena")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.OVERVIEW) }
    var selectedWindow by rememberSaveable { mutableStateOf(StatsWindow.MONTH) }
    var openTransactionSheet by rememberSaveable { mutableStateOf(false) }
    var selectedProductDetail by rememberSaveable { mutableStateOf<String?>(null) }
    var forcedType by rememberSaveable { mutableStateOf<TransactionType?>(null) }

    val positionByName = remember(state.positions) { state.positions.associateBy { it.productName } }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message.orEmpty())
            viewModel.clearMessage()
        }
    }

    if (selectedProductDetail != null) {
        ProductDetailScreen(
            productName = selectedProductDetail.orEmpty(),
            catalog = state.catalog,
            positions = positionByName,
            transactions = state.transactions,
            onBack = { selectedProductDetail = null },
            onAddPurchase = {
                forcedType = TransactionType.PURCHASE
                openTransactionSheet = true
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Navy,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (selectedTab == MainTab.TRANSACTIONS) {
                    FloatingActionButton(
                        onClick = {
                            forcedType = null
                            openTransactionSheet = true
                        },
                        containerColor = Pink,
                        contentColor = Purple
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Pridať transakciu")
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Purple) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = Purple,
                                selectedTextColor = Pink,
                                indicatorColor = Pink,
                                unselectedIconColor = Mint,
                                unselectedTextColor = Mint
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                MainTab.OVERVIEW -> OverviewScreen(
                    modifier = Modifier.padding(padding),
                    state = state,
                    selectedWindow = selectedWindow,
                    onWindowSelected = { selectedWindow = it }
                )

                MainTab.INVENTORY -> InventoryScreen(
                    modifier = Modifier.padding(padding),
                    catalog = state.catalog,
                    positions = positionByName,
                    onOpenDetail = { selectedProductDetail = it }
                )

                MainTab.TRANSACTIONS -> TransactionsScreen(
                    modifier = Modifier.padding(padding),
                    transactions = state.transactions,
                    onDelete = viewModel::deleteTransaction
                )

                MainTab.CATALOG -> CatalogScreen(
                    modifier = Modifier.padding(padding),
                    catalog = state.catalog,
                    onAddProduct = viewModel::addCatalogProduct
                )
            }
        }
    }

    if (openTransactionSheet) {
        AddTransactionSheet(
            catalog = state.catalog,
            initialProductName = selectedProductDetail,
            initialType = forcedType ?: TransactionType.PURCHASE,
            onDismiss = { openTransactionSheet = false },
            onSave = { product, quantity, totalPrice, date, type ->
                viewModel.addTransaction(product, quantity, totalPrice, date, type)
                openTransactionSheet = false
            }
        )
    }
}

@Composable
private fun OverviewScreen(
    modifier: Modifier,
    state: UiState,
    selectedWindow: StatsWindow,
    onWindowSelected: (StatsWindow) -> Unit
) {
    val stats = state.stats[selectedWindow] ?: PeriodStats(0.0, 0.0, 0.0, 0, 0)
    val allTime = state.stats[StatsWindow.ALL_TIME] ?: PeriodStats(0.0, 0.0, 0.0, 0, 0)

    val inventoryValue = state.positions.sumOf { position ->
        val market = state.catalog.firstOrNull { it.name == position.productName }?.marketPriceEur ?: 0.0
        position.quantityInStock.coerceAtLeast(0) * market
    }
    val netProfit = inventoryValue + allTime.revenueEur - allTime.spentEur
    val profitPercent = if (allTime.spentEur > 0.0) (netProfit / allTime.spentEur) * 100.0 else 0.0

    val chartPoints = remember(state.transactions, selectedWindow) {
        buildChartPoints(state.transactions, selectedWindow)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DashboardHeader()
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Purple),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Hodnota skladu", style = MaterialTheme.typography.titleLarge, color = Mint)
                    Text(formatCurrency(inventoryValue), style = MaterialTheme.typography.headlineMedium, color = Mint)
                    Text(
                        "Celkový zisk: ${formatCurrency(netProfit)} (${formatPercent(profitPercent)})",
                        color = if (netProfit >= 0) Profit else Loss
                    )
                    PerformanceChart(points = chartPoints)
                }
            }
        }

        item {
            PeriodSelector(selected = selectedWindow, onSelected = onWindowSelected)
        }

        item {
            StatsGrid(stats = stats)
        }
    }
}

@Composable
private fun DashboardHeader() {
    val logoPainter = rememberAssetPainter("design/logo-2.png")
    val patternPainter = rememberAssetPainter("design/pozadie.png")

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Purple),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (patternPainter != null) {
                Image(
                    painter = patternPainter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .alpha(0.18f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (logoPainter != null) {
                    Image(
                        painter = logoPainter,
                        contentDescription = "Logo CardCatcher",
                        modifier = Modifier.size(58.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Pink),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Catcher(), contentDescription = null, tint = Purple)
                    }
                }
                Column {
                    Text("CardCatcher", color = Mint, style = MaterialTheme.typography.headlineMedium)
                    Text("Portfólio sealed TCG", color = Mint.copy(alpha = 0.9f))
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(selected: StatsWindow, onSelected: (StatsWindow) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        StatsWindow.entries.forEachIndexed { index, window ->
            SegmentedButton(
                shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = StatsWindow.entries.size
                ),
                selected = selected == window,
                onClick = { onSelected(window) },
                label = {
                    Text(
                        text = windowLabel(window),
                        maxLines = 1
                    )
                },
                colors = androidx.compose.material3.SegmentedButtonDefaults.colors(
                    activeContainerColor = Pink,
                    activeContentColor = Purple,
                    inactiveContainerColor = Purple,
                    inactiveContentColor = Mint
                )
            )
        }
    }
}

@Composable
private fun StatsGrid(stats: PeriodStats) {
    val invested = (stats.spentEur - stats.revenueEur).coerceAtLeast(0.0)
    val cards = listOf(
        Triple("Investované", invested, Mint),
        Triple("Zisk (realizovaný)", stats.profitEur, if (stats.profitEur >= 0) Profit else Loss),
        Triple("Výdavky", stats.spentEur, Mint),
        Triple("Tržby", stats.revenueEur, Mint)
    )

    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cards.forEach { (title, value, color) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(110.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Purple)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, color = Mint, style = MaterialTheme.typography.bodyMedium)
                    Text(formatCurrency(value), color = color, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun PerformanceChart(points: List<Double>) {
    val max = points.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val min = points.minOrNull()?.coerceAtMost(0.0) ?: 0.0
    val range = (max - min).coerceAtLeast(1.0)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (points.size < 2) {
            Text(
                text = "Málo dát pre graf",
                color = Mint,
                modifier = Modifier.padding(12.dp)
            )
        } else {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(8.dp)) {
                val stepX = if (points.size > 1) size.width / (points.size - 1) else 0f
                for (index in 0 until points.lastIndex) {
                    val x1 = index * stepX
                    val x2 = (index + 1) * stepX
                    val y1 = size.height - (((points[index] - min) / range) * size.height).toFloat()
                    val y2 = size.height - (((points[index + 1] - min) / range) * size.height).toFloat()
                    drawLine(
                        color = if (points.last() >= points.first()) Profit else Loss,
                        start = androidx.compose.ui.geometry.Offset(x1, y1),
                        end = androidx.compose.ui.geometry.Offset(x2, y2),
                        strokeWidth = 6f
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryScreen(
    modifier: Modifier,
    catalog: List<CatalogProductEntity>,
    positions: Map<String, ProductPosition>,
    onOpenDetail: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var sortByValue by rememberSaveable { mutableStateOf(true) }
    var sealedOnly by rememberSaveable { mutableStateOf(false) }

    val rows = remember(catalog, positions, query, sortByValue, sealedOnly) {
        catalog.map { product ->
            val position = positions[product.name]
            val quantity = position?.quantityInStock?.coerceAtLeast(0) ?: 0
            val isSealed = isSealedName(product.name)
            InventoryRow(
                name = product.name,
                marketPrice = product.marketPriceEur,
                quantity = quantity,
                value = quantity * product.marketPriceEur,
                sealed = isSealed
            )
        }
            .filter { row -> row.name.contains(query.trim(), ignoreCase = true) }
            .filter { row -> !sealedOnly || row.sealed }
            .sortedByDescending { if (sortByValue) it.value else it.quantity.toDouble() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sklad", color = Mint, style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Hľadať produkt") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = sortByValue,
                onClick = { sortByValue = true },
                label = { Text("Podľa hodnoty") }
            )
            FilterChip(
                selected = !sortByValue,
                onClick = { sortByValue = false },
                label = { Text("Podľa množstva") }
            )
            FilterChip(
                selected = sealedOnly,
                onClick = { sealedOnly = !sealedOnly },
                label = { Text("Len Sealed") }
            )
        }
        Text("Počet položiek: ${rows.size}", color = Mint)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rows, key = { it.name }) { row ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Purple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(row.name) }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProductImage(name = row.name)
                        Text(row.name, color = Mint, fontWeight = FontWeight.SemiBold)
                        Text("Množstvo: ${row.quantity} ks", color = Mint)
                        if (row.sealed) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Sealed") },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                            )
                        }
                        Text(formatCurrency(row.value), color = Mint, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductDetailScreen(
    productName: String,
    catalog: List<CatalogProductEntity>,
    positions: Map<String, ProductPosition>,
    transactions: List<InventoryTransactionEntity>,
    onBack: () -> Unit,
    onAddPurchase: () -> Unit
) {
    val marketPrice = catalog.firstOrNull { it.name == productName }?.marketPriceEur ?: 0.0
    val position = positions[productName]
    val quantity = position?.quantityInStock?.coerceAtLeast(0) ?: 0
    val totalValue = quantity * marketPrice
    val costBasis = position?.totalSpentEur ?: 0.0
    val profitPercent = if (costBasis > 0.0) ((totalValue - costBasis) / costBasis) * 100.0 else 0.0

    val purchaseLots = transactions
        .filter { it.productName == productName && it.type == TransactionType.PURCHASE }
        .sortedByDescending { it.dateEpochDay }

    Scaffold(
        containerColor = Navy,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) { Text("Späť") }
                Text("Detail produktu", color = Mint, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.size(8.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Purple), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProductImage(name = productName)
                        Text(productName, color = Mint, style = MaterialTheme.typography.titleLarge)
                        Text("Celková hodnota: ${formatCurrency(totalValue)}", color = Mint)
                        Text("Množstvo: $quantity ks", color = Mint)
                        Text("Nákupná cena spolu: ${formatCurrency(costBasis)}", color = Mint)
                        Text(
                            "Zmena: ${formatPercent(profitPercent)}",
                            color = if (profitPercent >= 0.0) Profit else Loss
                        )
                        TextButton(onClick = onAddPurchase, modifier = Modifier.fillMaxWidth()) {
                            Text("Pridať nákup")
                        }
                    }
                }
            }

            item {
                Text("Nákupné loty", color = Mint, style = MaterialTheme.typography.titleLarge)
            }

            items(purchaseLots.size) { index ->
                val lot = purchaseLots[index]
                val unitPrice = if (lot.quantity > 0) lot.totalPriceEur / lot.quantity else 0.0
                val change = if (unitPrice > 0.0) ((marketPrice - unitPrice) / unitPrice) * 100.0 else 0.0
                Card(colors = CardDefaults.cardColors(containerColor = Purple), shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${lot.quantity} × ${formatCurrency(unitPrice)}",
                            color = Mint,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Dátum: ${formatDate(lot.dateEpochDay)}", color = Mint)
                        Text(
                            "Zmena: ${formatPercent(change)}",
                            color = if (change >= 0.0) Profit else Loss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsScreen(
    modifier: Modifier,
    transactions: List<InventoryTransactionEntity>,
    onDelete: (Long) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .padding(16.dp)
    ) {
        Text("Transakcie", color = Mint, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions.size, key = { transactions[it].id }) { index ->
                val tx = transactions[index]
                val title = if (tx.type == TransactionType.PURCHASE) "Nákup" else "Predaj"
                Card(
                    colors = CardDefaults.cardColors(containerColor = Purple),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(tx.productName, color = Mint, fontWeight = FontWeight.SemiBold)
                            Text(
                                "$title • ${tx.quantity} ks • ${formatCurrency(tx.totalPriceEur)}",
                                color = Mint
                            )
                            Text(formatDate(tx.dateEpochDay), color = Mint.copy(alpha = 0.9f))
                        }
                        IconButton(onClick = { onDelete(tx.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Vymazať", tint = Loss)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogScreen(
    modifier: Modifier,
    catalog: List<CatalogProductEntity>,
    onAddProduct: (String, Double) -> Unit
) {
    var openDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Katalóg", color = Mint, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = { openDialog = true }) {
                Text("Pridať produkt")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(catalog.size, key = { catalog[it].name }) { index ->
                val product = catalog[index]
                Card(colors = CardDefaults.cardColors(containerColor = Purple), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProductImage(name = product.name, modifier = Modifier.size(58.dp))
                        Column {
                            Text(product.name, color = Mint, fontWeight = FontWeight.SemiBold)
                            Text("Trhová cena: ${formatCurrency(product.marketPriceEur)}", color = Mint)
                        }
                    }
                }
            }
        }
    }

    if (openDialog) {
        AddCatalogDialog(
            onDismiss = { openDialog = false },
            onConfirm = { name, price ->
                onAddProduct(name, price)
                openDialog = false
            }
        )
    }
}

@Composable
private fun AddCatalogDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pridať produkt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Názov produktu") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = normalizeDecimalInput(it) },
                    label = { Text("Trhová cena (€)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(name, parseDecimal(price))
            }) {
                Text("Uložiť")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Zrušiť") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(
    catalog: List<CatalogProductEntity>,
    initialProductName: String?,
    initialType: TransactionType,
    onDismiss: () -> Unit,
    onSave: (String, Int, Double, LocalDate, TransactionType) -> Unit
) {
    var selectedProduct by rememberSaveable { mutableStateOf(initialProductName.orEmpty()) }
    var productSearch by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var priceInput by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(initialType) }
    var pricingMode by rememberSaveable { mutableStateOf(PricingMode.PER_PIECE) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    val filteredProducts = remember(catalog, productSearch) {
        catalog.filter { it.name.contains(productSearch.trim(), ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Purple) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Nová transakcia", color = Mint, style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = productSearch,
                onValueChange = { productSearch = it },
                label = { Text("Hľadať produkt") },
                modifier = Modifier.fillMaxWidth()
            )

            Box {
                OutlinedTextField(
                    value = selectedProduct,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Produkt") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    filteredProducts.forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.name) },
                            onClick = {
                                selectedProduct = product.name
                                expanded = false
                            }
                        )
                    }
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(TransactionType.PURCHASE to "Nákup", TransactionType.SALE to "Predaj").forEachIndexed { index, item ->
                    SegmentedButton(
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(index, 2),
                        selected = type == item.first,
                        onClick = { type = item.first },
                        label = { Text(item.second) },
                        colors = androidx.compose.material3.SegmentedButtonDefaults.colors(
                            activeContainerColor = Pink,
                            activeContentColor = Purple,
                            inactiveContainerColor = Purple,
                            inactiveContentColor = Mint
                        )
                    )
                }
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter(Char::isDigit) },
                label = { Text("Množstvo") },
                modifier = Modifier.fillMaxWidth()
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PricingMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(index, PricingMode.entries.size),
                        selected = pricingMode == mode,
                        onClick = { pricingMode = mode },
                        label = { Text(mode.label) },
                        colors = androidx.compose.material3.SegmentedButtonDefaults.colors(
                            activeContainerColor = Pink,
                            activeContentColor = Purple,
                            inactiveContainerColor = Purple,
                            inactiveContentColor = Mint
                        )
                    )
                }
            }

            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = normalizeDecimalInput(it) },
                label = {
                    Text(if (pricingMode == PricingMode.PER_PIECE) "Cena za kus (€)" else "Celková cena (€)")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(containerColor = Navy.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "Dátum: ${selectedDate.dayOfMonth}.${selectedDate.monthValue}.${selectedDate.year}",
                    color = Mint,
                    modifier = Modifier.padding(12.dp)
                )
            }

            val totalPrice = remember(priceInput, quantity, pricingMode) {
                val parsed = parseDecimal(priceInput)
                val qty = quantity.toIntOrNull() ?: 0
                if (pricingMode == PricingMode.PER_PIECE) parsed * qty else parsed
            }

            Text(
                "Výsledná suma: ${formatCurrency(totalPrice)}",
                color = Mint,
                style = MaterialTheme.typography.bodyLarge
            )

            TextButton(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    if (selectedProduct.isBlank() || qty <= 0 || totalPrice <= 0.0) {
                        return@TextButton
                    }
                    onSave(selectedProduct, qty, totalPrice, selectedDate, type)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Uložiť transakciu")
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = pickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        selectedDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Zrušiť")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun ProductImage(name: String, modifier: Modifier = Modifier) {
    val painter = rememberAssetPainter("design/products/${slugify(name)}.png")
    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Mint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .background(Navy.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Mint)
        }
    }
}

@Composable
private fun rememberAssetPainter(path: String): Painter? {
    val context = LocalContext.current
    val bitmap = remember(path) {
        runCatching {
            context.assets.open(path).use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        }.getOrNull()
    }
    return bitmap?.let { BitmapPainter(it) }
}

private data class InventoryRow(
    val name: String,
    val marketPrice: Double,
    val quantity: Int,
    val value: Double,
    val sealed: Boolean
)

private fun windowLabel(window: StatsWindow): String = when (window) {
    StatsWindow.TODAY -> "Dnes"
    StatsWindow.WEEK -> "Týždeň"
    StatsWindow.MONTH -> "Mesiac"
    StatsWindow.THREE_MONTHS -> "3 mesiace"
    StatsWindow.YEAR -> "Rok"
    StatsWindow.ALL_TIME -> "Celé obdobie"
}

private fun formatCurrency(value: Double): String = eurFormatter.format(value)

private fun formatPercent(value: Double): String = String.format(slovakLocale, "%.2f %%", value)

private fun formatDate(epochDay: Long): String {
    val date = LocalDate.ofEpochDay(epochDay)
    return "${date.dayOfMonth}.${date.monthValue}.${date.year}"
}

private fun buildChartPoints(transactions: List<InventoryTransactionEntity>, window: StatsWindow): List<Double> {
    val today = LocalDate.now()
    val startDate = when (window) {
        StatsWindow.TODAY -> today
        StatsWindow.WEEK -> today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        StatsWindow.MONTH -> today.withDayOfMonth(1)
        StatsWindow.THREE_MONTHS -> today.minusMonths(2).withDayOfMonth(1)
        StatsWindow.YEAR -> today.withDayOfYear(1)
        StatsWindow.ALL_TIME -> transactions.minByOrNull { it.dateEpochDay }?.let { LocalDate.ofEpochDay(it.dateEpochDay) }
            ?: today
    }

    val scoped = transactions
        .filter { !LocalDate.ofEpochDay(it.dateEpochDay).isBefore(startDate) }
        .groupBy { LocalDate.ofEpochDay(it.dateEpochDay) }

    val dates = scoped.keys.sorted()
    if (dates.isEmpty()) return listOf(0.0, 0.0)

    var cumulative = 0.0
    return dates.map { date ->
        cumulative += scoped[date].orEmpty().sumOf { tx ->
            if (tx.type == TransactionType.SALE) tx.totalPriceEur else -tx.totalPriceEur
        }
        cumulative
    }
}

private fun normalizeDecimalInput(input: String): String {
    val normalized = input.replace(',', '.')
    return normalized.filterIndexed { index, c -> c.isDigit() || (c == '.' && '.' !in normalized.take(index)) }
}

private fun parseDecimal(input: String): Double = normalizeDecimalInput(input).toDoubleOrNull() ?: 0.0

private fun isSealedName(name: String): Boolean {
    val lowered = name.lowercase(slovakLocale)
    return lowered.contains("box") || lowered.contains("tin") || lowered.contains("collection") || lowered.contains("trainer")
}

private fun slugify(value: String): String = value
    .lowercase(slovakLocale)
    .replace("&", "and")
    .replace(Regex("[^a-z0-9]+"), "-")
    .trim('-')

@Composable
private fun Icons.Default.Catcher(): ImageVector = Icons.Default.ShowChart
