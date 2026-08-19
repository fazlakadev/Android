package com.fazlaka.app.ui.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager as PlatformAccessibilityManager
import androidx.compose.runtime.Composable

/**
 * Modifier extension that marks a composable as an accessible button
 * with a screen-reader label and optional hint for TalkBack users.
 */
fun Modifier.accessibleButton(label: String, hint: String? = null): Modifier =
    semantics {
        role = Role.Button
        contentDescription = buildString {
            append(label)
            if (hint != null) {
                append(". ")
                append(hint)
            }
        }
    }

/**
 * Modifier extension that marks a composable as an accessible card
 * with a title and optional onClick description.
 */
fun Modifier.accessibleCard(title: String, onClickDescription: String? = null): Modifier =
    semantics {
        role = Role.Button
        contentDescription = buildString {
            append(title)
            if (onClickDescription != null) {
                append(". ")
                append(onClickDescription)
            }
        }
    }

/**
 * Modifier extension that adds a content description to an image
 * composable for screen reader accessibility.
 */
fun Modifier.accessibleImage(description: String): Modifier =
    semantics {
        contentDescription = description
    }

/**
 * Announces a message to TalkBack users by injecting an accessibility event.
 * Use this to inform users of dynamic state changes (e.g., "Playing episode",
 * "Episode paused", "Added to playlist").
 */
@Composable
fun announceToAccessibility(message: String) {
    val context = LocalContext.current
    val accessibilityManager =
        context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE)
                as? PlatformAccessibilityManager ?: return
    if (!accessibilityManager.isEnabled) return

    val event = AccessibilityEvent.obtain().apply {
        eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
        contentDescription = message
    }
    @Suppress("DEPRECATION")
    accessibilityManager.sendAccessibilityEvent(event)
}
