package com.kitappla.app.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Liste sona yaklaşınca (son 4 öğe görünürken) [onLoadMore]'u çağırır; Keşfet'teki sayfalama kalıbı. Çağrı her yeni
 * sayfadan ve yükleme bitişinden sonra yeniden değerlendirilir; durma koşulu (bitti/hata/sürüyor) ViewModel'dedir.
 */
@Composable
fun LoadMoreWhenNearEnd(listState: LazyListState, itemCount: Int, loadingMore: Boolean, onLoadMore: () -> Unit) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val nearEnd by remember(listState) {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= listState.layoutInfo.totalItemsCount - 4
        }
    }
    LaunchedEffect(nearEnd, itemCount, loadingMore) { if (nearEnd) currentOnLoadMore() }
}

/** Liste sonu: sonraki sayfa yüklenirken gösterge, yükleme hata verdiyse mesaj ve "Tekrar deneyin". */
fun LazyListScope.pagingFooter(loadingMore: Boolean, error: String?, onRetry: () -> Unit) {
    when {
        loadingMore -> item(key = "sayfa-yukleniyor") {
            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null -> item(key = "sayfa-hatasi") {
            Column(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = onRetry) { Text("Tekrar deneyin") }
            }
        }
    }
}
