package com.kitappla.app.ui.screens.common

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PullRefreshStateTest {

    /** Her karede 16 ms ilerleyen sahte saat: animasyonlar gerçek zaman beklemeden biter. */
    private class StepClock : MonotonicFrameClock {
        private var now = 0L
        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            yield()
            now += 16_000_000L
            return onFrame(now)
        }
    }

    private val threshold = 200f

    private fun TestScope.newState() = PullRefreshState(CoroutineScope(coroutineContext + StepClock()), threshold)

    /** Liste en üstteyken aşağı çekme (sürükleme payı yarıya indirilir: 100 px sürükleme = 50 px gösterge). */
    private fun PullRefreshState.dragDown(px: Float) =
        nestedScrollConnection.onPostScroll(Offset.Zero, Offset(0f, px), NestedScrollSource.Drag)

    @Test
    fun releasingBelowTheThresholdHidesTheIndicator() = runTest {
        val state = newState()
        state.dragDown(100f)
        assertEquals(50f, state.verticalOffset)

        state.nestedScrollConnection.onPreFling(Velocity.Zero)
        advanceUntilIdle()

        assertEquals(0f, state.verticalOffset)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun releasingPastTheThresholdRefreshesUntilEnded() = runTest {
        val state = newState()
        state.dragDown(500f)

        state.nestedScrollConnection.onPreFling(Velocity.Zero)
        advanceUntilIdle()
        assertTrue(state.isRefreshing)
        assertEquals(threshold, state.verticalOffset)

        state.endRefresh()
        advanceUntilIdle()
        assertFalse(state.isRefreshing)
        assertEquals(0f, state.verticalOffset)
    }

    @Test
    fun indicatorStillHidesWhenTheFlingIsInterruptedByANewTouch() = runTest {
        val state = newState()
        state.dragDown(120f)

        // Bırakma, kaydırmanın fling eşyordamında gelir; ekrana yeniden dokunulunca o eşyordam iptal olur.
        val fling = launch(start = CoroutineStart.UNDISPATCHED) {
            state.nestedScrollConnection.onPreFling(Velocity.Zero)
            awaitCancellation()
        }
        fling.cancel()
        advanceUntilIdle()

        assertEquals("gösterge yarı açık kalmamalı", 0f, state.verticalOffset)
    }

    @Test
    fun cancelledDragWithoutReleaseIsTidiedUpWhenTheFingerLifts() = runTest {
        val state = newState()
        state.dragDown(120f)

        // İçerik değişip liste kaldırılınca bırakma olayı hiç gelmez; yalnızca parmak kalkar.
        state.onGestureEnd()
        advanceUntilIdle()

        assertEquals(0f, state.verticalOffset)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun cancelledDragPastTheThresholdStillRefreshes() = runTest {
        val state = newState()
        state.dragDown(500f)

        state.onGestureEnd()
        advanceUntilIdle()

        assertTrue(state.isRefreshing)
    }

    @Test
    fun swipingUpRetractsThePulledIndicatorFirst() = runTest {
        val state = newState()
        state.dragDown(100f)

        val consumed = state.nestedScrollConnection.onPreScroll(Offset(0f, -40f), NestedScrollSource.Drag)

        assertEquals(-40f, consumed.y)
        assertEquals(30f, state.verticalOffset)
    }

    @Test
    fun scrollingTheListNormallyIsNotIntercepted() = runTest {
        val state = newState()

        val pre = state.nestedScrollConnection.onPreScroll(Offset(0f, -40f), NestedScrollSource.Drag)
        val fling = state.nestedScrollConnection.onPreFling(Velocity(0f, -900f))

        assertEquals(Offset.Zero, pre)
        assertEquals(Velocity.Zero, fling)
        assertEquals(0f, state.verticalOffset)
    }

    @Test
    fun pullingIsIgnoredWhileRefreshing() = runTest {
        val state = newState()
        state.startRefresh()
        advanceUntilIdle()

        val consumed = state.dragDown(100f)

        assertEquals(Offset.Zero, consumed)
        assertEquals(threshold, state.verticalOffset)
    }
}
