# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for better Crashlytics reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase Crashlytics
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keep class com.google.android.gms.** { *; }

# Keep model classes to preserve stack traces in Crashlytics
-keep class com.prepstack.domain.model.** { *; }
-keep class com.prepstack.data.dto.** { *; }

# Jetpack Compose rules
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Room Database
-keep class com.prepstack.bookmarks.data.** { *; }

# Coroutines/Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclasseswithmembers class kotlinx.coroutines.** { 
    *;
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}