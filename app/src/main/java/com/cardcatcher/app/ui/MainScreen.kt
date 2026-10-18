package com.cardcatcher.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.cardcatcher.app.ui.theme.NavyDeep
import com.cardcatcher.app.ui.theme.Pink
import com.cardcatcher.app.ui.theme.Profit
import com.cardcatcher.app.ui.theme.Surface1
import com.cardcatcher.app.ui.theme.Surface2
import com.cardcatcher.app.ui.theme.TextMain
import com.cardcatcher.app.ui.theme.TextMuted
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

// ---------------------------------------------------------------- root

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
                        contentColor = Navy
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Pridať transakciu")
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = NavyDeep) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Mint,
                                selectedTextColor = Mint,
                                indicatorColor = Surface2,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
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

// ---------------------------------------------------------------- prehľad

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
    val invested = (stats.spentEur - stats.revenueEur).coerceAtLeast(0.0)

    val chartPoints = remember(state.transactions, selectedWindow) {
        buildChartPoints(state.transactions, selectedWindow)
    }
    val pattern = rememberAssetPainter("design/pozadie.png", sample = 2)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Navy),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { DashboardHeader() }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Surface1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    if (pattern != null) {
                        Image(
                            painter = pattern,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alpha = 0.10f,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Hodnota skladu", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            formatCurrency(inventoryValue),
                            color = TextMain,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        TrendPill(
                            text = "${formatSigned(netProfit)} (${formatPercent(profitPercent)})",
                            positive = netProfit >= 0
                        )
                        PerformanceChart(points = chartPoints)
                        PeriodSelector(selected = selectedWindow, onSelected = onWindowSelected)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Investované", formatCurrency(invested), TextMain, Modifier.weight(1f))
                StatCard(
                    "Zisk (realizovaný)",
                    formatCurrency(stats.profitEur),
                    if (stats.profitEur >= 0) Profit else Loss,
                    Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Výdavky", formatCurrency(stats.spentEur), TextMain, Modifier.weight(1f))
                StatCard("Tržby", formatCurrency(stats.revenueEur), TextMain, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    val logo = rememberAssetPainter("design/logo-2.png")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (logo != null) {
            Image(painter = logo, contentDescription = "Logo CardCatcher", modifier = Modifier.size(48.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface1),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = Pink)
            }
        }
        Column {
            Text("CardCatcher", color = TextMain, style = MaterialTheme.typography.titleLarge)
            Text("Portfólio sealed Pokémon TCG", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TrendPill(text: String, positive: Boolean) {
    val color = if (positive) Profit else Loss
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = color, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StatCard(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            Text(value, color = valueColor, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        }
    }
}

@Composable
private fun PeriodSelector(selected: StatsWindow, onSelected: (StatsWindow) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NavyDeep)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        StatsWindow.entries.forEach { window ->
            val isSelected = window == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Mint else Color.Transparent)
                    .clickable { onSelected(window) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = windowLabel(window),
                    color = if (isSelected) Navy else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun PerformanceChart(points: List<Double>) {
    if (points.size < 2) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
            Text("Zatiaľ málo dát pre graf", color = TextMuted)
        }
        return
    }
    val lineColor = if (points.last() >= points.first()) Profit else Loss
    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        val minV = points.min()
        val maxV = points.max()
        val range = (maxV - minV).takeIf { it > 0.0 } ?: 1.0
        val padV = size.height * 0.12f
        val usableH = size.height - 2 * padV
        val stepX = size.width / (points.size - 1)
        fun px(i: Int) = i * stepX
        fun py(v: Double) = (size.height - padV - ((v - minV) / range * usableH)).toFloat()
        fun Path.curveThroughPoints() {
            for (i in 1 until points.size) {
                val midX = (px(i - 1) + px(i)) / 2f
                cubicTo(midX, py(points[i - 1]), midX, py(points[i]), px(i), py(points[i]))
            }
        }

        val fill = Path().apply {
            moveTo(0f, size.height)
            lineTo(px(0), py(points[0]))
            curveThroughPoints()
            lineTo(size.width, size.height)
            close()
        }
        drawPath(fill, brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)))

        val line = Path().apply {
            moveTo(px(0), py(points[0]))
            curveThroughPoints()
        }
        drawPath(line, color = lineColor, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(lineColor, radius = 5.dp.toPx(), center = Offset(px(points.lastIndex), py(points.last())))
    }
}

// ---------------------------------------------------------------- sklad

@Composable
private fun InventoryScreen(
    modifier: Modifier,
    catalog: List<CatalogProductEntity>,
    positions: Map<String, ProductPosition>,
    onOpenDetail: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var sortByValue by rememberSaveable { mutableStateOf(true) }
    var inStockOnly by rememberSaveable { mutableStateOf(true) }

    val rows = remember(catalog, positions, query, sortByValue, inStockOnly) {
        catalog.map { product ->
            val quantity = positions[product.name]?.quantityInStock?.coerceAtLeast(0) ?: 0
            InventoryRow(
                name = product.name,
                quantity = quantity,
                value = quantity * product.marketPriceEur,
                sealed = isSealedName(product.name)
            )
        }
            .filter { it.name.contains(query.trim(), ignoreCase = true) }
            .filter { !inStockOnly || it.quantity > 0 }
            .sortedByDescending { if (sortByValue) it.value else it.quantity.toDouble() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Sklad", "${rows.size} položiek")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Hľadať v sklade") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleChip("Podľa hodnoty", sortByValue) { sortByValue = true }
            ToggleChip("Podľa množstva", !sortByValue) { sortByValue = false }
            ToggleChip("Na sklade", inStockOnly) { inStockOnly = !inStockOnly }
        }

        if (rows.isEmpty()) {
            Text(
                "Nič sa nenašlo. Skús iný názov, vypni filter Na sklade alebo pridaj nákup v Transakciách.",
                color = TextMuted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rows, key = { it.name }) { row ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(row.name) }
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box {
                            ProductImage(row.name, Modifier.fillMaxWidth().height(110.dp))
                            if (row.sealed) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Mint)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("Sealed", color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            row.name,
                            color = TextMain,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            minLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${row.quantity} ks", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            Text(formatCurrency(row.value), color = Mint, fontWeight = FontWeight.Bold)
                        }
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack, colors = mintText()) { Text("← Späť") }
                Text(
                    "Detail produktu",
                    color = TextMain,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(72.dp))
            }
        },
        bottomBar = {
            Box(modifier = Modifier.background(Navy).navigationBarsPadding().padding(16.dp)) {
                PrimaryButton("Pridať nákup", onAddPurchase)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Surface1), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProductImage(productName, Modifier.fillMaxWidth().height(170.dp))
                        Text(productName, color = TextMain, style = MaterialTheme.typography.titleLarge)
                        Text("Celková hodnota", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        Text(formatCurrency(totalValue), color = TextMain, style = MaterialTheme.typography.headlineMedium)
                        TrendPill(formatPercent(profitPercent), profitPercent >= 0.0)
                        HorizontalDivider(color = Surface2)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Množstvo", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                                Text("$quantity ks", color = TextMain, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Nákupná cena spolu", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                                Text(formatCurrency(costBasis), color = TextMain, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Text("Nákupy", color = TextMain, style = MaterialTheme.typography.titleLarge) }

            if (purchaseLots.isEmpty()) {
                item { Text("Zatiaľ žiadny nákup. Pridaj prvý tlačidlom dole.", color = TextMuted) }
            }

            items(purchaseLots.size) { index ->
                val lot = purchaseLots[index]
                val unitPrice = if (lot.quantity > 0) lot.totalPriceEur / lot.quantity else 0.0
                val change = if (unitPrice > 0.0) ((marketPrice - unitPrice) / unitPrice) * 100.0 else 0.0
                Card(colors = CardDefaults.cardColors(containerColor = Surface1), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "${lot.quantity} × ${formatCurrency(unitPrice)}",
                                color = TextMain,
                                fontWeight = FontWeight.Bold
                            )
                            Text(formatDate(lot.dateEpochDay), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            formatPercent(change),
                            color = if (change >= 0.0) Profit else Loss,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- transakcie

@Composable
private fun TransactionsScreen(
    modifier: Modifier,
    transactions: List<InventoryTransactionEntity>,
    onDelete: (Long) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<InventoryTransactionEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Transakcie", "${transactions.size} záznamov")

        if (transactions.isEmpty()) {
            Text("Zatiaľ žiadne transakcie. Ťukni na + a pridaj prvý nákup.", color = TextMuted)
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(transactions.size, key = { transactions[it].id }) { index ->
                val tx = transactions[index]
                val isPurchase = tx.type == TransactionType.PURCHASE
                Card(colors = CardDefaults.cardColors(containerColor = Surface1), shape = RoundedCornerShape(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background((if (isPurchase) Mint else Pink).copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (isPurchase) "Nákup" else "Predaj",
                                        color = if (isPurchase) Mint else Pink,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(formatDate(tx.dateEpochDay), color = TextMuted, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                tx.productName,
                                color = TextMain,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("${tx.quantity} ks", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                (if (isPurchase) "−" else "+") + formatCurrency(tx.totalPriceEur),
                                color = if (isPurchase) TextMain else Profit,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { deleteTarget = tx }) {
                                Icon(Icons.Default.Delete, contentDescription = "Vymazať", tint = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { tx ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = Surface1,
            titleContentColor = TextMain,
            textContentColor = TextMain,
            title = { Text("Vymazať transakciu?") },
            text = { Text("${tx.productName}, ${tx.quantity} ks. Túto akciu nejde vrátiť.") },
            confirmButton = {
                TextButton(onClick = { onDelete(tx.id); deleteTarget = null }, colors = mintText()) { Text("Vymazať") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }, colors = mintText()) { Text("Zrušiť") }
            }
        )
    }
}

// ---------------------------------------------------------------- katalóg

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
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenTitle("Katalóg", "${catalog.size} produktov")
            Button(
                onClick = { openDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pink, contentColor = Navy)
            ) { Text("Pridať", fontWeight = FontWeight.Bold) }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(catalog.size, key = { catalog[it].name }) { index ->
                val product = catalog[index]
                Card(colors = CardDefaults.cardColors(containerColor = Surface1), shape = RoundedCornerShape(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProductImage(product.name, Modifier.size(56.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.name,
                                color = TextMain,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("Trhová cena", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(formatCurrency(product.marketPriceEur), color = Mint, fontWeight = FontWeight.Bold)
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
private fun AddCatalogDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface1,
        titleContentColor = TextMain,
        textContentColor = TextMain,
        title = { Text("Nový produkt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Názov produktu") },
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = normalizeDecimalInput(it) },
                    label = { Text("Trhová cena (€)") },
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, parseDecimal(price)) }, colors = mintText()) { Text("Uložiť") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = mintText()) { Text("Zrušiť") }
        }
    )
}

// ---------------------------------------------------------------- nová transakcia

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
    val qty = quantity.toIntOrNull() ?: 0
    val totalPrice = remember(priceInput, quantity, pricingMode) {
        val parsed = parseDecimal(priceInput)
        if (pricingMode == PricingMode.PER_PIECE) parsed * qty else parsed
    }
    val canSave = selectedProduct.isNotBlank() && qty > 0 && totalPrice > 0.0

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface1) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nová transakcia", color = TextMain, style = MaterialTheme.typography.titleLarge)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(TransactionType.PURCHASE to "Nákup", TransactionType.SALE to "Predaj")
                    .forEachIndexed { index, item ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index, 2),
                            selected = type == item.first,
                            onClick = { type = item.first },
                            label = { Text(item.second) },
                            colors = segmentedColors()
                        )
                    }
            }

            OutlinedTextField(
                value = productSearch,
                onValueChange = { productSearch = it },
                placeholder = { Text("Hľadať produkt") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Navy)
                        .clickable { expanded = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (selectedProduct.isBlank()) "Vyber produkt" else selectedProduct,
                        color = if (selectedProduct.isBlank()) TextMuted else TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text("▾", color = Mint)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Surface2)
                ) {
                    filteredProducts.forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.name, color = TextMain) },
                            onClick = {
                                selectedProduct = product.name
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter(Char::isDigit) },
                label = { Text("Množstvo") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PricingMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, PricingMode.entries.size),
                        selected = pricingMode == mode,
                        onClick = { pricingMode = mode },
                        label = { Text(mode.label) },
                        colors = segmentedColors()
                    )
                }
            }

            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = normalizeDecimalInput(it) },
                label = {
                    Text(if (pricingMode == PricingMode.PER_PIECE) "Cena za kus (€)" else "Celková cena (€)")
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Navy)
                    .clickable { showDatePicker = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dátum", color = TextMuted)
                Text(
                    "${selectedDate.dayOfMonth}.${selectedDate.monthValue}.${selectedDate.year}",
                    color = TextMain,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Výsledná suma", color = TextMuted)
                Text(formatCurrency(totalPrice), color = Mint, style = MaterialTheme.typography.titleLarge)
            }

            PrimaryButton(
                text = "Uložiť transakciu",
                enabled = canSave,
                onClick = { onSave(selectedProduct, qty, totalPrice, selectedDate, type) }
            )
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePicker = false
                    },
                    colors = mintText()
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }, colors = mintText()) { Text("Zrušiť") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// ---------------------------------------------------------------- spoločné prvky

@Composable
private fun ScreenTitle(title: String, subtitle: String) {
    Column {
        Text(title, color = TextMain, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ToggleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp, maxLines = 1) },
        shape = RoundedCornerShape(50),
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Surface1,
            labelColor = TextMuted,
            selectedContainerColor = Mint,
            selectedLabelColor = Navy
        )
    )
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Pink,
            contentColor = Navy,
            disabledContainerColor = Surface2,
            disabledContentColor = TextMuted
        ),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextMain,
    unfocusedTextColor = TextMain,
    focusedContainerColor = Navy,
    unfocusedContainerColor = Navy,
    focusedBorderColor = Mint,
    unfocusedBorderColor = Surface2,
    focusedLabelColor = Mint,
    unfocusedLabelColor = TextMuted,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted,
    focusedLeadingIconColor = Mint,
    unfocusedLeadingIconColor = TextMuted,
    cursorColor = Mint
)

@Composable
private fun segmentedColors() = SegmentedButtonDefaults.colors(
    activeContainerColor = Mint,
    activeContentColor = Navy,
    inactiveContainerColor = Navy,
    inactiveContentColor = TextMuted
)

@Composable
private fun mintText() = ButtonDefaults.textButtonColors(contentColor = Mint)

@Composable
private fun ProductImage(name: String, modifier: Modifier = Modifier) {
    val painter = rememberAssetPainter("design/products/${slugify(name)}.png")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyDeep),
        contentAlignment = Alignment.Center
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(6.dp)
            )
        } else {
            Icon(Icons.Default.Inventory2, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
private fun rememberAssetPainter(path: String, sample: Int = 1): Painter? {
    val context = LocalContext.current
    val bitmap = remember(path, sample) {
        runCatching {
            context.assets.open(path).use { input ->
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                BitmapFactory.decodeStream(input, null, options)?.asImageBitmap()
            }
        }.getOrNull()
    }
    return bitmap?.let { BitmapPainter(it) }
}

// ---------------------------------------------------------------- dáta a formátovanie

private data class InventoryRow(
    val name: String,
    val quantity: Int,
    val value: Double,
    val sealed: Boolean
)

private fun windowLabel(window: StatsWindow): String = when (window) {
    StatsWindow.TODAY -> "Dnes"
    StatsWindow.WEEK -> "Týždeň"
    StatsWindow.MONTH -> "Mesiac"
    StatsWindow.THREE_MONTHS -> "3 mes."
    StatsWindow.YEAR -> "Rok"
    StatsWindow.ALL_TIME -> "Všetko"
}

private fun formatCurrency(value: Double): String = eurFormatter.format(value)

private fun formatSigned(value: Double): String =
    (if (value >= 0) "+" else "−") + eurFormatter.format(kotlin.math.abs(value))

private fun formatPercent(value: Double): String =
    (if (value >= 0) "+" else "") + String.format(slovakLocale, "%.2f %%", value)

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