package com.farm2market.shared

import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.random.Random

enum class AppRole(val databaseValue: String) { CUSTOMER("customer"), FARMER("farmer") }

@Serializable
data class Product(
    val id: String = "",
    @SerialName("seller_id") val sellerId: String = "",
    val name: String = "",
    val category: String = "vegetables",
    val price: Double = 0.0,
    val emoji: String = "🌱",
    @SerialName("stock_quantity") val stock: Int = 0,
    @SerialName("seller_name") val sellerName: String = "Local farm"
)

@Serializable
private data class NearbyProduct(
    @SerialName("product_id") val productId: String,
    @SerialName("seller_id") val sellerId: String,
    val name: String,
    val category: String,
    val price: Double,
    val emoji: String,
    @SerialName("stock_quantity") val stock: Int,
    @SerialName("seller_name") val sellerName: String,
    @SerialName("distance_m") val distanceMeters: Double
) {
    fun toProduct() = Product(productId, sellerId, name, category, price, emoji, stock, sellerName)
}

@Serializable
private data class OrderRecord(
    val id: String,
    @SerialName("buyer_name") val buyerName: String,
    @SerialName("seller_id") val sellerId: String,
    val status: String,
    val total: Double,
    @SerialName("created_at") val createdAt: String
)

@Serializable
private data class ProfileNameRecord(@SerialName("display_name") val displayName: String)

@Serializable
data class OrderItem(
    val id: String = "",
    @SerialName("order_id") val orderId: String = "",
    @SerialName("product_id") val productId: String = "",
    @SerialName("product_name") val productName: String,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double
)

data class MarketOrder(
    val id: String,
    val buyerName: String,
    val sellerId: String,
    val status: String,
    val total: Double,
    val createdAt: String,
    val items: List<OrderItem>
)

@Serializable
private data class ProductInsert(
    @SerialName("seller_id") val sellerId: String,
    val name: String,
    val category: String,
    val price: Double,
    val emoji: String,
    @SerialName("stock_quantity") val stockQuantity: Int,
    @SerialName("is_listed") val isListed: Boolean = true
)

class FarmRepository(url: String, key: String, private val authScheme: String) {
    val live = url.startsWith("https://") && key.startsWith("sb_publishable_") &&
        !key.contains("your_key", ignoreCase = true)
    private val client: SupabaseClient? = if (live) {
        createSupabaseClient(url, key) {
            install(Auth) {
                host = "auth"
                scheme = authScheme
                autoLoadFromStorage = true
                autoSaveToStorage = true
                alwaysAutoRefresh = true
            }
            install(Postgrest)
            install(Realtime)
        }
    } else null

    suspend fun hasSession(): Boolean {
        val auth = client?.auth ?: return false
        auth.awaitInitialization()
        val user = auth.currentUserOrNull() ?: return false
        if (user.isAnonymous == true) {
            auth.signOut()
            return false
        }
        return true
    }

    suspend fun signUp(email: String, password: String) {
        val auth = client?.auth ?: error("Add the Supabase URL and publishable key in supabase.properties first")
        auth.signUpWith(Email, redirectUrl = "$authScheme://auth") {
            this.email = email.trim()
            this.password = password
        }
    }

    fun handleDeepLink(intent: Intent, onSessionSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        client?.handleDeeplinks(
            intent = intent,
            onSessionSuccess = { onSessionSuccess() },
            onError = onError
        )
    }

    suspend fun signIn(email: String, password: String) {
        val auth = client?.auth ?: error("Add the Supabase URL and publishable key in supabase.properties first")
        auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun profileName(): String? {
        val client = requireClient()
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return client.from("profiles").select {
            filter { eq("id", userId) }
        }.decodeList<ProfileNameRecord>().firstOrNull()?.displayName
    }

    suspend fun ensureProfile(role: AppRole, displayName: String, latitude: Double, longitude: Double) {
        val client = requireClient()
        if (client.auth.currentUserOrNull() == null) error("Sign in before saving your profile")
        client.postgrest.rpc("ensure_profile", buildJsonObject {
            put("p_role", role.databaseValue)
            put("p_display_name", displayName.trim())
            put("p_latitude", latitude)
            put("p_longitude", longitude)
        })
    }

    suspend fun products(latitude: Double?, longitude: Double?): List<Product> {
        if (!live) return demoProducts
        val client = requireClient()
        if (latitude == null || longitude == null) return emptyList()
        return client.postgrest.rpc("nearby_products", buildJsonObject {
            put("p_latitude", latitude)
            put("p_longitude", longitude)
            put("p_radius_m", 20000)
        }).decodeList<NearbyProduct>().map(NearbyProduct::toProduct)
    }

    suspend fun farmerProducts(): List<Product> {
        if (!live) return demoProducts
        val client = requireClient()
        val userId = client.auth.currentUserOrNull()?.id ?: error("Sign in to manage your farm")
        return client.from("products").select {
            filter { eq("seller_id", userId) }
        }.decodeList()
    }

    suspend fun addProduct(name: String, category: String, price: Double, stock: Int) {
        val client = requireClient()
        val sellerId = client.auth.currentUserOrNull()?.id ?: error("Sign in to add products")
        client.from("products").insert(ProductInsert(
            sellerId = sellerId,
            name = name.trim(),
            category = category,
            price = price,
            emoji = categoryEmoji[category] ?: "🌱",
            stockQuantity = stock
        ))
    }

    suspend fun setStock(id: String, quantity: Int) {
        val client = requireClient()
        val sellerId = client.auth.currentUserOrNull()?.id ?: error("Sign in to update stock")
        client.from("products").update({ set("stock_quantity", quantity.coerceAtLeast(0)) }) {
            filter {
                eq("id", id)
                eq("seller_id", sellerId)
            }
        }
    }

    suspend fun orders(role: AppRole): List<MarketOrder> {
        val client = requireClient()
        val userId = client.auth.currentUserOrNull()?.id ?: error("Sign in to view orders")
        val records = client.from("orders").select {
            filter {
                if (role == AppRole.FARMER) eq("seller_id", userId) else eq("buyer_id", userId)
            }
        }.decodeList<OrderRecord>()
        return records.map { order ->
            val lines = client.from("order_items").select {
                filter { eq("order_id", order.id) }
            }.decodeList<OrderItem>()
            MarketOrder(order.id, order.buyerName, order.sellerId, order.status, order.total, order.createdAt, lines)
        }
    }

    suspend fun updateOrder(id: String, status: String) {
        val client = requireClient()
        val sellerId = client.auth.currentUserOrNull()?.id ?: error("Sign in to update orders")
        client.from("orders").update({ set("status", status) }) {
            filter {
                eq("id", id)
                eq("seller_id", sellerId)
            }
        }
    }

    suspend fun placeOrder(items: List<Product>, latitude: Double, longitude: Double) {
        val client = requireClient()
        if (client.auth.currentUserOrNull() == null) error("Sign in before placing an order")
        if (items.isEmpty()) error("Your cart is empty")
        val itemsArray = buildJsonArray {
            items.forEach { product ->
                add(buildJsonObject {
                    put("product_id", product.id)
                    put("quantity", 1)
                })
            }
        }
        client.postgrest.rpc("place_orders", buildJsonObject {
            put("p_items", itemsArray)
            put("p_latitude", latitude)
            put("p_longitude", longitude)
        })
    }

    suspend fun signOut() {
        client?.auth?.signOut()
    }

    fun subscribeChanges(scope: CoroutineScope, onChange: () -> Unit): Job? {
        val client = client ?: return null
        return scope.launch {
            coroutineScope {
                listOf("products", "orders").forEach { table ->
                    launch {
                        val channel = client.channel("farm2market-$table-${Random.nextInt()}")
                        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                            this.table = table
                        }
                        val collector = launch { changes.collect { onChange() } }
                        try {
                            channel.subscribe(blockUntilSubscribed = true)
                            awaitCancellation()
                        } finally {
                            collector.cancelAndJoin()
                            client.realtime.removeChannel(channel)
                        }
                    }
                }
                awaitCancellation()
            }
        }
    }

    private fun requireClient(): SupabaseClient = client
        ?: error("Add the Supabase URL and publishable key in supabase.properties first")

    companion object {
        val categoryEmoji = mapOf(
            "vegetables" to "🥕",
            "fruits" to "🍎",
            "greens" to "🥬",
            "grains" to "🌾"
        )

        val demoProducts = listOf(
            Product("demo-tomato", name = "Tomatoes", category = "vegetables", price = 35.0, stock = 48),
            Product("demo-mango", name = "Mangoes", category = "fruits", price = 80.0, stock = 24),
            Product("demo-onion", name = "Onions", category = "vegetables", price = 28.0, stock = 65),
            Product("demo-spinach", name = "Spinach", category = "greens", price = 20.0, stock = 30),
            Product("demo-banana", name = "Bananas", category = "fruits", price = 42.0, stock = 50),
            Product("demo-rice", name = "Rice", category = "grains", price = 58.0, stock = 40)
        )
    }
}
