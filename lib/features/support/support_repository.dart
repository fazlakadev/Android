import '../../../core/api/api_client.dart';

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
}
