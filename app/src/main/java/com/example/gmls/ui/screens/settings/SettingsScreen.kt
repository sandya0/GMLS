package com.example.gmls.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.gmls.ui.theme.AppTheme

// Add this enum at the top or in a shared file
// enum class AppTheme { LIGHT, DARK, SYSTEM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.semantics { contentDescription = "Go back" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text("Theme", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = currentTheme == AppTheme.LIGHT,
                    onClick = { onThemeChange(AppTheme.LIGHT) },
                    modifier = Modifier.semantics { contentDescription = "Select light theme" }
                )
                Text("Light", modifier = Modifier.padding(end = 16.dp))
                RadioButton(
                    selected = currentTheme == AppTheme.DARK,
                    onClick = { onThemeChange(AppTheme.DARK) },
                    modifier = Modifier.semantics { contentDescription = "Select dark theme" }
                )
                Text("Dark", modifier = Modifier.padding(end = 16.dp))
                RadioButton(
                    selected = currentTheme == AppTheme.SYSTEM,
                    onClick = { onThemeChange(AppTheme.SYSTEM) },
                    modifier = Modifier.semantics { contentDescription = "Select system default theme" }
                )
                Text("System Default")
            }
        }
    }
}