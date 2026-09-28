package com.farm2market.shared

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NameEntryScreen(
    role: AppRole,
    language: String,
    onLanguageChange: () -> Unit,
    displayName: String,
    onDisplayName: (String) -> Unit,
    identifier: String,
    onIdentifier: (String) -> Unit,
    password: String,
    onPassword: (String) -> Unit,
    createAccount: Boolean,
    onModeChange: (Boolean) -> Unit,
    sessionPresent: Boolean,
    location: Location?,
    onRequestLocation: () -> Unit,
    busy: Boolean,
    statusMessage: String,
    liveMode: Boolean,
    onContinue: () -> Unit,
    onContinueDemo: () -> Unit
) {
    val blue = Color(0xFF4167D5)
    val isFarmer = role == AppRole.FARMER
    val canContinue = !busy && (sessionPresent || (identifier.isNotBlank() && password.isNotBlank()))

    Box(
        Modifier.fillMaxSize().background(Color(0xFFF2F4FB)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FilledTonalButton(onClick = onLanguageChange, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Language, null)
                Text("  ${when (language) { "te" -> "TE"; "hi" -> "HI"; else -> "English" }}")
            }
            Spacer(Modifier.height(14.dp))
            Text(
                if (isFarmer) "Farm2Market Farmer" else "Farm2Market",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF263F91),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                when {
                    sessionPresent -> "Finish setting up your ${if (isFarmer) "farmer" else "customer"} profile."
                    isFarmer -> "Manage your farm and receive nearby customer orders."
                    else -> "Sign in to find fresh produce from nearby farms."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF747B8D),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    Text(
                        when {
                            sessionPresent -> "Profile details"
                            createAccount -> "Create your account"
                            else -> "Sign in"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF263F91),
                        fontWeight = FontWeight.Bold
                    )
                    if (!sessionPresent) {
                        Row(
                            Modifier.fillMaxWidth().background(Color(0xFFF0F2F9), RoundedCornerShape(12.dp)).padding(4.dp)
                        ) {
                            ModeButton("Sign in", !createAccount, blue, Modifier.weight(1f)) { onModeChange(false) }
                            ModeButton("Sign up", createAccount, blue, Modifier.weight(1f)) { onModeChange(true) }
                        }
                        if (createAccount) {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = onDisplayName,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Full name") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = onIdentifier,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email address") },
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = onPassword,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        if (createAccount) {
                            Text("Use at least 8 characters. Email accounts may need to confirm the link sent by Supabase.", color = Color(0xFF747B8D), style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = onDisplayName,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Full name") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = onRequestLocation,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, null)
                        Text(if (location == null) "Set location for nearby orders" else "Location ready")
                    }
                    if (statusMessage.isNotBlank()) {
                        Surface(color = Color(0xFFEFF3FF), shape = RoundedCornerShape(12.dp)) {
                            Text(statusMessage, Modifier.fillMaxWidth().padding(12.dp), color = Color(0xFF263F91), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Button(
                        onClick = onContinue,
                        enabled = canContinue,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = blue)
                    ) {
                        Text(if (busy) "Please wait…" else when {
                            sessionPresent -> "Save and continue"
                            createAccount -> "Create account"
                            else -> "Sign in"
                        })
                    }
                    if (!liveMode) {
                        TextButton(onClick = onContinueDemo, modifier = Modifier.fillMaxWidth()) {
                            Text("Continue in demo mode", color = Color(0xFF69738A))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeButton(label: String, selected: Boolean, blue: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(42.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        color = if (selected) Color.White else Color.Transparent,
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) blue else Color(0xFF747B8D), fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

