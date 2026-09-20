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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.ui.screens.common.EmptyStateView
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
fun AdminNoktalarScreen(
    onBack: () -> Unit,
    viewModel: AdminNoktalarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AdminNoktalarContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::loadData,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNoktalarContent(
    state: AdminNoktalarUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
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
                state.points.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.Place,
                        title = "Teslim Noktası Yok",
                        message = "Sistemde tanımlı teslim noktası bulunmuyor.",
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
                                        text = "Kitaplar kampüs içinde yüz yüze teslim ediliyor. Üyeler buluşma yeri olarak bu noktalardan birini seçer veya serbest konum belirtebilir.",
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
                                colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
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
                                        ) {
                                            Icon(Icons.Outlined.Place, contentDescription = null, tint = KoyuVurgu)
                                            Text(
                                                text = point.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
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
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Noktalar silinmez, geçmiş buluşma kayıtlarının bütünlüğü için pasifleştirilir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KoyuSoluk,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminNoktalarPreview() {
    AdminTheme {
        Surface {
            AdminNoktalarContent(
                state = AdminNoktalarUiState(
                    points = listOf(
                        PickupPointDto(id = 1, name = "Merkez Kütüphane Önü", description = "Giriş turnikeleri yanı", active = true),
                        PickupPointDto(id = 2, name = "Mühendislik Fakültesi Kantini", description = "Giriş kat", active = false),
                    ),
                ),
                onBack = {},
                onRetry = {},
            )
        }
    }
}
