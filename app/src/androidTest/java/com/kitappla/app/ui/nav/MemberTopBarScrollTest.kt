package com.kitappla.app.ui.nav

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Logo çubuğu görünmeyen ekranlar (Giriş → Kayıt → E-posta doğrulama gibi, çubuk hiç ölçülmeden açılanlar) kaymalı:
 * gizli çubuğun kaydırma davranışı kaydırmayı yutuyordu.
 */
@OptIn(ExperimentalMaterial3Api::class)
class MemberTopBarScrollTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pageWithoutChromeScrollsEvenIfTheBarWasNeverMeasured() {
        lateinit var scroll: ScrollState
        rule.setContent {
            val topBarScroll = rememberMemberTopBarScroll(showChrome = false)
            scroll = rememberScrollState()
            // Gerçek yapıdaki gibi: kök Scaffold kaydırmayı dinler, çubuk bu ekranda çizilmez.
            Scaffold(modifier = Modifier.nestedScroll(topBarScroll.nestedScrollConnection)) { padding ->
                Column(Modifier.fillMaxSize().testTag("sayfa").verticalScroll(scroll)) {
                    Text("Uzun içerik", Modifier.height(4000.dp))
                }
            }
        }

        rule.onNodeWithTag("sayfa").performTouchInput { swipeUp() }
        rule.waitForIdle()

        assertTrue("sayfa kaymalı (kaydırma: ${scroll.value})", scroll.value > 0)
    }
}
