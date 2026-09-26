package com.kitappla.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.AnnotatedString
import com.kitappla.app.ui.screens.common.AlertBox
import com.kitappla.app.ui.screens.common.FlowHeader
import com.kitappla.app.ui.screens.common.HeroTone
import com.kitappla.app.ui.screens.common.StatusHero
import com.kitappla.app.ui.screens.common.StudentPriorityCard
import com.kitappla.app.ui.theme.Adacayi
import com.kitappla.app.ui.theme.AdacayiSoft
import com.kitappla.app.ui.theme.KahveSoluk
import com.kitappla.app.ui.theme.Vurgu
import com.kitappla.app.ui.theme.VurguSoft

@Composable
fun OgrenciDogrulamaScreen(
    onBack: () -> Unit,
    onExplore: () -> Unit = {},
    viewModel: OgrenciDogrulamaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
        onExplore = onExplore,
        onEmailChange = viewModel::updateEmail,
        onTokenChange = viewModel::updateToken,
        onSendVerification = viewModel::sendVerification,
        onConfirmToken = viewModel::confirmToken,
        onSchoolLevelChange = viewModel::updateSchoolLevel,
        onDocumentNoChange = viewModel::updateDocumentNo,
        onDocumentUriChange = viewModel::updateDocumentUri,
        onUploadDocument = viewModel::uploadDocument,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OgrenciDogrulamaContent(
    state: OgrenciDogrulamaState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onExplore: () -> Unit = {},
    onEmailChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onSendVerification: () -> Unit,
    onConfirmToken: () -> Unit,
    onSchoolLevelChange: (String) -> Unit,
    onDocumentNoChange: (String) -> Unit,
    onDocumentUriChange: (Uri?) -> Unit,
    onUploadDocument: () -> Unit,
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            onDocumentUriChange(uri)
        }
    }

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
            // Onayın sonucu (bağlantıdan açılışta sürerken de) en üstte.
            LinkResultPanel(state, onExplore)

            // Öğrenci Öncelik Bilgilendirme Kartı (kutlama panelinde zaten gösterildiyse tekrarlanmaz)
            if (!state.justConfirmed) StudentPriorityCard()

            if (state.justConfirmed) {
                // Sonuç yukarıda; onaylı öğrenci için formlar gösterilmez.
            } else if (state.isConfirmed) {
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
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Adacayi, modifier = Modifier.size(54.dp))
                        Text("Zaten Onaylı Bir Öğrencisin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Tüm öğrenci önceliklerinden ve yüksek kotalardan yararlanıyorsun.", style = MaterialTheme.typography.bodySmall, color = KahveSoluk)
                    }
                }
            } else {
                // 1. Yöntem: Okul E-postası ile Doğrulama Formu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("1. Yöntem: Okul E-postası (.edu.tr)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            "Okulunuzun verdiği .edu.tr uzantılı e-posta adresinize gönderilen bağlantıyı onaylayın.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KahveSoluk,
                        )

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = {
                                Text(
                                    text = "Okul E-postası (ornek@universite.edu.tr)",
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = fieldColors,
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

                // E-posta Doğrulama Jetonu Yapıştırma Kartı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Gelen Bağlantıyı Onayla", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "E-postanıza gelen bağlantıyı kopyalayıp buraya yapıştırabilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KahveSoluk,
                        )

                        OutlinedTextField(
                            value = state.token,
                            onValueChange = onTokenChange,
                            label = {
                                Text(
                                    text = "Bağlantı veya Jeton Kodu",
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
                                )
                            },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        OutlinedButton(
                            onClick = onConfirmToken,
                            enabled = state.token.isNotBlank() && !state.confirmingToken,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            if (state.confirmingToken) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Bağlantıyı Onayla")
                            }
                        }
                    }
                }

                // 2. Yöntem: Öğrenci Belgesi ile Doğrulama (Lise / Üniversite Belge Yükleme)
                if (state.user?.hasDocument == true && state.user.studentStatus.equals("PENDING", ignoreCase = true)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Outlined.HourglassEmpty, contentDescription = null, tint = Vurgu, modifier = Modifier.size(32.dp))
                            Column {
                                Text("Öğrenci Belgeniz İncelemede", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Yöneticilerimiz belgenizi kontrol ettikten sonra onaylayacaktır.", style = MaterialTheme.typography.bodySmall, color = KahveSoluk)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("2. Yöntem: Öğrenci Belgesi Yükle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                "Okul e-postanız yoksa lise veya üniversite öğrenci belgenizi (PDF veya görsel) yükleyerek başvurabilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KahveSoluk,
                            )

                            // Okul Seviyesi Seçimi
                            Text("Okul Seviyesi", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = state.schoolLevel == "LISE",
                                    onClick = { onSchoolLevelChange("LISE") },
                                    label = { Text("Lise") },
                                )
                                FilterChip(
                                    selected = state.schoolLevel == "UNIVERSITE",
                                    onClick = { onSchoolLevelChange("UNIVERSITE") },
                                    label = { Text("Üniversite") },
                                )
                            }

                            // Belge Numarası
                            OutlinedTextField(
                                value = state.documentNo,
                                onValueChange = onDocumentNoChange,
                                label = {
                                    Text(
                                        text = "Öğrenci Belge Numarası *",
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp),
                                    )
                                },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            // Belge Dosyası Seçimi
                            OutlinedButton(
                                onClick = {
                                    documentPickerLauncher.launch("*/*")
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                            ) {
                                Icon(Icons.Outlined.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (state.documentUri != null) "Belge Seçildi ✓" else "Belge Seç (PDF / Görsel)",
                                    fontSize = 13.sp,
                                )
                            }

                            if (state.documentUri != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = Adacayi, modifier = Modifier.size(16.dp))
                                    Text("Belge dosyası hazır", style = MaterialTheme.typography.bodySmall, color = Adacayi)
                                }
                            }

                            Button(
                                onClick = onUploadDocument,
                                enabled = state.documentUri != null && state.documentNo.isNotBlank() && !state.uploadingDocument,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                            ) {
                                if (state.uploadingDocument) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Yönetici Onayına Gönder")
                                }
                            }
                        }
                    }
                }
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

/**
 * Jetonla yapılan onayın sonucu: bağlantıdan açılışta sürüyor, başarılı (kutlama) ya da başarısız (yeni bağlantı iste).
 * Gösterilecek bir şey yoksa boş kalır.
 */
@Composable
private fun LinkResultPanel(state: OgrenciDogrulamaState, onExplore: () -> Unit) {
    when {
        state.openedFromLink && state.confirmingToken -> Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.5.dp)
                Text("Okul e-postan doğrulanıyor…", style = MaterialTheme.typography.titleSmall)
            }
        }
        state.justConfirmed -> Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatusHero(Icons.Outlined.School, HeroTone.SUCCESS)
            FlowHeader(
                eyebrow = "Öğrenci doğrulandı",
                title = "Okul e-postan onaylandı",
                body = AnnotatedString("Bağışlarda öğrenci önceliğin artık açık. Kitaplar seni bekliyor."),
                tone = HeroTone.SUCCESS,
            )
            StudentPriorityCard(title = "Öğrenci önceliğin açıldı")
            Button(onClick = onExplore, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("Keşfet'e göz at", fontWeight = FontWeight.Bold)
            }
        }
        state.linkError != null -> AlertBox(
            message = state.linkError + " Aşağıdan okul adresine yeni bir doğrulama bağlantısı isteyebilirsin.",
            isError = true,
        )
    }
}
