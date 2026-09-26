package com.kitappla.app.ui.screens.kesfet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.BuildConfig
import com.kitappla.app.data.dto.BookDto
import com.kitappla.app.data.dto.DonationDto
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.ui.screens.common.PullRefresh
import com.kitappla.app.ui.theme.KitapplaTheme

@Composable
fun KesfetScreen(onOpenBook: (Long) -> Unit, viewModel: KesfetViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    KesfetScreenContent(
        state = state,
        onOpenBook = onOpenBook,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        onRefresh = viewModel::refresh,
    )
}

@Composable
fun KesfetScreenContent(
    state: KesfetState,
    onOpenBook: (Long) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
) {
    val listState = rememberLazyListState()

    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= listState.layoutInfo.totalItemsCount - 4
        }
    }
    // loadingMore anahtarı: yalnızca yinelenen kayıt içeren bir sayfa items.size'ı değiştirmese de sayfalama sürer
    // (loadMore hata/bitişte kendini durdurur, sonsuz istek döngüsü oluşmaz).
    LaunchedEffect(nearEnd, state.items.size, state.loadingMore) { if (nearEnd) onLoadMore() }

    // Yukarıdan çekince ağdan yeniler (yeni bağışlar otomatik gelmez).
    PullRefresh(
        loading = state.loading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        scrollableContent = state.items.isNotEmpty(),
    ) {
        when {
            state.loading && state.items.isEmpty() ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

            state.error != null && state.items.isEmpty() ->
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text("Tekrar deneyin") }
                }

            state.items.isEmpty() ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Şu an açık bağış yok.") }

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.items, key = { it.id }) { donation ->
                    DonationCard(donation, coverBaseUrl = BuildConfig.API_BASE_URL, onClick = { onOpenBook(donation.id) })
                }
                if (state.loadingMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                state.error?.let { message ->
                    item {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = onRetry) { Text("Tekrar deneyin") }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KesfetScreenPreview() {
    KitapplaTheme {
        Surface {
            KesfetScreenContent(
                state = KesfetState(
                    items = listOf(
                        DonationDto(
                            id = 1L,
                            book = BookDto(id = 1L, title = "Suç ve Ceza", author = "Fyodor Dostoyevski", coverUrl = null),
                            quantity = 3,
                            remaining = 2,
                            donorName = "Ahmet Yılmaz",
                            point = PickupPointDto(id = 1L, name = "Merkez Kütüphane")
                        ),
                        DonationDto(
                            id = 2L,
                            book = BookDto(id = 2L, title = "Kürk Mantolu Madonna", author = "Sabahattin Ali", coverUrl = null),
                            quantity = 1,
                            remaining = 1,
                            donorName = "Ayşe Demir",
                            point = PickupPointDto(id = 2L, name = "Kadıköy Noktası")
                        )
                    )
                ),
                onOpenBook = {},
                onRetry = {},
                onLoadMore = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KesfetScreenEmptyPreview() {
    KitapplaTheme {
        Surface {
            KesfetScreenContent(
                state = KesfetState(items = emptyList(), loading = false),
                onOpenBook = {},
                onRetry = {},
                onLoadMore = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KesfetScreenErrorPreview() {
    KitapplaTheme {
        Surface {
            KesfetScreenContent(
                state = KesfetState(error = "Bağlantı hatası oluştu.", items = emptyList()),
                onOpenBook = {},
                onRetry = {},
                onLoadMore = {},
                onRefresh = {}
            )
        }
    }
}
