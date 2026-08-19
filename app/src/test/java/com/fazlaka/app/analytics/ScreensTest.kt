package com.fazlaka.app.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreensTest {

    private val allScreenNames = listOf(
        Screens.HOME,
        Screens.SEASONS,
        Screens.SEASON_DETAIL,
        Screens.SEARCH,
        Screens.PROFILE,
        Screens.EDIT_PROFILE,
        Screens.EPISODE_DETAIL,
        Screens.ARTICLE,
        Screens.PLAYLIST,
        Screens.PLAYLIST_DETAIL,
        Screens.MESSAGES,
        Screens.CONVERSATION,
        Screens.NOTIFICATIONS,
        Screens.FRIENDS,
        Screens.SETTINGS,
        Screens.SECURITY,
        Screens.SUPPORT,
        Screens.ONBOARDING,
        Screens.LOGIN,
        Screens.REGISTER,
        Screens.FORGOT_PASSWORD,
        Screens.RESET_PASSWORD,
        Screens.VERIFY_EMAIL,
        Screens.PHONE_AUTH,
        Screens.TWO_FACTOR,
        Screens.SPLASH,
        Screens.ALL_EPISODES,
        Screens.USER_PROFILE,
    )

    @Test
    fun allScreenNames_areNonEmpty() {
        allScreenNames.forEach { name ->
            assertFalse("Screen name should not be blank: '$name'", name.isBlank())
            assertTrue("Screen name should not be empty: '$name'", name.isNotEmpty())
        }
    }

    @Test
    fun noDuplicateScreenNames() {
        val seen = mutableSetOf<String>()
        val duplicates = mutableListOf<String>()
        allScreenNames.forEach { name ->
            if (!seen.add(name)) {
                duplicates.add(name)
            }
        }
        assertTrue(
            "Duplicate screen names found: $duplicates",
            duplicates.isEmpty(),
        )
    }

    @Test
    fun screenNames_followLowerSnakeCaseConvention() {
        val regex = Regex("^[a-z][a-z0-9]*(_[a-z0-9]+)*$")
        allScreenNames.forEach { name ->
            assertTrue(
                "Screen name '$name' should follow lower_snake_case convention",
                regex.matches(name),
            )
        }
    }

    @Test
    fun screenNames_areDistinctConstants() {
        assertNotEquals("HOME and SEARCH should differ", Screens.HOME, Screens.SEARCH)
        assertNotEquals("LOGIN and REGISTER should differ", Screens.LOGIN, Screens.REGISTER)
        assertNotEquals("SETTINGS and SECURITY should differ", Screens.SETTINGS, Screens.SECURITY)
        assertNotEquals("PROFILE and EDIT_PROFILE should differ", Screens.PROFILE, Screens.EDIT_PROFILE)
    }

    @Test
    fun screenNames_expectedValues() {
        assertEquals("home", Screens.HOME)
        assertEquals("seasons", Screens.SEASONS)
        assertEquals("season_detail", Screens.SEASON_DETAIL)
        assertEquals("search", Screens.SEARCH)
        assertEquals("profile", Screens.PROFILE)
        assertEquals("edit_profile", Screens.EDIT_PROFILE)
        assertEquals("episode_detail", Screens.EPISODE_DETAIL)
        assertEquals("article", Screens.ARTICLE)
        assertEquals("playlist", Screens.PLAYLIST)
        assertEquals("playlist_detail", Screens.PLAYLIST_DETAIL)
        assertEquals("messages", Screens.MESSAGES)
        assertEquals("conversation", Screens.CONVERSATION)
        assertEquals("notifications", Screens.NOTIFICATIONS)
        assertEquals("friends", Screens.FRIENDS)
        assertEquals("settings", Screens.SETTINGS)
        assertEquals("security", Screens.SECURITY)
        assertEquals("support", Screens.SUPPORT)
        assertEquals("onboarding", Screens.ONBOARDING)
        assertEquals("login", Screens.LOGIN)
        assertEquals("register", Screens.REGISTER)
        assertEquals("forgot_password", Screens.FORGOT_PASSWORD)
        assertEquals("reset_password", Screens.RESET_PASSWORD)
        assertEquals("verify_email", Screens.VERIFY_EMAIL)
        assertEquals("phone_auth", Screens.PHONE_AUTH)
        assertEquals("two_factor", Screens.TWO_FACTOR)
        assertEquals("splash", Screens.SPLASH)
        assertEquals("all_episodes", Screens.ALL_EPISODES)
        assertEquals("user_profile", Screens.USER_PROFILE)
    }

    @Test
    fun screenNames_containNoWhitespace() {
        allScreenNames.forEach { name ->
            assertFalse(
                "Screen name '$name' should not contain whitespace",
                name.contains(" "),
            )
            assertFalse(
                "Screen name '$name' should not contain tab",
                name.contains("\t"),
            )
            assertFalse(
                "Screen name '$name' should not contain newline",
                name.contains("\n"),
            )
        }
    }

    @Test
    fun screenNames_areAllAscii() {
        allScreenNames.forEach { name ->
            assertTrue(
                "Screen name '$name' should contain only ASCII characters",
                name.all { it.code < 128 },
            )
        }
    }
}
