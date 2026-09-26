package com.kitappla.app.ui.screens.donation

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.data.dto.PickupPointDto
import com.kitappla.app.ui.screens.common.BookFields
import com.kitappla.app.ui.screens.common.BookFormFields
import com.kitappla.app.ui.theme.KitapplaTheme

@Composable
fun BagisYeniScreen(
    onBack: () -> Unit,
    onSuccess: (Long) -> Unit,
    viewModel: BagisYeniViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val book by viewModel.book.collectAsStateWithLifecycle()

    BagisYeniContent(
        state = state,
        book = book,
        onBack = onBack,
        onTitleChange = viewModel::updateTitle,
        onAuthorChange = viewModel::updateAuthor,
        onPurchaseLinkChange = viewModel::updatePurchaseLink,
        onFetchPreview = viewModel::fetchPreview,
        onUploadCover = viewModel::uploadCover,
        onRemoveCover = viewModel::removeCover,
        onQuantityChange = viewModel::updateQuantity,
        onTargetLevelChange = viewModel::updateTargetLevel,
        onPointIdChange = viewModel::updatePointId,
        onPointNoteChange = viewModel::updatePointNote,
        onDescriptionChange = viewModel::updateDescription,
        onSubmit = { viewModel.submit(onSuccess) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagisYeniContent(
    state: BagisYeniState,
    book: BookFields,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onPurchaseLinkChange: (String) -> Unit,
    onFetchPreview: () -> Unit,
    onUploadCover: (Uri) -> Unit = {},
    onRemoveCover: () -> Unit = {},
    onQuantityChange: (Int) -> Unit,
    onTargetLevelChange: (String) -> Unit,
    onPointIdChange: (Long?) -> Unit,
    onPointNoteChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val levels = listOf(
        "HEPSI" to "Tüm Öğrenim Seviyeleri",
        "ILKOKUL" to "İlkokul",
        "ORTAOKUL" to "Ortaokul",
        "LISE" to "Lise",
        "UNIVERSITE" to "Üniversite",
    )
    var levelDropdownExpanded by remember { mutableStateOf(false) }
    var pointDropdownExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.background,
        unfocusedContainerColor = MaterialTheme.colorScheme.background,
        disabledContainerColor = MaterialTheme.colorScheme.background,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bağış Yap", fontWeight = FontWeight.Bold) },
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
            BookFormFields(
                book = book,
                onPurchaseLinkChange = onPurchaseLinkChange,
                onFetchPreview = onFetchPreview,
                onUploadCover = onUploadCover,
                onRemoveCover = onRemoveCover,
                onTitleChange = onTitleChange,
                onAuthorChange = onAuthorChange,
                linkLabel = "Kitap Linki (D&R, Kitapyurdu vb.)",
            )

            // Adet Seçici
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Adet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Bağışlamak istediğiniz kitap sayısı", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledIconButton(
                        onClick = { onQuantityChange(state.quantity - 1) },
                        enabled = state.quantity > 1,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Azalt")
                    }
                    Text(
                        text = state.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    FilledIconButton(
                        onClick = { onQuantityChange(state.quantity + 1) },
                        enabled = state.quantity < 99,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Artır")
                    }
                }
            }

            // Hedef Seviye Dropdown
            ExposedDropdownMenuBox(
                expanded = levelDropdownExpanded,
                onExpandedChange = { levelDropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = levels.find { it.first == state.targetLevel }?.second ?: "Tüm Seviyeler",
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(
                            text = "Hedef Öğrenci Seviyesi",
                            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 4.dp),
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelDropdownExpanded) },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = levelDropdownExpanded,
                    onDismissRequest = { levelDropdownExpanded = false },
                ) {
                    levels.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onTargetLevelChange(code)
                                levelDropdownExpanded = false
                            },
                        )
                    }
                }
            }

            // Buluşma Yeri Seçimi (Teslim Noktası Dropdown)
            ExposedDropdownMenuBox(
                expanded = pointDropdownExpanded,
                onExpandedChange = { pointDropdownExpanded = it },
            ) {
                val selectedPointName = state.pickupPoints.find { it.id == state.pointId }?.name
                    ?: if (state.loadingPoints) "Buluşma noktaları yükleniyor..." else "Buluşma yeri seçin *"

                OutlinedTextField(
                    value = selectedPointName,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(
                            text = "Buluşma Yeri (Kampüs Teslim Noktası) *",
                            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 4.dp),
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pointDropdownExpanded) },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = pointDropdownExpanded,
                    onDismissRequest = { pointDropdownExpanded = false },
                ) {
                    state.pickupPoints.forEach { point ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(point.name, fontWeight = FontWeight.SemiBold)
                                    if (!point.description.isNullOrBlank()) {
                                        Text(point.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            },
                            onClick = {
                                onPointIdChange(point.id)
                                pointDropdownExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.pointNote,
                onValueChange = onPointNoteChange,
                label = {
                    Text(
                        text = "Ek Konum Tarifi / Özel Yer",
                        modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 4.dp),
                    )
                },
                placeholder = { Text("Listede yoksa yaz: örn. Fen Fakültesi kantini") },
                supportingText = { Text("Bu bir öneri; alıcıyla mesajlaşıp birlikte değiştirebilirsiniz.") },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = {
                    Text(
                        text = "Kitap Durumu ve Açıklama",
                        modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 4.dp),
                    )
                },
                minLines = 3,
                colors = fieldColors,
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
                    Text("Bağışı Yayınla")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BagisYeniScreenPreview() {
    KitapplaTheme {
        Surface {
            BagisYeniContent(
                state = BagisYeniState(
                    quantity = 2,
                    pickupPoints = listOf(PickupPointDto(1L, "Kadıköy Merkez Kütüphanesi", "Giriş kapısı önü")),
                    pointId = 1L,
                ),
                book = BookFields(title = "Körlük", author = "José Saramago"),
                onBack = {},
                onTitleChange = {},
                onAuthorChange = {},
                onPurchaseLinkChange = {},
                onFetchPreview = {},
                onUploadCover = {},
                onRemoveCover = {},
                onQuantityChange = {},
                onTargetLevelChange = {},
                onPointIdChange = {},
                onDescriptionChange = {},
                onSubmit = {},
            )
        }
    }
}
