class AppVersionInfo {
  const AppVersionInfo({
    required this.version,
    required this.tagName,
    required this.releaseNotes,
    required this.downloadUrl,
    this.htmlUrl,
    this.forceUpdate = false,
    this.forceUpdateMessage,
    this.minVersion,
  });

  final String version;
  final String tagName;
  final String releaseNotes;
  final String downloadUrl;
  final String? htmlUrl;
  final bool forceUpdate;
  final String? forceUpdateMessage;
  final String? minVersion;

  factory AppVersionInfo.fromBackendJson(Map<String, dynamic> j) {
    return AppVersionInfo(
      version: _clean(j['version'] as String? ?? ''),
      tagName: j['tagName'] as String? ?? '',
      releaseNotes: j['releaseNotes'] as String? ?? '',
      downloadUrl: j['downloadUrl'] as String? ?? '',
      htmlUrl: j['htmlUrl'] as String?,
      forceUpdate: j['forceUpdate'] == true,
      forceUpdateMessage: j['forceUpdateMessage'] as String?,
      minVersion: j['minVersion'] as String?,
    );
  }

  factory AppVersionInfo.fromGithubJson(Map<String, dynamic> j) {
    final assets = (j['assets'] as List? ?? const []);
    String url = '';
    for (final a in assets) {
      final link = a['browser_download_url'] as String?;
      final name = (a['name'] as String?)?.toLowerCase() ?? '';
      if (link != null && name.endsWith('.apk')) {
        url = link;
        break;
      }
    }
    return AppVersionInfo(
      version: _clean(j['tag_name'] as String? ?? ''),
      tagName: j['tag_name'] as String? ?? '',
      releaseNotes: j['body'] as String? ?? '',
      downloadUrl: url,
      htmlUrl: j['html_url'] as String?,
    );
  }

  bool get isDownloadable => downloadUrl.isNotEmpty;

  static String _clean(String s) => s.trim().replaceFirst(RegExp('^v'), '');

  /// True when [a] is strictly newer than [b] (semver-ish x.y.z).
  static bool isNewer(String a, String b) {
    final pa = _parts(a);
    final pb = _parts(b);
    for (var i = 0; i < 3; i++) {
      if (pa[i] > pb[i]) return true;
      if (pa[i] < pb[i]) return false;
    }
    return false;
  }

  static List<int> _parts(String v) {
    final core = v.split('+').first.split('-').first;
    final nums = core.split('.').map((e) => int.tryParse(e) ?? 0).toList();
    while (nums.length < 3) {
      nums.add(0);
    }
    return nums.sublist(0, 3);
  }

  bool isNewerThan(String current) =>
      isNewer(version, current) ||
      (minVersion != null &&
          minVersion!.isNotEmpty &&
          isNewer(minVersion!, current));
}
