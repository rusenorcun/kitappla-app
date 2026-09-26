package com.kitappla.app.ui.screens.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.SubcomposeAsyncImage
import com.kitappla.app.BuildConfig
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.BookMetadataDto
import com.kitappla.app.data.repo.BookRepository
import com.kitappla.app.data.repo.UploadRepository
import com.kitappla.app.data.repo.resolveCoverUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Bağış, istek ve takas formlarında ortak kitap bilgileri: link, kapak, ad ve yazar. */
data class BookFields(
    val title: String = "",
    val author: String = "",
    val purchaseLink: String = "",
    val coverUrl: String = "",
    val uploadingCover: Boolean = false,
    val fetchingPreview: Boolean = false,
    /** Linkten getirme ya da kapak yükleme hakkında son bilgi notu (ör. "Bu linkten kitap bilgisi alınamadı"). */
    val notice: String? = null,
    /** Gönder'e basılınca açılır; zorunlu alan hataları kullanıcı yazarken değil, ancak o zaman gösterilir. */
    val showErrors: Boolean = false,
) {
    val titleError: String? get() = if (showErrors && title.isBlank()) TITLE_REQUIRED else null
    val authorError: String? get() = if (showErrors && author.isBlank()) AUTHOR_REQUIRED else null

    /** Gönder düğmesinin yanında gösterilen özet; alan düzeltilince kendiliğinden kalkar. */
    val validationMessage: String? get() = if (showErrors) firstProblem() else null

    /** Kapak yüklenirken ya da link okunurken gönderilmez: yarım bilgiyle kayıt açılmasın. */
    val busy: Boolean get() = uploadingCover || fetchingPreview

    /**
     * Yazar da zorunludur: web'de olduğu gibi aynı kitabın ad + yazarla tek kayıt olması beklenir; yazarsız gönderim
     * sunucuda "beklenmeyen hata" ile sonuçlanıyordu.
     */
    fun firstProblem(): String? = when {
        title.isBlank() -> TITLE_REQUIRED
        author.isBlank() -> AUTHOR_REQUIRED
        else -> null
    }

    companion object {
        const val TITLE_REQUIRED = "Kitap adı boş bırakılamaz."
        const val AUTHOR_REQUIRED = "Yazar adı boş bırakılamaz."
        const val LINK_NOT_FOUND = "Bu linkten kitap bilgisi alınamadı. Kitap adını ve yazarı elle girebilirsiniz."
        const val AUTHOR_AND_COVER_MISSING = "Linkten yazar ve kapak alınamadı; yazarı elle girin, isterseniz kapak yükleyin."
        const val AUTHOR_MISSING = "Linkten yazar bilgisi alınamadı; lütfen yazarı elle girin."
        const val COVER_MISSING = "Linkten kapak görseli alınamadı; isterseniz kendiniz yükleyebilirsiniz."
        const val COVER_PREVIEW_FAILED = "Kapak görseli önizlenemedi. İsterseniz başka bir görsel yükleyin."
    }
}

/**
 * "Getir" sonucunu forma işler. Kullanıcının yazdığı ya da yüklediği hiçbir şeyin üzerine yazılmaz (web'deki gibi,
 * seçilen kapak dosyası linkteki kapaktan önce gelir); yalnızca boş alanlar doldurulur. Eksik kalan bilgi için not düşer.
 */
fun BookFields.withMetadata(meta: BookMetadataDto): BookFields {
    val title = meta.title?.trim().orEmpty()
    val author = meta.author?.trim().orEmpty()
    val cover = meta.coverUrl?.trim().orEmpty()
    if (!meta.found && title.isEmpty() && author.isEmpty() && cover.isEmpty()) {
        return copy(notice = BookFields.LINK_NOT_FOUND)
    }
    val merged = copy(
        title = this.title.ifBlank { title },
        author = this.author.ifBlank { author },
        coverUrl = coverUrl.ifBlank { cover },
    )
    val notice = when {
        merged.author.isBlank() && merged.coverUrl.isBlank() -> BookFields.AUTHOR_AND_COVER_MISSING
        merged.author.isBlank() -> BookFields.AUTHOR_MISSING
        merged.coverUrl.isBlank() -> BookFields.COVER_MISSING
        else -> null
    }
    return merged.copy(notice = notice)
}

/**
 * Kitap formu ekranlarının ortak ViewModel tabanı: linkten getirme, kapak yükleme ve zorunlu alan denetimi. Ekrana
 * özgü alanlar (açıklama, adet, not…) alt sınıfın kendi durumundadır.
 */
abstract class BookFormViewModel(
    private val bookRepository: BookRepository,
    private val uploadRepository: UploadRepository,
) : ViewModel() {

    private val _book = MutableStateFlow(BookFields())
    val book: StateFlow<BookFields> = _book.asStateFlow()

    fun updateTitle(v: String) = _book.update { it.copy(title = v) }
    fun updateAuthor(v: String) = _book.update { it.copy(author = v) }
    fun updatePurchaseLink(v: String) = _book.update { it.copy(purchaseLink = v, notice = null) }
    fun removeCover() = _book.update { it.copy(coverUrl = "", notice = null) }

    fun uploadCover(uri: Uri) {
        viewModelScope.launch {
            _book.update { it.copy(uploadingCover = true, notice = null) }
            when (val r = uploadRepository.uploadImage(uri)) {
                is ApiResult.Failure -> _book.update { it.copy(uploadingCover = false, notice = r.message) }
                is ApiResult.Success -> _book.update { it.copy(uploadingCover = false, coverUrl = r.value.url) }
            }
        }
    }

    fun fetchPreview() {
        val current = _book.value
        val link = current.purchaseLink.trim()
        if (link.isBlank() || current.fetchingPreview) return

        viewModelScope.launch {
            _book.update { it.copy(fetchingPreview = true, notice = null) }
            when (val r = bookRepository.preview(link)) {
                is ApiResult.Failure -> _book.update { it.copy(fetchingPreview = false, notice = r.message) }
                is ApiResult.Success -> {
                    _book.update { it.copy(fetchingPreview = false).withMetadata(r.value) }
                    onMetadata(r.value)
                }
            }
        }
    }

    /** Linkten gelen, forma özgü bilgiler için (ör. boşsa açıklama alanını doldurmak). */
    protected open fun onMetadata(meta: BookMetadataDto) {}

    /** Zorunlu alanları denetler; eksik varsa alan hatalarını açar ve `false` döner. */
    protected fun validateBook(): Boolean {
        if (_book.value.firstProblem() == null) return true
        _book.update { it.copy(showErrors = true) }
        return false
    }
}

/**
 * Linkten getirme, kapak önizleme/yükleme, kitap adı ve yazar alanları. [linkLabel] ekrana göre değişir
 * ("Kitap Linki (D&R, Kitapyurdu vb.)" gibi).
 */
@Composable
fun BookFormFields(
    book: BookFields,
    onPurchaseLinkChange: (String) -> Unit,
    onFetchPreview: () -> Unit,
    onUploadCover: (Uri) -> Unit,
    onRemoveCover: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    linkLabel: String = "Kitap Linki (İsteğe bağlı)",
) {
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onUploadCover)
    }
    val pickCover = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.background,
        unfocusedContainerColor = MaterialTheme.colorScheme.background,
        disabledContainerColor = MaterialTheme.colorScheme.background,
    )

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Satın alma linki (otomatik doldurma)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = book.purchaseLink,
                    onValueChange = onPurchaseLinkChange,
                    label = { FieldLabel(linkLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onFetchPreview() }),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = onFetchPreview,
                    enabled = book.purchaseLink.isNotBlank() && !book.fetchingPreview,
                    contentPadding = CompactButtonPadding,
                    modifier = Modifier.height(54.dp),
                ) {
                    if (book.fetchingPreview) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        ButtonLabel("Getir", fontSize = 12.sp)
                    }
                }
            }
            if (book.fetchingPreview) {
                FormNote("Linkten bilgiler alınıyor…")
            }
            book.notice?.let { FormNote(it) }
        }

        CoverSection(book, onPickCover = pickCover, onRemoveCover = onRemoveCover)

        OutlinedTextField(
            value = book.title,
            onValueChange = onTitleChange,
            label = { FieldLabel("Kitap Adı *") },
            singleLine = true,
            isError = book.titleError != null,
            supportingText = book.titleError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = book.author,
            onValueChange = onAuthorChange,
            label = { FieldLabel("Yazar *") },
            singleLine = true,
            isError = book.authorError != null,
            supportingText = book.authorError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FormNote(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Kitap formu ekranlarındaki alan etiketi (çerçeve çizgisinin üstünde zemin rengiyle okunaklı durur). */
@Composable
fun FieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 4.dp),
    )
}

@Composable
private fun CoverSection(book: BookFields, onPickCover: () -> Unit, onRemoveCover: () -> Unit) {
    val coverUrl = resolveCoverUrl(BuildConfig.API_BASE_URL, book.coverUrl)
    when {
        book.uploadingCover -> Card(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text("Görsel yükleniyor...", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        coverUrl != null -> Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SubcomposeAsyncImage(
                    model = coverUrl,
                    contentDescription = "Kitap kapağı önizleme",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(160.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    },
                    error = {
                        // Görsel açılamazsa boş bir kutu değil, ne olduğu görünsün.
                        Column(
                            Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                        ) {
                            Icon(
                                Icons.Outlined.BrokenImage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(28.dp),
                            )
                            Text(
                                BookFields.COVER_PREVIEW_FAILED,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    },
                )
                ActionButtonsRow {
                    OutlinedButton(
                        onClick = onPickCover,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = CompactButtonPadding,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        ButtonLabel("Görseli Değiştir", fontSize = 12.sp)
                    }
                    TextButton(onClick = onRemoveCover, contentPadding = CompactButtonPadding) {
                        Text("Kaldır", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        else -> OutlinedButton(
            onClick = onPickCover,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Kitap Kapağı Görseli Yükle (İsteğe bağlı)", textAlign = TextAlign.Center)
        }
    }
}
