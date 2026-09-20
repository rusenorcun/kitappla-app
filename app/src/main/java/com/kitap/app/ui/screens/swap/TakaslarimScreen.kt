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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.SwapHoriz
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.kitap.app.data.dto.OfferStatus
import com.kitap.app.data.dto.MeetingDto
import com.kitap.app.data.dto.OfferDto
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.data.repo.resolveCoverUrl
import com.kitap.app.ui.screens.common.ArrangeMeetingDialog
import com.kitap.app.ui.screens.common.BookCover
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.MeetingCard
import com.kitap.app.ui.screens.common.StatusBadge
import com.kitap.app.ui.screens.common.UserAvatar
import com.kitap.app.ui.theme.AdacayiMurekkep
import com.kitap.app.ui.theme.AdacayiSoft
import com.kitap.app.ui.theme.KitapTheme
import com.kitap.app.ui.theme.Vurgu
import com.kitap.app.ui.theme.VurguSoft

@Composable
fun TakaslarimScreen(
    onBack: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    viewModel: TakaslarimViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    TakaslarimContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onTabSelect = viewModel::setTab,
        onAccept = viewModel::acceptOffer,
        onReject = viewModel::rejectOffer,
        onCancel = viewModel::cancelOffer,
        onHandover = viewModel::handoverOffer,
        onArrangeMeeting = viewModel::arrangeMeeting,
        onNoShow = viewModel::noShowOffer,
        onChat = onNavigateToChat,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakaslarimContent(
    state: TakaslarimState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onTabSelect: (Int) -> Unit,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onHandover: (Long) -> Unit,
    onArrangeMeeting: (id: Long, pointId: Long?, note: String?, at: String) -> Unit,
    onNoShow: (Long) -> Unit,
    onChat: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var arrangeDialogOfferId by remember { mutableStateOf<Long?>(null) }

    if (arrangeDialogOfferId != null) {
        ArrangeMeetingDialog(
            points = state.pickupPoints,
            onDismiss = { arrangeDialogOfferId = null },
            onConfirm = { pointId, note, at ->
                onArrangeMeeting(arrangeDialogOfferId!!, pointId, note, at)
                arrangeDialogOfferId = null
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Takaslarım", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { onTabSelect(0) },
                    text = { Text("Gelen Teklifler (${state.incoming.size})", fontWeight = FontWeight.SemiBold) },
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { onTabSelect(1) },
                    text = { Text("Giden Teklifler (${state.outgoing.size})", fontWeight = FontWeight.SemiBold) },
                )
            }

            val currentOffers = if (state.selectedTab == 0) state.incoming else state.outgoing

            when {
                state.loading && currentOffers.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null && currentOffers.isEmpty() -> ErrorStateView(
                    message = state.error,
                    onRetry = onRetry,
                )
                currentOffers.isEmpty() -> EmptyStateView(
                    title = if (state.selectedTab == 0) "Gelen Teklif Yok" else "Giden Teklif Yok",
                    message = if (state.selectedTab == 0) "Kitaplarınıza henüz bir takas teklifi gelmedi." else "Henüz bir takas teklifinde bulunmadınız.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(currentOffers, key = { it.id }) { offer ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        UserAvatar(initials = offer.counterpartInitials ?: "T", size = 30)
                                        Text(
                                            text = offer.counterpartName ?: "Üye",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }

                                    StatusBadge(
                                        text = when (offer.status) {
                                            OfferStatus.ACCEPTED -> "Kabul Edildi"
                                            OfferStatus.REJECTED -> "Reddedildi"
                                            OfferStatus.CANCELLED -> "İptal Edildi"
                                            OfferStatus.COMPLETED -> "Teslim Edildi"
                                            else -> "Bekliyor"
                                        },
                                        containerColor = when (offer.status) {
                                            OfferStatus.ACCEPTED -> AdacayiSoft
                                            OfferStatus.REJECTED, OfferStatus.CANCELLED -> VurguSoft
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        contentColor = when (offer.status) {
                                            OfferStatus.ACCEPTED -> AdacayiMurekkep
                                            OfferStatus.REJECTED, OfferStatus.CANCELLED -> Vurgu
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                // Kitaplar Karşılaştırması
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            if (offer.takeBook != null) {
                                                BookCover(
                                                    title = offer.takeBook.title,
                                                    author = offer.takeBook.author,
                                                    coverUrl = resolveCoverUrl(BuildConfig.API_BASE_URL, offer.takeBook.coverUrl),
                                                    modifier = Modifier.size(36.dp, 50.dp),
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Alınacak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = offer.takeBook?.title ?: "Kitap",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2,
                                                )
                                            }
                                        }
                                    }

                                    Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            if (offer.giveBook != null) {
                                                BookCover(
                                                    title = offer.giveBook.title,
                                                    author = offer.giveBook.author,
                                                    coverUrl = resolveCoverUrl(BuildConfig.API_BASE_URL, offer.giveBook.coverUrl),
                                                    modifier = Modifier.size(36.dp, 50.dp),
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Verilecek", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = offer.giveBook?.title ?: "Kitap",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2,
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!offer.message.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Mesaj: ${offer.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                if (offer.meeting != null) {
                                    Spacer(Modifier.height(10.dp))
                                    MeetingCard(
                                        meeting = offer.meeting,
                                        onArrangeClick = { arrangeDialogOfferId = offer.id },
                                    )
                                }

                                Spacer(Modifier.height(14.dp))

                                // Eylemler
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (state.selectedTab == 0 && offer.status == OfferStatus.PENDING) {
                                        Button(
                                            onClick = { onAccept(offer.id) },
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Kabul Et", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { onReject(offer.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Reddet", fontSize = 12.sp)
                                        }
                                    }

                                    if (state.selectedTab == 1 && offer.status == OfferStatus.PENDING) {
                                        OutlinedButton(
                                            onClick = { onCancel(offer.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Teklifi İptal Et", fontSize = 12.sp)
                                        }
                                    }

                                    if (offer.status == OfferStatus.ACCEPTED) {
                                        OutlinedButton(
                                            onClick = { arrangeDialogOfferId = offer.id },
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Buluşma", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { onHandover(offer.id) },
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Teslim Ettim", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { onNoShow(offer.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(0.8f),
                                        ) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text("Gelmedi", fontSize = 11.sp)
                                        }
                                    }

                                    offer.conversationId?.let { convId ->
                                        IconButton(onClick = { onChat(convId) }) {
                                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Sohbet")
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
private fun TakaslarimScreenPreview() {
    KitapTheme {
        Surface {
            TakaslarimContent(
                state = TakaslarimState(
                    loading = false,
                    selectedTab = 0,
                    incoming = listOf(
                        OfferDto(
                            id = 1L,
                            counterpartName = "Ali Vural",
                            counterpartInitials = "AV",
                            status = OfferStatus.PENDING,
                            takeBook = BookDto(title = "Nutuk", author = "Mustafa Kemal Atatürk"),
                            giveBook = BookDto(title = "Şu Çılgın Türkler", author = "Turgut Özakman"),
                            message = "Kampüs merkezinde buluşabiliriz.",
                        ),
                    ),
                ),
                onBack = {},
                onTabSelect = {},
                onAccept = {},
                onReject = {},
                onCancel = {},
                onHandover = {},
                onArrangeMeeting = { _, _, _, _ -> },
                onNoShow = {},
                onChat = {},
                onRetry = {},
            )
        }
    }
}
