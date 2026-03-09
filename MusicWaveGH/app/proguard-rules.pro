-keep class com.musicwave.app.MainActivity$AndroidBridge { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
