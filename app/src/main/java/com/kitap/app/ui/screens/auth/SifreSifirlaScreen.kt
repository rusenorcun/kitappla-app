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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SifreSifirlaScreen(
    form: AuthFormState,
    onSubmit: (token: String, newPass: String, confirmPass: String, onSuccess: () -> Unit) -> Unit,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    var token by rememberSaveable { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Şifre Belirle", fontWeight = FontWeight.Bold) },
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
                text = "Şifrenizi Sıfırlayın",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "E-postanıza iletilen sıfırlama kodunu ve yeni şifrenizi girin.",
                style = MaterialTheme.typography.bodyMedium,
                color = KahveSoluk,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            AuthTextField(
                value = token,
                onValueChange = { token = it },
                label = "Sıfırlama Kodu (Token)",
                enabled = !form.loading,
            )

            Spacer(Modifier.height(14.dp))

            AuthTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Yeni Şifre",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                enabled = !form.loading,
            )

            Spacer(Modifier.height(14.dp))

            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Yeni Şifre (Tekrar)",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                enabled = !form.loading,
            )

            AuthError(form.error)

            AuthSubmitButton("Şifreyi Güncelle", form.loading) {
                onSubmit(token, newPassword, confirmPassword, onSuccess)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SifreSifirlaScreenPreview() {
    KitapTheme {
        Surface {
            SifreSifirlaScreen(
                form = AuthFormState(),
                onSubmit = { _, _, _, _ -> },
                onSuccess = {},
                onBack = {},
            )
        }
    }
}
