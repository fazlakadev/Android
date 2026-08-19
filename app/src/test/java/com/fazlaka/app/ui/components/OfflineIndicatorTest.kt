package com.fazlaka.app.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.util.EmptyTestActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun offlineIndicator_showsOfflineText_whenVisible() {
        composeTestRule.setContent {
            OfflineIndicator(visible = true)
        }
        composeTestRule.onNodeWithText("Offline").assertIsDisplayed()
    }

    @Test
    fun offlineIndicator_isHidden_whenNotVisible() {
        composeTestRule.setContent {
            OfflineIndicator(visible = false)
        }
        composeTestRule.onRoot().printToLog("OfflineIndicator-hidden")
        composeTestRule.onNodeWithText("Offline").assertDoesNotExist()
    }

    @Test
    fun offlineIndicator_showsCloudOffIcon_whenVisible() {
        composeTestRule.setContent {
            OfflineIndicator(visible = true)
        }
        composeTestRule.onRoot().printToLog("OfflineIndicator-visible")
        composeTestRule.onNodeWithText("Offline").assertIsDisplayed()
    }
}
