# KİTAPLA Android İskeleti Uygulama Planı

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** kitappla.com'un mobil deneyimini karşılayan native Android iskeletini kurmak: derlenen Gradle yapısı, tema, çerez+CSRF ağ katmanı, tüm sayfaların rotaları, gerçek Giriş/Kayıt ve Keşfet dilimleri, sallamayla açılan Yönetici Kapısı ve tek geri tuşuyla çıkışı engelleyen güvence.

**Architecture:** Tek modüllü (`:app`) Kotlin + Jetpack Compose uygulaması, özellik bazlı paketleme. Hilt ile bağımlılık enjeksiyonu, Retrofit/OkHttp ile `/api/v1` (oturum çerezi + CSRF), Navigation Compose ile iki ayrı grafik (Üye/Misafir ve Yönetim). Saf mantık (ExitGuard, ShakeLogic, çerez/CSRF) Android'den bağımsız yazılıp JVM testleriyle doğrulanır.

**Tech Stack:** Kotlin 1.9.22, AGP 8.2.2, Gradle 8.9, Compose BOM 2024.02.00 (compiler 1.5.8), Material3, Navigation Compose 2.7.7, Hilt 2.50 + KSP, Retrofit 2.11.0 + OkHttp 4.12.0, kotlinx.serialization 1.6.3, Coil 2.5.0, security-crypto, JUnit4 + MockWebServer + coroutines-test.

**Spec:** `docs/superpowers/specs/2026-09-19-kitapla-android-iskelet-design.md` (yürütücü önce bunu baştan sona okumalı). Backend (yalnızca okunur): `C:\Project\kitap\kitap\kitappla` — API'yi tahmin etme, `src/main/java/app/kitapla/api/v1/*` ve `api/dto/*` kodundan doğrula.

## Global Constraints

Her görevin gereksinimlerine örtük olarak dahildir.

- Paket adı `com.kitap.app`; `minSdk = 24`, `compileSdk = targetSdk = 34`; Kotlin 1.9.22 / AGP 8.2.2 / Compose compiler `1.5.8` **korunur**. Gradle wrapper 8.9 (sistemde yalnızca JDK 21 var; Gradle 8.2 JDK 21'i desteklemez).
- Çalışma dizini: `C:\Project\kitap\app` (git deposu kökü; dal `master`, dalı yeniden adlandırma). Komutlar PowerShell'dedir: `.\gradlew.bat ...`.
- **Backend'e dokunulmaz.** Eksik endpoint gerekirse dur ve kullanıcıya sor.
- Base URL: debug `http://10.0.2.2:8080/`, release `https://kitappla.com/`. Cleartext yalnızca debug'ta.
- **Liste istekleri her zaman `page` (0 tabanlı) ve `size` (1–100) gönderir**; `page` verilmezse sunucu tüm listeyi döndürür. Toplam kayıt `X-Total-Count` başlığında.
- Auth: oturum çerezi + CSRF (`GET /api/v1/auth/csrf` → `{token, headerName, parameterName}`); `POST/PUT/PATCH/DELETE`'e `headerName` başlığı eklenir. Giriş rolü `user.admin`.
- **Geri tuşu:** tek bir geri basışı uygulamadan çıkışa neden olamaz (kök hedefte ilk basış uyarı, 2 sn içinde ikinci basış çıkış). Giriş/çıkış/rol değişiminde geri yığını temizlenir.
- **Yönetici kapısı:** sallama yalnızca `SessionState.Guest` iken dinlenir; eşik toplam ivme ≥ 2,7 g, 1,5 sn içinde 3 sıçrama (aralarında ≥ 150 ms), sonra 2 sn bekleme; tek ~400 ms titreşim. Normal Giriş'te admin hesabı **ve** Yönetici Girişi'nde admin olmayan hesap: hemen `POST /auth/logout`, hata metni `E-posta ya da şifre hatalı.`
- Tema: web paleti (açık `#F3EAD3/#FCF8EE/#3E2723/#C65D47/#8FA89B`, koyu `#211B17/#2A231D/#ECE3D4/#D6785F`), yazı tipi Plus Jakarta Sans. Arayüz metinleri Türkçe ve "siz" hitabıyla.
- Testler `camelCase` adlı olmalı (instrumented testlerde boşluklu ad API < 30'da derlenmez).
- **Commit:** her görevin sonundaki commit adımı yalnızca kullanıcı o oturumda commit izni verdiyse yapılır; izin yoksa değişiklikleri bırak ve raporla. Backend deposuna (`C:\Project\kitap\kitap`) asla commit atma.

## Dosya Haritası

```
.gitignore, local.properties(ignored), gradlew(.bat), gradle/wrapper/*, gradle/libs.versions.toml
app/build.gradle.kts, app/proguard-rules.pro
app/src/main/AndroidManifest.xml, res/values/{strings,themes}.xml, res/font/plus_jakarta_sans.ttf
app/src/debug/AndroidManifest.xml, res/xml/network_security_config.xml
app/src/main/java/com/kitap/app/
  KitapApp.kt, MainActivity.kt
  core/net/    KitapJson, ApiResult, CookieCodec, PersistentCookieJar, EncryptedCookiePersistence, CsrfInterceptor, UnauthorizedInterceptor
  core/session/SessionManager (SessionState)
  core/device/ ShakeLogic (ShakeConfig), ShakeDetector (+ShakeEffect), Haptics
  core/nav/    ExitGuard (+ExitGuardHandler)
  data/dto/    Dtos.kt
  data/api/    AuthApi, DonationApi, NotificationApi
  data/repo/   AuthRepository, DonationRepository (+PagedResult), CoverUrl
  di/          NetworkModule
  ui/theme/    Color, Type, Theme
  ui/nav/      Routes, RootBackGuard, AppRoot, AppViewModel, MemberNavHost, AdminNavHost
  ui/screens/common/  PlaceholderScreen, MenuScreen
  ui/screens/auth/    AuthValidator, AuthViewModel, LoginScreen, RegisterScreen, AdminLoginScreen
  ui/screens/kesfet/  KesfetViewModel, KesfetScreen, DonationCard
app/src/test/java/com/kitap/app/...        (JVM birim testleri)
app/src/androidTest/java/com/kitap/app/... (emülatör testleri)
```

---

### Task 1: Araç zinciri ve temel derleme

**Files:**
- Create: `.gitignore`, `local.properties` (ignore edilir), `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`
- Modify: `gradle/wrapper/gradle-wrapper.properties`, `gradle.properties`

**Interfaces:**
- Produces: `.\gradlew.bat` çalışır; Android platform 34 ve `kitap_api34` emülatörü kurulu; mevcut kod `assembleDebug` ile derlenir.

Makine gerçekleri (kontrol edildi): JDK 21 PATH'te (`C:\Program Files\Java\jdk-21.0.11`), JDK 17 yok; SDK `%LOCALAPPDATA%\Android\Sdk` (platform 35/36/37, build-tools 34.0.0, `cmdline-tools\latest`, lisanslar kabul); platform **34 yok**, system image ve AVD **yok**; `~\.gradle\wrapper\dists\gradle-8.9-bin` önbellekte.

- [ ] **Step 1: `.gitignore` yaz**

`C:\Project\kitap\app\.gitignore`:

```
.gradle/
build/
local.properties
.idea/
*.iml
.kotlin/
captures/
.externalNativeBuild/
.cxx/
*.apk
*.aab
```

- [ ] **Step 2: `local.properties` yaz** (ignore edilir; SDK yolu)

```powershell
"sdk.dir=" + ($env:LOCALAPPDATA -replace '\\','/') + "/Android/Sdk" | Set-Content -Path C:\Project\kitap\app\local.properties -Encoding ascii
Get-Content C:\Project\kitap\app\local.properties
```

Beklenen: `sdk.dir=C:/Users/rusen/AppData/Local/Android/Sdk` (Gradle düz eğik çizgiyi kabul eder).

- [ ] **Step 3: Gradle wrapper üret (8.9)**

Boş bir geçici dizinde üretip kopyala; projeyi yapılandırmaya gerek kalmaz:

```powershell
$gradle = (Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.9-bin" -Recurse -Filter gradle.bat | Select-Object -First 1).FullName
$tmp = Join-Path $env:TEMP "kitap-wrapgen"; Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue; New-Item -ItemType Directory $tmp | Out-Null
Push-Location $tmp; & $gradle wrapper --gradle-version 8.9 --distribution-type bin; Pop-Location
Copy-Item "$tmp\gradlew","$tmp\gradlew.bat" C:\Project\kitap\app\ -Force
Copy-Item "$tmp\gradle\wrapper\gradle-wrapper.jar","$tmp\gradle\wrapper\gradle-wrapper.properties" C:\Project\kitap\app\gradle\wrapper\ -Force
Get-Content C:\Project\kitap\app\gradle\wrapper\gradle-wrapper.properties
```

Beklenen: `distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip`. (Gradle önbelleği bulunamazsa `gradle-8.9-bin.zip`'i indirip aynı komutu onunla çalıştır.)

- [ ] **Step 4: `gradle.properties` güncelle**

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 5: Platform 34, system image ve AVD kur**

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"; $bin = "$sdk\cmdline-tools\latest\bin"
"y" | & "$bin\sdkmanager.bat" "platforms;android-34" "system-images;android-34;google_apis;x86_64"
"no" | & "$bin\avdmanager.bat" create avd -n kitap_api34 -k "system-images;android-34;google_apis;x86_64" -d pixel_6 --force
& "$sdk\emulator\emulator.exe" -list-avds
```

Beklenen: liste `kitap_api34` içerir. (Donanım hızlandırma mevcut: `HypervisorPresent = True`. İndirme başarısız olursa kullanıcıya bildir; fiziksel cihaz da kullanılabilir.)

- [ ] **Step 6: Mevcut kodla temel derleme**

```powershell
Set-Location C:\Project\kitap\app
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug
```

Beklenen: `--version` çıktısında `JVM: 21...`; derleme `BUILD SUCCESSFUL`.

**Bilinen risk:** AGP 8.2.2 + JDK 21 birleşiminde `JdkImageTransform` / `jlink.exe` hatası çıkarsa (`Execution failed for JdkImageTransform ... core-for-system-modules.jar`): JDK 17 kur (`winget install Microsoft.OpenJDK.17`) ve **repo dışında**, kullanıcı düzeyi `%USERPROFILE%\.gradle\gradle.properties` dosyasına `org.gradle.java.home=<JDK17 yolu>` (çift ters eğik çizgiyle) ekle. AGP/Gradle sürümlerini yükseltme; sorun sürerse dur ve kullanıcıya bildir.

- [ ] **Step 7: Commit** (izin varsa)

Depoda hiç commit yok; spec, plan ve mevcut dosyalar dahil başlangıç commit'i:

```powershell
git add .gitignore gradlew gradlew.bat gradle gradle.properties build.gradle.kts settings.gradle.kts app docs
git commit -m "chore: proje iskeleti, Gradle wrapper 8.9, spec ve plan"
```

---

### Task 2: Bağımlılıklar, sürüm kataloğu, Hilt, manifest ve BuildConfig

**Files:**
- Create: `gradle/libs.versions.toml`, `app/proguard-rules.pro`, `app/src/main/res/values/strings.xml`, `app/src/main/res/values/themes.xml`, `app/src/debug/AndroidManifest.xml`, `app/src/debug/res/xml/network_security_config.xml`, `app/src/main/java/com/kitap/app/KitapApp.kt`
- Modify: `build.gradle.kts`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`
- Test: `app/src/test/java/com/kitap/app/BuildConfigTest.kt`

**Interfaces:**
- Produces: `BuildConfig.API_BASE_URL: String` (sonu `/` ile biter); Hilt aktif (`@HiltAndroidApp KitapApp`); tüm kütüphaneler `libs.*` takma adlarıyla erişilebilir.

- [ ] **Step 1: Başarısız testi yaz**

`app/src/test/java/com/kitap/app/BuildConfigTest.kt`:

```kotlin
package com.kitap.app

import org.junit.Assert.assertTrue
import org.junit.Test

class BuildConfigTest {
    @Test
    fun apiBaseUrlIsHttpAndEndsWithSlash() {
        val url = BuildConfig.API_BASE_URL
        assertTrue(url.startsWith("http"))
        assertTrue("Retrofit için taban adres '/' ile bitmeli", url.endsWith("/"))
    }
}
```

- [ ] **Step 2: Testin başarısız olduğunu doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.BuildConfigTest"
```

Beklenen: FAIL (`Unresolved reference: BuildConfig` ve/veya `testImplementation` yok).

- [ ] **Step 3: Sürüm kataloğu**

`gradle/libs.versions.toml`:

```toml
[versions]
agp = "8.2.2"
kotlin = "1.9.22"
ksp = "1.9.22-1.0.17"
hilt = "2.50"
composeBom = "2024.02.00"
coreKtx = "1.12.0"
lifecycle = "2.7.0"
activityCompose = "1.8.2"
navigationCompose = "2.7.7"
hiltNavigationCompose = "1.1.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
serialization = "1.6.3"
coil = "2.5.0"
securityCrypto = "1.1.0-alpha06"
coroutines = "1.7.3"
junit = "4.13.2"
androidxTestExt = "1.1.5"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
androidx-hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
androidx-security-crypto = { module = "androidx.security:security-crypto", version.ref = "securityCrypto" }
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-serialization = { module = "com.squareup.retrofit2:converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-mockwebserver = { module = "com.squareup.okhttp3:mockwebserver", version.ref = "okhttp" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
junit = { module = "junit:junit", version.ref = "junit" }
androidx-test-ext-junit = { module = "androidx.test.ext:junit", version.ref = "androidxTestExt" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

`build.gradle.kts` (kök) — tamamını değiştir:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Step 4: `app/build.gradle.kts` — tamamını değiştir**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kitap.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kitap.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"https://kitappla.com/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.security.crypto)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

- [ ] **Step 5: Manifest, kaynaklar, Application**

`app/proguard-rules.pro`: boş dosya (`New-Item app\proguard-rules.pro -ItemType File`).

`app/src/main/AndroidManifest.xml` — tamamını değiştir:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:name=".KitapApp"
        android:allowBackup="false"
        android:enableOnBackInvokedCallback="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Kitap">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

(`allowBackup=false`: şifreli çerez deposunun Keystore anahtarı yedekten geri yüklenemez.)

`app/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">KİTAPLA</string>
</resources>
```

`app/src/main/res/values/themes.xml`:

```xml
<resources>
    <style name="Theme.Kitap" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

`app/src/debug/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application android:networkSecurityConfig="@xml/network_security_config" />
</manifest>
```

`app/src/debug/res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">10.0.2.2</domain>
        <domain includeSubdomains="false">localhost</domain>
    </domain-config>
</network-security-config>
```

`app/src/main/java/com/kitap/app/KitapApp.kt`:

```kotlin
package com.kitap.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KitapApp : Application()
```

`MainActivity.kt` bu görevde `LoginScreen`'i doğrudan çağırmaya devam eder (Görev 9'da değişir); dokunma.

- [ ] **Step 6: Testi ve derlemeyi çalıştır**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.BuildConfigTest"
.\gradlew.bat :app:assembleDebug
```

Beklenen: PASS, ardından `BUILD SUCCESSFUL`. (`surfaceContainer*` vb. henüz yok; Hilt KSP üretimi hatasız geçmeli.)

- [ ] **Step 7: Commit** (izin varsa)

```powershell
git add gradle/libs.versions.toml build.gradle.kts app
git commit -m "build: sürüm kataloğu, Hilt, ağ/test bağımlılıkları, manifest ve BuildConfig"
```

---
### Task 3: API modelleri, JSON yapılandırması ve hata eşleme

**Files:**
- Create: `app/src/main/java/com/kitap/app/core/net/KitapJson.kt`, `core/net/ApiResult.kt`, `data/dto/Dtos.kt`
- Test: `app/src/test/java/com/kitap/app/core/net/ApiResultTest.kt`, `app/src/test/java/com/kitap/app/data/dto/DtoParsingTest.kt`

**Interfaces:**
- Produces:
  - `object KitapJson { val instance: Json }` (`ignoreUnknownKeys`, `coerceInputValues`)
  - `sealed interface ApiResult<out T> { data class Success<T>(val value: T); data class Failure(val message: String, val code: Int? = null, val isNetwork: Boolean = false) }`
  - `suspend fun <T> safeApiCall(block: suspend () -> Response<T>): ApiResult<T>` (gövde zorunlu), `suspend fun <T> safeResponse(block: suspend () -> Response<T>): ApiResult<Response<T>>` (başlıklar için)
  - `object ApiMessages` (Türkçe sabit metinler)
  - DTO'lar (`com.kitap.app.data.dto`): `UserDto, QuotaDto, MeDto, ApiErrorDto, CsrfTokenDto, BookDto, PickupPointDto, EligibilityDto, DonationDto, LoginRequest, RegisterRequest, NotificationsResponse`

- [ ] **Step 1: Başarısız testleri yaz**

`app/src/test/java/com/kitap/app/data/dto/DtoParsingTest.kt`:

```kotlin
package com.kitap.app.data.dto

import com.kitap.app.core.net.KitapJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DtoParsingTest {
    private val json = KitapJson.instance

    @Test
    fun meDtoParsesAndIgnoresUnknownKeys() {
        val text = """
            {"user":{"id":7,"name":"Ayşe Demir","email":"ayse@ornek.com","admin":false,
             "studentStatus":null,"schoolLevel":null,"initials":"AD","address":"İzmir",
             "phone":null,"school":null,"yeniAlan":"yok sayilir"},
             "quota":{"tier":"member","weeklyUsed":0,"weeklyLimit":3,"weeklyRemaining":3,
             "monthlyUsed":1,"monthlyLimit":5,"monthlyRemaining":4,"canReceive":true}}
        """.trimIndent()
        val me = json.decodeFromString(MeDto.serializer(), text)
        assertEquals(7L, me.user.id)
        assertFalse(me.user.admin)
        assertNull(me.user.phone)
        assertEquals(4L, me.quota?.monthlyRemaining)
    }

    @Test
    fun adminFlagIsRead() {
        val text = """{"user":{"id":1,"name":"Yönetici","email":"admin@kitapla.app","admin":true,"initials":"Y"}}"""
        assertTrue(json.decodeFromString(MeDto.serializer(), text).user.admin)
    }

    @Test
    fun csrfTokenParses() {
        val text = """{"parameterName":"_csrf","token":"abc123","headerName":"X-CSRF-TOKEN"}"""
        val dto = json.decodeFromString(CsrfTokenDto.serializer(), text)
        assertEquals("abc123", dto.token)
        assertEquals("X-CSRF-TOKEN", dto.headerName)
    }

    @Test
    fun donationListParsesWithNullableParts() {
        val text = """
            [{"id":5,"book":{"id":2,"title":"Sefiller","author":"Victor Hugo","coverUrl":"/uploads/covers/a.jpg",
              "purchaseLink":null,"description":null},"donorName":"Ayşe","donorInitials":"A","description":null,
              "quantity":2,"claimed":1,"remaining":1,"source":"USER","targetLevel":"HEPSI","status":"OPEN",
              "priorityActive":false,"priorityLeft":null,"point":null,"createdAt":"2026-09-01T10:00:00Z","eligibility":null}]
        """.trimIndent()
        val list = json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(DonationDto.serializer()), text)
        assertEquals(1, list.size)
        assertEquals("Sefiller", list[0].book.title)
        assertEquals(1L, list[0].remaining)
        assertNull(list[0].point)
    }

    @Test
    fun loginRequestEncodesFields() {
        val text = json.encodeToString(LoginRequest.serializer(), LoginRequest("a@b.com", "sifre"))
        assertEquals("""{"email":"a@b.com","password":"sifre"}""", text)
    }
}
```

`app/src/test/java/com/kitap/app/core/net/ApiResultTest.kt`:

```kotlin
package com.kitap.app.core.net

import com.kitap.app.data.dto.MeDto
import com.kitap.app.data.dto.UserDto
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiResultTest {
    private fun errorResponse(code: Int, body: String) =
        Response.error<MeDto>(code, body.toResponseBody("application/json".toMediaType()))

    private val me = MeDto(UserDto(id = 1))

    @Test
    fun serverErrorMessageIsUsed() = runTest {
        val r = safeApiCall { errorResponse(400, """{"error":"E-posta ya da şifre hatalı."}""") }
        assertEquals(ApiResult.Failure("E-posta ya da şifre hatalı.", 400), r)
    }

    @Test
    fun nonJsonServerErrorFallsBackToGenericMessage() = runTest {
        val r = safeApiCall { errorResponse(502, "<html>Bad Gateway</html>") }
        assertEquals(ApiResult.Failure(ApiMessages.SERVER_ERROR, 502), r)
    }

    @Test
    fun emptyUnauthorizedBodyMapsToSessionExpired() = runTest {
        val r = safeApiCall { errorResponse(401, "") }
        assertEquals(ApiResult.Failure(ApiMessages.SESSION_EXPIRED, 401), r)
    }

    @Test
    fun ioExceptionMapsToNetworkFailure() = runTest {
        val r = safeApiCall<MeDto> { throw IOException("boom") }
        assertEquals(ApiResult.Failure(ApiMessages.NETWORK, null, true), r)
    }

    @Test
    fun serializationExceptionMapsToInvalidResponse() = runTest {
        val r = safeApiCall<MeDto> { throw SerializationException("bozuk") }
        assertEquals(ApiResult.Failure(ApiMessages.INVALID_RESPONSE), r)
    }

    @Test
    fun successReturnsBody() = runTest {
        val r = safeApiCall { Response.success(me) }
        assertEquals(ApiResult.Success(me), r)
    }

    @Test
    fun successWithNullBodyIsFailure() = runTest {
        val r = safeApiCall<MeDto> { Response.success<MeDto>(null) }
        assertEquals(ApiResult.Failure(ApiMessages.EMPTY_RESPONSE, 200), r)
    }

    @Test
    fun unitResponseIsSuccess() = runTest {
        val r = safeApiCall { Response.success(Unit) }
        assertTrue(r is ApiResult.Success)
    }

    @Test
    fun safeResponseKeepsHeaders() = runTest {
        val raw = Response.success(me, okhttp3.Headers.headersOf("X-Total-Count", "42"))
        val r = safeResponse { raw } as ApiResult.Success
        assertEquals("42", r.value.headers()["X-Total-Count"])
    }
}
```

- [ ] **Step 2: Testlerin başarısız olduğunu doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.data.dto.DtoParsingTest" --tests "com.kitap.app.core.net.ApiResultTest"
```

Beklenen: FAIL (derleme hatası: `Unresolved reference: KitapJson`, `MeDto`, `safeApiCall` …).

- [ ] **Step 3: Uygulamayı yaz**

`core/net/KitapJson.kt`:

```kotlin
package com.kitap.app.core.net

import kotlinx.serialization.json.Json

object KitapJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}
```

`data/dto/Dtos.kt`:

```kotlin
package com.kitap.app.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val name: String = "",
    val email: String = "",
    val admin: Boolean = false,
    val studentStatus: String? = null,
    val schoolLevel: String? = null,
    val initials: String = "",
    val address: String? = null,
    val phone: String? = null,
    val school: String? = null,
)

@Serializable
data class QuotaDto(
    val tier: String = "",
    val weeklyUsed: Long = 0,
    val weeklyLimit: Int = 0,
    val weeklyRemaining: Long = 0,
    val monthlyUsed: Long = 0,
    val monthlyLimit: Int = 0,
    val monthlyRemaining: Long = 0,
    val canReceive: Boolean = false,
)

@Serializable
data class MeDto(val user: UserDto, val quota: QuotaDto? = null)

@Serializable
data class ApiErrorDto(val error: String? = null)

@Serializable
data class CsrfTokenDto(
    val token: String,
    val headerName: String = "X-CSRF-TOKEN",
    val parameterName: String = "_csrf",
)

@Serializable
data class BookDto(
    val id: Long = 0,
    val title: String = "",
    val author: String? = null,
    val coverUrl: String? = null,
    val purchaseLink: String? = null,
    val description: String? = null,
)

@Serializable
data class PickupPointDto(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
    val active: Boolean = true,
)

@Serializable
data class EligibilityDto(
    val allowed: Boolean = false,
    val code: String? = null,
    val reason: String? = null,
)

@Serializable
data class DonationDto(
    val id: Long,
    val book: BookDto = BookDto(),
    val donorName: String? = null,
    val donorInitials: String? = null,
    val description: String? = null,
    val quantity: Int = 1,
    val claimed: Long = 0,
    val remaining: Long = 0,
    val source: String? = null,
    val targetLevel: String? = null,
    val status: String? = null,
    val priorityActive: Boolean = false,
    val priorityLeft: String? = null,
    val point: PickupPointDto? = null,
    val createdAt: String? = null,
    val eligibility: EligibilityDto? = null,
)

@Serializable
data class NotificationsResponse(val unread: Long = 0)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val school: String? = null,
    val level: String? = null,
    val phone: String? = null,
    val address: String? = null,
)
```

`core/net/ApiResult.kt`:

```kotlin
package com.kitap.app.core.net

import com.kitap.app.data.dto.ApiErrorDto
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException

object ApiMessages {
    const val NETWORK = "Bağlantı kurulamadı. İnternet bağlantınızı kontrol edip tekrar deneyin."
    const val SESSION_EXPIRED = "Oturum süresi doldu. Lütfen tekrar giriş yapın."
    const val FORBIDDEN = "Bu işlem için yetkiniz bulunmuyor."
    const val NOT_FOUND = "İstenen kayıt bulunamadı."
    const val SERVER_ERROR = "Sunucuda bir sorun oluştu. Lütfen tekrar deneyin."
    const val UNEXPECTED = "Beklenmeyen bir hata oluştu."
    const val INVALID_RESPONSE = "Sunucudan geçersiz bir yanıt alındı."
    const val EMPTY_RESPONSE = "Sunucudan boş yanıt alındı."
}

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(
        val message: String,
        val code: Int? = null,
        val isNetwork: Boolean = false,
    ) : ApiResult<Nothing>
}

internal fun errorMessage(raw: String?, code: Int): String {
    val fromServer = raw?.let {
        runCatching { KitapJson.instance.decodeFromString(ApiErrorDto.serializer(), it).error }.getOrNull()
    }
    if (!fromServer.isNullOrBlank()) return fromServer
    return when {
        code == 401 -> ApiMessages.SESSION_EXPIRED
        code == 403 -> ApiMessages.FORBIDDEN
        code == 404 -> ApiMessages.NOT_FOUND
        code >= 500 -> ApiMessages.SERVER_ERROR
        else -> ApiMessages.UNEXPECTED
    }
}

/** Yanıtın kendisini (başlıklar dahil) döndürür; hata gövdesini Türkçe mesaja çevirir. */
suspend fun <T> safeResponse(block: suspend () -> Response<T>): ApiResult<Response<T>> = try {
    val response = block()
    if (response.isSuccessful) {
        ApiResult.Success(response)
    } else {
        ApiResult.Failure(errorMessage(response.errorBody()?.string(), response.code()), response.code())
    }
} catch (e: IOException) {
    ApiResult.Failure(ApiMessages.NETWORK, null, true)
} catch (e: SerializationException) {
    ApiResult.Failure(ApiMessages.INVALID_RESPONSE)
}

/** Yalnızca gövdeyi döndürür; gövde yoksa hata sayılır. */
suspend fun <T> safeApiCall(block: suspend () -> Response<T>): ApiResult<T> =
    when (val result = safeResponse(block)) {
        is ApiResult.Failure -> result
        is ApiResult.Success -> {
            val body = result.value.body()
            if (body != null) ApiResult.Success(body)
            else ApiResult.Failure(ApiMessages.EMPTY_RESPONSE, result.value.code())
        }
    }
```

- [ ] **Step 4: Testlerin geçtiğini doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.data.dto.DtoParsingTest" --tests "com.kitap.app.core.net.ApiResultTest"
```

Beklenen: PASS (14 test).

- [ ] **Step 5: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(net): API DTO'ları, KitapJson ve ApiResult hata eşlemesi"
```

---

### Task 4: Kalıcı çerez deposu ve CSRF/401 interceptor'ları

**Files:**
- Create: `core/net/CookieCodec.kt`, `core/net/PersistentCookieJar.kt`, `core/net/EncryptedCookiePersistence.kt`, `core/net/CsrfInterceptor.kt`, `core/net/UnauthorizedInterceptor.kt`
- Test: `app/src/test/java/com/kitap/app/core/net/InMemoryCookiePersistence.kt`, `CookieCodecTest.kt`, `PersistentCookieJarTest.kt`, `CsrfInterceptorTest.kt`, `UnauthorizedInterceptorTest.kt`

**Interfaces:**
- Consumes: `KitapJson.instance`, `CsrfTokenDto` (Görev 3).
- Produces:
  - `interface CookiePersistence { fun load(): List<Cookie>; fun save(cookies: List<Cookie>) }`
  - `class PersistentCookieJar(persistence: CookiePersistence, clock: () -> Long = System::currentTimeMillis) : CookieJar { fun clear() }`
  - `object CookieCodec { fun encode(List<Cookie>): String; fun decode(String): List<Cookie> }`
  - `class EncryptedCookiePersistence(context: Context) : CookiePersistence`
  - `class CsrfInterceptor(baseUrl: HttpUrl) : Interceptor`
  - `class UnauthorizedInterceptor(onUnauthorized: () -> Unit) : Interceptor`
  - Test yardımcısı `class InMemoryCookiePersistence : CookiePersistence` (test kaynak kümesi; Görev 5 yeniden kullanır)

- [ ] **Step 1: Başarısız testleri yaz**

`InMemoryCookiePersistence.kt`:

```kotlin
package com.kitap.app.core.net

import okhttp3.Cookie

class InMemoryCookiePersistence : CookiePersistence {
    var stored: List<Cookie> = emptyList()
    override fun load(): List<Cookie> = stored
    override fun save(cookies: List<Cookie>) {
        stored = cookies
    }
}
```

`CookieCodecTest.kt`:

```kotlin
package com.kitap.app.core.net

import okhttp3.Cookie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CookieCodecTest {
    @Test
    fun roundTripKeepsAllFields() {
        val original = listOf(
            Cookie.Builder().name("KITAPLA_SESSION").value("abc").hostOnlyDomain("10.0.2.2")
                .path("/").httpOnly().build(),
            Cookie.Builder().name("x").value("y").domain("kitappla.com").path("/api")
                .secure().expiresAt(4_102_444_800_000L).build(),
        )
        val decoded = CookieCodec.decode(CookieCodec.encode(original))
        assertEquals(original.size, decoded.size)
        original.zip(decoded).forEach { (a, b) ->
            assertEquals(a.name, b.name)
            assertEquals(a.value, b.value)
            assertEquals(a.domain, b.domain)
            assertEquals(a.path, b.path)
            assertEquals(a.secure, b.secure)
            assertEquals(a.httpOnly, b.httpOnly)
            assertEquals(a.hostOnly, b.hostOnly)
            assertEquals(a.expiresAt, b.expiresAt)
        }
    }

    @Test
    fun emptyListRoundTrips() {
        assertTrue(CookieCodec.decode(CookieCodec.encode(emptyList())).isEmpty())
    }
}
```

`PersistentCookieJarTest.kt`:

```kotlin
package com.kitap.app.core.net

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistentCookieJarTest {
    private val url = "http://10.0.2.2:8080/api/v1/me".toHttpUrl()

    private fun session(value: String, expiresAt: Long? = null) =
        Cookie.Builder().name("KITAPLA_SESSION").value(value).hostOnlyDomain("10.0.2.2").path("/")
            .httpOnly().apply { if (expiresAt != null) expiresAt(expiresAt) }.build()

    @Test
    fun savedCookieIsReturnedForMatchingUrl() {
        val jar = PersistentCookieJar(InMemoryCookiePersistence())
        jar.saveFromResponse(url, listOf(session("abc")))
        assertEquals(listOf("abc"), jar.loadForRequest(url).map { it.value })
    }

    @Test
    fun cookieSurvivesNewJarInstanceOverSamePersistence() {
        val store = InMemoryCookiePersistence()
        PersistentCookieJar(store).saveFromResponse(url, listOf(session("abc")))
        assertEquals(listOf("abc"), PersistentCookieJar(store).loadForRequest(url).map { it.value })
    }

    @Test
    fun sameNameCookieIsReplaced() {
        val jar = PersistentCookieJar(InMemoryCookiePersistence())
        jar.saveFromResponse(url, listOf(session("eski")))
        jar.saveFromResponse(url, listOf(session("yeni")))
        assertEquals(listOf("yeni"), jar.loadForRequest(url).map { it.value })
    }

    @Test
    fun expiredCookieIsDroppedAndNotPersisted() {
        val store = InMemoryCookiePersistence()
        val jar = PersistentCookieJar(store, clock = { 10_000L })
        jar.saveFromResponse(url, listOf(session("bayat", expiresAt = 5_000L)))
        assertTrue(jar.loadForRequest(url).isEmpty())
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun clearRemovesEverythingIncludingPersistedCopy() {
        val store = InMemoryCookiePersistence()
        val jar = PersistentCookieJar(store)
        jar.saveFromResponse(url, listOf(session("abc")))
        jar.clear()
        assertTrue(jar.loadForRequest(url).isEmpty())
        assertTrue(store.stored.isEmpty())
    }
}
```

`CsrfInterceptorTest.kt`:

```kotlin
package com.kitap.app.core.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CsrfInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        client = OkHttpClient.Builder().addInterceptor(CsrfInterceptor(server.url("/"))).build()
    }

    @After
    fun tearDown() = server.shutdown()

    private fun csrf(token: String) = MockResponse().setBody(
        """{"token":"$token","headerName":"X-CSRF-TOKEN","parameterName":"_csrf"}"""
    )

    private fun post(path: String) = client.newCall(
        Request.Builder().url(server.url(path)).post("{}".toRequestBody()).build()
    ).execute()

    @Test
    fun mutatingRequestFetchesTokenFirstAndSendsHeader() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(200))
        assertEquals(200, post("/api/v1/donations").code)

        val first = server.takeRequest()
        assertEquals("GET", first.method)
        assertEquals("/api/v1/auth/csrf", first.path)
        val second = server.takeRequest()
        assertEquals("POST", second.method)
        assertEquals("t1", second.getHeader("X-CSRF-TOKEN"))
    }

    @Test
    fun getRequestsDoNotFetchToken() {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(Request.Builder().url(server.url("/api/v1/donations")).build()).execute().close()
        assertEquals(1, server.requestCount)
        assertNull(server.takeRequest().getHeader("X-CSRF-TOKEN"))
    }

    @Test
    fun tokenIsReusedAcrossRequests() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(200))
        post("/api/v1/a").close()
        post("/api/v1/b").close()
        assertEquals(3, server.requestCount)
    }

    @Test
    fun forbiddenRefreshesTokenAndRetriesOnce() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(403))
        server.enqueue(csrf("t2"))
        server.enqueue(MockResponse().setResponseCode(200))
        assertEquals(200, post("/api/v1/donations").code)

        repeat(2) { server.takeRequest() }          // csrf(t1), POST(t1) -> 403
        assertEquals("/api/v1/auth/csrf", server.takeRequest().path)
        assertEquals("t2", server.takeRequest().getHeader("X-CSRF-TOKEN"))
    }

    @Test
    fun secondForbiddenIsReturnedAsIs() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(403))
        server.enqueue(csrf("t2"))
        server.enqueue(MockResponse().setResponseCode(403))
        assertEquals(403, post("/api/v1/donations").code)
        assertEquals(4, server.requestCount)
    }

    @Test
    fun successfulLoginInvalidatesToken() {
        server.enqueue(csrf("t1"))
        server.enqueue(MockResponse().setResponseCode(200))   // login
        server.enqueue(csrf("t2"))
        server.enqueue(MockResponse().setResponseCode(200))   // sonraki POST
        post("/api/v1/auth/login").close()
        post("/api/v1/donations").close()

        repeat(2) { server.takeRequest() }
        assertEquals("/api/v1/auth/csrf", server.takeRequest().path)
        assertEquals("t2", server.takeRequest().getHeader("X-CSRF-TOKEN"))
    }
}
```

`UnauthorizedInterceptorTest.kt`:

```kotlin
package com.kitap.app.core.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UnauthorizedInterceptorTest {
    private lateinit var server: MockWebServer
    private var calls = 0
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        client = OkHttpClient.Builder().addInterceptor(UnauthorizedInterceptor { calls++ }).build()
    }

    @After
    fun tearDown() = server.shutdown()

    private fun get(path: String, code: Int) {
        server.enqueue(MockResponse().setResponseCode(code))
        client.newCall(Request.Builder().url(server.url(path)).build()).execute().close()
    }

    @Test
    fun unauthorizedOnProtectedEndpointTriggersCallback() {
        get("/api/v1/me", 401)
        assertEquals(1, calls)
    }

    @Test
    fun unauthorizedOnAuthEndpointDoesNot() {
        get("/api/v1/auth/login", 401)
        assertEquals(0, calls)
    }

    @Test
    fun otherStatusesDoNot() {
        get("/api/v1/me", 200)
        get("/api/v1/me", 403)
        assertEquals(0, calls)
    }
}
```

- [ ] **Step 2: Testlerin başarısız olduğunu doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.core.net.*"
```

Beklenen: FAIL (derleme: `Unresolved reference: CookiePersistence`, `CookieCodec`, `PersistentCookieJar`, `CsrfInterceptor`, `UnauthorizedInterceptor`).

- [ ] **Step 3: Uygulamayı yaz**

`core/net/CookieCodec.kt`:

```kotlin
package com.kitap.app.core.net

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import okhttp3.Cookie

@Serializable
private data class StoredCookie(
    val name: String,
    val value: String,
    val expiresAt: Long,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean,
)

object CookieCodec {
    private val serializer = ListSerializer(StoredCookie.serializer())

    fun encode(cookies: List<Cookie>): String =
        KitapJson.instance.encodeToString(serializer, cookies.map { it.toStored() })

    fun decode(text: String): List<Cookie> =
        KitapJson.instance.decodeFromString(serializer, text).map { it.toCookie() }

    private fun Cookie.toStored() =
        StoredCookie(name, value, expiresAt, domain, path, secure, httpOnly, hostOnly)

    private fun StoredCookie.toCookie(): Cookie {
        val builder = Cookie.Builder().name(name).value(value).expiresAt(expiresAt).path(path)
        if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
        if (secure) builder.secure()
        if (httpOnly) builder.httpOnly()
        return builder.build()
    }
}
```

`core/net/PersistentCookieJar.kt`:

```kotlin
package com.kitap.app.core.net

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

interface CookiePersistence {
    fun load(): List<Cookie>
    fun save(cookies: List<Cookie>)
}

/** Çerezleri bellekte tutar ve her değişiklikte kalıcı depoya yazar. Tek çerez deposu vardır. */
class PersistentCookieJar(
    private val persistence: CookiePersistence,
    private val clock: () -> Long = System::currentTimeMillis,
) : CookieJar {
    private val lock = Any()
    private val cookies: MutableList<Cookie> = persistence.load().toMutableList()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) = synchronized(lock) {
        for (incoming in cookies) {
            this.cookies.removeAll {
                it.name == incoming.name && it.domain == incoming.domain && it.path == incoming.path
            }
            this.cookies.add(incoming)
        }
        prune()
        persistence.save(this.cookies.toList())
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = synchronized(lock) {
        prune()
        cookies.filter { it.matches(url) }
    }

    fun clear() = synchronized(lock) {
        cookies.clear()
        persistence.save(emptyList())
    }

    private fun prune() {
        cookies.removeAll { it.expiresAt < clock() }
    }
}
```

`core/net/EncryptedCookiePersistence.kt`:

```kotlin
package com.kitap.app.core.net

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie

class EncryptedCookiePersistence(private val context: Context) : CookiePersistence {
    private val prefs: SharedPreferences = open()

    private fun open(): SharedPreferences = try {
        create()
    } catch (e: Exception) {
        // Bozulmuş Keystore/dosya: depoyu silip temiz başla (kullanıcı yeniden giriş yapar).
        context.deleteSharedPreferences(FILE)
        create()
    }

    private fun create(): SharedPreferences {
        val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        return EncryptedSharedPreferences.create(
            context,
            FILE,
            key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun load(): List<Cookie> =
        prefs.getString(KEY, null)?.let { runCatching { CookieCodec.decode(it) }.getOrNull() }.orEmpty()

    override fun save(cookies: List<Cookie>) {
        prefs.edit().putString(KEY, CookieCodec.encode(cookies)).apply()
    }

    private companion object {
        const val FILE = "kitap_session"
        const val KEY = "cookies"
    }
}
```

`core/net/CsrfInterceptor.kt`:

```kotlin
package com.kitap.app.core.net

import com.kitap.app.data.dto.CsrfTokenDto
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Uygulama interceptor'ı: değiştirici isteklere CSRF başlığı ekler. Token yoksa `GET /api/v1/auth/csrf`
 * ile alır; 403'te token'ı yenileyip bir kez yeniden dener; giriş/kayıt/çıkış başarısında token'ı düşürür
 * (sunucu oturum değişince token'ı yeniler).
 */
class CsrfInterceptor(private val baseUrl: HttpUrl) : Interceptor {
    @Volatile private var header: String? = null
    @Volatile private var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.method in SAFE_METHODS) return chain.proceed(request)

        var response = chain.proceed(withToken(chain, request))
        if (response.code == 403) {
            response.close()
            invalidate()
            response = chain.proceed(withToken(chain, request))
        }
        if (response.isSuccessful && request.url.encodedPath in SESSION_CHANGING_PATHS) invalidate()
        return response
    }

    private fun withToken(chain: Interceptor.Chain, request: Request): Request {
        val (name, value) = currentToken(chain)
        return request.newBuilder().header(name, value).build()
    }

    @Synchronized
    private fun currentToken(chain: Interceptor.Chain): Pair<String, String> {
        val h = header
        val t = token
        if (h != null && t != null) return h to t
        val fetched = fetch(chain)
        header = fetched.headerName
        token = fetched.token
        return fetched.headerName to fetched.token
    }

    private fun fetch(chain: Interceptor.Chain): CsrfTokenDto {
        val url = baseUrl.newBuilder().addPathSegments("api/v1/auth/csrf").build()
        chain.proceed(Request.Builder().url(url).get().build()).use { response ->
            if (!response.isSuccessful) throw IOException("CSRF jetonu alınamadı (${response.code}).")
            val body = response.body?.string() ?: throw IOException("CSRF yanıtı boş.")
            return KitapJson.instance.decodeFromString(CsrfTokenDto.serializer(), body)
        }
    }

    @Synchronized
    private fun invalidate() {
        header = null
        token = null
    }

    private companion object {
        val SAFE_METHODS = setOf("GET", "HEAD", "OPTIONS", "TRACE")
        val SESSION_CHANGING_PATHS = setOf(
            "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/logout",
        )
    }
}
```

`core/net/UnauthorizedInterceptor.kt`:

```kotlin
package com.kitap.app.core.net

import okhttp3.Interceptor
import okhttp3.Response

/** Korumalı uçta 401 gelirse oturumun düştüğünü bildirir (auth uçları hariç: giriş hataları 400 döner). */
class UnauthorizedInterceptor(private val onUnauthorized: () -> Unit) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401 && !chain.request().url.encodedPath.startsWith("/api/v1/auth/")) {
            onUnauthorized()
        }
        return response
    }
}
```

- [ ] **Step 4: Testlerin geçtiğini doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.core.net.*"
```

Beklenen: PASS (Görev 3 testleri dahil). `assembleDebug` de geçmeli (EncryptedCookiePersistence derlenir).

- [ ] **Step 5: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(net): kalıcı çerez deposu, CSRF ve 401 interceptor'ları"
```

---
### Task 5: Oturum yönetimi, Retrofit servisleri, AuthRepository ve Hilt modülü

**Files:**
- Create: `core/session/SessionManager.kt`, `data/api/AuthApi.kt`, `data/api/DonationApi.kt`, `data/api/NotificationApi.kt`, `data/repo/AuthRepository.kt`, `di/NetworkModule.kt`
- Test: `app/src/test/java/com/kitap/app/core/session/SessionManagerTest.kt`, `app/src/test/java/com/kitap/app/data/repo/AuthRepositoryTest.kt`

**Interfaces:**
- Consumes: `PersistentCookieJar`, `CookiePersistence`, `InMemoryCookiePersistence` (test), `ApiResult`, `safeApiCall`, DTO'lar, `KitapJson`, `CsrfInterceptor`, `UnauthorizedInterceptor`, `EncryptedCookiePersistence`.
- Produces:
  - `sealed interface SessionState { data object Loading; data object Guest; data class Member(val user: UserDto); data class Admin(val user: UserDto) }`
  - `@Singleton class SessionManager @Inject constructor(cookieJar: PersistentCookieJar) { val state: StateFlow<SessionState>; fun signedIn(user: UserDto); fun signedOut(); fun markGuest(); fun onUnauthorized() }`
  - `interface AuthApi { login(LoginRequest): Response<MeDto>; register(RegisterRequest): Response<MeDto>; logout(): Response<Unit>; me(): Response<MeDto> }`
  - `interface DonationApi { donations(page, size, level, q, available): Response<List<DonationDto>> }`, `interface NotificationApi { notifications(): Response<NotificationsResponse> }`
  - `enum class LoginMode { MEMBER, ADMIN }`, `const val LOGIN_FAILED_MESSAGE = "E-posta ya da şifre hatalı."`
  - `@Singleton class AuthRepository @Inject constructor(api: AuthApi, session: SessionManager) { suspend fun restore(); suspend fun login(email, password, mode): ApiResult<UserDto>; suspend fun register(name, email, password): ApiResult<UserDto>; suspend fun logout() }`

- [ ] **Step 1: Başarısız testleri yaz**

`SessionManagerTest.kt`:

```kotlin
package com.kitap.app.core.session

import com.kitap.app.core.net.InMemoryCookiePersistence
import com.kitap.app.core.net.PersistentCookieJar
import com.kitap.app.data.dto.UserDto
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionManagerTest {
    private val url = "http://10.0.2.2:8080/".toHttpUrl()
    private val store = InMemoryCookiePersistence()
    private val jar = PersistentCookieJar(store)
    private val manager = SessionManager(jar)
    private val member = UserDto(id = 1, name = "Ayşe", admin = false)
    private val admin = UserDto(id = 2, name = "Yönetici", admin = true)

    private fun seedCookie() = jar.saveFromResponse(
        url, listOf(Cookie.Builder().name("S").value("v").hostOnlyDomain("10.0.2.2").path("/").build())
    )

    @Test
    fun startsAsLoading() {
        assertEquals(SessionState.Loading, manager.state.value)
    }

    @Test
    fun signedInPicksStateByAdminFlag() {
        manager.signedIn(member)
        assertEquals(SessionState.Member(member), manager.state.value)
        manager.signedIn(admin)
        assertEquals(SessionState.Admin(admin), manager.state.value)
    }

    @Test
    fun signedOutClearsCookiesAndBecomesGuest() {
        seedCookie()
        manager.signedIn(member)
        manager.signedOut()
        assertEquals(SessionState.Guest, manager.state.value)
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun markGuestKeepsCookies() {
        seedCookie()
        manager.markGuest()
        assertEquals(SessionState.Guest, manager.state.value)
        assertEquals(1, store.stored.size)
    }

    @Test
    fun onUnauthorizedDropsMemberSessionAndCookies() {
        seedCookie()
        manager.signedIn(member)
        manager.onUnauthorized()
        assertEquals(SessionState.Guest, manager.state.value)
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun onUnauthorizedWhileGuestIsNoOp() {
        seedCookie()
        manager.markGuest()
        manager.onUnauthorized()
        assertEquals(1, store.stored.size)
    }
}
```

`AuthRepositoryTest.kt`:

```kotlin
package com.kitap.app.data.repo

import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.InMemoryCookiePersistence
import com.kitap.app.core.net.PersistentCookieJar
import com.kitap.app.core.session.SessionManager
import com.kitap.app.core.session.SessionState
import com.kitap.app.data.api.AuthApi
import com.kitap.app.data.dto.LoginRequest
import com.kitap.app.data.dto.MeDto
import com.kitap.app.data.dto.RegisterRequest
import com.kitap.app.data.dto.UserDto
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

private class FakeAuthApi : AuthApi {
    var loginResponse: Response<MeDto> = ok(admin = false)
    var meResponse: Response<MeDto> = ok(admin = false)
    var meThrows: IOException? = null
    var logoutCalls = 0

    override suspend fun login(body: LoginRequest) = loginResponse
    override suspend fun register(body: RegisterRequest) = loginResponse
    override suspend fun me(): Response<MeDto> {
        meThrows?.let { throw it }
        return meResponse
    }

    override suspend fun logout(): Response<Unit> {
        logoutCalls++
        return Response.success(Unit)
    }

    companion object {
        fun ok(admin: Boolean): Response<MeDto> =
            Response.success(MeDto(UserDto(id = if (admin) 2 else 1, name = "X", admin = admin)))

        fun error(code: Int, body: String): Response<MeDto> =
            Response.error(code, body.toResponseBody("application/json".toMediaType()))
    }
}

class AuthRepositoryTest {
    private val api = FakeAuthApi()
    private val session = SessionManager(PersistentCookieJar(InMemoryCookiePersistence()))
    private val repo = AuthRepository(api, session)

    @Test
    fun memberModeAcceptsNormalUser() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = false)
        val r = repo.login("a@b.com", "sifre", LoginMode.MEMBER)
        assertTrue(r is ApiResult.Success)
        assertTrue(session.state.value is SessionState.Member)
        assertEquals(0, api.logoutCalls)
    }

    @Test
    fun memberModeRejectsAdminAccountAndLogsOut() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = true)
        val r = repo.login("admin@kitapla.app", "admin123", LoginMode.MEMBER)
        assertEquals(ApiResult.Failure(LOGIN_FAILED_MESSAGE, 400), r)
        assertEquals(SessionState.Guest, session.state.value)
        assertEquals(1, api.logoutCalls)
    }

    @Test
    fun adminModeAcceptsAdminAccount() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = true)
        val r = repo.login("admin@kitapla.app", "admin123", LoginMode.ADMIN)
        assertTrue(r is ApiResult.Success)
        assertTrue(session.state.value is SessionState.Admin)
    }

    @Test
    fun adminModeRejectsNormalUserAndLogsOut() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = false)
        val r = repo.login("ayse@ornek.com", "sifre123", LoginMode.ADMIN)
        assertEquals(ApiResult.Failure(LOGIN_FAILED_MESSAGE, 400), r)
        assertEquals(SessionState.Guest, session.state.value)
        assertEquals(1, api.logoutCalls)
    }

    @Test
    fun serverErrorIsPassedThroughWithoutLogout() = runTest {
        api.loginResponse = FakeAuthApi.error(400, """{"error":"Hesabın askıya alınmış."}""")
        val r = repo.login("a@b.com", "x", LoginMode.MEMBER)
        assertEquals(ApiResult.Failure("Hesabın askıya alınmış.", 400), r)
        assertEquals(0, api.logoutCalls)
    }

    @Test
    fun registerSignsInAsMember() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = false)
        val r = repo.register("Ayşe", "a@b.com", "sifre")
        assertTrue(r is ApiResult.Success)
        assertTrue(session.state.value is SessionState.Member)
    }

    @Test
    fun restoreWithValidSessionSignsIn() = runTest {
        api.meResponse = FakeAuthApi.ok(admin = true)
        repo.restore()
        assertTrue(session.state.value is SessionState.Admin)
    }

    @Test
    fun restoreWithUnauthorizedBecomesGuest() = runTest {
        api.meResponse = FakeAuthApi.error(401, "")
        repo.restore()
        assertEquals(SessionState.Guest, session.state.value)
    }

    @Test
    fun restoreWithNetworkErrorBecomesGuest() = runTest {
        api.meThrows = IOException("offline")
        repo.restore()
        assertEquals(SessionState.Guest, session.state.value)
    }

    @Test
    fun logoutCallsApiAndBecomesGuest() = runTest {
        session.signedIn(UserDto(id = 1))
        repo.logout()
        assertEquals(1, api.logoutCalls)
        assertEquals(SessionState.Guest, session.state.value)
    }
}
```

- [ ] **Step 2: Testlerin başarısız olduğunu doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.core.session.*" --tests "com.kitap.app.data.repo.AuthRepositoryTest"
```

Beklenen: FAIL (derleme: `Unresolved reference: SessionManager`, `AuthApi`, `AuthRepository`, `LoginMode`).

- [ ] **Step 3: Uygulamayı yaz**

`core/session/SessionManager.kt`:

```kotlin
package com.kitap.app.core.session

import com.kitap.app.core.net.PersistentCookieJar
import com.kitap.app.data.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SessionState {
    data object Loading : SessionState
    data object Guest : SessionState
    data class Member(val user: UserDto) : SessionState
    data class Admin(val user: UserDto) : SessionState
}

@Singleton
class SessionManager @Inject constructor(private val cookieJar: PersistentCookieJar) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    fun signedIn(user: UserDto) {
        _state.value = if (user.admin) SessionState.Admin(user) else SessionState.Member(user)
    }

    /** Çıkış/oturum düşmesi: çerezleri siler. */
    fun signedOut() {
        cookieJar.clear()
        _state.value = SessionState.Guest
    }

    /** Ağ hatası gibi durumlarda çerezleri koruyarak misafir görünümüne düşer. */
    fun markGuest() {
        _state.value = SessionState.Guest
    }

    fun onUnauthorized() {
        if (_state.value !is SessionState.Guest) signedOut()
    }
}
```

`data/api/AuthApi.kt`:

```kotlin
package com.kitap.app.data.api

import com.kitap.app.data.dto.LoginRequest
import com.kitap.app.data.dto.MeDto
import com.kitap.app.data.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<MeDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<MeDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/v1/me")
    suspend fun me(): Response<MeDto>
}
```

`data/api/DonationApi.kt`:

```kotlin
package com.kitap.app.data.api

import com.kitap.app.data.dto.DonationDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DonationApi {
    @GET("api/v1/donations")
    suspend fun donations(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("level") level: String?,
        @Query("q") query: String?,
        @Query("available") available: Boolean,
    ): Response<List<DonationDto>>
}
```

`data/api/NotificationApi.kt`:

```kotlin
package com.kitap.app.data.api

import com.kitap.app.data.dto.NotificationsResponse
import retrofit2.Response
import retrofit2.http.GET

interface NotificationApi {
    @GET("api/v1/notifications")
    suspend fun notifications(): Response<NotificationsResponse>
}
```

`data/repo/AuthRepository.kt`:

```kotlin
package com.kitap.app.data.repo

import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeApiCall
import com.kitap.app.core.session.SessionManager
import com.kitap.app.data.api.AuthApi
import com.kitap.app.data.dto.LoginRequest
import com.kitap.app.data.dto.RegisterRequest
import com.kitap.app.data.dto.UserDto
import javax.inject.Inject
import javax.inject.Singleton

enum class LoginMode { MEMBER, ADMIN }

const val LOGIN_FAILED_MESSAGE = "E-posta ya da şifre hatalı."

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val session: SessionManager,
) {
    /** Uygulama açılışında kalıcı çerezle oturumu çözer. Herhangi bir hatada misafir görünümüne düşer. */
    suspend fun restore() {
        when (val r = safeApiCall { api.me() }) {
            is ApiResult.Success -> session.signedIn(r.value.user)
            is ApiResult.Failure -> session.markGuest()
        }
    }

    suspend fun login(email: String, password: String, mode: LoginMode): ApiResult<UserDto> =
        when (val r = safeApiCall { api.login(LoginRequest(email.trim(), password)) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> admitOrReject(r.value.user, mode)
        }

    suspend fun register(name: String, email: String, password: String): ApiResult<UserDto> =
        when (val r = safeApiCall {
            api.register(RegisterRequest(name = name.trim(), email = email.trim(), password = password))
        }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> admitOrReject(r.value.user, LoginMode.MEMBER)
        }

    suspend fun logout() {
        safeApiCall { api.logout() }
        session.signedOut()
    }

    /**
     * Rol/kapı uyumsuzsa sunucudaki oturumu hemen kapatır. Hata metni genel tutulur: hesabın
     * varlığı ya da rolü ifşa edilmez.
     */
    private suspend fun admitOrReject(user: UserDto, mode: LoginMode): ApiResult<UserDto> {
        val allowed = (mode == LoginMode.ADMIN) == user.admin
        if (!allowed) {
            safeApiCall { api.logout() }
            session.signedOut()
            return ApiResult.Failure(LOGIN_FAILED_MESSAGE, 400)
        }
        session.signedIn(user)
        return ApiResult.Success(user)
    }
}
```

`di/NetworkModule.kt`:

```kotlin
package com.kitap.app.di

import android.content.Context
import com.kitap.app.BuildConfig
import com.kitap.app.core.net.CookiePersistence
import com.kitap.app.core.net.CsrfInterceptor
import com.kitap.app.core.net.EncryptedCookiePersistence
import com.kitap.app.core.net.KitapJson
import com.kitap.app.core.net.PersistentCookieJar
import com.kitap.app.core.net.UnauthorizedInterceptor
import com.kitap.app.core.session.SessionManager
import com.kitap.app.data.api.AuthApi
import com.kitap.app.data.api.DonationApi
import com.kitap.app.data.api.NotificationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun cookiePersistence(@ApplicationContext context: Context): CookiePersistence =
        EncryptedCookiePersistence(context)

    @Provides @Singleton
    fun cookieJar(persistence: CookiePersistence): PersistentCookieJar = PersistentCookieJar(persistence)

    @Provides @Singleton
    fun baseUrl(): HttpUrl = BuildConfig.API_BASE_URL.toHttpUrl()

    @Provides @Singleton
    fun okHttp(jar: PersistentCookieJar, baseUrl: HttpUrl, session: SessionManager): OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(jar)
            .addInterceptor(CsrfInterceptor(baseUrl))
            .addInterceptor(UnauthorizedInterceptor { session.onUnauthorized() })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun retrofit(client: OkHttpClient, baseUrl: HttpUrl): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(KitapJson.instance.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton fun authApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    @Provides @Singleton fun donationApi(retrofit: Retrofit): DonationApi = retrofit.create(DonationApi::class.java)
    @Provides @Singleton fun notificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)
}
```

- [ ] **Step 4: Testlerin geçtiğini ve derlemenin sağlam olduğunu doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Beklenen: tüm birim testleri PASS; `assembleDebug` başarılı (Hilt grafiği KSP ile üretilir: `SessionManager` ← `PersistentCookieJar` ← `CookiePersistence`; `OkHttpClient` ← `SessionManager`, döngü yok).

- [ ] **Step 5: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(auth): SessionManager, AuthRepository (rol kapısı), Retrofit servisleri ve Hilt modülü"
```

---

### Task 6: Tema (açık/koyu, Plus Jakarta Sans, Yönetici teması)

**Files:**
- Create: `ui/theme/Color.kt`, `ui/theme/Type.kt`, `ui/theme/Theme.kt`, `app/src/main/res/font/plus_jakarta_sans.ttf`

**Interfaces:**
- Produces: `@Composable fun KitapTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)`, `@Composable fun AdminTheme(content: @Composable () -> Unit)` (koyu espresso, tam ekran `Surface` içerir), `val KitapTypography: Typography`.

Bu görevde anlamlı bir saf mantık yok; doğrulama derleme + görsel kontroldür (test yazılmaz).

- [ ] **Step 1: Yazı tipini indir**

```powershell
$dir = "C:\Project\kitap\app\app\src\main\res\font"; New-Item -ItemType Directory $dir -Force | Out-Null
Invoke-WebRequest -Uri "https://github.com/google/fonts/raw/main/ofl/plusjakartasans/PlusJakartaSans%5Bwght%5D.ttf" -OutFile "$dir\plus_jakarta_sans.ttf"
$b = [System.IO.File]::ReadAllBytes("$dir\plus_jakarta_sans.ttf"); "boyut=$($b.Length) başlık=$('{0:X2}{1:X2}{2:X2}{3:X2}' -f $b[0],$b[1],$b[2],$b[3])"
```

Beklenen: boyut > 100000 ve başlık `00010000`. **İndirme başarısız olursa** yazı tipi dosyasını ekleme; `Type.kt`'de `PlusJakartaSans` yerine `FontFamily.Default` kullan (`Font(...)` satırlarını ve `R` import'unu sil) ve bunu rapora yaz.

- [ ] **Step 2: `Color.kt`**

```kotlin
package com.kitap.app.ui.theme

import androidx.compose.ui.graphics.Color

// Açık tema (web: --bg, --surface, --ink, --accent, --sage ...)
val Krem = Color(0xFFF3EAD3)
val KremYuzey = Color(0xFFFCF8EE)
val KremYuzey2 = Color(0xFFFBF3E2)
val KremCizgi = Color(0xFFE6D8BC)
val Kahve = Color(0xFF3E2723)
val KahveSoluk = Color(0xFF8C7B6B)
val Vurgu = Color(0xFFC65D47)
val VurguSoft = Color(0xFFF4DCD3)
val Adacayi = Color(0xFF8FA89B)
val AdacayiSoft = Color(0xFFE1E9E2)
val AdacayiMurekkep = Color(0xFF48594F)
val BordoKitap = Color(0xFF7A2E2A)

// Koyu tema
val KoyuZemin = Color(0xFF211B17)
val KoyuYuzey = Color(0xFF2A231D)
val KoyuYuzey2 = Color(0xFF2F271F)
val KoyuCizgi = Color(0xFF3A2F27)
val KoyuMurekkep = Color(0xFFECE3D4)
val KoyuSoluk = Color(0xFFA99C88)
val KoyuVurgu = Color(0xFFD6785F)
val KoyuVurguSoft = Color(0xFF3A2A23)
val KoyuAdacayiSoft = Color(0xFF26312B)
val KoyuAdacayiMurekkep = Color(0xFFB7C9BD)

// Yönetici Kapısı (koyu espresso)
val EspressoZemin = Color(0xFF2A1A17)
val EspressoYuzey = Color(0xFF3E2723)
```

- [ ] **Step 3: `Type.kt`**

```kotlin
package com.kitap.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.kitap.app.R

/** Değişken yazı tipi: ağırlık ekseni yalnızca API 26+'da uygulanır, altında normal ağırlık görünür. */
@OptIn(ExperimentalTextApi::class)
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
)

private fun Typography.withFont(f: FontFamily) = copy(
    displayLarge = displayLarge.copy(fontFamily = f),
    displayMedium = displayMedium.copy(fontFamily = f),
    displaySmall = displaySmall.copy(fontFamily = f),
    headlineLarge = headlineLarge.copy(fontFamily = f),
    headlineMedium = headlineMedium.copy(fontFamily = f),
    headlineSmall = headlineSmall.copy(fontFamily = f),
    titleLarge = titleLarge.copy(fontFamily = f),
    titleMedium = titleMedium.copy(fontFamily = f),
    titleSmall = titleSmall.copy(fontFamily = f),
    bodyLarge = bodyLarge.copy(fontFamily = f),
    bodyMedium = bodyMedium.copy(fontFamily = f),
    bodySmall = bodySmall.copy(fontFamily = f),
    labelLarge = labelLarge.copy(fontFamily = f),
    labelMedium = labelMedium.copy(fontFamily = f),
    labelSmall = labelSmall.copy(fontFamily = f),
)

val KitapTypography: Typography = Typography().withFont(PlusJakartaSans)
```

- [ ] **Step 4: `Theme.kt`**

```kotlin
package com.kitap.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Vurgu, onPrimary = Color.White,
    primaryContainer = VurguSoft, onPrimaryContainer = BordoKitap,
    secondary = Adacayi, onSecondary = Kahve,
    secondaryContainer = AdacayiSoft, onSecondaryContainer = AdacayiMurekkep,
    background = Krem, onBackground = Kahve,
    surface = KremYuzey, onSurface = Kahve,
    surfaceVariant = KremYuzey2, onSurfaceVariant = KahveSoluk,
    outline = KremCizgi, outlineVariant = KremCizgi,
    error = Color(0xFFB3261E), onError = Color.White,
    surfaceContainerLowest = KremYuzey, surfaceContainerLow = KremYuzey,
    surfaceContainer = KremYuzey2, surfaceContainerHigh = KremYuzey2, surfaceContainerHighest = KremYuzey2,
)

private val DarkColors = darkColorScheme(
    primary = KoyuVurgu, onPrimary = KoyuZemin,
    primaryContainer = KoyuVurguSoft, onPrimaryContainer = VurguSoft,
    secondary = Adacayi, onSecondary = KoyuZemin,
    secondaryContainer = KoyuAdacayiSoft, onSecondaryContainer = KoyuAdacayiMurekkep,
    background = KoyuZemin, onBackground = KoyuMurekkep,
    surface = KoyuYuzey, onSurface = KoyuMurekkep,
    surfaceVariant = KoyuYuzey2, onSurfaceVariant = KoyuSoluk,
    outline = KoyuCizgi, outlineVariant = KoyuCizgi,
    error = Color(0xFFF2B8B5), onError = KoyuZemin,
    surfaceContainerLowest = KoyuZemin, surfaceContainerLow = KoyuYuzey,
    surfaceContainer = KoyuYuzey2, surfaceContainerHigh = KoyuYuzey2, surfaceContainerHighest = KoyuYuzey2,
)

private val AdminColors = darkColorScheme(
    primary = KoyuVurgu, onPrimary = EspressoZemin,
    primaryContainer = KoyuVurguSoft, onPrimaryContainer = VurguSoft,
    secondary = Adacayi, onSecondary = EspressoZemin,
    background = EspressoZemin, onBackground = KoyuMurekkep,
    surface = EspressoYuzey, onSurface = KoyuMurekkep,
    surfaceVariant = EspressoYuzey, onSurfaceVariant = KoyuSoluk,
    outline = KoyuCizgi, outlineVariant = KoyuCizgi,
    error = Color(0xFFF2B8B5), onError = EspressoZemin,
    surfaceContainerLowest = EspressoZemin, surfaceContainerLow = EspressoYuzey,
    surfaceContainer = EspressoYuzey, surfaceContainerHigh = EspressoYuzey, surfaceContainerHighest = EspressoYuzey,
)

@Composable
fun KitapTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KitapTypography,
        content = content,
    )
}

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AdminColors, typography = KitapTypography) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, content = content)
    }
}
```

**Not:** `surfaceContainer*` parametreleri derlenmezse (Material3 < 1.2), yalnızca bu satırları sil; başka bir şeyi değiştirme.

- [ ] **Step 5: Derle**

```powershell
.\gradlew.bat :app:assembleDebug
```

Beklenen: `BUILD SUCCESSFUL`. (Tema Görev 9'da `MainActivity`'ye bağlanınca görsel olarak doğrulanır.)

- [ ] **Step 6: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(ui): web paletiyle açık/koyu tema, Plus Jakarta Sans ve Yönetici teması"
```

---

### Task 7: ExitGuard — tek geri tuşuyla çıkışı engelleyen güvence

**Files:**
- Create: `app/src/main/java/com/kitap/app/core/nav/ExitGuard.kt`
- Test: `app/src/test/java/com/kitap/app/core/nav/ExitGuardTest.kt`, `app/src/androidTest/java/com/kitap/app/core/nav/ExitGuardHandlerTest.kt`

**Interfaces:**
- Produces:
  - `class ExitGuard(windowMs: Long = 2_000L, clock: () -> Long = System::currentTimeMillis) { enum class Decision { WARN, EXIT }; fun onBackAtRoot(): Decision }`
  - `@Composable fun ExitGuardHandler(enabled: Boolean, onWarn: () -> Unit, onExit: () -> Unit)` — `enabled` iken geri tuşunu yakalar; ilk basış `onWarn`, pencere içindeki ikinci basış `onExit`.

- [ ] **Step 1: Başarısız birim testini yaz**

`ExitGuardTest.kt`:

```kotlin
package com.kitap.app.core.nav

import com.kitap.app.core.nav.ExitGuard.Decision
import org.junit.Assert.assertEquals
import org.junit.Test

class ExitGuardTest {
    private var now = 0L
    private val guard = ExitGuard(windowMs = 2_000L, clock = { now })

    @Test
    fun firstPressWarnsAndNeverExits() {
        assertEquals(Decision.WARN, guard.onBackAtRoot())
    }

    @Test
    fun secondPressWithinWindowExits() {
        guard.onBackAtRoot()
        now = 1_500L
        assertEquals(Decision.EXIT, guard.onBackAtRoot())
    }

    @Test
    fun pressExactlyAtWindowBoundaryExits() {
        guard.onBackAtRoot()
        now = 2_000L
        assertEquals(Decision.EXIT, guard.onBackAtRoot())
    }

    @Test
    fun secondPressAfterWindowWarnsAgain() {
        guard.onBackAtRoot()
        now = 2_001L
        assertEquals(Decision.WARN, guard.onBackAtRoot())
        now = 2_500L
        assertEquals(Decision.EXIT, guard.onBackAtRoot())
    }

    @Test
    fun afterExitNextPressWarnsAgain() {
        guard.onBackAtRoot()
        guard.onBackAtRoot()   // EXIT
        assertEquals(Decision.WARN, guard.onBackAtRoot())
    }
}
```

`ExitGuardHandlerTest.kt` (emülatör):

```kotlin
package com.kitap.app.core.nav

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class ExitGuardHandlerTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun pressBack() = rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }

    @Test
    fun singleBackPressDoesNotFinishActivity() {
        var warned = 0
        var exited = 0
        rule.setContent { ExitGuardHandler(enabled = true, onWarn = { warned++ }, onExit = { exited++ }) }
        pressBack()
        rule.waitForIdle()
        assertEquals(1, warned)
        assertEquals(0, exited)
        assertFalse(rule.activity.isFinishing)
    }

    @Test
    fun secondBackPressWithinWindowExits() {
        var warned = 0
        var exited = 0
        rule.setContent { ExitGuardHandler(enabled = true, onWarn = { warned++ }, onExit = { exited++ }) }
        pressBack()
        pressBack()
        rule.waitForIdle()
        assertEquals(1, warned)
        assertEquals(1, exited)
    }
}
```

- [ ] **Step 2: Birim testinin başarısız olduğunu doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.core.nav.ExitGuardTest"
```

Beklenen: FAIL (`Unresolved reference: ExitGuard`).

- [ ] **Step 3: Uygulamayı yaz**

`core/nav/ExitGuard.kt`:

```kotlin
package com.kitap.app.core.nav

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Kök hedefte geri tuşu kuralı: ilk basış uyarır, [windowMs] içindeki ikinci basış çıkar.
 * Tek bir basış hiçbir koşulda [Decision.EXIT] döndürmez.
 */
class ExitGuard(
    private val windowMs: Long = 2_000L,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    enum class Decision { WARN, EXIT }

    private var lastWarnAt: Long? = null

    fun onBackAtRoot(): Decision {
        val now = clock()
        val last = lastWarnAt
        return if (last != null && now - last <= windowMs) {
            lastWarnAt = null
            Decision.EXIT
        } else {
            lastWarnAt = now
            Decision.WARN
        }
    }
}

/** [enabled] iken geri tuşunu yakalar (kök hedef). Kök değilken kapalı tutulmalı; navigasyon kendi geri davranışını sürdürür. */
@Composable
fun ExitGuardHandler(enabled: Boolean, onWarn: () -> Unit, onExit: () -> Unit) {
    val guard = remember { ExitGuard() }
    BackHandler(enabled = enabled) {
        when (guard.onBackAtRoot()) {
            ExitGuard.Decision.WARN -> onWarn()
            ExitGuard.Decision.EXIT -> onExit()
        }
    }
}
```

- [ ] **Step 4: Testleri çalıştır**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.core.nav.ExitGuardTest"
```

Beklenen: PASS (5 test). Emülatör testi (Görev 9'un sonunda, emülatör açıkken birlikte koşulur):

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --tests "com.kitap.app.core.nav.ExitGuardHandlerTest"
```

Emülatör yoksa bu adımı Görev 9'a ertele; `assembleDebugAndroidTest` derlemesi şimdi geçmeli: `.\gradlew.bat :app:assembleDebugAndroidTest`.

- [ ] **Step 5: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(nav): ExitGuard — tek geri tuşuyla çıkış engeli (birim + emülatör testi)"
```

---
### Task 8: Sallama algılama ve titreşim

**Files:**
- Create: `core/device/ShakeLogic.kt`, `core/device/ShakeDetector.kt`, `core/device/Haptics.kt`
- Test: `app/src/test/java/com/kitap/app/core/device/ShakeLogicTest.kt`

**Interfaces:**
- Produces:
  - `object ShakeConfig { THRESHOLD_G = 2.7f; REQUIRED_SPIKES = 3; WINDOW_MS = 1500L; MIN_SPIKE_GAP_MS = 150L; COOLDOWN_MS = 2000L; GRAVITY = 9.80665f }` (sabitler tek yerde)
  - `class ShakeLogic { fun onSample(x: Float, y: Float, z: Float, nowMs: Long): Boolean; fun reset() }` — sallama tamamlandığında `true`
  - `class ShakeDetector(context: Context, onShake: () -> Unit) : SensorEventListener { fun start(): Boolean; fun stop() }`
  - `@Composable fun ShakeEffect(enabled: Boolean, onShake: () -> Unit)` — yalnızca `enabled` ve Activity `STARTED` iken ivmeölçeri dinler
  - `class Haptics(context: Context) { fun shakeBuzz() }` — tek ~400 ms titreşim

- [ ] **Step 1: Başarısız testi yaz**

`ShakeLogicTest.kt`:

```kotlin
package com.kitap.app.core.device

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeLogicTest {
    private val logic = ShakeLogic()

    /** ~3,06 g: eşiğin üstünde. */
    private fun spike(t: Long) = logic.onSample(0f, 30f, 0f, t)

    /** ~1 g: telefon dururken. */
    private fun rest(t: Long) = logic.onSample(0f, 0f, 9.81f, t)

    @Test
    fun restingNeverTriggers() {
        for (t in 0L..5_000L step 20L) assertFalse(rest(t))
    }

    @Test
    fun belowThresholdSamplesAreIgnored() {
        // ~2,04 g
        for (t in listOf(0L, 300L, 600L, 900L)) assertFalse(logic.onSample(0f, 20f, 0f, t))
    }

    @Test
    fun threeSpikesTriggerOnTheThird() {
        assertFalse(spike(0))
        assertFalse(spike(300))
        assertTrue(spike(600))
    }

    @Test
    fun twoSpikesDoNotTrigger() {
        assertFalse(spike(0))
        assertFalse(spike(300))
        assertFalse(rest(700))
    }

    @Test
    fun spikesSpreadWiderThanWindowDoNotTrigger() {
        assertFalse(spike(0))
        assertFalse(spike(800))
        assertFalse(spike(1_600))
    }

    @Test
    fun sustainedJoltIsCountedOnce() {
        for (t in 0L..140L step 20L) assertFalse(spike(t))
    }

    @Test
    fun cooldownIgnoresNewShakeThenAllowsAgain() {
        spike(0); spike(300)
        assertTrue(spike(600))
        assertFalse(spike(900))
        assertFalse(spike(1_200))
        assertFalse(spike(1_500))
        assertFalse(spike(3_000))
        assertFalse(spike(3_300))
        assertTrue(spike(3_600))
    }

    @Test
    fun resetClearsPartialShake() {
        spike(0); spike(300)
        logic.reset()
        assertFalse(spike(600))
    }
}
```

- [ ] **Step 2: Testin başarısız olduğunu doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.core.device.ShakeLogicTest"
```

Beklenen: FAIL (`Unresolved reference: ShakeLogic`).

- [ ] **Step 3: Uygulamayı yaz**

`core/device/ShakeLogic.kt`:

```kotlin
package com.kitap.app.core.device

import kotlin.math.sqrt

object ShakeConfig {
    /** Toplam ivme büyüklüğü (g); telefon dururken ≈1 g. */
    const val THRESHOLD_G = 2.7f
    const val REQUIRED_SPIKES = 3
    const val WINDOW_MS = 1_500L
    const val MIN_SPIKE_GAP_MS = 150L
    const val COOLDOWN_MS = 2_000L
    const val GRAVITY = 9.80665f
}

/** Saf mantık: örnekleri alır, sallama tamamlandığında `true` döndürür. */
class ShakeLogic(
    private val thresholdG: Float = ShakeConfig.THRESHOLD_G,
    private val requiredSpikes: Int = ShakeConfig.REQUIRED_SPIKES,
    private val windowMs: Long = ShakeConfig.WINDOW_MS,
    private val minSpikeGapMs: Long = ShakeConfig.MIN_SPIKE_GAP_MS,
    private val cooldownMs: Long = ShakeConfig.COOLDOWN_MS,
) {
    private val spikes = ArrayDeque<Long>()
    private var cooldownUntil = 0L

    fun onSample(x: Float, y: Float, z: Float, nowMs: Long): Boolean {
        if (nowMs < cooldownUntil) return false
        val g = sqrt(x * x + y * y + z * z) / ShakeConfig.GRAVITY
        if (g < thresholdG) return false
        if (spikes.isNotEmpty() && nowMs - spikes.last() < minSpikeGapMs) return false

        spikes.addLast(nowMs)
        while (spikes.isNotEmpty() && nowMs - spikes.first() > windowMs) spikes.removeFirst()
        if (spikes.size >= requiredSpikes) {
            spikes.clear()
            cooldownUntil = nowMs + cooldownMs
            return true
        }
        return false
    }

    fun reset() {
        spikes.clear()
        cooldownUntil = 0L
    }
}
```

`core/device/ShakeDetector.kt`:

```kotlin
package com.kitap.app.core.device

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class ShakeDetector(context: Context, private val onShake: () -> Unit) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val logic = ShakeLogic()

    /** İvmeölçer yoksa `false` döner (kapı erişilemez kalır; kabul edilen durum). */
    fun start(): Boolean {
        logic.reset()
        val sensor = accelerometer ?: return false
        return sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent) {
        val v = event.values
        if (logic.onSample(v[0], v[1], v[2], SystemClock.elapsedRealtime())) onShake()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

/** Yalnızca [enabled] iken ve Activity ön plandayken (STARTED) dinler; aksi hâlde sensör kapalıdır. */
@Composable
fun ShakeEffect(enabled: Boolean, onShake: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnShake by rememberUpdatedState(onShake)

    DisposableEffect(enabled, lifecycleOwner) {
        if (!enabled) return@DisposableEffect onDispose { }
        val detector = ShakeDetector(context) { currentOnShake() }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> detector.start()
                Lifecycle.Event.ON_STOP -> detector.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            detector.stop()
        }
    }
}
```

`core/device/Haptics.kt`:

```kotlin
package com.kitap.app.core.device

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class Haptics(context: Context) {
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    fun shakeBuzz() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(BUZZ_MS, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(BUZZ_MS)
        }
    }

    private companion object {
        const val BUZZ_MS = 400L
    }
}
```

- [ ] **Step 4: Testleri ve derlemeyi doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.core.device.ShakeLogicTest"
.\gradlew.bat :app:assembleDebug
```

Beklenen: PASS (8 test), `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(device): sallama algılama (ShakeLogic/ShakeEffect) ve titreşim"
```

---

### Task 9: Navigasyon kabuğu — tüm rotalar, üye/yönetim grafikleri, geri tuşu entegrasyonu

**Files:**
- Create: `ui/nav/Routes.kt`, `ui/nav/NavExt.kt`, `ui/nav/RootBackGuard.kt`, `ui/nav/AppViewModel.kt`, `ui/nav/AppRoot.kt`, `ui/nav/MemberNavHost.kt`, `ui/nav/AdminNavHost.kt`, `ui/screens/common/PlaceholderScreen.kt`, `ui/screens/common/MenuScreen.kt`
- Modify: `MainActivity.kt`
- Test: `app/src/test/java/com/kitap/app/ui/nav/RoutesTest.kt`, `app/src/androidTest/java/com/kitap/app/ui/nav/RootBackGuardTest.kt`

**Interfaces:**
- Consumes: `SessionState`, `SessionManager`, `AuthRepository` (`restore`, `logout`), `NotificationApi`, `safeApiCall`, `ExitGuardHandler`, `KitapTheme`, `AdminTheme`.
- Produces:
  - `object Routes` — tüm rota sabitleri; `TABS: List<String>`; `fun requiresAuth(route: String): Boolean`; `fun kitapDetay(id: Long): String`
  - `@Composable fun RootBackGuard(navController: NavHostController, onWarn: () -> Unit, onExit: () -> Unit)` (test edilebilir), `@Composable fun AppRootBackGuard(navController)`, `@Composable fun AppExitGuardHandler(enabled: Boolean)`, `const val EXIT_HINT`
  - `internal fun NavGraphBuilder.placeholder(route: String, title: String, onBack: (() -> Unit)?)`, `fun NavHostController.navigateToTab(route: String)`
  - `@Composable fun PlaceholderScreen(title: String, onBack: (() -> Unit)? = null)`, `data class MenuEntry(label, route)`, `@Composable fun MenuScreen(title, entries, onEntry, onLogout: (() -> Unit)?)`
  - `@HiltViewModel class AppViewModel { val session: StateFlow<SessionState>; val unread: StateFlow<Long>; fun logout() }`
  - `@Composable fun AppRoot()`, `@Composable fun MemberNavHost(session, unread, pendingRoute, onNeedLogin, onPendingConsumed, onLogout)`, `@Composable fun AdminNavHost(onLogout)`

- [ ] **Step 1: Başarısız testleri yaz**

`RoutesTest.kt`:

```kotlin
package com.kitap.app.ui.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTest {
    @Test
    fun publicRoutesDoNotRequireAuth() {
        listOf(
            Routes.KESFET, Routes.ISTEKLER, Routes.kitapDetay(5), Routes.SSS, Routes.KURALLAR,
            Routes.GIZLILIK, Routes.ILETISIM, Routes.LOGIN, Routes.REGISTER,
            Routes.SIFREMI_UNUTTUM, Routes.SIFRE_SIFIRLA, Routes.ADMIN_LOGIN,
        ).forEach { assertFalse("$it herkese açık olmalı", Routes.requiresAuth(it)) }
    }

    @Test
    fun memberRoutesRequireAuth() {
        listOf(
            Routes.TAKAS, Routes.MESAJLAR, Routes.PANOM, Routes.BILDIRIMLER, Routes.BAGIS_YENI,
            Routes.ISTEK_YENI, Routes.PROFIL, "sohbet/3", "sikayet/bagis/4", Routes.BAGISLARIM,
            Routes.ALDIKLARIM, Routes.TAKAS_TEKLIF,
        ).forEach { assertTrue("$it giriş istemeli", Routes.requiresAuth(it)) }
    }

    @Test
    fun tabsAreTheFiveMainDestinationsStartingWithKesfet() {
        assertEquals(
            listOf(Routes.KESFET, Routes.ISTEKLER, Routes.TAKAS, Routes.MESAJLAR, Routes.PANOM),
            Routes.TABS,
        )
    }

    @Test
    fun kitapDetayBuildsPath() {
        assertEquals("kitap/42", Routes.kitapDetay(42))
    }
}
```

`RootBackGuardTest.kt` (emülatör):

```kotlin
package com.kitap.app.ui.nav

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class RootBackGuardTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var nav: NavHostController
    private var warned = 0
    private var exited = 0

    private fun show() = rule.setContent {
        nav = rememberNavController()
        NavHost(nav, startDestination = "home") {
            composable("home") { Text("home") }
            composable("detail") { Text("detail") }
        }
        RootBackGuard(nav, onWarn = { warned++ }, onExit = { exited++ })
    }

    private fun pressBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun goTo(route: String) {
        rule.runOnUiThread { nav.navigate(route) }
        rule.waitForIdle()
    }

    private fun route(): String? = rule.runOnUiThread { nav.currentBackStackEntry?.destination?.route }

    @Test
    fun backAtRootWarnsAndKeepsActivityAlive() {
        show()
        pressBack()
        assertEquals(1, warned)
        assertEquals(0, exited)
        assertFalse(rule.activity.isFinishing)
    }

    @Test
    fun backFromDetailPopsToRootWithoutWarning() {
        show()
        goTo("detail")
        pressBack()
        assertEquals("home", route())
        assertEquals(0, warned)
        assertEquals(0, exited)
    }

    @Test
    fun secondBackAtRootWithinWindowExits() {
        show()
        pressBack()
        pressBack()
        assertEquals(1, warned)
        assertEquals(1, exited)
    }

    @Test
    fun singleBackNeverFinishesActivityAcrossNavigation() {
        show()
        goTo("detail")
        pressBack()   // detail -> home (NavHost)
        pressBack()   // home: uyarı
        assertEquals(1, warned)
        assertEquals(0, exited)
        assertFalse(rule.activity.isFinishing)
    }
}
```

- [ ] **Step 2: Birim testinin başarısız olduğunu doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.ui.nav.RoutesTest"
```

Beklenen: FAIL (`Unresolved reference: Routes`).

- [ ] **Step 3: Rotalar ve yardımcılar**

`ui/nav/Routes.kt`:

```kotlin
package com.kitap.app.ui.nav

object Routes {
    // Alt sekmeler
    const val KESFET = "kesfet"
    const val ISTEKLER = "istekler"
    const val TAKAS = "takas"
    const val MESAJLAR = "mesajlar"
    const val PANOM = "panom"
    val TABS = listOf(KESFET, ISTEKLER, TAKAS, MESAJLAR, PANOM)

    // Üye rotaları
    const val BILDIRIMLER = "bildirimler"
    const val BAGIS_YENI = "bagis/yeni"
    const val ISTEK_YENI = "istek/yeni"
    const val KITAP_DETAY = "kitap/{id}"
    fun kitapDetay(id: Long) = "kitap/$id"
    const val BAGISLARIM = "bagislarim"
    const val ALDIKLARIM = "aldiklarim"
    const val ISTEKLERIM = "isteklerim"
    const val KARSILADIKLARIM = "karsiladiklarim"
    const val TAKASLARIM = "takaslarim"
    const val TAKAS_KITAPLARIM = "takas/kitaplarim"
    const val TAKAS_TEKLIF = "takas/teklif"
    const val TAKAS_TEKLIF_DETAY = "takas/teklif/{id}"
    const val SIKAYETLERIM = "sikayetlerim"
    const val SIKAYET_ET = "sikayet/{kind}/{refId}"
    const val PROFIL = "profil"
    const val OGRENCI_DOGRULAMA = "profil/ogrenci"
    const val SOHBET = "sohbet/{id}"
    const val SSS = "sss"
    const val KURALLAR = "kurallar"
    const val GIZLILIK = "gizlilik"
    const val ILETISIM = "iletisim"

    // Kimlik
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SIFREMI_UNUTTUM = "sifremi-unuttum"
    const val SIFRE_SIFIRLA = "sifre-sifirla"
    const val ADMIN_LOGIN = "admin/login"

    // Yönetim grafiği
    const val ADMIN_PANO = "admin/pano"
    const val ADMIN_BELGELER = "admin/belgeler"
    const val ADMIN_UYELER = "admin/uyeler"
    const val ADMIN_ICERIK = "admin/icerik"
    const val ADMIN_NOKTALAR = "admin/noktalar"
    const val ADMIN_SIKAYETLER = "admin/sikayetler"
    const val ADMIN_SIKAYET_DETAY = "admin/sikayetler/{id}"
    const val ADMIN_MESAJLAR = "admin/mesajlar"

    /** Web'de herkese açık sayfalar + kimlik rotaları (ilk yol parçasına göre). */
    private val PUBLIC_PREFIXES = setOf(
        "kesfet", "istekler", "kitap", "sss", "kurallar", "gizlilik", "iletisim",
        "login", "register", "sifremi-unuttum", "sifre-sifirla", "admin",
    )

    fun requiresAuth(route: String): Boolean = route.substringBefore('/') !in PUBLIC_PREFIXES
}
```

`ui/nav/NavExt.kt`:

```kotlin
package com.kitap.app.ui.nav

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.kitap.app.ui.screens.common.PlaceholderScreen

internal fun NavGraphBuilder.placeholder(route: String, title: String, onBack: (() -> Unit)?) {
    composable(route) { PlaceholderScreen(title, onBack) }
}

/** Sekme geçişi: yığında yalnızca başlangıç hedefi (Keşfet) kalır, böylece geri önce Keşfet'e döner. */
fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
```

`ui/nav/RootBackGuard.kt`:

```kotlin
package com.kitap.app.ui.nav

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kitap.app.core.nav.ExitGuardHandler

const val EXIT_HINT = "Çıkmak için tekrar geri tuşuna basın"

/** Geri yığınında geri gidilecek yer kalmadıysa (kök hedef) `true`. */
@Composable
fun rememberIsAtRoot(navController: NavHostController): Boolean {
    val entry by navController.currentBackStackEntryAsState()
    return entry != null && navController.previousBackStackEntry == null
}

/** Test edilebilir sürüm: uyarı ve çıkış davranışı dışarıdan verilir. */
@Composable
fun RootBackGuard(navController: NavHostController, onWarn: () -> Unit, onExit: () -> Unit) {
    ExitGuardHandler(enabled = rememberIsAtRoot(navController), onWarn = onWarn, onExit = onExit)
}

/** Toast ile uyarır, ikinci basışta Activity'yi bitirir. Grafiği olmayan ekranlarda (splash) doğrudan kullanılır. */
@Composable
fun AppExitGuardHandler(enabled: Boolean) {
    val context = LocalContext.current
    ExitGuardHandler(
        enabled = enabled,
        onWarn = { Toast.makeText(context, EXIT_HINT, Toast.LENGTH_SHORT).show() },
        onExit = { context.findActivity()?.finish() },
    )
}

@Composable
fun AppRootBackGuard(navController: NavHostController) {
    AppExitGuardHandler(enabled = rememberIsAtRoot(navController))
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
```

- [ ] **Step 4: Ortak ekranlar**

`ui/screens/common/PlaceholderScreen.kt`:

```kotlin
package com.kitap.app.ui.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** İçeriği sonraki turlarda doldurulacak sayfalar için yer tutucu. [onBack] varsa üstte geri okuyla başlık gösterir. */
@Composable
fun PlaceholderScreen(title: String, onBack: (() -> Unit)? = null) {
    Column(Modifier.fillMaxSize()) {
        if (onBack != null) {
            Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (onBack == null) Text(title, style = MaterialTheme.typography.headlineMedium)
                Text("Bu sayfa yakında.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
```

`ui/screens/common/MenuScreen.kt`:

```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

package com.kitap.app.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class MenuEntry(val label: String, val route: String)

/** Panom ve Yönetim Pano'su için basit kart listesi; [onLogout] verilirse altta "Çıkış yap" düğmesi çıkar. */
@Composable
fun MenuScreen(
    title: String,
    entries: List<MenuEntry>,
    onEntry: (String) -> Unit,
    onLogout: (() -> Unit)?,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Text(title, style = MaterialTheme.typography.headlineMedium) }
        items(entries) { entry ->
            ElevatedCard(onClick = { onEntry(entry.route) }, modifier = Modifier.fillMaxWidth()) {
                Text(entry.label, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
        if (onLogout != null) {
            item {
                OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Çıkış yap") }
            }
        }
    }
}
```

- [ ] **Step 5: ViewModel, grafikler, AppRoot, MainActivity**

`ui/nav/AppViewModel.kt`:

```kotlin
package com.kitap.app.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeApiCall
import com.kitap.app.core.session.SessionManager
import com.kitap.app.core.session.SessionState
import com.kitap.app.data.api.NotificationApi
import com.kitap.app.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationApi: NotificationApi,
    sessionManager: SessionManager,
) : ViewModel() {
    val session: StateFlow<SessionState> = sessionManager.state

    private val _unread = MutableStateFlow(0L)
    val unread: StateFlow<Long> = _unread.asStateFlow()

    init {
        viewModelScope.launch { authRepository.restore() }
        viewModelScope.launch {
            session.collectLatest { s ->
                _unread.value = if (s is SessionState.Member) fetchUnread() else 0L
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    private suspend fun fetchUnread(): Long =
        (safeApiCall { notificationApi.notifications() } as? ApiResult.Success)?.value?.unread ?: 0L
}
```

`ui/nav/MemberNavHost.kt`:

```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

package com.kitap.app.ui.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kitap.app.core.session.SessionState
import com.kitap.app.ui.screens.common.MenuEntry
import com.kitap.app.ui.screens.common.MenuScreen
import com.kitap.app.ui.screens.common.PlaceholderScreen

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val TAB_ITEMS = listOf(
    TabItem(Routes.KESFET, "Keşfet", Icons.Outlined.Explore),
    TabItem(Routes.ISTEKLER, "İstekler", Icons.Outlined.MenuBook),
    TabItem(Routes.TAKAS, "Takas", Icons.Outlined.SwapHoriz),
    TabItem(Routes.MESAJLAR, "Mesajlar", Icons.Outlined.ChatBubbleOutline),
    TabItem(Routes.PANOM, "Panom", Icons.Outlined.Dashboard),
)

private val PANOM_ENTRIES = listOf(
    MenuEntry("Bağışlarım", Routes.BAGISLARIM),
    MenuEntry("Aldıklarım", Routes.ALDIKLARIM),
    MenuEntry("İsteklerim", Routes.ISTEKLERIM),
    MenuEntry("Karşıladıklarım", Routes.KARSILADIKLARIM),
    MenuEntry("Takaslarım", Routes.TAKASLARIM),
    MenuEntry("Takas kitaplarım", Routes.TAKAS_KITAPLARIM),
    MenuEntry("Şikâyetlerim", Routes.SIKAYETLERIM),
    MenuEntry("Profil", Routes.PROFIL),
    MenuEntry("Öğrenci doğrulama", Routes.OGRENCI_DOGRULAMA),
    MenuEntry("SSS", Routes.SSS),
    MenuEntry("Kurallar", Routes.KURALLAR),
    MenuEntry("Gizlilik", Routes.GIZLILIK),
    MenuEntry("İletişim", Routes.ILETISIM),
)

/**
 * Üye ve misafir grafiği (Keşfet açılış). Giriş gerektiren hedefe misafir gidince [onNeedLogin] ile hedef
 * saklanır ve Giriş açılır; giriş sonrası (AppRoot bu grafiği yeniden kurar) hedefe gidilir.
 */
@Composable
fun MemberNavHost(
    session: SessionState,
    unread: Long,
    pendingRoute: String?,
    onNeedLogin: (String) -> Unit,
    onPendingConsumed: () -> Unit,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isGuest = session is SessionState.Guest

    AppRootBackGuard(navController)

    fun open(route: String) {
        when {
            isGuest && Routes.requiresAuth(route) -> {
                onNeedLogin(route)
                navController.navigate(Routes.LOGIN) { launchSingleTop = true }
            }
            route in Routes.TABS -> navController.navigateToTab(route)
            else -> navController.navigate(route)
        }
    }

    LaunchedEffect(session) {
        if (session is SessionState.Member && pendingRoute != null) {
            navController.navigate(pendingRoute)
            onPendingConsumed()
        }
    }

    val showChrome = currentRoute != null && currentRoute in Routes.TABS
    val back: () -> Unit = { navController.popBackStack() }

    Scaffold(
        topBar = {
            if (showChrome) {
                MemberTopBar(
                    unread = unread,
                    onDonate = { open(Routes.BAGIS_YENI) },
                    onBell = { open(Routes.BILDIRIMLER) },
                )
            }
        },
        bottomBar = { if (showChrome) MemberBottomBar(currentRoute) { open(it) } },
    ) { padding ->
        NavHost(navController, startDestination = Routes.KESFET, modifier = Modifier.padding(padding)) {
            composable(Routes.KESFET) { PlaceholderScreen("Keşfet") }
            placeholder(Routes.ISTEKLER, "İstekler", null)
            placeholder(Routes.TAKAS, "Takas", null)
            placeholder(Routes.MESAJLAR, "Mesajlar", null)
            composable(Routes.PANOM) {
                MenuScreen("Panom", PANOM_ENTRIES, onEntry = { open(it) }, onLogout = onLogout)
            }

            placeholder(Routes.BILDIRIMLER, "Bildirimler", back)
            placeholder(Routes.BAGIS_YENI, "Bağış yap", back)
            placeholder(Routes.ISTEK_YENI, "İstek oluştur", back)
            placeholder(Routes.KITAP_DETAY, "Kitap detayı", back)
            placeholder(Routes.BAGISLARIM, "Bağışlarım", back)
            placeholder(Routes.ALDIKLARIM, "Aldıklarım", back)
            placeholder(Routes.ISTEKLERIM, "İsteklerim", back)
            placeholder(Routes.KARSILADIKLARIM, "Karşıladıklarım", back)
            placeholder(Routes.TAKASLARIM, "Takaslarım", back)
            placeholder(Routes.TAKAS_KITAPLARIM, "Takas kitaplarım", back)
            placeholder(Routes.TAKAS_TEKLIF, "Takas teklifi", back)
            placeholder(Routes.TAKAS_TEKLIF_DETAY, "Takas teklifi detayı", back)
            placeholder(Routes.SIKAYETLERIM, "Şikâyetlerim", back)
            placeholder(Routes.SIKAYET_ET, "Şikâyet et", back)
            placeholder(Routes.PROFIL, "Profil", back)
            placeholder(Routes.OGRENCI_DOGRULAMA, "Öğrenci doğrulama", back)
            placeholder(Routes.SOHBET, "Sohbet", back)
            placeholder(Routes.SSS, "Sık sorulan sorular", back)
            placeholder(Routes.KURALLAR, "Kurallar", back)
            placeholder(Routes.GIZLILIK, "Gizlilik", back)
            placeholder(Routes.ILETISIM, "İletişim", back)

            placeholder(Routes.LOGIN, "Giriş", back)
            placeholder(Routes.REGISTER, "Kayıt", back)
            placeholder(Routes.SIFREMI_UNUTTUM, "Şifremi unuttum", back)
            placeholder(Routes.SIFRE_SIFIRLA, "Şifre sıfırla", back)
            placeholder(Routes.ADMIN_LOGIN, "Yönetici girişi", back)
        }
    }
}

@Composable
private fun MemberTopBar(unread: Long, onDonate: () -> Unit, onBell: () -> Unit) {
    TopAppBar(
        title = {
            Text("KİTAPLA", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        },
        actions = {
            Button(onClick = onDonate, contentPadding = PaddingValues(horizontal = 12.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Bağış yap")
            }
            IconButton(onClick = onBell) {
                BadgedBox(badge = { if (unread > 0) Badge { Text(unread.toString()) } }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Bildirimler")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun MemberBottomBar(currentRoute: String?, onTab: (String) -> Unit) {
    NavigationBar {
        TAB_ITEMS.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTab(tab.route) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label, maxLines = 1) },
            )
        }
    }
}
```

`ui/nav/AdminNavHost.kt`:

```kotlin
package com.kitap.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kitap.app.ui.screens.common.MenuEntry
import com.kitap.app.ui.screens.common.MenuScreen
import com.kitap.app.ui.theme.AdminTheme

private val ADMIN_ENTRIES = listOf(
    MenuEntry("Öğrenci belgeleri", Routes.ADMIN_BELGELER),
    MenuEntry("Üyeler", Routes.ADMIN_UYELER),
    MenuEntry("İçerik", Routes.ADMIN_ICERIK),
    MenuEntry("Teslim noktaları", Routes.ADMIN_NOKTALAR),
    MenuEntry("Şikâyetler", Routes.ADMIN_SIKAYETLER),
    MenuEntry("Mesajlar", Routes.ADMIN_MESAJLAR),
)

/** Yönetim grafiği: yalnızca Admin oturumunda kurulur; kök hedef Pano'dur. */
@Composable
fun AdminNavHost(onLogout: () -> Unit) {
    val navController = rememberNavController()
    AppRootBackGuard(navController)
    val back: () -> Unit = { navController.popBackStack() }

    AdminTheme {
        NavHost(navController, startDestination = Routes.ADMIN_PANO) {
            composable(Routes.ADMIN_PANO) {
                MenuScreen("Yönetim", ADMIN_ENTRIES, onEntry = { navController.navigate(it) }, onLogout = onLogout)
            }
            placeholder(Routes.ADMIN_BELGELER, "Öğrenci belgeleri", back)
            placeholder(Routes.ADMIN_UYELER, "Üyeler", back)
            placeholder(Routes.ADMIN_ICERIK, "İçerik", back)
            placeholder(Routes.ADMIN_NOKTALAR, "Teslim noktaları", back)
            placeholder(Routes.ADMIN_SIKAYETLER, "Şikâyetler", back)
            placeholder(Routes.ADMIN_SIKAYET_DETAY, "Şikâyet detayı", back)
            placeholder(Routes.ADMIN_MESAJLAR, "Mesajlar", back)
        }
    }
}
```

`ui/nav/AppRoot.kt`:

```kotlin
package com.kitap.app.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.core.session.SessionState

/**
 * Oturum durumuna göre grafiği seçer. Grafik değişince (Misafir→Üye, Üye→Misafir, →Yönetim) yeni bir
 * NavHost kurulur; böylece geri yığını sıfırlanır ve geri tuşu Giriş/kapı ekranlarına dönmez.
 */
@Composable
fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val unread by viewModel.unread.collectAsStateWithLifecycle()
    var pendingRoute by rememberSaveable { mutableStateOf<String?>(null) }

    when (val s = session) {
        SessionState.Loading -> SplashScreen()
        is SessionState.Admin -> AdminNavHost(onLogout = viewModel::logout)
        else -> key(s::class) {
            MemberNavHost(
                session = s,
                unread = unread,
                pendingRoute = pendingRoute,
                onNeedLogin = { pendingRoute = it },
                onPendingConsumed = { pendingRoute = null },
                onLogout = viewModel::logout,
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    AppExitGuardHandler(enabled = true)   // oturum çözülürken de tek geri basışı çıkarmaz
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}
```

`MainActivity.kt` — tamamını değiştir:

```kotlin
package com.kitap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kitap.app.ui.nav.AppRoot
import com.kitap.app.ui.theme.KitapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KitapTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }
}
```

Mevcut `ui/screens/LoginScreen.kt` ve `RegisterScreen.kt` bu görevde artık çağrılmaz ama derlenir; Görev 10'da yeniden yazılıp `ui/screens/auth/` altına taşınır. **Dokunma.**

- [ ] **Step 6: Birim testleri, derleme ve emülatör testleri**

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
```

Beklenen: tüm birim testleri PASS; derlemeler başarılı.

Emülatörü başlat ve hazır olana kadar bekle (Görev 13'teki gibi; bir kez yeter, sonraki görevlerde açık bırak):

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"; $adb = "$sdk\platform-tools\adb.exe"
Start-Process "$sdk\emulator\emulator.exe" -ArgumentList "-avd","kitap_api34","-no-snapshot","-no-audio" -WindowStyle Minimized
& $adb wait-for-device
do { Start-Sleep 5; $b = (& $adb shell getprop sys.boot_completed).Trim() } until ($b -eq "1")
.\gradlew.bat :app:connectedDebugAndroidTest
```

Beklenen: `ExitGuardHandlerTest` (2) ve `RootBackGuardTest` (4) PASS.

- [ ] **Step 7: Elle duman testi**

```powershell
.\gradlew.bat :app:installDebug
& $adb shell am start -n com.kitap.app/.MainActivity
Start-Sleep 5
& $adb shell uiautomator dump /sdcard/ui.xml | Out-Null; & $adb pull /sdcard/ui.xml "$env:TEMP\ui.xml" | Out-Null
Select-String -Path "$env:TEMP\ui.xml" -Pattern "Keşfet" -Encoding utf8 | Select-Object -First 1
```

Beklenen: çıktıda `Keşfet` geçer (alt çubuk etiketi + yer tutucu). Backend çalışmıyorsa `GET /me` ağ hatasıyla Misafir'e düşer; bu beklenendir.

- [ ] **Step 8: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(nav): tüm rotalar, üye/yönetim grafikleri, kök geri tuşu güvencesi ve AppRoot"
```

---
### Task 10: Giriş ve Kayıt ekranları (gerçek API)

**Files:**
- Create: `ui/screens/auth/AuthValidator.kt`, `ui/screens/auth/AuthViewModel.kt`, `ui/screens/auth/AuthWidgets.kt`, `ui/screens/auth/LoginScreen.kt`, `ui/screens/auth/RegisterScreen.kt`
- Delete: `ui/screens/LoginScreen.kt`, `ui/screens/RegisterScreen.kt` (eski, `com.kitap.app.ui.screens` paketi)
- Modify: `ui/nav/MemberNavHost.kt` (LOGIN ve REGISTER rotaları)
- Test: `app/src/test/java/com/kitap/app/ui/screens/auth/AuthValidatorTest.kt`

**Interfaces:**
- Consumes: `AuthRepository`, `LoginMode`, `ApiResult`, `UserDto`.
- Produces:
  - `object AuthValidator { fun validateLogin(email, password): String?; fun validateRegister(name, email, password, confirm): String? }`
  - `data class AuthFormState(val loading: Boolean = false, val error: String? = null)`
  - `@HiltViewModel class AuthViewModel { val form: StateFlow<AuthFormState>; fun login(email, password, mode: LoginMode); fun register(name, email, password, confirm) }`
  - `@Composable fun AuthTextField(...)`, `AuthError(message)`, `AuthSubmitButton(text, loading, onClick)` (Görev 12 yeniden kullanır)
  - `LoginScreen(form, onSubmit: (String, String) -> Unit, onNavigateToRegister, onForgotPassword)`, `RegisterScreen(form, onSubmit: (name, email, password, confirm) -> Unit, onNavigateToLogin)`

- [ ] **Step 1: Başarısız testi yaz**

`AuthValidatorTest.kt`:

```kotlin
package com.kitap.app.ui.screens.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {
    @Test
    fun loginNeedsBothFields() {
        assertEquals("E-posta ve şifre gerekli.", AuthValidator.validateLogin("", "x"))
        assertEquals("E-posta ve şifre gerekli.", AuthValidator.validateLogin("a@b.com", "  "))
        assertNull(AuthValidator.validateLogin("a@b.com", "x"))
    }

    @Test
    fun registerChecksFieldsInOrder() {
        assertEquals("Ad soyad gerekli.", AuthValidator.validateRegister(" ", "a@b.com", "s", "s"))
        assertEquals("Geçerli bir e-posta adresi girin.", AuthValidator.validateRegister("Ayşe", "abc", "s", "s"))
        assertEquals("Şifre gerekli.", AuthValidator.validateRegister("Ayşe", "a@b.com", "", ""))
        assertEquals("Şifreler eşleşmiyor.", AuthValidator.validateRegister("Ayşe", "a@b.com", "s1", "s2"))
        assertNull(AuthValidator.validateRegister("Ayşe", "a@b.com", "s1", "s1"))
    }
}
```

- [ ] **Step 2: Başarısızlığı doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.ui.screens.auth.AuthValidatorTest"
```

Beklenen: FAIL (`Unresolved reference: AuthValidator`).

- [ ] **Step 3: Uygulamayı yaz**

`ui/screens/auth/AuthValidator.kt`:

```kotlin
package com.kitap.app.ui.screens.auth

object AuthValidator {
    fun validateLogin(email: String, password: String): String? =
        if (email.isBlank() || password.isBlank()) "E-posta ve şifre gerekli." else null

    fun validateRegister(name: String, email: String, password: String, confirm: String): String? = when {
        name.isBlank() -> "Ad soyad gerekli."
        !email.trim().contains('@') -> "Geçerli bir e-posta adresi girin."
        password.isBlank() -> "Şifre gerekli."
        password != confirm -> "Şifreler eşleşmiyor."
        else -> null
    }
}
```

`ui/screens/auth/AuthViewModel.kt`:

```kotlin
package com.kitap.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.UserDto
import com.kitap.app.data.repo.AuthRepository
import com.kitap.app.data.repo.LoginMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthFormState(val loading: Boolean = false, val error: String? = null)

@HiltViewModel
class AuthViewModel @Inject constructor(private val repo: AuthRepository) : ViewModel() {
    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form.asStateFlow()

    fun login(email: String, password: String, mode: LoginMode) {
        val problem = AuthValidator.validateLogin(email, password)
        if (problem != null) {
            _form.value = AuthFormState(error = problem)
            return
        }
        submit { repo.login(email, password, mode) }
    }

    fun register(name: String, email: String, password: String, confirm: String) {
        val problem = AuthValidator.validateRegister(name, email, password, confirm)
        if (problem != null) {
            _form.value = AuthFormState(error = problem)
            return
        }
        submit { repo.register(name, email, password) }
    }

    /** Başarıda oturum durumu değişir ve AppRoot grafiği yeniden kurar; form yalnızca sıfırlanır. */
    private fun submit(call: suspend () -> ApiResult<UserDto>) {
        if (_form.value.loading) return
        _form.value = AuthFormState(loading = true)
        viewModelScope.launch {
            _form.value = when (val r = call()) {
                is ApiResult.Success -> AuthFormState()
                is ApiResult.Failure -> AuthFormState(error = r.message)
            }
        }
    }
}
```

`ui/screens/auth/AuthWidgets.kt`:

```kotlin
package com.kitap.app.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun AuthError(message: String?) {
    if (message == null) return
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 12.dp),
    )
}

@Composable
fun AuthSubmitButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Spacer(Modifier.height(24.dp))
    Button(onClick = onClick, enabled = !loading, modifier = Modifier.fillMaxWidth().height(50.dp)) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(text)
        }
    }
}
```

`ui/screens/auth/LoginScreen.kt`:

```kotlin
package com.kitap.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    form: AuthFormState,
    onSubmit: (email: String, password: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Hoş Geldiniz",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp),
        )
        AuthTextField(email, { email = it }, "E-posta", KeyboardType.Email, enabled = !form.loading)
        Spacer(Modifier.height(16.dp))
        AuthTextField(password, { password = it }, "Şifre", KeyboardType.Password, isPassword = true, enabled = !form.loading)
        AuthError(form.error)
        AuthSubmitButton("Giriş Yap", form.loading) { onSubmit(email, password) }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onForgotPassword) { Text("Şifremi unuttum") }
        TextButton(onClick = onNavigateToRegister) { Text("Hesabınız yok mu? Kayıt olun") }
    }
}
```

`ui/screens/auth/RegisterScreen.kt`:

```kotlin
package com.kitap.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    form: AuthFormState,
    onSubmit: (name: String, email: String, password: String, confirm: String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Hesap Oluştur",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp),
        )
        AuthTextField(name, { name = it }, "Ad Soyad", enabled = !form.loading)
        Spacer(Modifier.height(16.dp))
        AuthTextField(email, { email = it }, "E-posta", KeyboardType.Email, enabled = !form.loading)
        Spacer(Modifier.height(16.dp))
        AuthTextField(password, { password = it }, "Şifre", KeyboardType.Password, isPassword = true, enabled = !form.loading)
        Spacer(Modifier.height(16.dp))
        AuthTextField(confirm, { confirm = it }, "Şifre Tekrar", KeyboardType.Password, isPassword = true, enabled = !form.loading)
        AuthError(form.error)
        AuthSubmitButton("Kayıt Ol", form.loading) { onSubmit(name, email, password, confirm) }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onNavigateToLogin) { Text("Zaten hesabınız var mı? Giriş yapın") }
    }
}
```

Eski dosyaları sil:

```powershell
Remove-Item app\src\main\java\com\kitap\app\ui\screens\LoginScreen.kt, app\src\main\java\com\kitap\app\ui\screens\RegisterScreen.kt
```

- [ ] **Step 4: Rotaları bağla** — `ui/nav/MemberNavHost.kt`

Import'lara ekle:

```kotlin
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.data.repo.LoginMode
import com.kitap.app.ui.screens.auth.AuthViewModel
import com.kitap.app.ui.screens.auth.LoginScreen
import com.kitap.app.ui.screens.auth.RegisterScreen
```

`NavHost` içindeki şu iki satırı:

```kotlin
            placeholder(Routes.LOGIN, "Giriş", back)
            placeholder(Routes.REGISTER, "Kayıt", back)
```

şununla değiştir:

```kotlin
            composable(Routes.LOGIN) {
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                LoginScreen(
                    form = form,
                    onSubmit = { email, password -> vm.login(email, password, LoginMode.MEMBER) },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) { launchSingleTop = true } },
                    onForgotPassword = { navController.navigate(Routes.SIFREMI_UNUTTUM) },
                )
            }
            composable(Routes.REGISTER) {
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                RegisterScreen(
                    form = form,
                    onSubmit = { name, email, password, confirm -> vm.register(name, email, password, confirm) },
                    onNavigateToLogin = { navController.popBackStack() },
                )
            }
```

- [ ] **Step 5: Test ve derleme**

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Beklenen: PASS ve `BUILD SUCCESSFUL`. (Canlı giriş denemesi Görev 13'te backend ile yapılır.)

- [ ] **Step 6: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(auth): gerçek API'ye bağlı Giriş ve Kayıt ekranları"
```

---

### Task 11: Keşfet dikey dilimi

**Files:**
- Create: `data/repo/CoverUrl.kt`, `data/repo/DonationRepository.kt`, `ui/screens/kesfet/KesfetViewModel.kt`, `ui/screens/kesfet/DonationCard.kt`, `ui/screens/kesfet/KesfetScreen.kt`
- Modify: `ui/nav/MemberNavHost.kt` (KESFET rotası)
- Test: `app/src/test/java/com/kitap/app/data/repo/CoverUrlTest.kt`, `DonationRepositoryTest.kt`, `app/src/test/java/com/kitap/app/ui/screens/kesfet/KesfetViewModelTest.kt`

**Interfaces:**
- Consumes: `DonationApi`, `safeResponse`, `ApiResult`, `DonationDto`, `KitapJson`, `Routes.kitapDetay`.
- Produces:
  - `fun resolveCoverUrl(baseUrl: String, cover: String?): String?`
  - `data class PagedResult<T>(val items: List<T>, val total: Long)`, `const val DEFAULT_PAGE_SIZE = 24`
  - `@Singleton class DonationRepository @Inject constructor(api: DonationApi) { suspend fun page(page: Int, size: Int = DEFAULT_PAGE_SIZE, query: String? = null, level: String? = null): ApiResult<PagedResult<DonationDto>> }`
  - `data class KesfetState(items, total, loading, loadingMore, error, endReached)`; `@HiltViewModel class KesfetViewModel(repo) { val state: StateFlow<KesfetState>; fun refresh(); fun loadMore() }`
  - `@Composable fun KesfetScreen(onOpenBook: (Long) -> Unit)`

- [ ] **Step 1: Başarısız testleri yaz**

`CoverUrlTest.kt`:

```kotlin
package com.kitap.app.data.repo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoverUrlTest {
    private val base = "http://10.0.2.2:8080/"

    @Test
    fun nullOrBlankGivesNull() {
        assertNull(resolveCoverUrl(base, null))
        assertNull(resolveCoverUrl(base, "   "))
    }

    @Test
    fun absoluteUrlIsKept() {
        assertEquals("https://cdn.example.com/a.jpg", resolveCoverUrl(base, "https://cdn.example.com/a.jpg"))
    }

    @Test
    fun relativePathIsResolvedAgainstBase() {
        assertEquals("http://10.0.2.2:8080/uploads/covers/a.jpg", resolveCoverUrl(base, "/uploads/covers/a.jpg"))
        assertEquals("http://10.0.2.2:8080/uploads/covers/a.jpg", resolveCoverUrl(base, "uploads/covers/a.jpg"))
    }
}
```

`DonationRepositoryTest.kt`:

```kotlin
package com.kitap.app.data.repo

import com.kitap.app.core.net.ApiMessages
import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.KitapJson
import com.kitap.app.data.api.DonationApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

internal fun donationJson(id: Int) =
    """{"id":$id,"book":{"id":$id,"title":"Kitap $id","author":"Yazar"},"donorName":"Ayşe","quantity":1,"remaining":1}"""

internal fun donationRepository(server: MockWebServer): DonationRepository {
    val retrofit = Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(KitapJson.instance.asConverterFactory("application/json".toMediaType()))
        .build()
    return DonationRepository(retrofit.create(DonationApi::class.java))
}

class DonationRepositoryTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun requestAlwaysCarriesPageSizeAndAvailable() = runTest {
        server.enqueue(MockResponse().setHeader("X-Total-Count", "0").setBody("[]"))
        donationRepository(server).page(0)
        assertEquals("/api/v1/donations?page=0&size=24&available=true", server.takeRequest().path)
    }

    @Test
    fun readsTotalFromHeader() = runTest {
        server.enqueue(MockResponse().setHeader("X-Total-Count", "50").setBody("[${donationJson(1)}]"))
        val r = donationRepository(server).page(0) as ApiResult.Success
        assertEquals(1, r.value.items.size)
        assertEquals(50L, r.value.total)
    }

    @Test
    fun missingHeaderFallsBackToItemCount() = runTest {
        server.enqueue(MockResponse().setBody("[${donationJson(1)},${donationJson(2)}]"))
        val r = donationRepository(server).page(0) as ApiResult.Success
        assertEquals(2L, r.value.total)
    }

    @Test
    fun serverErrorBecomesFailure() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val r = donationRepository(server).page(0)
        assertEquals(ApiResult.Failure(ApiMessages.SERVER_ERROR, 500), r)
    }
}
```

`KesfetViewModelTest.kt` (gerçek zamanlı bekleme için `runBlocking`; `runTest` sanal zamanı gerçek ağ beklerken erken zaman aşımı üretir):

```kotlin
package com.kitap.app.ui.screens.kesfet

import com.kitap.app.core.net.ApiMessages
import com.kitap.app.data.repo.donationJson
import com.kitap.app.data.repo.donationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KesfetViewModelTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun enqueuePage(range: IntRange, total: Int) = server.enqueue(
        MockResponse().setHeader("X-Total-Count", total.toString())
            .setBody(range.joinToString(prefix = "[", postfix = "]") { donationJson(it) })
    )

    private fun newVm() = KesfetViewModel(donationRepository(server))

    private fun awaitIdle(vm: KesfetViewModel) = runBlocking {
        withTimeout(5_000) { vm.state.first { !it.loading && !it.loadingMore } }
    }

    @Test
    fun firstPageLoadsAndIsNotTheEnd() {
        enqueuePage(1..24, total = 30)
        val s = awaitIdle(newVm())
        assertEquals(24, s.items.size)
        assertEquals(30L, s.total)
        assertFalse(s.endReached)
        assertNull(s.error)
    }

    @Test
    fun loadMoreAppendsThenStopsAtTheEnd() {
        enqueuePage(1..24, total = 30)
        val vm = newVm()
        awaitIdle(vm)

        enqueuePage(25..30, total = 30)
        vm.loadMore()
        val s = awaitIdle(vm)
        assertEquals(30, s.items.size)
        assertTrue(s.endReached)

        vm.loadMore()   // bitti: yeni istek atılmamalı
        assertEquals(2, server.requestCount)
    }

    @Test
    fun pagesAreRequestedInOrder() {
        enqueuePage(1..24, total = 30)
        val vm = newVm()
        awaitIdle(vm)
        enqueuePage(25..30, total = 30)
        vm.loadMore()
        awaitIdle(vm)
        assertTrue(server.takeRequest().path!!.contains("page=0"))
        assertTrue(server.takeRequest().path!!.contains("page=1"))
    }

    @Test
    fun errorOnFirstPageIsShownAndRefreshRecovers() {
        server.enqueue(MockResponse().setResponseCode(500))
        val vm = newVm()
        val failed = awaitIdle(vm)
        assertEquals(ApiMessages.SERVER_ERROR, failed.error)
        assertTrue(failed.items.isEmpty())

        vm.loadMore()   // hata varken sessizce yeni sayfa istenmez
        assertEquals(1, server.requestCount)

        enqueuePage(1..3, total = 3)
        vm.refresh()
        val ok = awaitIdle(vm)
        assertNull(ok.error)
        assertEquals(3, ok.items.size)
        assertTrue(ok.endReached)
    }
}
```

- [ ] **Step 2: Başarısızlığı doğrula**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.kitap.app.data.repo.*" --tests "com.kitap.app.ui.screens.kesfet.*"
```

Beklenen: FAIL (`Unresolved reference: resolveCoverUrl`, `DonationRepository`, `KesfetViewModel`).

- [ ] **Step 3: Veri katmanı**

`data/repo/CoverUrl.kt`:

```kotlin
package com.kitap.app.data.repo

/** Sunucu kapak adresi göreli (`/uploads/covers/x.jpg`) ya da mutlak olabilir. */
fun resolveCoverUrl(baseUrl: String, cover: String?): String? {
    val c = cover?.trim().orEmpty()
    if (c.isEmpty()) return null
    if (c.startsWith("http://") || c.startsWith("https://")) return c
    return baseUrl.trimEnd('/') + "/" + c.trimStart('/')
}
```

`data/repo/DonationRepository.kt`:

```kotlin
package com.kitap.app.data.repo

import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeResponse
import com.kitap.app.data.api.DonationApi
import com.kitap.app.data.dto.DonationDto
import javax.inject.Inject
import javax.inject.Singleton

const val DEFAULT_PAGE_SIZE = 24

data class PagedResult<T>(val items: List<T>, val total: Long)

@Singleton
class DonationRepository @Inject constructor(private val api: DonationApi) {
    /** Her zaman `page` ve `size` gönderir (sunucu `page` yoksa tüm listeyi döndürür). Toplam `X-Total-Count`'tan okunur. */
    suspend fun page(
        page: Int,
        size: Int = DEFAULT_PAGE_SIZE,
        query: String? = null,
        level: String? = null,
    ): ApiResult<PagedResult<DonationDto>> =
        when (val r = safeResponse { api.donations(page, size, level, query, true) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> {
                val items = r.value.body().orEmpty()
                val total = r.value.headers()["X-Total-Count"]?.toLongOrNull() ?: items.size.toLong()
                ApiResult.Success(PagedResult(items, total))
            }
        }
}
```

- [ ] **Step 4: ViewModel ve arayüz**

`ui/screens/kesfet/KesfetViewModel.kt`:

```kotlin
package com.kitap.app.ui.screens.kesfet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.DonationDto
import com.kitap.app.data.repo.DonationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KesfetState(
    val items: List<DonationDto> = emptyList(),
    val total: Long = 0,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
)

@HiltViewModel
class KesfetViewModel @Inject constructor(private val repo: DonationRepository) : ViewModel() {
    private val _state = MutableStateFlow(KesfetState(loading = true))
    val state: StateFlow<KesfetState> = _state.asStateFlow()

    private var nextPage = 0
    private var job: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        nextPage = 0
        load(reset = true)
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || s.endReached || s.error != null) return
        load(reset = false)
    }

    private fun load(reset: Boolean) {
        job?.cancel()
        job = viewModelScope.launch {
            _state.update { if (reset) it.copy(loading = true, error = null) else it.copy(loadingMore = true) }
            when (val r = repo.page(nextPage)) {
                is ApiResult.Success -> {
                    nextPage++
                    _state.update { s ->
                        val items = if (reset) r.value.items else s.items + r.value.items
                        s.copy(
                            items = items,
                            total = r.value.total,
                            loading = false,
                            loadingMore = false,
                            error = null,
                            endReached = r.value.items.isEmpty() || items.size >= r.value.total,
                        )
                    }
                }
                is ApiResult.Failure ->
                    _state.update { it.copy(loading = false, loadingMore = false, error = r.message) }
            }
        }
    }
}
```

`ui/screens/kesfet/DonationCard.kt`:

```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

package com.kitap.app.ui.screens.kesfet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kitap.app.data.dto.DonationDto
import com.kitap.app.data.repo.resolveCoverUrl

@Composable
fun DonationCard(donation: DonationDto, coverBaseUrl: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = resolveCoverUrl(coverBaseUrl, donation.book.coverUrl),
                contentDescription = donation.book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(64.dp)
                    .height(92.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    donation.book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
                    color = MaterialTheme.colorScheme.primary,
                )
                donation.point?.let {
                    Text(it.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
```

`ui/screens/kesfet/KesfetScreen.kt`:

```kotlin
package com.kitap.app.ui.screens.kesfet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.BuildConfig

@Composable
fun KesfetScreen(onOpenBook: (Long) -> Unit, viewModel: KesfetViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= listState.layoutInfo.totalItemsCount - 4
        }
    }
    LaunchedEffect(nearEnd, state.items.size) { if (nearEnd) viewModel.loadMore() }

    when {
        state.loading && state.items.isEmpty() ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

        state.error != null && state.items.isEmpty() ->
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = 16.dp)) { Text("Tekrar dene") }
            }

        state.items.isEmpty() ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Şu an açık bağış yok.") }

        else -> LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.items, key = { it.id }) { donation ->
                DonationCard(donation, coverBaseUrl = BuildConfig.API_BASE_URL, onClick = { onOpenBook(donation.id) })
            }
            if (state.loadingMore) {
                item {
                    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            state.error?.let { message ->
                item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::refresh) { Text("Tekrar dene") }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 5: Rotayı bağla** — `ui/nav/MemberNavHost.kt`

Import ekle: `import com.kitap.app.ui.screens.kesfet.KesfetScreen`. Şu satırı:

```kotlin
            composable(Routes.KESFET) { PlaceholderScreen("Keşfet") }
```

şununla değiştir:

```kotlin
            composable(Routes.KESFET) {
                KesfetScreen(onOpenBook = { open(Routes.kitapDetay(it)) })
            }
```

(`PlaceholderScreen` import'u dosyada hâlâ başka yerde kullanılmıyorsa kaldır.)

- [ ] **Step 6: Testler ve derleme**

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Beklenen: tüm birim testleri PASS; `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(kesfet): sayfalı bağış listesi (gerçek API), kapak çözümleme ve kartlar"
```

---

### Task 12: Yönetici Kapısı — sallama, titreşim, Yönetici Girişi

**Files:**
- Create: `ui/screens/auth/AdminLoginScreen.kt`
- Modify: `ui/nav/MemberNavHost.kt`

**Interfaces:**
- Consumes: `ShakeEffect`, `Haptics`, `AuthViewModel.login(..., LoginMode.ADMIN)`, `AuthTextField/AuthError/AuthSubmitButton`, `AdminTheme`, `Routes.ADMIN_LOGIN`.
- Produces: `@Composable fun AdminLoginScreen(form: AuthFormState, onSubmit: (String, String) -> Unit, onBack: () -> Unit)`.

Rol kapısı mantığı (`LoginMode`) Görev 5'te test edildi; bu görev arayüzü ve sensörü bağlar. Sensör bileşeni Görev 8'de test edildi; uçtan uca doğrulama Görev 13'tedir.

- [ ] **Step 1: `AdminLoginScreen.kt`**

```kotlin
package com.kitap.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** `AdminTheme` içinde gösterilir (koyu espresso). Herhangi bir başarısızlıkta genel hata metni gösterilir. */
@Composable
fun AdminLoginScreen(
    form: AuthFormState,
    onSubmit: (email: String, password: String) -> Unit,
    onBack: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Yönetici Girişi",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Yalnızca yetkili hesaplar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp),
        )
        AuthTextField(email, { email = it }, "E-posta", KeyboardType.Email, enabled = !form.loading)
        Spacer(Modifier.height(16.dp))
        AuthTextField(password, { password = it }, "Şifre", KeyboardType.Password, isPassword = true, enabled = !form.loading)
        AuthError(form.error)
        AuthSubmitButton("Yönetici girişi yap", form.loading) { onSubmit(email, password) }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("Vazgeç") }
    }
}
```

- [ ] **Step 2: `MemberNavHost.kt`'yi bağla**

Import'lara ekle:

```kotlin
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.kitap.app.core.device.Haptics
import com.kitap.app.core.device.ShakeEffect
import com.kitap.app.ui.screens.auth.AdminLoginScreen
import com.kitap.app.ui.theme.AdminTheme
```

`AppRootBackGuard(navController)` satırından hemen sonra ekle:

```kotlin
    val context = LocalContext.current
    val haptics = remember { Haptics(context) }
    // Yalnızca oturum kapalıyken (Guest) ve Yönetici Girişi zaten açık değilken dinlenir.
    ShakeEffect(enabled = isGuest && currentRoute != Routes.ADMIN_LOGIN) {
        haptics.shakeBuzz()
        navController.navigate(Routes.ADMIN_LOGIN) { launchSingleTop = true }
    }
```

`NavHost` içindeki şu satırı:

```kotlin
            placeholder(Routes.ADMIN_LOGIN, "Yönetici girişi", back)
```

şununla değiştir:

```kotlin
            composable(Routes.ADMIN_LOGIN) {
                val vm: AuthViewModel = hiltViewModel()
                val form by vm.form.collectAsStateWithLifecycle()
                AdminTheme {
                    AdminLoginScreen(
                        form = form,
                        onSubmit = { email, password -> vm.login(email, password, LoginMode.ADMIN) },
                        onBack = back,
                    )
                }
            }
```

- [ ] **Step 3: Derle ve testleri çalıştır**

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Beklenen: PASS ve `BUILD SUCCESSFUL`. Uçtan uca (sallama → titreşim → ekran; admin/üye reddi; soğuk açılış) Görev 13'te doğrulanır.

- [ ] **Step 4: Commit** (izin varsa)

```powershell
git add app/src
git commit -m "feat(admin): sallama+titreşimle açılan Yönetici Girişi ve rol kapısı bağlantısı"
```

---

### Task 13: Uçtan uca doğrulama (spec §9 kabul kriterleri)

**Files:** yok (kod değişikliği yalnızca doğrulamada çıkan hataları düzeltmek içindir; her düzeltme kendi testiyle ve ayrı commit'le).

**Interfaces:** Consumes: tüm görevler. Produces: kabul kriterlerinin kanıtlı raporu.

Bu görevde **hiçbir başarı iddiası çıktı göstermeden yapılmaz** (superpowers:verification-before-completion). Otomatikleştirilemeyen bir madde varsa raporda "elle doğrulanmadı" diye açıkça yaz.

- [ ] **Step 1: Tam otomatik test paketi**

```powershell
Set-Location C:\Project\kitap\app
.\gradlew.bat clean :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

Beklenen: tüm birim testleri PASS, iki derleme de `BUILD SUCCESSFUL`. (Release imzasız APK üretir; yalnızca derlemenin geçtiğini doğrular.)

- [ ] **Step 2: Emülatör testleri**

Emülatör Görev 9'dan açık değilse aç (Görev 9 Step 6'daki komutlar). Sonra:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Beklenen: `ExitGuardHandlerTest` (2) ve `RootBackGuardTest` (4) PASS.

- [ ] **Step 3: Yerel backend'i başlat** (yalnızca okuma amaçlı çalıştırma; backend kodu değiştirilmez)

JDK 21 gerekli (mevcut). Arka planda çalıştır (Bash aracında `run_in_background`):

```bash
cd /c/Project/kitap/kitap/kitappla && ./mvnw spring-boot:run
```

Hazır olana kadar bekle:

```powershell
do { Start-Sleep 5; try { $ok = (Invoke-WebRequest http://localhost:8080/api/v1/features -UseBasicParsing).StatusCode -eq 200 } catch { $ok = $false } } until ($ok)
```

Dev profilinde H2 dosya veritabanı ve demo veri açıktır: yönetici `admin@kitapla.app` / `admin123`, üyeler `ayse@ornek.com` ve `elif@ornek.com` / `sifre123`. (Backend'in `data/` ve `uploads/` dizinleri değişebilir; bu depoya commit edilmez.) API'yi elle doğrula:

```powershell
(Invoke-WebRequest "http://localhost:8080/api/v1/donations?page=0&size=24&available=true" -UseBasicParsing).Headers["X-Total-Count"]
```

- [ ] **Step 4: ADB yardımcılarını tanımla**

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
function Dump-Ui { & $adb shell uiautomator dump /sdcard/ui.xml | Out-Null; & $adb pull /sdcard/ui.xml "$env:TEMP\ui.xml" | Out-Null; [xml](Get-Content "$env:TEMP\ui.xml" -Raw -Encoding utf8) }
function Has-Text($t) { [bool](Dump-Ui).SelectSingleNode("//node[contains(@text,'$t') or contains(@content-desc,'$t')]") }
function Tap-Node($n) { if ($n.bounds -match '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') { & $adb shell input tap ([int](([int]$matches[1]+[int]$matches[3])/2)) ([int](([int]$matches[2]+[int]$matches[4])/2)) } }
function Tap-Text($t) { $n = (Dump-Ui).SelectSingleNode("//node[contains(@text,'$t') or contains(@content-desc,'$t')]"); if (-not $n) { throw "Bulunamadı: $t" }; Tap-Node $n }
function Type-In($i, $text) { $n = (Dump-Ui).SelectNodes("//node[@class='android.widget.EditText']")[$i]; Tap-Node $n; & $adb shell input text $text }
function Shake { 1..3 | ForEach-Object { & $adb emu sensor set acceleration 0:35:0; Start-Sleep -Milliseconds 100; & $adb emu sensor set acceleration 0:0:9.81; Start-Sleep -Milliseconds 200 } }
function Resumed { (& $adb shell dumpsys activity activities | Select-String "ResumedActivity" | Select-Object -First 1).Line }
```

Klavye düğmeyi örtüyorsa şifre girişinden sonra `& $adb shell input keyevent 66` (Enter) gönder; gerekirse ekranı `input swipe 500 1500 500 500` ile kaydır. Kılavuzlar uyarlanabilir; amaç aşağıdaki gözlemleri kanıtlamaktır.

- [ ] **Step 5: §9.5 Geri tuşu (gerçek MainActivity)**

```powershell
.\gradlew.bat :app:installDebug
& $adb shell pm clear com.kitap.app | Out-Null
& $adb shell am start -n com.kitap.app/.MainActivity; Start-Sleep 5
& $adb shell input keyevent KEYCODE_BACK; Start-Sleep 1
"1. tek geri sonrası: " + (Resumed)          # beklenen: MainActivity hâlâ ön planda
Start-Sleep 3                                 # pencere (2 sn) dolsun
& $adb shell input keyevent KEYCODE_BACK; Start-Sleep 1
"2. pencere sonrası tek geri: " + (Resumed)   # beklenen: hâlâ MainActivity
& $adb shell input keyevent KEYCODE_BACK; Start-Sleep 1
"3. pencere içinde ikinci geri: " + (Resumed) # beklenen: MainActivity DEĞİL (launcher)
```

Beklenen: 1 ve 2'de `com.kitap.app/.MainActivity`; 3'te değil. Ayrıca Keşfet dışı bir sekmede (Misafir olarak İstekler) tek geri Keşfet'e döner (uygulama açık kalır), sonra Keşfet'te tek geri yine çıkarmaz.

- [ ] **Step 6: §9.3 Keşfet ve §9.2 Giriş (üye)**

Backend çalışırken uygulamayı yeniden başlat: `& $adb shell am force-stop com.kitap.app; & $adb shell am start -n com.kitap.app/.MainActivity`.

1. Misafir Keşfet gerçek kitapları listeler: `Has-Text "Kalan:"` → `True` (demo veri).
2. `Tap-Text "Takas"` → Giriş açılır: `Has-Text "Hoş Geldiniz"`.
3. `Type-In 0 "ayse@ornek.com"`, `Type-In 1 "sifre123"`, `Tap-Text "Giriş Yap"` → Takas yer tutucusu açılır (giriş sonrası hedefe dönüş): `Has-Text "Bu sayfa yakında."`; alt çubukta `Panom` görünür.
4. Oturum kalıcılığı: `force-stop` + yeniden başlat → Giriş istemeden alt çubuk görünür, Panom'da `Çıkış yap` var.
5. Giriş sonrası geri: `KEYCODE_BACK` → Giriş ekranına dönmez (Keşfet'e döner).
6. Çıkış: Panom → `Çıkış yap` → Keşfet (misafir); `KEYCODE_BACK` yönetim/üye içeriğine dönmez.

- [ ] **Step 7: §9.4 Yönetici Kapısı**

Oturum kapalıyken (Step 6.6 sonrası):

1. `Shake` → `Has-Text "Yönetici Girişi"` → `True`. Titreşim kanıtı (isteğe bağlı): `& $adb shell dumpsys vibrator_manager | Select-String -Pattern "duration|400"`.
2. Üye hesabı reddi: `ayse@ornek.com` / `sifre123` ile Yönetici Girişi → `Has-Text "E-posta ya da şifre hatalı."`; Guest kalınır.
3. Admin ile Yönetici Girişi: `admin@kitapla.app` / `admin123` → `Has-Text "Yönetim"` ve **alt sekme çubuğu yok** (`Has-Text "İstekler"` → `False`).
4. Soğuk açılış: `force-stop` + başlat → doğrudan Yönetim Pano'su.
5. Admin çıkış → Keşfet (misafir). Normal Giriş'te admin reddi: Takas → Giriş → `admin@kitapla.app` / `admin123` → `Has-Text "E-posta ya da şifre hatalı."`.
6. Oturum açıkken sallama etkisiz: üye olarak giriş yap, `Shake` → `Has-Text "Yönetici Girişi"` → `False`.

- [ ] **Step 8: §9.6 Tüm rotalar çökmeden açılır**

Üye olarak Panom'daki her girişi aç, `KEYCODE_BACK` ile dön; admin olarak Yönetim Pano'sundaki 6 girişi aynı şekilde dolaş. Sonra:

```powershell
& $adb shell pidof com.kitap.app
& $adb logcat -d -s AndroidRuntime:E | Select-String "com.kitap.app"
```

Beklenen: süreç yaşıyor (`pidof` bir PID döner) ve `FATAL EXCEPTION` yok.

- [ ] **Step 9: Temizlik ve rapor**

```powershell
& $adb emu kill
Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

Kullanıcıya raporla: her kabul kriteri için kanıt (komut çıktısı), otomatikleştirilemeyenler, plandan sapmalar (ör. JDK 17 kurulduysa, yazı tipi indirilemediyse), backend'in `data/`/`uploads/` dizinlerinin değiştiği bilgisi. Doğrulamada bir hata çıkarsa: `superpowers:systematic-debugging` ile kök nedeni bul, testini yaz, düzelt, ayrı commit at.

- [ ] **Step 10: Commit** (izin varsa; yalnızca düzeltme yapıldıysa)

---

## Spec Kapsam Tablosu

| Spec | Görev |
|---|---|
| §1.1 Gradle/izinler/gradlew | 1, 2 |
| §1.2, §8 Tema | 6 |
| §1.3, §4 Ağ katmanı (çerez, CSRF, ApiError, sayfalama) | 3, 4, 5, 11 |
| §1.4, §5 Navigasyon (tüm rotalar, üye/yönetim grafiği, yığın temizliği, misafir yönlendirme) | 9 |
| §1.5 Giriş/Kayıt dilimi | 10 |
| §1.5 Keşfet dilimi | 11 |
| §1.6, §6 Yönetici Kapısı (sallama, titreşim, giriş kuralları, soğuk açılış) | 5, 8, 12, 13 |
| §1.7, §7 Geri tuşu güvencesi (ExitGuard, splash dahil, predictive back, yığın temizliği) | 2 (manifest), 7, 9, 13 |
| §3 Oturum durumu (`Loading/Guest/Member/Admin`) | 5, 9 |
| §9 Kabul kriterleri 1–6 | 13 |
| §10 Riskler (sabitler tek yerde, `page` zorunlu) | 8 (`ShakeConfig`), 11 (`DonationRepository`) |

## Yürütme Notları

- Görevler sıralıdır; her görev bir öncekinin çıktısına dayanır (Görev 7 ve 8 birbirinden bağımsızdır, ama Görev 9'dan önce bitmelidir).
- Bir kod parçası sürüm farkı yüzünden derlenmezse (ör. bir Compose/Material3 API adı), **davranışı ve testi koruyarak** en küçük değişiklikle düzelt ve raporla; sürümleri yükseltme.
- Emülatör/JDK/SDK kurulumu (Görev 1) makineye kalıcı yazılım kurar; indirmeler başarısız olursa dur ve kullanıcıya sor.
