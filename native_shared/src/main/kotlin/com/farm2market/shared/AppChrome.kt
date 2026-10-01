package com.farm2market.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun F2MAppBar(
    language: String,
    onLanguageChange: (String) -> Unit,
    statusText: String? = null,
    locationReady: Boolean = false,
    onRequestLocation: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    TopAppBar(
        navigationIcon = {
            Icon(
                painter = painterResource(R.drawable.f2m_logo),
                contentDescription = "F2M",
                tint = Color.Unspecified,
                modifier = Modifier.padding(start = 12.dp).size(42.dp)
            )
        },
        title = {
            statusText?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        },
        actions = {
            onRequestLocation?.let { requestLocation ->
                IconButton(onClick = requestLocation) {
                    Icon(
                        if (locationReady) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        contentDescription = "Location"
                    )
                }
            }
            onRefresh?.let { refresh ->
                IconButton(onClick = refresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
            LanguagePicker(language = language, onLanguageChange = onLanguageChange)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = F2MGreenPrimary,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
