import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../content/providers.dart';

class SupportScreen extends ConsumerStatefulWidget {
  const SupportScreen({super.key});

  @override
  ConsumerState<SupportScreen> createState() => _SupportScreenState();
}

class _SupportScreenState extends ConsumerState<SupportScreen> {
  List<Map<String, dynamic>>? _tickets;
  bool _busy = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _busy = true);
    try {
      final tickets = await ref.read(supportRepositoryProvider).myTickets();
      if (!mounted) return;
      setState(() => _tickets = tickets);
    } catch (_) {
      if (mounted) setState(() => _tickets = const []);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _newTicket() async {
    final subject = TextEditingController();
    final message = TextEditingController();
    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
        ),
        child: SafeArea(
          top: false,
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text('تذكرة دعم جديدة',
                    style: Theme.of(context)
                        .textTheme
                        .titleLarge
                        ?.copyWith(fontWeight: FontWeight.w800)),
                const SizedBox(height: 16),
                TextField(
                  controller: subject,
                  decoration: const InputDecoration(
                    labelText: 'الموضوع',
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: message,
                  maxLines: 4,
                  decoration: const InputDecoration(
                    labelText: 'اشرح مشكلتك…',
                    alignLabelWithHint: true,
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 16),
                FilledButton(
                  onPressed: () {
                    if (subject.text.trim().isEmpty ||
                        message.text.trim().isEmpty) {
                      return;
                    }
                    Navigator.pop(context, true);
                  },
                  child: const Text('إرسال'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
    if (ok != true || !mounted) return;
    try {
      await ref.read(supportRepositoryProvider).createTicket(
            subject: subject.text.trim(),
            message: message.text.trim(),
          );
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('تم إرسال التذكرة، سنرد عليك قريبًا')),
      );
      _load();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString())),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: const Text('الدعم الفني')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _newTicket,
        icon: const Icon(Icons.add_rounded),
        label: const Text('تذكرة جديدة'),
      ),
      body: _busy
          ? const Center(child: CircularProgressIndicator())
          : (_tickets == null || _tickets!.isEmpty)
              ? Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.support_agent_rounded,
                          size: 56, color: theme.colorScheme.outlineVariant),
                      const SizedBox(height: 10),
                      Text('لا توجد تذاكر بعد',
                          style: TextStyle(color: theme.colorScheme.outline)),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.separated(
                    physics: const AlwaysScrollableScrollPhysics(),
                    padding: const EdgeInsets.fromLTRB(12, 8, 12, 90),
                    itemCount: _tickets!.length,
                    separatorBuilder: (_, _) => const SizedBox(height: 8),
                    itemBuilder: (context, i) {
                      final t = _tickets![i];
                      final status = (t['status'] ?? '').toString();
                      return Card(
                        elevation: 0,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                          side: BorderSide(color: theme.colorScheme.outlineVariant),
                        ),
                        child: ListTile(
                          leading: Icon(
                            switch (status) {
                              'OPEN' || 'IN_PROGRESS' => Icons.schedule_rounded,
                              'RESOLVED' || 'CLOSED' => Icons.check_circle_rounded,
                              _ => Icons.confirmation_number_rounded,
                            },
                            color: status == 'RESOLVED' || status == 'CLOSED'
                                ? Colors.green
                                : theme.colorScheme.primary,
                          ),
                          title: Text((t['subject'] ?? '').toString(),
                              maxLines: 1, overflow: TextOverflow.ellipsis),
                          subtitle: (t['messages'] is List &&
                                  (t['messages'] as List).isNotEmpty)
                              ? Text(
                                  (((t['messages'] as List).last
                                              as Map)['message'] ??
                                          '')
                                      .toString(),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis)
                              : null,
                          trailing: Text(status,
                              style: theme.textTheme.labelSmall
                                  ?.copyWith(color: theme.colorScheme.outline)),
                        ),
                      );
                    },
                  ),
                ),
    );
  }
}
