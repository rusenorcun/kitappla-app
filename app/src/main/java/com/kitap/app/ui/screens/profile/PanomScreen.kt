package com.kitap.app.ui.screens.profile

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContactSupport
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.data.dto.MeDto
import com.kitap.app.data.dto.QuotaDto
import com.kitap.app.data.dto.StudentStatus
import com.kitap.app.data.dto.UserDto
import com.kitap.app.data.dto.isApprovedStudent
import com.kitap.app.ui.screens.common.ErrorStateView
import com.kitap.app.ui.screens.common.SectionIconBox
import com.kitap.app.ui.screens.common.StatusBadge
import com.kitap.app.ui.screens.common.SuccessBadge
import com.kitap.app.ui.screens.common.UserAvatar
import com.kitap.app.ui.theme.AdacayiMurekkep
import com.kitap.app.ui.theme.AdacayiSoft
import com.kitap.app.ui.theme.KahveSoluk
import com.kitap.app.ui.theme.KitapTheme
import com.kitap.app.ui.theme.SolukCizgi
import com.kitap.app.ui.theme.VurguSoft

@Composable
fun PanomScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: PanomViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Doğrulama başka bir cihazda onaylanıp uygulamaya dönüldüğünde durum (rozet, kota) güncel görünsün.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    PanomContent(
        state = state,
        onNavigate = onNavigate,
        onLogout = { viewModel.logout(onLogout) },
        onRetry = viewModel::load,
    )
}

@Composable
fun PanomContent(
    state: PanomState,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold { padding ->
        when {
            state.loading && state.me == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null && state.me == null -> ErrorStateView(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier.padding(padding),
            )
            else -> {
                val user = state.me?.user
                val quota = state.me?.quota

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Kullanıcı Kartı
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                UserAvatar(initials = user?.initials ?: "K", size = 52)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user?.name ?: "Kullanıcı",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = user?.email.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KahveSoluk,
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    if (user?.isApprovedStudent == true) {
                                        SuccessBadge("Onaylı Öğrenci")
                                    } else {
                                        StatusBadge(
                                            text = "Üye (Öğrenci Doğrula)",
                                            containerColor = VurguSoft,
                                            modifier = Modifier.clickable { onNavigate("profil/ogrenci") },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Kota Kartı
                    if (quota != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Kitap Alma Kotası", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        StatusBadge(
                                            text = if (quota.canReceive) "Hak Var" else "Kota Dolu",
                                            containerColor = if (quota.canReceive) AdacayiSoft else VurguSoft,
                                            contentColor = if (quota.canReceive) AdacayiMurekkep else MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    "Haftalık Kalan",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = "${quota.weeklyRemaining} / ${quota.weeklyLimit}",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }

                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    "Aylık Kalan",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = "${quota.monthlyRemaining} / ${quota.monthlyLimit}",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // İşlem Menüleri
                    item {
                        Text("İşlemlerim", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column {
                                PanomMenuItem("Bağışlarım", Icons.Outlined.VolunteerActivism) { onNavigate("bagislarim") }
                                PanomMenuItem("Aldıklarım", Icons.Outlined.Inbox) { onNavigate("aldiklarim") }
                                PanomMenuItem("İsteklerim", Icons.Outlined.MenuBook) { onNavigate("isteklerim") }
                                PanomMenuItem("Karşıladıklarım", Icons.Outlined.CheckCircle) { onNavigate("karsiladiklarim") }
                                PanomMenuItem("Takaslarım", Icons.Outlined.SwapHoriz) { onNavigate("takaslarim") }
                                PanomMenuItem("Takas Kitaplarım", Icons.Outlined.Description) { onNavigate("takas/kitaplarim") }
                                PanomMenuItem("Şikâyetlerim", Icons.Outlined.Flag, isLast = true) { onNavigate("sikayetlerim") }
                            }
                        }
                    }

                    // Hesap ve Bilgi
                    item {
                        Text("Hesap & Yardım", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column {
                                PanomMenuItem("Profil Bilgileri", Icons.Outlined.Person) { onNavigate("profil") }
                                PanomMenuItem("Öğrenci Doğrulama", Icons.Outlined.School) { onNavigate("profil/ogrenci") }
                                PanomMenuItem("Sık Sorulan Sorular", Icons.AutoMirrored.Outlined.HelpOutline) { onNavigate("sss") }
                                PanomMenuItem("Topluluk Kuralları", Icons.Outlined.Gavel) { onNavigate("kurallar") }
                                PanomMenuItem("Gizlilik Politikası", Icons.Outlined.PrivacyTip) { onNavigate("gizlilik") }
                                PanomMenuItem("İletişim", Icons.Outlined.ContactSupport, isLast = true) { onNavigate("iletisim") }
                            }
                        }
                    }

                    // Çıkış Butonu
                    item {
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Oturumu Kapat")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanomMenuItem(
    title: String,
    icon: ImageVector,
    isLast: Boolean = false,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionIconBox(icon = icon)
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = SolukCizgi,
                modifier = Modifier.size(18.dp),
            )
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 64.dp, end = 16.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PanomScreenPreview() {
    KitapTheme {
        Surface {
            PanomContent(
                state = PanomState(
                    loading = false,
                    me = MeDto(
                        user = UserDto(
                            id = 1L,
                            name = "Ali Yılmaz",
                            email = "ali@ornek.com",
                            initials = "AY",
                            studentStatus = StudentStatus.APPROVED,
                        ),
                        quota = QuotaDto(
                            tier = "STUDENT",
                            weeklyRemaining = 2,
                            weeklyLimit = 3,
                            monthlyRemaining = 7,
                            monthlyLimit = 10,
                            canReceive = true,
                        ),
                    ),
                ),
                onNavigate = {},
                onLogout = {},
                onRetry = {},
            )
        }
    }
}
