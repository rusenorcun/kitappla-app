package com.kitappla.app.ui.screens.swap

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.ui.screens.common.BookFields
import com.kitappla.app.ui.screens.common.BookFormFields
import com.kitappla.app.ui.screens.common.FieldLabel
import com.kitappla.app.ui.theme.KitapplaTheme

@Composable
fun TakasKitapEkleScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: TakasKitapEkleViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val book by viewModel.book.collectAsStateWithLifecycle()

    TakasKitapEkleContent(
        state = state,
        book = book,
        onBack = onBack,
        onTitleChange = viewModel::updateTitle,
        onAuthorChange = viewModel::updateAuthor,
        onPurchaseLinkChange = viewModel::updatePurchaseLink,
        onFetchPreview = viewModel::fetchPreview,
        onUploadCover = viewModel::uploadCover,
        onRemoveCover = viewModel::removeCover,
        onNoteChange = viewModel::updateNote,
        onSubmit = { viewModel.submit(onSuccess) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakasKitapEkleContent(
    state: TakasKitapEkleState,
    book: BookFields,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onPurchaseLinkChange: (String) -> Unit,
    onFetchPreview: () -> Unit,
    onUploadCover: (Uri) -> Unit,
    onRemoveCover: () -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takasa Kitap Ekle", fontWeight = FontWeight.Bold) },
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Alışveriş linkini yapıştırarak başlık ve kapağı otomatik getirebilir ya da bilgileri elle " +
                    "doldurabilirsiniz. Takasa açtığınız kitaplar diğer üyelere görünür.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )

            BookFormFields(
                book = book,
                onPurchaseLinkChange = onPurchaseLinkChange,
                onFetchPreview = onFetchPreview,
                onUploadCover = onUploadCover,
                onRemoveCover = onRemoveCover,
                onTitleChange = onTitleChange,
                onAuthorChange = onAuthorChange,
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { FieldLabel("Takas Notu (İsteğe bağlı)") },
                placeholder = { Text("Karşılığında ne istersiniz? Örn. distopya / klasik") },
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            (book.validationMessage ?: state.error)?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSubmit,
                enabled = !state.submitting && !book.busy,
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Takasa Aç")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TakasKitapEkleScreenPreview() {
    KitapplaTheme {
        Surface {
            TakasKitapEkleContent(
                state = TakasKitapEkleState(note = "Roman olur"),
                book = BookFields(title = "Kozmos", author = "Carl Sagan"),
                onBack = {},
                onTitleChange = {},
                onAuthorChange = {},
                onPurchaseLinkChange = {},
                onFetchPreview = {},
                onUploadCover = {},
                onRemoveCover = {},
                onNoteChange = {},
                onSubmit = {},
            )
        }
    }
}
