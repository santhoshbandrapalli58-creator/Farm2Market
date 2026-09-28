package com.farm2market.shared

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Tabs
// ─────────────────────────────────────────────────────────────────────────────

enum class FarmerTab { DASHBOARD, PRODUCTS, ORDERS, PROFILE }

// ─────────────────────────────────────────────────────────────────────────────
// App Shell
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerApp(
    repo: FarmRepository,
    language: String,
    onLanguageChange: () -> Unit,
    location: Location?,
    onRequestLocation: () -> Unit,
    displayName: String,
    onDisplayName: (String) -> Unit,
    products: List<Product>,
    orders: List<MarketOrder>,
    message: String,
    onMessage: (String) -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    onShowAuth: () -> Unit
) {
    var tab by remember { mutableStateOf(FarmerTab.DASHBOARD) }

    Scaffold(
        containerColor = F2MBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Farm2Market", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (repo.live) "🟢 Live" else "🔵 Demo mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = F2MTextMuted
                        )
                    }
                },
                actions = {
                    // Language switcher
                    TextButton(onClick = onLanguageChange) {
                        Icon(Icons.Default.Language, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            when (language) { "te" -> "తె"; "hi" -> "हि"; else -> "EN" },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    // Refresh
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = F2MGreenPrimary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = tab == FarmerTab.DASHBOARD,
                    onClick  = { tab = FarmerTab.DASHBOARD },
                    icon     = {
                        Icon(
                            if (tab == FarmerTab.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            null
                        )
                    },
                    label    = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = tab == FarmerTab.PRODUCTS,
                    onClick  = { tab = FarmerTab.PRODUCTS },
                    icon     = {
                        Icon(
                            if (tab == FarmerTab.PRODUCTS) Icons.Filled.Inventory else Icons.Outlined.Inventory2,
                            null
                        )
                    },
                    label    = { Text("Products") }
                )
                NavigationBarItem(
                    selected = tab == FarmerTab.ORDERS,
                    onClick  = { tab = FarmerTab.ORDERS },
                    icon     = {
                        BadgedBox(badge = {
                            val pending = orders.count { it.status == "pending" }
                            if (pending > 0) Badge { Text("$pending") }
                        }) {
                            Icon(
                                if (tab == FarmerTab.ORDERS) Icons.Filled.Receipt else Icons.Outlined.Receipt,
                                null
                            )
                        }
                    },
                    label    = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = tab == FarmerTab.PROFILE,
                    onClick  = { tab = FarmerTab.PROFILE },
                    icon     = {
                        Icon(
                            if (tab == FarmerTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            null
                        )
                    },
                    label    = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                FarmerTab.DASHBOARD -> FarmerDashboardScreen(products, orders, message) { tab = FarmerTab.ORDERS }
                FarmerTab.PRODUCTS  -> FarmerProductsScreen(repo, products, message, onMessage, onRefresh)
                FarmerTab.ORDERS    -> FarmerOrdersScreen(repo, orders, message, onMessage, onRefresh)
                FarmerTab.PROFILE   -> FarmerProfileScreen(
                    displayName, onDisplayName, location, onRequestLocation,
                    repo, onMessage, onShowAuth, onSignOut
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dashboard screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FarmerDashboardScreen(
    products: List<Product>,
    orders: List<MarketOrder>,
    message: String,
    onGoToOrders: () -> Unit
) {
    val inStock     = products.count { it.stock > 0 }
    val pendingOrders = orders.count { it.status == "pending" }
    val totalRevenue = orders.filter { it.status == "delivered" }.sumOf { it.total }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting
        item {
            Text("Welcome back 👨‍🌾", style = MaterialTheme.typography.headlineMedium, color = F2MGreenPrimary)
            Text("Here's a snapshot of your farm.", style = MaterialTheme.typography.bodyMedium, color = F2MTextMuted)
        }

        // Status message
        if (message.isNotBlank()) {
            item {
                Surface(color = F2MGreenContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(message, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = F2MGreenDark)
                }
            }
        }

        // Stats row
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "🌿", "${products.size}", "Products")
                StatCard(Modifier.weight(1f), "✅", "$inStock", "In stock")
                StatCard(Modifier.weight(1f), "📋", "${orders.size}", "Orders")
            }
        }

        // Revenue card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(containerColor = F2MGreenPrimary)
            ) {
                Row(
                    Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Total Revenue", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(0.8f))
                        Text("₹${"%.0f".format(totalRevenue)}", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        Text("from delivered orders", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f))
                    }
                    Text("💰", fontSize = 40.sp)
                }
            }
        }

        // Pending orders alert
        if (pendingOrders > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(18.dp),
                    colors   = CardDefaults.cardColors(containerColor = StatusPendingBg),
                    onClick  = onGoToOrders
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⏳", fontSize = 28.sp)
                        Column(Modifier.weight(1f)) {
                            Text(
                                "$pendingOrders new order${if (pendingOrders > 1) "s" else ""} waiting",
                                style = MaterialTheme.typography.titleSmall,
                                color = StatusPendingFg
                            )
                            Text("Tap to review and accept", style = MaterialTheme.typography.bodySmall, color = StatusPendingFg.copy(0.7f))
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = StatusPendingFg)
                    }
                }
            }
        }

        // Recent orders
        if (orders.isNotEmpty()) {
            item {
                Text("Recent Orders", style = MaterialTheme.typography.titleMedium, color = F2MTextDark)
            }
            items(orders.take(4)) { order ->
                OrderCard(order = order, isFarmer = true, onAction = null)
            }
            if (orders.size > 4) {
                item {
                    TextButton(onClick = onGoToOrders, modifier = Modifier.fillMaxWidth()) {
                        Text("View all ${orders.size} orders →")
                    }
                }
            }
        } else {
            item {
                EmptyState("📦", "No orders yet", "Orders from customers will appear here.")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Products screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FarmerProductsScreen(
    repo: FarmRepository,
    products: List<Product>,
    message: String,
    onMessage: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val scope  = rememberCoroutineScope()
    var showForm        by remember { mutableStateOf(false) }
    var newName         by remember { mutableStateOf("") }
    var newCategory     by remember { mutableStateOf("vegetables") }
    var newPrice        by remember { mutableStateOf("") }
    var newStock        by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("My Products", style = MaterialTheme.typography.headlineMedium, color = F2MGreenPrimary)
                Button(
                    onClick = { showForm = !showForm },
                    shape   = RoundedCornerShape(14.dp)
                ) {
                    Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (showForm) "Cancel" else "Add Product")
                }
            }
        }

        if (message.isNotBlank()) {
            item {
                Surface(color = F2MGreenContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(message, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = F2MGreenDark)
                }
            }
        }

        // Add product form
        if (showForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("New Listing", style = MaterialTheme.typography.titleMedium, color = F2MGreenPrimary)
                        OutlinedTextField(
                            value = newName, onValueChange = { newName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Product name") },
                            leadingIcon = { Icon(Icons.Default.Spa, null) },
                            singleLine = true, shape = RoundedCornerShape(12.dp)
                        )
                        // Category chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("vegetables" to "🥕", "fruits" to "🍎", "greens" to "🥬", "grains" to "🌾")) { (cat, emoji) ->
                                FilterChip(
                                    selected  = newCategory == cat,
                                    onClick   = { newCategory = cat },
                                    label     = { Text("$emoji ${cat.replaceFirstChar { it.uppercase() }}") }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = newPrice, onValueChange = { newPrice = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Price ₹/kg") },
                                singleLine = true, shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = newStock, onValueChange = { newStock = it.filter(Char::isDigit) },
                                modifier = Modifier.weight(1f),
                                label = { Text("Stock (kg)") },
                                singleLine = true, shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Button(
                            onClick = {
                                val price = newPrice.toDoubleOrNull()
                                val stock = newStock.toIntOrNull()
                                when {
                                    newName.isBlank()            -> onMessage("Enter a product name.")
                                    price == null || price <= 0  -> onMessage("Enter a valid price.")
                                    stock == null || stock < 0   -> onMessage("Enter a valid stock quantity.")
                                    else -> scope.launch {
                                        runCatching {
                                            if (repo.live) repo.addProduct(newName.trim(), newCategory, price, stock)
                                            newName = ""; newPrice = ""; newStock = ""
                                            showForm = false
                                            onMessage("✅ Product published!")
                                            onRefresh()
                                        }.onFailure { onMessage(it.message ?: "Could not add product") }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Publish, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Publish Listing")
                        }
                    }
                }
            }
        }

        if (products.isEmpty()) {
            item { EmptyState("🌱", "No products yet", "Tap 'Add Product' to create your first listing.") }
        } else {
            items(products) { product ->
                FarmerProductCard(product = product, repo = repo, onMessage = onMessage, onRefresh = onRefresh)
            }
        }
    }
}

@Composable
private fun FarmerProductCard(
    product: Product,
    repo: FarmRepository,
    onMessage: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val stockColor = when {
        product.stock == 0  -> MaterialTheme.colorScheme.error
        product.stock <= 5  -> F2MAmber
        else                -> F2MGreenPrimary
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(F2MGreenContainer),
                    contentAlignment = Alignment.Center
                ) { Text(product.emoji, fontSize = 22.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${product.category.replaceFirstChar { it.uppercase() }} · ₹${product.price}/kg",
                        style = MaterialTheme.typography.bodySmall, color = F2MTextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (product.stock == 0) "Sold out" else "${product.stock} kg",
                        style = MaterialTheme.typography.labelLarge,
                        color = stockColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text("in stock", style = MaterialTheme.typography.labelSmall, color = F2MTextMuted)
                }
            }

            // Stock controls
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Adjust stock:", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            scope.launch {
                                val q = (product.stock - 1).coerceAtLeast(0)
                                runCatching {
                                    if (repo.live) repo.setStock(product.id, q)
                                    onRefresh()
                                }.onFailure { onMessage(it.message ?: "Update failed") }
                            }
                        }, modifier = Modifier.size(36.dp)
                    ) { Text("−", fontWeight = FontWeight.Bold) }

                    Text(
                        "${product.stock}",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.widthIn(min = 32.dp),
                        textAlign = TextAlign.Center
                    )

                    FilledTonalIconButton(
                        onClick = {
                            scope.launch {
                                runCatching {
                                    if (repo.live) repo.setStock(product.id, product.stock + 1)
                                    onRefresh()
                                }.onFailure { onMessage(it.message ?: "Update failed") }
                            }
                        }, modifier = Modifier.size(36.dp)
                    ) { Text("+", fontWeight = FontWeight.Bold) }

                    if (product.stock > 0) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    runCatching {
                                        if (repo.live) repo.setStock(product.id, 0)
                                        onRefresh()
                                    }.onFailure { onMessage(it.message ?: "Update failed") }
                                }
                            }
                        ) { Text("Mark sold out", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Orders screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FarmerOrdersScreen(
    repo: FarmRepository,
    orders: List<MarketOrder>,
    message: String,
    onMessage: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val tabs = listOf("All", "Pending", "Active", "Delivered")
    var selectedTab by remember { mutableIntStateOf(0) }
    val orderScope  = rememberCoroutineScope()           // ← correct composable scope
    val filtered = when (selectedTab) {
        1    -> orders.filter { it.status == "pending" }
        2    -> orders.filter { it.status in listOf("accepted", "ready", "out_for_delivery") }
        3    -> orders.filter { it.status == "delivered" }
        else -> orders
    }

    Column(Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor   = Color.White,
            contentColor     = F2MGreenPrimary,
            edgePadding      = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }

        if (message.isNotBlank()) {
            Surface(color = F2MGreenContainer, modifier = Modifier.fillMaxWidth()) {
                Text(message, Modifier.padding(12.dp, 8.dp), style = MaterialTheme.typography.bodySmall, color = F2MGreenDark)
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    "📋", "No ${tabs[selectedTab].lowercase()} orders",
                    if (selectedTab == 1) "New orders will appear here." else "Nothing to show here yet."
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { order ->
                    OrderCard(order = order, isFarmer = true) { nextStatus ->
                        orderScope.launch {
                            runCatching {
                                repo.updateOrder(order.id, nextStatus)
                                onRefresh()
                            }.onFailure { onMessage(it.message ?: "Update failed") }
                        }
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Profile screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FarmerProfileScreen(
    displayName: String,
    onDisplayName: (String) -> Unit,
    location: Location?,
    onRequestLocation: () -> Unit,
    repo: FarmRepository,
    onMessage: (String) -> Unit,
    onShowAuth: () -> Unit,
    onSignOut: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar + name header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = F2MGreenPrimary)
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            displayName.firstOrNull()?.uppercase() ?: "F",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(displayName.ifBlank { "Farmer" }, style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text("Farmer Account", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.75f))
                    }
                }
            }
        }

        // Location info
        item {
            ProfileSection(title = "📍 Location") {
                if (location != null) {
                    Text(
                        "${"%.4f".format(location.latitude)}, ${"%.4f".format(location.longitude)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("20 km marketplace radius active", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
                } else {
                    Text("No location set", style = MaterialTheme.typography.bodyMedium, color = F2MTextMuted)
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(onClick = onRequestLocation, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.MyLocation, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (location != null) "Update Location" else "Set Location")
                }
            }
        }

        // Account actions
        item {
            ProfileSection(title = "⚙️ Account") {
                Button(
                    onClick  = onShowAuth,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = F2MSurfaceVariant, contentColor = F2MTextDark)
                ) {
                    Icon(Icons.Default.ManageAccounts, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Change Name")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick  = onSignOut,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
        }

        // App info
        item {
            ProfileSection(title = "ℹ️ About") {
                Text("Farm2Market", style = MaterialTheme.typography.titleSmall)
                Text("Version 1.0 · Farmer Portal", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
                Text(if (repo.live) "Connected to live marketplace" else "Running in demo mode", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared small composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatCard(modifier: Modifier, emoji: String, value: String, label: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 24.sp)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = F2MGreenPrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = F2MTextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun OrderCard(order: MarketOrder, isFarmer: Boolean, onAction: ((String) -> Unit)?) {
    val nextStatus = when (order.status) {
        "pending"          -> "accepted"
        "accepted"         -> "ready"
        "ready"            -> "out_for_delivery"
        "out_for_delivery" -> "delivered"
        else               -> null
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("Order #${order.id.take(8).uppercase()}", style = MaterialTheme.typography.titleSmall)
                    if (isFarmer) Text("Customer: ${order.buyerName}", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
                    Text(order.createdAt.take(10), style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
                }
                // Status chip
                Surface(
                    color  = statusBackground(order.status),
                    shape  = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        statusLabel(order.status),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = statusForeground(order.status)
                    )
                }
            }

            HorizontalDivider(color = F2MDivider)

            // Order items
            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.productName} × ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                    Text("₹${"%.0f".format(item.unitPrice * item.quantity)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(color = F2MDivider)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.labelMedium, color = F2MTextMuted)
                Text("₹${"%.0f".format(order.total)}", style = MaterialTheme.typography.titleMedium, color = F2MGreenPrimary, fontWeight = FontWeight.Bold)
            }

            // Action button for farmers
            if (isFarmer && nextStatus != null && onAction != null) {
                Button(
                    onClick  = { onAction(nextStatus) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text(when (nextStatus) {
                        "accepted"         -> "✅ Accept Order"
                        "ready"            -> "📦 Mark Ready"
                        "out_for_delivery" -> "🚚 Start Delivery"
                        else               -> "🎉 Mark Delivered"
                    }, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(title, style = MaterialTheme.typography.titleMedium, color = F2MTextDark, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = F2MTextMuted, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = F2MGreenPrimary)
            HorizontalDivider(color = F2MDivider)
            content()
        }
    }
}
