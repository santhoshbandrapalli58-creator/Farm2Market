package com.farm2market.shared

import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Tabs
// ─────────────────────────────────────────────────────────────────────────────

enum class FarmerTab { DASHBOARD, PRODUCTS, ORDERS, NOTIFICATIONS, PROFILE }

// ─────────────────────────────────────────────────────────────────────────────
// App Shell
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FarmerApp(
    repo: FarmRepository,
    location: Location?,
    onRequestLocation: () -> Unit,
    displayName: String,
    onDisplayName: (String) -> Unit,
    products: List<Product>,
    orders: List<MarketOrder>,
    notifications: List<AppNotification>,
    onNotificationsOpened: () -> Unit,
    message: String,
    onMessage: (String) -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    onShowAuth: () -> Unit
) {
    var tab by remember { mutableStateOf(FarmerTab.DASHBOARD) }

    Scaffold(
        containerColor = F2MBackground,
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
                NavigationBarItem(
                    selected = tab == FarmerTab.NOTIFICATIONS,
                    onClick = { tab = FarmerTab.NOTIFICATIONS; onNotificationsOpened() },
                    icon = {
                        BadgedBox(badge = {
                            val unread = notifications.count { it.readAt == null }
                            if (unread > 0) Badge { Text("$unread") }
                        }) {
                            Icon(
                                if (tab == FarmerTab.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    },
                    label = { Text("Alerts") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                FarmerTab.DASHBOARD -> FarmerDashboardScreen(products, orders, message) { tab = FarmerTab.ORDERS }
                FarmerTab.PRODUCTS  -> FarmerProductsScreen(repo, products, message, onMessage, onRefresh)
                FarmerTab.ORDERS    -> FarmerOrdersScreen(repo, orders, message, onMessage, onRefresh)
                FarmerTab.NOTIFICATIONS -> NotificationsScreen(notifications)
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
    val context = androidx.compose.ui.platform.LocalContext.current
    var showForm        by remember { mutableStateOf(false) }
    var newName         by remember { mutableStateOf("") }
    var newCategory     by remember { mutableStateOf("vegetables") }
    var newPrice        by remember { mutableStateOf("") }
    var newStock        by remember { mutableStateOf("") }
    var selectedPhoto by remember { mutableStateOf<android.net.Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedPhoto = it }

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
                        OutlinedButton(
                            onClick = { photoPicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (selectedPhoto == null) "Choose product picture" else "Picture selected · Change")
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
                                    selectedPhoto == null        -> onMessage("Choose a product photo before publishing.")
                                    else -> scope.launch {
                                        runCatching {
                                             val imageUrl = if (repo.live && selectedPhoto != null) {
                                                 repo.uploadProductImage(context.contentResolver, selectedPhoto!!)
                                             } else null
                                             if (repo.live) repo.addProduct(newName.trim(), newCategory, price, stock, imageUrl)
                                             newName = ""; newPrice = ""; newStock = ""; selectedPhoto = null
                                            showForm = false
                                            onMessage("✅ Product published!")
                                            onRefresh()
                                        }.onFailure { onMessage(productPublishError(it)) }
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

private fun productPublishError(error: Throwable): String {
    val detail = error.message.orEmpty()
    return when {
        detail.contains("row-level security", ignoreCase = true) ->
            "Could not publish product: Supabase denied the database write. Check the farmer profile and products table policies."
        detail.contains("bucket not found", ignoreCase = true) ->
            "Could not upload the photo: create a public Supabase Storage bucket named 'product-images', then retry."
        detail.contains("sign in", ignoreCase = true) || detail.contains("session", ignoreCase = true) ->
            "Could not publish product: sign in again and retry."
        else -> "Could not publish product. Check your connection and Supabase setup, then retry."
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
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(product.name) }
    var editCategory by remember { mutableStateOf(product.category) }
    var editPrice by remember { mutableStateOf(product.price.toString()) }
    var editStock by remember { mutableStateOf(product.stock.toString()) }
    var replacementPhoto by remember { mutableStateOf<Uri?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        replacementPhoto = it
    }
    val stockColor = when {
        !product.isListed || product.stock == 0 -> MaterialTheme.colorScheme.error
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
                ProductPhoto(
                    url = product.imageUrl,
                    modifier = Modifier.size(58.dp).clip(RoundedCornerShape(12.dp))
                )
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
                        if (!product.isListed || product.stock == 0) "Sold out" else "${product.stock} kg",
                        style = MaterialTheme.typography.labelLarge,
                        color = stockColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(if (product.isListed) "in stock" else "hidden", style = MaterialTheme.typography.labelSmall, color = F2MTextMuted)
                }
            }

            OutlinedButton(
                onClick = {
                    editName = product.name
                    editCategory = product.category
                    editPrice = product.price.toString()
                    editStock = product.stock.toString()
                    replacementPhoto = null
                    showEditDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Edit name, type, price or stock")
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

                    if (product.stock > 0 && product.isListed) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    runCatching {
                                        if (repo.live) repo.updateProduct(product.copy(isListed = false))
                                        onRefresh()
                                    }.onFailure { onMessage(it.message ?: "Update failed") }
                                }
                            }
                        ) { Text("Mark sold out", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium) }
                    } else if (!product.isListed && product.stock > 0) {
                        TextButton(onClick = {
                            scope.launch {
                                runCatching {
                                    if (repo.live) repo.updateProduct(product.copy(isListed = true))
                                    onRefresh()
                                }.onFailure { onMessage(it.message ?: "Could not relist product") }
                            }
                        }) { Text("Relist") }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit product") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Product name") },
                        singleLine = true
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("vegetables", "fruits", "greens", "grains")) { category ->
                            FilterChip(
                                selected = editCategory == category,
                                onClick = { editCategory = category },
                                label = { Text(category.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("Price ₹/kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editStock,
                        onValueChange = { editStock = it.filter(Char::isDigit) },
                        label = { Text("Stock (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedButton(onClick = { photoPicker.launch("image/*") }) {
                        Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (replacementPhoto == null && product.imageUrl != null) "Change product photo" else if (replacementPhoto == null) "Add product photo" else "Photo selected · Change")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val price = editPrice.toDoubleOrNull()
                    val stock = editStock.toIntOrNull()
                    when {
                        editName.isBlank() -> onMessage("Enter a product name.")
                        price == null || price <= 0 -> onMessage("Enter a valid price.")
                        stock == null || stock < 0 -> onMessage("Enter a valid stock quantity.")
                        else -> scope.launch {
                            runCatching {
                                if (repo.live) repo.updateProduct(product.copy(
                                    name = editName.trim(), category = editCategory,
                                    price = price, stock = stock,
                                    isListed = if (stock == 0) false else product.isListed,
                                    imageUrl = replacementPhoto?.let {
                                        repo.uploadProductImage(context.contentResolver, it)
                                    } ?: product.imageUrl
                                ))
                                showEditDialog = false
                                onMessage("Product updated")
                                onRefresh()
                            }.onFailure { onMessage(productPublishError(it)) }
                        }
                    }
                }) { Text("Save changes") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
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
    val language = LocalAppLanguage.current
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

            if (order.status != "cancelled") {
                OrderProgress(order.status)
                Text(
                    "Estimated delivery by ${formatDeliveryEstimate(order.estimatedDeliveryAt, language)} (within 24 hours)",
                    style = MaterialTheme.typography.bodySmall,
                    color = F2MGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isFarmer) {
                Text("Delivery address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall)
                Text("Customer contact: ${order.contactPhone}", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
            }

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
private fun OrderProgress(status: String) {
    val labels = listOf("Placed", "Accepted", "Preparing", "On the way", "Delivered")
    val statuses = listOf("pending", "accepted", "ready", "out_for_delivery", "delivered")
    val current = statuses.indexOf(status).coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(F2MDivider)) {
            Box(
                Modifier.fillMaxWidth(current.toFloat() / (statuses.lastIndex).coerceAtLeast(1))
                    .fillMaxHeight().background(F2MGreenPrimary)
            )
        }
        Row(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index <= current) F2MGreenDark else F2MTextMuted,
                    textAlign = if (index == 0) TextAlign.Start else if (index == labels.lastIndex) TextAlign.End else TextAlign.Center
                )
            }
        }
    }
}

private fun formatDeliveryEstimate(value: String, language: String): String = runCatching {
    OffsetDateTime.parse(value)
        .atZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a", Locale.forLanguageTag(language)))
}.getOrDefault(value)

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
