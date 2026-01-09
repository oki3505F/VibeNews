# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

-dontwarn org.xmlpull.v1.**
-keep class org.xmlpull.v1.** { *; }
-dontwarn javax.xml.stream.**
-dontwarn javax.xml.namespace.QName

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
