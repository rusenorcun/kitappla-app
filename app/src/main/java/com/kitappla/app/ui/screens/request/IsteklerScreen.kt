package com.kitappla.app.ui.screens.request

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.BuildConfig
import com.kitappla.app.data.dto.BookDto
import com.kitappla.app.data.dto.RequestDto
import com.kitappla.app.data.repo.resolveCoverUrl
import com.kitappla.app.ui.screens.common.BookCover
import com.kitappla.app.ui.screens.common.EmptyStateView
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.LoadMoreWhenNearEnd
import com.kitappla.app.ui.screens.common.PullRefresh
import com.kitappla.app.ui.screens.common.pagingFooter
import com.kitappla.app.ui.screens.common.UserAvatar
import com.kitappla.app.ui.theme.KitapplaTheme

@Composable
fun IsteklerScreen(
    onNavigateToNewRequest: () -> Unit,
    /** Misafirde dolu: "Bu İsteği Karşıla" sunucuya gitmez (401 "oturum süresi doldu" verirdi), girişe götürür. */
    onRequireLogin: (() -> Unit)? = null,
    viewModel: IsteklerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    IsteklerContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::setQuery,
        onFulfill = if (onRequireLogin != null) ({ _ -> onRequireLogin() }) else viewModel::fulfill,
        onNewRequest = onNavigateToNewRequest,
        onRetry = { viewModel.load(reset = true) },
        onLoadMore = viewModel::loadMore,
        onRetryMore = viewModel::retryMore,
    )
}

@Composable
fun IsteklerContent(
    state: IsteklerState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onQueryChange: (String) -> Unit,
    onFulfill: (Long) -> Unit,
    onNewRequest: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit = {},
    onRetryMore: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    LoadMoreWhenNearEnd(listState, state.items.size, state.loadingMore, onLoadMore)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewRequest,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Add, contentDescription = "İstek Oluştur")
                    Spacer(Modifier.width(6.dp))
                    Text("İstek Oluştur", fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Arama Çubuğu
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Kitap adı veya yazar ara...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Temizle")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
            )

            // Yukarıdan çekince listeyi ağdan yeniler.
            PullRefresh(
                loading = state.loading,
                onRefresh = onRetry,
                modifier = Modifier.fillMaxSize(),
                scrollableContent = state.items.isNotEmpty(),
            ) {
                when {
                    state.loading && state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    state.error != null && state.items.isEmpty() -> ErrorStateView(
                        message = state.error,
                        onRetry = onRetry,
                    )
                    state.items.isEmpty() -> EmptyStateView(
                        title = "Açık İstek Yok",
                        message = if (state.query.isNotBlank()) "Aramanıza uygun kitap isteği bulunamadı." else "Şu anda açık bir kitap isteği bulunmuyor. İlk isteği siz oluşturabilirsiniz!",
                        actionText = "Yeni İstek Oluştur",
                        onAction = onNewRequest,
                    )
                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        UserAvatar(initials = item.requesterInitials ?: "İ", size = 34)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.requesterName ?: "Öğrenci / Okur",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                text = "Kitap Arıyor",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        BookCover(
                                            title = item.book.title,
                                            author = item.book.author,
                                            coverUrl = resolveCoverUrl(BuildConfig.API_BASE_URL, item.book.coverUrl),
                                            modifier = Modifier.size(54.dp, 76.dp),
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.book.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            if (!item.book.author.isNullOrBlank()) {
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = item.book.author,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                            if (!item.description.isNullOrBlank()) {
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    text = item.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 18.sp,
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Button(
                                        onClick = { onFulfill(item.id) },
                                        enabled = !state.actionLoading,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Icon(Icons.Outlined.VolunteerActivism, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Bu İsteği Karşıla")
                                    }
                                }
                            }
                        }
                        pagingFooter(state.loadingMore, state.error, onRetryMore)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IsteklerScreenPreview() {
    KitapplaTheme {
        Surface {
            IsteklerContent(
                state = IsteklerState(
                    loading = false,
                    items = listOf(
                        RequestDto(
                            id = 1L,
                            book = BookDto(title = "İnce Memed 1", author = "Yaşar Kemal"),
                            requesterName = "Merve Çelik",
                            requesterInitials = "MÇ",
                            description = "Edebiyat dersi için okumam gerekiyor, elinde olan paylaşabilir mi?",
                        ),
                    ),
                ),
                onQueryChange = {},
                onFulfill = {},
                onNewRequest = {},
                onRetry = {},
            )
        }
    }
}
