# Fazlaka Android App (Flutter)

تطبيق موبايل أندرويد احترافي لتطبيق Fazlaka، متصل بالباك اند NestJS، وبيبدأ بتسجيل الدخول بجوجل الأصلي (Credential Manager - بدون WebView).

## البيانات المطلوبة لـ Google Cloud Console

| الإعداد | القيمة |
|---|---|
| **Package name** | `com.fazlaka.app` |
| **SHA-1 (Debug)** | `7F:15:3F:51:3D:B5:8B:45:1C:1B:0B:BE:2A:6D:D7:AA:85:6F:60:40` |
| **SHA-256 (Debug)** | `C9:2D:9C:3F:6B:1D:D6:D0:C3:A5:F6:C7:AF:D4:41:BE:B0:33:7F:26:17:5B:AA:03:88:C1:DA:CC:8A:F4:3F:CF` |

> الـ SHA-1/SHA-256 دي بتاعة الـ debug keystore على الجهاز ده (`%USERPROFILE%\.android\debug.keystore`).
> لازم تتسجل في نفس مشروع Google Cloud (`919871876990`) جوه الـ OAuth client من نوع **Android**.
>
> لو هتعمل release keystore جديد، اعمله وأضف الـ SHA-1 بتاعه كمان في نفس الـ OAuth client.

## إزاي تسجيل الدخول بيشتغل؟

```
Google Sign-In (Native/Credential Manager)
        │  idToken (aud = Web Client ID)
        ▼
POST {API}/api/v1/auth/google/native   { "idToken": "..." }
        │  { accessToken, refreshToken, user }
        ▼
التوكنات تتحفظ في EncryptedSharedPreferences
+ تجديد تلقائي للتوكن عند انتهاء صلاحيته (401 → refresh → retry)
```

- **Android Client ID** (`919871876990-bt6k1athf3faceeake9eu1ii4aqqbq1s...`) → بيتسجل في Google Console بالـ package name + SHA-1 فوق، وهو اللي بيفوض التطبيق نفسه.
- **Web Client ID** (`919871876990-hqb49huhl0gg2osdcg7jv7e39adf9fo1...`) → ده `serverClientId` في التطبيق (موجود في `lib/core/config.dart`) ولازم يكون هو نفسه `GOOGLE_CLIENT_ID` في `.env` بتاع النستJS لأن الباك اند بيعمل verify للتوكن عليه.

## تشغيل التطبيق

```powershell
# المحاكي (الافتراضي يشتغل على http://10.0.2.2:3001)
flutter run

# موبايل حقيقي على نفس شبكة الواي فاي
flutter run --dart-define=API_BASE_URL=http://<IP-الجهاز>:3001

# نسخة الإنتاج
flutter run --dart-define=API_BASE_URL=https://api.your-domain.com --release
```

> متنساش تشغّل الباك اند الأول: `cd ../NestJS && npm run start:dev`

## بنية المشروع

```
lib/
├── main.dart                          # نقطه البداية + الثيم
├── core/
│   ├── config.dart                    # API URL + Google Client IDs
│   ├── api/api_client.dart            # Dio + refresh token تلقائي
│   ├── storage/token_storage.dart     # حفظ آمن للتوكنات
│   └── theme/app_theme.dart           # ثيم Material 3 بألوان البراند
└── features/
    ├── auth/
    │   ├── controllers/auth_controller.dart   # حالة تسجيل الدخول (Riverpod)
    │   ├── data/auth_repository.dart          # endpoints المصادقة
    │   ├── models/user.dart
    │   └── screens/                   # Splash + Login
    └── home/screens/home_screen.dart  # البروفايل بعد الدخول
```

## قبل ما تعمل Release APK

1. اعمل keystore خاص بالإنتاج:
   ```powershell
   keytool -genkey -v -keystore android/app/fazlaka-release.keystore -alias fazlaka -keyalg RSA -keysize 2048 -validity 10000
   ```
2. أضف الـ SHA-1 بتاعه في نفس OAuth client (Android) في Google Console.
3. اضبط signingConfig في `android/app/build.gradle.kts`.
4. اعمل Build:
   ```powershell
   flutter build appbundle --dart-define=API_BASE_URL=https://api.your-domain.com
   ```
