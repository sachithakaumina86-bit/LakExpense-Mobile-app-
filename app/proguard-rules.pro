# LakExpense ProGuard / R8 Keep Rules
# Preserves debugging line numbers and reflection-accessed AndroidX / Room / Vault classes
# when R8 optimization is enabled on release builds.

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database & Entities
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keepclassmembers class com.example.data.local.** {
    <init>(...);
    *;
}

# Kotlin Coroutines & Metadata
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.flow.**

# Biometric & FileProvider
-keep class androidx.biometric.** { *; }
-keep class androidx.core.content.FileProvider { *; }
