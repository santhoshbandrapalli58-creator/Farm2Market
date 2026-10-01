package com.farm2market.shared

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch

@Composable
fun Farm2MarketApp(role: AppRole, url: String, key: String, authIntent: Intent? = null) {
    val ctx   = LocalContext.current
    val authScheme = if (role == AppRole.CUSTOMER) "farm2market-customer" else "farm2market-farmer"
    val repo  = remember { FarmRepository(url, key, authScheme) }
    val scope = rememberCoroutineScope()
    val prefs = remember { ctx.getSharedPreferences("farm2market", 0) }

    // ── State ─────────────────────────────────────────────────────────────────
    var language    by remember {
        mutableStateOf(prefs.getString("language", "en")?.takeIf { code -> appLanguages.any { it.code == code } } ?: "en")
    }
    var signedIn    by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf(prefs.getString("display_name", "") ?: "") }
    var identifier  by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var createAccount by remember { mutableStateOf(false) }
    var hasAuthSession by remember { mutableStateOf(false) }
    var showNameEntry by remember { mutableStateOf(true) }
    var startingSession by remember { mutableStateOf(false) }

    var location    by remember { mutableStateOf<Location?>(null) }
    var message     by remember { mutableStateOf("") }

    var products    by remember { mutableStateOf(FarmRepository.demoProducts) }
    var orders      by remember { mutableStateOf(emptyList<MarketOrder>()) }
    var notifications by remember { mutableStateOf(emptyList<AppNotification>()) }
    var cart        by remember { mutableStateOf(emptyList<Product>()) }

    val isFarmer = role == AppRole.FARMER

    fun selectLanguage(code: String) {
        if (appLanguages.any { it.code == code }) {
            language = code
            prefs.edit().putString("language", code).apply()
        }
    }

    LaunchedEffect(authIntent?.dataString) {
        authIntent?.let { intent ->
            repo.handleDeepLink(
                intent = intent,
                onSessionSuccess = {
                    hasAuthSession = true
                    createAccount = false
                    startingSession = false
                    message = "Email confirmed. Continue to finish setting up your account."
                },
                onError = { error ->
                    message = error.message ?: "The confirmation link could not be completed. Request a fresh email and try again."
                }
            )
        }
    }

    // ── Location helpers ──────────────────────────────────────────────────────
    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            LocationServices.getFusedLocationProviderClient(ctx).getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { loc ->
                location = loc
                if (loc == null) message = "Could not read location — ensure device GPS is on."
            }
        } else {
            message = "Location permission is required for the 20 km marketplace."
        }
    }

    fun requestLocation() {
        val fine   = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            LocationServices.getFusedLocationProviderClient(ctx).getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token
            ).addOnSuccessListener { loc ->
                location = loc
                if (loc == null) message = "Could not read location — ensure device GPS is on."
            }
        } else {
            locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    // Profile completion needs a location for the nearby marketplace. Ask when
    // an authenticated user reaches this screen instead of making the permission
    // request depend on discovering and tapping the location button.
    LaunchedEffect(showNameEntry, hasAuthSession, location) {
        if (showNameEntry && hasAuthSession && location == null) {
            requestLocation()
        }
    }

    // ── Data refresh ──────────────────────────────────────────────────────────
    fun refresh() {
        scope.launch {
            if (repo.live && !signedIn) {
                products = emptyList(); orders = emptyList(); return@launch
            }
            val productResult = runCatching {
                if (isFarmer) repo.farmerProducts()
                else repo.products(location?.latitude, location?.longitude)
            }
            val orderResult = runCatching { repo.orders(role) }
            val notificationResult = runCatching { repo.notifications() }
            productResult.onSuccess { products = it }
                .onFailure { message = it.message ?: "Could not refresh products" }
            orderResult.onSuccess { orders = it }
                .onFailure { message = it.message ?: "Could not refresh orders" }
            notificationResult.onSuccess { notifications = it }
                .onFailure { message = it.message ?: "Could not refresh notifications" }
        }
    }

    // ── Password sign-in and profile completion ────────────────────────────────
    fun continueWithAccount() {
        scope.launch {
            startingSession = true
            runCatching {
                if (repo.live) {
                    val alreadyAuthenticated = hasAuthSession || repo.hasSession()
                    if (!alreadyAuthenticated) {
                        val email = identifier.trim()
                        if (email.isBlank()) error("Enter your email address.")
                        if (!email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
                            error("Enter a valid email address.")
                        }
                        if (password.isBlank()) error("Enter your password.")
                        if (createAccount && displayName.isBlank()) error("Enter your full name to create an account.")
                        if (createAccount && password.length < 8) error("Use a password with at least 8 characters.")

                        if (createAccount) repo.signUp(email, password)
                        else repo.signIn(email, password)

                        if (!repo.hasSession()) {
                            if (createAccount) {
                                prefs.edit().putString("display_name", displayName.trim()).apply()
                                message = "Account created. Confirm the link in your email, then sign in with your password."
                                createAccount = false
                                startingSession = false
                                return@launch
                            }
                            error("No session was created. Check the Supabase email confirmation settings.")
                        }
                    }

                    hasAuthSession = true
                    if (displayName.isBlank()) displayName = repo.profileName().orEmpty()
                    if (location == null) {
                        message = "Sign-in succeeded. Set your location, then continue to finish your profile."
                        startingSession = false
                        return@launch
                    }
                    repo.ensureProfile(role, displayName, location!!.latitude, location!!.longitude)
                } else if (displayName.isBlank()) {
                    error("Enter your name to continue in demo mode.")
                }
                prefs.edit().putString("display_name", displayName.trim()).apply()
                signedIn = true
                showNameEntry = false
                message = ""
                refresh()
            }.onFailure { error ->
                val detail = error.message.orEmpty()
                message = detail.lineSequence().firstOrNull()?.take(180)
                    ?: "Could not sign in. Check your connection and Supabase settings."
            }
            startingSession = false
        }
    }
    LaunchedEffect(Unit) {
        val hasSession = runCatching { repo.hasSession() }.getOrDefault(false)
        hasAuthSession = hasSession
        var profileExists = false
        if (hasSession) {
            val profileName = runCatching { repo.profileName() }.getOrNull()
            if (!profileName.isNullOrBlank()) {
                displayName = profileName
                profileExists = true
            }
        }
        signedIn = if (repo.live) hasSession && profileExists else displayName.isNotBlank()
        showNameEntry = !signedIn
        refresh()
    }

    LaunchedEffect(signedIn) {
        if (signedIn) {
            if (repo.live) repo.subscribeChanges(this) { refresh() }
            refresh()
        }
    }

    LaunchedEffect(location?.latitude, location?.longitude, signedIn) {
        val loc = location ?: return@LaunchedEffect
        if (signedIn && repo.live) {
            runCatching {
                repo.ensureProfile(role, displayName, loc.latitude, loc.longitude)
            }.onFailure { message = it.message ?: "Could not sync location" }
        }
        refresh()
    }

    // ── Render ────────────────────────────────────────────────────────────────
    Farm2MarketTheme {
      ProvideAppLanguage(language) {
        Scaffold(
            topBar = {
                F2MAppBar(
                    language = language,
                    onLanguageChange = { selectLanguage(it) },
                    statusText = if (showNameEntry) null else if (repo.live) "Live" else "Demo mode",
                    locationReady = location != null,
                    onRequestLocation = if (showNameEntry) null else ({ requestLocation() }),
                    onRefresh = if (showNameEntry) null else ({ refresh() })
                )
            }
        ) { appPadding ->
            Box(Modifier.fillMaxSize().padding(appPadding)) {
                if (showNameEntry) {
                    NameEntryScreen(
                        role = role,
                        displayName = displayName,
                        onDisplayName = { displayName = it },
                        identifier = identifier,
                        onIdentifier = { identifier = it },
                        password = password,
                        onPassword = { password = it },
                        createAccount = createAccount,
                        onModeChange = { createAccount = it; message = "" },
                        sessionPresent = hasAuthSession,
                        location = location,
                        onRequestLocation = { requestLocation() },
                        busy = startingSession,
                        statusMessage = message,
                        liveMode = repo.live,
                        onContinue = { continueWithAccount() },
                        onContinueDemo = {
                            if (displayName.isBlank()) message = "Enter your name to continue in demo mode."
                            else {
                                signedIn = true
                                showNameEntry = false
                                refresh()
                            }
                        }
                    )
                } else if (isFarmer) {
                    FarmerApp(
                        repo = repo,
                        location = location,
                        onRequestLocation = { requestLocation() },
                        displayName = displayName,
                        onDisplayName = { displayName = it },
                        products = products,
                        orders = orders,
                        notifications = notifications,
                        onNotificationsOpened = {
                            if (repo.live) scope.launch {
                                runCatching { repo.markNotificationsRead(); notifications = repo.notifications() }
                                    .onFailure { message = it.message ?: "Could not update notifications" }
                            }
                        },
                        message = message,
                        onMessage = { message = it },
                        onRefresh = { refresh() },
                        onSignOut = {
                            scope.launch {
                                runCatching { repo.signOut() }.onSuccess {
                                    signedIn = false; hasAuthSession = false; showNameEntry = true
                                    products = FarmRepository.demoProducts; orders = emptyList()
                                    message = "Signed out"
                                }.onFailure { message = it.message ?: "Could not sign out" }
                            }
                        },
                        onShowAuth = { message = ""; showNameEntry = true }
                    )
                } else {
                    CustomerApp(
                        repo = repo,
                        location = location,
                        onRequestLocation = { requestLocation() },
                        displayName = displayName,
                        onDisplayName = { displayName = it },
                        products = products,
                        orders = orders,
                        notifications = notifications,
                        onNotificationsOpened = {
                            if (repo.live) scope.launch {
                                runCatching { repo.markNotificationsRead(); notifications = repo.notifications() }
                                    .onFailure { message = it.message ?: "Could not update notifications" }
                            }
                        },
                        cart = cart,
                        onCart = { cart = it },
                        message = message,
                        onMessage = { message = it },
                        onRefresh = { refresh() },
                        onPlaceOrder = { deliveryAddress, contactPhone ->
                            scope.launch {
                                runCatching {
                                    val loc = location ?: error("Set your location before placing an order")
                                    if (repo.live) {
                                        repo.ensureProfile(role, displayName, loc.latitude, loc.longitude)
                                        repo.placeOrder(cart, loc.latitude, loc.longitude, deliveryAddress, contactPhone)
                                    }
                                    cart = emptyList()
                                    message = if (repo.live) "Order placed 🎉" else "Demo order placed 🎉"
                                    refresh()
                                }.onFailure { message = it.message ?: "Order failed" }
                            }
                        },
                        onSignOut = {
                            scope.launch {
                                runCatching { repo.signOut() }.onSuccess {
                                    signedIn = false; hasAuthSession = false; showNameEntry = true
                                    cart = emptyList(); products = FarmRepository.demoProducts
                                    orders = emptyList(); message = "Signed out"
                                }.onFailure { message = it.message ?: "Could not sign out" }
                            }
                        },
                        onShowAuth = { message = ""; showNameEntry = true }
                    )
                }
            }
        }
      }
    }
}
