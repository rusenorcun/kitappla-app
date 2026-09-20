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
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.material3.Surface
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
import com.kitap.app.data.dto.StudentStatus
import com.kitap.app.data.dto.UserDto
import com.kitap.app.data.dto.isApprovedStudent
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.StatusBadge
import com.kitap.app.ui.screens.common.SuccessBadge
import com.kitap.app.ui.screens.common.UserAvatar
import com.kitap.app.ui.theme.AdminTheme
import com.kitap.app.ui.theme.EspressoYuzey
import com.kitap.app.ui.theme.EspressoZemin
import com.kitap.app.ui.theme.KahveSoluk
import com.kitap.app.ui.theme.KoyuSoluk
import com.kitap.app.ui.theme.KremCizgi
import com.kitap.app.ui.theme.Vurgu
import com.kitap.app.ui.theme.VurguSoft

@Composable
fun AdminUyelerScreen(
    onBack: () -> Unit,
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
        onQueryChange = viewModel::onSearchQueryChanged,
        onToggleBlock = viewModel::toggleBlockUser,
        onRetry = { viewModel.searchUsers(state.searchQuery) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUyelerContent(
    state: AdminUyelerUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleBlock: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var confirmingUser by remember { mutableStateOf<UserDto?>(null) }

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
            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("İsim veya e-posta ile ara...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = KoyuSoluk) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = EspressoYuzey,
                    unfocusedContainerColor = EspressoYuzey,
                ),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
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
                            title = "Üye Bulunamadı",
                            message = "Arama kriterine uygun üye kaydı yok.",
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.users, key = { it.id }) { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                UserAvatar(initials = user.initials.ifBlank { user.name.take(2) }, size = 42)
                                                Column {
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
                                                }
                                            }

                                            // Badges
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (user.admin) {
                                                    StatusBadge(text = "Yönetici")
                                                }
                                                if (user.isApprovedStudent) {
                                                    SuccessBadge(text = "Öğrenci")
                                                } else if (user.studentStatus == StudentStatus.PENDING) {
                                                    StatusBadge(text = "Belge Bekliyor", containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }

                                        // Block / Unblock action
                                        if (!user.admin) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                            ) {
                                                OutlinedButton(
                                                    onClick = { confirmingUser = user },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Vurgu),
                                                    enabled = !state.isActionLoading,
                                                ) {
                                                    Icon(Icons.Outlined.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.size(6.dp))
                                                    Text("Askıya Al / Aktifleştir")
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

    // Confirmation dialog
    confirmingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { confirmingUser = null },
            title = { Text("Hesap Durumu") },
            text = {
                Text("${user.name} adlı kullanıcının hesap durumunu (aktif/askıda) değiştirmek istiyor musunuz?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleBlock(user.id)
                        confirmingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Vurgu),
                ) {
                    Text("Onayla")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingUser = null }) {
                    Text("İptal")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminUyelerPreview() {
    AdminTheme {
        Surface {
            AdminUyelerContent(
                state = AdminUyelerUiState(
                    users = listOf(
                        UserDto(id = 1, name = "Ruşen Göbel", email = "rusen@kitappla.com", admin = true),
                        UserDto(id = 2, name = "Ayşe Kaya", email = "ayse@deu.edu.tr", studentStatus = StudentStatus.APPROVED),
                        UserDto(id = 3, name = "Mehmet Demir", email = "mehmet@gmail.com", studentStatus = StudentStatus.PENDING),
                    ),
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBack = {},
                onQueryChange = {},
                onToggleBlock = {},
                onRetry = {},
            )
        }
    }
}
