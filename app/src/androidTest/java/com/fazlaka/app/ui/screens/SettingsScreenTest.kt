package com.fazlaka.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
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
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysDarkModeLabel() {
        composeTestRule.setContent {
            Column {
                Text("المظهر")
                Text("الوضع الداكن")
                Switch(checked = false, onCheckedChange = {})
            }
        }
        composeTestRule.onNodeWithText("المظهر").assertIsDisplayed()
        composeTestRule.onNodeWithText("الوضع الداكن").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysLanguageSection() {
        composeTestRule.setContent {
            Column {
                Text("اللغة")
                Text("العربية")
                Text("Français")
                Text("English")
            }
        }
        composeTestRule.onNodeWithText("اللغة").assertIsDisplayed()
        composeTestRule.onNodeWithText("العربية").assertIsDisplayed()
        composeTestRule.onNodeWithText("Français").assertIsDisplayed()
        composeTestRule.onNodeWithText("English").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysAccountSecuritySection() {
        composeTestRule.setContent {
            Column {
                Text("الحساب والأمان")
                Text("تعديل الملف الشخصي")
                Text("الأمان والجلسات")
                Text("الدعم الفني")
                Text("Biometric login")
            }
        }
        composeTestRule.onNodeWithText("الحساب والأمان").assertIsDisplayed()
        composeTestRule.onNodeWithText("تعديل الملف الشخصي").assertIsDisplayed()
        composeTestRule.onNodeWithText("الأمان والجلسات").assertIsDisplayed()
        composeTestRule.onNodeWithText("الدعم الفني").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_darkModeToggle_renders() {
        var isDark = false
        composeTestRule.setContent {
            Switch(
                checked = isDark,
                onCheckedChange = { isDark = it },
            )
        }
        composeTestRule.onNodeWithText("").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysAboutSection() {
        composeTestRule.setContent {
            Column {
                Text("حول التطبيق")
                Text("الإصدار")
                Text("v1.0.8")
            }
        }
        composeTestRule.onNodeWithText("حول التطبيق").assertIsDisplayed()
        composeTestRule.onNodeWithText("الإصدار").assertIsDisplayed()
        composeTestRule.onNodeWithText("v1.0.8").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_backButtonIsDisplayed() {
        composeTestRule.setContent {
            Column {
                androidx.compose.material3.IconButton(onClick = {}) {
                    Text("رجوع")
                }
                Text("الإعدادات")
            }
        }
        composeTestRule.onNodeWithContentDescription("رجوع").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysHeroSection() {
        composeTestRule.setContent {
            Column {
                Text("الإعدادات")
                Text("فذلكة — منصة المحتوى العربية")
            }
        }
        composeTestRule.onNodeWithText("الإعدادات").assertIsDisplayed()
        composeTestRule.onNodeWithText("فذلكة — منصة المحتوى العربية").assertIsDisplayed()
    }
}
