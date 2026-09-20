package com.kitap.app.ui.screens.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import com.kitap.app.data.dto.BookDto
import com.kitap.app.data.dto.DonationDto
import com.kitap.app.data.dto.RequestDto
import com.kitap.app.data.dto.SwapListingDto
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.theme.AdminTheme
import com.kitap.app.ui.theme.EspressoYuzey
import com.kitap.app.ui.theme.EspressoZemin
import com.kitap.app.ui.theme.KoyuSoluk
import com.kitap.app.ui.theme.Vurgu

@Composable
fun AdminIcerikScreen(
    onBack: () -> Unit,
    viewModel: AdminIcerikViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    AdminIcerikContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onTabSelect = viewModel::setTab,
        onRemoveDonation = viewModel::removeDonation,
        onRemoveSwap = viewModel::removeSwapBook,
        onRetry = viewModel::loadData,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIcerikContent(
    state: AdminIcerikUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onTabSelect: (AdminIcerikTab) -> Unit,
    onRemoveDonation: (Long) -> Unit,
    onRemoveSwap: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var removingItem by remember { mutableStateOf<Pair<String, Long>?>(null) } // Pair(Type, Id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İçerik Moderasyonu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EspressoZemin),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(EspressoZemin),
        ) {
            TabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = EspressoZemin,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Tab(
                    selected = state.selectedTab == AdminIcerikTab.BAGISLAR,
                    onClick = { onTabSelect(AdminIcerikTab.BAGISLAR) },
                    text = { Text("Bağışlar (${state.donations.size})") },
                )
                Tab(
                    selected = state.selectedTab == AdminIcerikTab.ISTEKLER,
                    onClick = { onTabSelect(AdminIcerikTab.ISTEKLER) },
                    text = { Text("İstekler (${state.requests.size})") },
                )
                Tab(
                    selected = state.selectedTab == AdminIcerikTab.TAKASLAR,
                    onClick = { onTabSelect(AdminIcerikTab.TAKASLAR) },
                    text = { Text("Takas (${state.swaps.size})") },
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
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
                    else -> {
                        when (state.selectedTab) {
                            AdminIcerikTab.BAGISLAR -> {
                                if (state.donations.isEmpty()) {
                                    EmptyStateView(
                                        icon = Icons.Outlined.VolunteerActivism,
                                        title = "Açık Bağış Yok",
                                        message = "Şu anda yayında açık bağış bulunmuyor.",
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        items(state.donations, key = { it.id }) { donation ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(donation.book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                        Text(donation.book.author.orEmpty(), style = MaterialTheme.typography.bodySmall, color = KoyuSoluk)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { removingItem = Pair("Bağış", donation.id) },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Vurgu),
                                                    ) {
                                                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.size(4.dp))
                                                        Text("Kaldır")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            AdminIcerikTab.ISTEKLER -> {
                                if (state.requests.isEmpty()) {
                                    EmptyStateView(
                                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                                        title = "Açık İstek Yok",
                                        message = "Karşılanmayı bekleyen kitap isteği bulunmuyor.",
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        items(state.requests, key = { it.id }) { req ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(req.book.title.ifBlank { "Kitap" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                        Text("İsteyen: ${req.requesterName ?: "Öğrenci"}", style = MaterialTheme.typography.bodySmall, color = KoyuSoluk)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            AdminIcerikTab.TAKASLAR -> {
                                if (state.swaps.isEmpty()) {
                                    EmptyStateView(
                                        icon = Icons.Outlined.SwapHoriz,
                                        title = "Takas İlanı Yok",
                                        message = "Şu anda takasta kitap bulunmuyor.",
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        items(state.swaps, key = { it.id }) { swap ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(swap.book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                        Text("${swap.book.author} · Sahibi: ${swap.ownerName ?: "Üye"}", style = MaterialTheme.typography.bodySmall, color = KoyuSoluk)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { removingItem = Pair("Takas", swap.id) },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Vurgu),
                                                    ) {
                                                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.size(4.dp))
                                                        Text("Kaldır")
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
    }

    removingItem?.let { (type, id) ->
        AlertDialog(
            onDismissRequest = { removingItem = null },
            title = { Text("$type İlanını Kaldır") },
            text = { Text("Bu $type ilanını yayından kaldırmak istediğinize emin misiniz? İlan sahibine bildirim gönderilecektir.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (type == "Bağış") onRemoveDonation(id)
                        else if (type == "Takas") onRemoveSwap(id)
                        removingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Vurgu),
                ) {
                    Text("Kaldır")
                }
            },
            dismissButton = {
                TextButton(onClick = { removingItem = null }) {
                    Text("İptal")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminIcerikPreview() {
    AdminTheme {
        Surface {
            AdminIcerikContent(
                state = AdminIcerikUiState(
                    donations = listOf(
                        DonationDto(id = 1, book = BookDto(id = 1, title = "Kuyucaklı Yusuf", author = "Sabahattin Ali")),
                        DonationDto(id = 2, book = BookDto(id = 2, title = "İnce Memed", author = "Yaşar Kemal")),
                    ),
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBack = {},
                onTabSelect = {},
                onRemoveDonation = {},
                onRemoveSwap = {},
                onRetry = {},
            )
        }
    }
}
