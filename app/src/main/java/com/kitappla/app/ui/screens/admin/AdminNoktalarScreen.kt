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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.ui.screens.common.EmptyStateView
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.PullRefresh
import com.kitappla.app.ui.screens.common.StatusBadge
import com.kitappla.app.ui.screens.common.SuccessBadge
import com.kitappla.app.ui.theme.Adacayi
import com.kitappla.app.ui.theme.EspressoYuzey
import com.kitappla.app.ui.theme.EspressoZemin
import com.kitappla.app.ui.theme.KoyuSoluk
import com.kitappla.app.ui.theme.KoyuVurgu
import com.kitappla.app.ui.theme.Vurgu

@Composable
fun AdminNoktalarScreen(
    onBack: () -> Unit,
    viewModel: AdminNoktalarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    AdminNoktalarContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onCreatePoint = viewModel::createPoint,
        onUpdatePoint = viewModel::updatePoint,
        onToggleActive = viewModel::toggleActive,
        onRetry = viewModel::loadData,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNoktalarContent(
    state: AdminNoktalarUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onCreatePoint: (String, String, String?) -> Unit,
    onUpdatePoint: (Long, String, String, String?) -> Unit,
    onToggleActive: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingPoint by remember { mutableStateOf<PickupPointDto?>(null) }

    var campusInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = EspressoYuzey,
        unfocusedContainerColor = EspressoYuzey,
    )

    // Yeni Nokta Ekleme Diyaloğu
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                campusInput = ""
                nameInput = ""
                descriptionInput = ""
            },
            title = { Text("Yeni Nokta Ekle", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = campusInput,
                        onValueChange = { campusInput = it },
                        label = { Text("Kampüs *") },
                        placeholder = { Text("Örn. Tınaztepe Kampüsü") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nokta Adı *") },
                        placeholder = { Text("Örn. Merkez Kütüphane Girişi") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Tarif (İsteğe bağlı)") },
                        placeholder = { Text("Örn. Turnikelerin solundaki banklar") },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreatePoint(campusInput, nameInput, descriptionInput.ifBlank { null })
                        showCreateDialog = false
                        campusInput = ""
                        nameInput = ""
                        descriptionInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        campusInput = ""
                        nameInput = ""
                        descriptionInput = ""
                    },
                ) {
                    Text("İptal")
                }
            },
        )
    }

    // Nokta Güncelleme Diyaloğu
    if (editingPoint != null) {
        AlertDialog(
            onDismissRequest = { editingPoint = null },
            title = { Text("Noktayı Düzenle", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = campusInput,
                        onValueChange = { campusInput = it },
                        label = { Text("Kampüs *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nokta Adı *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Tarif (İsteğe bağlı)") },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingPoint?.let {
                            onUpdatePoint(it.id, campusInput, nameInput, descriptionInput.ifBlank { null })
                        }
                        editingPoint = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPoint = null }) {
                    Text("İptal")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teslim Noktaları", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EspressoZemin),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    campusInput = ""
                    nameInput = ""
                    descriptionInput = ""
                    showCreateDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Yeni Nokta Ekle")
            }
        },
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
                scrollableContent = state.points.isNotEmpty(),
            ) {
                when {
                    state.isLoading && state.points.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    state.error != null && state.points.isEmpty() -> {
                        ErrorStateView(
                            message = state.error,
                            onRetry = onRetry,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    state.points.isEmpty() -> {
                        EmptyStateView(
                            icon = Icons.Outlined.Place,
                            title = "Teslim Noktası Yok",
                            message = "En az bir nokta ekleyin ki üyeler buluşma yeri seçebilsin.",
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
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(Icons.Outlined.Info, contentDescription = null, tint = KoyuVurgu)
                                        Text(
                                            text = "Kitaplar kampüs içinde yüz yüze teslim ediliyor. Üyeler buluşma yeri olarak buradaki noktalardan birini seçer.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = KoyuSoluk,
                                            lineHeight = 18.sp,
                                        )
                                    }
                                }
                            }

                            items(state.points, key = { it.id }) { point ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (point.active) EspressoYuzey else EspressoYuzey.copy(alpha = 0.6f),
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Icon(Icons.Outlined.Place, contentDescription = null, tint = KoyuVurgu)
                                                Column {
                                                    if (point.campus.isNotBlank()) {
                                                        Text(
                                                            text = point.campus,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = KoyuSoluk,
                                                        )
                                                    }
                                                    Text(
                                                        text = point.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                            }
                                            if (point.active) {
                                                SuccessBadge(text = "Aktif")
                                            } else {
                                                StatusBadge(
                                                    text = "Pasif",
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }

                                        if (!point.description.isNullOrBlank()) {
                                            Text(
                                                text = "Tarif: ${point.description}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KoyuSoluk,
                                            )
                                        }

                                        Spacer(Modifier.height(4.dp))

                                        // Aksiyon Butonları (Düzenle & Aktif/Pasif)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    campusInput = point.campus
                                                    nameInput = point.name
                                                    descriptionInput = point.description.orEmpty()
                                                    editingPoint = point
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                            ) {
                                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Düzenle", fontSize = 12.sp)
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            Button(
                                                onClick = { onToggleActive(point.id) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (point.active) Vurgu else Adacayi,
                                                ),
                                            ) {
                                                Icon(Icons.Outlined.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(if (point.active) "Pasifleştir" else "Aktifleştir", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Noktalar silinmez, geçmiş buluşma kayıtlarının bütünlüğü için pasifleştirilir. Pasif noktalar yeni seçimlerde görünmez.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KoyuSoluk,
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                                Spacer(Modifier.height(72.dp)) // FAB için boşluk
                            }
                        }
                    }
                }
            }
        }
    }
}
