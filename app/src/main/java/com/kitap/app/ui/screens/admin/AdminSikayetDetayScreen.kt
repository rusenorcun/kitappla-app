package com.kitap.app.ui.screens.admin

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
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
import com.kitap.app.data.dto.AdminReportDto
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.StatusBadge
import com.kitap.app.ui.screens.common.SuccessBadge
import com.kitap.app.ui.theme.AdminTheme
import com.kitap.app.ui.theme.EspressoYuzey
import com.kitap.app.ui.theme.EspressoZemin
import com.kitap.app.ui.theme.KoyuSoluk
import com.kitap.app.ui.theme.KoyuVurgu
import com.kitap.app.ui.theme.VurguSoft

@Composable
fun AdminSikayetDetayScreen(
    reportId: Long,
    onBack: () -> Unit,
    viewModel: AdminSikayetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(reportId) {
        viewModel.selectReport(reportId)
    }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    AdminSikayetDetayContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onResolve = viewModel::resolveReport,
        onRetry = { viewModel.selectReport(reportId) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSikayetDetayContent(
    state: AdminSikayetUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onResolve: (Long, Boolean, String?) -> Unit,
    onRetry: () -> Unit,
) {
    var adminNote by remember { mutableStateOf("") }
    val report = state.selectedReport

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Şikâyet Detayı", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(EspressoZemin),
        ) {
            when {
                state.isLoading && report == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                report == null -> {
                    ErrorStateView(
                        message = state.error ?: "Şikâyet bulunamadı.",
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    val isOpen = report.status.equals("OPEN", ignoreCase = true)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Overview Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                            text = "İşlem gerekmedi",
                                            containerColor = EspressoZemin,
                                            contentColor = KoyuSoluk,
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Şikâyet Eden", style = MaterialTheme.typography.labelSmall, color = KoyuSoluk)
                                        Text(
                                            text = report.reporterName ?: "Anonim",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Şikâyet Edilen", style = MaterialTheme.typography.labelSmall, color = KoyuSoluk)
                                        Text(
                                            text = report.reportedUserName ?: "—",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }

                                if (!report.note.isNullOrBlank()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(EspressoZemin, RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                    ) {
                                        Text(text = "AÇIKLAMA", style = MaterialTheme.typography.labelSmall, color = KoyuSoluk, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Text(text = report.note, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }

                        // Existing resolution info
                        if (!isOpen) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = "Sonuçlandırma Bilgisi",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = if (report.status.equals("ACTIONED", ignoreCase = true))
                                            "Bu şikâyet için gerekli yaptırım veya işlem uygulanmıştır."
                                        else
                                            "Bu şikâyet incelenmiş ve herhangi bir işlem gerekmediğine karar verilmiştir.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = KoyuSoluk,
                                    )
                                    if (!report.adminNote.isNullOrBlank()) {
                                        Text(
                                            text = "Yönetici Notu: ${report.adminNote}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        } else {
                            // Resolve Actions Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = "Şikâyeti Sonuçlandır",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )

                                    OutlinedTextField(
                                        value = adminNote,
                                        onValueChange = { adminNote = it },
                                        label = { Text("Yönetici Notu (isteğe bağlı)") },
                                        placeholder = { Text("Yapılan işlem veya açıklama...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = EspressoZemin,
                                            unfocusedContainerColor = EspressoZemin,
                                        ),
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onResolve(report.id, false, adminNote.ifBlank { null })
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = !state.isActionLoading,
                                        ) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.size(4.dp))
                                            Text("İşlem Gerekmedi")
                                        }

                                        Button(
                                            onClick = {
                                                onResolve(report.id, true, adminNote.ifBlank { null })
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = !state.isActionLoading,
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.size(4.dp))
                                            Text("İşlem Yapıldı")
                                        }
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
}

@Preview(showBackground = true)
@Composable
private fun AdminSikayetDetayPreview() {
    AdminTheme {
        Surface {
            AdminSikayetDetayContent(
                state = AdminSikayetUiState(
                    selectedReport = AdminReportDto(
                        id = 1,
                        kind = "DONATION",
                        kindLabel = "Bağış",
                        reason = "INAPPROPRIATE",
                        reasonLabel = "Uygunsuz içerik",
                        note = "Kapak fotoğrafı genel ahlaka uygun olmayan resim içeriyor.",
                        status = "OPEN",
                        reporterName = "Ali Veli",
                        reportedUserName = "Mehmet Can",
                    ),
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBack = {},
                onResolve = { _, _, _ -> },
                onRetry = {},
            )
        }
    }
}
