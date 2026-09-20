@file:OptIn(ExperimentalMaterial3Api::class)

package com.kitap.app.ui.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.kitap.app.ui.theme.titleLogo
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kitap.app.core.device.Haptics
import com.kitap.app.core.device.ShakeEffect
import com.kitap.app.core.session.SessionState
import com.kitap.app.data.repo.LoginMode
import com.kitap.app.ui.screens.auth.AdminLoginScreen
import com.kitap.app.ui.screens.auth.AuthViewModel
import com.kitap.app.ui.screens.auth.LoginScreen
import com.kitap.app.ui.screens.auth.RegisterScreen
import com.kitap.app.ui.screens.auth.SifremiUnuttumScreen
import com.kitap.app.ui.screens.auth.SifreSifirlaScreen
import com.kitap.app.ui.screens.common.MenuEntry
import com.kitap.app.ui.screens.donation.AldiklarimScreen
import com.kitap.app.ui.screens.donation.BagislarimScreen
import com.kitap.app.ui.screens.donation.BagisYeniScreen
import com.kitap.app.ui.screens.donation.KitapDetayScreen
import com.kitap.app.ui.screens.info.GizlilikScreen
import com.kitap.app.ui.screens.info.IletisimScreen
import com.kitap.app.ui.screens.info.KurallarScreen
import com.kitap.app.ui.screens.info.SssScreen
import com.kitap.app.ui.screens.kesfet.KesfetScreen
import com.kitap.app.ui.screens.message.MesajlarScreen
import com.kitap.app.ui.screens.message.SohbetScreen
import com.kitap.app.ui.screens.notification.BildirimlerScreen
import com.kitap.app.ui.screens.profile.OgrenciDogrulamaScreen
import com.kitap.app.ui.screens.profile.PanomScreen
import com.kitap.app.ui.screens.profile.ProfilScreen
import com.kitap.app.ui.screens.report.SikayetEtScreen
import com.kitap.app.ui.screens.report.SikayetlerimScreen
import com.kitap.app.ui.screens.request.IsteklerScreen
import com.kitap.app.ui.screens.request.IsteklerimScreen
import com.kitap.app.ui.screens.request.IstekYeniScreen
import com.kitap.app.ui.screens.request.KarsiladiklarimScreen
import com.kitap.app.ui.screens.swap.TakasKitaplarimScreen
import com.kitap.app.ui.screens.swap.TakaslarimScreen
import com.kitap.app.ui.screens.swap.TakasScreen
import com.kitap.app.ui.screens.swap.TakasTeklifDetayScreen
import com.kitap.app.ui.screens.swap.TakasTeklifScreen
import com.kitap.app.ui.theme.AdminTheme

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val TAB_ITEMS = listOf(
    TabItem(Routes.KESFET, "Keşfet", Icons.Outlined.Explore),
    TabItem(Routes.ISTEKLER, "İstekler", Icons.AutoMirrored.Outlined.MenuBook),
    TabItem(Routes.TAKAS, "Takas", Icons.Outlined.SwapHoriz),
    TabItem(Routes.MESAJLAR, "Mesajlar", Icons.Outlined.ChatBubbleOutline),
    TabItem(Routes.PANOM, "Panom", Icons.Outlined.Dashboard),
)

/**
 * Üye ve misafir grafiği (Keşfet açılış). Giriş gerektiren hedefe misafir gidince [onNeedLogin] ile hedef
 * saklanır ve Giriş açılır; giriş sonrası (AppRoot bu grafiği yeniden kurar) hedefe gidilir.
 */
@Composable
fun MemberNavHost(
    session: SessionState,
    unread: Long,
    pendingRoute: String?,
    onNeedLogin: (String) -> Unit,
    onPendingConsumed: () -> Unit,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isGuest = session is SessionState.Guest

    AppRootBackGuard(navController)

    val context = LocalContext.current
    val haptics = remember { Haptics(context) }
    ShakeEffect(enabled = shakeGateEnabled(session, currentRoute)) {
        haptics.shakeBuzz()
        navController.navigate(Routes.ADMIN_LOGIN) { launchSingleTop = true }
    }

    fun open(route: String) {
        when {
            isGuest && Routes.requiresAuth(route) -> {
                onNeedLogin(route)
                navController.navigate(Routes.LOGIN) { launchSingleTop = true }
            }
            route in Routes.TABS -> navController.navigateToTab(route)
            else -> navController.navigate(route) { launchSingleTop = true }
        }
    }

    LaunchedEffect(session) {
        if (session is SessionState.Member && pendingRoute != null) {
            navController.navigate(pendingRoute)
            onPendingConsumed()
        }
    }

    // Misafir giriş kapısından başlayan "girişten sonra hedefe dön" isteği, kullanıcı kimlik akışından
    // (Giriş/Kayıt/Şifre/Yönetici girişi) giriş yapmadan çıkınca unutulur. Akış içinde gezinmek hedefi korur.
    LaunchedEffect(currentRoute) {
        if (isGuest && pendingRoute != null && !Routes.shouldKeepPendingRoute(currentRoute)) {
            onPendingConsumed()
        }
    }

    val showChrome = currentRoute != null && currentRoute in Routes.TABS
    val back: () -> Unit = { navController.popIfNotRoot() }

    Scaffold(
        topBar = {
            if (showChrome) {
                MemberTopBar(
                    unread = unread,
                    onDonate = { open(Routes.BAGIS_YENI) },
                    onBell = { open(Routes.BILDIRIMLER) },
                )
            }
        },
        bottomBar = { if (showChrome) MemberBottomBar(currentRoute) { open(it) } },
    ) { padding ->
        NavHost(navController, startDestination = Routes.KESFET, modifier = Modifier.padding(padding)) {
            // Ana Sekmeler
            composable(Routes.KESFET) {
                KesfetScreen(onOpenBook = { open(Routes.kitapDetay(it)) })
            }
            composable(Routes.ISTEKLER) {
                IsteklerScreen(onNavigateToNewRequest = { open(Routes.ISTEK_YENI) })
            }
            composable(Routes.TAKAS) {
                TakasScreen(
                    onNavigateToMyBooks = { open(Routes.TAKAS_KITAPLARIM) },
                    onProposeSwap = { open(Routes.takasTeklif(it)) },
                )
            }
            composable(Routes.MESAJLAR) {
                MesajlarScreen(onOpenConversation = { open(Routes.sohbet(it)) })
            }
            composable(Routes.PANOM) {
                PanomScreen(onNavigate = { open(it) }, onLogout = onLogout)
            }

            // Alt Sayfalar & Detaylar
            composable(Routes.BILDIRIMLER) {
                BildirimlerScreen(onBack = back, onOpenLink = { if (it != null) open(it) })
            }
            composable(Routes.BAGIS_YENI) {
                BagisYeniScreen(onBack = back, onSuccess = { open(Routes.BAGISLARIM) })
            }
            composable(Routes.ISTEK_YENI) {
                IstekYeniScreen(onBack = back, onSuccess = { open(Routes.ISTEKLERIM) })
            }
            composable(Routes.KITAP_DETAY) {
                KitapDetayScreen(
                    onBack = back,
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                    onNavigateToSwap = { open(Routes.takasTeklif(it)) },
                )
            }
            composable(Routes.BAGISLARIM) {
                BagislarimScreen(
                    onBack = back,
                    onNavigateToNewDonation = { open(Routes.BAGIS_YENI) },
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                )
            }
            composable(Routes.ALDIKLARIM) {
                AldiklarimScreen(
                    onBack = back,
                    onNavigateToExplore = { open(Routes.KESFET) },
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                )
            }
            composable(Routes.ISTEKLERIM) {
                IsteklerimScreen(
                    onBack = back,
                    onNavigateToNewRequest = { open(Routes.ISTEK_YENI) },
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                )
            }
            composable(Routes.KARSILADIKLARIM) {
                KarsiladiklarimScreen(
                    onBack = back,
                    onNavigateToRequests = { open(Routes.ISTEKLER) },
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                )
            }
            composable(Routes.TAKASLARIM) {
                TakaslarimScreen(
                    onBack = back,
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                )
            }
            composable(Routes.TAKAS_KITAPLARIM) {
                TakasKitaplarimScreen(onBack = back)
            }
            composable(Routes.TAKAS_TEKLIF) {
                TakasTeklifScreen(
                    onBack = back,
                    onSuccess = { open(Routes.TAKASLARIM) },
                    onNavigateToAddBook = { open(Routes.TAKAS_KITAPLARIM) },
                )
            }
            composable(Routes.TAKAS_TEKLIF_DETAY) {
                TakasTeklifDetayScreen(
                    onBack = back,
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                    onReport = { kind, refId -> open(Routes.sikayetEt(kind, refId)) },
                )
            }
            composable(Routes.SIKAYETLERIM) {
                SikayetlerimScreen(
                    onBack = back,
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                )
            }
            composable(Routes.SIKAYET_ET) {
                SikayetEtScreen(onBack = back)
            }
            composable(Routes.PROFIL) {
                ProfilScreen(onBack = back)
            }
            composable(Routes.OGRENCI_DOGRULAMA) {
                OgrenciDogrulamaScreen(onBack = back)
            }
            composable(Routes.SOHBET) {
                SohbetScreen(onBack = back)
            }
            composable(Routes.SSS) {
                SssScreen(
                    onBack = back,
                    onNavigateToContact = { open(Routes.ILETISIM) },
                )
            }
            composable(Routes.KURALLAR) {
                KurallarScreen(onBack = back)
            }
            composable(Routes.GIZLILIK) {
                GizlilikScreen(onBack = back)
            }
            composable(Routes.ILETISIM) {
                IletisimScreen(
                    onBack = back,
                    onNavigateToSss = { open(Routes.SSS) },
                    onNavigateToKurallar = { open(Routes.KURALLAR) },
                    onNavigateToGizlilik = { open(Routes.GIZLILIK) },
                    onNavigateToSupportChat = { open(Routes.MESAJLAR) },
                )
            }

            // Auth Ekranları
            composable(Routes.LOGIN) {
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                LoginScreen(
                    form = form,
                    onSubmit = { email, password -> vm.login(email, password, LoginMode.MEMBER) },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) { launchSingleTop = true } },
                    onForgotPassword = {
                        navController.navigate(Routes.SIFREMI_UNUTTUM) { launchSingleTop = true }
                    },
                )
            }
            composable(Routes.REGISTER) {
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                RegisterScreen(
                    form = form,
                    onSubmit = { name, email, password, confirm -> vm.register(name, email, password, confirm) },
                    onNavigateToLogin = { navController.popIfNotRoot() },
                )
            }
            composable(Routes.SIFREMI_UNUTTUM) {
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                SifremiUnuttumScreen(
                    form = form,
                    onSubmit = { email, onSent -> vm.forgotPassword(email, onSent) },
                    onNavigateToReset = { open(Routes.SIFRE_SIFIRLA) },
                    onBack = back,
                )
            }
            composable(Routes.SIFRE_SIFIRLA) {
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                SifreSifirlaScreen(
                    form = form,
                    onSubmit = { token, newPass, confirmPass, onSuccess ->
                        vm.resetPassword(token, newPass, confirmPass, onSuccess)
                    },
                    onSuccess = { open(Routes.LOGIN) },
                    onBack = back,
                )
            }
            composable(Routes.ADMIN_LOGIN) {
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                AdminTheme {
                    AdminLoginScreen(
                        form = form,
                        onSubmit = { email, password -> vm.login(email, password, LoginMode.ADMIN) },
                        onBack = back,
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberTopBar(unread: Long, onDonate: () -> Unit, onBell: () -> Unit) {
    val lineCol = MaterialTheme.colorScheme.outlineVariant
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                // 1px --line alt kenarlık
                drawLine(
                    color = lineCol,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            },
    ) {
        TopAppBar(
            title = {
                Text(
                    "KitAppLa",
                    style = MaterialTheme.typography.titleLogo,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            actions = {
                Button(
                    onClick = onDonate,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Bağış yap",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                    )
                }
                IconButton(onClick = onBell) {
                    BadgedBox(
                        badge = {
                            if (unread > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White,
                                ) {
                                    Text(
                                        unread.toString(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                        },
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Bildirimler",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )
    }
}

@Composable
private fun MemberBottomBar(currentRoute: String?, onTab: (String) -> Unit) {
    val lineCol = MaterialTheme.colorScheme.outlineVariant
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                // 1px --line üst kenarlık
                drawLine(
                    color = lineCol,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            },
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            TAB_ITEMS.forEach { tab ->
                val selected = currentRoute == tab.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTab(tab.route) },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    label = {
                        Text(
                            tab.label,
                            maxLines = 1,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

