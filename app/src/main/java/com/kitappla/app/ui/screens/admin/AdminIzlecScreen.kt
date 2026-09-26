package com.kitappla.app.ui.screens.admin

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.data.dto.ActiveSessionDto
import com.kitappla.app.data.dto.AdminMonitorDto
import com.kitappla.app.data.dto.MonitorSummaryDto
import com.kitappla.app.ui.screens.common.AlertBox
import com.kitappla.app.ui.screens.common.EmptyStateView
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.PullRefresh
import com.kitappla.app.ui.screens.common.StatusBadge
import com.kitappla.app.ui.screens.common.UserAvatar
import com.kitappla.app.ui.theme.Adacayi
import com.kitappla.app.ui.theme.AdminTheme
import com.kitappla.app.ui.theme.EspressoZemin
import com.kitappla.app.ui.theme.KoyuAdacayiMurekkep
import com.kitappla.app.ui.theme.KoyuAdacayiSoft
import com.kitappla.app.ui.theme.statNumber

/** Web'deki "Sistem ve Oturum İzleci"nin (/admin/izlec) karşılığı. */
@Composable
fun AdminIzlecScreen(onBack: () -> Unit, viewModel: AdminIzlecViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Canlı yenileme yalnızca ekran görünürken: arka plandayken sunucuya boşuna istek atılmaz.
    LifecycleStartEffect(Unit) {
        viewModel.startPolling()
        onStopOrDispose { viewModel.stopPolling() }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    AdminIzlecContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRefresh = { viewModel.load() },
        onLiveChange = viewModel::setLive,
        onExpireSession = { viewModel.expireSession(it.id, it.userName) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIzlecContent(
    state: AdminIzlecState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onLiveChange: (Boolean) -> Unit,
    onExpireSession: (ActiveSessionDto) -> Unit = {},
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Sistem İzleci", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.refreshing) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Yenile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EspressoZemin),
            )
        },
        containerColor = EspressoZemin,
    ) { padding ->
        val data = state.data
        PullRefresh(
            loading = state.refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding),
            scrollableContent = data != null,
        ) {
            when {
                data == null && state.error != null -> ErrorStateView(
                    message = state.error,
                    onRetry = onRefresh,
                    modifier = Modifier.align(Alignment.Center),
                )
                data == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> MonitorList(data, state, onLiveChange, onExpireSession)
            }
        }
    }
}

@Composable
private fun MonitorList(
    data: AdminMonitorDto,
    state: AdminIzlecState,
    onLiveChange: (Boolean) -> Unit,
    onExpireSession: (ActiveSessionDto) -> Unit,
) {
    val s = data.summary
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { LiveHeader(state.live, state.lastUpdated, onLiveChange) }
        state.error?.let { item { AlertBox("Son yenileme başarısız: $it", isError = true) } }

        item { SectionTitle("Oturumlar") }
        item {
            MetricGrid(
                listOf(
                    Metric("Aktif oturum", s.activeSessions.toString(), "Süresi dolmamış açık oturum", Icons.Outlined.Wifi, accent = true),
                    Metric(
                        "Çevrim içi üye",
                        s.onlineUsers.toString(),
                        "${s.onlineAdmins} yönetici, ${s.onlineStudents} öğrenci",
                        Icons.Outlined.People,
                    ),
                    Metric("Kalıcı oturum", s.persistentLogins.toString(), "“Beni hatırla” belirteci", Icons.Outlined.VpnKey),
                    Metric("Giriş koruması", s.loginFailuresTracked.toString(), "İzlenen başarısız deneme", Icons.Outlined.Shield),
                ),
            )
        }

        item { SectionTitle("Sunucu") }
        item {
            MetricGrid(
                listOf(
                    Metric(
                        "Çalışma süresi",
                        s.uptimeFormatted.ifBlank { "-" },
                        s.startTime?.let { "Başlangıç: $it" } ?: "Kesintisiz açık",
                        Icons.Outlined.Schedule,
                        compactValue = true,
                    ),
                    Metric(
                        "İşlemci & thread",
                        s.threadCount.toString(),
                        "${s.availableProcessors} çekirdek • zirve ${s.peakThreadCount}",
                        Icons.Outlined.Dns,
                    ),
                ),
            )
        }
        item { MemoryCard(s) }
        item { DatabaseCard(s) }

        item { SectionTitle("Aktif oturumlar (${data.sessions.size})") }
        if (data.sessions.isEmpty()) {
            item {
                EmptyStateView(
                    title = "Aktif oturum yok",
                    message = "Şu anda sunucuda kayıtlı açık oturum bulunmuyor.",
                    icon = Icons.Outlined.Wifi,
                )
            }
        } else {
            // Tanıtıcı boş gelirse (eski sunucu) liste anahtarları çakışıp liste çökmesin diye sıra numarasına düşülür.
            items(data.sessions.size, key = { i -> data.sessions[i].id.ifBlank { "sira-$i" } }) { i ->
                val session = data.sessions[i]
                SessionCard(
                    session = session,
                    expiring = state.expiringId != null && state.expiringId == session.id,
                    onExpire = { onExpireSession(session) },
                )
            }
        }
    }
}

/** "Canlı" anahtarı: yanıp sönen nokta + son güncelleme saati. */
@Composable
private fun LiveHeader(live: Boolean, lastUpdated: String?, onLiveChange: (Boolean) -> Unit) {
    val pulse by rememberInfiniteTransition(label = "canli").animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "nokta",
    )
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .alpha(if (live) pulse else 1f)
                    .clip(CircleShape)
                    .background(if (live) Adacayi else MaterialTheme.colorScheme.onSurfaceVariant),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    if (live) "Canlı izleniyor" else "Canlı izleme kapalı",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    lastUpdated?.let { "Son güncelleme $it • 10 sn'de bir" } ?: "Güncelleniyor…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = live,
                onCheckedChange = onLiveChange,
                colors = SwitchDefaults.colors(checkedTrackColor = Adacayi),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp, start = 4.dp).semantics { heading() },
    )
}

private class Metric(
    val title: String,
    val value: String,
    val caption: String,
    val icon: ImageVector,
    val accent: Boolean = false,
    val compactValue: Boolean = false,
)

@Composable
private fun MetricGrid(metrics: List<Metric>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        metrics.chunked(2).forEach { row ->
            // Aynı satırdaki kartlar en uzun olanın yüksekliğini alır.
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { MetricCard(it, Modifier.weight(1f).fillMaxHeight()) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricCard(metric: Metric, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    metric.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(metric.icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
            Text(
                metric.value,
                style = if (metric.compactValue) MaterialTheme.typography.titleLarge else MaterialTheme.typography.statNumber,
                color = if (metric.accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                metric.caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MemoryCard(s: MonitorSummaryDto) {
    val warn = s.memoryPercent >= AdminIzlecViewModel.MEMORY_WARN_PERCENT
    val barColor = if (warn) MaterialTheme.colorScheme.error else Adacayi
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Memory, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "JVM bellek (heap)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text("%${s.memoryPercent}", style = MaterialTheme.typography.titleLarge, color = barColor)
            }
            LinearProgressIndicator(
                progress = { (s.memoryPercent.coerceIn(0, 100)) / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = EspressoZemin,
            )
            Text(
                "${s.usedMemoryMb} MB kullanımda • ayrılan ${s.totalMemoryMb} MB • üst sınır ${s.maxMemoryMb} MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DatabaseCard(s: MonitorSummaryDto) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.Storage, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text("Veritabanı", style = MaterialTheme.typography.titleSmall)
                Text(
                    s.dbProduct ?: "Bilinmiyor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (s.dbActiveConnections != null || s.dbIdleConnections != null) {
                    Text(
                        "${s.dbActiveConnections ?: 0} aktif, ${s.dbIdleConnections ?: 0} boşta bağlantı",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (s.dbConnected) {
                StatusBadge("Bağlı", containerColor = KoyuAdacayiSoft, contentColor = KoyuAdacayiMurekkep)
            } else {
                StatusBadge("Bağlantı yok", containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.18f), contentColor = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun SessionCard(session: ActiveSessionDto, expiring: Boolean, onExpire: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Oturum sonlandırılsın mı?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${session.userName} adlı kullanıcının bu oturumu hemen kapanır; aynı cihazda yeniden giriş yapması gerekir.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    onExpire()
                }) { Text("Sonlandır", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Vazgeç") } },
        )
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UserAvatar(initials = session.initials, size = 40)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(session.userName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!session.userEmail.isNullOrBlank()) {
                    Text(
                        session.userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val badges = buildList {
                    if (session.currentSession) add("Bu oturum")
                    if (session.admin) add("Yönetici")
                    if (session.student) add("Öğrenci")
                    if (session.blocked) add("Askıda")
                }
                if (badges.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 2.dp)) {
                        badges.forEach { label ->
                            val danger = label == "Askıda"
                            StatusBadge(
                                text = label,
                                containerColor = if (danger) MaterialTheme.colorScheme.error.copy(alpha = 0.18f) else EspressoZemin,
                                contentColor = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                Text(
                    "Son işlem: ${session.lastRequestRelative ?: "Bilinmiyor"}" +
                        (session.lastRequestTime?.takeIf { it != "-" }?.let { " ($it)" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Oturum ${session.maskedSessionId}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
                // Kendi oturumu sunucuda reddedilir; düğme hiç gösterilmez.
                if (!session.currentSession && session.id.isNotBlank()) {
                    OutlinedButton(
                        onClick = { confirm = true },
                        enabled = !expiring,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(top = 6.dp).height(34.dp),
                    ) {
                        if (expiring) {
                            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("Oturumu sonlandır", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun AdminIzlecPreview() {
    AdminTheme {
        AdminIzlecContent(
            state = AdminIzlecState(
                isLoading = false,
                lastUpdated = "12:00:05",
                data = AdminMonitorDto(
                    summary = MonitorSummaryDto(
                        activeSessions = 7, onlineUsers = 5, onlineAdmins = 1, onlineStudents = 3,
                        persistentLogins = 12, uptimeFormatted = "2 gün 4 sa 12 dk", startTime = "22.09.2026 08:14:03",
                        usedMemoryMb = 312, totalMemoryMb = 512, maxMemoryMb = 1024, memoryPercent = 30,
                        availableProcessors = 4, threadCount = 41, peakThreadCount = 58,
                        dbConnected = true, dbProduct = "PostgreSQL 16.4", dbActiveConnections = 2, dbIdleConnections = 8,
                        loginFailuresTracked = 1,
                    ),
                    sessions = listOf(
                        ActiveSessionDto("a", "A1B2C...9Z8Y7", 1, "Ayşe Kaya", "ayse@ornek.com", "AK", student = true,
                            lastRequestTime = "12:00:01", lastRequestRelative = "Az önce"),
                        ActiveSessionDto("b", "Q1W2E...R4T5Y", 2, "Yönetici", "admin@kitappla.com", "YÖ", admin = true,
                            lastRequestTime = "11:52:40", lastRequestRelative = "7 dakika önce"),
                    ),
                ),
            ),
            onBack = {}, onRefresh = {}, onLiveChange = {},
        )
    }
}
