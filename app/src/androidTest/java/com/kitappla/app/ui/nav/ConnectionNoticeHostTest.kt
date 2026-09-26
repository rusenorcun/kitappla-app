package com.kitappla.app.ui.nav

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

/** Ön bellek eski veri gösterdiğinde çıkan "bağlantı sorunu" uyarısının arayüz yarısı. */
class ConnectionNoticeHostTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private fun show() = rule.setContent {
        Box(Modifier.fillMaxSize()) {
            ConnectionNoticeHost(events, Modifier.align(Alignment.TopCenter))
        }
    }

    @Test
    fun nothingIsShownUntilAProblemIsReported() {
        show()
        rule.waitForIdle()
        rule.onNodeWithText(CONNECTION_PROBLEM_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun reportedProblemShowsTheNotice() {
        show()
        rule.waitForIdle()   // toplayıcı başlasın
        events.tryEmit(Unit)
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText(CONNECTION_PROBLEM_MESSAGE).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText(CONNECTION_PROBLEM_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun aSecondProblemAfterTheFirstNoticeIsStillShown() {
        show()
        rule.waitForIdle()
        events.tryEmit(Unit)
        rule.waitUntil(5_000) { rule.onAllNodesWithText(CONNECTION_PROBLEM_MESSAGE).fetchSemanticsNodes().isNotEmpty() }
        // Kısa süre (~4 sn) dolunca kapanır.
        rule.waitUntil(10_000) { rule.onAllNodesWithText(CONNECTION_PROBLEM_MESSAGE).fetchSemanticsNodes().isEmpty() }
        events.tryEmit(Unit)
        rule.waitUntil(5_000) { rule.onAllNodesWithText(CONNECTION_PROBLEM_MESSAGE).fetchSemanticsNodes().isNotEmpty() }
    }
}
