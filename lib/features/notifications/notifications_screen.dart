import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../content/providers.dart';
import 'notifications_repository.dart';

class NotificationsScreen extends ConsumerStatefulWidget {
  const NotificationsScreen({super.key});

  @override
  ConsumerState<NotificationsScreen> createState() =>
      _NotificationsScreenState();
}

class _NotificationsScreenState extends ConsumerState<NotificationsScreen> {
  List<NotificationItem>? _items;
  bool _busy = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _busy = true);
    try {
      final (items, _) =
          await ref.read(notificationsRepositoryProvider).list();
      if (!mounted) return;
      setState(() => _items = items);
      // Mark all visible unread notifications as read (best effort).
      final repo = ref.read(notificationsRepositoryProvider);
      for (final n in items.where((n) => !n.read)) {
        unawaited(repo.markRead(n.id));
      }
    } catch (_) {
      if (mounted) setState(() => _items = const []);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: const Text('الإشعارات')),
      body: _busy
          ? const Center(child: CircularProgressIndicator())
          : (_items == null || _items!.isEmpty)
              ? Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.notifications_none_rounded,
                          size: 56, color: theme.colorScheme.outlineVariant),
                      const SizedBox(height: 10),
                      Text('لا توجد إشعارات',
                          style: TextStyle(color: theme.colorScheme.outline)),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.separated(
                    physics: const AlwaysScrollableScrollPhysics(),
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    itemCount: _items!.length,
                    separatorBuilder: (_, _) =>
                        Divider(height: 1, color: theme.dividerColor),
                    itemBuilder: (context, i) {
                      final n = _items![i];
                      return Dismissible(
                        key: ValueKey('notif-${n.id}'),
                        direction: DismissDirection.endToStart,
                        background: Container(
                          color: theme.colorScheme.errorContainer,
                          alignment: Alignment.centerRight,
                          padding: const EdgeInsets.only(right: 20),
                          child: Icon(Icons.delete_rounded,
                              color: theme.colorScheme.onErrorContainer),
                        ),
                        onDismissed: (_) async {
                          setState(() => _items!.removeAt(i));
                          try {
                            await ref
                                .read(notificationsRepositoryProvider)
                                .remove(n.id);
                          } catch (_) {}
                        },
                        child: ListTile(
                          leading: CircleAvatar(
                            backgroundColor: n.read
                                ? theme.colorScheme.surfaceContainerHighest
                                : theme.colorScheme.primaryContainer,
                            child: Icon(
                              Icons.notifications_rounded,
                              size: 20,
                              color: n.read
                                  ? theme.colorScheme.outline
                                  : theme.colorScheme.primary,
                            ),
                          ),
                          title: Text(n.title,
                              maxLines: 2, overflow: TextOverflow.ellipsis),
                          subtitle: (n.body?.isNotEmpty ?? false)
                              ? Text(n.body!,
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis)
                              : null,
                          trailing: n.createdAt == null
                              ? null
                              : Text(
                                  '${n.createdAt!.day}/${n.createdAt!.month}',
                                  style: theme.textTheme.labelSmall?.copyWith(
                                      color: theme.colorScheme.outline)),
                        ),
                      );
                    },
                  ),
                ),
    );
  }
}
