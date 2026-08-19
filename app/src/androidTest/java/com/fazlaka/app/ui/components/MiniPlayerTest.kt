package com.fazlaka.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiniPlayerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun miniPlayer_showsEpisodeTitle() {
        val state = MiniPlayerState(
            visible = true,
            title = "حلقة الصبر",
            subtitle = "الموسم الأول",
            isPlaying = false,
            progress = 0f,
        )
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = {},
                onNext = {},
                onPrevious = {},
                onClick = {},
            )
        }
        composeTestRule.onNodeWithText("حلقة الصبر").assertIsDisplayed()
    }

    @Test
    fun miniPlayer_showsSubtitle_whenNotBlank() {
        val state = MiniPlayerState(
            visible = true,
            title = "حلقة الصبر",
            subtitle = "الموسم الأول",
            isPlaying = false,
            progress = 0f,
        )
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = {},
                onNext = {},
                onPrevious = {},
                onClick = {},
            )
        }
        composeTestRule.onNodeWithText("الموسم الأول").assertIsDisplayed()
    }

    @Test
    fun miniPlayer_showsPlayButton_whenNotPlaying() {
        val state = MiniPlayerState(
            visible = true,
            title = "حلقة الصبر",
            subtitle = "",
            isPlaying = false,
            progress = 0f,
        )
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = {},
                onNext = {},
                onPrevious = {},
                onClick = {},
            )
        }
        composeTestRule.onNodeWithContentDescription("تشغيل").assertIsDisplayed()
    }

    @Test
    fun miniPlayer_showsPauseButton_whenPlaying() {
        val state = MiniPlayerState(
            visible = true,
            title = "حلقة الصبر",
            subtitle = "",
            isPlaying = true,
            progress = 0.5f,
        )
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = {},
                onNext = {},
                onPrevious = {},
                onClick = {},
            )
        }
        composeTestRule.onNodeWithContentDescription("إيقاف").assertIsDisplayed()
    }

    @Test
    fun miniPlayer_playButton_triggersPlayAction() {
        var playPauseClicked = false
        val state = MiniPlayerState(
            visible = true,
            title = "حلقة الصبر",
            subtitle = "",
            isPlaying = false,
            progress = 0f,
        )
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = { playPauseClicked = true },
                onNext = {},
                onPrevious = {},
                onClick = {},
            )
        }
        composeTestRule.onNodeWithContentDescription("تشغيل").performClick()
        assert(playPauseClicked) { "onPlayPause should have been triggered" }
    }

    @Test
    fun miniPlayer_pauseButton_triggersPauseAction() {
        var playPauseClicked = false
        val state = MiniPlayerState(
            visible = true,
            title = "حلقة الصبر",
            subtitle = "",
            isPlaying = true,
            progress = 0.5f,
        )
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = { playPauseClicked = true },
                onNext = {},
                onPrevious = {},
                onClick = {},
            )
        }
        composeTestRule.onNodeWithContentDescription("إيقاف").performClick()
        assert(playPauseClicked) { "onPlayPause should have been triggered" }
    }

    @Test
    fun miniPlayer_nextButton_triggersNextAction() {
        var nextClicked = false
        val state = MiniPlayerState(
            visible = true,
            title = "حلقة الصبر",
            subtitle = "",
            isPlaying = false,
            progress = 0f,
        )
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = {},
                onNext = { nextClicked = true },
                onPrevious = {},
                onClick = {},
            )
        }
        composeTestRule.onNodeWithContentDescription("التالي").performClick()
        assert(nextClicked) { "onNext should have been triggered" }
    }

    @Test
    fun miniPlayer_previousButton_triggersPreviousAction() {
        var previousClicked = false
        val state = MiniPlayerState(
            visible = true,
            title = "حلقة الصبر",
            subtitle = "",
            isPlaying = false,
            progress = 0f,
        )
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = {},
                onNext = {},
                onPrevious = { previousClicked = true },
                onClick = {},
            )
        }
        composeTestRule.onNodeWithContentDescription("السابق").performClick()
        assert(previousClicked) { "onPrevious should have been triggered" }
    }

    @Test
    fun miniPlayer_isNotVisible_whenStateHidden() {
        val state = MiniPlayerState(visible = false)
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = {},
                onNext = {},
                onPrevious = {},
                onClick = {},
            )
        }
        composeTestRule.onNodeWithText("").assertDoesNotExist()
    }

    @Test
    fun miniPlayer_showsProgressBar_whenPlaying() {
        val state = MiniPlayerState(
            visible = true,
            title = "حلقة الصبر",
            subtitle = "",
            isPlaying = true,
            progress = 0.35f,
        )
        composeTestRule.setContent {
            MiniPlayer(
                state = state,
                onPlayPause = {},
                onNext = {},
                onPrevious = {},
                onClick = {},
            )
        }
        composeTestRule.onNodeWithText("حلقة الصبر").assertIsDisplayed()
    }
}
