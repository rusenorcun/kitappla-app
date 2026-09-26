package com.kitappla.app.ui.screens.common

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/** Kartlardaki işlem düğmeleri için dar iç boşluk (M3 varsayılanı yatayda 24dp; yan yana 3-4 düğmeye yer kalmıyordu). */
val CompactButtonPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)

@LayoutScopeMarker
@Immutable
interface ActionButtonsScope {
    /** Düğmenin satırdaki payı: satır, düğmeler en az etiketleri kadar geniş kalmak koşuluyla bu oranda paylaşılır. */
    @Stable
    fun Modifier.weight(weight: Float): Modifier
}

/**
 * Kart altındaki işlem düğmeleri. Her düğme en az etiketinin tamamı kadar geniş olur; hepsi bir satıra sığmazsa
 * sığmayanlar alt satıra geçer. Böylece dar ekranda ya da büyük yazı boyutunda etiketler bölünmez ("Buluşm/a",
 * "Bağışa/Taşı") ve kısalmaz. Bir satırdaki düğmeler satırı [ActionButtonsScope.weight] oranında paylaşır
 * (ağırlıksızlar etiketleri kadar yer tutar). Etiketler için [ButtonLabel].
 */
@Composable
fun ActionButtonsRow(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable ActionButtonsScope.() -> Unit,
) {
    Layout(
        content = { ActionButtonsScopeInstance.content() },
        modifier = modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val maxWidth = constraints.maxWidth
        val natural = measurables.map { it.maxIntrinsicWidth(Constraints.Infinity).coerceAtMost(maxWidth) }
        val weights = measurables.map { (it.parentData as? ActionWeight)?.weight ?: 0f }
        val lines = packLines(natural, gap, maxWidth)

        val placed = lines.map { line ->
            val widths = shareLine(line.map { natural[it] }, line.map { weights[it] }, maxWidth - gap * (line.size - 1))
            line.mapIndexed { i, index ->
                measurables[index].measure(
                    Constraints(minWidth = widths[i], maxWidth = widths[i], maxHeight = constraints.maxHeight),
                )
            }
        }
        val lineHeights = placed.map { row -> row.maxOf { it.height } }
        val height = lineHeights.sum() + gap * (placed.size - 1).coerceAtLeast(0)

        layout(maxWidth, height.coerceIn(constraints.minHeight, constraints.maxHeight)) {
            var y = 0
            placed.forEachIndexed { lineIndex, row ->
                var x = 0
                row.forEach { p ->
                    p.placeRelative(x, y + (lineHeights[lineIndex] - p.height) / 2)
                    x += p.width + gap
                }
                y += lineHeights[lineIndex] + gap
            }
        }
    }
}

/** Öğeleri sırayla, doğal genişlikleri sığdıkça aynı satıra dizer; satırları öğe konumu listeleri olarak döndürür. */
internal fun packLines(natural: List<Int>, gap: Int, maxWidth: Int): List<List<Int>> {
    val lines = mutableListOf<MutableList<Int>>()
    var used = 0
    natural.forEachIndexed { index, width ->
        val current = lines.lastOrNull()
        if (current != null && used + gap + width <= maxWidth) {
            current += index
            used += gap + width
        } else {
            lines += mutableListOf(index)
            used = width
        }
    }
    return lines
}

/**
 * Bir satırın genişliğini ağırlıklara göre paylaştırır; payı doğal genişliğinin altında kalan öğe doğal genişliğinde
 * sabitlenir, kalan yer diğerleri arasında yeniden paylaşılır. Ağırlıksız öğeler doğal genişliğinde kalır.
 */
internal fun shareLine(natural: List<Int>, weights: List<Float>, width: Int): List<Int> {
    val fixed = BooleanArray(natural.size) { weights[it] <= 0f }
    while (true) {
        val available = width - natural.indices.filter { fixed[it] }.sumOf { natural[it] }
        val totalWeight = natural.indices.filterNot { fixed[it] }.sumOf { weights[it].toDouble() }
        if (totalWeight <= 0.0) return natural
        val tooNarrow = natural.indices.filter { !fixed[it] && available * weights[it] / totalWeight < natural[it] }
        if (tooNarrow.isEmpty()) {
            return natural.indices.map { if (fixed[it]) natural[it] else (available * weights[it] / totalWeight).toInt() }
        }
        tooNarrow.forEach { fixed[it] = true }
    }
}

private object ActionButtonsScopeInstance : ActionButtonsScope {
    override fun Modifier.weight(weight: Float): Modifier = this.then(ActionWeight(weight))
}

private class ActionWeight(val weight: Float) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = this@ActionWeight
    override fun equals(other: Any?) = other is ActionWeight && other.weight == weight
    override fun hashCode() = weight.hashCode()
}

/** Düğme etiketi: her zaman tek satır; [ActionButtonsRow] düğmeye etiketin tamamı kadar yer ayırır. */
@Composable
fun ButtonLabel(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
    )
}
