package com.kitap.app.ui.screens.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitap.app.ui.theme.KahveSoluk
import com.kitap.app.ui.theme.KitapTheme
import com.kitap.app.ui.theme.Vurgu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KurallarScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Topluluk Kuralları", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Sınırlı sayıda kitap var; kurallar bu kitapların önce en çok ihtiyacı olana ulaşması için belirlenmiştir.",
                style = MaterialTheme.typography.bodyMedium,
                color = KahveSoluk,
                lineHeight = 22.sp,
            )

            RuleCard(
                icon = Icons.Outlined.LockClock,
                title = "Öğrenci Önceliği",
                description = "Her yeni bağış ilk 48 saat yalnızca belgesi onaylanmış öğrencilere açıktır. Süre dolduğunda tüm üyelere açılır.",
            )

            RuleCard(
                icon = Icons.Outlined.PieChart,
                title = "Alma Kotası",
                description = "Kimse elindekinden fazlasını almasın diye alma hakkı sınırlıdır:\n• Öğrenci: 7 günde 3 · 30 günde 10 kitap\n• Üye: 7 günde 1 · 30 günde 3 kitap\nBağış yapmanın ve takasın sınırı yoktur.",
            )

            RuleCard(
                icon = Icons.Outlined.School,
                title = "Öğrenci Doğrulaması",
                description = "Okulunuzun verdiği .edu.tr uzantılı e-posta adresini girmeniz yeterlidir. Bir okul adresi yalnızca bir hesaba bağlanabilir. Sahte adres kullanımı hesabın askıya alınmasıyla sonuçlanır.",
            )

            RuleCard(
                icon = Icons.Outlined.LocationOn,
                title = "Teslim: Kampüste Yüz Yüze",
                description = "Kitaplar kampüs içinde elden teslim edilir; kargo yoktur ve ev adresi paylaşılmaz. Eşleştikten sonra taraflar belirlenen noktalarda buluşur.",
            )

            RuleCard(
                icon = Icons.Outlined.SwapHoriz,
                title = "Takas",
                description = "Kitabınızı takasa açar, başkasının kitabına teklif verirsiniz. Karşılıklı kabul edilince kampüste buluşup değiştirirsiniz. Takas, bağış kotasından bağımsızdır.",
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Cancel, contentDescription = null, tint = Vurgu)
                        Text("Yapılmaması Gerekenler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Vurgu)
                    }
                    Text(
                        "• Kitapları satmak veya ticari amaçla toplamak\n• Sahte öğrenci e-postası veya belge kullanmak\n• Buluşmaya gelmemek veya teslim etmeden işlemi tamamlamak\n• Reklam, hakaret veya kişisel bilgi paylaşmak\n• Kota sınırını aşmak için birden fazla hesap açmak",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                    )
                }
            }

            RuleCard(
                icon = Icons.Outlined.Flag,
                title = "Şikâyet ve Moderasyon",
                description = "Kural dışı bir ilan veya davranış gördüğünüzde Şikâyet et bağlantısını kullanın. Moderatörler inceleyip size bildirim gönderir.",
            )
        }
    }
}

@Composable
private fun RuleCard(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium, color = KahveSoluk, lineHeight = 20.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KurallarScreenPreview() {
    KitapTheme {
        Surface {
            KurallarScreen(onBack = {})
        }
    }
}
