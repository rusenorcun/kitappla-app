package com.kitappla.app.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.ui.screens.common.AlertBox
import com.kitappla.app.ui.screens.common.FlowHeader
import com.kitappla.app.ui.screens.common.HeroTone
import com.kitappla.app.ui.screens.common.HintText
import com.kitappla.app.ui.screens.common.NO_EMAIL_APP_MESSAGE
import com.kitappla.app.ui.screens.common.SectionIconBox
import com.kitappla.app.ui.screens.common.StatusHero
import com.kitappla.app.ui.screens.common.StepsCard
import com.kitappla.app.ui.screens.common.StudentPriorityCard
import com.kitappla.app.ui.screens.common.openEmailApp
import com.kitappla.app.ui.screens.common.textWithBold
import com.kitappla.app.ui.theme.KitapplaTheme

private val INBOX_STEPS = listOf(
    "Gelen kutunu aç" to "“E-posta adresini doğrula” konulu iletiyi bul; gelmesi birkaç dakika sürebilir.",
    "Bağlantıya dokun" to "KitAppLa kendiliğinden açılır ve doğrulamayı tamamlar.",
    "Başla" to "Hesabın bağış, istek, takas ve mesaj için hazır olur.",
)

/**
 * Hesap e-postası doğrulama ekranı: "gelen kutunu kontrol et", "doğrulandı", "bağlantı geçersiz" ve oturum açık
 * değilken gösterilen hâller (bkz. [HesapDogrulamaView]). Kullanıcı e-posta uygulamasından geri dönünce durum
 * sessizce yeniden sorulur; bağlantı başka bir cihazda onaylanmışsa ekran kendiliğinden "doğrulandı"ya geçer.
 *
 * @param onContinue Keşfet'e (doğrulandıysa işe başlamak, değilse şimdilik gezmek için)
 * @param onOpenDashboard Panom sekmesine
 * @param onLogin Giriş ekranına (misafir)
 * @param onLoginThenVerify giriş yaptırıp bu ekrana geri getirir (misafir + geçersiz bağlantı)
 */
@Composable
fun HesapDogrulamaScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onOpenDashboard: () -> Unit,
    onLogin: () -> Unit,
    onLoginThenVerify: () -> Unit,
    viewModel: HesapDogrulamaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.check(silent = true) }

    HesapDogrulamaContent(
        state = state,
        onBack = onBack,
        onOpenMail = { if (!openEmailApp(context)) viewModel.showError(NO_EMAIL_APP_MESSAGE) },
        onCheck = { viewModel.check(silent = false) },
        onResend = viewModel::resend,
        onContinue = onContinue,
        onOpenDashboard = onOpenDashboard,
        onLogin = onLogin,
        onLoginThenVerify = onLoginThenVerify,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HesapDogrulamaContent(
    state: HesapDogrulamaState,
    onBack: () -> Unit,
    onOpenMail: () -> Unit,
    onCheck: () -> Unit,
    onResend: () -> Unit,
    onContinue: () -> Unit,
    onOpenDashboard: () -> Unit,
    onLogin: () -> Unit,
    onLoginThenVerify: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("E-posta Doğrulama", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.view == HesapDogrulamaView.CHECKING) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    "Hesabın kontrol ediliyor…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    when (state.view) {
                        HesapDogrulamaView.PENDING -> PendingBody(state, onOpenMail, onCheck, onResend, onContinue)
                        HesapDogrulamaView.INVALID_LINK -> InvalidBody(
                            body = textWithBold(
                                "Süresi dolmuş ya da yerine yenisi gönderilmiş olabilir. ",
                                state.email,
                                " adresine yeni bir doğrulama bağlantısı gönderebiliriz.",
                            ),
                            error = state.error,
                            primaryText = "Yeni bağlantı gönder",
                            primaryLoading = state.resending,
                            onPrimary = onResend,
                            onContinue = onContinue,
                        )
                        HesapDogrulamaView.VERIFIED -> VerifiedBody(state, onContinue, onOpenDashboard)
                        HesapDogrulamaView.GUEST_VERIFIED -> {
                            StatusHero(Icons.Outlined.MarkEmailRead, HeroTone.SUCCESS)
                            FlowHeader(
                                eyebrow = "Hesabın hazır",
                                title = "E-postan doğrulandı",
                                body = AnnotatedString(
                                    "Bağış yapmaya, kitap istemeye ve takas etmeye başlamak için hesabına giriş yap.",
                                ),
                                tone = HeroTone.SUCCESS,
                            )
                            FlowButton("Giriş yap", onClick = onLogin)
                            TextButton(onClick = onContinue) { Text("Keşfet'e göz at") }
                        }
                        HesapDogrulamaView.GUEST_INVALID -> InvalidBody(
                            body = AnnotatedString(
                                "Süresi dolmuş ya da yerine yenisi gönderilmiş olabilir. Giriş yaptıktan sonra yeni bir " +
                                    "doğrulama bağlantısı isteyebilirsin.",
                            ),
                            error = null,
                            primaryText = "Giriş yap ve yeni bağlantı iste",
                            primaryLoading = false,
                            onPrimary = onLoginThenVerify,
                            onContinue = onContinue,
                        )
                        HesapDogrulamaView.LINK_FAILED -> {
                            StatusHero(Icons.Outlined.CloudOff, HeroTone.ATTENTION)
                            FlowHeader(
                                eyebrow = "Doğrulama tamamlanamadı",
                                title = "Bağlantını şu an onaylayamadık",
                                body = AnnotatedString(
                                    "İnternet bağlantını kontrol edip tekrar dene. E-postadaki bağlantı bu sürede geçerliliğini korur.",
                                ),
                                tone = HeroTone.ATTENTION,
                            )
                            state.error?.let { AlertBox(it, isError = true) }
                            FlowButton("Tekrar dene", icon = Icons.Outlined.Refresh, loading = state.checking, onClick = onCheck)
                            TextButton(onClick = onContinue) { Text("Keşfet'e göz at") }
                        }
                        HesapDogrulamaView.CHECKING -> Unit
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PendingBody(
    state: HesapDogrulamaState,
    onOpenMail: () -> Unit,
    onCheck: () -> Unit,
    onResend: () -> Unit,
    onContinue: () -> Unit,
) {
    StatusHero(Icons.Outlined.MarkEmailUnread, HeroTone.ATTENTION)
    FlowHeader(
        eyebrow = "Son bir adım",
        title = "E-postanı doğrula",
        body = textWithBold(
            "Bağış, istek, takas ve mesaj gibi işlemler için ",
            state.email,
            " adresinin sana ait olduğunu doğrulaman gerekiyor.",
        ),
        tone = HeroTone.ATTENTION,
    )
    if (state.otherAccountOpen) {
        AlertBox(
            "Onayladığın bağlantı bu cihazda açık olan hesaba ait değil. Bu hesap için yeni bir bağlantı isteyebilirsin.",
            isError = true,
        )
    }
    state.sentMessage?.let { AlertBox(it, isError = false) }
    state.error?.let { AlertBox(it, isError = true) }

    StepsCard(INBOX_STEPS)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowButton("E-posta uygulamasını aç", icon = Icons.Outlined.Mail, onClick = onOpenMail)
        FlowButton(
            "Doğruladım, kontrol et",
            icon = Icons.Outlined.Refresh,
            loading = state.checking,
            outlined = true,
            onClick = onCheck,
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val cooling = state.resendCooldown > 0
        TextButton(onClick = onResend, enabled = !state.resending && !cooling) {
            if (state.resending) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                if (cooling) "Yeniden gönder (${state.resendCooldown} sn)" else "Doğrulama e-postasını yeniden gönder",
                fontWeight = FontWeight.Bold,
            )
        }
        HintText("E-posta gelmediyse gereksiz (spam) klasörüne de bak.")
    }

    TextButton(onClick = onContinue) {
        Text("Şimdilik keşfetmeye devam et", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InvalidBody(
    body: AnnotatedString,
    error: String?,
    primaryText: String,
    primaryLoading: Boolean,
    onPrimary: () -> Unit,
    onContinue: () -> Unit,
) {
    StatusHero(Icons.Outlined.LinkOff, HeroTone.ATTENTION)
    FlowHeader(
        eyebrow = "Bağlantı geçersiz",
        title = "Bu bağlantı artık çalışmıyor",
        body = body,
        tone = HeroTone.ATTENTION,
    )
    error?.let { AlertBox(it, isError = true) }
    FlowButton(primaryText, loading = primaryLoading, onClick = onPrimary)
    TextButton(onClick = onContinue) { Text("Keşfet'e göz at") }
}

@Composable
private fun VerifiedBody(state: HesapDogrulamaState, onContinue: () -> Unit, onOpenDashboard: () -> Unit) {
    StatusHero(Icons.Outlined.MarkEmailRead, HeroTone.SUCCESS)
    FlowHeader(
        eyebrow = "Hesabın hazır",
        title = if (state.justVerified) "E-postan doğrulandı" else "Hesabın zaten doğrulanmış",
        body = textWithBold("", state.email, " adresi onaylandı. Artık KitAppLa'nın tüm özelliklerini kullanabilirsin."),
        tone = HeroTone.SUCCESS,
    )
    UnlockedFeatures()
    if (state.studentUnlocked) {
        StudentPriorityCard(title = "Okul adresinle doğruladın: öğrenci önceliğin açıldı")
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowButton("Keşfetmeye başla", onClick = onContinue)
        FlowButton("Panoma git", outlined = true, onClick = onOpenDashboard)
    }
}

/** Doğrulamayla açılan işlemler: 2×2 küçük kart. */
@Composable
private fun UnlockedFeatures() {
    val items = listOf(
        "Bağış yap" to Icons.Outlined.VolunteerActivism,
        "Kitap iste" to Icons.AutoMirrored.Outlined.MenuBook,
        "Takas et" to Icons.Outlined.SwapHoriz,
        "Mesajlaş" to Icons.Outlined.ChatBubbleOutline,
    )
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (label, icon) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SectionIconBox(icon)
                            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/** Akış ekranlarının tam genişlik düğmesi; yüklenirken metin yerine küçük bir gösterge. */
@Composable
internal fun FlowButton(
    text: String,
    icon: ImageVector? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    outlined: Boolean = false,
    onClick: () -> Unit,
) {
    val modifier = Modifier.fillMaxWidth().height(50.dp)
    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
    if (outlined) {
        OutlinedButton(onClick = onClick, enabled = enabled && !loading, modifier = modifier) { content() }
    } else {
        Button(onClick = onClick, enabled = enabled && !loading, modifier = modifier) { content() }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HesapDogrulamaPendingPreview() {
    KitapplaTheme {
        Surface {
            HesapDogrulamaContent(
                state = HesapDogrulamaState(
                    view = HesapDogrulamaView.PENDING,
                    email = "ayse@ornek.com",
                    sentMessage = "ayse@ornek.com adresine doğrulama bağlantısı gönderildi.",
                    resendCooldown = 24,
                ),
                onBack = {}, onOpenMail = {}, onCheck = {}, onResend = {}, onContinue = {},
                onOpenDashboard = {}, onLogin = {}, onLoginThenVerify = {},
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HesapDogrulamaVerifiedPreview() {
    KitapplaTheme {
        Surface {
            HesapDogrulamaContent(
                state = HesapDogrulamaState(
                    view = HesapDogrulamaView.VERIFIED,
                    email = "ayse@ogr.universite.edu.tr",
                    justVerified = true,
                    studentUnlocked = true,
                ),
                onBack = {}, onOpenMail = {}, onCheck = {}, onResend = {}, onContinue = {},
                onOpenDashboard = {}, onLogin = {}, onLoginThenVerify = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HesapDogrulamaInvalidPreview() {
    KitapplaTheme {
        Surface {
            HesapDogrulamaContent(
                state = HesapDogrulamaState(view = HesapDogrulamaView.INVALID_LINK, email = "ayse@ornek.com"),
                onBack = {}, onOpenMail = {}, onCheck = {}, onResend = {}, onContinue = {},
                onOpenDashboard = {}, onLogin = {}, onLoginThenVerify = {},
            )
        }
    }
}
