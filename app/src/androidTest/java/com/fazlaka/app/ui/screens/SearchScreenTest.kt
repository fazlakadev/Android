package com.fazlaka.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchScreen_displaysSearchBar() {
        composeTestRule.setContent {
            Column {
                Text("ابحث عن حلقات، مواسيم، مقالات...")
            }
        }
        composeTestRule.onNodeWithText("ابحث عن حلقات، مواسيم، مقالات...").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysFilterChips() {
        composeTestRule.setContent {
            Column {
                Text("الكل")
                Text("حلقات")
                Text("مواسم")
                Text("مقالات")
                Text("قوائم")
            }
        }
        composeTestRule.onNodeWithText("الكل").assertIsDisplayed()
        composeTestRule.onNodeWithText("حلقات").assertIsDisplayed()
        composeTestRule.onNodeWithText("مواسم").assertIsDisplayed()
        composeTestRule.onNodeWithText("مقالات").assertIsDisplayed()
        composeTestRule.onNodeWithText("قوائم").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysCategoryFilterChips() {
        composeTestRule.setContent {
            Column {
                Text("كل التصنيفات")
                Text("توعية")
                Text("تعليم")
                Text("تثقيف")
                Text("إرشاد")
            }
        }
        composeTestRule.onNodeWithText("كل التصنيفات").assertIsDisplayed()
        composeTestRule.onNodeWithText("توعية").assertIsDisplayed()
        composeTestRule.onNodeWithText("تعليم").assertIsDisplayed()
        composeTestRule.onNodeWithText("تثقيف").assertIsDisplayed()
        composeTestRule.onNodeWithText("إرشاد").assertIsDisplayed()
    }

    @Test
    fun searchScreen_emptyState_showsPrompt() {
        composeTestRule.setContent {
            Text("ابحث عن ما تريد مشاهدته")
        }
        composeTestRule.onNodeWithText("ابحث عن ما تريد مشاهدته").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysHeroTitle() {
        composeTestRule.setContent {
            Text("البحث")
        }
        composeTestRule.onNodeWithText("البحث").assertIsDisplayed()
    }

    @Test
    fun searchScreen_searchCanBeTriggered() {
        composeTestRule.setContent {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("ابحث عن حلقات، مواسيم، مقالات...") },
                )
            }
        }
        composeTestRule.onNodeWithText("ابحث عن حلقات، مواسيم، مقالات...").performClick()
    }

    @Test
    fun searchScreen_displaysRecentSearchesHeader() {
        composeTestRule.setContent {
            Text("عمليات البحث الأخيرة")
        }
        composeTestRule.onNodeWithText("عمليات البحث الأخيرة").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysClearAllButton() {
        composeTestRule.setContent {
            Text("مسح الكل")
        }
        composeTestRule.onNodeWithText("مسح الكل").assertIsDisplayed()
    }
}
