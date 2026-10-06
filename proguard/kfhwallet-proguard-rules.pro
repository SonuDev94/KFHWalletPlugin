# --- BouncyCastle: keep everything, including internal SPI classes ---
# BC's provider registers algorithms via reflective Class.forName() lookups
# using string class names. If R8 renames or strips these, JCA lookups
# (KeyStore.getInstance("BKS"), etc.) fail at runtime with NoSuchAlgorithmException.

-keep class org.bouncycastle.** { *; }
-keepclassmembers class org.bouncycastle.** { *; }
-keepnames class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Explicitly keep the provider class and its keystore SPI implementations
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }
-keep class org.bouncycastle.jcajce.provider.** { *; }

# Keep the security provider registration mechanism itself
-keep class java.security.Provider
-keepclassmembers class java.security.Provider {
    <init>(...);
}

# Preserve service-loader / provider metadata if present
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes Signature