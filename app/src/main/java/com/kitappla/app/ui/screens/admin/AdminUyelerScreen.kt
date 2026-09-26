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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
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
import com.kitappla.app.ui.screens.common.SuccessBadge
import com.kitappla.app.ui.screens.common.UserAvatar
import com.kitappla.app.ui.theme.Adacayi
import com.kitappla.app.ui.theme.AdacayiSoft
import com.kitappla.app.ui.theme.EspressoYuzey
import com.kitappla.app.ui.theme.EspressoZemin
import com.kitappla.app.ui.theme.KoyuSoluk
import com.kitappla.app.ui.theme.Vurgu
import com.kitappla.app.ui.theme.VurguSoft

@Composable
fun AdminUyelerScreen(
    onBack: () -> Unit,
    onOpenConversation: (Long) -> Unit,
    viewModel: AdminUyelerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    AdminUyelerContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onToggleBlock = viewModel::toggleBlockUser,
        onToggleAdminRole = viewModel::toggleAdminRole,
        onDeleteUser = viewModel::deleteUser,
        onSendMessage = viewModel::sendMessageToUser,
        onOpenConversation = { userId ->
            viewModel.openConversationWithUser(userId) { convId ->
                onOpenConversation(convId)
            }
        },
        onRetry = { viewModel.searchUsers(state.searchQuery) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUyelerContent(
    state: AdminUyelerUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleBlock: (Long) -> Unit,
    onToggleAdminRole: (Long) -> Unit,
    onDeleteUser: (Long) -> Unit,
    onSendMessage: (Long, String) -> Unit,
    onOpenConversation: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var messagingUser by remember { mutableStateOf<UserDto?>(null) }
    var messageText by remember { mutableStateOf("") }
    var blockingUser by remember { mutableStateOf<UserDto?>(null) }
    var roleUser by remember { mutableStateOf<UserDto?>(null) }
    var deletingUser by remember { mutableStateOf<UserDto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Üyeler", fontWeight = FontWeight.Bold) },
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
            // Arama Kutusu
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Ad ya da e-posta ara...", color = KoyuSoluk) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = KoyuSoluk) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = EspressoYuzey,
                    unfocusedContainerColor = EspressoYuzey,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                // Yukarıdan çekince listeyi ağdan yeniler.
                PullRefresh(
                    loading = state.isLoading,
                    onRefresh = onRetry,
                    modifier = Modifier.fillMaxSize(),
                    scrollableContent = state.users.isNotEmpty(),
                ) {
                    when {
                        state.isLoading && state.users.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        state.error != null && state.users.isEmpty() -> {
                            ErrorStateView(
                                message = state.error,
                                onRetry = onRetry,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                        state.users.isEmpty() -> {
                            EmptyStateView(
                                icon = Icons.Outlined.People,
                                title = "Sonuç Yok",
                                message = "Aramanızla eşleşen üye bulunamadı.",
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(state.users, key = { it.id }) { user ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (user.blocked) EspressoYuzey.copy(alpha = 0.65f) else EspressoYuzey,
                                        ),
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.Top,
                                            ) {
                                                UserAvatar(initials = user.initials, size = 44)

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    ) {
                                                        Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                        if (user.admin) {
                                                            StatusBadge(text = "Yönetici", containerColor = VurguSoft, contentColor = Vurgu)
                                                        }
                                                        if (user.blocked) {
                                                            StatusBadge(text = "Askıda", containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                        if (user.student) {
                                                            SuccessBadge(text = "Öğrenci")
                                                        } else if (user.studentStatus.equals("PENDING", ignoreCase = true)) {
                                                            StatusBadge(text = "Belge İncelemede", containerColor = VurguSoft, contentColor = Vurgu)
                                                        } else if (user.studentStatus.equals("REJECTED", ignoreCase = true)) {
                                                            StatusBadge(text = "Belge Reddedildi", containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    }

                                                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = KoyuSoluk)

                                                    if (user.noShowCount > 0) {
                                                        Spacer(Modifier.size(4.dp))
                                                        StatusBadge(
                                                            text = "${user.noShowCount} kez gelmedi",
                                                            containerColor = VurguSoft,
                                                            contentColor = Vurgu,
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(Modifier.size(12.dp))

                                            // Aksiyon Butonları
                                            ActionButtonsRow(spacing = 6.dp) {
                                                if (!user.admin) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            messagingUser = user
                                                            messageText = ""
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                        modifier = Modifier.weight(1f),
                                                    ) {
                                                        Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(Modifier.size(4.dp))
                                                        ButtonLabel("Mesaj", fontSize = 11.sp)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { blockingUser = user },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = if (user.blocked) Adacayi else Vurgu,
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                    modifier = Modifier.weight(1f),
                                                ) {
                                                    Icon(Icons.Outlined.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(Modifier.size(4.dp))
                                                    ButtonLabel(if (user.blocked) "Aktif Et" else "Askıya Al", fontSize = 11.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = { roleUser = user },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                    modifier = Modifier.weight(1f),
                                                ) {
                                                    Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(Modifier.size(4.dp))
                                                    ButtonLabel(if (user.admin) "Yetkiyi Al" else "Yönetici Yap", fontSize = 11.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = { deletingUser = user },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Vurgu),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                    modifier = Modifier.weight(0.7f),
                                                ) {
                                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(Modifier.size(4.dp))
                                                    ButtonLabel("Sil", fontSize = 11.sp)
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

    // Mesaj Gönderme Diyaloğu
    messagingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { messagingUser = null },
            title = { Text("Üyeye Mesaj Gönder") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${user.name} (${user.email})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Mesajınızı yazın...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(10.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSendMessage(user.id, messageText)
                        messagingUser = null
                    },
                    enabled = messageText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Gönder")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            val targetUserId = user.id
                            messagingUser = null
                            onOpenConversation(targetUserId)
                        },
                    ) {
                        Text("Sohbete Git")
                    }
                    TextButton(onClick = { messagingUser = null }) {
                        Text("İptal")
                    }
                }
            },
        )
    }

    // Askıya Alma / Aktif Etme Diyaloğu
    blockingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { blockingUser = null },
            title = { Text(if (user.blocked) "Üyeyi Aktifleştir" else "Üyeyi Askıya Al") },
            text = {
                Text(
                    if (user.blocked)
                        "${user.name} üyesinin askısı kaldırılacak ve tekrar giriş yapabilecektir."
                    else
                        "${user.name} üyesi askıya alınınca süren talep, istek ve takasları iptal edilir; karşı taraflara hakları iade edilir. Devam edilsin mi?",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleBlock(user.id)
                        blockingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.blocked) Adacayi else Vurgu,
                    ),
                ) {
                    Text(if (user.blocked) "Aktifleştir" else "Askıya Al")
                }
            },
            dismissButton = {
                TextButton(onClick = { blockingUser = null }) {
                    Text("İptal")
                }
            },
        )
    }

    // Yönetici Yetkisi Diyaloğu
    roleUser?.let { user ->
        AlertDialog(
            onDismissRequest = { roleUser = null },
            title = { Text(if (user.admin) "Yönetici Yetkisini Al" else "Yönetici Yap") },
            text = {
                Text(
                    if (user.admin)
                        "${user.name} üyesinin yönetici yetkisi kaldırılacak, normal üye olarak devam edecektir."
                    else
                        "${user.name} üyesine yönetici yetkisi verilecek, yönetim paneline erişebilecektir.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleAdminRole(user.id)
                        roleUser = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.admin) Vurgu else Adacayi,
                    ),
                ) {
                    Text(if (user.admin) "Yetkiyi Al" else "Yönetici Yap")
                }
            },
            dismissButton = {
                TextButton(onClick = { roleUser = null }) {
                    Text("İptal")
                }
            },
        )
    }

    // Üye Silme Diyaloğu
    deletingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { deletingUser = null },
            title = { Text("Üyeyi Kalıcı Olarak Sil") },
            text = {
                Text(
                    "${user.name} adlı üye kalıcı olarak silinsin mi? Bağış, talep ya da takas kaydı olan üyeler geçmişin bozulmaması için silinemez, yalnızca askıya alınabilir.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(user.id)
                        deletingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Vurgu),
                ) {
                    Text("Kalıcı Olarak Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingUser = null }) {
                    Text("İptal")
                }
            },
        )
    }
}
