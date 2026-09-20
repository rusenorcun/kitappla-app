package com.kitap.app.ui.screens.admin

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.BuildConfig
import com.kitap.app.data.dto.UserDto
import com.kitap.app.ui.screens.common.EmptyStateView
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.UserAvatar
import com.kitap.app.ui.theme.AdminTheme
import com.kitap.app.ui.theme.EspressoYuzey
import com.kitap.app.ui.theme.EspressoZemin
import com.kitap.app.ui.theme.KoyuSoluk
import com.kitap.app.ui.theme.Vurgu

@Composable
fun AdminBelgelerScreen(
    onBack: () -> Unit,
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
        onApprove = viewModel::approveDoc,
        onReject = viewModel::rejectDoc,
        onRetry = viewModel::loadData,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBelgelerContent(
    state: AdminBelgelerUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onApprove: (Long) -> Unit,
    onReject: (Long, String?) -> Unit,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    var rejectingUserId by remember { mutableStateOf<Long?>(null) }
    var rejectReason by remember { mutableStateOf("") }

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
                state.pendingDocs.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.School,
                        title = "Bekleyen Belge Yok",
                        message = "Tüm öğrenci doğrulama talepleri incelenmiş.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item {
                            Text(
                                text = "İncelenmeyi Bekleyen Başvurular (${state.pendingDocs.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = KoyuSoluk,
                            )
                        }

                        items(state.pendingDocs, key = { it.id }) { user ->
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
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        UserAvatar(initials = user.initials.ifBlank { user.name.take(2) }, size = 44)
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
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(EspressoZemin, RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        if (!user.schoolLevel.isNullOrBlank()) {
                                            Text(
                                                text = "Eğitim Düzeyi: ${user.schoolLevel}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                        if (!user.school.isNullOrBlank()) {
                                            Text(
                                                text = "Okul: ${user.school}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KoyuSoluk,
                                            )
                                        }
                                    }

                                    // Document link button
                                    OutlinedButton(
                                        onClick = {
                                            val docUrl = "${BuildConfig.API_BASE_URL}/admin/belgeler/${user.id}/dosya"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(docUrl))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.size(6.dp))
                                        Text("Öğrenci Belgesini Görüntüle")
                                        Spacer(Modifier.weight(1f))
                                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }

                                    // Action buttons: Approve / Reject
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                rejectingUserId = user.id
                                                rejectReason = ""
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Vurgu),
                                            enabled = !state.isActionLoading,
                                        ) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.size(4.dp))
                                            Text("Reddet")
                                        }

                                        Button(
                                            onClick = { onApprove(user.id) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = !state.isActionLoading,
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.size(4.dp))
                                            Text("Onayla")
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

    if (rejectingUserId != null) {
        AlertDialog(
            onDismissRequest = { rejectingUserId = null },
            title = { Text("Öğrenci Belgesini Reddet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bu başvuruyu reddetmek istediğinize emin misiniz? Gerekçe öğrenciye iletilecektir.")
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Red Gerekçesi (isteğe bağlı)") },
                        placeholder = { Text("Örn: Belge okunamıyor veya güncel değil") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = rejectingUserId ?: return@Button
                        onReject(uid, rejectReason.ifBlank { null })
                        rejectingUserId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Vurgu),
                ) {
                    Text("Reddet")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingUserId = null }) {
                    Text("İptal")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminBelgelerPreview() {
    AdminTheme {
        Surface {
            AdminBelgelerContent(
                state = AdminBelgelerUiState(
                    pendingDocs = listOf(
                        UserDto(
                            id = 1,
                            name = "Ahmet Yılmaz",
                            email = "ahmet@ogr.deu.edu.tr",
                            initials = "AY",
                            schoolLevel = "UNIVERSITE",
                            school = "Dokuz Eylül Üniversitesi",
                        ),
                        UserDto(
                            id = 2,
                            name = "Zeynep Kaya",
                            email = "zeynep@ogr.ege.edu.tr",
                            initials = "ZK",
                            schoolLevel = "LISE",
                        ),
                    ),
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBack = {},
                onApprove = {},
                onReject = { _, _ -> },
                onRetry = {},
            )
        }
    }
}
