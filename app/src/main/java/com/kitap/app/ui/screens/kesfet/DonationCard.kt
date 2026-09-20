@file:OptIn(ExperimentalMaterial3Api::class)

package com.kitap.app.ui.screens.kesfet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitap.app.data.dto.BookDto
import com.kitap.app.data.dto.DonationDto
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.data.repo.resolveCoverUrl
import com.kitap.app.ui.screens.common.BookCover
import com.kitap.app.ui.theme.KitapTheme

@Composable
fun DonationCard(donation: DonationDto, coverBaseUrl: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BookCover(
                title = donation.book.title,
                author = donation.book.author,
                coverUrl = resolveCoverUrl(coverBaseUrl, donation.book.coverUrl),
                modifier = Modifier
                    .width(64.dp)
                    .height(92.dp),
                shape = RoundedCornerShape(10.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    donation.book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                donation.book.author?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Spacer(Modifier.height(6.dp))
                donation.donorName?.let { Text("Bağışlayan: $it", style = MaterialTheme.typography.bodySmall) }
                Text(
                    "Kalan: ${donation.remaining} / ${donation.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                donation.point?.let {
                    Text(it.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DonationCardPreview() {
    KitapTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            DonationCard(
                donation = DonationDto(
                    id = 1L,
                    book = BookDto(
                        id = 1L,
                        title = "Suç ve Ceza",
                        author = "Fyodor Dostoyevski",
                        coverUrl = null
                    ),
                    quantity = 3,
                    remaining = 2,
                    donorName = "Ahmet Yılmaz",
                    point = PickupPointDto(id = 1L, name = "Merkez Kütüphane")
                ),
                coverBaseUrl = "https://example.com",
                onClick = {}
            )
        }
    }
}
