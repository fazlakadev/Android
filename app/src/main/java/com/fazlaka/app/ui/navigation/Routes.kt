package com.fazlaka.app.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password/{email}"
    const val VERIFY_EMAIL = "verify_email/{email}"
    const val PHONE_AUTH = "phone_auth"
    const val TWO_FACTOR = "two_factor"
    const val OAUTH = "oauth/{provider}"

    const val HOME = "home"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val SETTINGS_TAB = "settings_tab"
    const val MAIN = "main"

    const val SEASON = "season/{idOrSlug}"
    const val EPISODE = "episode/{idOrSlug}"
    const val ARTICLE = "article/{idOrSlug}"
    const val PLAYLIST = "playlist/{idOrSlug}"
    const val CHAT = "chat/{conversationId}"
    const val USER_PROFILE = "user/{identifier}"

    const val NOTIFICATIONS = "notifications"
    const val ALL_EPISODES = "episodes"
    const val FRIENDS = "friends"
    const val SETTINGS = "settings"
    const val EDIT_PROFILE = "edit_profile"
    const val SECURITY = "security"
    const val SESSIONS = "sessions"
    const val ACTIVITY_LOG = "activity_log"
    const val SECONDARY_EMAILS = "secondary_emails"
    const val LINKED_ACCOUNTS = "linked_accounts"
    const val CHANGE_EMAIL = "change_email"
    const val PRIVACY_POLICY = "privacy_policy"
    const val TERMS = "terms"
    const val SUPPORT = "support"
    const val SUPPORT_TICKET = "support/{ticketId}"
    const val PROGRESS = "progress"
    const val VIEW_HISTORY = "view_history"
    const val MY_PLAYLISTS = "my_playlists"
    const val REFERRALS = "referrals"
    const val LIKES_HISTORY = "likes_history"
    const val SEASONS = "seasons"
    const val MESSAGES = "messages"

    fun season(idOrSlug: String) = "season/$idOrSlug"
    fun episode(idOrSlug: String) = "episode/$idOrSlug"
    fun article(idOrSlug: String) = "article/$idOrSlug"
    fun playlist(idOrSlug: String) = "playlist/$idOrSlug"
    fun chat(conversationId: String) = "chat/$conversationId"
    fun userProfile(identifier: String) = "user/$identifier"
    fun supportTicket(ticketId: String) = "support/$ticketId"
    fun oauth(provider: String) = "oauth/$provider"
    fun resetPassword(email: String) = "reset_password/$email"
    fun verifyEmail(email: String) = "verify_email/$email"
}

sealed class MainTab(
    val route: String,
) {
    data object Home : MainTab(Routes.HOME)
    data object Profile : MainTab(Routes.PROFILE)
    data object Settings : MainTab(Routes.SETTINGS_TAB)

    companion object {
        fun fromRoute(route: String?): MainTab = when (route) {
            Routes.PROFILE -> Profile
            Routes.SETTINGS_TAB -> Settings
            else -> Home
        }
    }
}

