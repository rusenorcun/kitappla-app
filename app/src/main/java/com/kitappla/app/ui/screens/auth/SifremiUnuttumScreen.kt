package com.kitappla.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitappla.app.ui.screens.common.AlertBox
import com.kitappla.app.ui.screens.common.FlowHeader
import com.kitappla.app.ui.screens.common.HeroTone
import com.kitappla.app.ui.screens.common.HintText
import com.kitappla.app.ui.screens.common.NO_EMAIL_APP_MESSAGE
import com.kitappla.app.ui.screens.common.StatusHero
import com.kitappla.app.ui.screens.common.StepsCard
import com.kitappla.app.ui.screens.common.openEmailApp
import com.kitappla.app.ui.screens.common.textWithBold
import com.kitappla.app.ui.theme.KitapplaTheme

private val RESET_STEPS = listOf(
    "Gelen kutunu aç" to "“Şifre sıfırlama” konulu iletiyi bul; gelmesi birkaç dakika sürebilir.",
    "Bağlantıya dokun" to "Bağlantı yeni şifreni belirleyeceğin ekranı açar.",
    "Yeni şifreni seç" to "Ardından yeni şifrenle giriş yapabilirsin.",
)

/**
 * Şifre sıfırlama bağlantısı isteme. Gönderimden sonra form yerini "gelen kutunu kontrol et" hâline bırakır.
 * Sunucu, adresin kayıtlı olup olmadığını ele vermemek için her durumda aynı yanıtı verir; metin de buna göre
 * ("bu adres kayıtlıysa") yazılmıştır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SifremiUnuttumScreen(
    form: AuthFormState,
    onSubmit: (email: String, onSent: (String) -> Unit) -> Unit,
    onNavigateToReset: () -> Unit,
    onBack: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var sentTo by rememberSaveable { mutableStateOf<String?>(null) }
    var noMailApp by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Şifremi Unuttum", fontWeight = FontWeight.Bold) },
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
                val target = sentTo
                if (target != null) {
                    StatusHero(Icons.Outlined.MarkEmailUnread, HeroTone.ATTENTION)
                    FlowHeader(
                        eyebrow = "Bağlantı yolda",
                        title = "Gelen kutunu kontrol et",
                        body = textWithBold(
                            "Bu adres bir hesaba kayıtlıysa ",
                            target,
                            " adresine şifre sıfırlama bağlantısı gönderdik.",
                        ),
                        tone = HeroTone.ATTENTION,
                    )
                    if (noMailApp) AlertBox(NO_EMAIL_APP_MESSAGE, isError = true)
                    StepsCard(RESET_STEPS)
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FlowButton("E-posta uygulamasını aç", icon = Icons.Outlined.Mail, onClick = {
                            noMailApp = !openEmailApp(context)
                        })
                        FlowButton(
                            "Bağlantı yerine kodu gir",
                            icon = Icons.Outlined.Key,
                            outlined = true,
                            onClick = onNavigateToReset,
                        )
                    }
                    HintText("E-posta gelmediyse gereksiz (spam) klasörüne de bak.")
                    TextButton(onClick = { sentTo = null; noMailApp = false }) {
                        Text("Farklı bir adres dene ya da yeniden gönder", fontWeight = FontWeight.Bold)
                    }
                } else {
                    StatusHero(Icons.Outlined.Key, HeroTone.ATTENTION)
                    FlowHeader(
                        eyebrow = null,
                        title = "Şifreni mi unuttun?",
                        body = AnnotatedString(
                            "Hesabına bağlı e-posta adresini yaz; yeni şifre belirlemen için sana bir bağlantı gönderelim.",
                        ),
                        tone = HeroTone.ATTENTION,
                    )
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "E-posta adresi",
                        keyboardType = KeyboardType.Email,
                        enabled = !form.loading,
                    )
                    form.error?.let { AlertBox(it, isError = true) }
                    FlowButton(
                        "Sıfırlama bağlantısı gönder",
                        loading = form.loading,
                        onClick = { onSubmit(email) { sentTo = email.trim() } },
                    )
                    TextButton(onClick = onNavigateToReset) {
                        Text("Bağlantım ya da kodum zaten var", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SifremiUnuttumScreenPreview() {
    KitapplaTheme {
        Surface {
            SifremiUnuttumScreen(
                form = AuthFormState(),
                onSubmit = { _, _ -> },
                onNavigateToReset = {},
                onBack = {},
            )
        }
    }
}
