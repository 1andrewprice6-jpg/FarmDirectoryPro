# ProGuard rules for Farm Directory App
# Optimized to reduce APK size while preserving functionality

# Keep app entry point
-keep class com.example.farmdirectoryupgraded.MainActivity { *; }

# Keep custom application classes (but allow obfuscation and optimization)
-keep class com.example.farmdirectoryupgraded.data.** { *; }
-keep class com.example.farmdirectoryupgraded.ui.** { *; }

# Room database - keep entity classes and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep @androidx.room.Query class *
-dontwarn androidx.room.paging.**

# Keep Room generated code
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }

# Gson - required for JSON serialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.internal.bind.** { *; }

# OkHttp - WebSocket functionality
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep interface okhttp3.** { *; }
-keep interface okio.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# OkHttp WebSocket internals
-keep class okhttp3.internal.ws.** { *; }

# ML Kit Text Recognition - critical for camera feature
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-keep interface com.google.mlkit.** { *; }
-keep interface com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep ML Kit models
-keep class com.google.mlkit.vision.** { *; }

# CameraX - keep implementation classes
-keep class androidx.camera.** { *; }
-keep interface androidx.camera.** { *; }

# Coroutines - keep essential classes
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.** { *; }

# Keep Kotlin metadata for reflection
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations

# Preserve data classes and enums
-keep class **$WhenMappings
-keep class * extends java.lang.Enum { *; }

# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Optimization settings
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryinterfaces

# Remove logging in release builds (optional - comment out if you want logs)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** println(...);
}

# Verbose output for debugging ProGuard
-verbose

# Keep synthetic methods and inner classes
-keep class **.BuildConfig { *; }
-keep class **.R$* { *; }

# Location services
-keep class com.google.android.gms.location.** { *; }
-keep interface com.google.android.gms.location.** { *; }

# Security crypto
-keep class androidx.security.crypto.** { *; }

# Material Design 3
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.material.** { *; }

# Fragment and Navigation
-keep class androidx.fragment.app.Fragment { *; }
-keep class androidx.navigation.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }
