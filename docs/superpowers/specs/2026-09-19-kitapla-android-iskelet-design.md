# KİTAPLA Android — Proje İskeleti Tasarımı

Tarih: 2026-09-19 · Durum: kullanıcı onayına sunuldu

## 1. Amaç ve kapsam

kitappla.com (kitap bağış ve takas platformu) sitesinin mobil görünümünü **native Android (Kotlin + Jetpack Compose)** uygulaması olarak yazmak. Bu belge yalnızca **iskeleti** kapsar; her sayfanın gerçek içeriği sonraki turlarda kendi mini tasarımıyla doldurulur.

**İskeletin teslim ettikleri**

1. Derlenen, çalışan Gradle yapısı (bağımlılıklar, izinler, ortam ayarları, `gradlew`).
2. Web paletiyle açık/koyu Material3 teması.
3. Ağ katmanı: kalıcı çerez, CSRF yönetimi, ortak hata modeli.
4. Navigasyon: web'deki **tüm** sayfalar rota olarak tanımlı, yer tutucu ekranla (yönetim dahil).
5. İki gerçek dikey dilim: **Giriş/Kayıt** (gerçek API) ve **Keşfet** listesi (`GET /donations`).
6. **Yönetici Kapısı** (sallama + titreşim) ve ayrı yönetim navigasyon grafiği.
7. **Geri tuşu güvencesi** (bkz. §7).

**Kapsam dışı (sonraki turlar):** sayfa içerikleri, bağış/takas/mesajlaşma akışları, push bildirim, çevrimdışı önbellek, yönetim için eksik backend endpoint'leri.

## 2. Backend gerçekleri (kodda doğrulandı)

Backend: `C:\Project\kitap\kitap\kitappla` — Spring Boot 3.3, Thymeleaf, Postgres. Mobil için `/api/v1/**` katmanı hazır.

- **Auth: oturum çerezi (`KITAPLA_SESSION`) + CSRF.** JWT yok. Oturum ömrü 14 gün. Backend'e dokunulmaz.
- **`POST /auth/login` ve `/auth/register` CSRF ister.** Akış: `GET /auth/csrf` → token al → `POST /auth/login` → sunucu token'ı yeniler → tekrar `GET /auth/csrf`.
- **Sayfalama:** `page` verilmezse API **tüm listeyi** döndürür. İstemci her zaman `page` (0 tabanlı) ve `size` (1–100) gönderir. Toplam kayıt `X-Total-Count` başlığında.
- **Rol:** giriş/`GET /me` yanıtındaki `user.admin` (boolean).
- **Herkese açık GET'ler:** `/features`, `/pickup-points`, `/donations[/**]`, `/requests/open`, `/swap/discover`, `/books/**`, `/auth/**`. Kalan her şey giriş ister; girişsiz istek `401` döner.
- **Admin API'si** (`/api/v1/admin/**`, `ROLE_ADMIN`): stats, pending-docs (+approve/reject), users (arama, block), reports (liste, resolve). Webdeki şu işler API'de **yok**: belge görseli, yetki verme/alma, üye silme, teslim noktası ekleme/düzenleme, şikâyet yazışması, içerik kaldırma.
- Web'de yönetim ayrı alan adında olabilir (`kitapla.admin-url`); `/api/v1/**` her zaman site alan adından çağrılır.

## 3. Mimari

Tek modül (`:app`), özellik (feature) bazlı paketleme. Kotlin 1.9.22 / AGP 8.2.2 / Compose compiler 1.5.8 korunur; eklenen kütüphaneler bunlarla uyumlu sürümlerde seçilir ve version catalog'da uygulama planında sabitlenir.

**Kütüphaneler:** Navigation Compose, Hilt (KSP), Retrofit + OkHttp, kotlinx.serialization, Coil, `security-crypto` (şifreli çerez deposu).

```
com.kitap.app
├─ KitapApp.kt                 @HiltAndroidApp
├─ MainActivity.kt             tek Activity, setContent → AppRoot
├─ core/
│  ├─ net/     ApiClient, PersistentCookieJar, CsrfInterceptor, ApiError
│  ├─ session/ SessionManager (SessionState akışı)
│  ├─ device/  ShakeDetector, Haptics
│  └─ nav/     ExitGuard (geri tuşu)
├─ data/
│  ├─ api/     Retrofit servisleri (auth, donations, requests, swap, …, admin)
│  ├─ dto/     API modelleri
│  └─ repo/    AuthRepository, DonationRepository, …
└─ ui/
   ├─ theme/   renk, tipografi, şekil
   ├─ nav/     Routes, AppRoot, MemberGraph, AdminGraph, alt çubuk
   └─ screens/ kesfet, istekler, takas, mesajlar, panom, bildirimler,
               profil, bagis, sikayet, statik, auth, admin
```

**Oturum durumu:** `SessionState = Loading | Guest | Member(user) | Admin(user)`. Uygulama açılışında çerez varsa `GET /me` ile çözülür; `AppRoot` duruma göre grafiği seçer. `401` alan her istek `Guest`'e düşürür ve çerezleri siler.

## 4. Ağ katmanı

- **Base URL:** `BuildConfig.API_BASE_URL` — debug: `http://10.0.2.2:8080/`, release: `https://kitappla.com/`. Açık (cleartext) trafik yalnızca debug'ta izinli (`network_security_config` debug kaynak kümesinde).
- **PersistentCookieJar:** çerezleri `EncryptedSharedPreferences`'ta saklar; süreç yeniden başlayınca oturum korunur. Tek çerez deposu vardır.
- **CsrfInterceptor:** `POST/PUT/DELETE`'e `X-CSRF-TOKEN` ekler. Token yoksa önce `GET /auth/csrf` çağırır. `403` CSRF hatasında token'ı bir kez yenileyip isteği tekrarlar. Giriş/kayıt sonrası token zorla yenilenir.
- **ApiError:** sunucu hata gövdesini (`ApiError` DTO) Türkçe kullanıcı mesajına çevirir; ağ hatası ile iş hatası ayrılır.
- **Liste istekleri** her zaman `page`+`size` ile yapılır; `X-Total-Count` okunur.

## 5. Navigasyon ve ekranlar

**Üye grafiği** (Keşfet açılış ekranı, misafire de açık):
- Alt sekmeler: **Keşfet · İstekler · Takas · Mesajlar · Panom**.
- Üst çubuk: bildirim zili (okunmamış sayacı) ve **Bağış yap**.
- **Panom** girişleri: Bağışlarım, Aldıklarım, İsteklerim, Karşıladıklarım, Takaslarım, Takas kitaplarım, Şikâyetlerim, Profil, Öğrenci doğrulama, SSS / Kurallar / Gizlilik / İletişim.
- Diğer rotalar: Kitap/bağış detayı, Bağış yap, İstek oluştur, Takas teklifi (+detay), Sohbet, Bildirimler, Şikâyet et.
- Misafir, girişi gereken bir sekmeye/rotaya gidince Giriş ekranına yönlenir; giriş sonrası hedefe döner.
- **Auth rotaları:** Giriş, Kayıt, Şifremi unuttum, Şifre sıfırla.

**Yönetim grafiği** (yalnızca `Admin` oturumunda): Pano (istatistik) → Belgeler, Üyeler, İçerik, Teslim noktaları, Şikâyetler (+detay); ayrıca Mesajlar (şikâyet destek yazışmaları) ve Çıkış. Bölüm içleri yer tutucu.

**Yığın temizliği:** giriş, çıkış ve rol değişiminde geri yığını temizlenir; giriş sonrası geri tuşu Giriş ekranına, çıkış sonrası geri tuşu yönetim/üye içeriğine **dönmez**.

## 6. Yönetici Kapısı

**Sallama algılama (`ShakeDetector`)**
- `TYPE_ACCELEROMETER`; toplam ivme büyüklüğü (yerçekimi dahil, telefon dururken ≈1 g) ~2,7 g eşiğini ~1,5 sn içinde **3 kez** aşarsa "sallandı" (ardışık iki sıçrama arası en az 150 ms). Ardından 2 sn bekleme süresi.
- Dinleme yalnızca `SessionState == Guest` iken açıktır (uygulamanın her yerinde). Oturum açıkken, ekran arka plandayken (Activity `STOPPED`) ve Yönetici Girişi zaten açıkken dinlenmez.
- Cihazda ivmeölçer yoksa kapı erişilemez kalır; bu kabul edilen bir durumdur.

**Titreşim (`Haptics`)**: sallamada tek ~400 ms titreşim (API 26+ `VibrationEffect`, altında `vibrate(long)`); `VIBRATE` izni.

**Giriş kuralları** (her ikisi de `POST /auth/login`, sonra `user.admin` kontrolü):

| Ekran | Hesap admin | Hesap admin değil |
|---|---|---|
| Normal Giriş | Hemen `POST /auth/logout`, "E-posta ya da şifre hatalı." | Üye olarak girer |
| Yönetici Girişi (koyu espresso tema) | Yönetim grafiği açılır | Hemen `POST /auth/logout`, aynı genel hata |

Genel hata metni hesabın varlığını/rolünü ifşa etmez. **Güvenlik sınırı sunucudur** (`hasRole('ADMIN')`); sallama yalnızca giriş kapısını gizler. Yönetici oturumu üye oturumunun yerini alır (tek çerez deposu). Soğuk açılışta `GET /me` `admin: true` dönerse doğrudan yönetim grafiği açılır.

## 7. Geri tuşu güvencesi

**Kural: tek bir geri tuşu basışı uygulamadan çıkışa neden olamaz.**

- `ExitGuard`, tek bir kök `BackHandler` olarak `AppRoot`'ta bulunur. Geri yığınında geri gidilecek yer kalmadığında (kök hedefte) çalışır:
  1. Birinci basış: "Çıkmak için tekrar geri tuşuna basın" mesajı gösterilir, çıkış **yapılmaz**.
  2. 2 sn içinde ikinci basış: `finish()` ile çıkılır.
  3. 2 sn geçtikten sonraki basış birinci basış sayılır (mesaj tekrar gösterilir).
- Kök olmayan hedeflerde geri normal davranır (yığından çıkar). Üye alt sekmelerinde geri, önce Keşfet'e döner; Keşfet kök hedeftir. Yönetim grafiğinde kök hedef Pano'dur. Giriş ekranı kökteyse (ör. yığın temizlendikten sonra) o da `ExitGuard` kapsamındadır.
- Sistem tahmini geri hareketi (predictive back) ile de aynı davranış: `BackHandler` bunu yakalar; manifestte `enableOnBackInvokedCallback="true"`.
- Yığın temizliği (§5) geri tuşuyla kapı ekranlarına (Giriş, Yönetici Girişi) istemeden dönülmesini de engeller.

## 8. Tema

Web CSS değişkenlerinden türetilir. Açık: zemin `#F3EAD3`, yüzey `#FCF8EE`, mürekkep `#3E2723`, vurgu `#C65D47`, adaçayı `#8FA89B`, çizgi `#E6D8BC`. Koyu: zemin `#211B17`, yüzey `#2A231D`, mürekkep `#ECE3D4`, vurgu `#D6785F`. Sistem temasını izler. Yazı tipi: Plus Jakarta Sans. Yönetici Girişi ekranı kendi koyu espresso görünümünü kullanır.

## 9. Doğrulama / kabul kriterleri

1. `./gradlew assembleDebug` başarılı; uygulama emülatörde açılır (`gradlew` ve wrapper jar eklenmiş olmalı).
2. **Giriş dilimi:** yerel backend'e (`10.0.2.2:8080`) karşı normal üye girişi çalışır; oturum uygulama yeniden başlatıldığında korunur.
3. **Keşfet dilimi:** `GET /donations?page=0&size=24` gerçek liste gösterir; misafir (girişsiz) görür.
4. **Yönetici Kapısı:** emülatörde *Virtual sensors → Move* ile sallama → titreşim → Yönetici Girişi; admin hesabı yönetim grafiğine girer, üye hesabı reddedilir; normal Giriş'te admin hesabı reddedilir.
5. **Geri tuşu:** birim testi (`ExitGuard`, enjekte edilebilir saatle: ilk basış uyarı, pencere içi ikinci basış çıkış, pencere sonrası basış yine uyarı) ve Activity testi (kök hedefte tek `pressBack` sonrası Activity yaşıyor). Elle: her kök hedefte tek geri çıkarmıyor; giriş/çıkış sonrası geri kapı ekranlarına dönmüyor.
6. Tüm rotalar yer tutucu ekranla açılır (çökme yok).

## 10. Varsayımlar ve riskler

- Sallama eşiği (~2,7 g / 3 kez) cihazlarda gerçek denemeyle ayarlanacak; sabitler tek yerde toplanır.
- Yönetim için eksik backend endpoint'leri (§2) her bölüm doldurulurken eklenir; iskelet bunlara bağımlı değildir.
- Sunucu `page` göndermezse tüm listeyi döndürdüğü için, sayfalama unutulursa büyük veri çekilir; bu yüzden liste servisleri `page`'i zorunlu parametre yapar.
