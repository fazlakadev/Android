import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';
import '../chat/chat_screen.dart';
import '../content/models.dart';
import '../content/providers.dart';

class FriendsScreen extends ConsumerStatefulWidget {
  const FriendsScreen({super.key});

  @override
  ConsumerState<FriendsScreen> createState() => _FriendsScreenState();
}

class _FriendsScreenState extends ConsumerState<FriendsScreen>
    with SingleTickerProviderStateMixin {
  late final TabController _tabs = TabController(length: 3, vsync: this);
  List<FriendItem> _friends = [];
  List<FriendItem> _requests = [];
  List<FriendItem> _suggestions = [];
  bool _loading = true;
  String? _error;
  final _busyIds = <String>{};

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _tabs.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    final repo = ref.read(contentRepositoryProvider);
    try {
      final results = await Future.wait([
        repo.friends(),
        repo.incomingRequests(),
        repo.suggestions(),
      ]);
      if (!mounted) return;
      setState(() {
        _friends = results[0];
        _requests = results[1];
        _suggestions = results[2];
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(6, 6, 6, 0),
              child: Row(
                children: [
                  BackButton(onPressed: () => Navigator.pop(context)),
                  Expanded(
                    child: Text('Friends',
                        textAlign: TextAlign.center,
                        style: theme.textTheme.titleLarge
                            ?.copyWith(fontWeight: FontWeight.w800)),
                  ),
                  const SizedBox(width: 48),
                ],
              ),
            ),
            TabBar(
              controller: _tabs,
              labelColor: theme.colorScheme.primary,
              unselectedLabelColor: theme.colorScheme.outline,
              indicatorColor: theme.colorScheme.primary,
              dividerColor: Colors.transparent,
              tabs: [
                Tab(text: 'My Friends${_friends.isEmpty ? '' : ' (${_friends.length})'}'),
                const Tab(text: 'Requests'),
                const Tab(text: 'Suggestions'),
              ],
            ),
            Divider(height: 1, color: theme.colorScheme.outlineVariant),
            Expanded(
              child: _loading
                  ? const Center(child: CircularProgressIndicator())
                  : _error != null
                      ? Center(
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.cloud_off_rounded,
                                  size: 52, color: theme.colorScheme.outline),
                              const SizedBox(height: 12),
                              FilledButton.icon(
                                onPressed: _load,
                                icon: const Icon(Icons.refresh_rounded),
                                label: const Text('Retry'),
                              ),
                            ],
                          ),
                        )
                      : TabBarView(
                          controller: _tabs,
                          children: [
                            RefreshIndicator(
                              onRefresh: _load,
                              child: _FriendList(
                                people: _friends,
                                emptyIcon: Icons.groups_outlined,
                                emptyText:
                                    'No friends yet â€” check the suggestions tab!',
                                busyIds: _busyIds,
                                trailingBuilder: (_, p) => Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    IconButton(
                                      tooltip: 'Message',
                                      icon: const Icon(Icons.chat_bubble_outline_rounded),
                                      onPressed: () => _openChat(p),
                                    ),
                                    IconButton(
                                      tooltip: 'Remove friend',
                                      icon: Icon(Icons.person_remove_outlined,
                                          color: theme.colorScheme.error),
                                      onPressed: () => _withBusy(p.userId, () =>
                                          ref
                                              .read(contentRepositoryProvider)
                                              .removeFriend(p.userId)
                                              .then((_) => _load())),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                            RefreshIndicator(
                              onRefresh: _load,
                              child: _FriendList(
                                people: _requests,
                                emptyIcon: Icons.mark_email_unread_outlined,
                                emptyText: 'No pending requests',
                                busyIds: _busyIds,
                                trailingBuilder: (context, p) => Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    IconButton.filledTonal(
                                      icon: const Icon(Icons.check_rounded),
                                      color: theme.colorScheme.primary,
                                      onPressed: () => _withBusy(p.requestId ?? p.userId, () =>
                                          ref
                                              .read(contentRepositoryProvider)
                                              .acceptRequest(p.requestId!)
                                              .then((_) => _load())),
                                    ),
                                    const SizedBox(width: 4),
                                    IconButton.filledTonal(
                                      icon: const Icon(Icons.close_rounded),
                                      onPressed: () => _withBusy(p.requestId ?? p.userId, () =>
                                          ref
                                              .read(contentRepositoryProvider)
                                              .rejectRequest(p.requestId!)
                                              .then((_) => _load())),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                            RefreshIndicator(
                              onRefresh: _load,
                              child: _FriendList(
                                people: _suggestions,
                                emptyIcon: Icons.explore_outlined,
                                emptyText: 'No suggestions right now',
                                busyIds: _busyIds,
                                trailingBuilder: (context, p) => TextButton.icon(
                                  onPressed: () => _withBusy(p.userId, () =>
                                      ref
                                          .read(contentRepositoryProvider)
                                          .sendFriendRequest(p.userId)
                                          .then((_) => _load())),
                                  icon: const Icon(Icons.person_add_alt_1_rounded,
                                      size: 18),
                                  label: const Text('Add'),
                                ),
                              ),
                            ),
                          ],
                        ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _withBusy(String id, Future<void> Function() action) async {
    setState(() => _busyIds.add(id));
    try {
      await action();
    } on ApiException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.message), behavior: SnackBarBehavior.floating),
        );
      }
    } finally {
      _busyIds.remove(id);
    }
  }

  Future<void> _openChat(FriendItem p) async {
    try {
      final conversation =
          await ref.read(chatRepositoryProvider).openWith(p.userId);
      if (!mounted) return;
      final other = conversation.title.isNotEmpty ? conversation.title : p.displayName;
      await Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => ChatScreen(
            conversationId: conversation.id,
            title: other,
          ),
        ),
      );
    } on ApiException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.message), behavior: SnackBarBehavior.floating),
        );
      }
    }
  }
}

class _FriendList extends StatelessWidget {
  const _FriendList({
    required this.people,
    required this.emptyIcon,
    required this.emptyText,
    required this.busyIds,
    required this.trailingBuilder,
  });

  final List<FriendItem> people;
  final IconData emptyIcon;
  final String emptyText;
  final Set<String> busyIds;
  final Widget Function(BuildContext, FriendItem) trailingBuilder;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    if (people.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          SizedBox(
            height: MediaQuery.sizeOf(context).height * .5,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(emptyIcon, size: 60, color: theme.colorScheme.outline),
                const SizedBox(height: 10),
                Text(emptyText, style: TextStyle(color: theme.colorScheme.outline)),
              ],
            ),
          ),
        ],
      );
    }
    return ListView.separated(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(16, 10, 16, 24),
      itemCount: people.length,
      separatorBuilder: (_, _) => const SizedBox(height: 8),
      itemBuilder: (_, i) {
        final p = people[i];
        final busy = busyIds.contains(p.requestId ?? p.userId);
        return Card(
          margin: EdgeInsets.zero,
          child: ListTile(
            leading: CircleAvatar(
              backgroundColor: theme.colorScheme.primaryContainer,
              backgroundImage: p.avatarUrl != null
                  ? CachedNetworkImageProvider(p.avatarUrl!)
                  : null,
              child: p.avatarUrl == null
                  ? Icon(Icons.person_rounded,
                      color: theme.colorScheme.onPrimaryContainer)
                  : null,
            ),
            title: Text(p.displayName,
                maxLines: 1, overflow: TextOverflow.ellipsis),
            subtitle: p.username != null ? Text('@${p.username}') : null,
            trailing: busy
                ? const SizedBox(
                    width: 22,
                    height: 22,
                    child: CircularProgressIndicator(strokeWidth: 2.2))
                : trailingBuilder(context, p),
          ),
        );
      },
    );
  }
}
