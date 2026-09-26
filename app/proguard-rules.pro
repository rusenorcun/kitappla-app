# ==============================================================================
# KitAppLa ProGuard / R8 Yapılandırması
# ==============================================================================

# Genel Özellikler ve Hata Bastırma
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature,SourceFile,LineNumberTable
-dontwarn javax.annotation.**
-dontwarn com.google.errorprone.annotations.**

# ------------------------------------------------------------------------------
# Kotlinx Serialization
# ------------------------------------------------------------------------------
-dontnote kotlinx.serialization.**
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class **$serializer {
    public static final ** INSTANCE;
}
-keep @kotlinx.serialization.Serializable class com.kitappla.app.data.dto.** { *; }
-keepclassmembers class com.kitappla.app.data.dto.** {
    <fields>;
    <init>(...);
}

# ------------------------------------------------------------------------------
# Retrofit & OkHttp
# ------------------------------------------------------------------------------
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface com.kitappla.app.data.api.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ------------------------------------------------------------------------------
# Kotlin Coroutines
# ------------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ------------------------------------------------------------------------------
# AndroidX Security Crypto (EncryptedSharedPreferences)
# ------------------------------------------------------------------------------
-keep class androidx.security.crypto.** { *; }

# ------------------------------------------------------------------------------
# Coil (Resim Yükleme)
# ------------------------------------------------------------------------------
-dontwarn coil.**
-keep class coil.** { *; }
