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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.data.dto.UserDto
import com.kitappla.app.ui.screens.common.ActionButtonsRow
import com.kitappla.app.ui.screens.common.ButtonLabel
import com.kitappla.app.ui.screens.common.CompactButtonPadding
import com.kitappla.app.ui.screens.common.EmptyStateView
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.PullRefresh
import com.kitappla.app.ui.screens.common.StatusBadge
import com.kitappla.app.ui.screens.common.UserAvatar
import com.kitappla.app.ui.theme.Adacayi
import com.kitappla.app.ui.theme.AdacayiSoft
import com.kitappla.app.ui.theme.EspressoYuzey
import com.kitappla.app.ui.theme.EspressoZemin
import com.kitappla.app.ui.theme.KoyuSoluk
import com.kitappla.app.ui.theme.Vurgu
import com.kitappla.app.ui.theme.VurguSoft

@Composable
fun AdminBelgelerScreen(
    onBack: () -> Unit,
    onOpenDocument: (userId: Long, userName: String) -> Unit = { _, _ -> },
    viewModel: AdminBelgelerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    AdminBelgelerContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onApprove = viewModel::approve,
        onReject = viewModel::reject,
        onRetry = viewModel::loadData,
        onOpenDocument = onOpenDocument,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBelgelerContent(
    state: AdminBelgelerUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onApprove: (Long) -> Unit,
    onReject: (Long, String?) -> Unit,
    onRetry: () -> Unit,
    onOpenDocument: (userId: Long, userName: String) -> Unit = { _, _ -> },
) {
    var rejectingUser by remember { mutableStateOf<UserDto?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    if (rejectingUser != null) {
        AlertDialog(
            onDismissRequest = {
                rejectingUser = null
                rejectReason = ""
            },
            title = { Text("Belgeyi Reddet", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "${rejectingUser?.name} kullanıcısının öğrenci belgesi reddedilecek ve dosya silinecektir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KoyuSoluk,
                    )
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Ret Gerekçesi (İsteğe bağlı)") },
                        placeholder = { Text("Örn. Belge okunamıyor, güncel değil...") },
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
                        rejectingUser?.let { onReject(it.id, rejectReason.ifBlank { null }) }
                        rejectingUser = null
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Vurgu),
                ) {
                    Text("Reddet")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        rejectingUser = null
                        rejectReason = ""
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
                title = { Text("Öğrenci Belgeleri", fontWeight = FontWeight.Bold) },
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
            // Yukarıdan çekince listeyi ağdan yeniler.
            PullRefresh(
                loading = state.isLoading,
                onRefresh = onRetry,
                modifier = Modifier.fillMaxSize(),
                scrollableContent = state.pendingUsers.isNotEmpty(),
            ) {
                when {
                    state.isLoading && state.pendingUsers.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    state.error != null && state.pendingUsers.isEmpty() -> {
                        ErrorStateView(
                            message = state.error,
                            onRetry = onRetry,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    state.pendingUsers.isEmpty() -> {
                        EmptyStateView(
                            icon = Icons.Outlined.Description,
                            title = "İncelemede Belge Yok",
                            message = "Yeni başvurular geldiğinde burada listelenecek.",
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item {
                                Text(
                                    text = "Belgeler yalnızca yönetim tarafından incelenir; hiçbir zaman herkese açık servis edilmez.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KoyuSoluk,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }

                            items(state.pendingUsers, key = { it.id }) { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            UserAvatar(initials = user.initials, size = 44)

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = user.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                                Text(
                                                    text = user.email,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = KoyuSoluk,
                                                )

                                                Spacer(Modifier.height(8.dp))

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                ) {
                                                    user.schoolLevel?.let { level ->
                                                        StatusBadge(
                                                            text = level,
                                                            containerColor = AdacayiSoft,
                                                            contentColor = Adacayi,
                                                        )
                                                    }
                                                    if (!user.documentNo.isNullOrBlank()) {
                                                        StatusBadge(
                                                            text = "Belge no: ${user.documentNo}",
                                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    }
                                                }
                                            }

                                            // Belgeyi Aç: yöneticinin oturumuyla uygulama içinde (tarayıcıda oturum yok)
                                            OutlinedButton(
                                                onClick = { onOpenDocument(user.id, user.name) },
                                                shape = RoundedCornerShape(8.dp),
                                            ) {
                                                Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Belgeyi Aç", fontSize = 12.sp)
                                            }
                                        }

                                        Spacer(Modifier.height(14.dp))

                                        // Onayla / Reddet Aksiyonları
                                        ActionButtonsRow {
                                            Button(
                                                onClick = { onApprove(user.id) },
                                                enabled = !state.isActionLoading,
                                                colors = ButtonDefaults.buttonColors(containerColor = Adacayi),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = CompactButtonPadding,
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                ButtonLabel("Onayla", fontSize = 13.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { rejectingUser = user },
                                                enabled = !state.isActionLoading,
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Vurgu),
                                                contentPadding = CompactButtonPadding,
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                ButtonLabel("Reddet", fontSize = 13.sp)
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
