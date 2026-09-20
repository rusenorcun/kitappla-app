package com.kitap.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitap.app.ui.theme.KahveSoluk
import com.kitap.app.ui.theme.KitapTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SifremiUnuttumScreen(
    form: AuthFormState,
    onSubmit: (email: String, onSent: (String) -> Unit) -> Unit,
    onNavigateToReset: () -> Unit,
    onBack: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Şifremi Unuttum", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Şifre Sıfırlama",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Hesabınıza bağlı e-posta adresinizi girin. Size bir şifre sıfırlama kodu göndereceğiz.",
                style = MaterialTheme.typography.bodyMedium,
                color = KahveSoluk,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "E-posta Adresi",
                keyboardType = KeyboardType.Email,
                enabled = !form.loading,
            )

            AuthError(form.error)

            AuthSubmitButton("Sıfırlama Bağlantısı Gönder", form.loading) {
                onSubmit(email) { msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onNavigateToReset) {
                Text("Zaten sıfırlama kodum var")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SifremiUnuttumScreenPreview() {
    KitapTheme {
        Surface {
            SifremiUnuttumScreen(
                form = AuthFormState(),
                onSubmit = { _, _ -> },
                onNavigateToReset = {},
                onBack = {},
            )
        }
    }
}
