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

enum class CustomerTab { HOME, CART, ORDERS, NOTIFICATIONS, PROFILE }

// ─────────────────────────────────────────────────────────────────────────────
// App Shell
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CustomerApp(
    repo: FarmRepository,
    location: Location?,
    onRequestLocation: () -> Unit,
    displayName: String,
    onDisplayName: (String) -> Unit,
    products: List<Product>,
    orders: List<MarketOrder>,
    notifications: List<AppNotification>,
    onNotificationsOpened: () -> Unit,
    cart: List<Product>,
    onCart: (List<Product>) -> Unit,
    message: String,
    onMessage: (String) -> Unit,
    onRefresh: () -> Unit,
    onPlaceOrder: (deliveryAddress: String, contactPhone: String) -> Unit,
    onSignOut: () -> Unit,
    onShowAuth: () -> Unit
) {
    var tab by remember { mutableStateOf(CustomerTab.HOME) }
    var deliveryAddress by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    Scaffold(
        containerColor = F2MBackground,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = tab == CustomerTab.HOME,
                    onClick  = { tab = CustomerTab.HOME },
                    icon     = {
                        Icon(
                            if (tab == CustomerTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            null
                        )
                    },
                    label    = { Text("Shop") }
                )
                NavigationBarItem(
                    selected = tab == CustomerTab.CART,
                    onClick  = { tab = CustomerTab.CART },
                    icon     = {
                        BadgedBox(badge = {
                            if (cart.isNotEmpty()) Badge { Text("${cart.size}") }
                        }) {
                            Icon(
                                if (tab == CustomerTab.CART) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                null
                            )
                        }
                    },
                    label    = { Text("Cart") }
                )
                NavigationBarItem(
                    selected = tab == CustomerTab.ORDERS,
                    onClick  = { tab = CustomerTab.ORDERS },
                    icon     = {
                        Icon(
                            if (tab == CustomerTab.ORDERS) Icons.Filled.Receipt else Icons.Outlined.Receipt,
                            null
                        )
                    },
                    label    = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = tab == CustomerTab.PROFILE,
                    onClick  = { tab = CustomerTab.PROFILE },
                    icon     = {
                        Icon(
                            if (tab == CustomerTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            null
                        )
                    },
                    label    = { Text("Profile") }
                )
                NavigationBarItem(
                    selected = tab == CustomerTab.NOTIFICATIONS,
                    onClick = { tab = CustomerTab.NOTIFICATIONS; onNotificationsOpened() },
                    icon = {
                        BadgedBox(badge = {
                            val unread = notifications.count { it.readAt == null }
                            if (unread > 0) Badge { Text("$unread") }
                        }) {
                            Icon(
                                if (tab == CustomerTab.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
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
                CustomerTab.HOME    -> CustomerHomeScreen(
                    products    = products,
                    location    = location,
                    cart        = cart,
                    onAddToCart = { p ->
                        onCart(cart + p)
                        onMessage("${p.name} added to cart 🛒")
                    },
                    message     = message,
                    liveMode    = repo.live,
                    onGoToCart  = { tab = CustomerTab.CART }
                )
                CustomerTab.CART    -> CartScreen(
                    cart         = cart,
                    location     = location,
                    deliveryAddress = deliveryAddress,
                    onDeliveryAddress = { deliveryAddress = it },
                    contactPhone = contactPhone,
                    onContactPhone = { contactPhone = it },
                    onRemove     = { p -> onCart(cart - p) },
                    onClear      = { onCart(emptyList()) },
                    onPlaceOrder = { address, phone ->
                        onPlaceOrder(address, phone)
                        tab = CustomerTab.ORDERS
                    },
                    message      = message,
                    liveMode     = repo.live
                )
                CustomerTab.ORDERS  -> CustomerOrdersScreen(orders = orders, message = message)
                CustomerTab.NOTIFICATIONS -> NotificationsScreen(notifications)
                CustomerTab.PROFILE -> CustomerProfileScreen(
                    displayName      = displayName,
                    onDisplayName    = onDisplayName,
                    location         = location,
                    onRequestLocation = onRequestLocation,
                    orders           = orders,
                    cart             = cart,
                    repo             = repo,
                    onShowAuth       = onShowAuth,
                    onSignOut        = onSignOut
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Home / Shop screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerHomeScreen(
    products: List<Product>,
    location: Location?,
    cart: List<Product>,
    onAddToCart: (Product) -> Unit,
    message: String,
    liveMode: Boolean,
    onGoToCart: () -> Unit
) {
    val categories = listOf("All", "Vegetables", "Fruits", "Leafy Greens", "Grains")
    var query    by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    fun catMatch(p: Product) = when (category) {
        "Vegetables"  -> p.category.equals("vegetables", ignoreCase = true)
        "Fruits"      -> p.category.equals("fruits", ignoreCase = true)
        "Leafy Greens"-> p.category.equals("greens", ignoreCase = true)
        "Grains"      -> p.category.equals("grains", ignoreCase = true)
        else          -> true
    }

    val filtered = products.filter {
        catMatch(it) && it.name.contains(query, ignoreCase = true)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = F2MGreenContainer)
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Fresh produce,", style = MaterialTheme.typography.titleMedium, color = F2MGreenDark)
                        Text("straight from\nnearby farms 🌾", style = MaterialTheme.typography.headlineSmall, color = F2MGreenPrimary, fontWeight = FontWeight.Bold)
                        if (location != null) {
                            Spacer(Modifier.height(4.dp))
                            Text("Showing products within 20 km", style = MaterialTheme.typography.labelSmall, color = F2MTextMuted)
                        }
                    }
                    Text("🥕🍎\n🥬🌾", fontSize = 32.sp, textAlign = TextAlign.Center)
                }
            }
        }

        // Notices
        if (message.isNotBlank()) {
            item {
                Surface(color = F2MGreenContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(message, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = F2MGreenDark)
                }
            }
        }
        if (liveMode && location == null) {
            item {
                Surface(color = StatusPendingBg, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LocationOff, null, tint = StatusPendingFg, modifier = Modifier.size(18.dp))
                        Text("Set your location to browse nearby farms.", style = MaterialTheme.typography.bodySmall, color = StatusPendingFg)
                    }
                }
            }
        }

        // Cart mini-bar
        if (cart.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = F2MGreenPrimary),
                    onClick  = onGoToCart
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(
                            "${cart.size} item${if (cart.size > 1) "s" else ""} in cart",
                            style = MaterialTheme.typography.labelLarge, color = Color.White, modifier = Modifier.weight(1f)
                        )
                        Text("View cart →", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.85f))
                    }
                }
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("Search produce…") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = if (query.isNotBlank()) {
                    { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, null) } }
                } else null,
                singleLine    = true,
                shape         = RoundedCornerShape(16.dp)
            )
        }

        // Category chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick  = { category = cat },
                        label    = { Text(cat) }
                    )
                }
            }
        }

        // Product count
        item {
            Text(
                "${filtered.size} product${if (filtered.size != 1) "s" else ""} available",
                style = MaterialTheme.typography.labelMedium,
                color = F2MTextMuted
            )
        }

        // Product list
        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    "🔍", "No products found",
                    if (query.isNotBlank()) "Try a different search term." else "No produce available in this category yet."
                )
            }
        } else {
            items(filtered) { product ->
                CustomerProductCard(product = product, inCart = cart.count { it.id == product.id }, onAddToCart = { onAddToCart(product) })
            }
        }
    }
}

@Composable
private fun CustomerProductCard(product: Product, inCart: Int, onAddToCart: () -> Unit) {
    val inStock = product.stock > 0 && product.isListed
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductPhoto(
                url = product.imageUrl,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                background = if (inStock) F2MGreenContainer else Color(0xFFF5F5F5)
            )

            Spacer(Modifier.width(14.dp))

            // Info
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${product.category.replaceFirstChar { it.uppercase() }} · ₹${product.price}/kg",
                    style = MaterialTheme.typography.bodySmall, color = F2MTextMuted
                )
                Text(
                    "Seller: ${product.sellerName}",
                    style = MaterialTheme.typography.bodySmall, color = F2MTextMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (!inStock) "Sold out" else "${product.stock} kg available",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (!inStock) MaterialTheme.colorScheme.error else F2MGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.width(8.dp))

            // Cart controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (inCart > 0) {
                    Badge(containerColor = F2MGreenPrimary) { Text("×$inCart", color = Color.White) }
                    Spacer(Modifier.height(4.dp))
                }
                Button(
                    onClick  = onAddToCart,
                    enabled  = inStock,
                    modifier = Modifier.height(40.dp),
                    shape    = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cart screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CartScreen(
    cart: List<Product>,
    location: Location?,
    deliveryAddress: String,
    onDeliveryAddress: (String) -> Unit,
    contactPhone: String,
    onContactPhone: (String) -> Unit,
    onRemove: (Product) -> Unit,
    onClear: () -> Unit,
    onPlaceOrder: (String, String) -> Unit,
    message: String,
    liveMode: Boolean
) {
    if (cart.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState("🛒", "Your cart is empty", "Browse the shop and add fresh produce to get started.")
        }
        return
    }

    // Group items by product id so we show a quantity
    val grouped = cart.groupBy { it.id }
    val total   = cart.sumOf { it.price }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Your Cart", style = MaterialTheme.typography.headlineMedium, color = F2MGreenPrimary)
                TextButton(onClick = onClear) {
                    Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear all", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
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

        // Cart items
        items(grouped.values.toList()) { group ->
            val product = group.first()
            val qty     = group.size
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductPhoto(
                        url = product.imageUrl,
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleSmall)
                        Text("₹${product.price}/kg × $qty kg", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${"%.0f".format(product.price * qty)}", style = MaterialTheme.typography.titleSmall, color = F2MGreenPrimary, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = { onRemove(product) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp))
                            Text("Remove 1", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Delivery details", style = MaterialTheme.typography.titleSmall, color = F2MGreenPrimary)
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = onDeliveryAddress,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Delivery address") },
                        supportingText = { Text("Include house or building, street, and area") },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { value ->
                            if (value.length <= 20 && value.all { it.isDigit() || it in "+()- " }) onContactPhone(value)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contact number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Order summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(containerColor = F2MSurfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Order Summary", style = MaterialTheme.typography.titleSmall, color = F2MGreenPrimary)
                    HorizontalDivider(color = F2MDivider)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Items (${cart.size})", style = MaterialTheme.typography.bodyMedium)
                        Text("₹${"%.0f".format(total)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery", style = MaterialTheme.typography.bodyMedium)
                        Text("Free", style = MaterialTheme.typography.bodyMedium, color = F2MGreenPrimary)
                    }
                    HorizontalDivider(color = F2MDivider)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("₹${"%.0f".format(total)}", style = MaterialTheme.typography.titleMedium, color = F2MGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Location warning
        if (liveMode && location == null) {
            item {
                Surface(color = StatusPendingBg, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOff, null, tint = StatusPendingFg, modifier = Modifier.size(18.dp))
                        Text("Set your location before placing an order.", style = MaterialTheme.typography.bodySmall, color = StatusPendingFg)
                    }
                }
            }
        }

        // Place order button
        item {
            Button(
                onClick  = { onPlaceOrder(deliveryAddress.trim(), contactPhone.trim()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                enabled  = (if (liveMode) location != null else true) &&
                    deliveryAddress.trim().length >= 8 && contactPhone.count(Char::isDigit) >= 8
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Place Order · ₹${"%.0f".format(total)}", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Orders screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CustomerOrdersScreen(orders: List<MarketOrder>, message: String) {
    if (orders.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState("📦", "No orders yet", "Your order history will appear here once you place your first order.")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Your Orders", style = MaterialTheme.typography.headlineMedium, color = F2MGreenPrimary)
            if (message.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(color = F2MGreenContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(message, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = F2MGreenDark)
                }
            }
        }
        items(orders) { order ->
            OrderCard(order = order, isFarmer = false, onAction = null)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CustomerProfileScreen(
    displayName: String,
    onDisplayName: (String) -> Unit,
    location: Location?,
    onRequestLocation: () -> Unit,
    orders: List<MarketOrder>,
    cart: List<Product>,
    repo: FarmRepository,
    onShowAuth: () -> Unit,
    onSignOut: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar header
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
                            displayName.firstOrNull()?.uppercase() ?: "C",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(displayName.ifBlank { "Customer" }, style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text("Customer Account", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.75f))
                    }
                }
            }
        }

        // Activity summary
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "📦", "${orders.size}", "Orders")
                StatCard(Modifier.weight(1f), "🛒", "${cart.size}", "In Cart")
                StatCard(
                    Modifier.weight(1f), "✅",
                    "${orders.count { it.status == "delivered" }}",
                    "Delivered"
                )
            }
        }

        // Location
        item {
            ProfileSection(title = "📍 Location") {
                if (location != null) {
                    Text("${"%.4f".format(location.latitude)}, ${"%.4f".format(location.longitude)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Browsing farms within 20 km", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
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
                Text("Version 1.0 · Customer App", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
                Text(if (repo.live) "Connected to live marketplace" else "Running in demo mode", style = MaterialTheme.typography.bodySmall, color = F2MTextMuted)
            }
        }
    }
}
