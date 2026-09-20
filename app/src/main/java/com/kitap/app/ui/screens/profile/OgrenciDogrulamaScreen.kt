package com.kitap.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.ui.screens.common.StudentPriorityCard
import com.kitap.app.ui.screens.common.SuccessBadge
import com.kitap.app.ui.theme.AdacayiMurekkep
import com.kitap.app.ui.theme.AdacayiSoft
import com.kitap.app.ui.theme.KahveSoluk
import com.kitap.app.ui.theme.KitapTheme

@Composable
fun OgrenciDogrulamaScreen(
    onBack: () -> Unit,
    viewModel: OgrenciDogrulamaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Bağlantı bilgisayarda/tarayıcıda onaylanıp uygulamaya dönüldüğünde durum güncellensin.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    OgrenciDogrulamaContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEmailChange = viewModel::updateEmail,
        onTokenChange = viewModel::updateToken,
        onSendVerification = viewModel::sendVerification,
        onConfirmToken = viewModel::confirmToken,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OgrenciDogrulamaContent(
    state: OgrenciDogrulamaState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onSendVerification: () -> Unit,
    onConfirmToken: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Öğrenci Doğrulama", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Öğrenci Öncelik Espresso Kartı
            StudentPriorityCard()

            if (state.isConfirmed) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(54.dp))
                        Text("Öğrenci Durumunuz Onaylandı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Tüm öğrenci önceliklerinden ve kotalarından yararlanabilirsiniz.", style = MaterialTheme.typography.bodySmall, color = KahveSoluk)
                    }
                }
            } else {
                // E-posta ile Doğrulama Formu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Okul E-postanızı Girin", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            "Üniversitenizin size tahsis ettiği .edu.tr uzantılı e-posta adresinizi girin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KahveSoluk,
                        )

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = { Text("Öğrenci E-postası (ornek@universite.edu.tr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Button(
                            onClick = onSendVerification,
                            enabled = state.email.isNotBlank() && !state.sendingEmail,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            if (state.sendingEmail) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Outlined.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Doğrulama Bağlantısı Gönder")
                            }
                        }
                    }
                }

                // Onay Kodu Formu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Doğrulama Bağlantısı ile Onayla", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "E-postanızdaki doğrulama bağlantısını (ya da bağlantıdaki kodu) buraya yapıştırın; tekrar giriş yapmanız gerekmez.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KahveSoluk,
                        )

                        OutlinedTextField(
                            value = state.token,
                            onValueChange = onTokenChange,
                            label = { Text("Doğrulama bağlantısı veya kodu") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Button(
                            onClick = onConfirmToken,
                            enabled = state.token.isNotBlank() && !state.confirmingToken,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            if (state.confirmingToken) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Onayla")
                            }
                        }
                    }
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OgrenciDogrulamaScreenPreview() {
    KitapTheme {
        Surface {
            OgrenciDogrulamaContent(
                state = OgrenciDogrulamaState(
                    email = "ogrenci@boun.edu.tr",
                    isConfirmed = false,
                ),
                onBack = {},
                onEmailChange = {},
                onTokenChange = {},
                onSendVerification = {},
                onConfirmToken = {},
            )
        }
    }
}
