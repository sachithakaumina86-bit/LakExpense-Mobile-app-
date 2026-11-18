# LakExpense ProGuard / R8 Keep Rules
# Ensures release builds with R8 optimization (optimization.enable = true)
# preserve Room, Kotlin Coroutines, Jetpack Compose, ViewModels, and Vault APIs.

# Stack trace & debugging metadata
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Room Database, DAOs & Entities
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep class * extends androidx.room.RoomOpenDelegate
-keep class com.example.data.local.** {
    *;
}
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# Kotlin & Coroutines
# ---------------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal

# ---------------------------------------------------------------------------
# Jetpack Compose & Navigation / ViewModel / Lifecycle
# ---------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.Composer { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * implements androidx.lifecycle.ViewModelProvider$Factory {
    public <init>(...);
}
-keep class com.example.ui.viewmodel.LakViewModelFactory { *; }
-keepnames class * extends androidx.navigation.Navigator
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ---------------------------------------------------------------------------
# DataStore Preferences, Biometric, FileProvider & ProfileInstaller
# ---------------------------------------------------------------------------
-keep class androidx.datastore.preferences.** { *; }
-keep class androidx.biometric.** { *; }
-keep class androidx.core.content.FileProvider { *; }
-keep class androidx.profileinstaller.** { *; }
