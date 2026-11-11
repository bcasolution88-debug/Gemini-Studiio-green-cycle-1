# Flutter 引擎与插件
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.** { *; }
-keep class io.flutter.util.** { *; }
-keep class io.flutter.view.** { *; }
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }
-dontwarn io.flutter.embedding.**

# Google Play Core（Flutter deferred components 引用，本应用未使用）
-dontwarn com.google.android.play.core.**

# 保留注解与行号（便于崩溃定位）
-keepattributes *Annotation*,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
