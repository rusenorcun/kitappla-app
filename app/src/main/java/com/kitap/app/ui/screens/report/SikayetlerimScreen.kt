package com.kitap.app.ui.screens.report

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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.kitap.app.data.dto.MyReportDto
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.StatusBadge
import com.kitap.app.ui.screens.common.SuccessBadge
import com.kitap.app.ui.theme.KahveSoluk
import com.kitap.app.ui.theme.KitapTheme
import com.kitap.app.ui.theme.Vurgu
import com.kitap.app.ui.theme.VurguSoft

@Composable
fun SikayetlerimScreen(
    onBack: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    viewModel: SikayetlerimViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SikayetlerimContent(
        state = state,
        onBack = onBack,
        onChat = onNavigateToChat,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SikayetlerimContent(
    state: SikayetlerimState,
    onBack: () -> Unit,
    onChat: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Şikâyetlerim", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
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
                title = "Şikâyetiniz Yok",
                message = "İlettiğiniz tüm kural ihlali bildirimleri ve durumları burada listelenecektir.",
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = item.reasonLabel ?: item.reason,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )

                                when (item.status) {
                                    "ACTIONED" -> SuccessBadge("İşlem Yapıldı")
                                    "DISMISSED" -> StatusBadge("Reddedildi", containerColor = VurguSoft, contentColor = Vurgu)
                                    else -> StatusBadge("İnceleniyor", containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                }
                            }

                            if (!item.kindLabel.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Tür: ${item.kindLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KahveSoluk,
                                )
                            }

                            if (!item.note.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Açıklamanız: ${item.note}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }

                            if (!item.adminNote.isNullOrBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Moderatör Yanıtı:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(2.dp))
                                        Text(text = item.adminNote, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            item.conversationId?.let { convId ->
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(onClick = { onChat(convId) }) {
                                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Destek Sohbetine Git", fontSize = 12.sp)
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
private fun SikayetlerimScreenPreview() {
    KitapTheme {
        Surface {
            SikayetlerimContent(
                state = SikayetlerimState(
                    loading = false,
                    items = listOf(
                        MyReportDto(
                            id = 1L,
                            kind = "DONATION",
                            kindLabel = "Bağış İlanı",
                            reason = "YANILTICI",
                            reasonLabel = "Yanıltıcı İçerik",
                            note = "Kitap baskısı orijinal değil, fotokopi.",
                            status = "ACTIONED",
                            adminNote = "İlan yayından kaldırıldı ve kullanıcı uyarıldı.",
                            conversationId = 3L,
                        ),
                    ),
                ),
                onBack = {},
                onChat = {},
                onRetry = {},
            )
        }
    }
}
