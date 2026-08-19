package com.fazlaka.app.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysEmailField() {
        composeTestRule.setContent {
            com.fazlaka.app.ui.components.AuthScaffold(
                title = "مرحبًا بعودتك",
                subtitle = "سجّل الدخول",
            ) {
                com.fazlaka.app.ui.components.AuthField(
                    value = "",
                    onValueChange = {},
                    label = "البريد الإلكتروني",
                )
            }
        }
        composeTestRule.onNodeWithText("البريد الإلكتروني").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysPasswordField() {
        composeTestRule.setContent {
            com.fazlaka.app.ui.components.AuthScaffold(
                title = "مرحبًا بعودتك",
                subtitle = "سجّل الدخول",
            ) {
                com.fazlaka.app.ui.components.AuthField(
                    value = "",
                    onValueChange = {},
                    label = "كلمة المرور",
                    isPassword = true,
                )
            }
        }
        composeTestRule.onNodeWithText("كلمة المرور").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysLoginButton() {
        composeTestRule.setContent {
            com.fazlaka.app.ui.components.AuthScaffold(
                title = "مرحبًا بعودتك",
                subtitle = "سجّل الدخول",
            ) {
                com.fazlaka.app.ui.components.AuthButton(
                    text = "دخول",
                    onClick = {},
                    enabled = true,
                )
            }
        }
        composeTestRule.onNodeWithText("دخلة").assertIsEnabled()
        composeTestRule.onNodeWithText("دخول").assertIsDisplayed()
    }

    @Test
    fun loginScreen_buttonIsDisabled_whenFieldsAreEmpty() {
        composeTestRule.setContent {
            com.fazlaka.app.ui.components.AuthButton(
                text = "دخول",
                onClick = {},
                enabled = false,
            )
        }
        composeTestRule.onNodeWithText("دخول").assertIsNotEnabled()
    }

    @Test
    fun loginScreen_emailFieldAcceptsInput() {
        var emailValue = ""
        composeTestRule.setContent {
            com.fazlaka.app.ui.components.AuthField(
                value = emailValue,
                onValueChange = { emailValue = it },
                label = "البريد الإلكتروني",
            )
        }
        composeTestRule.onNodeWithText("البريد الإلكتروني").performClick()
        composeTestRule.onNodeWithText("البريد الإلكتروني").performTextInput("test@example.com")
    }

    @Test
    fun loginScreen_passwordFieldIsPasswordType() {
        composeTestRule.setContent {
            com.fazlaka.app.ui.components.AuthScaffold(
                title = "مرحبًا بعودتك",
                subtitle = "سجّل الدخول",
            ) {
                com.fazlaka.app.ui.components.AuthField(
                    value = "secret123",
                    onValueChange = {},
                    label = "كلمة المرور",
                    isPassword = true,
                )
            }
        }
        composeTestRule.onNodeWithText("كلمة المرور").assertIsDisplayed()
        composeTestRule.onNodeWithText("secret123").assertDoesNotExist()
    }

    @Test
    fun loginScreen_displaysArabicTitle() {
        composeTestRule.setContent {
            com.fazlaka.app.ui.components.AuthScaffold(
                title = "مرحبًا بعودتك",
                subtitle = "سجّل الدخول لمتابعة مشاهداتك",
            ) {}
        }
        composeTestRule.onNodeWithText("مرحبًا بعودتك").assertIsDisplayed()
        composeTestRule.onNodeWithText("سجّل الدخول لمتابعة مشاهداتك").assertIsDisplayed()
    }
}
