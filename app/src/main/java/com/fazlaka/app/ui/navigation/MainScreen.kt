package com.fazlaka.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.event.EventBus
import com.fazlaka.app.ui.components.MiniPlayer
import com.fazlaka.app.ui.screens.home.HomeScreen
import com.fazlaka.app.ui.screens.messages.MessagesScreen
import com.fazlaka.app.ui.screens.profile.ProfileScreen
import com.fazlaka.app.ui.screens.search.SearchScreen
import com.fazlaka.app.ui.screens.seasons.SeasonsScreen
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch

private data class TabItem(
    val tab: MainTab,
    @androidx.annotation.StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val tabs = listOf(
    TabItem(MainTab.Home, com.fazlaka.app.R.string.tab_home, Icons.Filled.Home, Icons.Outlined.Home),
    TabItem(MainTab.Seasons, com.fazlaka.app.R.string.tab_seasons, Icons.Filled.List, Icons.Outlined.List),
    TabItem(MainTab.Search, com.fazlaka.app.R.string.tab_search, Icons.Filled.Search, Icons.Outlined.Search),
    TabItem(MainTab.Messages, com.fazlaka.app.R.string.tab_messages, Icons.Filled.Send, Icons.Outlined.Send),
    TabItem(MainTab.Profile, com.fazlaka.app.R.string.tab_profile, Icons.Filled.Person, Icons.Outlined.Person),
)

private val tabOrder = listOf(Routes.HOME, Routes.SEASONS, Routes.SEARCH, Routes.MESSAGES, Routes.PROFILE)

private fun tabDirection(prevRoute: String?, nextRoute: String?): Int =
    tabOrder.indexOf(nextRoute) - tabOrder.indexOf(prevRoute)

@Composable
fun MainScreen(
    onNavigate: (String) -> Unit,
    onLoggedOut: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTab = MainTab.fromRoute(currentRoute)
    val badgesViewModel: com.fazlaka.app.ui.viewmodel.NavBadgesViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
    val connectivityViewModel: com.fazlaka.app.ui.viewmodel.ConnectivityViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
    val playerViewModel: PlayerViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
    val badges by badgesViewModel.state.collectAsStateWithLifecycle()
    val miniPlayerState by playerViewModel.miniPlayerState.collectAsStateWithLifecycle()
    val currentEpisodeSlug by playerViewModel.episodeSlug.collectAsStateWithLifecycle()
    var showMore by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Double-tap back to exit
    var lastBackPress by remember { mutableLongStateOf(0L) }
    val backPressScope = rememberCoroutineScope()
    val activity = LocalContext.current as? android.app.Activity
    val doubleTapExitMessage = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.double_tap_exit)
    BackHandler(enabled = currentTab == MainTab.Home) {
        val now = System.currentTimeMillis()
        if (now - lastBackPress < 2000) {
            activity?.finish()
        } else {
            lastBackPress = now
            snackbarHostState.currentSnackbarData?.dismiss()
            backPressScope.launch {
                snackbarHostState.showSnackbar(
                    message = doubleTapExitMessage,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        EventBus.events.collect { event ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = event.message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                navController = navController,
                unreadMessages = badges.unreadMessages,
                onOpenMore = { showMore = true },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            com.fazlaka.app.ui.components.OfflineBanner(
                networkMonitor = connectivityViewModel.networkMonitor,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.weight(1f),
                enterTransition = {
                    val forward = tabDirection(initialState.destination.route, targetState.destination.route) > 0
                    fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                        if (forward) {
                            slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it / 3 }
                        } else {
                            slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { -it / 3 }
                        } +
                        scaleIn(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            initialScale = 0.99f,
                        )
                },
                exitTransition = {
                    val forward = tabDirection(initialState.destination.route, targetState.destination.route) > 0
                    fadeOut(tween(200)) +
                        if (forward) {
                            slideOutHorizontally(tween(220)) { -it / 5 }
                        } else {
                            slideOutHorizontally(tween(220)) { it / 5 }
                        }
                },
                popEnterTransition = {
                    fadeIn(tween(260)) +
                        scaleIn(animationSpec = tween(260), initialScale = 0.99f)
                },
                popExitTransition = {
                    fadeOut(tween(200))
                },
            ) {
                composable(Routes.HOME) {
                    HomeScreen(onNavigate = onNavigate)
                }
                composable(Routes.SEASONS) {
                    SeasonsScreen(onNavigate = onNavigate)
                }
                composable(Routes.SEARCH) {
                    SearchScreen(onNavigate = onNavigate)
                }
                composable(Routes.MESSAGES) {
                    MessagesScreen(onNavigate = onNavigate)
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(onNavigate = onNavigate, onLoggedOut = onLoggedOut)
                }
            }

            MiniPlayer(
                state = miniPlayerState,
                onPlayPause = { playerViewModel.togglePlayPause() },
                onNext = { playerViewModel.skipToNext() },
                onPrevious = { playerViewModel.skipToPrevious() },
                onClick = {
                    currentEpisodeSlug?.let { slug ->
                        onNavigate(Routes.episode(slug))
                    }
                },
            )
        }
    }

    if (showMore) {
        com.fazlaka.app.ui.components.MoreMenuSheet(
            onDismiss = { showMore = false },
            onNavigate = { route ->
                showMore = false
                onNavigate(route)
            },
        )
    }
}

@Composable
private fun BottomNavBar(
    currentTab: MainTab,
    navController: NavHostController,
    unreadMessages: Int = 0,
    onOpenMore: () -> Unit = {},
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val moreLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.tab_more)
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .semantics { contentDescription = "شريط التنقل السفلي" },
    ) {
        Surface(
            color = surfaceColor,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
            ) {
                tabs.forEach { item ->
                    val selected = currentTab == item.tab
                    val label = androidx.compose.ui.res.stringResource(item.labelRes)
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.9f,
                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                        label = "iconScale",
                    )
                    val offsetY by animateFloatAsState(
                        targetValue = if (selected) -2f else 0f,
                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                        label = "iconOffset",
                    )
                    val labelAlpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.7f,
                        animationSpec = tween(220),
                        label = "labelAlpha",
                    )
                    val indicatorGlow by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        label = "indicatorGlow",
                    )
                    val density = LocalDensity.current
                    val liftPx = with(density) { offsetY.dp.toPx() }

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.tab.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = if (selected) {
                                                listOf(
                                                    FazlakaGradientStart.copy(alpha = 0.95f),
                                                    FazlakaGradientMid.copy(alpha = 0.95f),
                                                )
                                            } else {
                                                listOf(Color.Transparent, Color.Transparent)
                                            },
                                        ),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = label,
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationY = liftPx
                                        alpha = if (selected) 1f else labelAlpha
                                    },
                                )
                                if (item.tab == MainTab.Messages && unreadMessages > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = FazlakaCyan,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(0.dp),
                                    ) {
                                        Text(
                                            text = if (unreadMessages > 99) "99+" else "$unreadMessages",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF06202A),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        )
                                    }
                                }
                            }
                        },
                        label = {
                            Text(
                                label,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.graphicsLayer {
                                    alpha = labelAlpha * if (selected) 1f else 0.6f
                                },
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = FazlakaGradientStart,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }

                // "المزيد" — opens the rich bottom-sheet menu (settings,
                // security, privacy, terms, support…).
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenMore,
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            FazlakaGradientStart.copy(alpha = 0.18f),
                                            FazlakaGradientMid.copy(alpha = 0.18f),
                                        ),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreHoriz,
                                contentDescription = moreLabel,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                    label = {
                        Text(
                            moreLabel,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
        // Gradient top accent line on the floating bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 10.dp)
                .size(height = 2.dp, width = 180.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(FazlakaCyan, FazlakaGradientStart, FazlakaCyan),
                    ),
                ),
        )
    }
}
