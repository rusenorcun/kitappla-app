package com.kitap.app.ui.screens.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Visibility
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
import com.kitap.app.ui.theme.AdacayiMurekkep
import com.kitap.app.ui.theme.AdacayiSoft
import com.kitap.app.ui.theme.KahveSoluk
import com.kitap.app.ui.theme.KitapTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GizlilikScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gizlilik Politikası", fontWeight = FontWeight.Bold) },
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
                text = "Kampüste buluşup kitabı teslim edebilmen için gereken en az bilgiyi tutuyoruz. Ne topladığımız ve kimin gördüğü aşağıda.",
                style = MaterialTheme.typography.bodyMedium,
                color = KahveSoluk,
                lineHeight = 22.sp,
            )

            PrivacySectionCard(
                icon = Icons.Outlined.Lock,
                title = "Ne Topluyoruz",
                items = listOf(
                    "Ad ve e-posta: Hesabın için zorunludur.",
                    "Şifre: Düz metin olarak saklanmaz, yalnızca güçlü BCrypt özeti tutulur.",
                    "Telefon: İsteğe bağlıdır; buluşmayı ayarlarken iletişim kolaylığı içindir. Ev adresi asla istenmez.",
                    "Öğrenci belgesi ve numarası: Yalnızca öğrenci doğrulaması isterseniz işlenir.",
                    "Hareketler: Bağışlar, istekler, takaslar ve bildirimler.",
                    "Çerez / Oturum: Yalnızca kimlik doğrulama belirteci kullanılır. Takip veya reklam çerezi yoktur.",
                ),
            )

            PrivacySectionCard(
                icon = Icons.Outlined.Visibility,
                title = "Kim Ne Görüyor",
                items = listOf(
                    "Adınız: İlanlarda ve listelerde diğer üyelere görünür.",
                    "Buluşma bilgisi: Kararlaştırdığınız kampüs noktası ve saat yalnızca eşleştiğiniz karşı tarafa gösterilir.",
                    "E-posta: Diğer üyelere hiçbir yerde gösterilmez.",
                    "Öğrenci belgeniz: Herkese açık paylaşılmaz; yalnızca yönetici inceleme panelinde görür.",
                    "Mesajlar: Yalnızca yazıştığınız kişiyle aranızdadır. Yönetim sadece şikâyet edilmiş bir sohbeti ve yalnızca şikâyet süresince inceleyebilir.",
                ),
            )

            PrivacySectionCard(
                icon = Icons.Outlined.DeleteOutline,
                title = "Ne Kadar Saklanıyor",
                items = listOf(
                    "Öğrenci belgeniz reddedilirse dosya diskten kalıcı olarak silinir.",
                    "Hesabınızı sildirmek isterseniz yöneticiye başvurabilirsiniz. Geçmiş işlem bütünlüğü için kayıtlar silinmez, hesap dondurulur/askıya alınır.",
                ),
            )

            PrivacySectionCard(
                icon = Icons.Outlined.Public,
                title = "Üçüncü Taraflar",
                items = listOf(
                    "Verileriniz hiçbir üçüncü tarafa satılmaz, kiralanmaz veya aktarılmaz.",
                    "Tek dış istek; bir kitap için link verildiğinde açık web üzerinden kapak ve başlık bilgisini getirmek için yapılır, kişisel bilgi içermez.",
                ),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AdacayiSoft),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = AdacayiMurekkep)
                        Text(
                            "Bu Kurulum Hakkında",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AdacayiMurekkep,
                        )
                    }
                    Text(
                        "KitAppLa açık kaynaklı bir topluluk projesidir. Veriler uygulamanın bağlı olduğu sunucuda güvenle barındırılır. Verilerinizle ilgili her türlü talep için kurulum yöneticisine ulaşabilirsiniz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdacayiMurekkep,
                        lineHeight = 20.sp,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PrivacySectionCard(
    icon: ImageVector,
    title: String,
    items: List<String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            items.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KahveSoluk,
                    lineHeight = 21.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GizlilikScreenPreview() {
    KitapTheme {
        Surface {
            GizlilikScreen(onBack = {})
        }
    }
}
