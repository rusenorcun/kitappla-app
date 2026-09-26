package com.kitappla.app.ui.screens.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitappla.app.core.document.DocumentRenderer
import com.kitappla.app.core.document.RenderedDocument
import com.kitappla.app.ui.screens.common.EmptyStateView
import com.kitappla.app.ui.screens.common.ErrorStateView
import com.kitappla.app.ui.theme.EspressoZemin

/**
 * Öğrenci belgesi (web: /admin/belge/{id}). PDF sayfaları ve görseller uygulama içinde gösterilir; iki parmakla
 * yakınlaştırılır, çift dokunuşla yakınlaştırılır/sıfırlanır.
 */
@Composable
fun AdminBelgeGoruntuleScreen(onBack: () -> Unit, viewModel: AdminBelgeGoruntuleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AdminBelgeGoruntuleContent(state = state, onBack = onBack, onRetry = viewModel::load)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBelgeGoruntuleContent(state: AdminBelgeState, onBack: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Öğrenci belgesi", fontWeight = FontWeight.ExtraBold)
                        if (state.userName.isNotBlank()) {
                            Text(
                                state.userName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EspressoZemin),
            )
        },
        containerColor = EspressoZemin,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val document = state.document
            when {
                state.loading -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text("Belge yükleniyor…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.error != null -> ErrorStateView(
                    message = state.error,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                document is RenderedDocument.Pages -> ZoomablePages(document)
                document is RenderedDocument.Text -> SelectionContainer {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Text(
                            document.text,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                else -> EmptyStateView(
                    title = "Önizleme yok",
                    message = "Bu belge türü uygulamada gösterilemiyor. Web yönetim panelinden açabilirsin.",
                    icon = Icons.Outlined.Description,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

/** Sayfalar alt alta; iki parmakla yakınlaştırma, yakınken kaydırma, çift dokunuşla 2,5× / sıfırla. */
@OptIn(ExperimentalFoundationApi::class)   // transformable(canPan = …)
@Composable
private fun ZoomablePages(document: RenderedDocument.Pages) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    fun clamp(o: Offset, s: Float): Offset {
        val maxX = (s - 1f) * size.width / 2f
        val maxY = (s - 1f) * size.height / 2f
        return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
    }

    val transform = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
        offset = if (scale == MIN_ZOOM) Offset.Zero else clamp(offset + pan, scale)
    }

    Box(
        Modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (scale > MIN_ZOOM) {
                        scale = MIN_ZOOM
                        offset = Offset.Zero
                    } else {
                        scale = DOUBLE_TAP_ZOOM
                    }
                })
            }
            // Yakın değilken sürükleme listeyi kaydırır; yakınken sayfayı gezdirir.
            .transformable(transform, canPan = { scale > MIN_ZOOM })
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(document.pages) { index, page ->
                val image = remember(page) { page.asImageBitmap() }
                Image(
                    bitmap = image,
                    contentDescription = if (document.pages.size > 1) "Belge sayfası ${index + 1}" else "Öğrenci belgesi",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White),
                )
            }
            item {
                Text(
                    text = buildString {
                        if (document.truncated) append("İlk ${DocumentRenderer.MAX_PAGES} sayfa gösteriliyor. ")
                        append("İki parmakla yakınlaştır; çift dokunuşla yakınlaştır ya da sıfırla.")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 4f
private const val DOUBLE_TAP_ZOOM = 2.5f
