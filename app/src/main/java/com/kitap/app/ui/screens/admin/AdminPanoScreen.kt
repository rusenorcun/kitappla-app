package com.kitap.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Badge
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.data.dto.AdminStatsDto
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.theme.AdminTheme
import com.kitap.app.ui.theme.EspressoYuzey
import com.kitap.app.ui.theme.EspressoZemin
import com.kitap.app.ui.theme.KoyuSoluk
import com.kitap.app.ui.theme.KoyuVurgu
import com.kitap.app.ui.theme.SolukCizgi
import com.kitap.app.ui.theme.Vurgu
import com.kitap.app.ui.theme.statNumber

@Composable
fun AdminPanoScreen(
    onNavigateToBelgeler: () -> Unit,
    onNavigateToUyeler: () -> Unit,
    onNavigateToIcerik: () -> Unit,
    onNavigateToNoktalar: () -> Unit,
    onNavigateToSikayetler: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminPanoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AdminPanoContent(
        state = state,
        onNavigateToBelgeler = onNavigateToBelgeler,
        onNavigateToUyeler = onNavigateToUyeler,
        onNavigateToIcerik = onNavigateToIcerik,
        onNavigateToNoktalar = onNavigateToNoktalar,
        onNavigateToSikayetler = onNavigateToSikayetler,
        onLogout = onLogout,
        onRetry = viewModel::loadData,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminPanoContent(
    state: AdminPanoUiState,
    onNavigateToBelgeler: () -> Unit,
    onNavigateToUyeler: () -> Unit,
    onNavigateToIcerik: () -> Unit,
    onNavigateToNoktalar: () -> Unit,
    onNavigateToSikayetler: () -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(KoyuVurgu),
                        )
                        Text("YÖNETİM", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = "Çıkış yap", tint = KoyuSoluk)
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = KoyuVurgu,
                    )
                }
                state.error != null -> {
                    ErrorStateView(
                        message = state.error,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    val stats = state.stats ?: AdminStatsDto()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Text(
                            text = "Genel İstatistikler",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        // 2x3 Grid of stats
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            maxItemsInEachRow = 2,
                        ) {
                            StatCard(
                                title = "Toplam Üye",
                                value = stats.totalUsers.toString(),
                                icon = Icons.Outlined.People,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                title = "Bekleyen Belge",
                                value = stats.pendingDocs.toString(),
                                icon = Icons.Outlined.School,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                title = "Toplam Bağış",
                                value = stats.donations.toString(),
                                icon = Icons.Outlined.VolunteerActivism,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                title = "Teslim Edilen",
                                value = stats.delivered.toString(),
                                icon = Icons.Outlined.CheckCircle,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Text(
                            text = "Yönetim Menüsü",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        // Navigation tiles
                        AdminNavTile(
                            title = "Öğrenci Belgeleri",
                            subtitle = "İncelenmeyi bekleyen okul belgeleri",
                            icon = Icons.Outlined.Description,
                            badgeCount = state.pendingDocsCount,
                            onClick = onNavigateToBelgeler,
                        )

                        AdminNavTile(
                            title = "Şikâyetler & Moderasyon",
                            subtitle = "Kullanıcılar tarafından iletilen bildirimler",
                            icon = Icons.Outlined.Flag,
                            badgeCount = state.openReportsCount,
                            onClick = onNavigateToSikayetler,
                        )

                        AdminNavTile(
                            title = "Üyeler",
                            subtitle = "Tüm kayıtlı üyeleri görüntüle ve yönet",
                            icon = Icons.Outlined.People,
                            onClick = onNavigateToUyeler,
                        )

                        AdminNavTile(
                            title = "İçerik Moderasyonu",
                            subtitle = "Bağış, istek ve takas ilanlarını denetle",
                            icon = Icons.AutoMirrored.Outlined.MenuBook,
                            onClick = onNavigateToIcerik,
                        )

                        AdminNavTile(
                            title = "Teslim Noktaları",
                            subtitle = "Kampüs içi teslimat noktalarını düzenle",
                            icon = Icons.Outlined.Place,
                            onClick = onNavigateToNoktalar,
                        )

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = KoyuSoluk,
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = KoyuSoluk,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.statNumber,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun AdminNavTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeCount: Int = 0,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EspressoZemin),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = KoyuVurgu,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    if (badgeCount > 0) {
                        Badge(containerColor = Vurgu) {
                            Text(badgeCount.toString())
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = KoyuSoluk,
                )
            }

            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = SolukCizgi,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminPanoPreview() {
    AdminTheme {
        Surface {
            AdminPanoContent(
                state = AdminPanoUiState(
                    stats = AdminStatsDto(
                        totalUsers = 142,
                        pendingDocs = 3,
                        donations = 54,
                        delivered = 112,
                    ),
                    pendingDocsCount = 3,
                    openReportsCount = 2,
                ),
                onNavigateToBelgeler = {},
                onNavigateToUyeler = {},
                onNavigateToIcerik = {},
                onNavigateToNoktalar = {},
                onNavigateToSikayetler = {},
                onLogout = {},
                onRetry = {},
            )
        }
    }
}
