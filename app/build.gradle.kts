import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// İmza bilgileri depoda tutulmaz: <proje>/keystore.properties (storeFile, storePassword, keyAlias, keyPassword).
// Dosya yoksa release APK imzasız üretilir (build kırılmaz); kurulabilir APK için dosya gerekir.
val keystoreProps = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

// Debug sürümün API adresi. Varsayılan emülatörün bilgisayara açılan takma adıdır (10.0.2.2); gerçek telefonda bu adres
// hiçbir yere gitmez ("Bağlantı kurulamadı"). Telefonda denemek için <proje>/local.properties içine (git'e girmez) ya da
// komut satırına şunlardan biri yazılır:
//   apiBaseUrl=https://www.kitappla.com/      (gerçek sunucu)
//   apiBaseUrl=http://localhost:8080/          (bilgisayardaki yerel sunucu; önce `adb reverse tcp:8080 tcp:8080`)
// Düz HTTP yalnızca 10.0.2.2 ve localhost için açıktır (debug/res/xml/network_security_config.xml).
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val debugApiBaseUrl: String = ((project.findProperty("apiBaseUrl") as String?) ?: localProps.getProperty("apiBaseUrl"))
    ?.trim()
    ?.also {
        require(Regex("https?://\\S+/").matches(it)) {
            "apiBaseUrl 'http://' ya da 'https://' ile başlamalı ve '/' ile bitmeli (verilen: $it)"
        }
    }
    ?: "http://10.0.2.2:8080/"

android {
    namespace = "com.kitappla.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kitappla.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (keystoreProps.getProperty("storeFile") != null) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Alan adı 301 ile www'ya yönleniyor; OkHttp yönlendirmede POST'u GET'e çevirdiği için
            // taban adres doğrudan www olmalı (aksi hâlde giriş/kayıt/yazma işlemleri bozulur).
            buildConfigField("String", "API_BASE_URL", "\"https://www.kitappla.com/\"")
            signingConfigs.findByName("release")?.let { signingConfig = it }
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
