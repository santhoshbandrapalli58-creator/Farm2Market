package com.farm2market.farmer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.farm2market.shared.AppRole
import com.farm2market.shared.Farm2MarketApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Farm2MarketApp(AppRole.FARMER, BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) }
    }
}
