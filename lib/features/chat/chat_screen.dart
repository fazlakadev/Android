import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../auth/controllers/auth_controller.dart';
import '../content/models.dart';
import '../content/providers.dart';

class ChatScreen extends ConsumerStatefulWidget {
  const ChatScreen({
    super.key,
    required this.conversationId,
    required this.title,
    this.isGroup = false,
  });

  final String conversationId;
  final String title;
  final bool isGroup;

  @override
  ConsumerState<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends ConsumerState<ChatScreen> {
  final _input = TextEditingController();
  final _scroll = ScrollController();
  List<ChatMessage> _messages = [];
  bool _loading = true;
  bool _sending = false;
  String? _error;
  Timer? _poll;
  late String _myId;

  @override
  void initState() {
    super.initState();
    _myId = ref.read(apiClientProvider).userId ?? '';
    _refresh();
    // Poll for new messages while the chat is open (realtime socket upgrade
    // can come later; polling keeps this dependency-free).
    _poll = Timer.periodic(const Duration(seconds: 3), (_) => _pollOnce());
  }

  @override
  void dispose() {
    _poll?.cancel();
    _input.dispose();
    _scroll.dispose();
    super.dispose();
  }

  Future<void> _refresh() async {
    try {
      final detail =
          await ref.read(chatRepositoryProvider).detail(widget.conversationId);
      if (!mounted) return;
      setState(() {
        _messages = detail.messages;
        _loading = false;
        _error = null;
      });
      _jumpToBottom();
      _markRead();
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = e.toString();
      });
    }
  }

  Future<void> _pollOnce() async {
    if (!mounted || _sending) return;
    try {
      final repo = ref.read(chatRepositoryProvider);
      final detail = await repo.detail(widget.conversationId);
      if (!mounted) return;
      final last = _messages.isEmpty ? null : _messages.last.id;
      final newLast =
          detail.messages.isEmpty ? null : detail.messages.last.id;
      setState(() => _messages = detail.messages);
      if (last != newLast) {
        _jumpToBottom();
        _markRead();
      }
    } catch (_) {}
  }

  Future<void> _markRead() async {
    try {
      await ref.read(chatRepositoryProvider).markRead(widget.conversationId);
    } catch (_) {}
  }

  void _jumpToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_scroll.hasClients) return;
      _scroll.jumpTo(_scroll.position.maxScrollExtent);
    });
  }

  Future<void> _send() async {
    final text = _input.text.trim();
    if (text.isEmpty || _sending) return;
    setState(() => _sending = true);
    _input.clear();
    try {
      await ref.read(chatRepositoryProvider).send(widget.conversationId, text);
      await _refresh();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(e.toString())));
        _input.text = text;
      }
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null && _messages.isEmpty
              ? Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(_error!,
                          textAlign: TextAlign.center,
                          style: TextStyle(color: theme.colorScheme.error)),
                      const SizedBox(height: 10),
                      FilledButton.icon(
                        onPressed: () {
                          setState(() => _loading = true);
                          _refresh();
                        },
                        icon: const Icon(Icons.refresh_rounded),
                        label: const Text('Retry'),
                      ),
                    ],
                  ),
                )
              : Column(
                  children: [
                    Expanded(
                      child: ListView.builder(
                        controller: _scroll,
                        padding:
                            const EdgeInsets.fromLTRB(12, 12, 12, 6),
                        itemCount: _messages.length,
                        itemBuilder: (_, i) =>
                            _bubble(theme, _messages[i]),
                      ),
                    ),
                    SafeArea(
                      top: false,
                      child: Padding(
                        padding: const EdgeInsets.fromLTRB(12, 4, 12, 8),
                        child: Row(
                          children: [
                            Expanded(
                              child: TextField(
                                controller: _input,
                                minLines: 1,
                                maxLines: 4,
                                textInputAction: TextInputAction.send,
                                onSubmitted: (_) => _send(),
                                decoration: InputDecoration(
                                  hintText: 'Message…',
                                  filled: true,
                                  fillColor: theme
                                      .colorScheme.surfaceContainerHighest
                                      .withValues(alpha: .5),
                                  contentPadding: const EdgeInsets.symmetric(
                                      horizontal: 16, vertical: 10),
                                  border: OutlineInputBorder(
                                    borderRadius: BorderRadius.circular(100),
                                    borderSide: BorderSide.none,
                                  ),
                                ),
                              ),
                            ),
                            const SizedBox(width: 8),
                            IconButton.filled(
                              onPressed: _sending ? null : _send,
                              icon: _sending
                                  ? const SizedBox(
                                      width: 18,
                                      height: 18,
                                      child: CircularProgressIndicator(
                                          strokeWidth: 2,
                                          color: Colors.white))
                                  : const Icon(Icons.send_rounded),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
    );
  }

  Widget _bubble(ThemeData theme, ChatMessage m) {
    final mine = m.senderId == _myId;
    final align = mine ? Alignment.centerRight : Alignment.centerLeft;
    final color = mine
        ? theme.colorScheme.primary
        : theme.colorScheme.surfaceContainerHighest.withValues(alpha: .7);
    final textColor = mine ? Colors.white : theme.colorScheme.onSurface;
    return Align(
      alignment: align,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 9),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * .75,
        ),
        decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.only(
            topLeft: Radius.circular(mine ? 18 : 4),
            topRight: Radius.circular(mine ? 4 : 18),
            bottomLeft: const Radius.circular(18),
            bottomRight: const Radius.circular(18),
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (!mine && widget.isGroup)
              Text(
                m.sender.displayName,
                style: TextStyle(
                  fontSize: 11.5,
                  fontWeight: FontWeight.w700,
                  color: mine
                      ? Colors.white70
                      : theme.colorScheme.primary,
                ),
              ),
            SelectableText(
              m.body,
              style: TextStyle(color: textColor, fontSize: 14.5, height: 1.3),
            ),
            const SizedBox(height: 2),
            Text(
              _timeLabel(m.createdAt),
              style: TextStyle(
                fontSize: 10.5,
                color: mine ? Colors.white60 : theme.colorScheme.outline,
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _timeLabel(DateTime? dt) {
    if (dt == null) return '';
    return '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }
}
