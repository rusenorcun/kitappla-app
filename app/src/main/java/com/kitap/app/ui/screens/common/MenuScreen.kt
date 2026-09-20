@file:OptIn(ExperimentalMaterial3Api::class)

package com.kitap.app.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitap.app.ui.theme.KitapTheme

data class MenuEntry(val label: String, val route: String)

/** Panom ve Yönetim Pano'su için basit kart listesi; [onLogout] verilirse altta "Çıkış yap" düğmesi çıkar. */
@Composable
fun MenuScreen(
    title: String,
    entries: List<MenuEntry>,
    onEntry: (String) -> Unit,
    onLogout: (() -> Unit)?,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Text(title, style = MaterialTheme.typography.headlineMedium) }
        items(entries) { entry ->
            ElevatedCard(onClick = { onEntry(entry.route) }, modifier = Modifier.fillMaxWidth()) {
                Text(entry.label, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
        if (onLogout != null) {
            item {
                OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Çıkış yap") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    KitapTheme {
        Surface {
            MenuScreen(
                title = "Yönetim Panosu",
                entries = listOf(
                    MenuEntry("Bağışlar", "donations"),
                    MenuEntry("Teslim Noktaları", "points"),
                    MenuEntry("Kullanıcılar", "users"),
                ),
                onEntry = {},
                onLogout = {},
            )
        }
    }
}
