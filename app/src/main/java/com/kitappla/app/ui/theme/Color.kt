package com.kitappla.app.ui.theme

import androidx.compose.ui.graphics.Color

// Açık tema (web: --bg, --surface, --surface2, --line, --ink, --muted, --faint, --accent, --sage ...)
val Krem = Color(0xFFF3EAD3)             // --bg
val KremYuzey = Color(0xFFFCF8EE)        // --surface
val KremYuzey2 = Color(0xFFFBF3E2)       // --surface2
val KremCizgi = Color(0xFFE6D8BC)        // --line
val Kahve = Color(0xFF3E2723)            // --ink
val KahveSoluk = Color(0xFF8C7B6B)       // --muted
val SolukCizgi = Color(0xFFB4A594)       // --faint (dekoratif ok rengi)
val Vurgu = Color(0xFFC65D47)            // --accent
val VurguGuclu = Color(0xFFA84734)       // --accent-strong
val VurguSoft = Color(0xFFF4DCD3)        // --accent-soft
val Adacayi = Color(0xFF8FA89B)          // --sage
val AdacayiSoft = Color(0xFFE1E9E2)      // --sage-soft
val AdacayiMurekkep = Color(0xFF48594F)  // --sage-ink
val BordoKitap = Color(0xFF7A2E2A)       // palet ilk rengi / geriye uyumluluk

// Koyu tema
val KoyuZemin = Color(0xFF211B17)        // --bg
val KoyuYuzey = Color(0xFF2A231D)        // --surface
val KoyuYuzey2 = Color(0xFF2F271F)       // --surface2
val KoyuCizgi = Color(0xFF3A2F27)        // --line
val KoyuMurekkep = Color(0xFFECE3D4)     // --ink
val KoyuSoluk = Color(0xFFA99C88)        // --muted
// Metin alanı çerçevesi / soluk ok: yüzeyde >= 3:1 (WCAG 1.4.11). Çizgi rengi (KoyuCizgi) outlineVariant'ta kalır.
val KoyuKenar = Color(0xFF8A7B6A)        // --faint (~KoyuKenar)
val KoyuVurgu = Color(0xFFD6785F)        // --accent
val KoyuVurguGuclu = Color(0xFFE08C72)   // --accent-strong koyu kontrast dostu karşılığı (Bölüm 5)
val KoyuVurguSoft = Color(0xFF3A2A23)    // --accent-soft
val KoyuAdacayiSoft = Color(0xFF26312B)  // --sage-soft
val KoyuAdacayiMurekkep = Color(0xFFB7C9BD) // --sage-ink

// Espresso Paneller (Giriş sol sütunu, öğrenci öncelik kartı - temadan bağımsız)
val EspressoZemin = Color(0xFF2A1A17)    // Yönetici kapısı zemin
val EspressoYuzey = Color(0xFF3E2723)    // --espresso
val EspressoMetin = Color(0xFFF3EAD3)    // Espresso üzeri metin
val EspressoAciklama = Color(0xFFC6B6AC) // Espresso üzeri açıklama metni
val EspressoAyrac = Color(0xFF5A4741)    // Espresso içi ayraç
val OgrenciOnayVurgu = Color(0xFFE9A38C) // "Onaylanınca" vurgu rengi

// Kitap Kapağı Yer Tutucusu (temadan bağımsız sabit palet ve renkler)
val KapakMetin = Color(0xFFF3E7D6)       // Kapak üzeri başlık ve yazar metni
val KapakPaleti = listOf(
    Color(0xFF7A2E2A),
    Color(0xFF2F4A3C),
    Color(0xFF23405C),
    Color(0xFF7A2E5A),
    Color(0xFF4A5240),
    Color(0xFF8A5A1C),
    Color(0xFF5A3A6B),
    Color(0xFF2E5E5A),
)

/**
 * Web'deki Book.getCoverColor() algoritmasının birebir eşdeğeri.
 * h = 31 * h + c (32 bit taşmalı tam sayı)
 * renk = palet[floorMod(h, 8)]
 */
fun getBookCoverColor(title: String?): Color {
    val key = title.orEmpty()
    var h = 0
    for (i in key.indices) {
        h = 31 * h + key[i].code
    }
    return KapakPaleti[Math.floorMod(h, KapakPaleti.size)]
}
