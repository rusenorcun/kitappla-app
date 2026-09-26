package com.kitappla.app.ui.screens.common

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import com.kitappla.app.ui.theme.KitapplaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Gerçek dokunuşlarla çekip yenileme: yeterince çekince yenilenir, kısa çekme yenilemez. */
class PullRefreshTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var refreshes = 0
    private var loading by mutableStateOf(false)

    private fun list(scrollable: Boolean = true) = rule.setContent {
        KitapplaTheme {
            PullRefresh(
                loading = loading,
                onRefresh = { refreshes++; loading = true },
                modifier = Modifier.fillMaxSize().testTag("alan"),
                scrollableContent = scrollable,
            ) {
                if (scrollable) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(50) { Text("Satır $it", Modifier.height(48.dp)) }
                    }
                } else {
                    Text("Henüz kayıt yok")
                }
            }
        }
    }

    @Test
    fun pullingTheListFarEnoughRefreshes() {
        list()
        rule.onNodeWithTag("alan").performTouchInput { swipeDown(startY = top + 20f, endY = bottom - 20f, durationMillis = 600) }
        rule.waitForIdle()
        assertEquals(1, refreshes)
    }

    @Test
    fun aShortPullDoesNotRefresh() {
        list()
        rule.onNodeWithTag("alan").performTouchInput { swipeDown(startY = top + 20f, endY = top + 120f, durationMillis = 300) }
        rule.waitForIdle()
        assertEquals(0, refreshes)
    }

    @Test
    fun emptyStateCanBePulledToo() {
        list(scrollable = false)
        rule.onNodeWithTag("alan").performTouchInput { swipeDown(startY = top + 20f, endY = bottom - 20f, durationMillis = 600) }
        rule.waitForIdle()
        assertEquals(1, refreshes)
    }
}
