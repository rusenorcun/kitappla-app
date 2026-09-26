package com.kitappla.app.ui.screens.admin

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
import com.kitappla.app.data.dto.AdminReportDto
import com.kitappla.app.ui.screens.common.ActionButtonsRow
import com.kitappla.app.ui.screens.common.ButtonLabel
import com.kitappla.app.ui.screens.common.CompactButtonPadding
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.StatusBadge
import com.kitappla.app.ui.screens.common.SuccessBadge
import com.kitappla.app.ui.theme.AdminTheme
import com.kitappla.app.ui.theme.EspressoYuzey
import com.kitappla.app.ui.theme.EspressoZemin
import com.kitappla.app.ui.theme.KoyuSoluk
import com.kitappla.app.ui.theme.KoyuVurgu
import com.kitappla.app.ui.theme.VurguSoft

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
        onSendMessage = viewModel::sendSupportMessage,
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
    onSendMessage: (id: Long, text: String, onSent: () -> Unit) -> Unit,
    onRetry: () -> Unit,
) {
    var adminNote by remember { mutableStateOf("") }
    var supportMessageText by remember { mutableStateOf("") }
    val report = state.selectedReport
    val detail = state.selectedDetail

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
                                    if (!report.createdAt.isNullOrBlank()) {
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            text = report.createdAt.take(10),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = KoyuSoluk,
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
                                        if (!report.reporterEmail.isNullOrBlank()) {
                                            Text(
                                                text = report.reporterEmail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KoyuSoluk,
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Şikâyet Edilen", style = MaterialTheme.typography.labelSmall, color = KoyuSoluk)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = report.reportedUserName ?: "—",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            if (report.reportedUserId != null) {
                                                Spacer(Modifier.size(4.dp))
                                                if (report.reportedUserBlocked) {
                                                    StatusBadge(
                                                        text = "Askıda",
                                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                                        contentColor = MaterialTheme.colorScheme.error,
                                                    )
                                                } else {
                                                    SuccessBadge(text = "Aktif")
                                                }
                                            }
                                        }
                                        if (!report.reportedUserEmail.isNullOrBlank()) {
                                            Text(
                                                text = report.reportedUserEmail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KoyuSoluk,
                                            )
                                        }
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

                        // Entity Details Card
                        if (detail?.entityTitle != null) {
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
                                        text = "İlgili İçerik / Teslimat Bilgisi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = KoyuSoluk,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = detail.entityTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    if (!detail.entitySubtitle.isNullOrBlank()) {
                                        Text(
                                            text = detail.entitySubtitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = KoyuSoluk,
                                        )
                                    }
                                    if (!detail.entityStatus.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Durum: ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KoyuSoluk,
                                            )
                                            StatusBadge(
                                                text = detail.entityStatus,
                                                containerColor = EspressoZemin,
                                                contentColor = KoyuVurgu,
                                            )
                                        }
                                    }
                                    if (!detail.entityDetails.isNullOrBlank()) {
                                        Text(
                                            text = detail.entityDetails,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    if (!detail.entityExtra.isNullOrBlank()) {
                                        Text(
                                            text = detail.entityExtra,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }

                        // Moderation Chat Card (if CONVERSATION report)
                        val modMessages = detail?.moderationMessages
                        if (!modMessages.isNullOrEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(
                                        text = "Şikâyet Edilen Sohbet Mesajları",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    modMessages.forEach { msg ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(EspressoZemin, RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    text = msg.senderName ?: "Kullanıcı",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                                if (!msg.createdAt.isNullOrBlank()) {
                                                    Text(
                                                        text = msg.createdAt.take(16).replace("T", " "),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = KoyuSoluk,
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = msg.body,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Bu mesajlar yalnızca açık şikâyet olduğu için görüntülenmektedir. Şikâyet kapatıldığında erişim kalkar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KoyuSoluk,
                                    )
                                }
                            }
                        }

                        // Support Messaging Card
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
                                    text = "Şikâyet Eden ile Destek / İrtibat",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )

                                val supportMsgs = detail?.supportMessages
                                if (supportMsgs.isNullOrEmpty()) {
                                    Text(
                                        text = "Henüz bir destek mesajı yok. Üyeyle irtibata geçmek veya ek bilgi istemek için aşağıdan mesaj yazabilirsiniz.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KoyuSoluk,
                                    )
                                } else {
                                    supportMsgs.forEach { msg ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (msg.mine) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                    else EspressoZemin,
                                                    RoundedCornerShape(10.dp),
                                                )
                                                .padding(10.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    text = msg.senderName ?: if (msg.mine) "Siz (Yönetim)" else "Kullanıcı",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                                if (!msg.createdAt.isNullOrBlank()) {
                                                    Text(
                                                        text = msg.createdAt.take(16).replace("T", " "),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = KoyuSoluk,
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = msg.body,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OutlinedTextField(
                                        value = supportMessageText,
                                        onValueChange = { supportMessageText = it },
                                        placeholder = { Text("Destek mesajı yaz...") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        maxLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = EspressoZemin,
                                            unfocusedContainerColor = EspressoZemin,
                                        ),
                                    )
                                    Button(
                                        onClick = {
                                            if (supportMessageText.isNotBlank()) {
                                                val sent = supportMessageText
                                                onSendMessage(report.id, sent) {
                                                    if (supportMessageText == sent) supportMessageText = ""
                                                }
                                            }
                                        },
                                        enabled = supportMessageText.isNotBlank() && !state.isActionLoading,
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Text("Gönder")
                                    }
                                }
                            }
                        }

                        // Resolution info / Actions
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
                                    Text(
                                        text = "İçeriği kaldırmak ya da üyeyi askıya almak için İçerik ve Üyeler sekmelerini kullanabilirsiniz. Ardından şikâyeti buradan kapatabilirsiniz.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KoyuSoluk,
                                    )

                                    OutlinedTextField(
                                        value = adminNote,
                                        onValueChange = { adminNote = it },
                                        label = { Text("Yönetici Notu (isteğe bağlı)") },
                                        placeholder = { Text("Yapılan işlem veya açıklama...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = EspressoZemin,
                                            unfocusedContainerColor = EspressoZemin,
                                        ),
                                    )

                                    ActionButtonsRow {
                                        OutlinedButton(
                                            onClick = {
                                                onResolve(report.id, false, adminNote.ifBlank { null })
                                            },
                                            contentPadding = CompactButtonPadding,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = !state.isActionLoading,
                                        ) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.size(4.dp))
                                            ButtonLabel("İşlem Gerekmedi")
                                        }

                                        Button(
                                            onClick = {
                                                onResolve(report.id, true, adminNote.ifBlank { null })
                                            },
                                            contentPadding = CompactButtonPadding,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = !state.isActionLoading,
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.size(4.dp))
                                            ButtonLabel("İşlem Yapıldı")
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
                onSendMessage = { _, _, _ -> },
                onRetry = {},
            )
        }
    }
}
