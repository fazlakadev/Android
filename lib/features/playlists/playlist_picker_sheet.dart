import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../content/models.dart';
import '../content/providers.dart';

/// Bottom sheet that lets the user add [episodeId] to one of their
/// playlists (or create a new playlist on the fly).
Future<void> showPlaylistPicker(
  BuildContext context,
  WidgetRef ref, {
  required String episodeId,
}) async {
  await showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
    ),
    builder: (_) => _PlaylistPicker(episodeId: episodeId),
  );
}

class _PlaylistPicker extends ConsumerStatefulWidget {
  const _PlaylistPicker({required this.episodeId});

  final String episodeId;

  @override
  ConsumerState<_PlaylistPicker> createState() => _PlaylistPickerState();
}

class _PlaylistPickerState extends ConsumerState<_PlaylistPicker> {
  List<PlaylistSummary>? _mine;
  bool _creating = false;
  final _name = TextEditingController();
  String? _busyId;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _name.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      // The playlists list endpoint returns public + own playlists.
      final all = await ref.read(contentRepositoryProvider).playlists(limit: 100);
      if (!mounted) return;
      setState(() => _mine = all);
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(e.toString())));
      Navigator.of(context).pop();
    }
  }

  Future<void> _addTo(PlaylistSummary p) async {
    if (_busyId != null) return;
    setState(() => _busyId = p.id);
    try {
      await ref
          .read(contentRepositoryProvider)
          .addToPlaylist(p.id, widget.episodeId);
      if (!mounted) return;
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Added to "${p.title}"')),
      );
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(e.toString())));
      }
    } finally {
      if (mounted) setState(() => _busyId = null);
    }
  }

  Future<void> _createAndAdd() async {
    final title = _name.text.trim();
    if (title.isEmpty || _creating) return;
    setState(() => _creating = true);
    try {
      final repo = ref.read(contentRepositoryProvider);
      final created = await repo.createPlaylist(title: title);
      await repo.addToPlaylist(created.id, widget.episodeId);
      if (!mounted) return;
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Created "$title" and added episode')),
      );
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(e.toString())));
      }
    } finally {
      if (mounted) setState(() => _creating = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding:
          EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
      child: SafeArea(
        top: false,
        child: SizedBox(
          height: 420,
          child: Column(
            children: [
              const SizedBox(height: 10),
              Container(
                width: 36,
                height: 4,
                decoration: BoxDecoration(
                  color: theme.colorScheme.outlineVariant,
                  borderRadius: BorderRadius.circular(100),
                ),
              ),
              const SizedBox(height: 12),
              Text('Add to playlist',
                  style: theme.textTheme.titleMedium
                      ?.copyWith(fontWeight: FontWeight.w800)),
              Expanded(
                child: _mine == null
                    ? const Center(child: CircularProgressIndicator())
                    : (_mine!.isEmpty
                        ? Center(
                            child: Text(
                              'No playlists yet.\nCreate your first one below!',
                              textAlign: TextAlign.center,
                              style:
                                  TextStyle(color: theme.colorScheme.outline),
                            ),
                          )
                        : ListView.builder(
                            padding: const EdgeInsets.symmetric(vertical: 8),
                            itemCount: _mine!.length,
                            itemBuilder: (_, i) {
                              final p = _mine![i];
                              return ListTile(
                                leading: Icon(
                                  p.kind == 'platform'
                                      ? Icons.star_rounded
                                      : Icons.playlist_play_rounded,
                                  color: theme.colorScheme.primary,
                                ),
                                title: Text(p.title,
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis),
                                subtitle: Text('${p.itemsCount} episodes'),
                                trailing: _busyId == p.id
                                    ? const SizedBox(
                                        width: 18,
                                        height: 18,
                                        child: CircularProgressIndicator(
                                            strokeWidth: 2))
                                    : const Icon(Icons.add_circle_outline_rounded),
                                onTap: () => _addTo(p),
                              );
                            },
                          )),
              ),
              const Divider(height: 1),
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 10, 16, 10),
                child: Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _name,
                        decoration: const InputDecoration(
                          hintText: 'New playlist name…',
                          isDense: true,
                          border: InputBorder.none,
                        ),
                        onSubmitted: (_) => _createAndAdd(),
                      ),
                    ),
                    IconButton.filled(
                      tooltip: 'Create playlist',
                      onPressed: _creating ? null : _createAndAdd,
                      icon: _creating
                          ? const SizedBox(
                              width: 16,
                              height: 16,
                              child: CircularProgressIndicator(
                                  strokeWidth: 2))
                          : const Icon(Icons.add_rounded),
                    ),
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
