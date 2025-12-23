# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /data/data/com.termux/files/home/ChickenFarmApp/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Keep data classes used with Room
-keep class com.example.farmdirectoryupgraded.data.** { *; }

# Keep Jetpack Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# If you use reflection to access classes in your code, you might need to
# add -keeprules to prevent them from being removed.
# -keep class com.example.MyClass
# -keep class com.example.MyClass {
#   public <fields>;
#   public <methods>;
# }
