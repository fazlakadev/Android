package com.fazlaka.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fazlaka.app.ui.screens.OnboardingScreen
import com.fazlaka.app.ui.screens.SplashScreen
import com.fazlaka.app.ui.screens.auth.ForgotPasswordScreen
import com.fazlaka.app.ui.screens.auth.LoginScreen
import com.fazlaka.app.ui.screens.auth.OAuthScreen
import com.fazlaka.app.ui.screens.auth.PhoneAuthScreen
import com.fazlaka.app.ui.screens.auth.RegisterScreen
import com.fazlaka.app.ui.screens.auth.ResetPasswordScreen
import com.fazlaka.app.ui.screens.auth.TwoFactorScreen
import com.fazlaka.app.ui.screens.auth.VerifyEmailScreen
import com.fazlaka.app.ui.screens.content.ArticleScreen
import com.fazlaka.app.ui.screens.content.PlaylistDetailScreen
import com.fazlaka.app.ui.screens.episode.EpisodeDetailScreen
import com.fazlaka.app.ui.screens.episodes.AllEpisodesScreen
import com.fazlaka.app.ui.screens.friends.FriendsScreen
import com.fazlaka.app.ui.screens.messages.ConversationScreen
import com.fazlaka.app.ui.screens.notifications.NotificationsScreen
import com.fazlaka.app.ui.screens.profile.EditProfileScreen
import com.fazlaka.app.ui.screens.profile.LikesHistoryScreen
import com.fazlaka.app.ui.screens.profile.MyPlaylistsScreen
import com.fazlaka.app.ui.screens.profile.ProgressScreen
import com.fazlaka.app.ui.screens.profile.ReferralsScreen
import com.fazlaka.app.ui.screens.profile.UserProfileScreen
import com.fazlaka.app.ui.screens.profile.ViewHistoryScreen
import com.fazlaka.app.ui.screens.seasons.SeasonDetailScreen
import com.fazlaka.app.ui.screens.security.ActivityLogScreen
import com.fazlaka.app.ui.screens.security.ChangeEmailScreen
import com.fazlaka.app.ui.screens.security.LinkedAccountsScreen
import com.fazlaka.app.ui.screens.security.SecondaryEmailsScreen
import com.fazlaka.app.ui.screens.security.SecurityScreen
import com.fazlaka.app.ui.screens.settings.SettingsScreen
import com.fazlaka.app.ui.screens.support.SupportScreen
import com.fazlaka.app.ui.screens.support.SupportTicketScreen

@Composable
fun FazlakaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH,
    notificationData: Map<String, String>? = null,
    onNotificationHandled: () -> Unit = {},
) {
    if (notificationData != null) {
        LaunchedEffect(notificationData) {
            val destination = when (notificationData["type"]) {
                "comment", "like" -> {
                    val contentType = notificationData["contentType"]
                    val contentId = notificationData["contentId"]
                    if (contentType == "episode" && contentId != null) {
                        "episode/$contentId"
                    } else if (contentType == "article" && contentId != null) {
                        "article/$contentId"
                    } else null
                }
                "friend_request", "friend_accepted" -> Routes.FRIENDS
                "system", "announcement" -> Routes.NOTIFICATIONS
                else -> null
            }
            if (destination != null) {
                navController.navigate(destination) {
                    popUpTo(Routes.MAIN) { inclusive = false }
                }
            }
            onNotificationHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(tween(280)) +
                slideInHorizontally(tween(300)) { it / 5 } +
                scaleIn(
                    animationSpec = tween(300),
                    initialScale = 0.97f,
                )
        },
        exitTransition = {
            fadeOut(tween(200)) + scaleOut(
                animationSpec = tween(200),
                targetScale = 0.97f,
            )
        },
        popEnterTransition = {
            fadeIn(tween(280)) +
                scaleIn(
                    animationSpec = tween(280),
                    initialScale = 1.04f,
                )
        },
        popExitTransition = {
            fadeOut(tween(220)) +
                slideOutHorizontally(tween(220)) { it / 5 } +
                scaleOut(
                    animationSpec = tween(220),
                    targetScale = 0.94f,
                )
        },
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(
                startDestination = startDestination,
                onNavigate = { dest ->
                    if (dest == Routes.MAIN) {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(dest) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onNavigate = { navController.navigate(it) },
                onDone = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        // Auth
        composable(Routes.LOGIN) {
            LoginScreen(onNavigate = { dest ->
                if (dest.startsWith("oauth/")) {
                    navController.navigate(dest)
                } else {
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onNavigate = { dest ->
                when {
                    dest.startsWith("oauth/") -> navController.navigate(dest)
                    dest.startsWith("verify_email/") -> navController.navigate(dest)
                    else -> navController.navigate(dest) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            })
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(
            route = Routes.RESET_PASSWORD,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
        ) {
            ResetPasswordScreen(
                email = it.arguments?.getString("email") ?: "",
                onBack = { navController.popBackStack() },
                onNavigate = { dest ->
                    navController.navigate(dest) {
                        popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.VERIFY_EMAIL,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
        ) {
            VerifyEmailScreen(
                email = it.arguments?.getString("email") ?: "",
                onBack = { navController.popBackStack() },
                onNavigate = { dest ->
                    navController.navigate(dest) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.PHONE_AUTH) {
            PhoneAuthScreen(
                mode = "register",
                onNavigate = { dest ->
                    navController.navigate(dest) {
                        popUpTo(Routes.PHONE_AUTH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.TWO_FACTOR) {
            TwoFactorScreen(onNavigate = { dest ->
                navController.navigate(dest) {
                    popUpTo(Routes.TWO_FACTOR) { inclusive = true }
                }
            })
        }
        composable(
            route = Routes.OAUTH,
            arguments = listOf(navArgument("provider") { type = NavType.StringType }),
        ) {
            OAuthScreen(
                provider = it.arguments?.getString("provider") ?: "google",
                onDone = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // Main shell
        composable(Routes.MAIN) {
            MainScreen(
                onNavigate = { navController.navigate(it) },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
            )
        }

        // Content details
        composable(Routes.SEASON) {
            SeasonDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.EPISODE) {
            EpisodeDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.ARTICLE) {
            ArticleScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PLAYLIST) {
            PlaylistDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.CHAT) {
            ConversationScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.USER_PROFILE) {
            UserProfileScreen(
                onBack = { navController.popBackStack() },
                onOpenConversation = { conversationId ->
                    navController.navigate(Routes.chat(conversationId))
                },
            )
        }

        // Profile / library sub-screens
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ALL_EPISODES) {
            AllEpisodesScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.FRIENDS) {
            FriendsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
                showBackButton = true,
            )
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SECURITY) {
            SecurityScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.SESSIONS) {
            SecurityScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.ACTIVITY_LOG) {
            ActivityLogScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SECONDARY_EMAILS) {
            SecondaryEmailsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.LINKED_ACCOUNTS) {
            LinkedAccountsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CHANGE_EMAIL) {
            ChangeEmailScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRIVACY_POLICY) {
            com.fazlaka.app.ui.screens.settings.PrivacyPolicyScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.TERMS) {
            com.fazlaka.app.ui.screens.settings.TermsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SUPPORT) {
            SupportScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.SUPPORT_TICKET) {
            SupportTicketScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PROGRESS) {
            ProgressScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.VIEW_HISTORY) {
            ViewHistoryScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.MY_PLAYLISTS) {
            MyPlaylistsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.REFERRALS) {
            ReferralsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.LIKES_HISTORY) {
            LikesHistoryScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
    }
}

