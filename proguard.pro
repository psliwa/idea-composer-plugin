-dontobfuscate
-dontoptimize
-dontwarn scala.**
-dontwarn com.intellij.uiDesigner.core.**
-dontwarn org.jetbrains.**
-dontwarn com.intellij.memory.**
-dontwarn javax.xml.bind.ModuleUtil
-dontwarn module-info
-dontwarn one.util.streamex.**

-keep class org.psliwa.idea.composerJson.**
-keepclassmembers class org.psliwa.idea.composerJson.** {
    public <init>(...);
}
