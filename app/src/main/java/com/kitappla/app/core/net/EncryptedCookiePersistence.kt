package com.kitappla.app.core.net

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie

class EncryptedCookiePersistence(private val context: Context) : CookiePersistence {
    // Depo hiç açılamazsa null kalır: çerezler yalnızca bellekte tutulur, uygulama çökmez.
    private val prefs: SharedPreferences? = runCatching { open() }.getOrNull()

    private fun open(): SharedPreferences = try {
        create()
    } catch (e: Exception) {
        // Bozulmuş Keystore/dosya: depoyu silip temiz başla (kullanıcı yeniden giriş yapar).
        wipe()
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

    /** Okunamayan (çözülemeyen) değer çökme değil, boş liste demektir; bozuk depo silinir. */
    override fun load(): List<Cookie> = try {
        prefs?.getString(KEY, null)?.let { CookieCodec.decode(it) }.orEmpty()
    } catch (e: Exception) {
        wipe()
        emptyList()
    }

    /** Yazma hatası yutulur: bellekteki liste geçerli kalır, bozuk depo silinir. */
    override fun save(cookies: List<Cookie>) {
        try {
            prefs?.edit()?.putString(KEY, CookieCodec.encode(cookies))?.apply()
        } catch (e: Exception) {
            wipe()
        }
    }

    private fun wipe() {
        runCatching { context.deleteSharedPreferences(FILE) }
    }

    private companion object {
        const val FILE = "kitappla_session"
        const val KEY = "cookies"
    }
}
