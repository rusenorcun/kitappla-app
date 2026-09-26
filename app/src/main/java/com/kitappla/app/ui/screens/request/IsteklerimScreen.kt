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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
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
import com.kitappla.app.BuildConfig
import com.kitappla.app.data.dto.BookDto
import com.kitappla.app.data.dto.MeetingDto
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.dto.RequestDto
import com.kitappla.app.data.dto.RequestStatus
import com.kitappla.app.data.dto.TransferRules
import com.kitappla.app.data.repo.resolveCoverUrl
import com.kitappla.app.ui.screens.common.ActionButtonsRow
import com.kitappla.app.ui.screens.common.ArrangeMeetingDialog
import com.kitappla.app.ui.screens.common.BookCover
import com.kitappla.app.ui.screens.common.ButtonLabel
import com.kitappla.app.ui.screens.common.CompactButtonPadding
import com.kitappla.app.ui.screens.common.EmptyStateView
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.MeetingCard
import com.kitappla.app.ui.screens.common.PullRefresh
import com.kitappla.app.ui.screens.common.StatusBadge
import com.kitappla.app.ui.screens.common.ThankDialog
import com.kitappla.app.ui.screens.common.UserAvatar
import com.kitappla.app.ui.theme.AdacayiMurekkep
import com.kitappla.app.ui.theme.AdacayiSoft
import com.kitappla.app.ui.theme.KitapplaTheme

@Composable
fun IsteklerimScreen(
    onBack: () -> Unit,
    onNavigateToNewRequest: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    viewModel: IsteklerimViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    IsteklerimContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onNewRequest = onNavigateToNewRequest,
        onArrangeMeeting = viewModel::arrangeMeeting,
        onDeliver = viewModel::deliver,
        onThank = viewModel::thank,
        onDelete = viewModel::delete,
        onChat = onNavigateToChat,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsteklerimContent(
    state: IsteklerimState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onNewRequest: () -> Unit,
    onArrangeMeeting: (requestId: Long, pointId: Long?, note: String?, at: String) -> Unit,
    onDeliver: (Long) -> Unit,
    onThank: (requestId: Long, message: String?) -> Unit,
    onDelete: (Long) -> Unit,
    onChat: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var arrangeDialogRequestId by remember { mutableStateOf<Long?>(null) }
    var thankDialogRequestId by remember { mutableStateOf<Long?>(null) }

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

    if (thankDialogRequestId != null) {
        ThankDialog(
            onDismiss = { thankDialogRequestId = null },
            onConfirm = { message ->
                onThank(thankDialogRequestId!!, message)
                thankDialogRequestId = null
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("İsteklerim", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = onNewRequest) {
                        Icon(Icons.Outlined.Add, contentDescription = "Yeni İstek")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
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
                    title = "Henüz İstek Oluşturmadınız",
                    message = "İhtiyacınız olan kitapları listelemek için istek oluşturabilirsiniz.",
                    actionText = "İstek Oluştur",
                    onAction = onNewRequest,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
                                                text = TransferRules.label(item.status, receiving = true) ?: "Açık",
                                                containerColor = if (item.status == RequestStatus.DELIVERED) AdacayiSoft else MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = if (item.status == RequestStatus.DELIVERED) AdacayiMurekkep else MaterialTheme.colorScheme.onPrimaryContainer,
                                            )
                                        }
                                    }
                                }

                                if (!item.fulfilledByName.isNullOrBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        UserAvatar(initials = item.fulfilledByInitials ?: "K", size = 28)
                                        Text(
                                            text = "Karşılayan: ${item.fulfilledByName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }

                                if (item.meeting != null) {
                                    Spacer(Modifier.height(10.dp))
                                    MeetingCard(
                                        meeting = item.meeting,
                                        onArrangeClick = { arrangeDialogRequestId = item.id },
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                ActionButtonsRow {
                                    // Kurallar backend/web ile aynı (bkz. TransferRules): teslim aldım → teşekkür; silme yalnızca OPEN.
                                    if (TransferRules.isInProgress(item.status)) {
                                        OutlinedButton(
                                            onClick = { arrangeDialogRequestId = item.id },
                                            contentPadding = CompactButtonPadding,
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            ButtonLabel("Buluşma", fontSize = 12.sp)
                                        }
                                    }

                                    if (TransferRules.canConfirmReceipt(item.status)) {
                                        Button(
                                            onClick = { onDeliver(item.id) },
                                            contentPadding = CompactButtonPadding,
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            ButtonLabel("Teslim Aldım", fontSize = 12.sp)
                                        }
                                    }

                                    if (TransferRules.canThank(item.status)) {
                                        Button(
                                            onClick = { thankDialogRequestId = item.id },
                                            contentPadding = CompactButtonPadding,
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            ButtonLabel("Teşekkür Et", fontSize = 12.sp)
                                        }
                                    }

                                    item.conversationId?.let { convId ->
                                        OutlinedButton(onClick = { onChat(convId) }) {
                                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Mesaj", fontSize = 12.sp)
                                        }
                                    }

                                    if (TransferRules.canRemoveRequest(item.status)) {
                                        OutlinedButton(
                                            onClick = { onDelete(item.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        ) {
                                            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
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
    }
}

@Preview(showBackground = true)
@Composable
private fun IsteklerimScreenPreview() {
    KitapplaTheme {
        Surface {
            IsteklerimContent(
                state = IsteklerimState(
                    loading = false,
                    items = listOf(
                        RequestDto(
                            id = 1L,
                            book = BookDto(title = "Kalkınma İktisadı", author = "Dani Rodrik"),
                            status = RequestStatus.FULFILLED,
                            fulfilledByName = "Kemal Sunal",
                            fulfilledByInitials = "KS",
                            meeting = MeetingDto(
                                point = PickupPointDto(1L, "İktisat Fakültesi Girişi"),
                                at = "Pazartesi 11:00",
                            ),
                            conversationId = 7L,
                        ),
                    ),
                ),
                onBack = {},
                onNewRequest = {},
                onArrangeMeeting = { _, _, _, _ -> },
                onDeliver = {},
                onThank = { _, _ -> },
                onDelete = {},
                onChat = {},
                onRetry = {},
            )
        }
    }
}
