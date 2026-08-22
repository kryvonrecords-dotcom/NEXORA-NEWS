# ProGuard / R8 Optimization & Obfuscation Rules for Nexora News

# Preserve Data Models for GSON & Serialization
-keep class com.nexoranews.app.data.model.** { *; }
-keepclassmembers class com.nexoranews.app.data.model.** { *; }

# GSON configuration
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Glide Image Loading Library Rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# ViewBinding
-keepclassmembers class com.nexoranews.app.databinding.** {
    public static ** inflate(...);
    public static ** bind(...);
    public ** getRoot();
}

# AndroidX Architecture & Lifecycle
-keep class androidx.lifecycle.** { *; }
-keep class androidx.fragment.app.** { *; }
-keep class androidx.appcompat.widget.** { *; }
-keep class com.google.android.material.** { *; }

# Google Mobile Ads (AdMob) SDK Rules
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# User Messaging Platform (UMP) SDK
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve line numbers and source file names for readable stack traces
-keepattributes SourceFile,LineNumberTable,EnclosingMethod,InnerClasses
-renamesourcefileattribute SourceFile

# Optimize network and JSON serialization
-dontwarn java.lang.invoke.**
