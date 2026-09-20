package com.kitap.app.ui.screens.swap

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.BuildConfig
import com.kitap.app.data.dto.BookDto
import com.kitap.app.data.dto.SwapListingDto
import com.kitap.app.data.repo.resolveCoverUrl
import com.kitap.app.ui.screens.common.BookCover
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.theme.KitapTheme

@Composable
fun TakasTeklifScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    viewModel: TakasTeklifViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TakasTeklifContent(
        state = state,
        onBack = onBack,
        onSelectBook = viewModel::selectBook,
        onMessageChange = viewModel::updateMessage,
        onSubmit = { viewModel.submit(onSuccess) },
        onAddBook = onNavigateToAddBook,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakasTeklifContent(
    state: TakasTeklifState,
    onBack: () -> Unit,
    onSelectBook: (Long) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onAddBook: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takas Teklifi Ver", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.myBooks.isEmpty()) {
            EmptyStateView(
                title = "Takasa Açık Kitabınız Yok",
                message = "Teklif verebilmek için önce kendi kitaplığınızdan bir kitabı takasa açmanız gerekmektedir.",
                actionText = "Takasa Kitap Ekle",
                onAction = onAddBook,
                modifier = Modifier.padding(padding),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Takas için elinizdeki kitaplardan birini seçin:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                state.myBooks.forEach { bookItem ->
                    val isSelected = bookItem.id == state.selectedMyBookId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable { onSelectBook(bookItem.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BookCover(
                                title = bookItem.book.title,
                                author = bookItem.book.author,
                                coverUrl = resolveCoverUrl(BuildConfig.API_BASE_URL, bookItem.book.coverUrl),
                                modifier = Modifier.size(44.dp, 62.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bookItem.book.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (!bookItem.book.author.isNullOrBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = bookItem.book.author,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = state.message,
                    onValueChange = onMessageChange,
                    label = { Text("Teklif Mesajı (İsteğe bağlı)") },
                    placeholder = { Text("Kitabın durumu veya buluşma öneriniz...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onSubmit,
                    enabled = state.selectedMyBookId != null && !state.submitting,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    if (state.submitting) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Teklifi Gönder")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TakasTeklifScreenPreview() {
    KitapTheme {
        Surface {
            TakasTeklifContent(
                state = TakasTeklifState(
                    loading = false,
                    myBooks = listOf(
                        SwapListingDto(id = 1L, book = BookDto(title = "Körlük", author = "José Saramago")),
                        SwapListingDto(id = 2L, book = BookDto(title = "Simyacı", author = "Paulo Coelho")),
                    ),
                    selectedMyBookId = 1L,
                ),
                onBack = {},
                onSelectBook = {},
                onMessageChange = {},
                onSubmit = {},
                onAddBook = {},
            )
        }
    }
}
