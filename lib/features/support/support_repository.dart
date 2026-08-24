import '../../../core/api/api_client.dart';

class TicketMessage {
  const TicketMessage({
    required this.id,
    required this.message,
    required this.isStaff,
    this.senderName,
    this.createdAt,
  });

  final String id;
  final String message;
  final bool isStaff;
  final String? senderName;
  final DateTime? createdAt;

  factory TicketMessage.fromJson(Map<String, dynamic> j) => TicketMessage(
        id: (j['id'] ?? '').toString(),
        message: (j['message'] ?? '').toString(),
        isStaff: ((j['senderType'] ??
                    (j['isStaff'] == true
                        ? 'staff'
                        : (j['senderRole'] ?? '')))
                .toString())
            .toLowerCase()
            .contains('staff'),
        senderName: j['senderName']?.toString() ??
            ((j['sender'] is Map<String, dynamic>)
                ? (j['sender'] as Map<String, dynamic>)['name']?.toString()
                : null),
        createdAt: DateTime.tryParse((j['createdAt'] ?? '').toString()),
      );
}

class SupportTicketDetail {
  const SupportTicketDetail({
    required this.id,
    required this.subject,
    required this.status,
    required this.messages,
  });

  final String id;
  final String subject;
  final String status;
  final List<TicketMessage> messages;

  factory SupportTicketDetail.fromJson(
    Map<String, dynamic> j,
    String myUserId,
  ) {
    final raw = j['messages'];
    final messages = raw is List
        ? raw
            .whereType<Map<String, dynamic>>()
            .map((m) => TicketMessage.fromJson(m))
            .toList()
        : <TicketMessage>[];
    return SupportTicketDetail(
      id: (j['id'] ?? '').toString(),
      subject: (j['subject'] ?? '').toString(),
      status: (j['status'] ?? '').toString(),
      messages: messages,
    );
  }
}

class SupportRepository {
  SupportRepository(this._client);

  final ApiClient _client;

  Future<void> createTicket({
    required String subject,
    required String message,
  }) =>
      _client.postJson(
        '/support/tickets',
        body: {'subject': subject, 'message': message},
      );

  Future<List<Map<String, dynamic>>> myTickets() async {
    final body = await _client.getJsonPage('/support/tickets');
    return body.$1.whereType<Map<String, dynamic>>().toList();
  }

  Future<SupportTicketDetail> ticket(String id) async {
    final data = await _client.getJson('/support/tickets/$id');
    return SupportTicketDetail.fromJson(data, _client.userId ?? '');
  }

  Future<void> addMessage(String id, String message) =>
      _client.postJson('/support/tickets/$id/messages',
          body: {'message': message});
}
