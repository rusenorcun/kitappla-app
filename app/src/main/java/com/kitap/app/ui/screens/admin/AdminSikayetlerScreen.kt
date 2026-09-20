package com.kitap.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.data.dto.AdminReportDto
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.StatusBadge
import com.kitap.app.ui.screens.common.SuccessBadge
import com.kitap.app.ui.theme.AdminTheme
import com.kitap.app.ui.theme.EspressoYuzey
import com.kitap.app.ui.theme.EspressoZemin
import com.kitap.app.ui.theme.KoyuSoluk
import com.kitap.app.ui.theme.VurguSoft

@Composable
fun AdminSikayetlerScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: AdminSikayetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    AdminSikayetlerContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onToggleShowAll = viewModel::toggleShowAll,
        onNavigateToDetail = onNavigateToDetail,
        onRetry = { viewModel.loadReports() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSikayetlerContent(
    state: AdminSikayetUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onToggleShowAll: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Şikâyetler", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    FilterChip(
                        selected = state.showAll,
                        onClick = onToggleShowAll,
                        label = { Text(if (state.showAll) "Tümü" else "Yalnızca Açıklar") },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EspressoZemin),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(EspressoZemin),
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
                state.reports.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.Flag,
                        title = "Bekleyen Şikâyet Yok",
                        message = "Şu anda incelenmeyi bekleyen şikâyet bulunmuyor.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.reports, key = { it.id }) { report ->
                            val isOpen = report.status.equals("OPEN", ignoreCase = true)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToDetail(report.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            StatusBadge(
                                                text = report.reasonLabel ?: report.reason,
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.primary,
                                            )
                                            StatusBadge(
                                                text = report.kindLabel ?: report.kind,
                                                containerColor = EspressoZemin,
                                                contentColor = KoyuSoluk,
                                            )
                                            if (isOpen) {
                                                StatusBadge(
                                                    text = "İnceleniyor",
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.primary,
                                                )
                                            } else if (report.status.equals("ACTIONED", ignoreCase = true)) {
                                                SuccessBadge(text = "İşlem yapıldı")
                                            } else {
                                                StatusBadge(
                                                    text = "Kapatıldı",
                                                    containerColor = EspressoZemin,
                                                    contentColor = KoyuSoluk,
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            text = "${report.reporterName ?: "Anonim"} → ${report.reportedUserName ?: "—"}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                        )

                                        if (!report.note.isNullOrBlank()) {
                                            Text(
                                                text = report.note,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KoyuSoluk,
                                                maxLines = 2,
                                            )
                                        }
                                    }

                                    Icon(
                                        Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null,
                                        tint = KoyuSoluk,
                                        modifier = Modifier.size(18.dp),
                                    )
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
private fun AdminSikayetlerPreview() {
    AdminTheme {
        Surface {
            AdminSikayetlerContent(
                state = AdminSikayetUiState(
                    reports = listOf(
                        AdminReportDto(
                            id = 1,
                            kind = "DONATION",
                            kindLabel = "Bağış",
                            reason = "INAPPROPRIATE",
                            reasonLabel = "Uygunsuz içerik",
                            note = "Kitap kapağında uygunsuz resim var.",
                            status = "OPEN",
                            reporterName = "Ali Veli",
                            reportedUserName = "Mehmet Can",
                        ),
                    ),
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBack = {},
                onToggleShowAll = {},
                onNavigateToDetail = {},
                onRetry = {},
            )
        }
    }
}
