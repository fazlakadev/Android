import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../../core/api/api_client.dart';
import '../../core/config.dart';
import '../../core/i18n/app_i18n.dart';
import '../../core/update/update_service.dart';
import '../content/models.dart';
import '../content/providers.dart';
import '../update/update_dialog.dart';

/// System-level settings: appearance, language and notification preferences.
/// Deliberately separate from [AccountSettingsScreen] which handles
/// profile data and security.
class SystemSettingsScreen extends ConsumerStatefulWidget {
  const SystemSettingsScreen({super.key});

  @override
  ConsumerState<SystemSettingsScreen> createState() =>
      _SystemSettingsScreenState();
}

class _SystemSettingsScreenState extends ConsumerState<SystemSettingsScreen> {
  Preferences? _prefs;
  bool _prefsLoading = true;
  String _version = AppConfig.appVersion;
  bool _checking = false;

  @override
  void initState() {
    super.initState();
    _loadPrefs();
    _loadVersion();
  }

  Future<void> _loadVersion() async {
    try {
      final info = await PackageInfo.fromPlatform();
      if (mounted) setState(() => _version = info.version);
    } catch (_) {}
  }

  Future<void> _checkForUpdates() async {
    if (_checking) return;
    setState(() => _checking = true);
    final hasUpdate = await ref.read(updateProvider.notifier).check();
    if (!mounted) return;
    setState(() => _checking = false);
    if (hasUpdate) {
      await maybeShowUpdateDialog(context, ref);
    } else {
      _snack(ref.read(sProvider).upToDate);
    }
  }

  Future<void> _loadPrefs() async {
    try {
      final prefs =
          await ref.read(contentRepositoryProvider).preferences();
      if (mounted) setState(() => _prefs = prefs);
    } on ApiException {
      // Non-blocking — toggles just stay hidden.
    } finally {
      if (mounted) setState(() => _prefsLoading = false);
    }
  }

  void _snack(String message, {bool error = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
        backgroundColor:
            error ? Theme.of(context).colorScheme.error : null,
      ),
    );
  }

  Future<void> _togglePref(String key, bool value) async {
    try {
      final updated =
          await ref.read(contentRepositoryProvider).updatePreferences({key: value});
      if (mounted) setState(() => _prefs = updated);
    } on ApiException catch (e) {
      if (mounted) _snack(e.message, error: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = ref.watch(sProvider);
    final currentLang = ref.watch(languageControllerProvider);
    final currentMode = ref.watch(themeModeControllerProvider);

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
                    child: Text(
                      s.settings,
                      style: theme.textTheme.titleLarge
                          ?.copyWith(fontWeight: FontWeight.w800),
                    ),
                  ),
                  const SizedBox(width: 48),
                ],
              ),
            ),
            Expanded(
              child: ListView(
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
                children: [
                  // ---------- Appearance ----------
                  _SettingsSectionHeader(theme: theme, icon:
                      Icons.brightness_6_rounded, title: s.appearance),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: SegmentedButton<ThemeMode>(
                        segments: [
                          ButtonSegment(
                            value: ThemeMode.light,
                            icon: const Icon(Icons.light_mode_rounded),
                            label: Text(s.lightMode),
                          ),
                          ButtonSegment(
                            value: ThemeMode.dark,
                            icon: const Icon(Icons.dark_mode_rounded),
                            label: Text(s.darkMode),
                          ),
                          ButtonSegment(
                            value: ThemeMode.system,
                            icon: const Icon(Icons.brightness_auto_rounded),
                            label: Text(s.systemMode),
                          ),
                        ],
                        selected: {currentMode},
                        onSelectionChanged: (selection) => ref
                            .read(themeModeControllerProvider.notifier)
                            .set(selection.first),
                      ),
                    ),
                  ),

                  // ---------- Language ----------
                  _SettingsSectionHeader(theme: theme, icon:
                      Icons.language_rounded, title: s.language),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: RadioGroup<AppLanguage>(
                        groupValue: currentLang,
                        onChanged: (v) => ref
                            .read(languageControllerProvider.notifier)
                            .set(v!),
                        child: Column(
                          children: AppLanguage.values
                              .map((lang) => RadioListTile<AppLanguage>(
                                    title: Text(
                                        '${lang.flag}  ${lang.nativeName}'),
                                    value: lang,
                                  ))
                              .toList(),
                        ),
                      ),
                    ),
                  ),

                  // ---------- Notification preferences ----------
                  _SettingsSectionHeader(theme: theme, icon:
                      Icons.notifications_active_outlined, title: 'Notifications'),
                  Card(
                    child: _prefsLoading || _prefs == null
                        ? const Padding(
                            padding: EdgeInsets.all(20),
                            child: Center(child: CircularProgressIndicator()),
                          )
                        : Column(
                            children: _prefs!.toggles().entries
                                .map((e) => SwitchListTile(
                                      title: Text(_prefLabel(e.key)),
                                      value: e.value,
                                      onChanged: (v) =>
                                          _togglePref(e.key, v),
                                    ))
                                .toList(),
                          ),
                  ),

                  // ---------- App & updates ----------
                  _SettingsSectionHeader(theme: theme,
                      icon: Icons.smartphone_rounded, title: s.appInfo),
                  Card(
                    child: Column(
                        children: [
                          ListTile(
                            leading:
                                const Icon(Icons.verified_outlined),
                            title: Text(s.currentVersion),
                            subtitle: Text('v$_version'),
                          ),
                          const Divider(height: 1),
                          Consumer(builder: (context, ref, _) {
                            final st = ref.watch(updateProvider);
                            final subtitle = switch (st.phase) {
                              UpdatePhase.checking => '…',
                              UpdatePhase.downloading =>
                                '${(st.progress * 100).toStringAsFixed(0)}%',
                              UpdatePhase.available => st.info?.tagName ?? '',
                              UpdatePhase.upToDate => s.upToDate,
                              _ => null,
                            };
                            return ListTile(
                              leading: Icon(
                                Icons.system_update_alt_rounded,
                                color: st.phase == UpdatePhase.available
                                    ? theme.colorScheme.primary
                                    : null,
                              ),
                              title: Text(s.checkUpdates),
                              subtitle: subtitle == null
                                  ? null
                                  : Text(subtitle),
                              trailing: _checking ||
                                      st.phase == UpdatePhase.checking
                                  ? const SizedBox(
                                      width: 20,
                                      height: 20,
                                      child: CircularProgressIndicator(
                                          strokeWidth: 2),
                                    )
                                  : const Icon(Icons.chevron_right),
                              onTap: _checkForUpdates,
                            );
                          }),
                        ],
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

  String _prefLabel(String key) => switch (key) {
        'notificationsEnabled' => 'Push notifications',
        'emailNotifications' => 'Email notifications',
        'loginAlerts' => 'Login alerts',
        _ => key,
      };
}

class _SettingsSectionHeader extends StatelessWidget {
  const _SettingsSectionHeader({
    required this.theme,
    required this.icon,
    required this.title,
  });

  final ThemeData theme;
  final IconData icon;
  final String title;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(4, 18, 4, 8),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(7),
            decoration: BoxDecoration(
              color: theme.colorScheme.primaryContainer,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon,
                size: 18, color: theme.colorScheme.onPrimaryContainer),
          ),
          const SizedBox(width: 10),
          Text(title,
              style: theme.textTheme.titleMedium
                  ?.copyWith(fontWeight: FontWeight.w800)),
        ],
      ),
    );
  }
}
