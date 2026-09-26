package com.kitappla.app.ui.screens.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitappla.app.ui.theme.AdacayiMurekkep
import com.kitappla.app.ui.theme.AdacayiSoft
import com.kitappla.app.ui.theme.KahveSoluk
import com.kitappla.app.ui.theme.KitapplaTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IletisimScreen(
    onBack: () -> Unit,
    onNavigateToSss: () -> Unit = {},
    onNavigateToKurallar: () -> Unit = {},
    onNavigateToGizlilik: () -> Unit = {},
    onNavigateToSupportChat: () -> Unit = {},
    supportEmail: String = "destek@kitappla.com",
    supportOpening: Boolean = false,
    supportError: String? = null,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İletişim & Destek", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Sorun bildirmek, bir ilanı şikâyet etmek ya da hesabınla ilgili bir talepte bulunmak için bize ulaşabilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = KahveSoluk,
                lineHeight = 22.sp,
            )

            // Direct support email card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("E-posta ile İletişim", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Bu kurulumla ilgili her konuda doğrudan yöneticisine yazabilirsin:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KahveSoluk,
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$supportEmail")
                                putExtra(Intent.EXTRA_SUBJECT, "KitAppLa Destek Talebi")
                            }
                            context.startActivity(Intent.createChooser(intent, "E-posta Gönder"))
                        },
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(supportEmail)
                    }
                }
            }

            // Quick Chat Support card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AdacayiSoft),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, tint = AdacayiMurekkep)
                        Text("Uygulama İçi Destek", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AdacayiMurekkep)
                    }
                    Text(
                        "Yöneticiye uygulama içinden doğrudan mesaj gönderebilirsin. Yanıtları 'Mesajlar' bölümünde görürsün.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdacayiMurekkep,
                        lineHeight = 20.sp,
                    )
                    FilledTonalButton(
                        onClick = onNavigateToSupportChat,
                        enabled = !supportOpening,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        if (supportOpening) {
                            androidx.compose.material3.CircularProgressIndicator(
                                Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Yöneticiye Mesaj Gönder")
                        }
                    }
                    if (supportError != null) {
                        com.kitappla.app.ui.screens.common.AlertBox(supportError, isError = true)
                    }
                }
            }

            // Topics card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Ne İçin Yazmalısın?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    val topics = listOf(
                        "Kurallara aykırı bir ilan gördüysen (bağış, istek ya da takas)",
                        "Öğrenci belgen beklediğinden uzun süredir incelemedeyse",
                        "Hesabın askıya alındıysa ve itiraz etmek istiyorsan",
                        "Şifreni unuttuysan ve sıfırlama kodu ulaşmadıysa",
                        "Hesabının ve verilerinin silinmesini istiyorsan",
                    )
                    topics.forEach { topic ->
                        Text(
                            text = "• $topic",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KahveSoluk,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }

            // Quick Links
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Önce Şuraya Bak", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Öncelik, kota, takas ve buluşma düzeniyle ilgili soruların cevabı büyük ihtimalle burada:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KahveSoluk,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = onNavigateToSss, shape = RoundedCornerShape(8.dp)) {
                            Text("Sık Sorulan Sorular")
                        }
                        OutlinedButton(onClick = onNavigateToKurallar, shape = RoundedCornerShape(8.dp)) {
                            Text("Topluluk Kuralları")
                        }
                        OutlinedButton(onClick = onNavigateToGizlilik, shape = RoundedCornerShape(8.dp)) {
                            Text("Gizlilik")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IletisimScreenPreview() {
    KitapplaTheme {
        Surface {
            IletisimScreen(onBack = {})
        }
    }
}
