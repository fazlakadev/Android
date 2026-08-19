# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class com.fazlaka.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.fazlaka.app.**$$serializer { *; }

# Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, Exceptions

# Room
-dontwarn androidx.room.**

# Media3
-keep class androidx.media3.** { *; }

# Hilt / Dagger
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Pusher
-keep class com.pusher.** { *; }
-dontwarn com.pusher.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Google Play Services Location
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.measurement.** { *; }

# AndroidX Splash Screen
-keep class androidx.core.splashscreen.** { *; }
-dontwarn androidx.core.splashscreen.**

# SLF4J (referenced by Pusher)
-dontwarn org.slf4j.**
