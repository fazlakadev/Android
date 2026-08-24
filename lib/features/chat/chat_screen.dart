import 'dart:async';
import 'dart:io';

import 'package:cached_network_image/cached_network_image.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:just_audio/just_audio.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:video_player/video_player.dart';

import '../../core/i18n/app_i18n.dart';
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
  bool _uploading = false;
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
    if (!mounted || _sending || _uploading) return;
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

  Future<void> _pickAndSend(String kind) async {
    Navigator.pop(context);
    final s = ref.read(sProvider);
    final result = await FilePicker.platform.pickFiles(
      type: switch (kind) {
        'image' => FileType.image,
        'video' => FileType.video,
        _ => FileType.audio,
      },
    );
    final file = result?.files.single;
    if (file == null || !mounted) return;
    const maxBytes = 10 * 1024 * 1024;
    if ((file.size) > maxBytes) {
      ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(s.attachmentTooBig)));
      return;
    }
    setState(() => _uploading = true);
    try {
      final bytes = await File(file.path!).readAsBytes();
      await ref.read(chatRepositoryProvider).sendAttachment(
            widget.conversationId,
            kind: kind,
            fileName: file.name,
            bytes: bytes,
          );
      await _refresh();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(e.toString())));
      }
    } finally {
      if (mounted) setState(() => _uploading = false);
    }
  }

  void _openAttachSheet() {
    final s = ref.read(sProvider);
    showModalBottomSheet<void>(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (sheetContext) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const SizedBox(height: 8),
            ListTile(
              leading: const Icon(Icons.photo_rounded),
              title: Text(s.attachPhoto),
              onTap: () => _pickAndSend('image'),
            ),
            ListTile(
              leading: const Icon(Icons.videocam_rounded),
              title: Text(s.attachVideo),
              onTap: () => _pickAndSend('video'),
            ),
            ListTile(
              leading: const Icon(Icons.graphic_eq_rounded),
              title: Text(s.attachAudio),
              onTap: () => _pickAndSend('audio'),
            ),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = ref.watch(sProvider);
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
                            IconButton.filledTonal(
                              onPressed: _uploading ? null : _openAttachSheet,
                              icon: _uploading
                                  ? const SizedBox(
                                      width: 18,
                                      height: 18,
                                      child: CircularProgressIndicator(
                                          strokeWidth: 2))
                                  : const Icon(Icons.add_rounded),
                            ),
                            const SizedBox(width: 6),
                            Expanded(
                              child: TextField(
                                controller: _input,
                                minLines: 1,
                                maxLines: 4,
                                textInputAction: TextInputAction.send,
                                onSubmitted: (_) => _send(),
                                decoration: InputDecoration(
                                  hintText: s.chatMessageHint,
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
    final hasCaption = m.body.trim().isNotEmpty && m.type != 'text';
    return Align(
      alignment: align,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        padding: EdgeInsets.symmetric(
          horizontal: m.type == 'text' ? 14 : 8,
          vertical: m.type == 'text' ? 9 : 6,
        ),
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
            _content(theme, m, textColor),
            if (hasCaption) ...[
              const SizedBox(height: 4),
              SelectableText(
                m.body,
                style:
                    TextStyle(color: textColor, fontSize: 14, height: 1.3),
              ),
            ],
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

  Widget _content(ThemeData theme, ChatMessage m, Color textColor) {
    switch (m.type) {
      case 'image':
        return _ImageBubble(url: m.attachmentUrl ?? '');
      case 'video':
        return _VideoTile(
          url: m.attachmentUrl ?? '',
          durationSec: m.durationSec,
          accent: textColor.withValues(alpha: .85),
        );
      case 'audio':
        return _AudioBubble(
          url: m.attachmentUrl ?? '',
          name: m.attachmentName,
          durationSec: m.durationSec,
        );
      default:
        if (m.type != 'text' && (m.attachmentUrl ?? '').isNotEmpty) {
          return InkWell(
            onTap: () =>
                launchUrl(Uri.parse(m.attachmentUrl!), mode: LaunchMode.externalApplication),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.insert_drive_file_rounded, size: 20, color: textColor),
                const SizedBox(width: 8),
                Flexible(
                  child: Text(
                    m.attachmentName ?? m.type,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(color: textColor, fontSize: 13.5),
                  ),
                ),
              ],
            ),
          );
        }
        return SelectableText(
          m.body,
          style: TextStyle(color: textColor, fontSize: 14.5, height: 1.3),
        );
    }
  }

  String _timeLabel(DateTime? dt) {
    if (dt == null) return '';
    return '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }
}

class _ImageBubble extends StatelessWidget {
  const _ImageBubble({required this.url});

  final String url;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => _showFull(context),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(14),
        child: ConstrainedBox(
          constraints: BoxConstraints(
            maxWidth: 260,
            maxHeight: 320,
            minWidth: 120,
          ),
          child: CachedNetworkImage(
            imageUrl: url,
            fit: BoxFit.cover,
            placeholder: (_, _) => Container(
              width: 180,
              height: 140,
              color: Colors.black12,
              child: const Center(
                  child: CircularProgressIndicator(strokeWidth: 2)),
            ),
            errorWidget: (_, _, _) => Container(
              width: 160,
              height: 100,
              color: Colors.black12,
              child: const Icon(Icons.broken_image_rounded),
            ),
          ),
        ),
      ),
    );
  }

  void _showFull(BuildContext context) {
    showDialog<void>(
      context: context,
      barrierColor: Colors.black87,
      builder: (_) => GestureDetector(
        onTap: () => Navigator.pop(context),
        child: InteractiveViewer(
          maxScale: 4,
          child: Center(child: CachedNetworkImage(imageUrl: url)),
        ),
      ),
    );
  }
}

class _VideoTile extends StatelessWidget {
  const _VideoTile({
    required this.url,
    required this.accent,
    this.durationSec,
  });

  final String url;
  final Color accent;
  final int? durationSec;

  String get _durationLabel {
    final d = durationSec ?? 0;
    return '${(d ~/ 60).toString().padLeft(2, '0')}:${(d % 60).toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => Navigator.push(
        context,
        MaterialPageRoute<void>(
          builder: (_) => _FullScreenVideo(url: url),
        ),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(14),
        child: Container(
          width: 240,
          height: 150,
          color: Colors.black87,
          child: Stack(
            alignment: Alignment.center,
            children: [
              const Icon(Icons.play_circle_fill_rounded,
                  size: 54, color: Colors.white70),
              Positioned(
                bottom: 8,
                left: 10,
                child: Row(
                  children: [
                    const Icon(Icons.videocam_rounded,
                        size: 15, color: Colors.white60),
                    const SizedBox(width: 5),
                    Text(_durationLabel,
                        style: const TextStyle(
                            color: Colors.white70, fontSize: 11.5)),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _FullScreenVideo extends StatefulWidget {
  const _FullScreenVideo({required this.url});

  final String url;

  @override
  State<_FullScreenVideo> createState() => _FullScreenVideoState();
}

class _FullScreenVideoState extends State<_FullScreenVideo> {
  VideoPlayerController? _controller;

  @override
  void initState() {
    super.initState();
    final c = VideoPlayerController.networkUrl(Uri.parse(widget.url));
    _controller = c;
    c.initialize().then((_) {
      if (mounted) {
        setState(() {});
        c.play();
      }
    }).catchError((Object _) {});
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final c = _controller;
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        foregroundColor: Colors.white,
      ),
      body: Center(
        child: c == null || !c.value.isInitialized
            ? const CircularProgressIndicator(color: Colors.white)
            : AspectRatio(
                aspectRatio: c.value.aspectRatio,
                child: VideoPlayer(c),
              ),
      ),
      floatingActionButton: c != null && c.value.isInitialized
          ? FloatingActionButton.small(
              onPressed: () {
                setState(() {
                  c.value.isPlaying ? c.pause() : c.play();
                });
              },
              child: Icon(
                c.value.isPlaying
                    ? Icons.pause_rounded
                    : Icons.play_arrow_rounded,
              ),
            )
          : null,
    );
  }
}

class _AudioBubble extends StatefulWidget {
  const _AudioBubble({
    required this.url,
    this.name,
    this.durationSec,
  });

  final String url;
  final String? name;
  final int? durationSec;

  @override
  State<_AudioBubble> createState() => _AudioBubbleState();
}

class _AudioBubbleState extends State<_AudioBubble> {
  AudioPlayer? _player;

  bool get _playing => _player?.playing ?? false;

  double get _progress {
    final p = _player;
    if (p == null) return 0;
    final total = p.duration?.inMilliseconds ?? 0;
    if (total <= 0) return 0;
    return (p.position.inMilliseconds / total).clamp(0.0, 1.0);
  }

  String get _positionLabel {
    final p = _player;
    var secs = p?.position.inSeconds ?? widget.durationSec ?? 0;
    return '${(secs ~/ 60).toString().padLeft(2, '0')}:${(secs % 60).toString().padLeft(2, '0')}';
  }

  Future<void> _toggle() async {
    if (_player == null) {
      final p = AudioPlayer();
      _player = p;
      p.playerStateStream.listen((st) {
        if (st.processingState == ProcessingState.completed) {
          p.seek(Duration.zero);
          p.pause();
        }
        if (mounted) setState(() {});
      });
      p.positionStream.listen((_) {
        if (mounted && _playing) setState(() {});
      });
      try {
        await p.setUrl(widget.url);
      } catch (_) {}
    }
    _playing ? _player!.pause() : unawaited(_player!.play());
    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    _player?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        InkResponse(
          onTap: _toggle,
          child: CircleAvatar(
            radius: 17,
            backgroundColor: scheme.surfaceContainerHighest,
            child: Icon(
              _playing ? Icons.pause_rounded : Icons.play_arrow_rounded,
              size: 22,
              color: scheme.onSurface,
            ),
          ),
        ),
        const SizedBox(width: 8),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 90,
              child: LinearProgressIndicator(
                value: _progress > 0 ? _progress : null,
                minHeight: 4,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
            const SizedBox(height: 4),
            Text(_positionLabel,
                style: TextStyle(fontSize: 11, color: scheme.outline)),
          ],
        ),
        const SizedBox(width: 6),
        Icon(Icons.graphic_eq_rounded, size: 20, color: scheme.outline),
        const SizedBox(width: 4),
      ],
    );
  }
}
