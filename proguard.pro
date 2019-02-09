-dontobfuscate
-dontoptimize
-dontwarn scala.**
-dontwarn com.intellij.uiDesigner.core.**
-dontwarn org.jetbrains.**

-keep class org.psliwa.idea.composerJson.**
-keepclassmembers class org.psliwa.idea.composerJson.** {
    public <init>(...);
}
