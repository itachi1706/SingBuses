# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Program Files\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

##---------------Begin: proguard configuration for Gson  ----------
## Ref: https://github.com/google/gson/blob/main/gson/src/main/resources/META-INF/proguard/gson.pro
# Keep generic signatures; needed for correct type resolution
-keepattributes Signature

# Keep Gson annotations
# Note: Cannot perform finer selection here to only cover Gson annotations, see also https://stackoverflow.com/q/47515093
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

### The following rules are needed for R8 in "full mode" which only adheres to `-keepattribtues` if
### the corresponding class or field is matches by a `-keep` rule as well, see
### https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#r8-full-mode

# Keep class TypeToken (respectively its generic signature) if present
-if class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken

# Keep any (anonymous) classes extending TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken

# Keep classes with @JsonAdapter annotation
-keep,allowobfuscation,allowoptimization @com.google.gson.annotations.JsonAdapter class *

# Keep fields with any other Gson annotation
# Also allow obfuscation, assuming that users will additionally use @SerializedName or
# other means to preserve the field names
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.Expose <fields>;
  @com.google.gson.annotations.JsonAdapter <fields>;
  @com.google.gson.annotations.Since <fields>;
  @com.google.gson.annotations.Until <fields>;
}

# Keep no-args constructor of classes which can be used with @JsonAdapter
# By default their no-args constructor is invoked to create an adapter instance
-keepclassmembers class * extends com.google.gson.TypeAdapter {
  <init>();
}
-keepclassmembers class * implements com.google.gson.TypeAdapterFactory {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonSerializer {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonDeserializer {
  <init>();
}

# Keep fields annotated with @SerializedName for classes which are referenced.
# If classes with fields annotated with @SerializedName have a no-args
# constructor keep that as well. Based on
# https://issuetracker.google.com/issues/150189783#comment11.
# See also https://github.com/google/gson/pull/2420#discussion_r1241813541
# for a more detailed explanation.
-if class *
-keepclasseswithmembers,allowobfuscation class <1> {
  @com.google.gson.annotations.SerializedName <fields>;
}
-if class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
}

##---------------End: proguard configuration for Gson  ----------

## My tweaks
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-dontobfuscate
-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn com.itachi1706.busarrivalsg.**
#noinspection ShrinkerUnresolvedReference
-keep public class com.itachi1706.busarrivalsg.**

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn kotlin.Experimental$Level
-dontwarn kotlin.Experimental
-dontwarn com.google.errorprone.BugPattern$SeverityLevel
-dontwarn com.google.errorprone.BugPattern
-dontwarn com.google.errorprone.ErrorProneFlags
-dontwarn com.google.errorprone.bugpatterns.BugChecker$MemberReferenceTreeMatcher
-dontwarn com.google.errorprone.bugpatterns.BugChecker$MethodInvocationTreeMatcher
-dontwarn com.google.errorprone.bugpatterns.BugChecker
-dontwarn com.google.errorprone.matchers.Matcher
-dontwarn com.google.errorprone.matchers.Matchers
-dontwarn com.google.errorprone.matchers.NextStatement
-dontwarn com.google.errorprone.matchers.method.MethodMatchers$InstanceMethodMatcher
-dontwarn com.google.errorprone.matchers.method.MethodMatchers$MethodClassMatcher
-dontwarn com.google.errorprone.matchers.method.MethodMatchers$MethodNameMatcher
-dontwarn com.google.errorprone.matchers.method.MethodMatchers$StaticMethodMatcher
-dontwarn com.google.errorprone.matchers.method.MethodMatchers
-dontwarn com.ryanharter.auto.value.gson.GsonTypeAdapterFactory
-dontwarn com.sun.source.tree.Tree$Kind