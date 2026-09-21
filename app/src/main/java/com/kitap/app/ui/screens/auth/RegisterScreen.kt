package com.kitap.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitap.app.R
import com.kitap.app.data.dto.School
import com.kitap.app.ui.theme.KitapTheme
import com.kitap.app.ui.theme.titleLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    form: AuthFormState,
    onSubmit: (name: String, email: String, password: String, confirm: String, school: String, phone: String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var school by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var schoolExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "KitAppLa Logo",
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
            )
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "KitAppLa",
                    style = MaterialTheme.typography.titleLogo,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Bir kitap bir öğrenciye",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Hesap Oluştur",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 32.dp),
            )
            AuthTextField(name, { name = it }, "Ad Soyad", enabled = !form.loading)
            Spacer(Modifier.height(16.dp))
            AuthTextField(email, { email = it }, "E-posta", KeyboardType.Email, enabled = !form.loading)
            Spacer(Modifier.height(16.dp))
            AuthTextField(password, { password = it }, "Şifre", KeyboardType.Password, isPassword = true, enabled = !form.loading)
            Spacer(Modifier.height(16.dp))
            AuthTextField(confirm, { confirm = it }, "Şifre Tekrar", KeyboardType.Password, isPassword = true, enabled = !form.loading)
            Spacer(Modifier.height(16.dp))

            // Okul Seçimi Dropdown
            ExposedDropdownMenuBox(
                expanded = schoolExpanded,
                onExpandedChange = { if (!form.loading) schoolExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = School.of(school)?.label.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Okul / Üniversite") },
                    placeholder = { Text("Okulunuzu seçin") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolExpanded) },
                    enabled = !form.loading,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = schoolExpanded,
                    onDismissRequest = { schoolExpanded = false },
                ) {
                    School.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.label) },
                            onClick = {
                                school = item.name
                                schoolExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            AuthTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Telefon (İsteğe bağlı)",
                keyboardType = KeyboardType.Phone,
                enabled = !form.loading,
            )

            AuthError(form.error)
            AuthSubmitButton("Kayıt Ol", form.loading) { onSubmit(name, email, password, confirm, school, phone) }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onNavigateToLogin) { Text("Zaten hesabınız var mı? Giriş yapın") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    KitapTheme {
        Surface {
            RegisterScreen(
                form = AuthFormState(),
                onSubmit = { _, _, _, _, _, _ -> },
                onNavigateToLogin = {},
            )
        }
    }
}
