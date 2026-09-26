@file:OptIn(ExperimentalMaterial3Api::class)

package com.kitappla.app.ui.nav

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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.sp
import com.kitappla.app.ui.theme.titleLogo
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kitappla.app.core.device.Haptics
import com.kitappla.app.core.device.ShakeEffect
import com.kitappla.app.core.session.SessionState
import com.kitappla.app.data.repo.LoginMode
import com.kitappla.app.ui.screens.auth.AdminLoginScreen
import com.kitappla.app.ui.screens.auth.AuthViewModel
import com.kitappla.app.ui.screens.auth.HesapDogrulamaScreen
import com.kitappla.app.ui.screens.auth.HesapDogrulamaViewModel
import com.kitappla.app.ui.screens.auth.LoginScreen
import com.kitappla.app.ui.screens.auth.RegisterScreen
import com.kitappla.app.ui.screens.auth.SifremiUnuttumScreen
import com.kitappla.app.ui.screens.auth.SifreSifirlaScreen
import com.kitappla.app.ui.screens.donation.AldiklarimScreen
import com.kitappla.app.ui.screens.donation.BagislarimScreen
import com.kitappla.app.ui.screens.donation.BagisYeniScreen
import com.kitappla.app.ui.screens.donation.KitapDetayScreen
import com.kitappla.app.ui.screens.info.GizlilikScreen
import com.kitappla.app.ui.screens.info.IletisimScreen
import com.kitappla.app.ui.screens.info.IletisimViewModel
import com.kitappla.app.ui.screens.info.KurallarScreen
import com.kitappla.app.ui.screens.info.SssScreen
import com.kitappla.app.ui.screens.kesfet.KesfetScreen
import com.kitappla.app.ui.screens.message.MesajlarScreen
import com.kitappla.app.ui.screens.message.SohbetScreen
import com.kitappla.app.ui.screens.notification.BildirimlerScreen
import com.kitappla.app.ui.screens.profile.OgrenciDogrulamaScreen
import com.kitappla.app.ui.screens.profile.PanomScreen
import com.kitappla.app.ui.screens.profile.ProfilScreen
import com.kitappla.app.ui.screens.report.SikayetEtScreen
import com.kitappla.app.ui.screens.report.SikayetlerimScreen
import com.kitappla.app.ui.screens.request.IsteklerScreen
import com.kitappla.app.ui.screens.request.IsteklerimScreen
import com.kitappla.app.ui.screens.request.IstekYeniScreen
import com.kitappla.app.ui.screens.request.KarsiladiklarimScreen
import com.kitappla.app.ui.screens.swap.TakasKitapEkleScreen
import com.kitappla.app.ui.screens.swap.TakasKitaplarimScreen
import com.kitappla.app.ui.screens.swap.TakaslarimScreen
import com.kitappla.app.ui.screens.swap.TakasScreen
import com.kitappla.app.ui.screens.swap.TakasTeklifDetayScreen
import com.kitappla.app.ui.screens.swap.TakasTeklifScreen
import com.kitappla.app.ui.theme.AdminTheme

/** "Takasa Kitap Ekle" → "Takas Kitaplarım" dönüş sonucu (önceki hedefin savedStateHandle'ında). */
private const val SWAP_BOOK_ADDED = "takasKitabiEklendi"

const val PASSWORD_UPDATED_NOTICE = "Şifren değiştirildi. Güvenliğin için oturumların kapatıldı; yeni şifrenle giriş yap."

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
 * saklanır ve Giriş açılır; giriş sonrası (AppRoot bu grafiği yeniden kurar) hedefe gidilir. E-posta bağlantısından
 * gelen hedef de [pendingRoute] ile gelir: herkese açıksa hemen açılır, giriş istiyorsa önce Giriş açılır.
 *
 * @param onSignOutTo oturumu kapatıp misafir grafiğinde verilen rotayı açar (ör. şifre sıfırlandıktan sonra Giriş)
 */
@Composable
fun MemberNavHost(
    session: SessionState,
    unread: Long,
    pendingRoute: String?,
    onNeedLogin: (String) -> Unit,
    onPendingConsumed: () -> Unit,
    onLogout: () -> Unit,
    onSignOutTo: (route: String, loginNotice: String?) -> Unit,
    onRefreshUnread: () -> Unit = {},
    loginNotice: String? = null,
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

    /** Misafirin denediği işlem için: [returnTo] saklanır, Giriş açılır; girişten sonra oraya dönülür. */
    fun requireLogin(returnTo: String) {
        onNeedLogin(returnTo)
        navController.navigate(Routes.LOGIN) { launchSingleTop = true }
    }

    /**
     * Form gönderildikten sonra sonuca gider ve formu yığından çıkarır: geri basınca dolu form yeniden açılıp aynı
     * kayıt ikinci kez gönderilemesin.
     */
    fun replaceWith(route: String) {
        val formId = navController.currentBackStackEntry?.destination?.id
        navController.navigate(route) {
            if (formId != null) popUpTo(formId) { inclusive = true }
            launchSingleTop = true
        }
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

    LaunchedEffect(session, pendingRoute) {
        val target = pendingRoute ?: return@LaunchedEffect
        when {
            session is SessionState.Member -> {
                navController.navigate(target)
                onPendingConsumed()
            }
            // Misafir + giriş isteyen hedef (ör. okul e-postası bağlantısı): hedef saklı kalır, Giriş açılır.
            // Kullanıcı zaten Giriş ya da Kayıt ekranındaysa yerinde bırakılır (hedef girişten sonra açılır).
            session is SessionState.Guest && Routes.requiresAuth(target) -> {
                if (currentRoute != Routes.LOGIN && currentRoute != Routes.REGISTER) {
                    navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                }
            }
            // Misafir + herkese açık hedef (ör. şifre sıfırlama ya da "e-postan doğrulandı" bağlantısı).
            session is SessionState.Guest -> {
                navController.navigate(target)
                onPendingConsumed()
            }
        }
    }

    // Misafir giriş kapısından başlayan "girişten sonra hedefe dön" isteği, kullanıcı kimlik akışından
    // (Giriş/Kayıt/Şifre/Yönetici girişi) giriş yapmadan çıkınca unutulur. Akış içinde gezinmek hedefi korur.
    LaunchedEffect(currentRoute) {
        if (isGuest && pendingRoute != null && !Routes.shouldKeepPendingRoute(currentRoute)) {
            onPendingConsumed()
        }
    }

    // Rozet (okunmamış bildirim) yalnızca oturum değişince çekiliyordu: bildirimler okununca ya da yenisi gelince
    // güncellenmiyordu. Sekmeye dönüşte ve uygulama öne gelince tazelenir.
    LaunchedEffect(currentRoute) { if (currentRoute in Routes.TABS) onRefreshUnread() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onRefreshUnread() }

    val showChrome = currentRoute != null && currentRoute in Routes.TABS
    val back: () -> Unit = { navController.popIfNotRoot() }

    // Logo çubuğu sayfayla birlikte yukarı kayar (sabit kalmaz); yukarı çekilince geri gelir.
    val topBarScroll = rememberMemberTopBarScroll(showChrome)
    // Sekme/sayfa değişince çubuk yeni ekranda gizli başlamasın.
    LaunchedEffect(currentRoute) { topBarScroll.state.heightOffset = 0f }

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            if (showChrome) {
                MemberTopBar(
                    scrollBehavior = topBarScroll,
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
                IsteklerScreen(
                    onNavigateToNewRequest = { open(Routes.ISTEK_YENI) },
                    onRequireLogin = if (isGuest) ({ requireLogin(Routes.ISTEKLER) }) else null,
                )
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
                // Sunucu web yolu gönderir (/bagislarim, /mesajlar/12…); karşılığı olmayan bağlantı yok sayılır.
                BildirimlerScreen(onBack = back, onOpenLink = { link -> WebRoutes.toRoute(link)?.let { open(it) } })
            }
            composable(Routes.BAGIS_YENI) {
                BagisYeniScreen(onBack = back, onSuccess = { replaceWith(Routes.BAGISLARIM) })
            }
            composable(Routes.ISTEK_YENI) {
                IstekYeniScreen(onBack = back, onSuccess = { replaceWith(Routes.ISTEKLERIM) })
            }
            composable(Routes.KITAP_DETAY) { entry ->
                val bookRoute = entry.arguments?.getString("id")?.let { "kitap/$it" } ?: Routes.KESFET
                KitapDetayScreen(
                    onBack = back,
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                    // "Takasa taşı" yeni takas kitabının kimliğini döndürür (üyenin kendi kitabı): teklif değil,
                    // takas kitaplarım açılır.
                    onNavigateToSwap = { replaceWith(Routes.TAKAS_KITAPLARIM) },
                    onRequireLogin = if (isGuest) ({ requireLogin(bookRoute) }) else null,
                )
            }
            composable(Routes.BAGISLARIM) {
                BagislarimScreen(
                    onBack = back,
                    onNavigateToNewDonation = { open(Routes.BAGIS_YENI) },
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                    onReportClaim = { open(Routes.sikayetEt("CLAIM", it)) },
                )
            }
            composable(Routes.ALDIKLARIM) {
                AldiklarimScreen(
                    onBack = back,
                    onNavigateToExplore = { open(Routes.KESFET) },
                    onNavigateToChat = { open(Routes.sohbet(it)) },
                    onReportClaim = { open(Routes.sikayetEt("CLAIM", it)) },
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
                    onNavigateToOfferDetail = { open(Routes.takasTeklifDetay(it)) },
                    onReportOffer = { open(Routes.sikayetEt("SWAP_OFFER", it)) },
                )
            }
            composable(Routes.TAKAS_KITAPLARIM) { entry ->
                val bookAdded by entry.savedStateHandle.getStateFlow(SWAP_BOOK_ADDED, false).collectAsStateWithLifecycle()
                TakasKitaplarimScreen(
                    onBack = back,
                    onAddBook = { open(Routes.TAKAS_KITAP_EKLE) },
                    bookAdded = bookAdded,
                    onBookAddedHandled = { entry.savedStateHandle[SWAP_BOOK_ADDED] = false },
                )
            }
            composable(Routes.TAKAS_KITAP_EKLE) {
                TakasKitapEkleScreen(
                    onBack = back,
                    onSuccess = {
                        // Takas kitaplarım listesi dönüşte tazelenip "eklendi" der.
                        navController.previousBackStackEntry?.savedStateHandle?.set(SWAP_BOOK_ADDED, true)
                        back()
                    },
                )
            }
            composable(
                route = Routes.TAKAS_TEKLIF_PATTERN,
                arguments = listOf(
                    navArgument(Routes.TAKAS_TEKLIF_TARGET_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
            ) {
                TakasTeklifScreen(
                    onBack = back,
                    onSuccess = { replaceWith(Routes.TAKASLARIM) },
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
                // Sunucu şifre değişince bu oturum dahil tüm oturumları düşürür (web'de de öyle): yerel oturum kapanır,
                // Giriş açıklamayla açılır. Aksi hâlde sonraki istekte uygulama sessizce misafire düşüyordu.
                ProfilScreen(onBack = back, onPasswordChanged = { onSignOutTo(Routes.LOGIN, PASSWORD_UPDATED_NOTICE) })
            }
            composable(
                route = Routes.HESAP_DOGRULAMA_PATTERN,
                arguments = listOf(
                    navArgument(HesapDogrulamaViewModel.KAYIT_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
            ) {
                AccountVerification(navController, back, ::open)
            }
            composable(
                route = Routes.EPOSTA_DOGRULANDI_PATTERN,
                arguments = listOf(
                    // Boş varsayılan: rota bu yoldan açıldı (bağlantı web'de onaylandı); "gecersiz" ise bağlantı geçersiz.
                    navArgument(HesapDogrulamaViewModel.DURUM_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                ),
            ) {
                AccountVerification(navController, back, ::open)
            }
            composable(
                route = Routes.EPOSTA_DOGRULA_PATTERN,
                arguments = listOf(
                    // Bağlantı uygulamada açıldı (App Link): jetonu ekran onaylatır.
                    navArgument(HesapDogrulamaViewModel.TOKEN_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
            ) {
                AccountVerification(navController, back, ::open)
            }
            composable(
                route = Routes.OGRENCI_DOGRULAMA_PATTERN,
                arguments = listOf(
                    navArgument("token") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
            ) {
                OgrenciDogrulamaScreen(onBack = back, onExplore = { open(Routes.KESFET) })
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
                val supportVm: IletisimViewModel = hiltViewModel()
                val supportOpening by supportVm.opening.collectAsStateWithLifecycle()
                val supportError by supportVm.error.collectAsStateWithLifecycle()
                IletisimScreen(
                    onBack = back,
                    onNavigateToSss = { open(Routes.SSS) },
                    onNavigateToKurallar = { open(Routes.KURALLAR) },
                    onNavigateToGizlilik = { open(Routes.GIZLILIK) },
                    onNavigateToSupportChat = {
                        supportVm.openSupportChat(
                            onOpened = { open(Routes.sohbet(it)) },
                            onNeedLogin = { requireLogin(Routes.ILETISIM) },
                        )
                    },
                    supportOpening = supportOpening,
                    supportError = supportError,
                )
            }

            // Auth Ekranları
            composable(Routes.LOGIN) {
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                LoginScreen(
                    form = form,
                    notice = loginNotice,
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
                    onSubmit = { name, email, password, confirm, school, phone ->
                        vm.register(name, email, password, confirm, school, phone) { user, emailSent ->
                            // Kayıt oturumu açar ve grafik yeniden kurulur; doğrulanmamış hesap önce "gelen kutunu
                            // kontrol et" ekranını görür (bekleyen rota yeni grafikte açılır).
                            if (!user.emailVerified) onNeedLogin(Routes.hesapDogrulamaAfterRegister(emailSent))
                        }
                    },
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
            composable(
                route = Routes.SIFRE_SIFIRLA_PATTERN,
                arguments = listOf(
                    navArgument("token") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
            ) { entry ->
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                SifreSifirlaScreen(
                    form = form,
                    initialToken = entry.arguments?.getString("token"),
                    onSubmit = { token, newPass, confirmPass, onSuccess ->
                        vm.resetPassword(token, newPass, confirmPass, onSuccess)
                    },
                    onDone = {
                        if (session is SessionState.Member) {
                            // Sunucu şifre değişince tüm oturumları kapatır; yerel oturum da kapanıp Giriş açılır.
                            onSignOutTo(Routes.LOGIN, PASSWORD_UPDATED_NOTICE)
                        } else {
                            // Kullanılmış sıfırlama formu (ve Şifremi Unuttum) geri yığında kalmasın.
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }
                    },
                    onRequestNewLink = { navController.navigate(Routes.SIFREMI_UNUTTUM) { launchSingleTop = true } },
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

/**
 * Logo çubuğunun kaydırma davranışı. Çubuk yalnızca sekmelerde görünür; diğer ekranlarda kaydırmaya karışmamalıdır.
 * Karışırsa, çubuk hiç ölçülmeden açılan ekranlarda (ör. Giriş → Kayıt → E-posta doğrulama) çubuğun daralma sınırı
 * başlangıç değeri `-Float.MAX_VALUE` kalır ve görünmeyen çubuk yukarı kaydırmanın tamamını yutar: sayfa hiç kaymaz.
 * Çubuk ölçülmüş olsa da gizli ekranlarda ilk ~64dp kaydırma boşa giderdi.
 */
@Composable
internal fun rememberMemberTopBarScroll(showChrome: Boolean): TopAppBarScrollBehavior {
    val chromeVisible by rememberUpdatedState(showChrome)
    return TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = { chromeVisible })
}

/** Hesap doğrulama ekranı (üç rotada ortak): Keşfet/Panom sekmelerine ve girişe giden yollar. */
@Composable
private fun AccountVerification(
    navController: androidx.navigation.NavHostController,
    onBack: () -> Unit,
    open: (String) -> Unit,
) {
    HesapDogrulamaScreen(
        onBack = onBack,
        onContinue = { open(Routes.KESFET) },
        onOpenDashboard = { open(Routes.PANOM) },
        onLogin = { navController.navigate(Routes.LOGIN) { launchSingleTop = true } },
        // Misafir + geçersiz bağlantı: giriş yaptıktan sonra bu ekran (yeni bağlantı isteme) açılır.
        onLoginThenVerify = { open(Routes.HESAP_DOGRULAMA) },
    )
}

@Composable
private fun MemberTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    unread: Long,
    onDonate: () -> Unit,
    onBell: () -> Unit,
) {
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
                scrolledContainerColor = MaterialTheme.colorScheme.surface,
            ),
            scrollBehavior = scrollBehavior,
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

