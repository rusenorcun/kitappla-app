package com.kitap.app.ui.screens.request

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
import com.kitap.app.data.dto.MeetingDto
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.data.dto.RequestDto
import com.kitap.app.data.dto.RequestStatus
import com.kitap.app.data.dto.TransferRules
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

@Composable
fun KarsiladiklarimScreen(
    onBack: () -> Unit,
    onNavigateToRequests: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    viewModel: KarsiladiklarimViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    KarsiladiklarimContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onBrowseRequests = onNavigateToRequests,
        onArrangeMeeting = viewModel::arrangeMeeting,
        onNoShow = viewModel::noShow,
        onChat = onNavigateToChat,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KarsiladiklarimContent(
    state: KarsiladiklarimState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onBrowseRequests: () -> Unit,
    onArrangeMeeting: (requestId: Long, pointId: Long?, note: String?, at: String) -> Unit,
    onNoShow: (Long) -> Unit,
    onChat: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var arrangeDialogRequestId by remember { mutableStateOf<Long?>(null) }

    if (arrangeDialogRequestId != null) {
        ArrangeMeetingDialog(
            points = state.pickupPoints,
            onDismiss = { arrangeDialogRequestId = null },
            onConfirm = { pointId, note, at ->
                onArrangeMeeting(arrangeDialogRequestId!!, pointId, note, at)
                arrangeDialogRequestId = null
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Karşıladıklarım", fontWeight = FontWeight.Bold) },
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
                title = "Karşıladığınız İstek Yok",
                message = "İstekler sayfasına göz atarak öğrencilerin ihtiyaç duyduğu kitapları karşılayabilirsiniz.",
                actionText = "İsteklere Göz At",
                onAction = onBrowseRequests,
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
                                            text = TransferRules.label(item.status, receiving = false) ?: "Karşılandı",
                                            containerColor = if (item.status == RequestStatus.DELIVERED) AdacayiSoft else MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = if (item.status == RequestStatus.DELIVERED) AdacayiMurekkep else MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                UserAvatar(initials = item.requesterInitials ?: "İ", size = 28)
                                Text(
                                    text = "İsteyen: ${item.requesterName ?: "Üye"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            if (item.meeting != null) {
                                Spacer(Modifier.height(10.dp))
                                MeetingCard(
                                    meeting = item.meeting,
                                    onArrangeClick = { arrangeDialogRequestId = item.id },
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                // Teslimi karşılayan değil isteyen (kitabı alan) onaylar (backend); burada buluşma ve "gelmedi".
                                if (TransferRules.isInProgress(item.status)) {
                                    OutlinedButton(
                                        onClick = { arrangeDialogRequestId = item.id },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Buluşma", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { onNoShow(item.id) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(0.8f),
                                    ) {
                                        Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("Gelmedi", fontSize = 11.sp)
                                    }
                                }

                                item.conversationId?.let { convId ->
                                    IconButton(onClick = { onChat(convId) }) {
                                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Mesaj")
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
private fun KarsiladiklarimScreenPreview() {
    KitapTheme {
        Surface {
            KarsiladiklarimContent(
                state = KarsiladiklarimState(
                    loading = false,
                    items = listOf(
                        RequestDto(
                            id = 1L,
                            book = BookDto(title = "Böyle Söyledi Zerdüşt", author = "Friedrich Nietzsche"),
                            requesterName = "Canan Er",
                            requesterInitials = "CE",
                            status = RequestStatus.ARRANGED,
                            meeting = MeetingDto(
                                point = PickupPointDto(1L, "Kütüphane Kafe"),
                                at = "Cuma 14:00",
                            ),
                            conversationId = 8L,
                        ),
                    ),
                ),
                onBack = {},
                onBrowseRequests = {},
                onArrangeMeeting = { _, _, _, _ -> },
                onNoShow = {},
                onChat = {},
                onRetry = {},
            )
        }
    }
}
