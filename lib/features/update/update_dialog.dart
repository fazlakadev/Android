import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/i18n/app_i18n.dart';
import '../../core/update/update_service.dart';

/// Pops the update dialog when appropriate (app start / FCM signal).
Future<void> maybeShowUpdateDialog(BuildContext context, WidgetRef ref) async {
  final state = ref.read(updateProvider);
  if (state.phase != UpdatePhase.available || state.info == null) return;
  await showDialog<void>(
    context: context,
    barrierDismissible: !state.info!.forceUpdate,
    builder: (_) => const _UpdateDialog(),
  );
}

class _UpdateDialog extends ConsumerWidget {
  const _UpdateDialog();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = ref.watch(sProvider);
    final st = ref.watch(updateProvider);
    final info = st.info!;
    final cs = Theme.of(context).colorScheme;

    return PopScope(
      canPop: !info.forceUpdate,
      child: AlertDialog(
        icon: Icon(
          Icons.system_update_alt_rounded,
          color: cs.primary,
          size: 36,
        ),
        title: Text(s.updateAvailableTitle),
        content: switch (st.phase) {
          UpdatePhase.downloading => Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(s.downloadingUpdate),
                const SizedBox(height: 12),
                LinearProgressIndicator(value: st.progress),
                const SizedBox(height: 8),
                Text(
                  '${(st.receivedBytes / 1048576).toStringAsFixed(1)}'
                  ' / ${(st.totalBytes / 1048576).toStringAsFixed(1)} MB'
                  '  ·  ${(st.progress * 100).toStringAsFixed(0)}%',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ],
            ),
          UpdatePhase.readyToInstall => Text(s.downloadDone),
          UpdatePhase.error => Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(st.errorMessage ?? s.updateCheckFailed),
                const SizedBox(height: 8),
              ],
            ),
          _ => Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${s.whatsNew} — ${st.info!.tagName}',
                  style: Theme.of(context)
                      .textTheme
                      .titleSmall
                      ?.copyWith(color: cs.primary),
                ),
                const SizedBox(height: 8),
                Flexible(
                  child: SingleChildScrollView(
                    child: Text(
                      st.info!.releaseNotes.isEmpty
                          ? '· ${s.updateAvailableTitle}'
                          : st.info!.releaseNotes,
                      maxLines: 10,
                    ),
                  ),
                ),
              ],
            ),
        },
        actions: [
          if (!info.forceUpdate &&
              st.phase == UpdatePhase.available)
            TextButton(
              onPressed: () {
                ref.read(updateProvider.notifier).defer();
                Navigator.of(context).pop();
              },
              child: Text(s.later),
            ),
          if (st.phase == UpdatePhase.downloading && !info.forceUpdate)
            TextButton(
              onPressed: () => ref.read(updateProvider.notifier).cancelDownload(),
              child: Text(s.later),
            ),
          FilledButton.icon(
            onPressed: switch (st.phase) {
              UpdatePhase.downloading => null,
              UpdatePhase.readyToInstall => () =>
                  ref.read(updateProvider.notifier).install(),
              _ => () => ref.read(updateProvider.notifier).download(),
            },
            icon: Icon(
              st.phase == UpdatePhase.readyToInstall
                  ? Icons.install_mobile_rounded
                  : Icons.download_rounded,
            ),
            label: Text(
              st.phase == UpdatePhase.readyToInstall
                  ? s.installNow
                  : s.updateNow,
            ),
          ),
        ],
      ),
    );
  }
}
