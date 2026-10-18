package com.cardcatcher.app.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
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
import coil.compose.AsyncImage
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
    TRANSACTIONS("Transakcie", Icons.Default.ListAlt)
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
                    positions = positionByName,
                    transactions = state.transactions,
                    onOpenDetail = { selectedProductDetail = it }
                )

                MainTab.TRANSACTIONS -> TransactionsScreen(
                    modifier = Modifier.padding(padding),
                    transactions = state.transactions,
                    onDelete = viewModel::deleteTransaction
                )
            }
        }
    }

    if (openTransactionSheet) {
        AddTransactionSheet(
            knownProducts = state.knownProducts,
            transactions = state.transactions,
            initialProductName = selectedProductDetail,
            initialType = forcedType ?: TransactionType.PURCHASE,
            onDismiss = { openTransactionSheet = false },
            onSave = { product, quantity, totalPrice, date, type, imageUri ->
                viewModel.addTransaction(product, quantity, totalPrice, date, type, imageUri)
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

    var showRealizedOnly by rememberSaveable { mutableStateOf(false) }

    // Totálny P&L
    val netProfit = allTime.revenueEur - allTime.spentEur
    val profitPercent = if (allTime.spentEur > 0.0) (netProfit / allTime.spentEur) * 100.0 else 0.0
    val invested = (stats.spentEur - stats.revenueEur).coerceAtLeast(0.0)

    // Realizovaný zisk = iba z predaných kusov (revenue - priemerná nákupná cena × predané kusy)
    val realizedProfit = remember(state.transactions, selectedWindow) {
        computeRealizedProfit(state.transactions, selectedWindow)
    }
    val realizedPercent = remember(state.transactions) {
        val allPurchases = state.transactions.filter { it.type == TransactionType.PURCHASE }
        val costOfSold = state.transactions.filter { it.type == TransactionType.SALE }.sumOf { saleTx ->
            val productPurchases = allPurchases.filter { it.productName == saleTx.productName }
            val totalUnits = productPurchases.sumOf { it.quantity }
            val totalCost = productPurchases.sumOf { it.totalPriceEur }
            val avgCost = if (totalUnits > 0) totalCost / totalUnits else 0.0
            avgCost * saleTx.quantity
        }
        if (costOfSold > 0.0) (realizedProfit / costOfSold) * 100.0 else 0.0
    }

    val displayProfit = if (showRealizedOnly) realizedProfit else netProfit
    val displayPercent = if (showRealizedOnly) realizedPercent else profitPercent
    val displayLabel = if (showRealizedOnly) "Zisk z predaných kusov" else "Výsledok obchodovania"

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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DashboardHeader()
                // Kruhové tlačidlo — prepínač realizovaný zisk
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (showRealizedOnly) Mint else Surface1)
                        .clickable { showRealizedOnly = !showRealizedOnly },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ShowChart,
                        contentDescription = "Prepnúť na realizovaný zisk",
                        tint = if (showRealizedOnly) Navy else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(displayLabel, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            if (showRealizedOnly) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Mint.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("Predané", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            formatCurrency(displayProfit),
                            color = if (displayProfit >= 0) TextMain else Loss,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        TrendPill(
                            text = "${formatSigned(displayProfit)} (${formatPercent(displayPercent)})",
                            positive = displayProfit >= 0
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
                    if (showRealizedOnly) "Zisk predaných" else "Zisk (realizovaný)",
                    formatCurrency(if (showRealizedOnly) realizedProfit else stats.profitEur),
                    if ((if (showRealizedOnly) realizedProfit else stats.profitEur) >= 0) Profit else Loss,
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
    positions: Map<String, ProductPosition>,
    transactions: List<InventoryTransactionEntity>,
    onOpenDetail: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var sortByValue by rememberSaveable { mutableStateOf(true) }
    var inStockOnly by rememberSaveable { mutableStateOf(false) }

    // Build rows from positions (no catalog needed)
    val rows = remember(positions, transactions, query, sortByValue, inStockOnly) {
        positions.values.map { position ->
            val imageUri = transactions
                .lastOrNull { it.productName == position.productName && it.imageUri != null }
                ?.imageUri
            InventoryRow(
                name = position.productName,
                quantity = position.quantityInStock.coerceAtLeast(0),
                value = position.totalSpentEur - position.totalRevenueEur,
                sealed = isSealedName(position.productName),
                imageUri = imageUri
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
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                            ProductImage(row.name, row.imageUri, Modifier.fillMaxWidth().height(110.dp))
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
                            Text(formatCurrency(row.value.coerceAtLeast(0.0)), color = Mint, fontWeight = FontWeight.Bold)
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
    positions: Map<String, ProductPosition>,
    transactions: List<InventoryTransactionEntity>,
    onBack: () -> Unit,
    onAddPurchase: () -> Unit
) {
    val position = positions[productName]
    val quantity = position?.quantityInStock?.coerceAtLeast(0) ?: 0
    val costBasis = position?.totalSpentEur ?: 0.0
    val revenue = position?.totalRevenueEur ?: 0.0
    val invested = (costBasis - revenue).coerceAtLeast(0.0)

    val imageUri = transactions
        .lastOrNull { it.productName == productName && it.imageUri != null }
        ?.imageUri

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
                        ProductImage(productName, imageUri, Modifier.fillMaxWidth().height(170.dp))
                        Text(productName, color = TextMain, style = MaterialTheme.typography.titleLarge)
                        Text("Investované", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        Text(formatCurrency(invested), color = TextMain, style = MaterialTheme.typography.headlineMedium)
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
                            formatCurrency(lot.totalPriceEur),
                            color = Mint,
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
                        // Product thumbnail
                        ProductImage(
                            name = tx.productName,
                            imageUri = tx.imageUri,
                            modifier = Modifier.size(48.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f).padding(start = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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

// ---------------------------------------------------------------- nová transakcia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(
    knownProducts: List<String>,
    transactions: List<InventoryTransactionEntity>,
    initialProductName: String?,
    initialType: TransactionType,
    onDismiss: () -> Unit,
    onSave: (String, Int, Double, LocalDate, TransactionType, String?) -> Unit
) {
    var productName by rememberSaveable { mutableStateOf(initialProductName.orEmpty()) }
    var showProductPicker by rememberSaveable { mutableStateOf(false) }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var priceInput by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(initialType) }
    var pricingMode by rememberSaveable { mutableStateOf(PricingMode.PER_PIECE) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var pickedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Auto-fill image from last transaction with same product name
    val autoImageUri = remember(productName, transactions) {
        transactions.lastOrNull { it.productName == productName.trim() && it.imageUri != null }?.imageUri
    }
    val displayImageUri = pickedImageUri ?: autoImageUri

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        pickedImageUri = uri?.toString()
    }

    val qty = quantity.toIntOrNull() ?: 0
    val totalPrice = remember(priceInput, quantity, pricingMode) {
        val parsed = parseDecimal(priceInput)
        if (pricingMode == PricingMode.PER_PIECE) parsed * qty else parsed
    }
    val canSave = productName.isNotBlank() && qty > 0 && totalPrice > 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nová transakcia", color = TextMain, style = MaterialTheme.typography.titleLarge)

            // Nákup / Predaj
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

            // Obrázok produktu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyDeep)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (displayImageUri != null) {
                    AsyncImage(
                        model = Uri.parse(displayImageUri),
                        contentDescription = "Obrázok produktu",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                    // Edit overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Surface1.copy(alpha = 0.85f))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Zmeniť obrázok", tint = Mint, modifier = Modifier.size(20.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Text("Pridať obrázok produktu", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Textové pole pre názov
            OutlinedTextField(
                value = productName,
                onValueChange = {
                    productName = it
                    // clear picked image if user types new product name
                    if (it != initialProductName) pickedImageUri = null
                },
                label = { Text("Názov produktu") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Výber existujúceho produktu (ak existujú)
            if (knownProducts.isNotEmpty()) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Navy)
                            .clickable { showProductPicker = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Vybrať existujúci produkt",
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text("▾", color = Mint)
                    }
                    DropdownMenu(
                        expanded = showProductPicker,
                        onDismissRequest = { showProductPicker = false },
                        modifier = Modifier.background(Surface2)
                    ) {
                        knownProducts.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name, color = TextMain) },
                                onClick = {
                                    productName = name
                                    pickedImageUri = null // reset so autoImage kicks in
                                    showProductPicker = false
                                }
                            )
                        }
                    }
                }
            }

            // Množstvo
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter(Char::isDigit) },
                label = { Text("Množstvo") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Cena za kus / Celková cena
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

            // Dátum
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

            // Výsledná suma
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
                onClick = {
                    onSave(productName.trim(), qty, totalPrice, selectedDate, type, pickedImageUri)
                }
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
private fun ProductImage(name: String, imageUri: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyDeep),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageUri != null -> {
                AsyncImage(
                    model = Uri.parse(imageUri),
                    contentDescription = name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(6.dp)
                )
            }
            else -> {
                val painter = rememberAssetPainter("design/products/${slugify(name)}.png")
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
    val sealed: Boolean,
    val imageUri: String?
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
    val points = dates.map { date ->
        cumulative += scoped[date].orEmpty().sumOf { tx ->
            if (tx.type == TransactionType.SALE) tx.totalPriceEur else -tx.totalPriceEur
        }
        cumulative
    }
    // Graf potrebuje aspoň 2 body — ak je len 1 dátum, pridáme štartovací bod 0
    return if (points.size < 2) listOf(0.0) + points else points
}

private fun computeRealizedProfit(
    transactions: List<InventoryTransactionEntity>,
    window: StatsWindow
): Double {
    val today = LocalDate.now()
    val startDate: LocalDate? = when (window) {
        StatsWindow.TODAY -> today
        StatsWindow.WEEK -> today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        StatsWindow.MONTH -> today.withDayOfMonth(1)
        StatsWindow.THREE_MONTHS -> today.minusMonths(2).withDayOfMonth(1)
        StatsWindow.YEAR -> today.withDayOfYear(1)
        StatsWindow.ALL_TIME -> null
    }

    val allPurchases = transactions.filter { it.type == TransactionType.PURCHASE }
    val periodSales = transactions.filter {
        it.type == TransactionType.SALE &&
        (startDate == null || !LocalDate.ofEpochDay(it.dateEpochDay).isBefore(startDate))
    }

    // Pre každý predaj: odpočítaj priemernú nákupnú cenu × predané kusy
    return periodSales.sumOf { saleTx ->
        val productPurchases = allPurchases.filter { it.productName == saleTx.productName }
        val totalUnits = productPurchases.sumOf { it.quantity }
        val totalCost = productPurchases.sumOf { it.totalPriceEur }
        val avgCost = if (totalUnits > 0) totalCost / totalUnits else 0.0
        saleTx.totalPriceEur - (avgCost * saleTx.quantity)
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