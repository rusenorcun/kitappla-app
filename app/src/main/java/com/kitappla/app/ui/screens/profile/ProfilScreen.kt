package com.kitappla.app.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.data.dto.School
import com.kitappla.app.data.dto.UserDto
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.screens.common.UserAvatar
import com.kitappla.app.ui.theme.KahveSoluk
import com.kitappla.app.ui.theme.KitapplaTheme

@Composable
fun ProfilScreen(
    onBack: () -> Unit,
    onPasswordChanged: () -> Unit = {},
    viewModel: ProfilViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.passwordChanged) { if (state.passwordChanged) onPasswordChanged() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    ProfilContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onNameChange = viewModel::updateName,
        onSchoolChange = viewModel::updateSchool,
        onAddressChange = viewModel::updateAddress,
        onPhoneChange = viewModel::updatePhone,
        onCurrentPasswordChange = viewModel::updateCurrentPassword,
        onNewPasswordChange = viewModel::updateNewPassword,
        onConfirmPasswordChange = viewModel::updateConfirmPassword,
        onSaveProfile = viewModel::saveProfile,
        onChangePassword = viewModel::changePassword,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilContent(
    state: ProfilState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onSchoolChange: (String) -> Unit,
    onAddressChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profil Bilgileri", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        when {
            state.loading && state.user == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null && state.user == null -> ErrorStateView(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier.padding(padding),
            )
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Üst Kullanıcı Bilgisi
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            UserAvatar(initials = state.user?.initials ?: "K", size = 48)
                            Column {
                                Text(
                                    text = state.user?.name.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = state.user?.email.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KahveSoluk,
                                )
                            }
                        }
                    }

                    // Kişisel Bilgiler Formu
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Kişisel Bilgiler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = state.name,
                                onValueChange = onNameChange,
                                label = {
                                    Text(
                                        text = "Ad Soyad",
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
                                    )
                                },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            var schoolExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = schoolExpanded,
                                onExpandedChange = { if (!state.savingProfile) schoolExpanded = it },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                OutlinedTextField(
                                    value = School.of(state.school)?.label ?: state.school,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = {
                                        Text(
                                            text = "Okul / Üniversite",
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
                                        )
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolExpanded) },
                                    enabled = !state.savingProfile,
                                    colors = fieldColors,
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
                                                onSchoolChange(item.name)
                                                schoolExpanded = false
                                            },
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = state.phone,
                                onValueChange = onPhoneChange,
                                label = {
                                    Text(
                                        text = "Telefon (Buluşma için isteğe bağlı)",
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                enabled = !state.savingProfile,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Button(
                                onClick = onSaveProfile,
                                enabled = !state.savingProfile,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                            ) {
                                if (state.savingProfile) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Bilgileri Kaydet")
                                }
                            }
                        }
                    }

                    // Şifre Değiştirme Formu
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Şifre Değiştir", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = state.currentPassword,
                                onValueChange = onCurrentPasswordChange,
                                label = {
                                    Text(
                                        text = "Mevcut Şifre",
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
                                    )
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            OutlinedTextField(
                                value = state.newPassword,
                                onValueChange = onNewPasswordChange,
                                label = {
                                    Text(
                                        text = "Yeni Şifre",
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
                                    )
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            OutlinedTextField(
                                value = state.confirmPassword,
                                onValueChange = onConfirmPasswordChange,
                                label = {
                                    Text(
                                        text = "Yeni Şifre (Tekrar)",
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
                                    )
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Button(
                                onClick = onChangePassword,
                                enabled = !state.changingPassword,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                            ) {
                                if (state.changingPassword) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Şifreyi Güncelle")
                                }
                            }
                        }
                    }

                    // Google Play Hesap Silme Politikası: Hesap Yönetimi ve Silme Talebi
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "Hesap Yönetimi & Veri Silme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "KitAppLa hesabınızı ve ilişkili kişisel verilerinizi silmek veya dondurmak için talep oluşturabilirsiniz. Başvurunuz gizlilik ilkelerimiz çerçevesinde incelenir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp,
                            )
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                                Text("Hesap Silme Talebi İlet")
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                title = { Text("Hesap Silme Talebi") },
                text = {
                    Text(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        text = "KitAppLa hesabınızı ve verilerinizi silmek istediğinizde mevcut işlemleriniz incelenir. Kitap bağış/takas işlem güvenliği ve topluluk kuralları gereği talep yönetici tarafından işleme alınır.\n\nDevam etmek için destek ekibimize e-posta ile talep iletebilirsiniz."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            val userEmail = state.user?.email.orEmpty()
                            val userName = state.user?.name.orEmpty()
                            val userId = state.user?.id ?: 0L
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:destek@kitappla.com")
                                putExtra(Intent.EXTRA_SUBJECT, "KitAppLa Hesap Silme Talebi - $userEmail")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Merhaba KitAppLa Ekibi,\n\n$userEmail e-posta adresine bağlı KitAppLa hesabımın ve ilişkili kişisel verilerimin silinmesini talep ediyorum.\n\nKullanıcı ID: $userId\nAd Soyad: $userName"
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Hesap Silme Talebi Gönder"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text("Talebi Gönder")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Vazgeç")
                    }
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilScreenPreview() {
    KitapplaTheme {
        Surface {
            ProfilContent(
                state = ProfilState(
                    loading = false,
                    user = UserDto(id = 1L, name = "Ayşe Demir", email = "ayse@ornek.com", initials = "AD"),
                    name = "Ayşe Demir",
                    school = "Boğaziçi Üniversitesi",
                ),
                onBack = {},
                onNameChange = {},
                onSchoolChange = {},
                onAddressChange = {},
                onPhoneChange = {},
                onCurrentPasswordChange = {},
                onNewPasswordChange = {},
                onConfirmPasswordChange = {},
                onSaveProfile = {},
                onChangePassword = {},
                onRetry = {},
            )
        }
    }
}
