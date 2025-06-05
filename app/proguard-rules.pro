# ProGuard configuration for Rhino JavaScript engine and scripting APIs

# Preserve classes in javax.script
-keep class javax.script.** { *; }

# Preserve all classes from org.mozilla.javascript (Rhino)
-keep class org.mozilla.javascript.** { *; }

# Optional: If using Nashorn (for Java 8 environments)
-keep class jdk.nashorn.** { *; }

# Optional: For dynamic linking support (Nashorn/Dynalink)
-keep class jdk.dynalink.** { *; }

# Optional: Internal JDK modules (use with caution – may not be necessary in Android)
-keep class jdk.internal.** { *; }

# Optional: JavaBeans classes, used in some scripting contexts
-keep class java.beans.** { *; }

# Preserve line number information for better stack traces
-keepattributes SourceFile,LineNumberTable

# Optional: Hide source file name (obfuscate)
#-renamesourcefileattribute SourceFile

# If you're using WebView with JavaScript interface
#-keepclassmembers class your.package.name.YourWebViewJSInterface {
#    public *;
#}

# Avoid obfuscating Rhino or scripting classes to prevent reflection issues
-dontwarn org.mozilla.javascript.**
-dontwarn javax.script.**
-dontwarn jdk.nashorn.**
-dontwarn jdk.dynalink.**
-dontwarn java.beans.**