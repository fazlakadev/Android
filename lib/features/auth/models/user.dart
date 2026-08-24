class User {
  const User({
    required this.id,
    this.email,
    this.name,
    this.username,
    this.avatarUrl,
    this.role,
    this.termsAccepted,
  });

  final String id;
  final String? email;
  final String? name;
  final String? username;
  final String? avatarUrl;
  final String? role;
  final bool? termsAccepted;

  String get displayName {
    if (name != null && name!.trim().isNotEmpty) return name!;
    if (username != null && username!.isNotEmpty) return username!;
    return email ?? 'Fazlaka user';
  }

  factory User.fromJson(Map<String, dynamic> json) => User(
        id: (json['id'] ?? json['publicId'] ?? '').toString(),
        email: json['email'] as String?,
        name: json['name'] as String?,
        username: json['username'] as String?,
        avatarUrl: json['avatarUrl'] as String?,
        role: json['role'] as String?,
        termsAccepted: () {
          final v = json['termsAcceptedAt'];
          if (v is bool) return v;
          return v != null;
        }(),
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'email': email,
        'name': name,
        'username': username,
        'avatarUrl': avatarUrl,
        'role': role,
        'termsAccepted': termsAccepted,
      };

  factory User.fromJsonCache(Map<String, dynamic> json) =>
      User.fromJson(json);

  User copyWithLocal({bool? termsAccepted}) => User(
        id: id,
        email: email,
        name: name,
        username: username,
        avatarUrl: avatarUrl,
        role: role,
        termsAccepted: termsAccepted ?? this.termsAccepted,
      );
}
