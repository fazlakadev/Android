import '../../core/i18n/app_i18n.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../auth/controllers/auth_controller.dart';
import '../content/providers.dart';
import '../friends/friends_screen.dart';
import '../articles/articles_tab.dart';
import '../episodes/episodes_tab.dart';
import '../home/home_tab.dart';
import '../notifications/notifications_screen.dart';
import '../player/audio_player_service.dart';
import '../player/mini_player_bar.dart';
import '../search/search_screen.dart';
import '../seasons/seasons_tab.dart';
import 'app_drawer.dart';

class HomeShell extends ConsumerStatefulWidget {
  const HomeShell({super.key});

  @override
  ConsumerState<HomeShell> createState() => _HomeShellState();
}

final _unreadProvider = FutureProvider<int>((ref) async {
  final auth = ref.watch(authControllerProvider);
  if (auth.status != AuthStatus.authenticated) return 0;
  try {
    return await ref.watch(notificationsRepositoryProvider).unreadCount();
  } catch (_) {
    return 0;
  }
});

class _NotificationsBell extends ConsumerWidget {
  const _NotificationsBell();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final unread = ref.watch(_unreadProvider).value ?? 0;
    final s = ref.watch(sProvider);
    return IconButton(
      tooltip: s.notifications,
      onPressed: () async {
        await Navigator.of(context).push(
          MaterialPageRoute(builder: (_) => const NotificationsScreen()),
        );
        ref.invalidate(_unreadProvider);
      },
      icon: Badge(
        isLabelVisible: unread > 0,
        label: Text('$unread'),
        child: const Icon(Icons.notifications_rounded),
      ),
    );
  }
}

class _HomeShellState extends ConsumerState<HomeShell>
    with SingleTickerProviderStateMixin {
  late final TabController _tabs = TabController(length: 4, vsync: this);
  final _scaffoldKey = GlobalKey<ScaffoldState>();

  @override
  void dispose() {
    _tabs.dispose();
    super.dispose();
  }

  Future<void> _openFriends() async {
    await Navigator.of(context).push(
      PageRouteBuilder(
        pageBuilder: (_, _, _) => const FriendsScreen(),
        transitionsBuilder: (_, animation, _, child) => FadeTransition(
          opacity: animation,
          child: child,
        ),
        transitionDuration: const Duration(milliseconds: 250),
      ),
    );
    // Refresh data when coming back (requests may have changed).
    if (mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = ref.watch(sProvider);
    final user = ref.watch(authControllerProvider).user;

    return Scaffold(
      key: _scaffoldKey,
      drawer: AppDrawer(user: user, onNavigate: () => _tabs.animateTo(0)),
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            // ---------- Top header ----------
            Padding(
              padding: const EdgeInsets.fromLTRB(6, 6, 6, 0),
              child: Row(
                children: [
                  IconButton(
                    tooltip: s.menu,
                    onPressed: () => _scaffoldKey.currentState?.openDrawer(),
                    icon: const Icon(Icons.menu_rounded),
                  ),
                  Expanded(
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        ClipRRect(
                          borderRadius: BorderRadius.circular(8),
                          child: Image.asset(
                            'assets/images/logo.png',
                            width: 26,
                            height: 26,
                            fit: BoxFit.contain,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Text(
                          'Fazlaka',
                          style: theme.textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.w800,
                            letterSpacing: .3,
                          ),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    tooltip: s.search,
                    onPressed: () => Navigator.of(context).push(
                      MaterialPageRoute(
                          builder: (_) => const SearchScreen()),
                    ),
                    icon: const Icon(Icons.search_rounded),
                  ),
                  _NotificationsBell(),
                  IconButton(
                    tooltip: s.friends,
                    onPressed: _openFriends,
                    icon: const Icon(Icons.group_rounded),
                  ),
                ],
              ),
            ),

            // ---------- Top navigation tabs ----------
            TabBar(
              controller: _tabs,
              labelColor: theme.colorScheme.primary,
              unselectedLabelColor: theme.colorScheme.outline,
              indicatorColor: theme.colorScheme.primary,
              indicatorSize: TabBarIndicatorSize.label,
              dividerColor: Colors.transparent,
              labelStyle:
                  const TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
              unselectedLabelStyle:
                  const TextStyle(fontWeight: FontWeight.w500, fontSize: 15),
              tabs: [
                Tab(text: s.home),
                Tab(text: s.seasons),
                Tab(text: s.episodes),
                Tab(text: s.articles),
              ],
            ),
            Divider(height: 1, color: theme.colorScheme.outlineVariant),

            // ---------- Pages ----------
            Expanded(
              child: TabBarView(
                controller: _tabs,
                children: [
                  HomeTab(onGoToTab: (i) => _tabs.animateTo(i)),
                  SeasonsTab(),
                  EpisodesTab(),
                  ArticlesTab(),
                ],
              ),
            ),

            // ---------- Docked mini player ----------
            Consumer(builder: (context, ref, _) {
              final hasEpisode =
                  ref.watch(audioPlayerProvider).hasEpisode;
              return AnimatedSwitcher(
                duration: const Duration(milliseconds: 200),
                child: hasEpisode
                    ? const MiniPlayerBar(key: ValueKey('mini'))
                    : const SizedBox.shrink(key: ValueKey('none')),
              );
            }),
          ],
        ),
      ),
    );
  }
}
