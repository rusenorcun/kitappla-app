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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kitap.app.data.dto.SwapBookStatus
import com.kitap.app.data.dto.SwapListingDto
import com.kitap.app.data.repo.resolveCoverUrl
import com.kitap.app.ui.screens.common.BookCover
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.StatusBadge
import com.kitap.app.ui.theme.AdacayiMurekkep
import com.kitap.app.ui.theme.AdacayiSoft
import com.kitap.app.ui.theme.KitapTheme
import com.kitap.app.ui.theme.Vurgu
import com.kitap.app.ui.theme.VurguSoft

@Composable
fun TakasKitaplarimScreen(
    onBack: () -> Unit,
    viewModel: TakasKitaplarimViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    TakasKitaplarimContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onAddBook = viewModel::addBook,
        onSetStatus = viewModel::setStatus,
        onMoveToDonation = viewModel::moveToDonation,
        onRemoveBook = viewModel::removeBook,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakasKitaplarimContent(
    state: TakasKitaplarimState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onAddBook: (title: String, author: String?, note: String?, link: String?) -> Unit,
    onSetStatus: (Long, String) -> Unit,
    onMoveToDonation: (Long) -> Unit,
    onRemoveBook: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var author by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var link by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Takasa Kitap Ekle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Kitap Adı *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Yazar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Takas Notu (Hangi kitaplarla takas edersiniz?)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("Kitap Linki (İsteğe bağlı)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddBook(title, author.ifBlank { null }, note.ifBlank { null }, link.ifBlank { null })
                            showAddDialog = false
                        }
                    },
                    enabled = title.isNotBlank(),
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("İptal") }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Takas Kitaplarım", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Add, contentDescription = "Kitap Ekle")
                    Spacer(Modifier.width(6.dp))
                    Text("Kitap Ekle", fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        when {
            state.loading && state.items.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null && state.items.isEmpty() -> ErrorStateView(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier.padding(padding),
            )
            state.items.isEmpty() -> EmptyStateView(
                title = "Takasa Açık Kitabınız Yok",
                message = "Başkalarının kitaplarıyla takas etmek istediğiniz kitapları ekleyebilirsiniz.",
                actionText = "Kitap Ekle",
                onAction = { showAddDialog = true },
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.book.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            if (!item.book.author.isNullOrBlank()) {
                                                Text(
                                                    text = item.book.author,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }

                                        val isOpen = item.status == SwapBookStatus.OPEN
                                        StatusBadge(
                                            text = if (isOpen) "Takasa Açık" else "Pasif",
                                            containerColor = if (isOpen) AdacayiSoft else VurguSoft,
                                            contentColor = if (isOpen) AdacayiMurekkep else Vurgu,
                                        )
                                    }
                                }
                            }

                            if (!item.note.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Not: ${item.note}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                val isOpen = item.status == SwapBookStatus.OPEN
                                OutlinedButton(
                                    onClick = { onSetStatus(item.id, if (isOpen) SwapBookStatus.CLOSED else SwapBookStatus.OPEN) },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(if (isOpen) Icons.Outlined.Lock else Icons.Outlined.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isOpen) "Durdur" else "Aç", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onMoveToDonation(item.id) },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Outlined.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Bağışa Taşı", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onRemoveBook(item.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(0.7f),
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(2.dp))
                                    Text("Sil", fontSize = 12.sp)
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
private fun TakasKitaplarimScreenPreview() {
    KitapTheme {
        Surface {
            TakasKitaplarimContent(
                state = TakasKitaplarimState(
                    loading = false,
                    items = listOf(
                        SwapListingDto(
                            id = 1L,
                            book = BookDto(title = "Kozmos", author = "Carl Sagan"),
                            note = "Bilim kurgu veya romanlarla takas olur.",
                            status = SwapBookStatus.OPEN,
                        ),
                    ),
                ),
                onBack = {},
                onAddBook = { _, _, _, _ -> },
                onSetStatus = { _, _ -> },
                onMoveToDonation = {},
                onRemoveBook = {},
                onRetry = {},
            )
        }
    }
}
