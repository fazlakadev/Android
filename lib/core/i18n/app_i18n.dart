import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Runtime language used by repositories / model translation pickers
/// (they are plain Dart classes without BuildContext).
AppLanguage i18nRuntime = AppLanguage.ar;

enum AppLanguage { ar, en, fr }

extension AppLanguageX on AppLanguage {
  Locale get locale => Locale(name);
  String get nativeName => switch (this) {
        AppLanguage.ar => 'العربية',
        AppLanguage.en => 'English',
        AppLanguage.fr => 'Français',
      };
  String get flag => switch (this) {
        AppLanguage.ar => '🇪🇬',
        AppLanguage.en => '🇬🇧',
        AppLanguage.fr => '🇫🇷',
      };
}

abstract class S {
  const S();
  // Tabs & shell
  String get home;
  String get seasons;
  String get episodes;
  String get articles;
  String get search;
  String get menu;
  String get settings;
  String get viewAll;
  String get general;
  String get profile;
  String get rateEpisode;
  String get notifications;
  String get friends;
  String get accountSettings;
  String get chats;
  String get playlists;
  String get support;
  String get signOut;
  String get signOutConfirm;
  String get cancel;
  String get signOutAction;

  // Updates
  String get appInfo;
  String get currentVersion;
  String get checkUpdates;
  String get upToDate;
  String get updateAvailableTitle;
  String get whatsNew;
  String get updateNow;
  String get later;
  String get downloadingUpdate;
  String get downloadDone;
  String get installNow;
  String get updateCheckFailed;

  // Auth
  String get welcomeTitle;
  String get continueWithGoogle;

  // Common
  String get retry;
  String get nothingFound;
  String get noArticlesYet;
  String get noSeasonsYet;
  String get latestEpisodes;
  String get latestArticles;
  String get comments;
  String get addToPlaylist;
  String get delete;
  String get close;
  String get save;
  String get send;
  String get loading;
  String get errorGeneric;

  // Search
  String get searchHint;
  String get searchPrompt;
  String get noResults;
  String get catEpisodes;
  String get catArticles;
  String get catSeasons;
  String get catPlaylists;

  // Notifications
  String get noNotifications;

  // Support
  String get newTicket;
  String get ticketSubject;
  String get ticketMessage;
  String get ticketSent;
  String get noTickets;

  // Player
  String get listenToEpisode;
  String get nowPlaying;
  String get resumeListening;
  String get buffering;
  String couldNotPlay(String e);

  // Rating
  String rateThis(String what);
  String ratingLabel(double avg, int count);

  // Playlists
  String get myPlaylists;
  String get newPlaylistName;
  String get createPlaylist;
  String get noPlaylistsYet;
  String episodesCount(int n);
  String get addedToPlaylist;
  String get removeFromPlaylist;

  // Chat
  String get messagePlaceholder;
  String get typeMessage;

  // Friends
  String get myFriends;
  String get requests;
  String get suggestions;
  String get removeFriend;

  // Settings
  String get appearance;
  String get lightMode;
  String get darkMode;
  String get systemMode;
  String get language;
}

class SAr extends S {
  const SAr();
  @override
  final String home = 'الرئيسية';
  @override
  final String seasons = 'المواسم';
  @override
  final String episodes = 'الحلقات';
  @override
  final String articles = 'المقالات';
  @override
  final String search = 'بحث';
  @override
  final String menu = 'القائمة';
  @override
  final String settings = 'الإعدادات';
  @override
  final String viewAll = 'عرض الكل';
  @override
  final String general = 'عام';
  @override
  final String profile = 'الملف الشخصي';
  @override
  final String rateEpisode = 'قيم الحلقة';
  @override
  final String notifications = 'الإشعارات';
  @override
  final String friends = 'الأصدقاء';
  @override
  final String accountSettings = 'إعدادات الحساب';
  @override
  final String chats = 'المحادثات';
  @override
  final String playlists = 'قوائم التشغيل';
  @override
  final String support = 'الدعم الفني';
  @override
  final String signOut = 'تسجيل الخروج';
  @override
  final String signOutConfirm = 'ستحتاج لتسجيل الدخول مرة أخرى للمتابعة.';
  @override
  final String cancel = 'إلغاء';
  @override
  final String signOutAction = 'خروج';

  @override
  final String appInfo = 'التطبيق';
  @override
  final String currentVersion = 'الإصدار الحالي';
  @override
  final String checkUpdates = 'فحص التحديثات';
  @override
  final String upToDate = 'أنت تستخدم أحدث إصدار';
  @override
  final String updateAvailableTitle = 'تحديث جديد متاح!';
  @override
  final String whatsNew = 'ما الجديد';
  @override
  final String updateNow = 'تحديث الآن';
  @override
  final String later = 'لاحقًا';
  @override
  final String downloadingUpdate = 'جاري تنزيل التحديث…';
  @override
  final String downloadDone = 'تم التنزيل، جاهز للتثبيت';
  @override
  final String installNow = 'تثبيت الآن';
  @override
  final String updateCheckFailed = 'تعذر التحقق من التحديثات';
  @override
  final String welcomeTitle = 'مرحبًا بك في فضلكة';
  @override
  final String continueWithGoogle = 'المتابعة بحساب Google';
  @override
  final String retry = 'إعادة المحاولة';
  @override
  final String nothingFound = 'لا توجد نتائج';
  @override
  final String noArticlesYet = 'لا توجد مقالات بعد';
  @override
  final String noSeasonsYet = 'لا توجد مواسم بعد';
  @override
  final String latestEpisodes = 'أحدث الحلقات';
  @override
  final String latestArticles = 'أحدث المقالات';
  @override
  final String comments = 'التعليقات';
  @override
  final String addToPlaylist = 'أضف إلى قائمة';
  @override
  final String delete = 'حذف';
  @override
  final String close = 'إغلاق';
  @override
  final String save = 'حفظ';
  @override
  final String send = 'إرسال';
  @override
  final String loading = 'جارٍ التحميل…';
  @override
  final String errorGeneric = 'حدث خطأ ما';
  @override
  final String searchHint = 'ابحث عن حلقة، مقال، موسم…';
  @override
  final String searchPrompt = 'اكتب للبحث في كل المحتوى';
  @override
  final String noResults = 'لا توجد نتائج';
  @override
  final String catEpisodes = 'حلقات';
  @override
  final String catArticles = 'مقالات';
  @override
  final String catSeasons = 'مواسم';
  @override
  final String catPlaylists = 'قوائم';
  @override
  final String noNotifications = 'لا توجد إشعارات';
  @override
  final String newTicket = 'تذكرة جديدة';
  @override
  final String ticketSubject = 'الموضوع';
  @override
  final String ticketMessage = 'اشرح مشكلتك…';
  @override
  final String ticketSent = 'تم إرسال التذكرة، سنرد عليك قريبًا';
  @override
  final String noTickets = 'لا توجد تذاكر بعد';
  @override
  final String listenToEpisode = 'استمع للحلقة';
  @override
  final String nowPlaying = 'قيد التشغيل الآن';
  @override
  final String resumeListening = 'متابعة الاستماع';
  @override
  final String buffering = 'جارٍ التحميل…';
  @override
  String couldNotPlay(String e) => 'تعذر تشغيل الصوت';
  @override
  String rateThis(String what) => 'قيّم $what';
  @override
  String ratingLabel(double avg, int count) =>
      '${avg.toStringAsFixed(1)} ($count تقييم)';
  @override
  final String myPlaylists = 'قوائمي';
  @override
  final String newPlaylistName = 'اسم القائمة الجديدة…';
  @override
  final String createPlaylist = 'إنشاء قائمة';
  @override
  final String noPlaylistsYet = 'لا توجد قوائم بعد';
  @override
  String episodesCount(int n) => '$n حلقات';
  @override
  final String addedToPlaylist = 'تمت الإضافة إلى القائمة';
  @override
  final String removeFromPlaylist = 'إزالة من القائمة';
  @override
  final String messagePlaceholder = 'اكتب رسالة…';
  @override
  final String typeMessage = 'اكتب رسالة…';
  @override
  final String myFriends = 'أصدقائي';
  @override
  final String requests = 'الطلبات';
  @override
  final String suggestions = 'مقترحات';
  @override
  final String removeFriend = 'إزالة صديق';
  @override
  final String appearance = 'المظهر';
  @override
  final String lightMode = 'فاتح';
  @override
  final String darkMode = 'داكن';
  @override
  final String systemMode = 'النظام';
  @override
  final String language = 'اللغة';
}

class SEn extends S {
  const SEn();
  @override
  final String home = 'Home';
  @override
  final String seasons = 'Seasons';
  @override
  final String episodes = 'Episodes';
  @override
  final String articles = 'Articles';
  @override
  final String search = 'Search';
  @override
  final String menu = 'Menu';
  @override
  final String settings = 'Settings';
  @override
  final String viewAll = 'View all';
  @override
  final String general = 'General';
  @override
  final String profile = 'Profile';
  @override
  final String rateEpisode = 'Rate this episode';
  @override
  final String notifications = 'Notifications';
  @override
  final String friends = 'Friends';
  @override
  final String accountSettings = 'Account Settings';
  @override
  final String chats = 'Chats';
  @override
  final String playlists = 'Playlists';
  @override
  final String support = 'Support';
  @override
  final String signOut = 'Sign Out';
  @override
  final String signOutConfirm = 'You will need to sign in again to continue.';
  @override
  final String cancel = 'Cancel';
  @override
  final String signOutAction = 'Sign out';

  @override
  final String appInfo = 'App';
  @override
  final String currentVersion = 'Current version';
  @override
  final String checkUpdates = 'Check for updates';
  @override
  final String upToDate = "You're on the latest version";
  @override
  final String updateAvailableTitle = 'Update available!';
  @override
  final String whatsNew = "What's new";
  @override
  final String updateNow = 'Update now';
  @override
  final String later = 'Later';
  @override
  final String downloadingUpdate = 'Downloading update…';
  @override
  final String downloadDone = 'Downloaded, ready to install';
  @override
  final String installNow = 'Install now';
  @override
  final String updateCheckFailed = 'Could not check for updates';
  @override
  final String welcomeTitle = 'Welcome to Fazlaka';
  @override
  final String continueWithGoogle = 'Continue with Google';
  @override
  final String retry = 'Retry';
  @override
  final String nothingFound = 'Nothing found';
  @override
  final String noArticlesYet = 'No articles yet';
  @override
  final String noSeasonsYet = 'No seasons yet';
  @override
  final String latestEpisodes = 'Latest Episodes';
  @override
  final String latestArticles = 'Latest Articles';
  @override
  final String comments = 'Comments';
  @override
  final String addToPlaylist = 'Add to playlist';
  @override
  final String delete = 'Delete';
  @override
  final String close = 'Close';
  @override
  final String save = 'Save';
  @override
  final String send = 'Send';
  @override
  final String loading = 'Loading…';
  @override
  final String errorGeneric = 'Something went wrong';
  @override
  final String searchHint = 'Search episodes, articles, seasons…';
  @override
  final String searchPrompt = 'Type to search all content';
  @override
  final String noResults = 'No results';
  @override
  final String catEpisodes = 'Episodes';
  @override
  final String catArticles = 'Articles';
  @override
  final String catSeasons = 'Seasons';
  @override
  final String catPlaylists = 'Playlists';
  @override
  final String noNotifications = 'No notifications';
  @override
  final String newTicket = 'New ticket';
  @override
  final String ticketSubject = 'Subject';
  @override
  final String ticketMessage = 'Describe your issue…';
  @override
  final String ticketSent = 'Ticket sent, we will reply soon';
  @override
  final String noTickets = 'No tickets yet';
  @override
  final String listenToEpisode = 'Listen to episode';
  @override
  final String nowPlaying = 'Now playing';
  @override
  final String resumeListening = 'Resume listening';
  @override
  final String buffering = 'Buffering…';
  @override
  String couldNotPlay(String e) => 'Could not play audio';
  @override
  String rateThis(String what) => 'Rate this $what';
  @override
  String ratingLabel(double avg, int count) =>
      '${avg.toStringAsFixed(1)} ($count ratings)';
  @override
  final String myPlaylists = 'My Playlists';
  @override
  final String newPlaylistName = 'New playlist name…';
  @override
  final String createPlaylist = 'Create playlist';
  @override
  final String noPlaylistsYet = 'No playlists yet';
  @override
  String episodesCount(int n) => '$n episodes';
  @override
  final String addedToPlaylist = 'Added to playlist';
  @override
  final String removeFromPlaylist = 'Remove from playlist';
  @override
  final String messagePlaceholder = 'Type a message…';
  @override
  final String typeMessage = 'Type a message…';
  @override
  final String myFriends = 'My Friends';
  @override
  final String requests = 'Requests';
  @override
  final String suggestions = 'Suggestions';
  @override
  final String removeFriend = 'Remove friend';
  @override
  final String appearance = 'Appearance';
  @override
  final String lightMode = 'Light';
  @override
  final String darkMode = 'Dark';
  @override
  final String systemMode = 'System';
  @override
  final String language = 'Language';
}

class SFr extends S {
  const SFr();
  @override
  final String home = 'Accueil';
  @override
  final String seasons = 'Saisons';
  @override
  final String episodes = 'Épisodes';
  @override
  final String articles = 'Articles';
  @override
  final String search = 'Recherche';
  @override
  final String menu = 'Menu';
  @override
  final String settings = 'Paramètres';
  @override
  final String viewAll = 'Tout voir';
  @override
  final String general = 'Général';
  @override
  final String profile = 'Profil';
  @override
  final String rateEpisode = 'Notez cet épisode';
  @override
  final String notifications = 'Notifications';
  @override
  final String friends = 'Amis';
  @override
  final String accountSettings = 'Paramètres du compte';
  @override
  final String chats = 'Discussions';
  @override
  final String playlists = 'Playlists';
  @override
  final String support = 'Assistance';
  @override
  final String signOut = 'Se déconnecter';
  @override
  final String signOutConfirm =
      'Vous devrez vous reconnecter pour continuer.';
  @override
  final String cancel = 'Annuler';
  @override
  final String signOutAction = 'Déconnexion';

  @override
  final String appInfo = 'Application';
  @override
  final String currentVersion = 'Version actuelle';
  @override
  final String checkUpdates = 'Vérifier les mises à jour';
  @override
  final String upToDate = 'Vous utilisez la dernière version';
  @override
  final String updateAvailableTitle = 'Mise à jour disponible !';
  @override
  final String whatsNew = 'Nouveautés';
  @override
  final String updateNow = 'Mettre à jour';
  @override
  final String later = 'Plus tard';
  @override
  final String downloadingUpdate = 'Téléchargement de la mise à jour…';
  @override
  final String downloadDone = 'Téléchargé, prêt à installer';
  @override
  final String installNow = 'Installer';
  @override
  final String updateCheckFailed = 'Impossible de vérifier les mises à jour';
  @override
  final String welcomeTitle = 'Bienvenue chez Fazlaka';
  @override
  final String continueWithGoogle = 'Continuer avec Google';
  @override
  final String retry = 'Réessayer';
  @override
  final String nothingFound = 'Aucun résultat';
  @override
  final String noArticlesYet = 'Pas encore d\u2019articles';
  @override
  final String noSeasonsYet = 'Pas encore de saisons';
  @override
  final String latestEpisodes = 'Derniers épisodes';
  @override
  final String latestArticles = 'Derniers articles';
  @override
  final String comments = 'Commentaires';
  @override
  final String addToPlaylist = 'Ajouter à une playlist';
  @override
  final String delete = 'Supprimer';
  @override
  final String close = 'Fermer';
  @override
  final String save = 'Enregistrer';
  @override
  final String send = 'Envoyer';
  @override
  final String loading = 'Chargement…';
  @override
  final String errorGeneric = 'Une erreur est survenue';
  @override
  final String searchHint = 'Chercher épisodes, articles, saisons…';
  @override
  final String searchPrompt = 'Tapez pour rechercher tout le contenu';
  @override
  final String noResults = 'Aucun résultat';
  @override
  final String catEpisodes = 'Épisodes';
  @override
  final String catArticles = 'Articles';
  @override
  final String catSeasons = 'Saisons';
  @override
  final String catPlaylists = 'Playlists';
  @override
  final String noNotifications = 'Aucune notification';
  @override
  final String newTicket = 'Nouveau ticket';
  @override
  final String ticketSubject = 'Sujet';
  @override
  final String ticketMessage = 'Décrivez votre problème…';
  @override
  final String ticketSent = 'Ticket envoyé, réponse bientôt';
  @override
  final String noTickets = 'Pas encore de tickets';
  @override
  final String listenToEpisode = 'Écouter l\u2019épisode';
  @override
  final String nowPlaying = 'En cours de lecture';
  @override
  final String resumeListening = 'Reprendre l\u2019écoute';
  @override
  final String buffering = 'Chargement…';
  @override
  String couldNotPlay(String e) => 'Impossible de lire le son';
  @override
  String rateThis(String what) => 'Notez ce $what';
  @override
  String ratingLabel(double avg, int count) =>
      '${avg.toStringAsFixed(1)} ($count notes)';
  @override
  final String myPlaylists = 'Mes playlists';
  @override
  final String newPlaylistName = 'Nom de la nouvelle playlist…';
  @override
  final String createPlaylist = 'Créer une playlist';
  @override
  final String noPlaylistsYet = 'Pas encore de playlists';
  @override
  String episodesCount(int n) => '$n épisodes';
  @override
  final String addedToPlaylist = 'Ajouté à la playlist';
  @override
  final String removeFromPlaylist = 'Retirer de la playlist';
  @override
  final String messagePlaceholder = 'Écrivez un message…';
  @override
  final String typeMessage = 'Écrivez un message…';
  @override
  final String myFriends = 'Mes amis';
  @override
  final String requests = 'Demandes';
  @override
  final String suggestions = 'Suggestions';
  @override
  final String removeFriend = 'Retirer cet ami';
  @override
  final String appearance = 'Apparence';
  @override
  final String lightMode = 'Clair';
  @override
  final String darkMode = 'Sombre';
  @override
  final String systemMode = 'Système';
  @override
  final String language = 'Langue';
}

S stringsOf(AppLanguage l) => switch (l) {
      AppLanguage.ar => const SAr(),
      AppLanguage.en => const SEn(),
      AppLanguage.fr => const SFr(),
    };

/// Provides [S] for the currently selected language and keeps the
/// runtime value in sync for non-widget code.
final sProvider = Provider<S>((ref) {
  final lang = ref.watch(languageControllerProvider);
  return stringsOf(lang);
});

class LanguageController extends Notifier<AppLanguage> {
  @override
  AppLanguage build() {
    _restore();
    return i18nRuntime;
  }

  Future<void> _restore() async {
    final prefs = await SharedPreferences.getInstance();
    final stored = prefs.getString('app_language');
    final lang = AppLanguage.values
        .where((l) => l.name == stored)
        .firstOrNull;
    if (lang != null && lang != state) {
      state = lang;
    }
    i18nRuntime = state;
  }

  Future<void> set(AppLanguage lang) async {
    state = lang;
    i18nRuntime = lang;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('app_language', lang.name);
  }
}

final languageControllerProvider =
    NotifierProvider<LanguageController, AppLanguage>(LanguageController.new);

class ThemeModeController extends Notifier<ThemeMode> {
  @override
  ThemeMode build() {
    _restore();
    return ThemeMode.system;
  }

  Future<void> _restore() async {
    final prefs = await SharedPreferences.getInstance();
    final stored = prefs.getString('app_theme_mode');
    final mode = switch (stored) {
      'light' => ThemeMode.light,
      'dark' => ThemeMode.dark,
      'system' => ThemeMode.system,
      _ => null,
    };
    if (mode != null && mode != state) state = mode;
  }

  Future<void> set(ThemeMode mode) async {
    state = mode;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('app_theme_mode', mode.name);
  }
}

final themeModeControllerProvider =
    NotifierProvider<ThemeModeController, ThemeMode>(ThemeModeController.new);
