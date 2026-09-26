package com.kitappla.app.ui.screens.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.data.dto.ChatMessageDto
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.ReportDialog
import com.kitappla.app.ui.theme.KahveSoluk
import com.kitappla.app.ui.theme.KitapplaTheme

@Composable
fun SohbetScreen(
    onBack: () -> Unit,
    viewModel: SohbetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    SohbetContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onSendMessage = viewModel::sendMessage,
        onReport = viewModel::report,
        onRetry = viewModel::loadMessages,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SohbetContent(
    state: SohbetState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onSendMessage: (text: String, onSent: () -> Unit) -> Unit,
    onReport: (reason: String, note: String?) -> Unit,
    onRetry: () -> Unit,
) {
    var messageText by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

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
                title = { Text("Sohbet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Outlined.Flag, contentDescription = "Şikâyet Et")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Mesaj yazın...") },
                        maxLines = 4,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                    )

                    FilledIconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                val sent = messageText
                                // Gönderilen metin, sunucu kabul edince silinir (arada yazılan yeni metne dokunulmaz).
                                onSendMessage(sent) { if (messageText == sent) messageText = "" }
                            }
                        },
                        enabled = messageText.isNotBlank() && !state.sending,
                        modifier = Modifier.size(46.dp),
                    ) {
                        if (state.sending) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Gönder")
                        }
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.loading && state.messages.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null && state.messages.isEmpty() -> ErrorStateView(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier.padding(padding),
            )
            state.messages.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = "Henüz mesaj yok. İlk mesajı siz gönderin!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KahveSoluk,
                )
            }
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    val isMine = msg.mine
                    val bubbleShape = if (isMine) {
                        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 4.dp, bottomStart = 14.dp)
                    } else {
                        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 4.dp)
                    }
                    val bubbleBg = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val textColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                    val lineBorder = MaterialTheme.colorScheme.outlineVariant

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.78f)
                                .then(
                                    if (isMine) Modifier.wrapContentWidth(Alignment.End)
                                    else Modifier.wrapContentWidth(Alignment.Start)
                                )
                                .clip(bubbleShape)
                                .then(
                                    if (!isMine) Modifier.border(1.dp, lineBorder, bubbleShape)
                                    else Modifier
                                )
                                .background(bubbleBg)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Column {
                                if (!isMine && !msg.senderName.isNullOrBlank()) {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                }
                                Text(
                                    text = msg.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor,
                                    lineHeight = 20.sp,
                                )
                                if (!msg.createdAt.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = msg.createdAt.takeLast(8).take(5), // hh:mm
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = textColor.copy(alpha = 0.70f),
                                        modifier = Modifier.align(Alignment.End),
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
private fun SohbetScreenPreview() {
    KitapplaTheme {
        Surface {
            SohbetContent(
                state = SohbetState(
                    loading = false,
                    messages = listOf(
                        ChatMessageDto(id = 1L, body = "Merhaba, kitabı ne zaman teslim alabilirim?", mine = false, senderName = "Ali"),
                        ChatMessageDto(id = 2L, body = "Merhaba! Yarın öğleden sonra kampüs kütüphanesinde buluşabiliriz.", mine = true),
                        ChatMessageDto(id = 3L, body = "Harika, saat 14:00 uygun mudur?", mine = false, senderName = "Ali"),
                    ),
                ),
                onBack = {},
                onSendMessage = { _, _ -> },
                onReport = { _, _ -> },
                onRetry = {},
            )
        }
    }
}
