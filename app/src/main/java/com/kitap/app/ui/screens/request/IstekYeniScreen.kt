package com.kitap.app.ui.screens.request

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitap.app.ui.theme.KitapTheme

@Composable
fun IstekYeniScreen(
    onBack: () -> Unit,
    onSuccess: (Long) -> Unit,
    viewModel: IstekYeniViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    IstekYeniContent(
        state = state,
        onBack = onBack,
        onTitleChange = viewModel::updateTitle,
        onAuthorChange = viewModel::updateAuthor,
        onPurchaseLinkChange = viewModel::updatePurchaseLink,
        onFetchPreview = viewModel::fetchPreview,
        onDescriptionChange = viewModel::updateDescription,
        onSubmit = { viewModel.submit(onSuccess) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IstekYeniContent(
    state: IstekYeniState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onPurchaseLinkChange: (String) -> Unit,
    onFetchPreview: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İstek Oluştur", fontWeight = FontWeight.Bold) },
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
                text = "Aradığınız kitabı belirtin; başka bir üye veya bağışçı bu kitabı karşılayabilir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.purchaseLink,
                    onValueChange = onPurchaseLinkChange,
                    label = { Text("Kitap Linki (İsteğe bağlı)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = onFetchPreview,
                    enabled = state.purchaseLink.isNotBlank() && !state.fetchingPreview,
                    modifier = Modifier.height(54.dp),
                ) {
                    if (state.fetchingPreview) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Getir", fontSize = 12.sp)
                    }
                }
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Kitap Adı *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.author,
                onValueChange = onAuthorChange,
                label = { Text("Yazar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Neden bu kitaba ihtiyacınız var? (Açıklama)") },
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
                enabled = state.title.isNotBlank() && !state.submitting,
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("İsteği Paylaş")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IstekYeniScreenPreview() {
    KitapTheme {
        Surface {
            IstekYeniContent(
                state = IstekYeniState(
                    title = "Algoritmalar: Teori ve Uygulama",
                    author = "Thomas H. Cormen",
                ),
                onBack = {},
                onTitleChange = {},
                onAuthorChange = {},
                onPurchaseLinkChange = {},
                onFetchPreview = {},
                onDescriptionChange = {},
                onSubmit = {},
            )
        }
    }
}
