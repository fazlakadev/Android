import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';
import '../auth/controllers/auth_controller.dart';
import '../content/models.dart';
import '../content/providers.dart';

class AccountSettingsScreen extends ConsumerStatefulWidget {
  const AccountSettingsScreen({super.key});

  @override
  ConsumerState<AccountSettingsScreen> createState() =>
      _AccountSettingsScreenState();
}

class _AccountSettingsScreenState extends ConsumerState<AccountSettingsScreen> {
  final _nameCtrl = TextEditingController();
  final _usernameCtrl = TextEditingController();
  final _bioCtrl = TextEditingController();

  final _currentPasswordCtrl = TextEditingController();
  final _newPasswordCtrl = TextEditingController();
  final _confirmPasswordCtrl = TextEditingController();

  Preferences? _prefs;
  bool _savingProfile = false;
  bool _savingPassword = false;

  @override
  void initState() {
    super.initState();
    final user = ref.read(authControllerProvider).user;
    _nameCtrl.text = user?.name ?? '';
    _usernameCtrl.text = user?.username ?? '';
    _loadPrefs();
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _usernameCtrl.dispose();
    _bioCtrl.dispose();
    _currentPasswordCtrl.dispose();
    _newPasswordCtrl.dispose();
    _confirmPasswordCtrl.dispose();
    super.dispose();
  }

  Future<void> _loadPrefs() async {
    try {
      final prefs =
          await ref.read(contentRepositoryProvider).preferences();
      if (mounted) setState(() => _prefs = prefs);
    } on ApiException {
      // Non-blocking.
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

  Future<void> _saveProfile() async {
    setState(() => _savingProfile = true);
    try {
      final updated = await ref
          .read(contentRepositoryProvider)
          .updateProfile(
            name: _nameCtrl.text.trim(),
            username: _usernameCtrl.text.trim().isEmpty
                ? null
                : _usernameCtrl.text.trim(),
            bio: _bioCtrl.text.trim().isEmpty ? null : _bioCtrl.text.trim(),
          );
      ref.read(authControllerProvider.notifier).applyUpdatedUser(updated);
      if (mounted) _snack('Profile saved');
    } on ApiException catch (e) {
      if (mounted) _snack(e.message, error: true);
    } finally {
      if (mounted) setState(() => _savingProfile = false);
    }
  }

  Future<void> _togglePref(String key, bool value) async {
    try {
      final updated = await ref
          .read(contentRepositoryProvider)
          .updatePreferences({key: value});
      if (mounted) setState(() => _prefs = updated);
    } on ApiException catch (e) {
      if (mounted) _snack(e.message, error: true);
    }
  }

  Future<void> _changePassword() async {
    if (_newPasswordCtrl.text != _confirmPasswordCtrl.text) {
      _snack('Passwords do not match', error: true);
      return;
    }
    setState(() => _savingPassword = true);
    try {
      await ref.read(contentRepositoryProvider).changePassword(
            currentPassword: _currentPasswordCtrl.text,
            newPassword: _newPasswordCtrl.text,
          );
      _currentPasswordCtrl.clear();
      _newPasswordCtrl.clear();
      _confirmPasswordCtrl.clear();
      if (mounted) _snack('Password changed');
    } on ApiException catch (e) {
      if (mounted) _snack(e.message, error: true);
    } finally {
      if (mounted) setState(() => _savingPassword = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final user = ref.watch(authControllerProvider).user;

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
                    child: Text('Account Settings',
                        textAlign: TextAlign.center,
                        style: theme.textTheme.titleLarge
                            ?.copyWith(fontWeight: FontWeight.w800)),
                  ),
                  const SizedBox(width: 48),
                ],
              ),
            ),
            Expanded(
              child: ListView(
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
                children: [
                  // ---------- Profile ----------
                  _SectionTitle(theme, 'Profile'),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        children: [
                          CircleAvatar(
                            radius: 38,
                            backgroundColor:
                                theme.colorScheme.primaryContainer,
                            backgroundImage: user?.avatarUrl != null
                                ? NetworkImage(user!.avatarUrl!)
                                : null,
                            child: user?.avatarUrl == null
                                ? Icon(Icons.person_rounded,
                                    size: 40,
                                    color:
                                        theme.colorScheme.onPrimaryContainer)
                                : null,
                          ),
                          const SizedBox(height: 14),
                          TextField(
                            controller: _nameCtrl,
                            decoration: const InputDecoration(
                              labelText: 'Full name',
                              prefixIcon: Icon(Icons.badge_outlined),
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _usernameCtrl,
                            decoration: const InputDecoration(
                              labelText: 'Username',
                              prefixIcon: Icon(Icons.alternate_email_rounded),
                              border: OutlineInputBorder(),
                              helperText: 'a-z, 0-9, dot, underscore, dash',
                            ),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _bioCtrl,
                            maxLines: 3,
                            maxLength: 500,
                            decoration: const InputDecoration(
                              labelText: 'Bio',
                              prefixIcon: Padding(
                                padding: EdgeInsets.only(bottom: 48),
                                child: Icon(Icons.notes_rounded),
                              ),
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),
                          FilledButton.icon(
                            onPressed:
                                _savingProfile ? null : _saveProfile,
                            icon: _savingProfile
                                ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                        strokeWidth: 2.2))
                                : const Icon(Icons.save_outlined),
                            label: const Text('Save Profile'),
                          ),
                        ],
                      ),
                    ),
                  ),

                  // ---------- Preferences ----------
                  _SectionTitle(theme, 'Notifications'),
                  Card(
                    child: _prefs == null
                        ? const Padding(
                            padding: EdgeInsets.all(20),
                            child: Center(
                                child: CircularProgressIndicator()),
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

                  // ---------- Security ----------
                  _SectionTitle(theme, 'Change Password'),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        children: [
                          TextField(
                            controller: _currentPasswordCtrl,
                            obscureText: true,
                            decoration: const InputDecoration(
                              labelText: 'Current password',
                              prefixIcon:
                                  Icon(Icons.lock_outline_rounded),
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _newPasswordCtrl,
                            obscureText: true,
                            decoration: const InputDecoration(
                              labelText: 'New password',
                              helperText:
                                  'At least 8 chars incl. a letter & a number',
                              prefixIcon: Icon(Icons.key_outlined),
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _confirmPasswordCtrl,
                            obscureText: true,
                            decoration: const InputDecoration(
                              labelText: 'Confirm new password',
                              prefixIcon: Icon(Icons.password_outlined),
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),
                          FilledButton.tonalIcon(
                            onPressed: _savingPassword
                                ? null
                                : _changePassword,
                            icon: _savingPassword
                                ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                        strokeWidth: 2.2))
                                : const Icon(Icons.shield_outlined),
                            label: const Text('Update Password'),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 24),
                  OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      foregroundColor: theme.colorScheme.error,
                      side: BorderSide(color: theme.colorScheme.error),
                      minimumSize: const Size.fromHeight(50),
                    ),
                    onPressed: () async {
                      await ref
                          .read(authControllerProvider.notifier)
                          .signOut();
                    },
                    icon: const Icon(Icons.logout_rounded),
                    label: const Text('Sign Out'),
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

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.theme, this.title);

  final ThemeData theme;
  final String title;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(4, 18, 0, 10),
        child: Text(
          title,
          style: theme.textTheme.titleMedium
              ?.copyWith(fontWeight: FontWeight.w800),
        ),
      );
}
