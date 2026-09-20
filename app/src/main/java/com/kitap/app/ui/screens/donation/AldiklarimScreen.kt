package com.kitap.app.ui.screens.donation

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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.kitap.app.data.dto.ClaimStatus
import com.kitap.app.data.dto.MeetingDto
import com.kitap.app.data.dto.MyClaimDto
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.data.dto.TransferRules
import com.kitap.app.data.repo.resolveCoverUrl
import com.kitap.app.ui.screens.common.BookCover
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.MeetingCard
import com.kitap.app.ui.screens.common.StatusBadge
import com.kitap.app.ui.screens.common.ThankDialog
import com.kitap.app.ui.screens.common.UserAvatar
import com.kitap.app.ui.theme.AdacayiMurekkep
import com.kitap.app.ui.theme.AdacayiSoft
import com.kitap.app.ui.theme.KitapTheme

@Composable
fun AldiklarimScreen(
    onBack: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    viewModel: AldiklarimViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    AldiklarimContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onExplore = onNavigateToExplore,
        onDeliver = viewModel::deliver,
        onThank = viewModel::thank,
        onCancel = viewModel::cancel,
        onChat = onNavigateToChat,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AldiklarimContent(
    state: AldiklarimState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onExplore: () -> Unit,
    onDeliver: (claimId: Long) -> Unit,
    onThank: (claimId: Long, message: String?) -> Unit,
    onCancel: (claimId: Long) -> Unit,
    onChat: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var thankDialogClaimId by remember { mutableStateOf<Long?>(null) }

    if (thankDialogClaimId != null) {
        ThankDialog(
            onDismiss = { thankDialogClaimId = null },
            onConfirm = { message ->
                onThank(thankDialogClaimId!!, message)
                thankDialogClaimId = null
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Aldıklarım", fontWeight = FontWeight.Bold) },
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
                title = "Henüz Kitap Almadınız",
                message = "Keşfet sayfasından ihtiyacın olan kitapları inceleyip talep edebilirsin.",
                actionText = "Kitapları Keşfet",
                onAction = onExplore,
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

                                        StatusBadge(
                                            text = TransferRules.label(item.status, receiving = true) ?: "Bekliyor",
                                            containerColor = if (item.status == ClaimStatus.DELIVERED) AdacayiSoft else MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = if (item.status == ClaimStatus.DELIVERED) AdacayiMurekkep else MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                UserAvatar(initials = item.donorInitials ?: "B", size = 28)
                                Text(
                                    text = "Bağışçı: ${item.donorName ?: "Üye"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            if (item.meeting != null) {
                                Spacer(Modifier.height(10.dp))
                                MeetingCard(meeting = item.meeting)
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                item.conversationId?.let { convId ->
                                    OutlinedButton(
                                        onClick = { onChat(convId) },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Mesajlaş", fontSize = 12.sp)
                                    }
                                }

                                // Kurallar backend/web ile aynı (bkz. TransferRules): teslim aldım → teşekkür; iptal yalnızca MATCHED.
                                if (TransferRules.canConfirmReceipt(item.status)) {
                                    Button(
                                        onClick = { onDeliver(item.id) },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Teslim Aldım", fontSize = 12.sp)
                                    }
                                }

                                if (TransferRules.canThank(item.status)) {
                                    Button(
                                        onClick = { thankDialogClaimId = item.id },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Teşekkür Et", fontSize = 12.sp)
                                    }
                                }

                                if (TransferRules.canCancelClaim(item.status)) {
                                    OutlinedButton(
                                        onClick = { onCancel(item.id) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(0.8f),
                                    ) {
                                        Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("İptal", fontSize = 12.sp)
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
private fun AldiklarimScreenPreview() {
    KitapTheme {
        Surface {
            AldiklarimContent(
                state = AldiklarimState(
                    loading = false,
                    items = listOf(
                        MyClaimDto(
                            id = 1L,
                            book = BookDto(title = "Dönüşüm", author = "Franz Kafka"),
                            donorName = "Mehmet Demir",
                            donorInitials = "MD",
                            status = ClaimStatus.ARRANGED,
                            meeting = MeetingDto(
                                point = PickupPointDto(1L, "Yemekhane Önü"),
                                at = "Perşembe 12:30",
                            ),
                            conversationId = 12L,
                        ),
                    ),
                ),
                onBack = {},
                onExplore = {},
                onDeliver = {},
                onThank = { _, _ -> },
                onCancel = {},
                onChat = {},
                onRetry = {},
            )
        }
    }
}
