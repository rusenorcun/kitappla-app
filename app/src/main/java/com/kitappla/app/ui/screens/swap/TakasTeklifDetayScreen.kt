package com.kitappla.app.ui.screens.swap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.PersonOff
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kitappla.app.BuildConfig
import com.kitappla.app.data.dto.BookDto
import com.kitappla.app.data.dto.MeetingDto
import com.kitappla.app.data.dto.OfferDto
import com.kitappla.app.data.dto.OfferStatus
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.repo.resolveCoverUrl
import com.kitappla.app.ui.screens.common.ActionButtonsRow
import com.kitappla.app.ui.screens.common.ArrangeMeetingDialog
import com.kitappla.app.ui.screens.common.BookCover
import com.kitappla.app.ui.screens.common.ButtonLabel
import com.kitappla.app.ui.screens.common.CompactButtonPadding
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.MeetingCard
import com.kitappla.app.ui.screens.common.StatusBadge
import com.kitappla.app.ui.screens.common.UserAvatar
import com.kitappla.app.ui.theme.Adacayi
import com.kitappla.app.ui.theme.AdacayiMurekkep
import com.kitappla.app.ui.theme.AdacayiSoft
import com.kitappla.app.ui.theme.KahveSoluk
import com.kitappla.app.ui.theme.KitapplaTheme
import com.kitappla.app.ui.theme.Vurgu
import com.kitappla.app.ui.theme.VurguSoft

@Composable
fun TakasTeklifDetayScreen(
    onBack: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    onReport: (String, Long) -> Unit,
    viewModel: TakasTeklifDetayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    TakasTeklifDetayContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onNavigateToChat = onNavigateToChat,
        onReport = onReport,
        onAccept = viewModel::acceptOffer,
        onReject = viewModel::rejectOffer,
        onCancel = viewModel::cancelOffer,
        onHandover = viewModel::handoverOffer,
        onArrangeMeeting = viewModel::arrangeMeeting,
        onNoShow = viewModel::noShowOffer,
        onRetry = viewModel::loadData,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakasTeklifDetayContent(
    state: TakasTeklifDetayUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    onReport: (String, Long) -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    onHandover: () -> Unit,
    onArrangeMeeting: (Long?, String?, String) -> Unit,
    onNoShow: () -> Unit,
    onRetry: () -> Unit,
) {
    var showMeetingDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takas Teklifi İncele", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    ErrorStateView(
                        message = state.error,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                state.offer != null -> {
                    val offer = state.offer
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Header Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusBadge(
                                text = OfferStatus.label(offer.status),
                                containerColor = if (offer.status == OfferStatus.PENDING) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (offer.status == OfferStatus.PENDING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                text = if (state.isIncoming) "Gelen Teklif" else "Giden Teklif",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = KahveSoluk,
                            )
                        }

                        // Counterpart Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    val initials = offer.counterpartInitials
                                        ?: offer.counterpartName?.take(2)
                                        ?: "K"
                                    UserAvatar(
                                        initials = initials,
                                        size = 44,
                                    )
                                    Column {
                                        Text(
                                            text = offer.counterpartName ?: "Kullanıcı",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = if (state.isIncoming) "Sana teklif gönderdi" else "Teklif gönderdin",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = KahveSoluk,
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (offer.conversationId != null) {
                                        IconButton(onClick = { onNavigateToChat(offer.conversationId) }) {
                                            Icon(
                                                Icons.AutoMirrored.Outlined.Chat,
                                                contentDescription = "Mesaj",
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onReport("swap_offer", offer.id) }) {
                                        Icon(
                                            Icons.Outlined.Flag,
                                            contentDescription = "Şikâyet et",
                                            tint = KahveSoluk,
                                        )
                                    }
                                }
                            }
                        }

                        // Side by side book comparison
                        Text(
                            text = "Kitap Karşılaştırması",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )

                        // Alacağın Kitap
                        offer.takeBook?.let { takeBook ->
                            BookComparisonCard(
                                title = "ALACAĞIN KİTAP",
                                subtitle = "Karşı tarafın kitabı",
                                book = takeBook,
                                badgeColor = AdacayiMurekkep,
                                badgeBg = AdacayiSoft,
                                borderColor = Adacayi.copy(alpha = 0.5f),
                            )
                        }

                        // Vereceğin Kitap
                        offer.giveBook?.let { giveBook ->
                            BookComparisonCard(
                                title = "VERECEĞİN KİTAP",
                                subtitle = "Senin teklif ettiğin",
                                book = giveBook,
                                badgeColor = Vurgu,
                                badgeBg = VurguSoft,
                                borderColor = Vurgu.copy(alpha = 0.4f),
                            )
                        }

                        // Teklif Mesajı
                        if (!offer.message.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "TEKLİF MESAJI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = KahveSoluk,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = offer.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }

                        // Buluşma ve Teslimat Alanı
                        if (offer.status == OfferStatus.ACCEPTED) {
                            val hasMeeting = offer.meeting?.point != null || !offer.meeting?.at.isNullOrBlank()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = "Kampüs İçi Buluşma & Teslimat",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )

                                    if (offer.meeting != null && hasMeeting) {
                                        MeetingCard(meeting = offer.meeting)
                                    }

                                    ActionButtonsRow {
                                        OutlinedButton(
                                            onClick = { showMeetingDialog = true },
                                            contentPadding = CompactButtonPadding,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                        ) {
                                            Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            ButtonLabel(if (hasMeeting) "Buluşmayı Güncelle" else "Buluşma Belirle")
                                        }

                                        if (hasMeeting) {
                                            OutlinedButton(
                                                onClick = onNoShow,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Vurgu),
                                            ) {
                                                Icon(Icons.Outlined.PersonOff, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Gelmedi")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Aksiyon Butonları
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = "İşlem ve Kararlar",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )

                                when {
                                    offer.status == OfferStatus.PENDING -> {
                                        if (state.isIncoming) {
                                            ActionButtonsRow {
                                                OutlinedButton(
                                                    onClick = onReject,
                                                    contentPadding = CompactButtonPadding,
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Vurgu),
                                                    enabled = !state.isActionLoading,
                                                ) {
                                                    Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    ButtonLabel("Reddet")
                                                }
                                                Button(
                                                    onClick = onAccept,
                                                    contentPadding = CompactButtonPadding,
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = AdacayiMurekkep),
                                                    enabled = !state.isActionLoading,
                                                ) {
                                                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    ButtonLabel("Kabul Et")
                                                }
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = onCancel,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                enabled = !state.isActionLoading,
                                            ) {
                                                Text("Teklifi Geri Çek")
                                            }
                                        }
                                    }

                                    offer.status == OfferStatus.ACCEPTED -> {
                                        if (offer.mineHandedOver) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(AdacayiSoft)
                                                    .padding(12.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = "Teslimi onayladın · Karşı taraf bekleniyor",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AdacayiMurekkep,
                                                )
                                            }
                                        } else {
                                            Button(
                                                onClick = onHandover,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                enabled = !state.isActionLoading,
                                            ) {
                                                Icon(Icons.Outlined.SwapHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Kitabı Teslim Ettim")
                                            }
                                        }
                                    }

                                    offer.status == OfferStatus.COMPLETED -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(AdacayiSoft)
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = "Bu takas başarıyla tamamlandı!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AdacayiMurekkep,
                                            )
                                        }
                                    }

                                    else -> {
                                        Text(
                                            text = "Teklif durumu: ${OfferStatus.label(offer.status)}.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = KahveSoluk,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showMeetingDialog) {
        ArrangeMeetingDialog(
            points = state.pickupPoints,
            onDismiss = { showMeetingDialog = false },
            onConfirm = { pointId, note, at ->
                onArrangeMeeting(pointId, note, at)
                showMeetingDialog = false
            },
        )
    }
}

@Composable
private fun BookComparisonCard(
    title: String,
    subtitle: String,
    book: BookDto,
    badgeColor: Color,
    badgeBg: Color,
    borderColor: Color,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = KahveSoluk,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BookCover(
                    title = book.title,
                    author = book.author,
                    coverUrl = resolveCoverUrl(BuildConfig.API_BASE_URL, book.coverUrl),
                    modifier = Modifier.size(width = 65.dp, height = 90.dp),
                    shape = RoundedCornerShape(10.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = book.author.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = KahveSoluk,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TakasTeklifDetayPreview() {
    KitapplaTheme {
        Surface {
            TakasTeklifDetayContent(
                state = TakasTeklifDetayUiState(
                    offer = OfferDto(
                        id = 12,
                        status = OfferStatus.ACCEPTED,
                        counterpartName = "Ayşe Yılmaz",
                        counterpartInitials = "AY",
                        takeBook = BookDto(id = 1, title = "Nutuk", author = "Mustafa Kemal Atatürk"),
                        giveBook = BookDto(id = 2, title = "Kürk Mantolu Madonna", author = "Sabahattin Ali"),
                        meeting = MeetingDto(
                            point = PickupPointDto(name = "Merkez Kütüphane Önü"),
                            at = "2026-09-22T14:00:00Z",
                        ),
                    ),
                    isIncoming = true,
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBack = {},
                onNavigateToChat = {},
                onReport = { _, _ -> },
                onAccept = {},
                onReject = {},
                onCancel = {},
                onHandover = {},
                onArrangeMeeting = { _, _, _ -> },
                onNoShow = {},
                onRetry = {},
            )
        }
    }
}
