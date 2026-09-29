package com.farm2market.farmer

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.farm2market.shared.AppRole
import com.farm2market.shared.Farm2MarketApp

class MainActivity : ComponentActivity() {
    private val authIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authIntent.value = intent
        setContent {
            Farm2MarketApp(AppRole.FARMER, BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY, authIntent.value)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        authIntent.value = intent
    }
}
