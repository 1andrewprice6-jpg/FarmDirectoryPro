# Farm Directory ProGuard Rules
# These rules optimize and obfuscate the release build for security

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove logging in release builds for security
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# ========== Android & AndroidX ==========
-keep class androidx.** { *; }
-dontwarn androidx.**

# ========== Kotlin ==========
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# ========== Jetpack Compose ==========
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ========== Room Database ==========
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class com.example.farmdirectoryupgraded.data.** { *; }

# ========== Security - Keep encryption classes ==========
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-keep class com.example.farmdirectoryupgraded.security.** { *; }

# ========== Validation & Sanitization ==========
-keep class com.example.farmdirectoryupgraded.utils.ValidationUtils { *; }
-keep class com.example.farmdirectoryupgraded.utils.SanitizationUtils { *; }

# ========== Gson ==========
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.example.farmdirectoryupgraded.data.Farmer { *; }
-keep class com.example.farmdirectoryupgraded.data.WebSocketModels** { *; }
-keep class com.example.farmdirectoryupgraded.data.AttendanceRecord { *; }
-keep class com.example.farmdirectoryupgraded.data.LogEntry { *; }

# ========== Socket.IO & OkHttp ==========
-keep class io.socket.** { *; }
-keep class okio.** { *; }
-keep class okhttp3.** { *; }
-dontwarn io.socket.**
-dontwarn okio.**
-dontwarn okhttp3.**

# ========== ViewModels ==========
-keep class com.example.farmdirectoryupgraded.viewmodel.** { *; }

# ========== Optimization Settings ==========
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose
