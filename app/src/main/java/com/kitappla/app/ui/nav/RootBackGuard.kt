package com.kitappla.app.ui.nav

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kitappla.app.core.nav.ExitGuardHandler

const val EXIT_HINT = "Çıkmak için tekrar geri tuşuna basın"

/**
 * Geri yığınında geri gidilecek yer kalmadıysa (kök hedef) `true`. Henüz giriş yoksa (grafik değişiminden sonraki
 * ilk kareler) kök sayılır: koruma kapalı kalırsa NavController'ın kendi geri işleyicisi de kapalıdır ve tek geri
 * basışı Activity'yi bitirirdi.
 */
@Composable
fun rememberIsAtRoot(navController: NavHostController): Boolean {
    val entry by navController.currentBackStackEntryAsState()   // yeniden birleşim için gözlenir
    return entry == null || navController.previousBackStackEntry == null
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
