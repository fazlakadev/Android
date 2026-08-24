import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../content/models.dart';
import '../content/providers.dart';
import 'chat_screen.dart';

class ChatsScreen extends ConsumerStatefulWidget {
  const ChatsScreen({super.key});

  @override
  ConsumerState<ChatsScreen> createState() => _ChatsScreenState();
}

class _ChatsScreenState extends ConsumerState<ChatsScreen> {
  List<ConversationItem>? _items;
  Timer? _poll;

  @override
  void initState() {
    super.initState();
    _load();
    // Light polling keeps unread counts fresh while the list is open.
    _poll = Timer.periodic(const Duration(seconds: 6), (_) => _load(silent: true));
  }

  @override
  void dispose() {
    _poll?.cancel();
    super.dispose();
  }

  Future<void> _load({bool silent = false}) async {
    try {
      final items =
          await ref.read(chatRepositoryProvider).conversations(limit: 50);
      if (!mounted) return;
      setState(() => _items = items);
    } catch (e) {
      if (!mounted || silent) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(e.toString())));
      setState(() => _items = []);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: const Text('Chats')),
      body: _items == null
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: () => _load(),
              child: _items!.isEmpty
                  ? ListView(
                      children: [
                        SizedBox(
                          height: MediaQuery.of(context).size.height * .6,
                          child: Center(
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Icon(Icons.chat_bubble_outline_rounded,
                                    size: 64, color: theme.colorScheme.outline),
                                const SizedBox(height: 12),
                                Text('No conversations yet',
                                    style: theme.textTheme.titleMedium),
                                const SizedBox(height: 6),
                                Text(
                                  'Open a friend\'s profile and tap the\nchat icon to start talking.',
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      color: theme.colorScheme.outline),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ],
                    )
                  : ListView.separated(
                      physics: const AlwaysScrollableScrollPhysics(),
                      itemCount: _items!.length,
                      separatorBuilder: (_, _) => Divider(
                        height: 1,
                        indent: 74,
                        color:
                            theme.colorScheme.outlineVariant.withValues(alpha: .5),
                      ),
                      itemBuilder: (_, i) {
                        final c = _items![i];
                        return ListTile(
                          leading: CircleAvatar(
                            radius: 24,
                            backgroundImage: c.avatarUrl != null
                                ? NetworkImage(c.avatarUrl!)
                                : null,
                            child: c.avatarUrl == null
                                ? Icon(c.kind == 'group'
                                    ? Icons.group_rounded
                                    : Icons.person_rounded)
                                : null,
                          ),
                          title: Text(
                            c.title,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(fontWeight: FontWeight.w700),
                          ),
                          subtitle: Text(
                            c.lastMessageBody ?? 'Say hi 👋',
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                          trailing: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text(_timeLabel(c.lastMessageAt),
                                  style: TextStyle(
                                      fontSize: 11.5,
                                      color: theme.colorScheme.outline)),
                              if (c.unreadCount > 0) ...[
                                const SizedBox(height: 4),
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 7, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: theme.colorScheme.primary,
                                    borderRadius:
                                        BorderRadius.circular(100),
                                  ),
                                  child: Text(
                                    '${c.unreadCount}',
                                    style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 11,
                                        fontWeight: FontWeight.w700),
                                  ),
                                ),
                              ],
                            ],
                          ),
                          onTap: () async {
                            await Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => ChatScreen(
                                  conversationId: c.id,
                                  title: c.title,
                                  isGroup: c.kind == 'group',
                                ),
                              ),
                            );
                            _load(silent: true);
                          },
                        );
                      },
                    ),
            ),
    );
  }

  String _timeLabel(DateTime? dt) {
    if (dt == null) return '';
    final now = DateTime.now();
    if (dt.year == now.year && dt.month == now.month && dt.day == now.day) {
      return '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
    }
    return '${dt.day}/${dt.month}';
  }
}
