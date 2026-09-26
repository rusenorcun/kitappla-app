package com.kitappla.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.DeleteOutline
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.ui.screens.common.EmptyStateView
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.PullRefresh
import com.kitappla.app.ui.screens.common.StatusBadge
import com.kitappla.app.ui.theme.EspressoYuzey
import com.kitappla.app.ui.theme.EspressoZemin
import com.kitappla.app.ui.theme.KoyuSoluk
import com.kitappla.app.ui.theme.Vurgu

private data class RemovingTarget(val type: String, val id: Long, val title: String)

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
        onRemoveRequest = viewModel::removeRequest,
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
    onRemoveDonation: (Long, String?) -> Unit,
    onRemoveRequest: (Long, String?) -> Unit,
    onRemoveSwap: (Long, String?) -> Unit,
    onRetry: () -> Unit,
) {
    var removingTarget by remember { mutableStateOf<RemovingTarget?>(null) }
    var removalReason by remember { mutableStateOf("") }

    if (removingTarget != null) {
        val target = removingTarget!!
        AlertDialog(
            onDismissRequest = {
                removingTarget = null
                removalReason = ""
            },
            title = { Text("${target.type} Kaldır", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "\"${target.title}\" başlıklı ${target.type.lowercase()} yayından kaldırılacak ve sahibine bildirim gönderilecektir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KoyuSoluk,
                    )
                    OutlinedTextField(
                        value = removalReason,
                        onValueChange = { removalReason = it },
                        label = { Text("Kaldırma Gerekçesi (İsteğe bağlı)") },
                        placeholder = { Text("Örn. Kural ihlali, telif, uygunsuz içerik...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = EspressoYuzey,
                            unfocusedContainerColor = EspressoYuzey,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = removalReason.ifBlank { null }
                        when (target.type) {
                            "Bağış" -> onRemoveDonation(target.id, reason)
                            "İstek" -> onRemoveRequest(target.id, reason)
                            "Takas" -> onRemoveSwap(target.id, reason)
                        }
                        removingTarget = null
                        removalReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Vurgu),
                ) {
                    Text("Kaldır")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        removingTarget = null
                        removalReason = ""
                    },
                ) {
                    Text("İptal")
                }
            },
        )
    }

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
                // Yukarıdan çekince listeyi ağdan yeniler.
                PullRefresh(
                    loading = state.isLoading,
                    onRefresh = onRetry,
                    modifier = Modifier.fillMaxSize(),
                    scrollableContent = when (state.selectedTab) { AdminIcerikTab.BAGISLAR -> state.donations.isNotEmpty(); AdminIcerikTab.ISTEKLER -> state.requests.isNotEmpty(); AdminIcerikTab.TAKASLAR -> state.swaps.isNotEmpty() },
                ) {
                    when {
                        state.isLoading && state.donations.isEmpty() && state.requests.isEmpty() && state.swaps.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        state.error != null && state.donations.isEmpty() && state.requests.isEmpty() && state.swaps.isEmpty() -> {
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
                                            message = "Şu anda yayında olan bağış ilanı bulunmuyor.",
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            items(state.donations, key = { it.id }) { don ->
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
                                                            Text(don.book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                            Text("${don.book.author ?: "Bilinmeyen yazar"} · Bağışçı: ${don.donorName ?: "Üye"}", style = MaterialTheme.typography.bodySmall, color = KoyuSoluk)
                                                            if (don.point != null) {
                                                                Text("Nokta: ${don.point.name}", style = MaterialTheme.typography.bodySmall, color = KoyuSoluk)
                                                            }
                                                        }
                                                        OutlinedButton(
                                                            onClick = { removingTarget = RemovingTarget("Bağış", don.id, don.book.title) },
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
                                                            req.status?.let {
                                                                Spacer(Modifier.size(4.dp))
                                                                StatusBadge(text = it)
                                                            }
                                                        }
                                                        OutlinedButton(
                                                            onClick = { removingTarget = RemovingTarget("İstek", req.id, req.book.title.ifBlank { "İstek" }) },
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
                                                            Text("${swap.book.author ?: "Bilinmeyen yazar"} · Sahibi: ${swap.ownerName ?: "Üye"}", style = MaterialTheme.typography.bodySmall, color = KoyuSoluk)
                                                        }
                                                        OutlinedButton(
                                                            onClick = { removingTarget = RemovingTarget("Takas", swap.id, swap.book.title) },
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
    }
}
