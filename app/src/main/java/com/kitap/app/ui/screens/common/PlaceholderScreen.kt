package com.kitap.app.ui.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitap.app.ui.theme.KitapTheme

/** İçeriği sonraki turlarda doldurulacak sayfalar için yer tutucu. [onBack] varsa üstte geri okuyla başlık gösterir. */
@Composable
fun PlaceholderScreen(title: String, onBack: (() -> Unit)? = null) {
    Column(Modifier.fillMaxSize()) {
        if (onBack != null) {
            Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (onBack == null) Text(title, style = MaterialTheme.typography.headlineMedium)
                Text("Bu sayfa yakında.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    KitapTheme {
        Surface {
            PlaceholderScreen(title = "Keşfet", onBack = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenNoBackPreview() {
    KitapTheme {
        Surface {
            PlaceholderScreen(title = "Profil")
        }
    }
}
