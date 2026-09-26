package com.kitappla.app.ui.screens.message

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.data.dto.ConversationDto
import com.kitappla.app.ui.screens.common.EmptyStateView
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.PullRefresh
import com.kitappla.app.ui.screens.common.StatusBadge
import com.kitappla.app.ui.screens.common.UserAvatar
import com.kitappla.app.ui.theme.KahveSoluk
import com.kitappla.app.ui.theme.KitapplaTheme

@Composable
fun MesajlarScreen(
    onOpenConversation: (Long) -> Unit,
    viewModel: MesajlarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MesajlarContent(
        state = state,
        onOpenConversation = onOpenConversation,
        onRetry = viewModel::load,
    )
}

@Composable
fun MesajlarContent(
    state: MesajlarState,
    onOpenConversation: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold { padding ->
        // Yukarıdan çekince listeyi ağdan yeniler.
        PullRefresh(
            loading = state.loading,
            onRefresh = onRetry,
            modifier = Modifier.fillMaxSize().padding(padding),
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
                    title = "Henüz Mesajınız Yok",
                    message = "Bir bağış, istek veya takas eşleşmesi gerçekleştiğinde sohbetler burada listelenecektir.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenConversation(item.id) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.unread > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                UserAvatar(initials = item.counterpartInitials ?: "M", size = 44)

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = item.counterpartName ?: "Üye",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (item.unread > 0) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        )

                                        if (!item.kind.isNullOrBlank()) {
                                            StatusBadge(
                                                text = when (item.kind) {
                                                    "CLAIM" -> "Bağış"
                                                    "REQUEST" -> "İstek"
                                                    "SWAP" -> "Takas"
                                                    "REPORT" -> "Şikâyet"
                                                    "SUPPORT" -> "Destek"
                                                    else -> item.kind
                                                },
                                                containerColor = MaterialTheme.colorScheme.background,
                                            )
                                        }
                                    }

                                    if (!item.title.isNullOrBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = item.lastMessage ?: "Henüz mesaj yok.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (item.unread > 0) MaterialTheme.colorScheme.onSurface else KahveSoluk,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )

                                        if (item.unread > 0) {
                                            Badge {
                                                Text(item.unread.toString(), fontSize = 11.sp)
                                            }
                                        }
                                    }
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
private fun MesajlarScreenPreview() {
    KitapplaTheme {
        Surface {
            MesajlarContent(
                state = MesajlarState(
                    loading = false,
                    items = listOf(
                        ConversationDto(
                            id = 1L,
                            kind = "CLAIM",
                            counterpartName = "Elif Demir",
                            counterpartInitials = "ED",
                            title = "Nutuk",
                            lastMessage = "Yarın saat 14'te kütüphanede buluşalım mı?",
                            unread = 2,
                        ),
                        ConversationDto(
                            id = 2L,
                            kind = "SWAP",
                            counterpartName = "Mert Yılmaz",
                            counterpartInitials = "MY",
                            title = "Sapiens Takası",
                            lastMessage = "Kitabı teslim ettim, teşekkürler!",
                            unread = 0,
                        ),
                    ),
                ),
                onOpenConversation = {},
                onRetry = {},
            )
        }
    }
}
