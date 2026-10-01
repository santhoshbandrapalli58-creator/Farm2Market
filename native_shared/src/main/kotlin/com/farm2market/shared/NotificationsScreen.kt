package com.farm2market.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NotificationsScreen(notifications: List<AppNotification>) {
    if (notifications.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            EmptyState("🔔", "No notifications", "Order updates will appear here.")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Notifications", style = MaterialTheme.typography.headlineMedium, color = F2MGreenPrimary)
        }
        items(notifications, key = { it.id }) { notification ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (notification.readAt == null) F2MGreenContainer else Color.White
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(notification.title, fontWeight = FontWeight.SemiBold, color = F2MGreenDark)
                    Text(notification.body, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        notification.createdAt.replace('T', ' ').take(16),
                        style = MaterialTheme.typography.labelSmall,
                        color = F2MTextMuted
                    )
                }
            }
        }
    }
}
