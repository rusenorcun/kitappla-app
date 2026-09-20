package com.kitap.app.ui.screens.info

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitap.app.ui.theme.KahveSoluk
import com.kitap.app.ui.theme.KitapTheme

data class FaqItem(
    val question: String,
    val answer: String,
    val defaultExpanded: Boolean = false,
)

private val faqList = listOf(
    FaqItem(
        question = "Öğrenci önceliği ne demek?",
        answer = "Her yeni bağış ilk 48 saat yalnızca öğrenci doğrulaması yapılmış üyelere açılır. Süre dolunca tüm üyelere açılır. Böylece sınırlı kaynak önce en çok ihtiyaç sahibine ulaşır.",
        defaultExpanded = true,
    ),
    FaqItem(
        question = "Nasıl öğrenci olurum?",
        answer = "Okulunun verdiği .edu.tr uzantılı e-posta adresini girmen yeterli; belge yüklemene gerek yok. Kayıt olurken bu adresi kullandıysan öğrenci durumun kendiliğinden açılır. Kişisel bir adresle kaydolduysan Hesap ayarları → Öğrenci ol sayfasından okul adresini ekleyebilirsin — tekrar üye olmana gerek yok.",
    ),
    FaqItem(
        question = "Kimler katılabilir?",
        answer = "Herkes üye olabilir; bağış yapabilir, takas edebilir ve kitap alabilir. Öğrenciler okul e-postasını doğrulayarak öncelik ve daha yüksek kota kazanır.",
    ),
    FaqItem(
        question = "Kaç kitap alabilirim? (Kota)",
        answer = "Öğrenci: 7 günde 3, 30 günde 10 kitap.\nÜye: 7 günde 1, 30 günde 3 kitap.\nBağış yapmanın ve takas etmenin sınırı yoktur.",
    ),
    FaqItem(
        question = "Eşleştikten sonra nasıl haberleşiyoruz?",
        answer = "Eşleşme olur olmaz iki taraf arasında uygulama içinde bir sohbet açılır; Mesajlar sayfasından ulaşırsın. Mesajlar anlık düşer. Sohbet yalnızca o eşleşmenin iki tarafına açıktır — başka kimse okuyamaz, üyelere rastgele mesaj atılamaz. Teslim noktasını ve saati burada kararlaştırırsınız.",
    ),
    FaqItem(
        question = "Kitabı nerede teslim alacağım?",
        answer = "Kampüs içindeki teslim noktalarından birinde. Eşleştikten sonra bağışçı bir nokta önerir; ikiniz de yeri ve saati değiştirebilirsiniz. Listede olmayan bir yerde buluşmak isterseniz onu da yazabilirsiniz. Buluşma kaydedildiğinde karşı tarafa bildirim gider.",
    ),
    FaqItem(
        question = "Takas nasıl çalışır?",
        answer = "Elindeki kitabı takasa açarsın, başkasının kitabına kendi kitabınla teklif verirsin. Karşı taraf kabul edince kampüste buluşup kitapları karşılıklı verirsiniz. Takas, bağış kotasından bağımsızdır.",
    ),
    FaqItem(
        question = "Karşı taraf buluşmaya gelmezse ne olur?",
        answer = "Buluşma sayfasından gelmedi olarak işaretleyebilirsin. Bu, karşı tarafın hesabında sayılır ve yönetim tekrar edenleri görür. Kitap yeniden dolaşıma girer, senin kotan boşa harcanmış olmaz.",
    ),
    FaqItem(
        question = "Bir sorunu nasıl bildiririm, moderatöre nasıl ulaşırım?",
        answer = "Her ilanın ve her sohbetin üstünde Şikâyet et bağlantısı var. Gerekçeyi seçip (taciz, uygunsuz içerik, sahte ilan, buluşmaya gelmedi…) kısa bir açıklama yazman yeterli; bildirim doğrudan yönetime düşer. Yöneticiler yalnızca açık şikâyeti olan sohbetleri inceleyebilir. Acil durumlar için İletişim sayfasından yazabilirsin.",
    ),
    FaqItem(
        question = "Kişisel bilgilerim paylaşılıyor mu?",
        answer = "Ev adresin hiç istenmiyor, telefonun isteğe bağlı ve kimseye gösterilmiyor. Karşı taraf yalnızca adını görür. Teslim kampüs içinde yüz yüze yapıldığı için paylaşılan tek şey buluşma noktası ve saati — o da sadece eşleştiğin kişiyle.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SssScreen(
    onBack: () -> Unit,
    onNavigateToContact: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sık Sorulan Sorular", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Aklına takılan tüm soruların cevaplarını burada bulabilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = KahveSoluk,
                lineHeight = 22.sp,
            )

            faqList.forEach { item ->
                FaqAccordionCard(item = item)
            }

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToContact() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Başka bir sorun mu var?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Bize doğrudan İletişim sayfasından ulaşabilirsin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KahveSoluk,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqAccordionCard(item: FaqItem) {
    var expanded by remember { mutableStateOf(item.defaultExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Kapat" else "Aç",
                    tint = KahveSoluk,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = item.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = KahveSoluk,
                        lineHeight = 22.sp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SssScreenPreview() {
    KitapTheme {
        Surface {
            SssScreen(onBack = {})
        }
    }
}
