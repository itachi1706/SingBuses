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
-dontwarn kotlinx.coroutines.scheduling.ExperimentalCoroutineDispatcher
-dontwarn io.ktor.client.call.TypeInfo
-dontwarn io.ktor.client.features.json.JsonSerializer$DefaultImpls
-dontwarn io.ktor.client.features.json.JsonSerializer
-dontwarn io.ktor.utils.io.core.Input
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
