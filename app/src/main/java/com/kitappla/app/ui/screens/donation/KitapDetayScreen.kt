package com.kitappla.app.ui.screens.donation

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.School
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
import com.kitappla.app.data.dto.DonationDto
import com.kitappla.app.data.dto.EligibilityDto
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.data.dto.isDonationClosed
import com.kitappla.app.data.repo.resolveCoverUrl
import com.kitappla.app.ui.screens.common.ActionButtonsRow
import com.kitappla.app.ui.screens.common.AlertBox
import com.kitappla.app.ui.screens.common.BookCover
import com.kitappla.app.ui.screens.common.ButtonLabel
import com.kitappla.app.ui.screens.common.CompactButtonPadding
import com.kitappla.app.ui.screens.common.EmptyStateView
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.PriorityBadge
import com.kitappla.app.ui.screens.common.ReportDialog
import com.kitappla.app.ui.screens.common.StatusBadge
import com.kitappla.app.ui.screens.common.UserAvatar
import com.kitappla.app.ui.theme.AdacayiMurekkep
import com.kitappla.app.ui.theme.AdacayiSoft
import com.kitappla.app.ui.theme.KahveSoluk
import com.kitappla.app.ui.theme.KitapplaTheme
import com.kitappla.app.ui.theme.KremCizgi
import com.kitappla.app.ui.theme.VurguSoft

@Composable
fun KitapDetayScreen(
    onBack: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    onNavigateToSwap: (Long) -> Unit,
    /** Misafirde dolu: talep ve şikâyet sunucuya gitmez (401 "oturum süresi doldu" verirdi), girişe götürür. */
    onRequireLogin: (() -> Unit)? = null,
    viewModel: KitapDetayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    LaunchedEffect(state.claimSuccess) {
        state.claimSuccess?.conversationId?.let { convId ->
            onNavigateToChat(convId)
        }
    }

    KitapDetayContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onClaim = viewModel::claim,
        onClose = viewModel::close,
        onReopen = viewModel::reopen,
        onMoveToSwap = { viewModel.moveToSwap(onNavigateToSwap) },
        onDelete = { viewModel.delete(onBack) },
        onReport = viewModel::report,
        onRetry = viewModel::load,
        onRequireLogin = onRequireLogin,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitapDetayContent(
    state: KitapDetayState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onClaim: () -> Unit,
    onClose: () -> Unit,
    onReopen: () -> Unit,
    onMoveToSwap: () -> Unit,
    onDelete: () -> Unit,
    onReport: (reason: String, note: String?) -> Unit,
    onRetry: () -> Unit,
    onRequireLogin: (() -> Unit)? = null,
) {
    var showReportDialog by remember { mutableStateOf(false) }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onConfirm = { reason, note ->
                showReportDialog = false
                onReport(reason, note)
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Kitap Detayı", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    val isOwner = state.donation?.mine == true || (state.donation?.donorId != null && state.donation?.donorId == state.currentUserId)
                    if (!isOwner && state.donation != null) {
                        IconButton(onClick = { onRequireLogin?.invoke() ?: run { showReportDialog = true } }) {
                            Icon(Icons.Outlined.Flag, contentDescription = "Şikâyet Et")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> ErrorStateView(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier.padding(padding),
            )
            state.donation == null -> EmptyStateView(
                title = "Bağış Bulunamadı",
                message = "Bu ilan yayından kaldırılmış olabilir.",
                modifier = Modifier.padding(padding),
            )
            else -> {
                val donation = state.donation
                val book = donation.book
                val isClosed = isDonationClosed(donation.status)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                BookCover(
                                    title = book.title,
                                    author = book.author,
                                    coverUrl = resolveCoverUrl(BuildConfig.API_BASE_URL, book.coverUrl),
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(136.dp),
                                    shape = RoundedCornerShape(10.dp),
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    if (donation.priorityActive) {
                                        PriorityBadge(text = donation.priorityLeft ?: "Öğrenci Önceliği")
                                        Spacer(Modifier.height(6.dp))
                                    }

                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )

                                    if (!book.author.isNullOrBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = book.author,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = KahveSoluk,
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        StatusBadge(
                                            text = "${donation.remaining} adet kaldı",
                                            containerColor = AdacayiSoft,
                                            contentColor = AdacayiMurekkep,
                                        )
                                        donation.targetLevel?.let {
                                            StatusBadge(text = it, containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            if (!donation.description.isNullOrBlank() || !book.description.isNullOrBlank()) {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = donation.description ?: book.description.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                UserAvatar(initials = donation.donorInitials ?: "B")
                                Column {
                                    Text(
                                        text = donation.donorName ?: "Bağışçı",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Bağışlayan Üye",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KahveSoluk,
                                    )
                                }
                            }

                            if (donation.point != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp),
                                ) {
                                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = donation.point.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }

                    val isOwner = donation.mine || (donation.donorId != null && donation.donorId == state.currentUserId)

                    if (isOwner) {
                        Spacer(Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Bu sizin bağış ilanınız",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = "İlanınızı yönetebilir, takasa taşıyabilir veya durumu kapatıp açabilirsiniz.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KahveSoluk,
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        ActionButtonsRow {
                            OutlinedButton(
                                onClick = onMoveToSwap,
                                enabled = !state.actionLoading,
                                contentPadding = CompactButtonPadding,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Outlined.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                ButtonLabel("Takasa Taşı", fontSize = 12.sp)
                            }

                            if (!isClosed) {
                                OutlinedButton(
                                    onClick = onClose,
                                    enabled = !state.actionLoading,
                                    contentPadding = CompactButtonPadding,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    ButtonLabel("Kapat", fontSize = 12.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onReopen,
                                    enabled = !state.actionLoading,
                                    contentPadding = CompactButtonPadding,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Outlined.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    ButtonLabel("Yeniden Aç", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = onDelete,
                                enabled = !state.actionLoading,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                contentPadding = CompactButtonPadding,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                ButtonLabel("Sil", fontSize = 12.sp)
                            }
                        }
                    } else {
                        if (donation.eligibility?.allowed == false && donation.eligibility.reason != null) {
                            Spacer(Modifier.height(12.dp))
                            AlertBox(message = donation.eligibility.reason, isError = true)
                        }

                        Spacer(Modifier.height(20.dp))

                        val canClaim = donation.eligibility?.allowed != false && donation.remaining > 0 && !isClosed

                        Button(
                            onClick = onRequireLogin ?: onClaim,
                            enabled = canClaim && !state.actionLoading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                        ) {
                            if (state.actionLoading) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    when {
                                        isClosed -> "İlan Kapalı"
                                        donation.remaining <= 0 -> "Tükendi"
                                        onRequireLogin != null -> "Talep etmek için giriş yap"
                                        else -> "Kitabı Talep Et"
                                    },
                                )
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
private fun KitapDetayScreenPreview() {
    KitapplaTheme {
        Surface {
            KitapDetayContent(
                state = KitapDetayState(
                    loading = false,
                    donation = DonationDto(
                        id = 1L,
                        book = BookDto(
                            id = 1L,
                            title = "Tutunamayanlar",
                            author = "Oğuz Atay",
                            description = "Türk edebiyatının en önemli eserlerinden biri. Temiz durumda, altı çizili sayfa yok.",
                        ),
                        quantity = 2,
                        remaining = 1,
                        donorName = "Ahmet Yılmaz",
                        donorInitials = "AY",
                        targetLevel = "Üniversite",
                        priorityActive = true,
                        priorityLeft = "28 saat kaldı",
                        point = PickupPointDto(1L, "Kadıköy Merkez Kütüphanesi"),
                    ),
                ),
                onBack = {},
                onClaim = {},
                onClose = {},
                onReopen = {},
                onMoveToSwap = {},
                onDelete = {},
                onReport = { _, _ -> },
                onRetry = {},
            )
        }
    }
}
