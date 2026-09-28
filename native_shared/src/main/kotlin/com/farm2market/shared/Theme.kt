package com.farm2market.shared

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Brand palette ─────────────────────────────────────────────────────────────
val F2MGreenDark       = Color(0xFF1B5E20)
val F2MGreenPrimary    = Color(0xFF2E7D32)
val F2MGreenLight      = Color(0xFF43A047)
val F2MGreenContainer  = Color(0xFFC8E6C9)
val F2MAmber           = Color(0xFFFB8C00)
val F2MAmberContainer  = Color(0xFFFFF3E0)
val F2MBackground      = Color(0xFFF4F8F2)
val F2MSurface         = Color.White
val F2MTextDark        = Color(0xFF1A2E1A)
val F2MTextMuted       = Color(0xFF5D7B5D)
val F2MDivider         = Color(0xFFDDE8DD)
val F2MSurfaceVariant  = Color(0xFFECF4EC)

// ── Order-status colours ───────────────────────────────────────────────────────
val StatusPendingBg        = Color(0xFFFFF3E0)
val StatusPendingFg        = Color(0xFFE65100)
val StatusAcceptedBg       = Color(0xFFE3F2FD)
val StatusAcceptedFg       = Color(0xFF0D47A1)
val StatusReadyBg          = Color(0xFFF3E5F5)
val StatusReadyFg          = Color(0xFF4A148C)
val StatusDeliveryBg       = Color(0xFFE0F7FA)
val StatusDeliveryFg       = Color(0xFF006064)
val StatusDeliveredBg      = Color(0xFFE8F5E9)
val StatusDeliveredFg      = Color(0xFF1B5E20)
val StatusCancelledBg      = Color(0xFFFFEBEE)
val StatusCancelledFg      = Color(0xFFB71C1C)

// ── Status helpers ────────────────────────────────────────────────────────────
fun statusBackground(status: String) = when (status) {
    "pending"          -> StatusPendingBg
    "accepted"         -> StatusAcceptedBg
    "ready"            -> StatusReadyBg
    "out_for_delivery" -> StatusDeliveryBg
    "delivered"        -> StatusDeliveredBg
    else               -> StatusCancelledBg
}

fun statusForeground(status: String) = when (status) {
    "pending"          -> StatusPendingFg
    "accepted"         -> StatusAcceptedFg
    "ready"            -> StatusReadyFg
    "out_for_delivery" -> StatusDeliveryFg
    "delivered"        -> StatusDeliveredFg
    else               -> StatusCancelledFg
}

fun statusLabel(status: String) = when (status) {
    "pending"          -> "⏳ Pending"
    "accepted"         -> "✅ Accepted"
    "ready"            -> "📦 Ready"
    "out_for_delivery" -> "🚚 On the way"
    "delivered"        -> "🎉 Delivered"
    else               -> status.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

// ── Material 3 Colour Scheme ──────────────────────────────────────────────────
private val F2MColorScheme = lightColorScheme(
    primary             = F2MGreenPrimary,
    onPrimary           = Color.White,
    primaryContainer    = F2MGreenContainer,
    onPrimaryContainer  = F2MGreenDark,
    secondary           = F2MAmber,
    onSecondary         = Color.White,
    secondaryContainer  = F2MAmberContainer,
    onSecondaryContainer= Color(0xFFE65100),
    background          = F2MBackground,
    onBackground        = F2MTextDark,
    surface             = F2MSurface,
    onSurface           = F2MTextDark,
    surfaceVariant      = F2MSurfaceVariant,
    onSurfaceVariant    = F2MTextMuted,
    error               = Color(0xFFC62828),
    onError             = Color.White,
    outline             = F2MDivider,
    outlineVariant      = Color(0xFFD0DDD0)
)

// ── Typography ────────────────────────────────────────────────────────────────
private val F2MTypography = Typography(
    displaySmall    = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.25).sp),
    headlineLarge   = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 24.sp),
    headlineMedium  = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 20.sp),
    headlineSmall   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 18.sp),
    titleLarge      = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 18.sp),
    titleMedium     = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 16.sp),
    titleSmall      = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 14.sp),
    bodyLarge       = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 16.sp),
    bodyMedium      = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 14.sp),
    bodySmall       = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 12.sp),
    labelLarge      = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 14.sp),
    labelMedium     = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 12.sp),
    labelSmall      = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 11.sp)
)

@Composable
fun Farm2MarketTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = F2MColorScheme,
        typography  = F2MTypography,
        content     = content
    )
}
