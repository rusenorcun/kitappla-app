package com.kitap.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.kitap.app.data.dto.MeetingDto
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.ui.theme.Adacayi
import com.kitap.app.ui.theme.AdacayiMurekkep
import com.kitap.app.ui.theme.AdacayiSoft
import com.kitap.app.ui.theme.EspressoAyrac
import com.kitap.app.ui.theme.EspressoMetin
import com.kitap.app.ui.theme.EspressoYuzey
import com.kitap.app.ui.theme.Kahve
import com.kitap.app.ui.theme.KahveSoluk
import com.kitap.app.ui.theme.KapakMetin
import com.kitap.app.ui.theme.KitapTheme
import com.kitap.app.ui.theme.KremCizgi
import com.kitap.app.ui.theme.OgrenciOnayVurgu
import com.kitap.app.ui.theme.SolukCizgi
import com.kitap.app.ui.theme.Vurgu
import com.kitap.app.ui.theme.VurguSoft
import com.kitap.app.ui.theme.getBookCoverColor

/**
 * Bölüm 2: Rozet (Badge)
 * Yarıçap 8px, 11px/700, dolgu 5x10.
 * - badge-accent: yazı --accent, zemin --accent-soft
 * - badge-sage: yazı --sage-ink, zemin --sage-soft
 * - badge-muted: yazı --muted, zemin --bg, kenarlık --line
 */
@Composable
fun StatusBadge(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(8.dp))
                else Modifier
            )
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(13.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            fontSize = 11.sp,
        )
    }
}

@Composable
fun PriorityBadge(text: String = "Öğrenci Önceliği", modifier: Modifier = Modifier) {
    StatusBadge(
        text = text,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        icon = Icons.Outlined.Schedule,
        modifier = modifier,
    )
}

@Composable
fun SuccessBadge(text: String, modifier: Modifier = Modifier) {
    StatusBadge(
        text = text,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        icon = Icons.Outlined.CheckCircle,
        modifier = modifier,
    )
}

@Composable
fun MutedBadge(text: String, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    StatusBadge(
        text = text,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        icon = icon,
        modifier = modifier,
    )
}

/**
 * Bölüm 2: Avatar — 38px daire, zemin --sage, yazı koyu (#3E2723 / Kahve, Bölüm 5 Kontrast Notu), 800 ağırlık, 13px.
 * Listelerde 34px'e düşer.
 */
@Composable
fun UserAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Int = 38,
    backgroundColor: Color = Adacayi,
    textColor: Color = Kahve,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, KremCizgi.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials.take(2).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            fontSize = if (size <= 34) 11.sp else 13.sp,
        )
    }
}

/**
 * Bölüm 3: Kitap kapağı yer tutucusu & kapak görseli
 * Kapak görseli yoksa başlıktan türetilen sabit bir renk kullanılır (Book.getCoverColor()).
 * Kapak kutusu: yarıçap 10px, iç gölge/sırt efekti (inset -7px 0 0 rgba(0,0,0,.16)),
 * yazı #F3E7D6, başlık 14px/800, yazar 10px ve %85 saydamlık.
 */
@Composable
fun BookCover(
    title: String,
    author: String? = null,
    coverUrl: String? = null,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
) {
    if (!coverUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = coverUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            loading = {
                BookCoverPlaceholder(title = title, author = author, shape = shape, modifier = Modifier.fillMaxSize())
            },
            error = {
                BookCoverPlaceholder(title = title, author = author, shape = shape, modifier = Modifier.fillMaxSize())
            },
        )
    } else {
        BookCoverPlaceholder(title = title, author = author, shape = shape, modifier = modifier)
    }
}

@Composable
fun BookCoverPlaceholder(
    title: String,
    author: String? = null,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
) {
    val bgColor = remember(title) { getBookCoverColor(title) }
    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .drawWithContent {
                drawContent()
                // Sırt gölgesi efekti
                drawRect(
                    color = Color.Black.copy(alpha = 0.16f),
                    size = Size(width = 7.dp.toPx(), height = size.height),
                )
            }
            .padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 16.sp,
                ),
                color = KapakMetin,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (!author.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = author,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = KapakMetin.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Bölüm 2: Uyarı kutuları — Yarıçap 12px, 14px/600.
 * Hata: zemin --accent-soft, yazı --accent-strong / onPrimaryContainer.
 * Başarı: zemin --sage-soft, yazı --sage-ink.
 */
@Composable
fun AlertBox(
    message: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isError) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isError) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            fontSize = 14.sp,
        )
    }
}

/**
 * Bölüm 2: Espresso paneller — Giriş/kayıt ve öğrenci öncelik kartı
 * #3E2723 zemin, #F3EAD3 yazı, #C6B6AC açıklama metni, #5a4741 ayraç, #E9A38C öğrenci ikonu.
 */
@Composable
fun StudentPriorityCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EspressoYuzey),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = OgrenciOnayVurgu,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Onaylanınca",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OgrenciOnayVurgu,
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "48 saat bağış önceliği",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = EspressoMetin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(EspressoAyrac),
            )
            Text(
                text = "Haftada 3, ayda 10 kitap hakkı",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = EspressoMetin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

/**
 * Bölüm 2: Bölüm ikonları — 34px kare, yarıçap 10px, zemin --accent-soft, ikon --accent.
 */
@Composable
fun SectionIconBox(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun EmptyStateView(
    title: String,
    message: String,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KahveSoluk,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = KahveSoluk,
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(52.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Tekrar Deneyin")
        }
    }
}

@Composable
fun MeetingCard(
    meeting: MeetingDto,
    onArrangeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Buluşma Bilgisi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (onArrangeClick != null) {
                    TextButton(onClick = onArrangeClick) {
                        Text("Düzenle", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (meeting.point != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = KahveSoluk, modifier = Modifier.size(15.dp))
                    Text(
                        text = meeting.point.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (!meeting.at.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = KahveSoluk, modifier = Modifier.size(15.dp))
                    Text(
                        text = meeting.at,
                        style = MaterialTheme.typography.bodySmall,
                        color = KahveSoluk,
                    )
                }
            }

            if (!meeting.note.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Not: ${meeting.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KahveSoluk,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrangeMeetingDialog(
    points: List<PickupPointDto>,
    initialPointId: Long? = null,
    initialNote: String = "",
    initialAt: String = "",
    onDismiss: () -> Unit,
    onConfirm: (pointId: Long?, note: String?, at: String) -> Unit,
) {
    var selectedPointId by remember { mutableStateOf(initialPointId ?: points.firstOrNull()?.id) }
    var note by remember { mutableStateOf(initialNote) }
    var at by remember { mutableStateOf(initialAt) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buluşma Ayarla", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (points.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = points.find { it.id == selectedPointId }?.name ?: "Nokta Seçin",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Teslim Noktası") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                        ) {
                            points.forEach { point ->
                                DropdownMenuItem(
                                    text = { Text(point.name) },
                                    onClick = {
                                        selectedPointId = point.id
                                        dropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = at,
                    onValueChange = { at = it },
                    label = { Text("Tarih ve Saat (Örn: 2026-10-15T14:30:00Z)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Buluşma Notu (İsteğe bağlı)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (at.isNotBlank()) onConfirm(selectedPointId, note.ifBlank { null }, at) },
                enabled = at.isNotBlank(),
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(
    onDismiss: () -> Unit,
    onConfirm: (reason: String, note: String?) -> Unit,
) {
    val reasons = listOf(
        "YANILTICI" to "Yanıltıcı / Gerçeğe aykırı bilgi",
        "UYGUNSUZ_ICERIK" to "Uygunsuz / Rahatsız edici içerik",
        "GELMEDI" to "Buluşmaya gelmedi / İletişim koptu",
        "HAKARET" to "Hakaret / Saygısız tutum",
        "DIGER" to "Diğer kural ihlali",
    )
    var selectedReason by remember { mutableStateOf(reasons.first().first) }
    var note by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Şikâyet Bildir", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = reasons.find { it.first == selectedReason }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gerekçe") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                    ) {
                        reasons.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedReason = code
                                    dropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Açıklama (Detay verin)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedReason, note.ifBlank { null }) },
            ) {
                Text("Gönder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        },
    )
}

@Composable
fun ThankDialog(
    onDismiss: () -> Unit,
    onConfirm: (message: String?) -> Unit,
) {
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Teşekkür Notu Gönder", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Kitabı aldığınız için bağışçıya teşekkür iletmek ister misiniz?",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Teşekkür mesajınız (İsteğe bağlı)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(message.ifBlank { null }) }) {
                Text("Tamamla")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun CommonWidgetsPreview() {
    KitapTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriorityBadge()
                    SuccessBadge("Teslim Edildi")
                    MutedBadge("Roman")
                    UserAvatar("AY")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BookCover(
                        title = "Suç ve Ceza",
                        author = "Fyodor Dostoyevski",
                        modifier = Modifier.size(width = 80.dp, height = 114.dp),
                    )
                    BookCover(
                        title = "Kürk Mantolu Madonna",
                        author = "Sabahattin Ali",
                        modifier = Modifier.size(width = 80.dp, height = 114.dp),
                    )
                }
                AlertBox(message = "Kitap başarıyla kaydedildi.", isError = false)
                AlertBox(message = "Bir hata oluştu. Lütfen tekrar deneyin.", isError = true)
                StudentPriorityCard()
                MeetingCard(
                    MeetingDto(
                        point = PickupPointDto(1L, "Kadıköy Merkez Kütüphanesi"),
                        at = "15 Ekim 2026, 14:00",
                        note = "Giriş kapısı önünde bekleyeceğim.",
                    ),
                    onArrangeClick = {},
                )
                EmptyStateView(
                    title = "Henüz İçerik Yok",
                    message = "Burada görüntülenecek herhangi bir kayıt bulunamadı.",
                    actionText = "Yeni Ekle",
                    onAction = {},
                )
            }
        }
    }
}

