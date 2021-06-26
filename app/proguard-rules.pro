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

#-----Glide Start-------#
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#-----Glide End-------#

#------Model Class------#
-keep class com.mobiversa.ezy2pay.network.response.** { *; }
-dontwarn com.mobiversa.ezy2pay.network.response.**
-keep class com.mobiversa.ezy2pay.network.** { *; }
-keep class com.mobiversa.ezy2pay.ui.** { *; }
-keep class com.mobiversa.ezy2pay.base.** { *; }
-keep class com.mobiversa.ezy2pay.adapter.** { *; }

-keep class com.bbpos.** {*;}

#-------Kotlin---------#
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
#-----Kotlin End----------#

#-------------------For AppCompact Pro Gurad Rules no needed-----------------#
-keep class androidx.appcompat.widget.** { *; }
-keep class android.support.v7.widget.** { *; }

#--------Material Designs-----------#
-keep class com.google.android.material.** { *; }

-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

#------Retrofit----------#
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

#-------Google---------#
-keep class com.google.** {*;}
-dontwarn com.google.**
-keep class com.google.firebase.** { *; }

# Proguard config for project using GMS

-keepnames @com.google.android.gms.common.annotation.KeepName class com.google.android.gms.**, com.google.ads.**

-keepclassmembernames class com.google.android.gms.**, com.google.ads.** { @com.google.android.gms.common.annotation.KeepName *; }

# Called by introspection
-keep class com.google.android.gms.**, com.google.ads.** extends java.util.ListResourceBundle { protected java.lang.Object[][] getContents(); }


# This keeps the class name as well as the creator field, because the
# "safe parcelable" can require them during unmarshalling.
-keepnames class com.google.android.gms.**, com.google.ads.** implements android.os.Parcelable { public static final ** CREATOR; }

# com.google.android.gms.auth.api.signin.SignInApiOptions$Builder
# references these classes but no implementation is provided.
-dontnote com.facebook.Session
-dontnote com.facebook.FacebookSdk
-keepnames class com.facebook.Session {}
-keepnames class com.facebook.FacebookSdk {}

# android.app.Notification.setLatestEventInfo() was removed in
# Marsmallow, but is still referenced (safely)
-dontwarn com.google.android.gms.common.GooglePlayServicesUtil

## Flurry 3.4.0 specific rules ##
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepattributes *Annotation*,EnclosingMethod
-keepclasseswithmembers class * {
	public <init>(android.content.Context, android.util.AttributeSet, int);
}

### Fabric
# In order to provide the most meaningful crash reports
-keepattributes SourceFile,LineNumberTable
# If you're using custom Eception
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

### OkHttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

#Finger Print
-keep class de.adorsys.android.finger.*
-dontwarn de.adorsys.android.finger.**