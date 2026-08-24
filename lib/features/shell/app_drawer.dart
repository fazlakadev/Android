import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/config.dart';
import '../../core/i18n/app_i18n.dart';
import '../../core/widgets/transitions.dart';
import '../auth/controllers/auth_controller.dart';
import '../auth/models/user.dart';
import '../chat/chats_screen.dart';
import '../friends/friends_screen.dart';
import '../playlists/playlists_screen.dart';
import '../settings/account_settings_screen.dart';
import '../settings/system_settings_screen.dart';
import '../support/support_screen.dart';

class AppDrawer extends ConsumerWidget {
  const AppDrawer({super.key, required this.user, required this.onNavigate});

  final User? user;
  final VoidCallback onNavigate;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final s = ref.watch(sProvider);

    return Drawer(
      shape: const RoundedRectangleBorder(),
      backgroundColor: theme.colorScheme.surface,
      child: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // ---------- Profile header ----------
            Container(
              margin: const EdgeInsets.fromLTRB(12, 12, 12, 4),
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: AlignmentDirectional.topStart,
                  end: AlignmentDirectional.bottomEnd,
                  colors: [
                    theme.colorScheme.primaryContainer,
                    theme.colorScheme.secondaryContainer,
                  ],
                ),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Row(
                children: [
                  Hero(
                    tag: 'avatar-${user?.id ?? 'me'}',
                    child: CircleAvatar(
                      radius: 28,
                      backgroundColor: theme.colorScheme.surface,
                      backgroundImage: user?.avatarUrl != null
                          ? CachedNetworkImageProvider(user!.avatarUrl!)
                          : null,
                      child: user?.avatarUrl == null
                          ? Icon(Icons.person_rounded,
                              size: 30,
                              color: theme.colorScheme.onPrimaryContainer)
                          : null,
                    ),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          user?.displayName ?? '',
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w800,
                            color:
                                theme.colorScheme.onPrimaryContainer,
                          ),
                        ),
                        if (user?.email != null)
                          Text(
                            user!.email!,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: theme
                                  .colorScheme.onPrimaryContainer
                                  .withValues(alpha: .8),
                            ),
                          ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 6),

            // ---------- Items ----------
            Expanded(
              child: ListView(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                children: [
                  _DrawerItem(
                    icon: Icons.home_outlined,
                    label: s.home,
                    onTap: () {
                      Navigator.pop(context);
                      onNavigate();
                    },
                  ),
                  _DrawerSectionLabel(theme: theme, text: s.general),
                  _DrawerItem(
                    icon: Icons.settings_outlined,
                    label: s.settings,
                    onTap: () => _push(context, const SystemSettingsScreen()),
                  ),
                  _DrawerItem(
                    icon: Icons.person_outline_rounded,
                    label: s.accountSettings,
                    onTap: () => _push(context, const AccountSettingsScreen()),
                  ),
                  _DrawerSectionLabel(theme: theme, text: s.friends),
                  _DrawerItem(
                    icon: Icons.groups_outlined,
                    label: s.friends,
                    onTap: () => _push(context, const FriendsScreen()),
                  ),
                  _DrawerItem(
                    icon: Icons.chat_bubble_outline_rounded,
                    label: s.chats,
                    onTap: () => _push(context, const ChatsScreen()),
                  ),
                  _DrawerItem(
                    icon: Icons.playlist_play_rounded,
                    label: s.playlists,
                    onTap: () => _push(context, const PlaylistsScreen()),
                  ),
                  _DrawerItem(
                    icon: Icons.support_agent_rounded,
                    label: s.support,
                    onTap: () => _push(context, const SupportScreen()),
                  ),
                ],
              ),
            ),

            // ---------- Footer ----------
            Divider(color: theme.colorScheme.outlineVariant, height: 1),
            _DrawerItem(
              icon: Icons.logout_rounded,
              label: s.signOut,
              danger: true,
              onTap: () async {
                final confirmed = await showDialog<bool>(
                  context: context,
                  builder: (context) => AlertDialog(
                    title: Text(s.signOut),
                    content: Text(s.signOutConfirm),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(context, false),
                        child: Text(s.cancel),
                      ),
                      FilledButton(
                        onPressed: () => Navigator.pop(context, true),
                        style: FilledButton.styleFrom(
                          backgroundColor:
                              Theme.of(context).colorScheme.error,
                        ),
                        child: Text(s.signOutAction),
                      ),
                    ],
                  ),
                );
                if (confirmed ?? false) {
                  if (context.mounted) Navigator.pop(context);
                  await ref.read(authControllerProvider.notifier).signOut();
                }
              },
            ),
            Padding(
              padding: const EdgeInsets.only(bottom: 14, top: 2),
              child: Center(
                child: Text(
                  'Fazlaka v${AppConfig.appVersion}',
                  style: theme.textTheme.bodySmall
                      ?.copyWith(color: theme.colorScheme.outline),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _push(BuildContext context, Widget screen) {
    Navigator.pop(context);
    Navigator.of(context).push(FadeSlideRoute(screen));
  }
}

class _DrawerSectionLabel extends StatelessWidget {
  const _DrawerSectionLabel({required this.theme, required this.text});

  final ThemeData theme;
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 6),
      child: Text(
        text.toUpperCase(),
        style: theme.textTheme.labelSmall?.copyWith(
          color: theme.colorScheme.primary,
          fontWeight: FontWeight.w800,
          letterSpacing: 1.1,
        ),
      ),
    );
  }
}

class _DrawerItem extends StatelessWidget {
  const _DrawerItem({
    required this.icon,
    required this.label,
    required this.onTap,
    this.danger = false,
  });

  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final bool danger;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Material(
        color: Colors.transparent,
        borderRadius: BorderRadius.circular(14),
        child: InkWell(
          borderRadius: BorderRadius.circular(14),
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
            child: Row(
              children: [
                Container(
                  width: 38,
                  height: 38,
                  decoration: BoxDecoration(
                    color: danger
                        ? theme.colorScheme.errorContainer
                        : theme.colorScheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(11),
                  ),
                  child: Icon(
                    icon,
                    size: 21,
                    color: danger
                        ? theme.colorScheme.error
                        : theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    label,
                    style: TextStyle(
                      color:
                          danger ? theme.colorScheme.error : null,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                Icon(Icons.chevron_right_rounded,
                    size: 18, color: theme.colorScheme.outline),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
