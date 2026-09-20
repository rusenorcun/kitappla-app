package com.kitap.app.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.ui.theme.KitapTheme

@Composable
fun SikayetEtScreen(
    onBack: () -> Unit,
    viewModel: SikayetEtViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SikayetEtContent(
        state = state,
        onBack = onBack,
        onReasonChange = viewModel::updateReason,
        onNoteChange = viewModel::updateNote,
        onSubmit = { viewModel.submit(onBack) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SikayetEtContent(
    state: SikayetEtState,
    onBack: () -> Unit,
    onReasonChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val reasons = listOf(
        "YANILTICI" to "Yanıltıcı / Gerçeğe aykırı bilgi",
        "UYGUNSUZ_ICERIK" to "Uygunsuz / Rahatsız edici içerik",
        "GELMEDI" to "Buluşmaya gelmedi / İletişim koptu",
        "HAKARET" to "Hakaret / Saygısız tutum",
        "DIGER" to "Diğer kural ihlali",
    )
    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Şikâyet Bildir", fontWeight = FontWeight.Bold) },
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
            Text(
                text = "Topluluk kurallarımıza aykırı bir durumla karşılaştıysanız lütfen gerekçeyi seçip açıklayınız. Bildiriminiz moderatörlerimiz tarafından incelenecektir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = reasons.find { it.first == state.reason }?.second ?: "Gerekçe Seçin",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Şikâyet Gerekçesi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                ) {
                    reasons.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onReasonChange(code)
                                dropdownExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text("Açıklama (Detay verin)") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onSubmit,
                enabled = !state.submitting,
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Şikâyeti Gönder")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SikayetEtScreenPreview() {
    KitapTheme {
        Surface {
            SikayetEtContent(
                state = SikayetEtState(kind = "DONATION", refId = 1L),
                onBack = {},
                onReasonChange = {},
                onNoteChange = {},
                onSubmit = {},
            )
        }
    }
}
