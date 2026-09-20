package com.kitap.app.ui.screens.swap

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.BuildConfig
import com.kitap.app.data.dto.BookDto
import com.kitap.app.data.dto.SwapListingDto
import com.kitap.app.data.repo.resolveCoverUrl
import com.kitap.app.ui.screens.common.BookCover
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.UserAvatar
import com.kitap.app.ui.theme.KitapTheme

@Composable
fun TakasScreen(
    onNavigateToMyBooks: () -> Unit,
    onProposeSwap: (Long) -> Unit,
    viewModel: TakasViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TakasContent(
        state = state,
        onQueryChange = viewModel::setQuery,
        onMyBooks = onNavigateToMyBooks,
        onProposeSwap = onProposeSwap,
        onRetry = { viewModel.load(reset = true) },
    )
}

@Composable
fun TakasContent(
    state: TakasState,
    onQueryChange: (String) -> Unit,
    onMyBooks: () -> Unit,
    onProposeSwap: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Takas Kitaplarım Hızlı Erişim Kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Takas Kitaplarım", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Kendi kitaplarını ekle ve yönet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(onClick = onMyBooks) {
                        Text("Yönet", fontSize = 12.sp)
                    }
                }
            }

            // Arama Kutusu
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Takaslık kitap veya yazar ara...") },
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
            )

            when {
                state.loading && state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null && state.items.isEmpty() -> ErrorStateView(
                    message = state.error,
                    onRetry = onRetry,
                )
                state.items.isEmpty() -> EmptyStateView(
                    title = "Takaslık Kitap Yok",
                    message = if (state.query.isNotBlank()) "Aramanıza uygun takas ilanı bulunamadı." else "Şu anda takasa açık kitap bulunmuyor. Kendi kitabınızı takasa ekleyerek başlayabilirsiniz!",
                    actionText = "Kitap Ekle",
                    onAction = onMyBooks,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
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
                                    UserAvatar(initials = item.ownerInitials ?: "T", size = 32)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.ownerName ?: "Kitap Sahibi",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = "Takasa Açtı",
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
                                        if (!item.note.isNullOrBlank()) {
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = "Not: ${item.note}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(14.dp))

                                Button(
                                    onClick = { onProposeSwap(item.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(Icons.Outlined.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Takas Teklifi Ver")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TakasScreenPreview() {
    KitapTheme {
        Surface {
            TakasContent(
                state = TakasState(
                    loading = false,
                    items = listOf(
                        SwapListingDto(
                            id = 1L,
                            book = BookDto(title = "Sapiens", author = "Yuval Noah Harari"),
                            ownerName = "Burak Yıldız",
                            ownerInitials = "BY",
                            note = "Tarih veya felsefe kitaplarıyla takas etmek isterim.",
                        ),
                    ),
                ),
                onQueryChange = {},
                onMyBooks = {},
                onProposeSwap = {},
                onRetry = {},
            )
        }
    }
}
