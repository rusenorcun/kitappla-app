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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
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
import com.kitap.app.data.dto.ClaimDto
import com.kitap.app.data.dto.DonationStatus
import com.kitap.app.data.dto.MeetingDto
import com.kitap.app.data.dto.MyDonationDto
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.data.dto.TransferRules
import com.kitap.app.data.dto.isDonationClosed
import com.kitap.app.data.repo.resolveCoverUrl
import com.kitap.app.ui.screens.common.ArrangeMeetingDialog
import com.kitap.app.ui.screens.common.BookCover
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.MeetingCard
import com.kitap.app.ui.screens.common.StatusBadge
import com.kitap.app.ui.screens.common.SuccessBadge
import com.kitap.app.ui.screens.common.UserAvatar
import com.kitap.app.ui.theme.AdacayiMurekkep
import com.kitap.app.ui.theme.AdacayiSoft
import com.kitap.app.ui.theme.KitapTheme
import com.kitap.app.ui.theme.Vurgu
import com.kitap.app.ui.theme.VurguSoft

@Composable
fun BagislarimScreen(
    onBack: () -> Unit,
    onNavigateToNewDonation: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    viewModel: BagislarimViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    BagislarimContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onNewDonation = onNavigateToNewDonation,
        onArrangeMeeting = viewModel::arrangeMeeting,
        onNoShowClaim = viewModel::noShowClaim,
        onCloseDonation = viewModel::closeDonation,
        onReopenDonation = viewModel::reopenDonation,
        onDeleteDonation = viewModel::deleteDonation,
        onChat = onNavigateToChat,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagislarimContent(
    state: BagislarimState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onNewDonation: () -> Unit,
    onArrangeMeeting: (claimId: Long, pointId: Long?, note: String?, at: String) -> Unit,
    onNoShowClaim: (Long) -> Unit,
    onCloseDonation: (Long) -> Unit,
    onReopenDonation: (Long) -> Unit,
    onDeleteDonation: (Long) -> Unit,
    onChat: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var arrangeDialogClaimId by remember { mutableStateOf<Long?>(null) }

    if (arrangeDialogClaimId != null) {
        ArrangeMeetingDialog(
            points = state.pickupPoints,
            onDismiss = { arrangeDialogClaimId = null },
            onConfirm = { pointId, note, at ->
                onArrangeMeeting(arrangeDialogClaimId!!, pointId, note, at)
                arrangeDialogClaimId = null
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Bağışlarım", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = onNewDonation) {
                        Icon(Icons.Outlined.Add, contentDescription = "Yeni Bağış")
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
                title = "Henüz Bağış Yapmadınız",
                message = "Elindeki kitapları paylaşarak öğrencilere ve okurlara destek olabilirsin.",
                actionText = "Yeni Bağış Yap",
                onAction = onNewDonation,
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            StatusBadge(
                                                text = "${item.remaining}/${item.quantity} kaldı",
                                                containerColor = AdacayiSoft,
                                                contentColor = AdacayiMurekkep,
                                            )
                                            val isClosed = isDonationClosed(item.status)
                                            StatusBadge(
                                                text = if (isClosed) "Kapandı" else "Açık",
                                                containerColor = if (isClosed) VurguSoft else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isClosed) Vurgu else MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Gelen Talepler (Claims)
                            if (item.claims.isNotEmpty()) {
                                Text(
                                    text = "Gelen Talepler (${item.claims.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(Modifier.height(8.dp))

                                item.claims.forEach { claim ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    UserAvatar(initials = claim.requesterInitials ?: "T", size = 32)
                                                    Text(
                                                        text = claim.requesterName ?: "Talep Eden",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                    )
                                                }
                                                StatusBadge(
                                                    text = TransferRules.label(claim.status, receiving = false) ?: "Talep Edildi",
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                )
                                            }

                                            if (claim.meeting != null) {
                                                Spacer(Modifier.height(8.dp))
                                                MeetingCard(
                                                    meeting = claim.meeting,
                                                    onArrangeClick = { arrangeDialogClaimId = claim.id },
                                                )
                                            }

                                            Spacer(Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                // Teslimi bağışçı değil kitabı alan onaylar (backend); burada yalnızca buluşma ve "gelmedi".
                                                if (TransferRules.isInProgress(claim.status)) {
                                                    OutlinedButton(
                                                        onClick = { arrangeDialogClaimId = claim.id },
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(horizontal = 4.dp),
                                                    ) {
                                                        Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Buluşma", fontSize = 11.sp)
                                                    }

                                                    OutlinedButton(
                                                        onClick = { onNoShowClaim(claim.id) },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                        contentPadding = PaddingValues(horizontal = 4.dp),
                                                    ) {
                                                        Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Gelmedi", fontSize = 11.sp)
                                                    }
                                                }

                                                claim.conversationId?.let { convId ->
                                                    IconButton(onClick = { onChat(convId) }) {
                                                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Mesaj")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Henüz talep gelmedi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                val isClosed = isDonationClosed(item.status)
                                if (!isClosed) {
                                    OutlinedButton(onClick = { onCloseDonation(item.id) }) {
                                        Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Kapat", fontSize = 12.sp)
                                    }
                                } else {
                                    OutlinedButton(onClick = { onReopenDonation(item.id) }) {
                                        Icon(Icons.Outlined.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Yeniden Aç", fontSize = 12.sp)
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
private fun BagislarimScreenPreview() {
    KitapTheme {
        Surface {
            BagislarimContent(
                state = BagislarimState(
                    loading = false,
                    items = listOf(
                        MyDonationDto(
                            id = 1L,
                            book = BookDto(title = "Kırmızı Pazartesi", author = "Gabriel García Márquez"),
                            quantity = 2,
                            claimed = 1,
                            remaining = 1,
                            status = DonationStatus.OPEN,
                            claims = listOf(
                                ClaimDto(
                                    id = 10L,
                                    requesterName = "Zeynep Kaya",
                                    requesterInitials = "ZK",
                                    status = ClaimStatus.ARRANGED,
                                    meeting = MeetingDto(
                                        point = PickupPointDto(1L, "Kütüphane Önü"),
                                        at = "Yarın 15:00",
                                    ),
                                    conversationId = 5L,
                                ),
                            ),
                        ),
                    ),
                ),
                onBack = {},
                onNewDonation = {},
                onArrangeMeeting = { _, _, _, _ -> },
                onNoShowClaim = {},
                onCloseDonation = {},
                onReopenDonation = {},
                onDeleteDonation = {},
                onChat = {},
                onRetry = {},
            )
        }
    }
}
