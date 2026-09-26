package com.kitappla.app.ui.screens.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kitappla.app.ui.theme.eyebrow

/** Durum görselinin tonu: başarı adaçayı, dikkat/eylem vurgu rengidir (temanın açık/koyu karşılıklarıyla). */
enum class HeroTone { SUCCESS, ATTENTION }

/**
 * E-posta akışı ekranlarının (doğrulama, şifre sıfırlama, öğrenci onayı) üst görseli: yumuşak zeminli halka içinde
 * ikon. Başarı tonu ilk görünüşte hafifçe büyüyerek gelir; dikkat tonu sabittir.
 */
@Composable
fun StatusHero(icon: ImageVector, tone: HeroTone, modifier: Modifier = Modifier) {
    val (soft, ink) = when (tone) {
        HeroTone.SUCCESS -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HeroTone.ATTENTION -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }
    val scale = remember(tone) { Animatable(if (tone == HeroTone.SUCCESS) 0.6f else 1f) }
    LaunchedEffect(tone) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Box(
        modifier = modifier
            .size(112.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clip(CircleShape)
            .background(soft.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(soft)
                .border(1.dp, ink.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(40.dp))
        }
    }
}

/** Ortalanmış üst başlık: küçük büyük harfli üst yazı (eyebrow), başlık ve açıklama. */
@Composable
fun FlowHeader(
    eyebrow: String?,
    title: String,
    body: AnnotatedString,
    tone: HeroTone,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (eyebrow != null) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.eyebrow,
                color = if (tone == HeroTone.SUCCESS) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Metnin içindeki [bold] parçasını (ör. e-posta adresi) kalın ve ana metin renginde gösterir. */
@Composable
fun textWithBold(before: String, bold: String, after: String): AnnotatedString {
    val ink = MaterialTheme.colorScheme.onSurface
    return buildAnnotatedString {
        append(before)
        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = ink)) { append(bold) }
        append(after)
    }
}

/** Numaralı adımlar kartı ("1 Gelen kutunu aç → 2 Bağlantıya dokun → 3 Uygulamaya dön"). */
@Composable
fun StepsCard(steps: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            steps.forEachIndexed { index, (title, detail) ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(2.dp))
                        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** Küçük, soluk bilgi satırı (ör. "Gelmezse gereksiz klasörüne bak"). */
@Composable
fun HintText(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Cihazdaki e-posta uygulamasının gelen kutusunu açar. Uygun uygulama yoksa `false` döner; çağıran kullanıcıyı
 * bilgilendirir. (Örtük Intent başlatmak Android 11 paket görünürlüğü kısıtına takılmaz.)
 */
fun openEmailApp(context: Context): Boolean = try {
    val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    true
} catch (e: ActivityNotFoundException) {
    false
} catch (e: SecurityException) {
    false
}

const val NO_EMAIL_APP_MESSAGE = "Bu cihazda bir e-posta uygulaması bulunamadı. Gelen kutunu tarayıcıdan kontrol edebilirsin."
