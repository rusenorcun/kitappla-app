package com.kitappla.app.ui.screens.common

import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.pow

/**
 * Yukarıdan çekip yenileme. [loading], ekranın ViewModel'indeki "yükleniyor" bayrağıdır: [onRefresh] çağrılınca true
 * olup iş bitince false olmalı; gösterge false'a dönünce kapanır.
 *
 * Çekme hareketi içeriğin kaydırılmasıyla tetiklenir. [scrollableContent] `true` ise içerik kendi kaydırılabilir
 * listesidir (LazyColumn vb.); `false` ise (yükleniyor/boş/hata gibi kaydırılamayan görünümler) içerik, çekme
 * hareketi yine çalışsın diye ekranı dolduran kaydırılabilir bir kabın içine konur. [content] bir [BoxScope]'tur
 * (`Modifier.align(...)` kullanılabilir).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRefresh(
    loading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    scrollableContent: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val threshold = with(LocalDensity.current) { PullToRefreshDefaults.PositionalThreshold.toPx() }
    val pullState = remember(scope, threshold) { PullRefreshState(scope, threshold) }
    val isLoading by rememberUpdatedState(loading)
    val refresh by rememberUpdatedState(onRefresh)

    if (pullState.isRefreshing) {
        LaunchedEffect(pullState) {
            refresh()
            // Yükleme bayrağı true olup false'a dönene kadar göstergeyi tut. Bayrak hiç true olmazsa (istek atılmadıysa)
            // gösterge takılı kalmasın diye beklemeyi kısa tut; yanıt hiç gelmezse de üst sınırda kapat.
            withTimeoutOrNull(1_000) { snapshotFlow { isLoading }.first { it } }
            withTimeoutOrNull(MAX_REFRESH_MILLIS) { snapshotFlow { isLoading }.first { !it } }
            pullState.endRefresh()
        }
    }

    Box(
        modifier
            .nestedScroll(pullState.nestedScrollConnection)
            // Kaydırma iptal edilince (içerik değişip liste kaldırıldığında vb.) bırakma olayı gelmez; parmak kalkınca
            // yarım kalan gösterge burada toplanır. Olaylar tüketilmez, içerik dokunuşları olduğu gibi alır.
            .pointerInput(pullState) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                    } while (event.changes.any { it.pressed })
                    pullState.onGestureEnd()
                }
            },
    ) {
        if (scrollableContent) {
            Box(Modifier.fillMaxSize()) { content() }
        } else {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                // propagateMinConstraints: içerik fillMaxSize/ortalama isterse görünür alanı doldursun.
                Box(
                    Modifier.fillMaxWidth().heightIn(min = maxHeight).verticalScroll(rememberScrollState()),
                    propagateMinConstraints = true,
                ) { content() }
            }
        }
        PullToRefreshContainer(state = pullState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

/** Sunucu hiç yanıt vermese de gösterge en geç bu sürede kapanır (ağ çağrısı üst sınırı 10 sn). */
private const val MAX_REFRESH_MILLIS = 10_000L

/** M3 ile aynı his: parmağın kat ettiği yolun yarısı kadar iner. */
private const val DRAG_MULTIPLIER = 0.5f

/**
 * Çekip yenileme durumu. M3 1.2.0'daki `PullToRefreshState` bırakınca geri çekilme animasyonunu kaydırma hareketinin
 * (fling) eşyordamında çalıştırıyordu: bu sırada ekrana yeniden dokunulunca ya da içerik değişince eşyordam iptal
 * oluyor, gösterge yarı açık "takılı" kalıyordu. Burada animasyonlar ekranın kapsamında ([scope]) çalışır; yeni bir
 * çekme onları kendisi durdurur. Görünüm için M3'ün [PullToRefreshContainer]'ı aynen kullanılır.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class PullRefreshState(
    private val scope: CoroutineScope,
    override val positionalThreshold: Float,
) : PullToRefreshState {

    private var distancePulled by mutableFloatStateOf(0f)
    private var offset by mutableFloatStateOf(0f)
    private var refreshing by mutableStateOf(false)
    private var settleJob: Job? = null

    private val adjustedDistance: Float get() = distancePulled * DRAG_MULTIPLIER

    override val progress: Float get() = adjustedDistance / positionalThreshold
    override val verticalOffset: Float get() = offset
    override val isRefreshing: Boolean get() = refreshing

    override fun startRefresh() {
        refreshing = true
        distancePulled = 0f
        animateOffsetTo(positionalThreshold)
    }

    override fun endRefresh() {
        refreshing = false
        distancePulled = 0f
        animateOffsetTo(0f)
    }

    override var nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = when {
            // Yukarı kaydırma önce çekilmiş göstergeyi geri toplar.
            source == NestedScrollSource.Drag && available.y < 0f -> Offset(0f, pull(available.y))
            else -> Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = when {
            // Liste en üstteyken artan aşağı çekme göstergeyi indirir.
            source == NestedScrollSource.Drag && available.y > 0f -> Offset(0f, pull(available.y))
            else -> Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity = Velocity(0f, release(available.y))
    }

    /** Parmak hareketini göstergeye uygular; tüketilen miktarı döndürür. */
    internal fun pull(delta: Float): Float {
        if (refreshing) return 0f
        if (delta < 0f && distancePulled <= 0f && offset <= 0f) return 0f
        if (settleJob?.isActive == true) {
            // Geri çekilirken yeniden tutuldu: gösterge olduğu yerden devam etsin, sıçramasın.
            settleJob?.cancel()
            distancePulled = offset / DRAG_MULTIPLIER
        }
        val newDistance = (distancePulled + delta).coerceAtLeast(0f)
        val consumed = newDistance - distancePulled
        distancePulled = newDistance
        offset = calculateOffset()
        return consumed
    }

    /** Parmak kalktı: eşik aşıldıysa yenileme başlar, aşılmadıysa gösterge gizlenir. Tüketilen hızı döndürür. */
    internal fun release(velocity: Float): Float {
        if (refreshing) return 0f
        val pulled = distancePulled
        if (pulled == 0f && offset == 0f) return 0f
        if (adjustedDistance > positionalThreshold) startRefresh() else animateOffsetTo(0f)
        distancePulled = 0f
        // Gösterge çekiliyken aşağı fırlatma tüketilir; yukarı fırlatma listeye kalır.
        return if (pulled == 0f || velocity < 0f) 0f else velocity
    }

    /**
     * Dokunuş bitti (tüm parmaklar kalktı ya da hareket iptal oldu). Bırakma olayı kaydırma zincirinden gelmediyse
     * gösterge burada toparlanır; geldiyse (animasyon sürüyor ya da yenileme başladıysa) bir şey yapılmaz.
     */
    internal fun onGestureEnd() {
        scope.launch {
            // Bırakma olayı aynı karede işlenir; ona fırsat ver.
            withFrameNanos { }
            if (!refreshing && settleJob?.isActive != true && (distancePulled > 0f || offset > 0f)) release(0f)
        }
    }

    private fun animateOffsetTo(target: Float) {
        settleJob?.cancel()
        settleJob = scope.launch {
            try {
                animate(initialValue = offset, targetValue = target) { value, _ -> offset = value }
            } finally {
                offset = target
            }
        }
    }

    /** M3 ile aynı: eşiğe kadar doğrusal, sonrası giderek sertleşen gerilim (en çok eşiğin %75'i kadar taşma). */
    private fun calculateOffset(): Float = when {
        adjustedDistance <= positionalThreshold -> adjustedDistance
        else -> {
            val overshootPercent = abs(progress) - 1f
            val linearTension = overshootPercent.coerceIn(0f, 2f)
            val tensionPercent = linearTension - linearTension.pow(2) / 4
            positionalThreshold + positionalThreshold * tensionPercent
        }
    }
}
