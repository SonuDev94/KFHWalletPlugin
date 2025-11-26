# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# This file is intended to be used along with the default Android proguard
# configuration available in AndroidSdk/tools/proguard/proguard-android.txt,
# specified in the app's build.gradle as:
# proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'

-dontwarn java.beans.**
-dontwarn javax.annotation.**
-dontwarn javax.naming.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn org.jetbrains.annotations.**
-dontwarn org.slf4j.**
-dontwarn org.w3c.dom.bootstrap.**
-keep class okhttp3.** { *; }
-keep class org.bouncycastle.** { *; }
-dontwarn ch.**
-dontwarn org.apache.**
-keep class rkkkkk.** { *; }
-dontwarn rkkkkk.**
-keep class com.idemia.wa.api.customer.** { *; }
-keep class com.idemia.wa.nfc.payment.wise.bundle.v25.V25CredentialDeserializer
{ *; }

-keep class com.fasterxml.jackson.** { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* *;
}
-dontwarn com.fasterxml.jackson.databind.**

-keepclassmembers public class * {
    @com.fasterxml.jackson.databind.* *;
}

-keep class retrofit2.** { *; }

-keepattributes Signature,*Annotation*,EnclosingMethod,Exceptions
-keep class * implements java.io.Serializable

-keep class kotlin.Metadata { *; }

-keep class kotlin.reflect.** { *; }
-keepnames class * { *; }


-keep class com.google.android.gms.** { *; }

-keep class okhttp3.** { *; }

-ignorewarnings