package com.kitappla.app.ui.screens.auth

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitappla.app.ui.screens.common.AlertBox
import com.kitappla.app.ui.screens.common.FlowHeader
import com.kitappla.app.ui.screens.common.HeroTone
import com.kitappla.app.ui.screens.common.StatusHero
import com.kitappla.app.ui.theme.KitapplaTheme

const val MIN_PASSWORD_LENGTH = 6

/**
 * Yeni şifre belirleme. E-postadaki bağlantıdan (`kitappla://sifre-sifirla?token=…`) gelindiyse jeton hazırdır ve
 * alanı gösterilmez; elle açıldıysa kullanıcı bağlantının tamamını ya da yalnızca kodu yapıştırabilir. Başarıda
 * "şifren güncellendi" hâline geçer: sunucu tüm oturumları kapattığı için oradan girişe gidilir.
 *
 * @param initialToken bağlantıdan gelen jeton (yoksa `null`)
 * @param onDone "Giriş yap" (başarıdan sonra)
 * @param onRequestNewLink bağlantı geçersizse yenisini istemek için Şifremi Unuttum'a
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SifreSifirlaScreen(
    form: AuthFormState,
    initialToken: String?,
    onSubmit: (token: String, newPass: String, confirmPass: String, onSuccess: () -> Unit) -> Unit,
    onDone: () -> Unit,
    onRequestNewLink: () -> Unit,
    onBack: () -> Unit,
) {
    val fromLink = !initialToken.isNullOrBlank()
    var token by rememberSaveable { mutableStateOf(initialToken.orEmpty()) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var done by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Şifre", fontWeight = FontWeight.Bold) },
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
                if (done) {
                    StatusHero(Icons.Outlined.LockOpen, HeroTone.SUCCESS)
                    FlowHeader(
                        eyebrow = "Tamamdır",
                        title = "Şifren güncellendi",
                        body = AnnotatedString(
                            "Güvenliğin için tüm cihazlardaki oturumların kapatıldı. Yeni şifrenle giriş yapabilirsin.",
                        ),
                        tone = HeroTone.SUCCESS,
                    )
                    FlowButton("Giriş yap", onClick = onDone)
                } else {
                    StatusHero(Icons.Outlined.LockReset, HeroTone.ATTENTION)
                    FlowHeader(
                        eyebrow = "Şifre sıfırlama",
                        title = "Yeni şifreni belirle",
                        body = AnnotatedString(
                            if (fromLink) "E-postandaki bağlantıyla geldin. Hesabın için yeni bir şifre seç."
                            else "E-postana gelen bağlantıyı ya da kodu yapıştır, ardından yeni şifreni seç.",
                        ),
                        tone = HeroTone.ATTENTION,
                    )

                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (!fromLink) {
                            AuthTextField(
                                value = token,
                                onValueChange = { token = it },
                                label = "Bağlantı veya sıfırlama kodu",
                                enabled = !form.loading,
                            )
                        }
                        AuthTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = "Yeni şifre",
                            isPassword = true,
                            keyboardType = KeyboardType.Password,
                            enabled = !form.loading,
                        )
                        AuthTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Yeni şifre (tekrar)",
                            isPassword = true,
                            keyboardType = KeyboardType.Password,
                            enabled = !form.loading,
                        )
                        PasswordRules(newPassword, confirmPassword)
                    }

                    form.error?.let { AlertBox(it, isError = true) }

                    FlowButton(
                        "Şifreyi güncelle",
                        loading = form.loading,
                        onClick = { onSubmit(token, newPassword, confirmPassword) { done = true } },
                    )

                    TextButton(onClick = onRequestNewLink) {
                        Text(
                            if (fromLink) "Bağlantı çalışmıyor mu? Yenisini iste" else "Yeni sıfırlama bağlantısı iste",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** Kullanıcı yazarken karşılanan kurallar: en az [MIN_PASSWORD_LENGTH] karakter ve iki alanın eşleşmesi. */
@Composable
private fun PasswordRules(password: String, confirm: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PasswordRule("En az $MIN_PASSWORD_LENGTH karakter", password.length >= MIN_PASSWORD_LENGTH)
        PasswordRule("Şifreler eşleşiyor", confirm.isNotEmpty() && password == confirm)
    }
}

@Composable
private fun PasswordRule(text: String, met: Boolean) {
    val color = if (met) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (met) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = if (met) "Karşılandı" else "Karşılanmadı",
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SifreSifirlaFromLinkPreview() {
    KitapplaTheme {
        Surface {
            SifreSifirlaScreen(
                form = AuthFormState(),
                initialToken = "abc",
                onSubmit = { _, _, _, _ -> },
                onDone = {},
                onRequestNewLink = {},
                onBack = {},
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SifreSifirlaManualPreview() {
    KitapplaTheme {
        Surface {
            SifreSifirlaScreen(
                form = AuthFormState(error = "Bağlantı geçersiz ya da süresi dolmuş. Yeniden sıfırlama isteyebilirsin."),
                initialToken = null,
                onSubmit = { _, _, _, _ -> },
                onDone = {},
                onRequestNewLink = {},
                onBack = {},
            )
        }
    }
}
