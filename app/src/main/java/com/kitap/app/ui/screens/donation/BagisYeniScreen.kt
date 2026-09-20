package com.kitap.app.ui.screens.donation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kitap.app.data.dto.PickupPointDto
import com.kitap.app.ui.theme.KitapTheme

@Composable
fun BagisYeniScreen(
    onBack: () -> Unit,
    onSuccess: (Long) -> Unit,
    viewModel: BagisYeniViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BagisYeniContent(
        state = state,
        onBack = onBack,
        onTitleChange = viewModel::updateTitle,
        onAuthorChange = viewModel::updateAuthor,
        onPurchaseLinkChange = viewModel::updatePurchaseLink,
        onFetchPreview = viewModel::fetchPreview,
        onCoverUrlChange = viewModel::updateCoverUrl,
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
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onPurchaseLinkChange: (String) -> Unit,
    onFetchPreview: () -> Unit,
    onCoverUrlChange: (String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onTargetLevelChange: (String) -> Unit,
    onPointIdChange: (Long?) -> Unit,
    onPointNoteChange: (String) -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Satın Alma Linki (Otomatik doldurma)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.purchaseLink,
                    onValueChange = onPurchaseLinkChange,
                    label = { Text("Kitap Linki (D&R, Kitapyurdu vb.)") },
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

            if (state.coverUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .height(140.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = state.coverUrl,
                        contentDescription = "Kitap Kapağı",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                    )
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
                    label = { Text("Hedef Öğrenci Seviyesi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelDropdownExpanded) },
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

            // Teslim Noktası Dropdown
            if (state.pickupPoints.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = pointDropdownExpanded,
                    onExpandedChange = { pointDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = state.pickupPoints.find { it.id == state.pointId }?.name ?: "Nokta Seçin",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Teslim Edilecek Kampüs Noktası") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pointDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = pointDropdownExpanded,
                        onDismissRequest = { pointDropdownExpanded = false },
                    ) {
                        state.pickupPoints.forEach { point ->
                            DropdownMenuItem(
                                text = { Text(point.name) },
                                onClick = {
                                    onPointIdChange(point.id)
                                    pointDropdownExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.pointNote,
                onValueChange = onPointNoteChange,
                label = { Text("Teslim Notu (Buluşma yeri detayı vb.)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Kitap Durumu ve Açıklama") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSubmit,
                enabled = state.title.isNotBlank() && !state.submitting,
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
    KitapTheme {
        Surface {
            BagisYeniContent(
                state = BagisYeniState(
                    title = "Körlük",
                    author = "José Saramago",
                    quantity = 2,
                    pickupPoints = listOf(PickupPointDto(1L, "Kadıköy Merkez Kütüphanesi")),
                    pointId = 1L,
                ),
                onBack = {},
                onTitleChange = {},
                onAuthorChange = {},
                onPurchaseLinkChange = {},
                onFetchPreview = {},
                onCoverUrlChange = {},
                onQuantityChange = {},
                onTargetLevelChange = {},
                onPointIdChange = {},
                onPointNoteChange = {},
                onDescriptionChange = {},
                onSubmit = {},
            )
        }
    }
}
