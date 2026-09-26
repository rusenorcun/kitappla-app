package com.kitappla.app.ui.screens.donation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Flag
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
import com.kitappla.app.data.dto.ClaimStatus
import com.kitappla.app.data.dto.MeetingDto
import com.kitappla.app.data.dto.MyClaimDto
import com.kitappla.app.data.dto.PickupPointDto
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
fun AldiklarimScreen(
    onBack: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    onReportClaim: (Long) -> Unit,
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
        onArrangeMeeting = viewModel::arrangeMeeting,
        onNoShow = viewModel::noShow,
        onChat = onNavigateToChat,
        onReport = onReportClaim,
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
    onArrangeMeeting: (claimId: Long, pointId: Long?, note: String?, at: String) -> Unit,
    onNoShow: (claimId: Long) -> Unit,
    onChat: (Long) -> Unit,
    onReport: (claimId: Long) -> Unit,
    onRetry: () -> Unit,
) {
    var thankDialogClaimId by remember { mutableStateOf<Long?>(null) }
    var arrangeMeetingClaimId by remember { mutableStateOf<Long?>(null) }

    if (thankDialogClaimId != null) {
        ThankDialog(
            onDismiss = { thankDialogClaimId = null },
            onConfirm = { message ->
                onThank(thankDialogClaimId!!, message)
                thankDialogClaimId = null
            },
        )
    }

    if (arrangeMeetingClaimId != null) {
        ArrangeMeetingDialog(
            points = state.pickupPoints,
            onDismiss = { arrangeMeetingClaimId = null },
            onConfirm = { pointId, note, at ->
                onArrangeMeeting(arrangeMeetingClaimId!!, pointId, note, at)
                arrangeMeetingClaimId = null
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
                    title = "Henüz Kitap Almadınız",
                    message = "Keşfet sayfasından ihtiyacın olan kitapları inceleyip talep edebilirsin.",
                    actionText = "Kitapları Keşfet",
                    onAction = onExplore,
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

                                // Stepper (Eşleşti -> Buluşma -> Teslim)
                                ClaimDeliveryStepper(status = item.status)

                                if (item.meeting != null) {
                                    Spacer(Modifier.height(8.dp))
                                    MeetingCard(
                                        meeting = item.meeting,
                                        onArrangeClick = { arrangeMeetingClaimId = item.id },
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                ActionButtonsRow {
                                    if (item.status != ClaimStatus.DELIVERED && item.status != ClaimStatus.NO_SHOW && item.status != ClaimStatus.CANCELLED) {
                                        OutlinedButton(
                                            onClick = { arrangeMeetingClaimId = item.id },
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

                                    if (item.status != ClaimStatus.DELIVERED && item.status != ClaimStatus.NO_SHOW && item.status != ClaimStatus.CANCELLED && item.meeting != null && !item.meeting.at.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = { onNoShow(item.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            contentPadding = CompactButtonPadding,
                                            modifier = Modifier.weight(0.8f),
                                        ) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(2.dp))
                                            ButtonLabel("Gelmedi", fontSize = 11.sp)
                                        }
                                    }

                                    if (TransferRules.canThank(item.status)) {
                                        Button(
                                            onClick = { thankDialogClaimId = item.id },
                                            contentPadding = CompactButtonPadding,
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            ButtonLabel("Teşekkür Et", fontSize = 12.sp)
                                        }
                                    }

                                    if (TransferRules.canCancelClaim(item.status)) {
                                        OutlinedButton(
                                            onClick = { onCancel(item.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            contentPadding = CompactButtonPadding,
                                            modifier = Modifier.weight(0.8f),
                                        ) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(2.dp))
                                            ButtonLabel("İptal", fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    item.conversationId?.let { convId ->
                                        IconButton(onClick = { onChat(convId) }) {
                                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Sohbet")
                                        }
                                    }
                                    IconButton(onClick = { onReport(item.id) }) {
                                        Icon(Icons.Outlined.Flag, contentDescription = "Şikâyet Et")
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

@Composable
private fun ClaimDeliveryStepper(status: String) {
    if (status == ClaimStatus.CANCELLED || status == ClaimStatus.NO_SHOW) return

    val step1Active = true
    val step2Active = status == ClaimStatus.ARRANGED || status == ClaimStatus.SHIPPED || status == ClaimStatus.DELIVERED
    val step3Active = status == ClaimStatus.DELIVERED

    val activeColor = AdacayiMurekkep
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(if (step1Active) activeColor else inactiveColor, RoundedCornerShape(7.dp)),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(if (step2Active) activeColor else inactiveColor),
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(if (step2Active) activeColor else inactiveColor, RoundedCornerShape(7.dp)),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(if (step3Active) activeColor else inactiveColor),
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(if (step3Active) activeColor else inactiveColor, RoundedCornerShape(7.dp)),
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Eşleşti", style = MaterialTheme.typography.labelSmall, color = if (step1Active) activeColor else inactiveColor)
            Text("Buluşma", style = MaterialTheme.typography.labelSmall, color = if (step2Active) activeColor else inactiveColor)
            Text("Teslim", style = MaterialTheme.typography.labelSmall, color = if (step3Active) activeColor else inactiveColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AldiklarimScreenPreview() {
    KitapplaTheme {
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
                onArrangeMeeting = { _, _, _, _ -> },
                onNoShow = {},
                onChat = {},
                onReport = {},
                onRetry = {},
            )
        }
    }
}
